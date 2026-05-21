package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Sensor {
    private final double anguloRelativo;
    private final Pista pista;
    private double ultimaDistancia;
    private double ultimoX;
    private double ultimoY;
    private double origenX;
    private double origenY;
    private static final double MAX_DISTANCIA = 300.0;
    private boolean metaDetectada;

    public Sensor(double anguloRelativo, Pista pista) {
        this.anguloRelativo = anguloRelativo;
        this.pista = pista;
        this.ultimaDistancia = MAX_DISTANCIA;
        this.ultimoX = 0;
        this.ultimoY = 0;
        this.origenX = 0;
        this.origenY = 0;
        this.metaDetectada = false;
    }

    public double medirDistancia(double origenX, double origenY, double anguloVehiculo) {
        this.origenX = origenX;
        this.origenY = origenY;
        double anguloGrados = Math.toDegrees(anguloVehiculo) + anguloRelativo;
        double anguloRad = Math.toRadians(anguloGrados);
        double dx = Math.cos(anguloRad);
        double dy = Math.sin(anguloRad);

        double distancia = 0;
        double x = origenX;
        double y = origenY;
        ultimoX = x;
        ultimoY = y;
        metaDetectada = false;

        while (distancia < MAX_DISTANCIA) {
            x += dx * 2;
            y += dy * 2;
            distancia += 2.0;

            if (pista != null && pista.dentroLimites(x, y)) {
                if (pista.esPared(x, y)) {
                    break;
                } else if (pista.esMeta(x, y)) {
                    metaDetectada = true;
                    break;
                }
            } else {
                break;
            }
        }

        ultimaDistancia = distancia;
        ultimoX = x;
        ultimoY = y;
        return distancia / MAX_DISTANCIA;
    }

    public boolean isMetaDetectada() {
        return metaDetectada;
    }

    public void render(GraphicsContext gc) {
        gc.setStroke(Color.YELLOW);
        gc.strokeLine(origenX, origenY, ultimoX, ultimoY);
    }

    public double getUltimaDistancia() {
        return ultimaDistancia / MAX_DISTANCIA;
    }
}
