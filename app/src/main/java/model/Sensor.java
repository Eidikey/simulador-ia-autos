package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.io.InputStream;

public class Sensor {
    private final double anguloRelativo;
    private double ultimaDistancia;
    private double ultimoX;
    private double ultimoY;
    private double origenX;
    private double origenY;
    private static final Image PISTA;
    private static final PixelReader PIXEL_READER;
    private static final double MAX_DISTANCIA = 300.0;

    static {
        Image img = null;
        PixelReader pr = null;
        try (InputStream is = Sensor.class.getResourceAsStream("/pista.png")) {
            if (is != null) {
                img = new Image(is, 800, 600, false, true);
                pr = img.getPixelReader();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar pista.png: " + e.getMessage());
        }
        PISTA = img;
        PIXEL_READER = pr;
    }

    public Sensor(double anguloRelativo) {
        this.anguloRelativo = anguloRelativo;
        this.ultimaDistancia = MAX_DISTANCIA;
        this.ultimoX = 0;
        this.ultimoY = 0;
        this.origenX = 0;
        this.origenY = 0;
    }

    public double medirDistancia(double origenX, double origenY, double anguloVehiculo) {
        this.origenX = origenX;
        this.origenY = origenY;
        double anguloGrados = anguloVehiculo + anguloRelativo;
        double anguloRad = Math.toRadians(anguloGrados);
        double dx = Math.cos(anguloRad);
        double dy = Math.sin(anguloRad);

        double distancia = 0;
        double x = origenX;
        double y = origenY;
        ultimoX = x;
        ultimoY = y;

        while (distancia < MAX_DISTANCIA) {
            x += dx * 2;
            y += dy * 2;
            distancia += 2.0;

            if (PIXEL_READER != null && x >= 0 && x < PISTA.getWidth() && y >= 0 && y < PISTA.getHeight()) {
                Color pixelColor = PIXEL_READER.getColor((int) x, (int) y);
                if (pixelColor.getRed() == 0 && pixelColor.getGreen() == 0 && pixelColor.getBlue() == 0) {
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

    public void render(GraphicsContext gc) {
        gc.setStroke(Color.YELLOW);
        gc.strokeLine(origenX, origenY, ultimoX, ultimoY);
    }

    public double getUltimaDistancia() {
        return ultimaDistancia / MAX_DISTANCIA;
    }
}
