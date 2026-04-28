package engine;

import ai.GestorRed;
import ai.Poblacion;
import ai.ControladorIA;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.util.Objects;
import model.Vehiculo;

public class Simulador {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Image imagenPista;
    private Poblacion poblacion;
    private AnimationTimer timer;

    public Simulador(Canvas canvas, Scene scene) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        imagenPista = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pista.png")), 800, 600, false, true);

        poblacion = new Poblacion(400, 400);

        initTimer();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.G) {
                Vehiculo mejor = poblacion.getVehiculos().get(0);
                GestorRed.guardarRed(((ControladorIA) mejor.getControladorIA()).getRed());
                System.out.println("Red guardada manualmente!");
            }
        });
    }

    private void initTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                gc.drawImage(imagenPista, 0, 0, canvas.getWidth(), canvas.getHeight());

                poblacion.update();
                poblacion.render(gc);

                renderHUD();

                if (poblacion.todosMuertos()) {
                    poblacion.siguienteGeneracion();
                }
            }
        };
    }

    private void renderHUD() {
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(18));
        gc.fillText("Generacion: " + poblacion.getGeneracion(), 20, 30);
        gc.fillText("Autos Vivos: " + poblacion.getVehiculosVivos(), 20, 50);
        gc.fillText("Mejor Fitness: " + Math.round(poblacion.getMejorFitness()), 20, 70);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
