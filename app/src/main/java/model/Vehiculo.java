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
    private boolean haCruzadoMeta;

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
        this.haCruzadoMeta = false;

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
        boolean metaDetectada = false;

        for (int i = 0; i < sensores.size(); i++) {
            double dist = sensores.get(i).medirDistancia(x, y, angulo);
            inputs[i] = dist;
            if (sensores.get(i).isMetaDetectada() && dist < 5.0 / 300.0) {
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
        fitness = distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3);

        if (rotacionAcumulada > Math.toRadians(360) && distanciaAvance < 50) {
            vivo = false;
        }

        for (Sensor s : sensores) {
            if (s.getUltimaDistancia() <= 0.01) {
                vivo = false;
                break;
            }
        }

        if (!vivo) return;

        if (chocaPared(x, y, nuevoX, nuevoY)) {
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
        if (Sensor.PIXEL_READER == null) return false;

        double dx = xFin - xInicio;
        double dy = yFin - yInicio;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        int pasos = (int) (distancia / 2) + 1;

        for (int paso = 0; paso <= pasos; paso++) {
            double t = paso / (double) pasos;
            double px = xInicio + dx * t;
            double py = yInicio + dy * t;

            int[] cornersX = {(int)px, (int)(px + ancho), (int)px, (int)(px + ancho)};
            int[] cornersY = {(int)py, (int)(py + alto), (int)(py + alto), (int)py};

            for (int i = 0; i < 4; i++) {
                int cx = cornersX[i];
                int cy = cornersY[i];
                if (cx >= 0 && cx < Sensor.PISTA.getWidth() && cy >= 0 && cy < Sensor.PISTA.getHeight()) {
                    javafx.scene.paint.Color c = Sensor.PIXEL_READER.getColor(cx, cy);
                    if (c.getRed() == 0 && c.getGreen() == 0 && c.getBlue() == 0) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
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
    public double getFitness() {
        if (haCruzadoMeta) {
            return fitness + 50000;
        }
        return fitness;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setAngulo(double a) { this.angulo = a; }
    public double getAngulo() { return angulo; }
    public boolean haCruzadoMeta() { return haCruzadoMeta; }
}
