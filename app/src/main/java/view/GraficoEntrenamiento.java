package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

public class GraficoEntrenamiento {
    private final List<Double> historial = new ArrayList<>();
    private static final int MAX_PUNTOS = 150;
    private static final int PADDING = 40;
    private static final double W = 350;
    private static final double H = 200;

    public void registrarFitness(double fitness) {
        historial.add(fitness);
        if (historial.size() > MAX_PUNTOS) {
            historial.remove(0);
        }
    }

    public void limpiar() {
        historial.clear();
    }

    public void dibujar(GraphicsContext gc, double x, double y, double ancho, double alto) {
        if (historial.size() < 2) {
            gc.setFill(Color.DARKGRAY);
            gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 12));
            gc.fillText("Grafico: esperando datos...", x, y + 20);
            return;
        }

        gc.setFill(Color.color(0.1, 0.1, 0.1, 0.85));
        gc.fillRect(x, y, W, H);

        double maxFit = historial.stream().mapToDouble(d -> d).max().orElse(1);
        if (maxFit == 0) maxFit = 1.0;
        double minFit = historial.stream().mapToDouble(d -> d).min().orElse(0);
        double rango = Math.max(maxFit - minFit, 1);

        gc.setStroke(Color.rgb(0, 255, 0, 0.2));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, W, H);

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(1.5);

        for (int i = 0; i < historial.size() - 1; i++) {
            double pX1 = x + PADDING + (i * (W - 2 * PADDING) / Math.max(1, historial.size() - 1));
            double pY1 = y + H - PADDING - ((historial.get(i) - minFit) / rango) * (H - 2 * PADDING);
            double pX2 = x + PADDING + ((i + 1) * (W - 2 * PADDING) / Math.max(1, historial.size() - 1));
            double pY2 = y + H - PADDING - ((historial.get(i + 1) - minFit) / rango) * (H - 2 * PADDING);
            gc.strokeLine(pX1, pY1, pX2, pY2);
        }

        gc.setFill(Color.rgb(0, 255, 0, 0.7));
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 10));
        gc.fillText(String.format("%.0f", maxFit), x + W - PADDING, y + PADDING - 2);
        gc.fillText(String.format("%.0f", minFit), x + W - PADDING, y + H - PADDING + 12);
        gc.fillText("Gen: " + historial.size(), x + PADDING - 30, y + PADDING - 2);
    }
}
