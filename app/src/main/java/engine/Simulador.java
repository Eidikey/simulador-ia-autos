package engine;

import ai.GestorRed;
import ai.Poblacion;
import ai.ControladorIA;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;
import model.Vehiculo;
import model.Sensor;

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

        double[] spawnPoint = Sensor.encontrarSpawnPoint();
        System.out.println("Spawn dinamico detectado en: (" + spawnPoint[0] + ", " + spawnPoint[1] + ")");
        poblacion = new Poblacion(spawnPoint[0], spawnPoint[1]);

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

                if (poblacion.todosMuertos()) {
                    poblacion.siguienteGeneracion();
                }

                gc.setEffect(null);
                gc.setGlobalAlpha(1.0);
                gc.setStroke(Color.YELLOW);
                gc.setFill(Color.YELLOW);
                gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
                gc.fillText("GEN: " + poblacion.getGeneracion(), 700, 20);
                gc.fillText("VIVOS: " + poblacion.getVehiculosVivos(), 700, 40);
                gc.fillText("FIT: " + Math.round(poblacion.getMejorFitness()), 700, 60);
            }
        };
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
