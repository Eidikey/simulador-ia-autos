package ai;

import com.google.gson.Gson;
import java.io.*;

public class GestorRed {
    private static final String RUTA = "/home/alex/Projects/simulador-ai-autos/mejor_red.json";
    private static final Gson gson = new Gson();

    public static void guardarRed(RedNeuronal red) {
        try {
            File archivo = new File(RUTA);
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
        File archivo = new File(RUTA);
        if (!archivo.exists()) {
            System.out.println("No se encontro red previa. Se generara una nueva.");
            return null;
        }
        try (Reader reader = new FileReader(archivo)) {
            double[] pesos = gson.fromJson(reader, double[].class);
            RedNeuronal red = new RedNeuronal(5, 4, 2);
            red.setPesosDesdeArray(pesos);
            System.out.println("Red pre-entrenada cargada exitosamente desde " + archivo.getAbsolutePath());
            return red;
        } catch (IOException e) {
            System.err.println("Error al cargar red: " + e.getMessage());
        }
        return null;
    }
}
