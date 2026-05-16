package ai;

import model.Controlador;

public class ControladorIA implements Controlador {
    private final RedNeuronal red;
    private double[] ultimasSalidas;
    private double[] ultimasEntradas;

    public ControladorIA(RedNeuronal red) {
        this.red = red;
        this.ultimasSalidas = new double[]{0, 0};
    }

    public double obtenerGiro() {
        return ultimasSalidas.length > 0 ? ultimasSalidas[0] : 0;
    }

    public double obtenerAceleracion() {
        if (ultimasSalidas.length < 2) return 0;
        double raw = ultimasSalidas[1];
        return (raw - 0.5) * 2;
    }

    public void procesar(double[] entradas) {
        this.ultimasEntradas = entradas.clone();
        ultimasSalidas = red.propagarHaciaAdelante(entradas);
    }

    public void imprimirAuditoriaMatricial() {
        if (ultimasEntradas == null || ultimasSalidas == null) {
            System.out.println("  [Auditoría] No hay datos de la última propagación.");
            return;
        }
        red.auditarTransformacionLineal(ultimasEntradas, ultimasSalidas);
    }

    public RedNeuronal getRed() {
        return red;
    }
}
