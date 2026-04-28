package ai;

import java.util.Random;

public class RedNeuronal {
    private final int numEntradas;
    private final int numOcultas;
    private final int numSalidas;
    private double[][] pesosEntradaOculta;
    private double[][] pesosOcultaSalida;

    public RedNeuronal(int numEntradas, int numOcultas, int numSalidas) {
        this.numEntradas = numEntradas;
        this.numOcultas = numOcultas;
        this.numSalidas = numSalidas;
        inicializarPesos();
    }

    private void inicializarPesos() {
        Random rand = new Random();
        pesosEntradaOculta = new double[numEntradas][numOcultas];
        pesosOcultaSalida = new double[numOcultas][numSalidas];

        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                pesosEntradaOculta[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                pesosOcultaSalida[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
    }

    public double[] feedForward(double[] inputs) {
        double[] capaOculta = new double[numOcultas];
        double[] salidas = new double[numSalidas];

        for (int j = 0; j < numOcultas; j++) {
            double suma = 0;
            for (int i = 0; i < numEntradas; i++) {
                suma += inputs[i] * pesosEntradaOculta[i][j];
            }
            capaOculta[j] = sigmoide(suma);
        }

        for (int j = 0; j < numSalidas; j++) {
            double suma = 0;
            for (int i = 0; i < numOcultas; i++) {
                suma += capaOculta[i] * pesosOcultaSalida[i][j];
            }
            salidas[j] = sigmoide(suma) * 2 - 1;
        }

        return salidas;
    }

    public double[] getPesosComoArray() {
        int total = (numEntradas * numOcultas) + (numOcultas * numSalidas);
        double[] array = new double[total];
        int idx = 0;

        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                array[idx++] = pesosEntradaOculta[i][j];
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                array[idx++] = pesosOcultaSalida[i][j];
            }
        }
        return array;
    }

    public void setPesosDesdeArray(double[] array) {
        int idx = 0;
        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                pesosEntradaOculta[i][j] = array[idx++];
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                pesosOcultaSalida[i][j] = array[idx++];
            }
        }
    }

    private double sigmoide(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public void mutar(double tasaMutacion) {
        Random rand = new Random();
        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                if (rand.nextDouble() < tasaMutacion) {
                    pesosEntradaOculta[i][j] += (rand.nextDouble() - 0.5) * 2;
                }
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                if (rand.nextDouble() < tasaMutacion) {
                    pesosOcultaSalida[i][j] += (rand.nextDouble() - 0.5) * 2;
                }
            }
        }
    }

    public int getNumEntradas() { return numEntradas; }
    public int getNumOcultas() { return numOcultas; }
    public int getNumSalidas() { return numSalidas; }
}
