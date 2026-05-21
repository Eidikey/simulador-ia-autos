package model;

import ai.ControladorIA;

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
    private boolean vivo;
    private double fitness;
    private int framesBajaVelocidad;
    private boolean haCruzadoMeta;
    private int framesVivo;
    private final Pista pista;

    public Vehiculo(double x, double y, double ancho, double alto, Controlador controlador, Pista pista) {
        super(x, y, ancho, alto);
        this.pista = pista;
        this.velocidad = 0.5;
        this.angulo = 0;
        this.controlador = controlador;
        this.distanciaRecorrida = 0;
        this.startX = x;
        this.startY = y;
        this.rotacionAcumulada = 0;
        this.vivo = true;
        this.fitness = 0;
        this.sensores = new ArrayList<>();
        this.framesBajaVelocidad = 0;
        this.haCruzadoMeta = false;
        this.framesVivo = 0;

        double[] angulos = {-90, -45, 0, 45, 90};
        for (double a : angulos) {
            sensores.add(new Sensor(a, pista));
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

        framesVivo++;

        double[] inputs = new double[sensores.size()];
        boolean metaDetectada = false;

        for (int i = 0; i < sensores.size(); i++) {
            double dist = sensores.get(i).medirDistancia(x, y, angulo);
            inputs[i] = dist;
            if (sensores.get(i).isMetaDetectada() && dist < 3.0 / 300.0 && framesVivo > 30) {
                metaDetectada = true;
            }
        }

        if (metaDetectada) {
            haCruzadoMeta = true;
            velocidad = 0;
            return;
        }

        if (controladorIA != null) {
            controladorIA.procesar(inputs);
        }

        double aceleracion = controlador.obtenerAceleracion();
        double giro = controlador.obtenerGiro();

        if (controladorIA != null) {
            velocidad += aceleracion + 0.1;
        } else {
            velocidad += aceleracion;
            if (velocidad < 0) velocidad = 0;
        }
        angulo += giro;
        rotacionAcumulada += Math.abs(giro);

        double nuevoX = x + velocidad * Math.cos(angulo);
        double nuevoY = y + velocidad * Math.sin(angulo);

        if (velocidad != 0) {
            double avance = Math.sqrt(Math.pow(nuevoX - x, 2) + Math.pow(nuevoY - y, 2));
            distanciaRecorrida += avance;
        }

        fitness = distanciaRecorrida + (velocidad * 5);

        if (rotacionAcumulada > Math.toRadians(360) && distanciaRecorrida < 50) {
            vivo = false;
        }

        if (framesVivo > 5 && chocaPared(x, y, nuevoX, nuevoY)) {
            vivo = false;
            return;
        }

        x = nuevoX;
        y = nuevoY;

        if (velocidad < 0.5) {
            framesBajaVelocidad++;
            if (framesBajaVelocidad > 60) {
                vivo = false;
            }
        } else {
            framesBajaVelocidad = 0;
        }
    }

    private boolean chocaPared(double xInicio, double yInicio, double xFin, double yFin) {
        if (pista == null) return false;
        return pista.hayColisionEnTrayecto(xInicio, yInicio, xFin, yFin, ancho, alto);
    }

    public boolean isVivo() { return vivo; }
    public double getDistanciaRecorrida() { return distanciaRecorrida; }
    public void setFitness(double f) { fitness = f; }
    public double getFitness() {
        if (haCruzadoMeta) {
            return fitness + 50000;
        }
        return fitness;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public void setAngulo(double a) { this.angulo = a; }
    public double getAngulo() { return angulo; }
    public boolean haCruzadoMeta() { return haCruzadoMeta; }
    public List<Sensor> getSensores() { return sensores; }

    public void reset(double nuevaX, double nuevaY, double nuevoAngulo) {
        this.x = nuevaX;
        this.y = nuevaY;
        this.angulo = nuevoAngulo;
        this.velocidad = 0.5;
        this.distanciaRecorrida = 0;
        this.rotacionAcumulada = 0;
        this.framesBajaVelocidad = 0;
        this.fitness = 0;
        this.vivo = true;
        this.haCruzadoMeta = false;
        this.framesVivo = 0;
        this.startX = nuevaX;
        this.startY = nuevaY;
    }
}
