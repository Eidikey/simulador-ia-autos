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
    static final PixelReader LECTOR_PIXELES;
    static final double MAX_DISTANCIA = 420.0;
    private static double metaX = -1;
    private static double metaY = -1;
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
        LECTOR_PIXELES = pr;
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

            if (LECTOR_PIXELES != null && x >= 0 && x < PISTA.getWidth() && y >= 0 && y < PISTA.getHeight()) {
                Color colorPixel = LECTOR_PIXELES.getColor((int) x, (int) y);
                // Wall detection (black) - more lenient
                if (colorPixel.getRed() < 0.1 && colorPixel.getGreen() < 0.1 && colorPixel.getBlue() < 0.1) {
                    break;
                } else if (colorPixel.getRed() > 0.9 && colorPixel.getGreen() < 0.1 && colorPixel.getBlue() < 0.1) {
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

    public boolean esMetaDetectada() {
        return metaDetectada;
    }

    public void render(GraphicsContext gc) {
        gc.setStroke(Color.YELLOW);
        gc.strokeLine(origenX, origenY, ultimoX, ultimoY);
    }

    public double getUltimaDistancia() {
        return ultimaDistancia / MAX_DISTANCIA;
    }

    public static double[] encontrarPuntoInicio() {
        if (LECTOR_PIXELES == null || PISTA == null) {
            System.err.println("ERROR: LECTOR_PIXELES o PISTA es null");
            return new double[]{400.0, 500.0};
        }

        double mejorAzul = 0;
        int mejorX = 400, mejorY = 500;
        int contador = 0;
        
        for (int y = 0; y < PISTA.getHeight(); y++) {
            for (int x = 0; x < PISTA.getWidth(); x++) {
                Color c = LECTOR_PIXELES.getColor(x, y);
                if (c.getBlue() > 0.5 && c.getRed() < 0.5 && c.getGreen() < 0.5) {
                    if (c.getBlue() > mejorAzul) {
                        mejorAzul = c.getBlue();
                        mejorX = x;
                        mejorY = y;
                    }
                    contador++;
                }
            }
        }
        
        if (contador > 0) {
            return new double[]{mejorX, mejorY};
        }
        
        return new double[]{400.0, 500.0};
    }

    public static double[] obtenerCoordenadasMeta() {
        if (metaX < 0 || metaY < 0) {
            encontrarPuntoMeta();
        }
        return new double[]{metaX, metaY};
    }

    private static void encontrarPuntoMeta() {
        if (LECTOR_PIXELES == null || PISTA == null) {
            System.err.println("ERROR: no se puede escanear la meta, LECTOR_PIXELES o PISTA es null");
            metaX = 400;
            metaY = 100;
            return;
        }

        double mejorRojo = 0;
        int mejorX = 400, mejorY = 100;

        for (int y = 0; y < PISTA.getHeight(); y++) {
            for (int x = 0; x < PISTA.getWidth(); x++) {
                Color c = LECTOR_PIXELES.getColor(x, y);
                if (c.getRed() > 0.9 && c.getGreen() < 0.1 && c.getBlue() < 0.1) {
                    if (c.getRed() > mejorRojo) {
                        mejorRojo = c.getRed();
                        mejorX = x;
                        mejorY = y;
                    }
                }
            }
        }

        metaX = mejorX;
        metaY = mejorY;
        System.out.println("Meta detectada en: (" + metaX + ", " + metaY + ")");
    }
}
