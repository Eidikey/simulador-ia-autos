package model;

import controller.ControladorJugador;
import controller.GestorEntradas;

public class Jugador {
    private final Vehiculo vehiculo;
    private final ControladorJugador controlador;
    private boolean activo;
    private double inicioX;
    private double inicioY;

    public Jugador(double x, double y, Pista pista, GestorEntradas input) {
        this.controlador = new ControladorJugador(input);
        this.vehiculo = new Vehiculo(x, y, 40, 20, controlador, pista);
        this.activo = true;
        this.inicioX = x;
        this.inicioY = y;
    }

    public void update() {
        if (!activo) return;
        vehiculo.update();
        if (!vehiculo.isVivo()) {
            activo = false;
        }
    }

    public void reiniciar(double x, double y, double angulo) {
        this.inicioX = x;
        this.inicioY = y;
        this.activo = true;
        vehiculo.reset(x, y, angulo);
    }

    public Vehiculo getVehiculo() { return vehiculo; }
    public boolean isActivo() { return activo; }
    public ControladorJugador getControlador() { return controlador; }
}
