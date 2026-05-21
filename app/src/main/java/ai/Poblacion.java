package ai;

import model.Pista;
import model.Vehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Poblacion {
  private final List<Vehiculo> vehiculos;
  private final int tamanoPoblacion;
  private final Pista pista;
  private int generacion;
  private int vehiculosVivos;
  private double mejorFitness;
  private Random rand;
  private double spawnX = 400.0;
  private double spawnY = 500.0;
  private double recordFitness = 0;
  private int generacionesEstancadas = 0;

  public Poblacion(Pista pista, double inicioX, double inicioY) {
    this.pista = pista;
    this.tamanoPoblacion = 50;
    this.generacion = 1;
    this.vehiculos = new ArrayList<>();
    this.rand = new Random();

    double[] spawnPoint = pista.encontrarSpawnPoint();
    this.spawnX = spawnPoint[0] - 20;
    this.spawnY = spawnPoint[1] - 10;
    this.recordFitness = 0;

    System.out.println("Spawn dinamico detectado en: (" + spawnX + ", " + spawnY + ")");

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
      Vehiculo v = new Vehiculo(x, y, 40, 20, controlador, pista);
      v.setAngulo(-Math.PI / 2);
      v.setControladorIA(controlador);
      vehiculos.add(v);
    }
    vehiculosVivos = tamanoPoblacion;
    mejorFitness = 0;
  }

  private void crearPoblacionConRed(double x, double y, RedNeuronal redBase) {
    ControladorIA ciElite = new ControladorIA(redBase);
    Vehiculo elite = new Vehiculo(x, y, 40, 20, ciElite, pista);
    elite.setAngulo(-Math.PI / 2);
    elite.setControladorIA(ciElite);
    vehiculos.add(elite);

    for (int i = 1; i < tamanoPoblacion; i++) {
      RedNeuronal redClon = new RedNeuronal(5, 4, 2);
      redClon.setPesosDesdeArray(redBase.getPesosComoArray());
      redClon.mutar(0.1);
      ControladorIA ci = new ControladorIA(redClon);
      Vehiculo v = new Vehiculo(x, y, 40, 20, ci, pista);
      v.setAngulo(-Math.PI / 2);
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

  public boolean todosMuertos() {
    return vehiculosVivos == 0;
  }

  public void siguienteGeneracion() {
    evaluarFitness();

    if (mejorFitness > recordFitness) {
      recordFitness = mejorFitness;
      generacionesEstancadas = 0;
      GestorRed.guardarRed(((ControladorIA) vehiculos.get(0).getControladorIA()).getRed());
      System.out.println("Nuevo record! Fitness: " + recordFitness + " (Guardado automatico)");
    } else {
      generacionesEstancadas++;
    }

    double[] spawnPoint = pista.encontrarSpawnPoint();
    this.spawnX = spawnPoint[0] - 20;
    this.spawnY = spawnPoint[1] - 10;

    List<Vehiculo> nuevaGeneracion = new ArrayList<>();

    crossover(nuevaGeneracion);
    double tasaMut = generacionesEstancadas > 15 ? 0.15 : 0.08;
    mutacion(nuevaGeneracion, tasaMut);
    if (generacionesEstancadas > 15) {
        inyectarAleatorios(nuevaGeneracion, 5);
        generacionesEstancadas = 0;
    }

    vehiculos.clear();
    vehiculos.addAll(nuevaGeneracion);
    vehiculosVivos = vehiculos.size();
    generacion++;
    mejorFitness = 0;
  }

  private void evaluarFitness() {
    vehiculos.sort((v1, v2) -> Double.compare(v2.getFitness(), v1.getFitness()));
  }

  private void crossover(List<Vehiculo> nuevaGeneracion) {
    for (int e = 0; e < Math.min(3, vehiculos.size()); e++) {
      Vehiculo top = vehiculos.get(e);
      RedNeuronal redElite = new RedNeuronal(5, 4, 2);
      redElite.setPesosDesdeArray(((ControladorIA) top.getControladorIA()).getRed().getPesosComoArray());
      ControladorIA ciElite = new ControladorIA(redElite);
      Vehiculo elite = new Vehiculo(spawnX, spawnY, 40, 20, ciElite, pista);
      elite.setAngulo(-Math.PI / 2);
      elite.setControladorIA(ciElite);
      nuevaGeneracion.add(elite);
    }

    for (int i = 3; i < tamanoPoblacion; i++) {
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
      ControladorIA ci = new ControladorIA(redHijo);
      Vehiculo nv = new Vehiculo(spawnX, spawnY, 40, 20, ci, pista);
      nv.setAngulo(-Math.PI / 2);
      nv.setControladorIA(ci);
      nuevaGeneracion.add(nv);
    }
  }

  private void mutacion(List<Vehiculo> poblacion, double tasaMutacion) {
    for (int i = 3; i < poblacion.size(); i++) {
      ControladorIA ci = (ControladorIA) poblacion.get(i).getControladorIA();
      ci.getRed().mutar(tasaMutacion);
    }
  }

  private Vehiculo seleccionarPadre() {
    Vehiculo mejor = null;
    for (int t = 0; t < 3; t++) {
      Vehiculo candidato = vehiculos.get(rand.nextInt(vehiculos.size()));
      if (mejor == null || candidato.getFitness() > mejor.getFitness()) {
        mejor = candidato;
      }
    }
    return mejor;
  }

  public int getGeneracion() {
    return generacion;
  }

  public int getVehiculosVivos() {
    return vehiculosVivos;
  }

  public int getTotal() {
    return tamanoPoblacion;
  }

  public double getMejorFitness() {
    return mejorFitness;
  }

  public List<Vehiculo> getVehiculos() {
    return vehiculos;
  }

  private void inyectarAleatorios(List<Vehiculo> poblacion, int cantidad) {
    int inicio = poblacion.size() - cantidad;
    for (int i = inicio; i < poblacion.size(); i++) {
      RedNeuronal redNueva = new RedNeuronal(5, 4, 2);
      ControladorIA ci = new ControladorIA(redNueva);
      Vehiculo v = new Vehiculo(spawnX, spawnY, 40, 20, ci, pista);
      v.setAngulo(-Math.PI / 2);
      v.setControladorIA(ci);
      poblacion.set(i, v);
    }
    System.out.println("Inyectados " + cantidad + " individuos aleatorios por estancamiento.");
  }
}
