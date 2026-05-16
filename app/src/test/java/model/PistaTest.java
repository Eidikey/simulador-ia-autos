package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PistaTest {
    private static Pista pista;

    @BeforeAll
    static void setUp() {
        pista = new Pista();
    }

    @Test
    void testPistaSeCarga() {
        assertNotNull(pista);
        assertNotNull(pista.getImagen());
        assertTrue(pista.getAncho() > 0);
        assertTrue(pista.getAlto() > 0);
    }

    @Test
    void testEncontrarSpawnPointDevuelveCoordenasValidas() {
        double[] spawn = pista.encontrarSpawnPoint();
        assertEquals(2, spawn.length);
        assertTrue(spawn[0] >= 0 && spawn[0] < pista.getAncho());
        assertTrue(spawn[1] >= 0 && spawn[1] < pista.getAlto());
    }

    @Test
    void testDentroLimites() {
        assertTrue(pista.dentroLimites(400, 300));
        assertFalse(pista.dentroLimites(-1, 300));
        assertFalse(pista.dentroLimites(400, -1));
    }

    @Test
    void testFueraDeLimitesEsPared() {
        assertTrue(pista.esPared(-5, 300));
        assertTrue(pista.esPared(400, -5));
    }

    @Test
    void testSpawnPointEsTransitable() {
        double[] spawn = pista.encontrarSpawnPoint();
        assertTrue(pista.esTransitable(spawn[0], spawn[1]),
            "Spawn point deberia estar en zona transitable");
    }

    @Test
    void testSpawnEsTransitable() {
        double[] spawn = pista.encontrarSpawnPoint();
        boolean colision = pista.hayColisionEnTrayecto(
            spawn[0], spawn[1], spawn[0], spawn[1] - 5, 10, 5);
        assertFalse(colision,
            "Spawn deberia ser transitable sin colisiones inmediatas");
    }
}
