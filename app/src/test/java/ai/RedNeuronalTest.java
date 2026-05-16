package ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RedNeuronalTest {

    @Test
    void testPropagarHaciaAdelante() {
        RedNeuronal red = new RedNeuronal(6, 4, 2);
        double[] entradas = {0.5, 0.3, 0.8, 0.1, 0.9, 0.7};
        double[] salidas = red.propagarHaciaAdelante(entradas);
        
        assertEquals(2, salidas.length);
        for (double salida : salidas) {
            assertTrue(salida >= -1 && salida <= 1);
        }
    }

    @Test
    void testGetPesosComoArray() {
        RedNeuronal red = new RedNeuronal(6, 4, 2);
        double[] pesos = red.getPesosComoArray();
        
        int expectedSize = (6 * 4) + (4 * 2);
        assertEquals(expectedSize, pesos.length);
    }

    @Test
    void testSetPesosDesdeArray() {
        RedNeuronal red = new RedNeuronal(6, 4, 2);
        double[] pesosOriginales = red.getPesosComoArray();
        
        RedNeuronal red2 = new RedNeuronal(6, 4, 2);
        red2.setPesosDesdeArray(pesosOriginales);
        
        double[] pesosRecuperados = red2.getPesosComoArray();
        assertArrayEquals(pesosOriginales, pesosRecuperados, 0.0001);
    }

    @Test
    void testMutar() {
        RedNeuronal red = new RedNeuronal(6, 4, 2);
        double[] pesosAntes = red.getPesosComoArray().clone();
        red.mutar(1.0);
        double[] pesosDespues = red.getPesosComoArray();
        
        boolean diferente = false;
        for (int i = 0; i < pesosAntes.length; i++) {
            if (Math.abs(pesosAntes[i] - pesosDespues[i]) > 0.001) {
                diferente = true;
                break;
            }
        }
        assertTrue(diferente);
    }
}
