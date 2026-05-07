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
    static final Image PISTA;
    static final PixelReader PIXEL_READER;
    private static final double MAX_DISTANCIA = 300.0;
    private boolean metaDetectada;

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
        this.metaDetectada = false;
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
        metaDetectada = false;

        while (distancia < MAX_DISTANCIA) {
            x += dx * 2;
            y += dy * 2;
            distancia += 2.0;

            if (PIXEL_READER != null && x >= 0 && x < PISTA.getWidth() && y >= 0 && y < PISTA.getHeight()) {
                Color pixelColor = PIXEL_READER.getColor((int) x, (int) y);
                // Wall detection (black) - more lenient
                if (pixelColor.getRed() < 0.1 && pixelColor.getGreen() < 0.1 && pixelColor.getBlue() < 0.1) {
                    break;
                } else if (pixelColor.getRed() > 0.9 && pixelColor.getGreen() < 0.1 && pixelColor.getBlue() < 0.1) {
                    // Finish line (red)
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

    public static double[] encontrarSpawnPoint() {
        if (PIXEL_READER == null || PISTA == null) {
            System.err.println("ERROR: PIXEL_READER o PISTA es null");
            return new double[]{400.0, 500.0};
        }

        // Search for BLUE spawn line pixels - search ENTIRE image
        double bestBlue = 0;
        int bestX = 400, bestY = 500;
        int count = 0;
        
        // Search entire image for blue pixels (spawn line)
        for (int y = 0; y < PISTA.getHeight(); y++) {
            for (int x = 0; x < PISTA.getWidth(); x++) {
                Color c = PIXEL_READER.getColor(x, y);
                // Blue spawn: B > 0.5 and R < 0.5 and G < 0.5
                if (c.getBlue() > 0.5 && c.getRed() < 0.5 && c.getGreen() < 0.5) {
                    if (c.getBlue() > bestBlue) {
                        bestBlue = c.getBlue();
                        bestX = x;
                        bestY = y;
                    }
                    count++;
                }
            }
        }
        
        if (count > 0) {
            // Use the bluest pixel as spawn point
            return new double[]{bestX, bestY};
        }
        
        // Last resort: use a reasonable default
        return new double[]{400.0, 500.0};
    }
}
