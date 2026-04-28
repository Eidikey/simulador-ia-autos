package ai;

import model.Controlador;
import model.Vehiculo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Poblacion {
    private final List<Vehiculo> vehiculos;
    private final int tamanoPoblacion;
    private int generacion;
    private int vehiculosVivos;
    private double mejorFitness;
    private Random rand;
    private double spawnX = 400.0;
    private double spawnY = 500.0;
    private double recordFitness = 0;
    private int generacionesEstancadas = 0;

    public Poblacion(double inicioX, double inicioY) {
        this.tamanoPoblacion = 50;
        this.generacion = 1;
        this.vehiculos = new ArrayList<>();
        this.rand = new Random();
        this.spawnX = inicioX;
        this.spawnY = inicioY;
        this.recordFitness = 0;

        RedNeuronal redCargada = GestorRed.cargarMejorRed();
        if (redCargada != null) {
            System.out.println("Red pre-entrenada cargada exitosamente.");
            crearPoblacionConRed(spawnX, spawnY, redCargada);
        } else {
            crearPoblacion(spawnX, spawnY);
        }
    }

    private void crearPoblacion(double x, double y) {
        for (int i = 0; i < tamanoPoblacion; i++) {
            RedNeuronal red = new RedNeuronal(5, 4, 2);
            ControladorIA controlador = new ControladorIA(red);
            Vehiculo v = new Vehiculo(x, y, 40, 20, controlador);
            v.setControladorIA(controlador);
            vehiculos.add(v);
        }
        vehiculosVivos = tamanoPoblacion;
        mejorFitness = 0;
    }

    private void crearPoblacionConRed(double x, double y, RedNeuronal redBase) {
        ControladorIA ciElite = new ControladorIA(redBase);
        Vehiculo elite = new Vehiculo(x, y, 40, 20, ciElite);
        elite.setAngulo(0);
        elite.setControladorIA(ciElite);
        vehiculos.add(elite);

        for (int i = 1; i < tamanoPoblacion; i++) {
            RedNeuronal redClon = new RedNeuronal(5, 4, 2);
            redClon.setPesosDesdeArray(redBase.getPesosComoArray());
            redClon.mutar(0.1);
            ControladorIA ci = new ControladorIA(redClon);
            Vehiculo v = new Vehiculo(x, y, 40, 20, ci);
            v.setAngulo(0);
            v.setControladorIA(ci);
            vehiculos.add(v);
        }
        vehiculosVivos = tamanoPoblacion;
        mejorFitness = 0;
    }

    public void update() {
        vehiculosVivos = 0;
        for (Vehiculo v : vehiculos) {
            if (v.isVivo()) {
                v.update();
                vehiculosVivos++;
                if (v.getDistanciaRecorrida() > mejorFitness) {
                    mejorFitness = v.getDistanciaRecorrida();
                }
            }
        }
    }

    public void render(javafx.scene.canvas.GraphicsContext gc) {
        for (Vehiculo v : vehiculos) {
            if (v.isVivo()) {
                v.render(gc);
            }
        }
    }

    public boolean todosMuertos() {
        return vehiculosVivos == 0;
    }

    public void siguienteGeneracion() {
        evaluarFitness();

        if (mejorFitness > recordFitness) {
            recordFitness = mejorFitness;
            generacionesEstancadas = 0;
            GestorRed.guardarRed(((ControladorIA) vehiculos.get(0).getControladorIA()).getRed());
            System.out.println("Nuevo récord! Fitness: " + recordFitness + " (Guardado automatico)");
        } else {
            generacionesEstancadas++;
        }

        List<Vehiculo> nuevaGeneracion = new ArrayList<>();

        crossover(nuevaGeneracion);
        mutacion(nuevaGeneracion, generacionesEstancadas > 5 ? 0.4 : 0.1);

        vehiculos.clear();
        vehiculos.addAll(nuevaGeneracion);
        vehiculosVivos = vehiculos.size();
        generacion++;
        mejorFitness = 0;
    }

    private void evaluarFitness() {
        for (Vehiculo v : vehiculos) {
            v.setFitness(v.getDistanciaRecorrida());
        }
        vehiculos.sort((v1, v2) -> Double.compare(v2.getFitness(), v1.getFitness()));
    }

    private void crossover(List<Vehiculo> nuevaGeneracion) {
        Vehiculo mejor = vehiculos.get(0);
        RedNeuronal redElite = new RedNeuronal(5, 4, 2);
        redElite.setPesosDesdeArray(((ControladorIA) mejor.getControladorIA()).getRed().getPesosComoArray());
        ControladorIA ciElite = new ControladorIA(redElite);
        Vehiculo elite = new Vehiculo(spawnX, spawnY, 40, 20, ciElite);
        elite.setAngulo(0);
        elite.setControladorIA(ciElite);
        nuevaGeneracion.add(elite);

        for (int i = 1; i < tamanoPoblacion; i++) {
            Vehiculo padre1 = seleccionarPadre();
            Vehiculo padre2 = seleccionarPadre();

            double[] pesos1 = ((ControladorIA) padre1.getControladorIA()).getRed().getPesosComoArray();
            double[] pesos2 = ((ControladorIA) padre2.getControladorIA()).getRed().getPesosComoArray();
            double[] hijo = new double[pesos1.length];

            int puntoCruce = rand.nextInt(pesos1.length);
            for (int j = 0; j < pesos1.length; j++) {
                hijo[j] = j < puntoCruce ? pesos1[j] : pesos2[j];
            }

            RedNeuronal redHijo = new RedNeuronal(5, 4, 2);
            redHijo.setPesosDesdeArray(hijo);
            redHijo.mutar(0.1);
            ControladorIA ci = new ControladorIA(redHijo);
            Vehiculo nv = new Vehiculo(spawnX, spawnY, 40, 20, ci);
            nv.setAngulo(0);
            nv.setControladorIA(ci);
            nuevaGeneracion.add(nv);
        }
    }

    private void mutacion(List<Vehiculo> poblacion, double tasaMutacion) {
        for (int i = 1; i < poblacion.size(); i++) {
            ControladorIA ci = (ControladorIA) poblacion.get(i).getControladorIA();
            ci.getRed().mutar(tasaMutacion);
        }
    }

    private Vehiculo seleccionarPadre() {
        double totalFitness = vehiculos.stream().mapToDouble(Vehiculo::getFitness).sum();
        double r = rand.nextDouble() * totalFitness;
        double acumulado = 0;
        for (Vehiculo v : vehiculos) {
            acumulado += v.getFitness();
            if (acumulado >= r) return v;
        }
        return vehiculos.get(0);
    }

    public int getGeneracion() { return generacion; }
    public int getVehiculosVivos() { return vehiculosVivos; }
    public int getTotal() { return tamanoPoblacion; }
    public double getMejorFitness() { return mejorFitness; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
}
