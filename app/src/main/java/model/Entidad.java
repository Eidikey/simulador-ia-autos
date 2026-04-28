package model;

import javafx.scene.canvas.GraphicsContext;

public abstract class Entidad {
    protected double x;
    protected double y;
    protected double ancho;
    protected double alto;

    public Entidad(double x, double y, double ancho, double alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    public abstract void update();

    public abstract void render(GraphicsContext gc);
}
