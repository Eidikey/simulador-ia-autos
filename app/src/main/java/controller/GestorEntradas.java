package controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class GestorEntradas {
    private final Set<KeyCode> presionadas = new HashSet<>();

    public GestorEntradas(Scene scene) {
        scene.setOnKeyPressed(e -> presionadas.add(e.getCode()));
        scene.setOnKeyReleased(e -> presionadas.remove(e.getCode()));
    }

    public boolean izquierda()  { return presionadas.contains(KeyCode.LEFT) || presionadas.contains(KeyCode.A); }
    public boolean derecha()    { return presionadas.contains(KeyCode.RIGHT) || presionadas.contains(KeyCode.D); }
    public boolean arriba()     { return presionadas.contains(KeyCode.UP) || presionadas.contains(KeyCode.W); }
    public boolean abajo()      { return presionadas.contains(KeyCode.DOWN) || presionadas.contains(KeyCode.S); }
    public boolean enter()         { return presionadas.contains(KeyCode.ENTER); }
    public boolean teclaG()        { return presionadas.contains(KeyCode.G); }
    public boolean teclaM()        { return presionadas.contains(KeyCode.M); }
    public boolean teclaR()        { return presionadas.contains(KeyCode.R); }
    public boolean teclaE()        { return presionadas.contains(KeyCode.E); }
    public boolean teclaC()        { return presionadas.contains(KeyCode.C); }

    public boolean menuArriba()    { return presionadas.contains(KeyCode.UP); }
    public boolean menuAbajo()     { return presionadas.contains(KeyCode.DOWN); }

    public void limpiar() { presionadas.clear(); }
}
