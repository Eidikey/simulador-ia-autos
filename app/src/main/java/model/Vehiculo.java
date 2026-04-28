package model;

import ai.ControladorIA;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Vehiculo extends Entidad {
    private double velocidad;
    private double angulo;
    private final Controlador controlador;
    private ControladorIA controladorIA;
    private final List<Sensor> sensores;
    private double distanciaRecorrida;
    private double startX;
    private double startY;
    private double rotacionAcumulada;
    private double ultimoX;
    private double ultimoY;
    private boolean vivo;
    private double fitness;
    private int framesBajaVelocidad;

    public Vehiculo(double x, double y, double ancho, double alto, Controlador controlador) {
        super(x, y, ancho, alto);
        this.velocidad = 0.5;
        this.angulo = 0;
        this.controlador = controlador;
        this.distanciaRecorrida = 0;
        this.startX = x;
        this.startY = y;
        this.rotacionAcumulada = 0;
        this.ultimoX = x;
        this.ultimoY = y;
        this.vivo = true;
        this.fitness = 0;
        this.sensores = new ArrayList<>();
        this.framesBajaVelocidad = 0;

        double[] angulos = {-90, -45, 0, 45, 90};
        for (double a : angulos) {
            sensores.add(new Sensor(a));
        }
    }

    public void setControladorIA(ControladorIA cia) {
        this.controladorIA = cia;
    }

    public ControladorIA getControladorIA() {
        return controladorIA;
    }

    @Override
    public void update() {
        if (!vivo) return;

        double[] inputs = new double[sensores.size()];
        for (int i = 0; i < sensores.size(); i++) {
            double dist = sensores.get(i).medirDistancia(x, y, angulo);
            inputs[i] = dist;
        }

        if (controladorIA != null) {
            controladorIA.procesar(inputs);
        }

        double aceleracion = controlador.obtenerAceleracion();
        double giro = controlador.obtenerGiro();

        velocidad += aceleracion + 0.1;
        angulo += giro;
        rotacionAcumulada += Math.abs(giro);

        double nuevoX = x + velocidad * Math.cos(angulo);
        double nuevoY = y + velocidad * Math.sin(angulo);

        if (velocidad != 0) {
            double avance = Math.sqrt(Math.pow(nuevoX - x, 2) + Math.pow(nuevoY - y, 2));
            distanciaRecorrida += avance;
        }

        double distanciaAvance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        fitness = distanciaAvance - (rotacionAcumulada * 0.5);

        if (rotacionAcumulada > Math.toRadians(360) && distanciaAvance < 50) {
            vivo = false;
        }

        ultimoX = x;
        ultimoY = y;
        x = nuevoX;
        y = nuevoY;

        for (Sensor s : sensores) {
            if (s.getUltimaDistancia() <= 0.01) {
                vivo = false;
                break;
            }
        }

        if (x < 0 || x > 800 || y < 0 || y > 600) {
            vivo = false;
        }

        if (velocidad < 0.5) {
            framesBajaVelocidad++;
            if (framesBajaVelocidad > 60) {
                vivo = false;
            }
        } else {
            framesBajaVelocidad = 0;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!vivo) return;

        gc.setFill(Color.web("#00FFCC"));
        gc.fillRect(x, y, ancho, alto);

        for (Sensor s : sensores) {
            s.render(gc);
        }
    }

    public boolean isVivo() { return vivo; }
    public double getDistanciaRecorrida() { return distanciaRecorrida; }
    public void setFitness(double f) { fitness = f; }
    public double getFitness() { return fitness; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setAngulo(double a) { this.angulo = a; }

    public double getAngulo() { return angulo; }
}
