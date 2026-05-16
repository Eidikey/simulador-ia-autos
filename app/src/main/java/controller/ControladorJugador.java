package controller;

import model.Controlador;

public class ControladorJugador implements Controlador {
    private final GestorEntradas input;

    public ControladorJugador(GestorEntradas input) {
        this.input = input;
    }

    @Override
    public double obtenerGiro() {
        double giro = 0;
        if (input.izquierda()) giro -= 0.05;
        if (input.derecha()) giro += 0.05;
        return giro;
    }

    @Override
    public double obtenerAceleracion() {
        double acel = 0;
        if (input.arriba()) acel += 0.15;
        if (input.abajo()) acel -= 0.2;
        return acel;
    }
}
