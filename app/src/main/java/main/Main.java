package main;

import engine.Simulador;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        Scene scene = new Scene(new Group(canvas));
        Simulador simulador = new Simulador(canvas, scene);

        stage.setScene(scene);
        stage.setTitle("Simulador de Autos Autonomos");
        stage.show();

        simulador.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
