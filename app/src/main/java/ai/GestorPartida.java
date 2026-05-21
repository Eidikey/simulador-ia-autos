package ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.PartidaGuardada;

import java.io.*;
import java.nio.file.*;

public class GestorPartida {
    private static final String DIRECTORIO = "partidas";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void guardar(PartidaGuardada partida) {
        try {
            Path dir = Paths.get(DIRECTORIO);
            Files.createDirectories(dir);

            String nombre = "partida_" + System.currentTimeMillis() + ".json";
            Path archivo = dir.resolve(nombre);

            try (Writer writer = Files.newBufferedWriter(archivo)) {
                gson.toJson(partida, writer);
                System.out.println("Partida guardada en " + archivo.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
        }
    }
}
