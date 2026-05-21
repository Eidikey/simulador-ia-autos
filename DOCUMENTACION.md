# Simulador de Autos Autónomos — Documentación Completa

## Índice
1. [Estructura del proyecto](#1-estructura-del-proyecto)
2. [Diagrama de flujo general](#2-diagrama-de-flujo-general)
3. [Paquete `main`](#3-paquete-main)
4. [Paquete `model`](#4-paquete-model)
5. [Paquete `engine`](#5-paquete-engine)
6. [Paquete `ai`](#6-paquete-ai)
7. [Paquete `controller`](#7-paquete-controller)
8. [Paquete `view`](#8-paquete-view)
9. [Paquete `util`](#9-paquete-util)
10. [Build system](#10-build-system)
11. [Bug conocido: muerte instantánea](#11-bug-conocido-muerte-instantánea)
12. [Bug conocido: IA no aprende](#12-bug-conocido-ia-no-aprende)
13. [Historial de cambios](#13-historial-de-cambios)

---

## 1. Estructura del proyecto

```
simulador-ia-autos/
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   ├── main/
│       │   │   │   └── Main.java
│       │   │   ├── model/
│       │   │   │   ├── Controlador.java        (interfaz)
│       │   │   │   ├── Entidad.java             (clase abstracta)
│       │   │   │   ├── Jugador.java
│       │   │   │   ├── PartidaGuardada.java
│       │   │   │   ├── Pista.java
│       │   │   │   ├── Sensor.java
│       │   │   │   └── Vehiculo.java
│       │   │   ├── engine/
│       │   │   │   ├── EstadoJuego.java         (enum)
│       │   │   │   └── Simulador.java
│       │   │   ├── ai/
│       │   │   │   ├── ControladorIA.java
│       │   │   │   ├── GestorPartida.java
│       │   │   │   ├── GestorRed.java
│       │   │   │   ├── Poblacion.java
│       │   │   │   └── RedNeuronal.java
│       │   │   ├── controller/
│       │   │   │   ├── ControladorJugador.java
│       │   │   │   └── GestorEntradas.java
│       │   │   ├── view/
│       │   │   │   ├── GraficoEntrenamiento.java
│       │   │   │   ├── HUD.java
│       │   │   │   ├── Renderizador.java
│       │   │   │   └── RenderizadorVehiculo.java
│       │   │   └── util/
│       │   │       └── InspectImage.java
│       │   └── resources/
│       │       └── pista.png  (JPEG real, extension .png)
│       └── test/
│           └── java/
│               ├── model/
│               │   ├── JugadorTest.java
│               │   ├── PistaTest.java
│               │   └── VehiculoTest.java
│               └── ai/
│                   ├── GestorRedTest.java
│                   └── RedNeuronalTest.java
```

---

## 2. Diagrama de flujo general

```
Main.java
  └─ JavaFX Application.start()
       └─ Crea Canvas(800,600) + Scene
            └─ Simulador(canvas, scene)
                 └─ AnimationTimer.handle(now)  ← 60 fps
                      ├─ estado == MENU       → actualizarMenu()
                      ├─ estado == ENTRENAMIENTO → actualizarEntrenamiento()
                      └─ estado == CARRERA    → actualizarCarrera()

MENU → (ENTER) → ENTRENAMIENTO
MENU → (ENTER) → CARRERA
CARRERA → (M) → MENU
ENTRENAMIENTO → (M) → MENU
```

### Ciclo de vida de una carrera (CARRERA):

```
iniciarCarrera()
  → calcular spawn (Jugador + IA)
  → crear Jugador(vehiculo + controladorJugador)
  → crear Vehiculo(IA) con red según dificultad
  → estado = CARRERA

actualizarCarrera() (cada frame):
  → jugador.update()
  → iaVehiculo.update()
  → verificar condiciones de victoria/derrota
  → renderizar todo
  → si carreraTerminada: mostrar resultado + opciones (R, G, M)
```

### Ciclo de entrenamiento (ENTRENAMIENTO):

```
iniciarEntrenamiento()
  → crear Poblacion(50 vehiculos con redes aleatorias)
  → estado = ENTRENAMIENTO

actualizarEntrenamiento() (cada frame):
  → poblacion.update()  ← llama v.update() en cada vehiculo vivo
  → si todos muertos:
      → siguienteGeneracion()
        → evaluarFitness() + ordenar
        → crossover + mutacion
        → guardar mejor red si hay record
```

---

## 3. Paquete `main`

### `Main.java` (27 líneas)
- Punto de entrada JavaFX
- Crea `Canvas(800, 600)` y `Scene`
- Instancia `Simulador(canvas, scene)` y llama `simulador.start()`
- Título: "Simulador de Autos Autonomos"

---

## 4. Paquete `model`

### `Controlador.java` (interfaz, 7 líneas)
- `double obtenerGiro()` — retorna giro (-1 a 1 aprox)
- `double obtenerAceleracion()` — retorna aceleración

### `Entidad.java` (clase abstracta, 17 líneas)
- `x, y` (double) — posición (esquina superior izquierda)
- `ancho, alto` (double)
- Constructor: `Entidad(x, y, ancho, alto)`
- `abstract void update()`

### `Vehiculo.java` (156 líneas aprox)
Extiende `Entidad`.

**Campos clave:**
- `velocidad` (double, arranca en 0.5)
- `angulo` (double, radianes, arranca en 0)
- `controlador` (Controlador) — el controlador activo
- `controladorIA` (ControladorIA, nullable) — si es IA
- `sensores` — 5 sensores con ángulos relativos: -90°, -45°, 0°, 45°, 90°
- `vivo` (boolean)
- `haCruzadoMeta` (boolean)
- `framesBajaVelocidad` (int) — contador de frames con velocidad < 0.5
- `rotacionAcumulada` (double) — suma de |giro| en radianes
- `distanciaRecorrida` (double) — suma de avance por frame
- `pista` (Pista) — referencia a la pista

**Constructor:**
```java
Vehiculo(x, y, ancho, alto, controlador, pista)
```
Inicializa: velocidad=0.5, angulo=0, vivo=true, crea 5 sensores con ángulos {-90, -45, 0, 45, 90}.

**`update()` — EL MÉTODO CRÍTICO:**
```
1. Si !vivo → return
2. Para cada sensor: medirDistancia(x, y, angulo)
   - Si algún sensor detecta META (rojo) a < UMBRAL px: metaDetectada = true
3. Si metaDetectada:
   - haCruzadoMeta = true, velocidad = 0, return (NO MUERE, pero no se mueve)
4. Si controladorIA != null → controladorIA.procesar(inputs)
5. aceleracion = controlador.obtenerAceleracion()
6. giro = controlador.obtenerGiro()
7. velocidad += aceleracion + 0.1    ← SIEMPRE suma 0.1
8. angulo += giro
9. rotacionAcumulada += |giro|
10. nuevoX = x + velocidad * cos(angulo)
11. nuevoY = y + velocidad * sin(angulo)
12. distanciaRecorrida += avance
13. fitness = distanciaDesdeStart + (velocidad*10) - (rotacionAcumulada*0.3)
14. Si rotacionAcumulada > 360° Y distanciaDesdeStart < 50px → vivo = false
15. Si chocaPared(x, y, nuevoX, nuevoY) → vivo = false, return
16. x = nuevoX, y = nuevoY
17. Si velocidad < 0.5 → framesBajaVelocidad++ (si > 60 → vivo = false)
    Sino → framesBajaVelocidad = 0
```

**UMBRAL_META**: `3.0 / 300.0 = 0.01` (reducido de 5.0/300.0 en la última modificación). El sensor avanza de a 2px, pasos 1 y 2 (distancias 2px y 4px) quedan bajo este umbral. Paso 3 (6px): 6/300=0.02 > 0.01 → no detecta.

**`chocaPared(xInicio, yInicio, xFin, yFin)`:**
```java
return pista.hayColisionEnTrayecto(xInicio, yInicio, xFin, yFin, ancho, alto);
```

**Métodos públicos:**
- `isVivo()`, `getDistanciaRecorrida()`, `getFitness()`, `setFitness()`, `getX()`, `getY()`, `getAncho()`, `getAlto()`
- `setAngulo(a)`, `getAngulo()`
- `haCruzadoMeta()`, `reset(x, y, nuevoAngulo)`, `getSensores()`
- `setControladorIA(cia)`, `getControladorIA()`

**`reset()`** — Reinicia: x, y, angulo=0, velocidad=0.5, distancia=0, rotacion=0, framesBaja=0, fitness=0, vivo=true, haCruzadoMeta=false.

### `Sensor.java` (78 líneas)

**Campos:**
- `anguloRelativo` (double, en grados: -90, -45, 0, 45, 90)
- `pista` (Pista)
- `ultimaDistancia`, `ultimoX`, `ultimoY`, `origenX`, `origenY`
- `MAX_DISTANCIA = 300.0`
- `metaDetectada` (boolean)

**`medirDistancia(origenX, origenY, anguloVehiculo)`:**
```
anguloGrados = Math.toDegrees(anguloVehiculo) + anguloRelativo
anguloRad = Math.toRadians(anguloGrados)
dx = cos(anguloRad)
dy = sin(anguloRad)

x = origenX, y = origenY
distancia = 0
Mientras distancia < MAX_DISTANCIA:
    x += dx * 2, y += dy * 2, distancia += 2
    Si dentroLimites(x,y):
        Si esPared(x,y) → break
        Si esMeta(x,y) → metaDetectada=true, break
    Sino → break

ultimaDistancia = distancia
return distancia / MAX_DISTANCIA  ← normalizado a [0, 1]
```

**Nota:** el sensor avanza de a 2px, por lo que la distancia detectada siempre es múltiplo de 2 (2, 4, 6, 8...).

### `Pista.java` (192 líneas)

**Campos:**
- `imagen` (Image JavaFX) — cargada de `/pista.png`
- `pixelReader` — para leer colores de píxeles
- `ancho, alto` — dimensiones de la imagen cargada
- `startX, startY` — spawn encontrado por `encontrarSpawnPoint()`

**Constructor:**
```java
try (InputStream is = getClass().getResourceAsStream("/pista.png")) {
    this.imagen = new Image(is, 800, 600, false, true);
    // preserveRatio = false → estirada a EXACTAMENTE 800x600
    // smooth = true → bilinear filtering
    this.pixelReader = imagen.getPixelReader();
}
double[] spawn = encontrarSpawnPoint();
this.startX = spawn[0];  // centerX
this.startY = spawn[1];  // y (fila del pixel azul)
```

**IMPORTANTE:** El archivo `pista.png` es en realidad un JPEG (cabecera FF D8 FF). Dimensiones originales: 1536×1024 (3:2). JavaFX lo estira a 800×600 (4:3). Esto causa una distorsión: la imagen original 3:2 se fuerza a 4:3.

**Dimensiones reales de la imagen cargada:**
- `ancho = imagen.getWidth()` → 800 (solicitado)
- `alto = imagen.getHeight()` → 600 (solicitado, preserveRatio=false)

**Métodos de detección de píxeles:**
- `esPared(x,y)` — negro (R<0.1, G<0.1, B<0.1) o fuera de límites
- `esMeta(x,y)` — rojo (R>0.9, G<0.1, B<0.1)
- `esSpawn(x,y)` — azul (B>0.5, R<0.5, G<0.5)
- `esTransitable(x,y)` — dentro de límites Y no es pared
- `dentroLimites(x,y)` — x en [0, ancho), y en [0, alto)

**`hayColisionEnTrayecto(x1,y1,x2,y2, anchoV,altoV)`:**
```
dx = x2-x1, dy = y2-y1
distancia = sqrt(dx²+dy²)
pasos = max(distancia/2 + 1, 1)  ← al menos 1

Para t = 0 a 1, en incrementos de 1/pasos:
    px = x1 + dx*t, py = y1 + dy*t
    Verifica 4 esquinas del vehículo (anchoV×altoV):
        (px, py), (px+anchoV, py), (px, py+altoV), (px+anchoV, py+altoV)
    Si alguna esquina NO es transitable → HAY COLISIÓN
```

**`cabeVehiculoEn(x, y, anchoV, altoV)`:** verifica solo las 4 esquinas en UNA posición (sin trayectoria).

**`encontrarSpawnPoint()`** — Busca el pixel más azul de toda la imagen, luego prueba márgenes progresivos:
```
double[] margenes = {10.0, 5.0, 3.0, 1.0, 0.6};
Para cada margen:
    resultado = encontrarSpawnConMargen(spawnX, spawnY, margen)
    Si resultado != null → return resultado

// Fallback: centrar en el camino horizontal
// (SIN validación de márgenes ni meta)
```

**`encontrarSpawnConMargen(spawnX, spawnY, margen)`:**
```
int[] candidatosY = {0, -5, 5, -10, 10, -15, 15, -20, 20};
Para cada dy:
    y = spawnY + dy
    Expandir left/right hasta encontrar paredes
    Si right-left < 40 → muy angosto, skip
    centerX = Math.round((left+right)/2)
    vx = centerX - 20, vy = y - 10
    
    Si NO hay colisión(vx, vy, vx+margen, vy, 40, 20)
       Y NO sensoresDetectanMetaCerca(vx, vy):
        return {centerX, y}
    
    // Intentar desplazamientos en X
    Para dx en {10, 20, 30} y sign en {-1, 1}:
        testVx = (centerX ± dx) - 20
        Si cabe horizontalmente Y pasa validación:
            return {centerX ± dx, y}

return null
```

**`sensoresDetectanMetaCerca(vx, vy)`** — (NUEVO) Simula los 5 sensores desde la esquina del vehículo. Cada sensor avanza 2px por paso, 2 pasos (4px total). Si algún sensor encuentra un pixel rojo (meta), retorna true.

### `Jugador.java` (39 líneas)

Wrapper del vehículo para el jugador humano.

```java
Jugador(x, y, pista, input)
  → controlador = new ControladorJugador(input)
  → vehiculo = new Vehiculo(x, y, 40, 20, controlador, pista)

update(): vehiculo.update(), si vehiculo.isVivo()==false → activo=false
reiniciar(x, y, angulo): vehiculo.reset(x, y, angulo)
```

### `PartidaGuardada.java` (47 líneas)

POJO para guardar resultados de carrera. Campos: timestamp, resultado, distanciaJugador, distanciaIA, metaJugador, metaIA, framesDuracion.

---

## 5. Paquete `engine`

### `EstadoJuego.java` (enum)
- `MENU`, `ENTRENAMIENTO`, `CARRERA`

### `Simulador.java` (487 líneas) — CLASE PRINCIPAL

**Campos importantes:**
- `canvas`, `gc` — lienzo y contexto gráfico
- `pista` — Pista singleton
- `input` — GestorEntradas
- `renderizador` — Renderizador
- `poblacion` — Poblacion (solo en ENTRENAMIENTO)
- `jugador` — Jugador (solo en CARRERA)
- `iaVehiculo` — Vehiculo IA (solo en CARRERA)
- `timer` — AnimationTimer (60 fps)
- `estado` — EstadoJuego actual
- `opcionMenu` (0, 1, 2): ENTRENAR IA, COMPETIR VS IA, SALIR
- `dificultadSeleccionada` (0,1,2): FACIL, MEDIO, DIFICIL
- `carreraTerminada`, `resultadoCarrera`, `framesResultado`
- Variables `*Previo` para detección de flanco de teclas
- `spawnJugadorX/Y`, `spawnIAX/Y`
- `ciAIReferencia` — ControladorIA usado (para reiniciar)

**Constructor:** `Simulador(canvas, scene)` → crea Pista, GestorEntradas, Renderizador, inicia timer.

**`initTimer()`** — AnimationTimer que:
- Limpia canvas
- Según estado: llama actualizarMenu(), actualizarEntrenamiento(), o actualizarCarrera()
- Corre a ~60 fps

**`actualizarMenu()`**
```
Leer teclas: UP, DOWN, ENTER, E, C
UP/DOWN → navegar entre opciones
Si opcion == 1 (COMPETIR VS IA):
    LEFT/RIGHT → cambiar dificultadSeleccionada
ENTER/E → ejecutar opción (entrenar, competir, salir)
C → iniciarCarrera() directo
Dibujar menú con dificultad visible: "COMPETIR VS IA   <FACIL>"
```

**`actualizarEntrenamiento()`**
```
Si poblacion == null → return
poblacion.update()
Dibujar trayectoria del mejor vehículo vivo
Dibujar todos los vehículos vivos + sensores
Si todos muertos:
    → registrar fitness, siguienteGeneracion()
    → limpiar trayectoria
G → guardar red manual
M → volver al menú
```

**`esSpawnSeguro(x,y)`**
```
double[] margenes = {10.0, 5.0, 3.0, 1.0, 0.6};
Para cada margen:
    Si !hayColisionEnTrayecto(x, y, x+margen, y, 40, 20)
       Y !sensoresDetectanMetaCerca(x, y):
        return true (el margen más grande que pase gana)
return false (ningún margen funciona)
```

**`iniciarCarrera()`**
```
gPrevia = false
cx = pista.getStartX(), cy = pista.getStartY()

// Jugador spawn
spawnJugadorX = cx - 20, spawnJugadorY = cy - 10
Si !esSpawnSeguro: intentar cx+20
  Si no: spawnJugadorX = cx-20 (fallback = posición validada)
jugador = new Jugador(spawnJugadorX, spawnJugadorY, pista, input)

// IA spawn
spawnIAX = cx + 20, spawnIAY = cy - 10
Si !esSpawnSeguro: intentar cx-60
  Si no: spawnIAX = cx-20, spawnIAY = cy-10 (fallback)

// Crear IA según dificultad
switch (dificultadSeleccionada):
  case 0 (FACIL): red = new RedNeuronal(5,4,2) (aleatoria)
  case 1 (MEDIO): cargar mejor red + mutar(0.2)
  case 2 (DIFICIL): cargar mejor red (sin modificar)

iaVehiculo = new Vehiculo(spawnIAX, spawnIAY, 40, 20, ciAI, pista)
reset(spawnIAX, spawnIAY, 0)
setAngulo(0)
```

**`actualizarCarrera()`**
```
Si carreraTerminada == false:
    jugador.update()
    iaVehiculo.update()
    framesCarrera++
    
    // Verificar condiciones de fin
    Si jugador cruzó meta Y ia NO → GANASTE
    Si ia cruzó meta Y jugador NO → PERDISTE
    Si ambos cruzaron → el de mayor distancia gana
    Si ambos muertos Y framesCarrera > 30 → EMPATE
    Si jugador muerto → PERDISTE (si frames > 30)
    Si ia muerta → GANASTE (si frames > 30)

// Render
dibujar vehículo jugador (rojo) + sensores
dibujar vehículo IA (azul) + sensores
dibujar HUD carrera

// Teclas en carrera
R → reiniciarCarrera()
Si carreraTerminada:
    dibujar resultado (stats + opciones)
    M → limpiarCarrera() (volver menú)
    G → guardar partida (PartidaGuardada + GestorPartida)
    (ENTER después de temporizador → limpiarCarrera)
```

**`reiniciarCarrera()`** — Similar a iniciarCarrera pero clona la IA de ciAIReferencia.

**`limpiarCarrera()`** — Vuelve al menú, limpia todas las referencias, resetea variables *Previo.

---

## 6. Paquete `ai`

### `RedNeuronal.java` (116 líneas)

Red neuronal simple de 3 capas:
- Entrada: 5 neuronas (distancias de los 5 sensores)
- Oculta: 4 neuronas
- Salida: 2 neuronas (giro, aceleración)

**Arquitectura:**
- `pesosEntradaOculta[5][4]`
- `pesosOcultaSalida[4][2]`

**`feedForward(inputs[5])` → `[giro, aceleracion]`:**
```
Para cada neurona oculta j:
    suma = Σ(inputs[i] * pesosEO[i][j])
    oculta[j] = sigmoide(suma)

Para cada neurona salida j:
    suma = Σ(oculta[i] * pesosOS[i][j])
    salidas[j] = sigmoide(suma) * 2 - 1

sigmoide(x) = 1 / (1 + e^(-x))
```

**`mutar(tasaMutacion)`** — Cada peso tiene probabilidad `tasaMutacion` de mutar: `peso += (random-0.5)*2`.

**`getPesosComoArray()` / `setPesosDesdeArray(array)`** — Serialización/deserialización a array lineal (total: 5*4 + 4*2 = 28 pesos).

### `ControladorIA.java` (31 líneas)
Implementa `Controlador`. Usa `RedNeuronal`.
- `procesar(inputs)`: ejecuta `red.feedForward(inputs)` y guarda en `ultimasSalidas`
- `obtenerGiro()`: retorna `ultimasSalidas[0]`
- `obtenerAceleracion()`: retorna `(ultimasSalidas[1] - 0.5) * 2` → mapea [0,1] a [-1, +1]

### `Poblacion.java` (203 líneas)

Algoritmo genético.

**Campos:**
- `vehiculos` — List<Vehiculo>
- `tamanoPoblacion = 50`
- `generacion` (int, arranca en 1)
- `spawnX, spawnY` — posición de spawn de cada generación
- `recordFitness`, `generacionesEstancadas`

**Constructor:**
```java
Poblacion(pista, inicioX, inicioY)
  → encontrarSpawnPoint() para spawn dinámico
  → Si hay red guardada: crearPoblacionConRed()
  → Sino: crearPoblacion()
```

**`crearPoblacion(x, y)`** — Crea 50 vehículos con redes aleatorias.

**`crearPoblacionConRed(x, y, redBase)`** — Crea 1 vehículo élite (red exacta) + 49 clones mutados (tasa 0.1).

**`update()`** — Por cada vehículo vivo: llama `v.update()`, actualiza `mejorFitness`.

**`siguienteGeneracion()`:**
```
1. evaluarFitness() — asigna fitness = distanciaRecorrida, ordena por fitness
2. Si mejorFitness > recordFitness → guardar red, resetear contador
3. encontrarSpawnPoint() — actualizar spawn para nueva generación
4. crossover(): élite (mejor vehículo) + 49 hijos de selección por torneo
5. mutacion(): tasa 0.1 (0.4 si estancado > 5 generaciones)
```

**`seleccionarPadre()`** — Ruleta de fitness: probabilidad proporcional al fitness de cada vehículo.

### `GestorRed.java` (48 líneas)
- `RUTA_ARCHIVO` = `"app/src/main/resources/mejor_red.json"`
- `guardarRed(RedNeuronal)`: serializa pesos a JSON con Gson
- `cargarMejorRed()`: deserializa JSON, retorna RedNeuronal (o null si no existe)

### `GestorPartida.java` (34 líneas)
- Guarda `PartidaGuardada` como JSON en `partidas/partida_<timestamp>.json`

---

## 7. Paquete `controller`

### `GestorEntradas.java` (31 líneas)

Maneja teclas mediante un `Set<KeyCode>`:
- `setOnKeyPressed` → `presionadas.add(code)`
- `setOnKeyReleased` → `presionadas.remove(code)`

**Métodos:**
- `izquierda()`: LEFT o A
- `derecha()`: RIGHT o D
- `arriba()`: UP o W
- `abajo()`: DOWN o S
- `enter()`: ENTER
- `teclaG()`, `teclaM()`, `teclaR()`, `teclaE()`, `teclaC()`
- `menuArriba()`: UP (solo UP, no W)
- `menuAbajo()`: DOWN (solo DOWN, no S)
- `limpiar()`: vacía el Set

### `ControladorJugador.java` (27 líneas)
Implementa `Controlador`. Lee de `GestorEntradas`:
- `obtenerGiro()`: LEFT → -0.05, RIGHT → +0.05 (acumula)
- `obtenerAceleracion()`: UP → +0.15, DOWN → -0.2

---

## 8. Paquete `view`

### `Renderizador.java` (70 líneas)
Delega a: HUD, RenderizadorVehiculo, GraficoEntrenamiento.

### `HUD.java` (110 líneas)
Métodos estáticos de dibujo:
- `dibujarMenu()`: título, opciones (> seleccionada en cyan), instrucciones
- `dibujarCarrera()`: stats en vivo (distancia, estado, teclas)
- `dibujarEntrenamiento()`: GEN, VIVOS, FIT
- `dibujarResultadoConReintento()`: overlay semitransparente con resultado, estadísticas, opciones R/G/M

### `RenderizadorVehiculo.java` (34 líneas)
- `dibujar()`: translate al centro del vehículo, rotar según ángulo, fillRect
- `dibujarSensores()`: llama sensor.render() en cada sensor
- `dibujarTrayectoria()`: líneas entre puntos de trayectoria

### `GraficoEntrenamiento.java` (65 líneas)
- Dibuja gráfico de líneas de fitness a lo largo de generaciones
- `registrarFitness(f)`: agrega punto
- `limpiar()`: borra historial

---

## 9. Paquete `util`

### `InspectImage.java` (59 líneas)
Herramienta CLI para inspeccionar pista.png:
- Carga la imagen
- Busca píxeles azules (spawn)
- Reporta colores del pixel más azul y del predeterminado (400,500)

---

## 10. Build system

**`app/build.gradle`**:
- Plugins: `java`, `application`, `org.openjfx.javafxplugin` v0.1.0
- JavaFX 17.0.6: módulos `javafx.controls`, `javafx.graphics`
- Dependencias: Gson 2.10.1, JUnit Jupiter 5.9.1
- Main class: `main.Main`

Compilar: `./gradlew compileJava`
Test: `./gradlew test`
Ejecutar: `./gradlew run`

---

## 11. Bug conocido: muerte instantánea

### Síntoma
Al seleccionar "COMPETIR VS IA", ambos vehículos aparecen muertos tras ~30 frames y se muestra "EMPATE!". El jugador reporta "ni siquiera puedo mover mi vehículo".

### Causas probables (en orden de probabilidad)

#### 1. Meta detection falsa en el spawn ← MÁS PROBABLE
Los sensores (`-90°, -45°, 0°, 45°, 90°`) disparan desde la esquina `(spawnX-20, spawnY-10)`. Si algún píxel rojo (meta) está a 2px o 4px de distancia en alguna de esas direcciones, el sensor lo detecta → `haCruzadoMeta=true` → `velocidad=0` → vehículo no se mueve.

**El archivo `pista.png` (JPEG de 1536×1024, estirado a 800×600) TIENE píxeles rojos cerca de los azules.** Esto se debe a que el JPEG no mantiene colores exactos pixel a pixel (compresión con pérdida, + estiramiento 3:2 → 4:3).

#### 2. Distorsión de aspecto (3:2 → 4:3)
La imagen original es 1536×1024 (3:2). El canvas es 800×600 (4:3). JavaFX estira la imagen para llenar el canvas. El `pixelReader` lee de la imagen estirada (800×600), pero los píxeles originales se distorsionan durante el estiramiento, causando que colores que ERAN azules/rojos puros en el original se mezclen con colores adyacentes.

#### 3. Fallback sin validación
En `iniciarCarrera()`, si `esSpawnSeguro(cx-20, cy-10)` falla (por meta cercana o pared), se intenta `cx+20`. Si también falla, se usa `cx-20` SIN VALIDACIÓN. Aunque este es el mismo punto validado por `encontrarSpawnPoint()`, si la validación falló la primera vez, el vehículo se coloca en un punto que `esSpawnSeguro` consideró no seguro.

#### 4. `input.limpiar()` eliminado
Se eliminó `input.limpiar()` de los métodos de inicio de carrera. Con la tecla UP todavía presionada del menú, el vehículo arranca con aceleración extra (+0.15), pero esto no debería matarlo instantáneamente gracias al margen de 10px.

### Lo que se ha intentado (sin éxito hasta ahora)

1. ✅ Aumentar margen de validación de 0.6px a 10px progresivo
2. ✅ Agregar `sensoresDetectanMetaCerca()` para evitar spawn cerca de meta
3. ✅ Reducir umbral del sensor de meta de 5.0 a 3.0
4. ✅ Reducir `sensoresDetectanMetaCerca` de 5 pasos a 2 pasos
5. ✅ Eliminar `input.limpiar()` que confundía al usuario
6. ✅ Eliminar console spam

### Lo que NO se ha intentado

1. **Escalar la imagen correctamente**: Usar `new Image(is)` sin redimensionar (1536×1024 original), dibujar con `gc.drawImage(pista, 0, 0, 800, 600)` y escalar las coordenadas del vehículo para que coincidan. O cambiar el canvas a 800×533 para que coincida con la imagen 3:2.

2. **Buscar spawn en más posiciones Y**: El array `candidatosY` solo prueba ±20px alrededor del pixel más azul. Si la línea azul está en una curva, ninguna posición cercana sirve. Habría que escanear toda la imagen.

3. **Reducir tamaño del vehículo**: De 40×20 a 30×15 para que quepa en espacios más angostos.

4. **Depurar la imagen real**: Escribir un programa que lea la imagen como la lee JavaFX (800×600, con el escalado y filtro bilinear de JavaFX) y muestre los píxeles reales alrededor del spawn.

5. **Ignorar completamente la detección de meta en los primeros N frames**: Agregar contador de frames en Vehiculo y no revisar meta hasta después de N frames (ej: 20).

---

## 12. Bug conocido: IA no aprende

### Síntoma
Después de 500+ generaciones, la IA no mejora. El fitness no sube.

### Causa raíz (probable)
Si todos los vehículos mueren en el mismo frame (ej: todos detectan meta en frame 1), todos tienen fitness ≈ 0.6 (o el mismo valor). La selección por ruleta se vuelve aleatoria, no hay presión evolutiva.

### Lo que se ha intentado
- Aumentar margen de spawn para que los vehículos vivan más frames → más variación en fitness
- `sensoresDetectanMetaCerca` para evitar spawn cerca de meta

### Lo que podría funcionar
- **Reducir el tamaño del vehículo** (30×15) para que quepa en más lugares de la pista
- **Aumentar la población** para tener más diversidad genética
- **Forzar que al menos algunos vehículos tengan giro=0 en los primeros frames** para que se muevan en línea recta y tengan fitness diferenciado

---

## 13. Historial de cambios

### Cambios recientes (esta sesión)

| Archivo | Cambio |
|---------|--------|
| `Vehiculo.java:64` | Meta threshold: `5.0/300.0` → `3.0/300.0` |
| `Pista.java:61-77` | `sensoresDetectanMetaCerca` 5 pasos → 2 pasos |
| `Pista.java` | Eliminados 3 `System.out.println` de spawn |
| `Simulador.java` | Eliminados 3 `input.limpiar()` |
| `Pista.java:119` | Márgenes `{10, 5, 3, 1, 0.6}` progresivos |
| `Pista.java:61-77` | Nuevo método `sensoresDetectanMetaCerca` |
| `Pista.java:154-190` | Nueva refactorización `encontrarSpawnConMargen` |
| `Simulador.java:205-213` | `esSpawnSeguro` con márgenes progresivos + meta check |
| `Simulador.java:131,210,343` | `input.limpiar()` añadido (luego eliminado) |
| `PartidaGuardada.java` | Nuevo archivo (guardar partida) |
| `GestorPartida.java` | Nuevo archivo (guardar partida) |
| `Simulador.java:345-357` | G handler en actualizarCarrera |
| `HUD.java:108` | Mostrar opción G en resultado |
| `Simulador.java:41-42` | Dificultad: FACIL/MEDIO/DIFICIL |
| `Simulador.java:276-301` | Switch de dificultad para IA |
| `Simulador.java:152-160` | `getOpcionesMenuVisuales()` con dificultad |
| `Simulador.java:117-131` | LEFT/RIGHT cambia dificultad en menú |
