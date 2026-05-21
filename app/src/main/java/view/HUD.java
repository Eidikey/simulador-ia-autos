package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HUD {

    public void dibujarEntrenamiento(GraphicsContext gc, int generacion, int vivos, double fitness) {
        gc.setEffect(null);
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.fillText("GEN: " + generacion, 700, 20);
        gc.fillText("VIVOS: " + vivos, 700, 40);
        gc.fillText("FIT: " + Math.round(fitness), 700, 60);
    }

    public void dibujarMenu(GraphicsContext gc, String titulo, int opcionSeleccionada, String[] opciones) {
        gc.setEffect(null);
        gc.setGlobalAlpha(1.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 32));
        gc.fillText(titulo, 150, 120);

        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
        int y = 220;
        for (int i = 0; i < opciones.length; i++) {
            if (i == opcionSeleccionada) {
                gc.setFill(Color.CYAN);
                gc.fillText("> " + opciones[i], 250, y);
            } else {
                gc.setFill(Color.GRAY);
                gc.fillText("  " + opciones[i], 250, y);
            }
            y += 45;
        }

        gc.setFill(Color.DARKGRAY);
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 14));
        gc.fillText("FLECHAS: Navegar  |  < >: Dificultad  |  ENTER: Ok  |  M: Menu", 150, 520);
        gc.fillText("E: Entrenar  |  C: Competir", 200, 540);
    }

    public void dibujarCarrera(GraphicsContext gc, double progresoJugador, double progresoIA,
                               boolean jugadorVivo, boolean iaVivo) {
        gc.setEffect(null);
        gc.setGlobalAlpha(1.0);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(Color.WHITE);
        gc.fillText("CARRERA: JUGADOR vs IA", 20, 30);

        gc.setFill(Color.RED);
        gc.fillText(String.format("JUGADOR: %.0fpx %s", progresoJugador, jugadorVivo ? "" : "(MUERTO)"), 20, 60);

        gc.setFill(Color.CYAN);
        gc.fillText(String.format("IA:      %.0fpx %s", progresoIA, iaVivo ? "" : "(MUERTO)"), 20, 85);

        gc.setFill(Color.YELLOW);
        if (!jugadorVivo && !iaVivo) {
            gc.fillText("AMBOS MUERTOS", 20, 110);
        } else if (!jugadorVivo) {
            gc.fillText("PERDIENDO (IA ganando)", 20, 110);
        } else if (!iaVivo) {
            gc.fillText("GANANDO (IA muerta)", 20, 110);
        } else if (progresoJugador > progresoIA) {
            gc.fillText("GANANDO", 20, 110);
        } else if (progresoIA > progresoJugador) {
            gc.fillText("PERDIENDO", 20, 110);
        } else {
            gc.fillText("EMPATE", 20, 110);
        }

        gc.setFill(Color.DARKGRAY);
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 12));
        gc.fillText("M: Volver al menu  |  R: Reiniciar", 20, 570);
    }

    public void dibujarResultadoConReintento(GraphicsContext gc, String resultado,
                                             double distJugador, double distIA,
                                             boolean metaJugador, boolean metaIA,
                                             long framesRestantes) {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, 800, 600);

        gc.setFill(resultado.toUpperCase().contains("GANASTE") ? Color.LIME : Color.RED);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
        gc.fillText(resultado, 180, 200);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
        gc.fillText("ESTADISTICAS", 300, 270);

        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 18));
        gc.setFill(Color.RED);
        gc.fillText(String.format("Jugador: %.0fpx %s", distJugador, metaJugador ? "(CRUZO META)" : ""), 250, 310);
        gc.setFill(Color.CYAN);
        gc.fillText(String.format("IA:      %.0fpx %s", distIA, metaIA ? "(CRUZO META)" : ""), 250, 340);

        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.fillText("FIN DE CARRERA", 300, 400);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 16));
        gc.fillText("R: Reintentar  |  G: Guardar partida  |  M: Menu", 180, 440);
    }
}
