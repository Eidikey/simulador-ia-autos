package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.Vehiculo;
import java.util.List;

public class Renderizador {
    private final HUD hud;
    private final RenderizadorVehiculo renderVehiculo;
    private final GraficoEntrenamiento grafico;

    public Renderizador() {
        this.hud = new HUD();
        this.renderVehiculo = new RenderizadorVehiculo();
        this.grafico = new GraficoEntrenamiento();
    }

    public void limpiar(GraphicsContext gc, double ancho, double alto) {
        gc.clearRect(0, 0, ancho, alto);
    }

    public void dibujarFondoMenu(GraphicsContext gc, double ancho, double alto) {
        gc.setFill(Color.web("#1e1e2e"));
        gc.fillRect(0, 0, ancho, alto);
    }

    public void dibujarPista(GraphicsContext gc, Image pista, double ancho, double alto) {
        gc.drawImage(pista, 0, 0, ancho, alto);
    }

    public void dibujarVehiculo(GraphicsContext gc, Vehiculo v, Color color) {
        renderVehiculo.dibujar(gc, v, color);
    }

    public void dibujarSensores(GraphicsContext gc, Vehiculo v) {
        renderVehiculo.dibujarSensores(gc, v);
    }

    public void dibujarTrayectoria(GraphicsContext gc, List<double[]> puntos, Color color) {
        renderVehiculo.dibujarTrayectoria(gc, puntos, color);
    }

    public void dibujarHUDEntrenamiento(GraphicsContext gc, int generacion, int vivos, double fitness) {
        hud.dibujarEntrenamiento(gc, generacion, vivos, fitness);
    }

    public void dibujarMenu(GraphicsContext gc, String titulo, int opcionSeleccionada, String[] opciones) {
        hud.dibujarMenu(gc, titulo, opcionSeleccionada, opciones);
    }

    public void dibujarHUDCarrera(GraphicsContext gc, double progresoJugador, double progresoIA,
                                  boolean jugadorVivo, boolean iaVivo) {
        hud.dibujarCarrera(gc, progresoJugador, progresoIA, jugadorVivo, iaVivo);
    }

    public void dibujarResultadoConReintento(GraphicsContext gc, String resultado,
                                             double distJugador, double distIA,
                                             boolean metaJugador, boolean metaIA,
                                             long framesRestantes) {
        hud.dibujarResultadoConReintento(gc, resultado, distJugador, distIA, metaJugador, metaIA, framesRestantes);
    }

    public void dibujarGrafico(GraphicsContext gc, double x, double y, double ancho, double alto) {
        grafico.dibujar(gc, x, y, ancho, alto);
    }

    public GraficoEntrenamiento getGrafico() { return grafico; }
}
