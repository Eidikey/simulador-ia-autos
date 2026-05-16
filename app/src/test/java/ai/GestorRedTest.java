package ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.File;
import java.io.FileWriter;
import static org.junit.jupiter.api.Assertions.*;

public class GestorRedTest {

    @BeforeEach
    void setUp() {
        File archivo1 = new File("app/src/main/resources/mejor_red.json");
        File archivo2 = new File("src/main/resources/mejor_red.json");
        if (archivo1.exists()) archivo1.delete();
        if (archivo2.exists()) archivo2.delete();
    }

    @AfterEach
    void tearDown() {
        File archivo = new File("app/src/main/resources/mejor_red.json");
        if (!archivo.exists()) {
            try (FileWriter fw = new FileWriter(archivo)) {
                fw.write("[-0.4489755410504741,-1.562628563295476,0.7377171524929116,-0.0170545456380371,0.95521096251723,4.897292852685808E-4,3.546450183623903,0.6899680536636648,0.8223979462616675,0.5844416813478057,-0.35913100060498815,1.2984412858144785,-0.6016627933469614,-0.34940887761468,-0.47617160367582123,-0.8383235871869228,1.4256579580692632,-1.7006113271403762,-0.9546995224483918,-1.3568506060480328,-0.16913235003460758,-0.5810186576417429,1.338207695007551,-0.803417359318251,-1.4618409662603715,1.3102633564204613,2.302088829047633,0.8936816113336765]");
            } catch (Exception e) {
                System.err.println("Error restoring mejor_red.json: " + e.getMessage());
            }
        }
    }

    @Test
    void testGuardarYCargarRed() {
        RedNeuronal redOriginal = new RedNeuronal(6, 4, 2);
        GestorRed.guardarRed(redOriginal);

        RedNeuronal redCargada = GestorRed.cargarMejorRed();

        assertNotNull(redCargada);
        double[] pesosOriginales = redOriginal.getPesosComoArray();
        double[] pesosCargados = redCargada.getPesosComoArray();

        assertEquals(pesosOriginales.length, pesosCargados.length);
        assertArrayEquals(pesosOriginales, pesosCargados, 0.0001);
    }

    @Test
    void testCargarRedInexistente() {
        RedNeuronal red = GestorRed.cargarMejorRed();
        assertNull(red);
    }
}
