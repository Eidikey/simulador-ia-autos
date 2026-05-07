package ai;

import com.google.gson.Gson;
import java.io.*;

public class GestorRed {
    private static final String RUTA_ARCHIVO;
    private static final Gson gson = new Gson();

    static {
        File archivo = new File("app/src/main/resources/mejor_red.json");
        if (archivo.exists() || new File("app").exists()) {
            RUTA_ARCHIVO = "app/src/main/resources/mejor_red.json";
        } else {
            RUTA_ARCHIVO = "src/main/resources/mejor_red.json";
        }
    }

    public static void guardarRed(RedNeuronal red) {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            archivo.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(archivo)) {
                double[] pesos = red.getPesosComoArray();
                gson.toJson(pesos, writer);
                System.out.println("Red guardada exitosamente en " + archivo.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar red: " + e.getMessage());
        }
    }

    public static RedNeuronal cargarMejorRed() {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.exists()) {
            try (Reader reader = new FileReader(archivo)) {
                double[] pesos = gson.fromJson(reader, double[].class);
                RedNeuronal red = new RedNeuronal(5, 4, 2);
                red.setPesosDesdeArray(pesos);
                System.out.println("Red pre-entrenada cargada desde " + archivo.getAbsolutePath());
                return red;
            } catch (IOException e) {
                System.err.println("Error al cargar red: " + e.getMessage());
            }
        }
        return null;
    }
}
