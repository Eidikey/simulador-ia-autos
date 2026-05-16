package model;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.io.InputStream;

public class Pista {
    private final Image imagen;
    private final PixelReader pixelReader;
    private final double ancho;
    private final double alto;
    private final double startX;
    private final double startY;

    public Pista() {
        try (InputStream is = getClass().getResourceAsStream("/pista.png")) {
            if (is == null) {
                throw new RuntimeException("No se encontro pista.png en resources");
            }
            this.imagen = new Image(is, 800, 600, false, true);
            this.pixelReader = imagen.getPixelReader();
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar pista.png", e);
        }
        this.ancho = imagen.getWidth();
        this.alto = imagen.getHeight();
        double[] spawn = encontrarSpawnPoint();
        this.startX = spawn[0];
        this.startY = spawn[1];
    }

    public Image getImagen() { return imagen; }
    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }

    public boolean dentroLimites(double x, double y) {
        return x >= 0 && x < ancho && y >= 0 && y < alto;
    }

    public boolean esPared(double x, double y) {
        if (!dentroLimites(x, y)) return true;
        Color c = pixelReader.getColor((int) x, (int) y);
        return c.getRed() < 0.1 && c.getGreen() < 0.1 && c.getBlue() < 0.1;
    }

    public boolean esMeta(double x, double y) {
        if (!dentroLimites(x, y)) return false;
        Color c = pixelReader.getColor((int) x, (int) y);
        return c.getRed() > 0.9 && c.getGreen() < 0.1 && c.getBlue() < 0.1;
    }

    public boolean esSpawn(double x, double y) {
        if (!dentroLimites(x, y)) return false;
        Color c = pixelReader.getColor((int) x, (int) y);
        return c.getBlue() > 0.5 && c.getRed() < 0.5 && c.getGreen() < 0.5;
    }

    public boolean esTransitable(double x, double y) {
        return dentroLimites(x, y) && !esPared(x, y);
    }

    public boolean hayColisionEnTrayecto(double x1, double y1, double x2, double y2,
                                          double anchoV, double altoV) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        int pasos = Math.max((int) (distancia / 2) + 1, 1);

        for (int paso = 0; paso <= pasos; paso++) {
            double t = (double) paso / pasos;
            double px = x1 + dx * t;
            double py = y1 + dy * t;

            if (!esTransitable(px, py) ||
                !esTransitable(px + anchoV, py) ||
                !esTransitable(px, py + altoV) ||
                !esTransitable(px + anchoV, py + altoV)) {
                return true;
            }
        }
        return false;
    }

    public double[] encontrarSpawnPoint() {
        if (pixelReader == null) {
            return new double[]{400.0, 500.0};
        }

        double bestBlue = 0;
        int spawnX = 400, spawnY = 500;
        int count = 0;

        for (int y = 0; y < (int) alto; y++) {
            for (int x = 0; x < (int) ancho; x++) {
                Color c = pixelReader.getColor(x, y);
                if (c.getBlue() > 0.5 && c.getRed() < 0.5 && c.getGreen() < 0.5) {
                    if (c.getBlue() > bestBlue) {
                        bestBlue = c.getBlue();
                        spawnX = x;
                        spawnY = y;
                    }
                    count++;
                }
            }
        }

        if (count == 0) return new double[]{400.0, 500.0};

        int left = spawnX;
        while (left > 0 && !esPared(left - 1, spawnY)) left--;
        int right = spawnX;
        while (right < (int) ancho - 1 && !esPared(right + 1, spawnY)) right++;

        double centerX = (left + right) / 2.0;
        return new double[]{centerX, (double) spawnY};
    }
}
