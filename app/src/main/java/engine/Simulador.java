package engine;

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

    private boolean gPrevia = false;
    private boolean enterPrevio = false;
    private boolean arribaPrevio = false;
    private boolean abajoPrevio = false;
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

        renderizador.dibujarMenu(gc, "SIMULADOR DE AUTOS AUTONOMOS", opcionMenu, OPCIONES_MENU);
    }

    private void iniciarEntrenamiento() {
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

    private void iniciarCarrera() {
        double cx = pista.getStartX();
        double cy = pista.getStartY();

        spawnJugadorX = cx - 20;
        spawnJugadorY = cy - 10;
        spawnIAX = cx - 20;
        spawnIAY = cy - 10 - 30;

        jugador = new Jugador(spawnJugadorX, spawnJugadorY, pista, input);

        RedNeuronal redCargada = GestorRed.cargarMejorRed();
        if (redCargada == null) {
            redCargada = new RedNeuronal(5, 4, 2);
            System.out.println("No se encontro red entrenada. Usando IA aleatoria.");
        }
        ControladorIA ciAI = new ControladorIA(redCargada);
        ciAIReferencia = ciAI;
        iaVehiculo = new Vehiculo(spawnIAX, spawnIAY, 40, 20, ciAI, pista);
        iaVehiculo.reset(spawnIAX, spawnIAY, -Math.PI / 2);
        iaVehiculo.setControladorIA(ciAI);

        carreraTerminada = false;
        resultadoCarrera = "";
        framesResultado = 0;
        framesCarrera = 0;

        System.out.println("Modo CARRERA iniciado - Jugador vs IA");
        System.out.println("  Jugador spawn: (" + spawnJugadorX + ", " + spawnJugadorY + ") angulo=-PI/2");
        System.out.println("  IA spawn: (" + spawnIAX + ", " + spawnIAY + ") angulo=-PI/2");
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
        } else {
            boolean mActual = input.teclaM();
            if (mActual && !mPrevia) {
                limpiarCarrera();
            }
            mPrevia = mActual;
        }
    }

    private void reiniciarCarrera() {
        double cx = pista.getStartX();
        double cy = pista.getStartY();

        spawnJugadorX = cx - 20;
        spawnJugadorY = cy - 10;
        spawnIAX = cx - 20;
        spawnIAY = cy - 10 - 30;

        jugador = new Jugador(spawnJugadorX, spawnJugadorY, pista, input);

        ControladorIA ciAI;
        if (ciAIReferencia != null) {
            RedNeuronal redClon = new RedNeuronal(5, 4, 2);
            redClon.setPesosDesdeArray(ciAIReferencia.getRed().getPesosComoArray());
            ciAI = new ControladorIA(redClon);
        } else {
            ciAI = new ControladorIA(new RedNeuronal(5, 4, 2));
        }
        iaVehiculo = new Vehiculo(spawnIAX, spawnIAY, 40, 20, ciAI, pista);
        iaVehiculo.reset(spawnIAX, spawnIAY, -Math.PI / 2);
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
