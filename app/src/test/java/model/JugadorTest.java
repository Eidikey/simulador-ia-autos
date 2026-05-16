package model;

import ai.ControladorIA;
import ai.RedNeuronal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JugadorTest {
    private static Pista pista;

    @BeforeAll
    static void setUp() {
        pista = new Pista();
    }

    @Test
    void testJugadorVehiculoSeCreaConControladorIA() {
        RedNeuronal red = new RedNeuronal(5, 4, 2);
        ControladorIA ci = new ControladorIA(red);
        Vehiculo v = new Vehiculo(400, 400, 40, 20, ci, pista);
        v.setControladorIA(ci);
        assertNotNull(v);
        assertTrue(v.isVivo());
    }

    @Test
    void testPosicionInicialVehiculo() {
        RedNeuronal red = new RedNeuronal(5, 4, 2);
        ControladorIA ci = new ControladorIA(red);
        Vehiculo v = new Vehiculo(150, 250, 40, 20, ci, pista);
        v.setControladorIA(ci);
        assertEquals(150, v.getX(), 0.001);
        assertEquals(250, v.getY(), 0.001);
    }

    @Test
    void testJugadorVehiculoEsVivoAlInicio() {
        RedNeuronal red = new RedNeuronal(5, 4, 2);
        ControladorIA ci = new ControladorIA(red);
        Vehiculo v = new Vehiculo(400, 450, 40, 20, ci, pista);
        v.setControladorIA(ci);
        assertTrue(v.isVivo());
    }
}
