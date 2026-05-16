package ai;

import model.Controlador;
import model.Sensor;
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
    private double mejorAptitud;
    private Random aleatorio;
    private double inicioX = 400.0;
    private double inicioY = 500.0;
    private double recordAptitud = 0;
    private int generacionesEstancadas = 0;

    public Poblacion(double inicioX, double inicioY) {
        this.tamanoPoblacion = 50;
        this.generacion = 1;
        this.vehiculos = new ArrayList<>();
        this.aleatorio = new Random();

        double[] puntoInicio = Sensor.encontrarPuntoInicio();
        this.inicioX = puntoInicio[0];
        this.inicioY = puntoInicio[1];
        this.recordAptitud = 0;

        System.out.println("Inicio dinamico detectado en: (" + inicioX + ", " + inicioY + ")");

        RedNeuronal redCargada = GestorRed.cargarMejorRed();
        if (redCargada != null) {
            System.out.println("Red pre-entrenada cargada exitosamente.");
            crearPoblacionConRed(inicioX, inicioY, redCargada);
        } else {
            crearPoblacion(inicioX, inicioY);
        }
    }

    private void crearPoblacion(double x, double y) {
        for (int i = 0; i < tamanoPoblacion; i++) {
            RedNeuronal red = new RedNeuronal(6, 4, 2);
            ControladorIA controlador = new ControladorIA(red);
            Vehiculo v = new Vehiculo(x, y, 40, 20, controlador);
            v.setControladorIA(controlador);
            vehiculos.add(v);
        }
        vehiculosVivos = tamanoPoblacion;
        mejorAptitud = 0;
    }

    private void crearPoblacionConRed(double x, double y, RedNeuronal redBase) {
        ControladorIA ciElite = new ControladorIA(redBase);
        Vehiculo elite = new Vehiculo(x, y, 40, 20, ciElite);
        elite.setAngulo(0);
        elite.setControladorIA(ciElite);
        vehiculos.add(elite);

        for (int i = 1; i < tamanoPoblacion; i++) {
            RedNeuronal redClon = new RedNeuronal(6, 4, 2);
            redClon.setPesosDesdeArray(redBase.getPesosComoArray());
            redClon.mutar(0.1);
            ControladorIA ci = new ControladorIA(redClon);
            Vehiculo v = new Vehiculo(x, y, 40, 20, ci);
            v.setAngulo(0);
            v.setControladorIA(ci);
            vehiculos.add(v);
        }
        vehiculosVivos = tamanoPoblacion;
        mejorAptitud = 0;
    }

    public void actualizar() {
        vehiculosVivos = 0;
        for (Vehiculo v : vehiculos) {
            if (v.estaVivo()) {
                v.actualizar();
                vehiculosVivos++;
                if (v.getDistanciaRecorrida() > mejorAptitud) {
                    mejorAptitud = v.getDistanciaRecorrida();
                }
            }
        }
    }

    public void dibujar(javafx.scene.canvas.GraphicsContext gc) {
        for (Vehiculo v : vehiculos) {
            if (v.estaVivo()) {
                v.dibujar(gc);
            }
        }
    }

    public boolean todosMuertos() {
        return vehiculosVivos == 0;
    }

    public void siguienteGeneracion() {
        evaluarAptitud();

        if (mejorAptitud > recordAptitud) {
            recordAptitud = mejorAptitud;
            generacionesEstancadas = 0;
            GestorRed.guardarRed(((ControladorIA) vehiculos.get(0).getControladorIA()).getRed());
            System.out.println("Nuevo record! Aptitud: " + recordAptitud + " (Guardado automatico)");
        } else {
            generacionesEstancadas++;
        }

        double[] puntoInicio = Sensor.encontrarPuntoInicio();
        this.inicioX = puntoInicio[0];
        this.inicioY = puntoInicio[1];

        List<Vehiculo> nuevaGeneracion = new ArrayList<>();

        int numElite = (int) Math.ceil(tamanoPoblacion * 0.10);
        int numCaos = (int) Math.floor(tamanoPoblacion * 0.20);
        int numRecombinacion = tamanoPoblacion - numElite - numCaos;

        agregarElites(nuevaGeneracion, numElite);
        cruzar(nuevaGeneracion, numRecombinacion);
        agregarInmigrantes(nuevaGeneracion, numCaos);

        vehiculos.clear();
        vehiculos.addAll(nuevaGeneracion);
        vehiculosVivos = vehiculos.size();
        generacion++;
        mejorAptitud = 0;
    }

    private void evaluarAptitud() {
        for (Vehiculo v : vehiculos) {
            v.establecerAptitud(v.getDistanciaRecorrida());
        }
        vehiculos.sort((v1, v2) -> Double.compare(v2.obtenerAptitud(), v1.obtenerAptitud()));
    }

    private void agregarElites(List<Vehiculo> lista, int cantidad) {
        for (int i = 0; i < cantidad && i < vehiculos.size(); i++) {
            Vehiculo original = vehiculos.get(i);
            RedNeuronal redClon = new RedNeuronal(6, 4, 2);
            redClon.setPesosDesdeArray(((ControladorIA) original.getControladorIA()).getRed().getPesosComoArray());
            ControladorIA ci = new ControladorIA(redClon);
            Vehiculo elite = new Vehiculo(inicioX, inicioY, 40, 20, ci);
            elite.setAngulo(0);
            elite.setControladorIA(ci);
            lista.add(elite);
        }
    }

    private void cruzar(List<Vehiculo> lista, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Vehiculo padre1 = seleccionarPadre();
            Vehiculo padre2 = seleccionarPadre();

            double[] pesos1 = ((ControladorIA) padre1.getControladorIA()).getRed().getPesosComoArray();
            double[] pesos2 = ((ControladorIA) padre2.getControladorIA()).getRed().getPesosComoArray();
            double[] hijo = new double[pesos1.length];

            int puntoCruce = aleatorio.nextInt(pesos1.length);
            for (int j = 0; j < pesos1.length; j++) {
                hijo[j] = j < puntoCruce ? pesos1[j] : pesos2[j];
            }

            RedNeuronal redHijo = new RedNeuronal(6, 4, 2);
            redHijo.setPesosDesdeArray(hijo);
            redHijo.mutar(0.1);
            ControladorIA ci = new ControladorIA(redHijo);
            Vehiculo vehiculoNuevo = new Vehiculo(inicioX, inicioY, 40, 20, ci);
            vehiculoNuevo.setAngulo(0);
            vehiculoNuevo.setControladorIA(ci);
            lista.add(vehiculoNuevo);
        }
    }

    private void agregarInmigrantes(List<Vehiculo> lista, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            RedNeuronal redNueva = new RedNeuronal(6, 4, 2);
            ControladorIA ci = new ControladorIA(redNueva);
            Vehiculo inmigrante = new Vehiculo(inicioX, inicioY, 40, 20, ci);
            inmigrante.setAngulo(0);
            inmigrante.setControladorIA(ci);
            lista.add(inmigrante);
        }
    }

    private Vehiculo seleccionarPadre() {
        double aptitudTotal = vehiculos.stream().mapToDouble(Vehiculo::obtenerAptitud).sum();
        double r = aleatorio.nextDouble() * aptitudTotal;
        double acumulado = 0;
        for (Vehiculo v : vehiculos) {
            acumulado += v.obtenerAptitud();
            if (acumulado >= r) return v;
        }
        return vehiculos.get(0);
    }

    public int getGeneracion() { return generacion; }
    public int getVehiculosVivos() { return vehiculosVivos; }
    public int getTotal() { return tamanoPoblacion; }
    public double obtenerMejorAptitud() { return mejorAptitud; }
    public List<Vehiculo> getVehiculos() { return vehiculos; }
}
