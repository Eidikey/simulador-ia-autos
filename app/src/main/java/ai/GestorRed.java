package ai;

import com.google.gson.Gson;
import java.io.*;

public class GestorRed {
    private static final String RUTA_ARCHIVO;
    private static final Gson gson = new Gson();

    static {
        String[] candidatos = {
            "app/src/main/resources/mejor_red.json",
            "src/main/resources/mejor_red.json"
        };
        String rutaElegida = System.getProperty("user.home")
            + java.io.File.separator + ".simulador_ia"
            + java.io.File.separator + "mejor_red.json";

        for (String ruta : candidatos) {
            File f = new File(ruta);
            if (f.exists()) {
                rutaElegida = ruta;
                break;
            }
            if (f.getParentFile() != null && f.getParentFile().isDirectory()) {
                rutaElegida = ruta;
                break;
            }
        }
        RUTA_ARCHIVO = rutaElegida;
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
