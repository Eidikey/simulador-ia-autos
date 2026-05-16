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
    private double inicioX;
    private double inicioY;
    private double rotacionAcumulada;
    private double ultimoX;
    private double ultimoY;
    private boolean vivo;
    private double aptitud;
    private int framesBajaVelocidad;
    private boolean haCruzadoMeta;

    public Vehiculo(double x, double y, double ancho, double alto, Controlador controlador) {
        super(x, y, ancho, alto);
        this.velocidad = 0.5;
        this.angulo = 0;
        this.controlador = controlador;
        this.distanciaRecorrida = 0;
        this.inicioX = x;
        this.inicioY = y;
        this.rotacionAcumulada = 0;
        this.ultimoX = x;
        this.ultimoY = y;
        this.vivo = true;
        this.aptitud = 0;
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
    public void actualizar() {
        if (!vivo) return;

        double[] entradas = new double[sensores.size() + 1];
        boolean metaDetectada = false;

        for (int i = 0; i < sensores.size(); i++) {
            double dist = sensores.get(i).medirDistancia(x, y, angulo);
            entradas[i] = dist;
            if (sensores.get(i).esMetaDetectada() && dist < 5.0 / Sensor.MAX_DISTANCIA) {
                metaDetectada = true;
            }
        }

        if (metaDetectada) {
            haCruzadoMeta = true;
            velocidad = 0;
            return;
        }

        double[] meta = Sensor.obtenerCoordenadasMeta();
        double dxMeta = meta[0] - x;
        double dyMeta = meta[1] - y;
        double normMeta = Math.sqrt(dxMeta * dxMeta + dyMeta * dyMeta);
        double cosDireccion = (Math.cos(angulo) * dxMeta + Math.sin(angulo) * dyMeta) / normMeta;
        entradas[5] = cosDireccion;

        if (controladorIA != null) {
            controladorIA.procesar(entradas);
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

        double distanciaAvance = Math.sqrt(Math.pow(x - inicioX, 2) + Math.pow(y - inicioY, 2));
        double desplazamientoY = inicioY - y;
        double penalizacionRotacion = rotacionAcumulada * 0.8;
        double penalizacionQuieto = (framesBajaVelocidad > 20) ? (framesBajaVelocidad * 10.0) : 0.0;
        double penalizacionRetroceso = (desplazamientoY < 0) ? Math.abs(desplazamientoY) * 10.0 : 0.0;
        aptitud = (distanciaAvance * 2.0) + (velocidad * 15.0) + Math.max(desplazamientoY * 5.0, 0)
                - penalizacionRotacion - penalizacionQuieto - penalizacionRetroceso;

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
        if (Sensor.LECTOR_PIXELES == null) return false;

        double dx = xFin - xInicio;
        double dy = yFin - yInicio;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        int pasos = (int) (distancia / 2) + 1;

        for (int paso = 0; paso <= pasos; paso++) {
            double t = paso / (double) pasos;
            double px = xInicio + dx * t;
            double py = yInicio + dy * t;

            int[] esquinasX = {(int)px, (int)(px + ancho), (int)px, (int)(px + ancho)};
            int[] esquinasY = {(int)py, (int)(py + alto), (int)(py + alto), (int)py};

            for (int i = 0; i < 4; i++) {
                int cx = esquinasX[i];
                int cy = esquinasY[i];
                if (cx >= 0 && cx < Sensor.PISTA.getWidth() && cy >= 0 && cy < Sensor.PISTA.getHeight()) {
                    javafx.scene.paint.Color c = Sensor.LECTOR_PIXELES.getColor(cx, cy);
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
    public void dibujar(GraphicsContext gc) {
        if (!vivo) return;

        gc.setFill(Color.web("#00FFCC"));
        gc.fillRect(x, y, ancho, alto);

        for (Sensor s : sensores) {
            s.render(gc);
        }
    }

    public boolean estaVivo() { return vivo; }
    public double getDistanciaRecorrida() { return distanciaRecorrida; }
    public void establecerAptitud(double a) { aptitud = a; }
    public double obtenerAptitud() {
        if (haCruzadoMeta) {
            return aptitud + 50000;
        }
        return aptitud;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setAngulo(double a) { this.angulo = a; }
    public double getAngulo() { return angulo; }
    public boolean haCruzadoMeta() { return haCruzadoMeta; }
}
