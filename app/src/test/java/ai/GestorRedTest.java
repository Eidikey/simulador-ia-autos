package ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class GestorRedTest {

    @BeforeEach
    void setUp() {
        File archivo = new File(System.getProperty("user.home") + "/.simulador-ia-autos/mejor_red.json");
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    @AfterEach
    void tearDown() {
        File archivo = new File(System.getProperty("user.home") + "/.simulador-ia-autos/mejor_red.json");
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    @Test
    void testGuardarYCargarRed() {
        RedNeuronal redOriginal = new RedNeuronal(5, 4, 2);
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
