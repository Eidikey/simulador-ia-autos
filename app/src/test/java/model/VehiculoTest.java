package model;

import ai.ControladorIA;
import ai.RedNeuronal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehiculoTest {

    static class ControladorSimulado implements Controlador {
        private double aceleracion = 0;
        private double giro = 0;

        public double obtenerAceleracion() { return aceleracion; }
        public double obtenerGiro() { return giro; }

        public void setAceleracion(double a) { this.aceleracion = a; }
        public void setGiro(double g) { this.giro = g; }
    }

    @Test
    void testVehiculoMuereAlColisionar() {
        ControladorSimulado controlador = new ControladorSimulado();
        Vehiculo v = new Vehiculo(10, 10, 20, 10, controlador);

        RedNeuronal red = new RedNeuronal(6, 4, 2);
        ControladorIA cia = new ControladorIA(red);
        v.setControladorIA(cia);

        controlador.setAceleracion(0.5);
        v.actualizar();

        assertFalse(v.estaVivo());
    }

    @Test
    void testVehiculoTieneAptitudInicial() {
        ControladorSimulado controlador = new ControladorSimulado();
        Vehiculo v = new Vehiculo(100, 100, 20, 10, controlador);

        assertEquals(0, v.obtenerAptitud(), 0.001);
    }

    @Test
    void testVehiculoCruzaMeta() {
        ControladorSimulado controlador = new ControladorSimulado();
        Vehiculo v = new Vehiculo(100, 100, 20, 10, controlador);

        RedNeuronal red = new RedNeuronal(6, 4, 2);
        ControladorIA cia = new ControladorIA(red);
        v.setControladorIA(cia);

        v.actualizar();

        assertFalse(v.haCruzadoMeta());
    }
}
