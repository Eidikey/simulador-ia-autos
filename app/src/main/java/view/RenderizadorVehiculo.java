package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Vehiculo;
import java.util.List;

public class RenderizadorVehiculo {

    public void dibujar(GraphicsContext gc, Vehiculo v, Color color) {
        if (!v.isVivo()) return;
        gc.setFill(color);
        gc.save();
        gc.translate(v.getX() + v.getAncho() / 2, v.getY() + v.getAlto() / 2);
        gc.rotate(Math.toDegrees(v.getAngulo()));
        gc.fillRect(-v.getAncho() / 2, -v.getAlto() / 2, v.getAncho(), v.getAlto());
        gc.restore();
    }

    public void dibujarSensores(GraphicsContext gc, Vehiculo v) {
        v.getSensores().forEach(s -> s.render(gc));
    }

    public void dibujarTrayectoria(GraphicsContext gc, List<double[]> puntos, Color color) {
        if (puntos == null || puntos.size() < 2) return;
        gc.setStroke(color);
        gc.setLineWidth(1);
        for (int i = 0; i < puntos.size() - 1; i++) {
            double[] p1 = puntos.get(i);
            double[] p2 = puntos.get(i + 1);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
        }
    }
}
