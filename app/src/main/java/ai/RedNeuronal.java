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
        Random aleatorio = new Random();
        pesosEntradaOculta = new double[numEntradas][numOcultas];
        pesosOcultaSalida = new double[numOcultas][numSalidas];

        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                pesosEntradaOculta[i][j] = aleatorio.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                pesosOcultaSalida[i][j] = aleatorio.nextDouble() * 2 - 1;
            }
        }
    }

    public double[] propagarHaciaAdelante(double[] entradas) {
        double[] capaOculta = new double[numOcultas];
        double[] salidas = new double[numSalidas];

        for (int j = 0; j < numOcultas; j++) {
            double suma = 0;
            for (int i = 0; i < numEntradas; i++) {
                suma += entradas[i] * pesosEntradaOculta[i][j];
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

    public void auditarTransformacionLineal(double[] entradas, double[] salidas) {
        System.out.printf("%n");
        System.out.printf("  ╔══════════════════════════════════════════════════════════════╗%n");
        System.out.printf("  ║       AUDITORÍA MATRICIAL — TRANSFORMACIÓN LINEAL           ║%n");
        System.out.printf("  ╚══════════════════════════════════════════════════════════════╝%n%n");

        System.out.printf("  [X] Vector de Entrada  (%d × 1)%n", numEntradas);
        String sepHorizX = "  ┌" + "──────────────┬".repeat(numEntradas - 1) + "──────────────┐%n";
        String sepMedioX = "  ├" + "──────────────┼".repeat(numEntradas - 1) + "──────────────┤%n";
        String sepFinX   = "  └" + "──────────────┴".repeat(numEntradas - 1) + "──────────────┘%n";
        System.out.printf(sepHorizX);
        System.out.printf("  │");
        for (int i = 0; i < numEntradas; i++) {
            System.out.printf("      %7.4f      │", entradas[i]);
        }
        System.out.printf("%n");
        System.out.printf(sepFinX);
        System.out.printf("%n");

        System.out.printf("  [W] Matriz de Pesos (Capa Entrada→Oculta)  (%d × %d)%n", numEntradas, numOcultas);
        String sepHorizW = "  ┌" + "──────────────┬".repeat(numOcultas - 1) + "──────────────┐%n";
        String sepMedioW = "  ├" + "──────────────┼".repeat(numOcultas - 1) + "──────────────┤%n";
        String sepFinW   = "  └" + "──────────────┴".repeat(numOcultas - 1) + "──────────────┘%n";
        System.out.printf(sepHorizW);
        for (int i = 0; i < numEntradas; i++) {
            if (i > 0) System.out.printf(sepMedioW);
            System.out.printf("  │");
            for (int j = 0; j < numOcultas; j++) {
                System.out.printf("      %7.4f      │", pesosEntradaOculta[i][j]);
            }
            System.out.printf("%n");
        }
        System.out.printf(sepFinW);
        System.out.printf("%n");

        System.out.printf("  [Y] Vector de Salida  (%d × 1)%n", numSalidas);
        String sepHorizY = "  ┌" + "──────────────┬".repeat(numSalidas - 1) + "──────────────┐%n";
        String sepFinY   = "  └" + "──────────────┴".repeat(numSalidas - 1) + "──────────────┘%n";
        System.out.printf(sepHorizY);
        System.out.printf("  │");
        for (int i = 0; i < numSalidas; i++) {
            System.out.printf("      %7.4f      │", salidas[i]);
        }
        System.out.printf("%n");
        System.out.printf(sepFinY);
        System.out.printf("%n");

        System.out.printf("  Operación:  Y = σ(X · W₁) · W₂   (escalada a [-1, 1])%n");
        System.out.printf("  ─────────────────────────────────────────────────────%n%n");
    }

    public double[] getPesosComoArray() {
        int total = (numEntradas * numOcultas) + (numOcultas * numSalidas);
        double[] arreglo = new double[total];
        int indice = 0;

        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                arreglo[indice++] = pesosEntradaOculta[i][j];
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                arreglo[indice++] = pesosOcultaSalida[i][j];
            }
        }
        return arreglo;
    }

    public void setPesosDesdeArray(double[] arreglo) {
        int indice = 0;
        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                pesosEntradaOculta[i][j] = arreglo[indice++];
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                pesosOcultaSalida[i][j] = arreglo[indice++];
            }
        }
    }

    private double sigmoide(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public void mutar(double tasaMutacion) {
        Random aleatorio = new Random();
        for (int i = 0; i < numEntradas; i++) {
            for (int j = 0; j < numOcultas; j++) {
                if (aleatorio.nextDouble() < tasaMutacion) {
                    pesosEntradaOculta[i][j] += (aleatorio.nextDouble() - 0.5) * 3.0;
                }
            }
        }
        for (int i = 0; i < numOcultas; i++) {
            for (int j = 0; j < numSalidas; j++) {
                if (aleatorio.nextDouble() < tasaMutacion) {
                    pesosOcultaSalida[i][j] += (aleatorio.nextDouble() - 0.5) * 3.0;
                }
            }
        }
    }

    public int getNumEntradas() { return numEntradas; }
    public int getNumOcultas() { return numOcultas; }
    public int getNumSalidas() { return numSalidas; }
}
