package engine;

import ai.GestorPartida;
import ai.GestorRed;
import ai.Poblacion;
import ai.ControladorIA;
import ai.RedNeuronal;
import controller.GestorEntradas;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import view.Renderizador;
import model.Jugador;
import model.PartidaGuardada;
import model.Pista;
import model.Vehiculo;

import java.util.ArrayList;
import java.util.List;

public class Simulador {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Pista pista;
    private final GestorEntradas input;
    private final Renderizador renderizador;
    private Poblacion poblacion;
    private Jugador jugador;
    private Vehiculo iaVehiculo;
    private AnimationTimer timer;

    private EstadoJuego estado = EstadoJuego.MENU;
    private int opcionMenu = 0;
    private static final String[] OPCIONES_MENU = {
        "ENTRENAR IA",
        "COMPETIR VS IA",
        "SALIR"
    };
    private static final String[] DIFICULTADES = {"FACIL", "MEDIO", "DIFICIL"};
    private int dificultadSeleccionada = 0;

    private boolean gPrevia = false;
    private boolean enterPrevio = false;
    private boolean arribaPrevio = false;
    private boolean abajoPrevio = false;
    private boolean izquierdaPrevia = false;
    private boolean derechaPrevia = false;
    private boolean mPrevia = false;
    private boolean rPrevia = false;
    private boolean ePrevia = false;
    private boolean cPrevia = false;

    private boolean carreraTerminada = false;
    private String resultadoCarrera = "";
    private long framesResultado = 0;
    private static final long DURACION_RESULTADO = 180;

    private List<double[]> trayectoria = new ArrayList<>();
    private int contadorTrayectoria = 0;

    private double spawnJugadorX, spawnJugadorY;
    private double spawnIAX, spawnIAY;
    private ControladorIA ciAIReferencia;

    private int framesCarrera = 0;
    private static final int FRAMES_SEGUROS = 30;

    public Simulador(Canvas canvas, Scene scene) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.pista = new Pista();
        this.input = new GestorEntradas(scene);
        this.renderizador = new Renderizador();

        initTimer();
    }

    private void initTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderizador.limpiar(gc, canvas.getWidth(), canvas.getHeight());

                switch (estado) {
                    case MENU -> {
                        renderizador.dibujarFondoMenu(gc, canvas.getWidth(), canvas.getHeight());
                        actualizarMenu();
                    }
                    case ENTRENAMIENTO -> {
                        renderizador.dibujarPista(gc, pista.getImagen(), canvas.getWidth(), canvas.getHeight());
                        actualizarEntrenamiento();
                    }
                    case CARRERA -> {
                        renderizador.dibujarPista(gc, pista.getImagen(), canvas.getWidth(), canvas.getHeight());
                        actualizarCarrera();
                    }
                }
            }
        };
    }

    private void actualizarMenu() {
        boolean arribaActual = input.menuArriba();
        boolean abajoActual = input.menuAbajo();
        boolean enterActual = input.enter();
        boolean eActual = input.teclaE();
        boolean cActual = input.teclaC();

        if (arribaActual && !arribaPrevio) {
            opcionMenu = (opcionMenu - 1 + OPCIONES_MENU.length) % OPCIONES_MENU.length;
        }
        if (abajoActual && !abajoPrevio) {
            opcionMenu = (opcionMenu + 1) % OPCIONES_MENU.length;
        }
        if (opcionMenu == 1) {
            boolean izqActual = input.izquierda();
            boolean derActual = input.derecha();
            if (izqActual && !izquierdaPrevia) {
                dificultadSeleccionada = (dificultadSeleccionada - 1 + DIFICULTADES.length) % DIFICULTADES.length;
            }
            if (derActual && !derechaPrevia) {
                dificultadSeleccionada = (dificultadSeleccionada + 1) % DIFICULTADES.length;
            }
            izquierdaPrevia = izqActual;
            derechaPrevia = derActual;
        } else {
            izquierdaPrevia = input.izquierda();
            derechaPrevia = input.derecha();
        }
        if ((enterActual && !enterPrevio) || (eActual && !ePrevia)) {
            switch (opcionMenu) {
                case 0 -> iniciarEntrenamiento();
                case 1 -> iniciarCarrera();
                case 2 -> System.exit(0);
            }
        }
        if (cActual && !cPrevia) {
            iniciarCarrera();
        }

        arribaPrevio = arribaActual;
        abajoPrevio = abajoActual;
        enterPrevio = enterActual;
        ePrevia = eActual;
        cPrevia = cActual;

        renderizador.dibujarMenu(gc, "SIMULADOR DE AUTOS AUTONOMOS", opcionMenu, getOpcionesMenuVisuales());
    }

    private String[] getOpcionesMenuVisuales() {
        String[] visuales = new String[OPCIONES_MENU.length];
        for (int i = 0; i < OPCIONES_MENU.length; i++) {
            if (i == 1) {
                visuales[i] = OPCIONES_MENU[i] + "   <" + DIFICULTADES[dificultadSeleccionada] + ">";
            } else {
                visuales[i] = OPCIONES_MENU[i];
            }
        }
        return visuales;
    }

    private void iniciarEntrenamiento() {
        gPrevia = false;
        poblacion = new Poblacion(pista, pista.getStartX(), pista.getStartY());
        renderizador.getGrafico().limpiar();
        trayectoria.clear();
        estado = EstadoJuego.ENTRENAMIENTO;
        System.out.println("Modo ENTRENAMIENTO iniciado");
    }

    private void actualizarEntrenamiento() {
        if (poblacion == null) return;

        poblacion.update();

        Vehiculo mejorVivo = null;
        double mejorDist = -1;
        for (Vehiculo v : poblacion.getVehiculos()) {
            if (v.isVivo() && v.getDistanciaRecorrida() > mejorDist) {
                mejorDist = v.getDistanciaRecorrida();
                mejorVivo = v;
            }
        }

        if (mejorVivo != null) {
            contadorTrayectoria++;
            if (contadorTrayectoria % 3 == 0) {
                trayectoria.add(new double[]{mejorVivo.getX(), mejorVivo.getY()});
                if (trayectoria.size() > 200) {
                    trayectoria.remove(0);
                }
            }
        }

        renderizador.dibujarTrayectoria(gc, trayectoria, Color.rgb(0, 255, 255, 0.3));

        for (Vehiculo v : poblacion.getVehiculos()) {
            if (v.isVivo()) {
                renderizador.dibujarVehiculo(gc, v, Color.web("#00FFCC"));
                renderizador.dibujarSensores(gc, v);
            }
        }

        if (poblacion.todosMuertos()) {
            double fitActual = poblacion.getMejorFitness();
            renderizador.getGrafico().registrarFitness(fitActual);
            poblacion.siguienteGeneracion();
            trayectoria.clear();
            contadorTrayectoria = 0;
        }

        renderizador.dibujarHUDEntrenamiento(gc,
            poblacion.getGeneracion(),
            poblacion.getVehiculosVivos(),
            poblacion.getMejorFitness());

        renderizador.dibujarGrafico(gc, 20, 500, 180, 80);

        boolean gActual = input.teclaG();
        if (gActual && !gPrevia) {
            Vehiculo mejor = poblacion.getVehiculos().get(0);
            GestorRed.guardarRed(((ControladorIA) mejor.getControladorIA()).getRed());
            System.out.println("Red guardada manualmente!");
        }
        gPrevia = gActual;

        boolean mActual = input.teclaM();
        if (mActual && !mPrevia) {
            estado = EstadoJuego.MENU;
            poblacion = null;
            System.out.println("Volviendo al menu principal");
        }
        mPrevia = mActual;
    }

    private double[] calcularSpawnsSeparados() {
        double cx = pista.getStartX();
        double cy = pista.getStartY();

        double jx = cx - 20, jy = cy - 10;
        double[][] candidatosJug = {
            {cx - 20, cy - 10}, {cx - 20, cy},     {cx - 20, cy + 10},
            {cx,      cy - 10}, {cx,      cy},     {cx,      cy + 10},
            {cx - 40, cy - 10}, {cx - 40, cy},
        };
        for (double[] c : candidatosJug) {
            if (esSpawnSeguro(c[0], c[1])) { jx = c[0]; jy = c[1]; break; }
        }

        double ix = jx, iy = jy - 35;
        double[][] candidatosIA = {
            {jx,      jy - 35}, {jx,      jy - 50}, {jx,      jy - 25},
            {jx - 5,  jy - 35}, {jx + 5,  jy - 35},
            {cx + 20, cy - 10}, {cx - 60, cy - 10},
            {cx,      cy + 20},
        };
        for (double[] c : candidatosIA) {
            double dist = Math.sqrt(Math.pow(c[0]-jx,2) + Math.pow(c[1]-jy,2));
            if (esSpawnSeguro(c[0], c[1]) && dist > 20) {
                ix = c[0]; iy = c[1]; break;
            }
        }

        double anguloJugador = pista.detectarAnguloInicial(jx, jy);
        double anguloIA      = pista.detectarAnguloInicial(ix, iy);
        System.out.println("Spawn Jugador: (" + jx + ", " + jy + ") angulo=" + Math.toDegrees(anguloJugador));
        System.out.println("Spawn IA:      (" + ix + ", " + iy + ") angulo=" + Math.toDegrees(anguloIA));
        return new double[]{jx, jy, ix, iy, anguloJugador, anguloIA};
    }

    private boolean esSpawnSeguro(double x, double y) {
        return !pista.hayColisionEnTrayecto(x, y, x + 1.0, y, 40, 20);
    }

    private void iniciarCarrera() {
        gPrevia = false;
        double[] spawns = calcularSpawnsSeparados();
        spawnJugadorX = spawns[0];
        spawnJugadorY = spawns[1];
        spawnIAX      = spawns[2];
        spawnIAY      = spawns[3];
        double anguloJugador = spawns[4];
        double anguloIA      = spawns[5];
        jugador = new Jugador(spawnJugadorX, spawnJugadorY, pista, input);
        jugador.getVehiculo().setAngulo(anguloJugador);

        RedNeuronal redCargada = null;
        switch (dificultadSeleccionada) {
            case 0 -> {
                redCargada = new RedNeuronal(5, 4, 2);
                System.out.println("Dificultad FACIL: IA aleatoria");
            }
            case 1 -> {
                redCargada = GestorRed.cargarMejorRed();
                if (redCargada != null) {
                    redCargada.mutar(0.2);
                    System.out.println("Dificultad MEDIO: IA con ruido");
                } else {
                    redCargada = new RedNeuronal(5, 4, 2);
                    System.out.println("No hay red guardada. Usando IA aleatoria.");
                }
            }
            case 2 -> {
                redCargada = GestorRed.cargarMejorRed();
                if (redCargada == null) {
                    redCargada = new RedNeuronal(5, 4, 2);
                    System.out.println("No hay red guardada. Usando IA aleatoria.");
                } else {
                    System.out.println("Dificultad DIFICIL: mejor red entrenada");
                }
            }
        }
        ControladorIA ciAI = new ControladorIA(redCargada);
        ciAIReferencia = ciAI;
        iaVehiculo = new Vehiculo(spawnIAX, spawnIAY, 40, 20, ciAI, pista);
        iaVehiculo.reset(spawnIAX, spawnIAY, anguloIA);
        iaVehiculo.setAngulo(anguloIA);
        iaVehiculo.setControladorIA(ciAI);

        carreraTerminada = false;
        resultadoCarrera = "";
        framesResultado = 0;
        framesCarrera = 0;

        System.out.println("Modo CARRERA iniciado - Jugador vs IA");
        System.out.println("  Jugador spawn: (" + spawnJugadorX + ", " + spawnJugadorY + ") angulo=" + Math.toDegrees(anguloJugador));
        System.out.println("  IA spawn: (" + spawnIAX + ", " + spawnIAY + ") angulo=" + Math.toDegrees(anguloIA));
        estado = EstadoJuego.CARRERA;
    }

    private void actualizarCarrera() {
        if (jugador == null || iaVehiculo == null) return;

        if (!carreraTerminada) {
            jugador.update();
            iaVehiculo.update();
            framesCarrera++;

            boolean jugadorMeta = jugador.getVehiculo().haCruzadoMeta();
            boolean iaMeta = iaVehiculo.haCruzadoMeta();
            boolean jugadorVivo = jugador.getVehiculo().isVivo();
            boolean iaVivo = iaVehiculo.isVivo();

            if (jugadorMeta && !iaMeta) {
                resultadoCarrera = "GANASTE!";
                carreraTerminada = true;
            } else if (iaMeta && !jugadorMeta) {
                resultadoCarrera = "PERDISTE!";
                carreraTerminada = true;
            } else if (jugadorMeta && iaMeta) {
                double dJug = jugador.getVehiculo().getDistanciaRecorrida();
                double dIA = iaVehiculo.getDistanciaRecorrida();
                resultadoCarrera = dJug >= dIA ? "GANASTE!" : "PERDISTE!";
                carreraTerminada = true;
            } else if (!jugadorVivo && !iaVivo) {
                if (framesCarrera > FRAMES_SEGUROS) {
                    resultadoCarrera = "EMPATE!";
                    carreraTerminada = true;
                }
            } else if (!jugadorVivo) {
                if (framesCarrera > FRAMES_SEGUROS) {
                    resultadoCarrera = "PERDISTE!";
                    carreraTerminada = true;
                }
            } else if (!iaVivo) {
                if (framesCarrera > FRAMES_SEGUROS) {
                    resultadoCarrera = "GANASTE!";
                    carreraTerminada = true;
                }
            }
        }

        renderizador.dibujarVehiculo(gc, jugador.getVehiculo(), Color.RED);
        renderizador.dibujarSensores(gc, jugador.getVehiculo());
        renderizador.dibujarVehiculo(gc, iaVehiculo, Color.BLUE);
        renderizador.dibujarSensores(gc, iaVehiculo);

        renderizador.dibujarHUDCarrera(gc,
            jugador.getVehiculo().getDistanciaRecorrida(),
            iaVehiculo.getDistanciaRecorrida(),
            jugador.getVehiculo().isVivo(),
            iaVehiculo.isVivo());

        boolean rActual = input.teclaR();
        if (rActual && !rPrevia) {
            reiniciarCarrera();
        }
        rPrevia = rActual;

        if (carreraTerminada) {
            framesResultado++;
            long restantes = Math.max(0, DURACION_RESULTADO - framesResultado);
            renderizador.dibujarResultadoConReintento(gc, resultadoCarrera,
                jugador.getVehiculo().getDistanciaRecorrida(),
                iaVehiculo.getDistanciaRecorrida(),
                jugador.getVehiculo().haCruzadoMeta(),
                iaVehiculo.haCruzadoMeta(),
                restantes);

            if (framesResultado > DURACION_RESULTADO) {
                if (input.enter() && !enterPrevio) {
                    limpiarCarrera();
                }
                enterPrevio = input.enter();
            }
            if (input.teclaM() && !mPrevia) {
                limpiarCarrera();
            }
            mPrevia = input.teclaM();

            boolean gActual = input.teclaG();
            if (gActual && !gPrevia) {
                PartidaGuardada pg = new PartidaGuardada(
                    resultadoCarrera,
                    jugador.getVehiculo().getDistanciaRecorrida(),
                    iaVehiculo.getDistanciaRecorrida(),
                    jugador.getVehiculo().haCruzadoMeta(),
                    iaVehiculo.haCruzadoMeta(),
                    framesCarrera
                );
                GestorPartida.guardar(pg);
            }
            gPrevia = gActual;
        } else {
            boolean mActual = input.teclaM();
            if (mActual && !mPrevia) {
                limpiarCarrera();
            }
            mPrevia = mActual;
        }
    }

    private void reiniciarCarrera() {
        double[] spawns = calcularSpawnsSeparados();
        spawnJugadorX = spawns[0];
        spawnJugadorY = spawns[1];
        spawnIAX      = spawns[2];
        spawnIAY      = spawns[3];
        double anguloJugador = spawns[4];
        double anguloIA      = spawns[5];
        jugador = new Jugador(spawnJugadorX, spawnJugadorY, pista, input);
        jugador.getVehiculo().setAngulo(anguloJugador);

        ControladorIA ciAI;
        if (ciAIReferencia != null) {
            RedNeuronal redClon = new RedNeuronal(5, 4, 2);
            redClon.setPesosDesdeArray(ciAIReferencia.getRed().getPesosComoArray());
            ciAI = new ControladorIA(redClon);
        } else {
            ciAI = new ControladorIA(new RedNeuronal(5, 4, 2));
        }
        iaVehiculo = new Vehiculo(spawnIAX, spawnIAY, 40, 20, ciAI, pista);
        iaVehiculo.reset(spawnIAX, spawnIAY, anguloIA);
        iaVehiculo.setAngulo(anguloIA);
        iaVehiculo.setControladorIA(ciAI);

        carreraTerminada = false;
        resultadoCarrera = "";
        framesResultado = 0;
        framesCarrera = 0;
        System.out.println("Carrera reiniciada!");
    }

    private void limpiarCarrera() {
        estado = EstadoJuego.MENU;
        jugador = null;
        iaVehiculo = null;
        ciAIReferencia = null;
        carreraTerminada = false;
        resultadoCarrera = "";
        framesResultado = 0;
        framesCarrera = 0;
        gPrevia = false;
        enterPrevio = false;
        mPrevia = false;
        rPrevia = false;
        System.out.println("Volviendo al menu principal");
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
