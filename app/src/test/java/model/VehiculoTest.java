package model;

import ai.ControladorIA;
import ai.RedNeuronal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehiculoTest {

    static class MockControlador implements Controlador {
        private double aceleracion = 0;
        private double giro = 0;

        public double obtenerAceleracion() { return aceleracion; }
        public double obtenerGiro() { return giro; }

        public void setAceleracion(double a) { this.aceleracion = a; }
        public void setGiro(double g) { this.giro = g; }
    }

    @Test
    void testVehiculoMuereAlColisionar() {
        MockControlador controlador = new MockControlador();
        Vehiculo v = new Vehiculo(10, 10, 20, 10, controlador);

        RedNeuronal red = new RedNeuronal(5, 4, 2);
        ControladorIA cia = new ControladorIA(red);
        v.setControladorIA(cia);

        controlador.setAceleracion(0.5);
        v.update();

        assertFalse(v.isVivo());
    }

    @Test
    void testVehiculoTieneFitnessInicial() {
        MockControlador controlador = new MockControlador();
        Vehiculo v = new Vehiculo(100, 100, 20, 10, controlador);

        assertEquals(0, v.getFitness(), 0.001);
    }

    @Test
    void testVehiculoCruzaMeta() {
        MockControlador controlador = new MockControlador();
        Vehiculo v = new Vehiculo(100, 100, 20, 10, controlador);

        RedNeuronal red = new RedNeuronal(5, 4, 2);
        ControladorIA cia = new ControladorIA(red);
        v.setControladorIA(cia);

        v.update();

        assertFalse(v.haCruzadoMeta());
    }
}
