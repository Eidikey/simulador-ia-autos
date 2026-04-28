# CHANGELOG

## 2026-04-27 - Fase 1: Arquitectura y Base

### Estructura del Proyecto

- Creado directorio `docs/` con archivo `CHANGELOG.md` para control de cambios.
- Creados paquetes `model`, `engine` y `main` bajo `app/src/main/java/`.

### Modelo Base (Paquete model)

- **Entidad.java** (Abstracta): Clase base para todos los objetos en pantalla.
  - Atributos `protected`: `x`, `y`, `ancho`, `alto`.
  - Metodos abstractos: `update()` para fisica y `render(GraphicsContext gc)` para dibujo.

- **Controlador.java** (Interfaz): Contrato para el manejo del vehiculo.
  - Metodos: `obtenerGiro()` y `obtenerAceleracion()`.
  - Cumple DIP: el vehiculo depende de una abstraccion.

### Motor del Simulador (Paquete engine)

- **Simulador.java**: Controlador del ciclo de vida (Game Loop).
  - Utiliza `javafx.animation.AnimationTimer`.
  - Metodo `handle(long now)` orquesta: limpieza de canvas, actualizacion y renderizado.

### Punto de Entrada (Paquete main)

- **Main.java**: Hereda de `javafx.application.Application`.
  - Configura ventana con `Scene` de 800x600 y `Canvas`.
  - Inicializa `Simulador` y lanza el ciclo.

### Correccion de Entorno

- **gradle.properties**: `org.gradle.configuration-cache=false` para JavaFX en Gradle 9+.
- Eliminados `App.java` y `AppTest.java` obsoletos.

---

## 2026-04-27 - Fase 2: Raycasting y Evolucion (Completada)

### Sensores y Raycasting

- **Sensor.java** (model): 5 rayos (-90, -45, 0, 45, 90 grados).
  - Carga `pista.png` con `PixelReader` estatico.
  - Detecta colision con pixeles negros y normaliza distancia (0 a 1).
  - `render()`: Dibuja lineas amarillas de rayos.

### Inteligencia Artificial

- **RedNeuronal.java** (ai): Arquitectura con capa oculta (4 neuronas).
  - `feedForward()`: Procesa entradas aplicando sigmoide.
  - `getPesosComoArray()` y `setPesosDesdeArray()` para persistencia.

- **ControladorIA.java** (ai): Implementa `Controlador`.
  - Procesa sensores via red neuronal.

### Algoritmo Genetico

- **Poblacion.java** (ai): Gestiona 50 vehiculos.
  - Fitness basado en distancia recorrida.
  - `crossover()` y `mutacion()` para nueva generacion.

### Vehiculo Actualizado

- **Vehiculo.java**: Integra 5 sensores y `ControladorIA`.
  - Detecta colision y marca `vivo = false`.

---

## 2026-04-27 - Fase 3: Telemetria UI en Canvas (Iniciada)

### Dashboard HUD

- **Simulador.java**: Renderiza en tiempo real usando `GraphicsContext.fillText()`.
  - Muestra: Generacion actual, Autos vivos, Mejor Fitness.
  - Game loop actualiza y renderiza poblacion entera.

### Resultado de Compilacion

- `./gradlew :app:build`: BUILD SUCCESSFUL
- `./gradlew :app:run`: EXITOSO (Ventana JavaFX con simulacion de 50 autos)

---

## 2026-04-27 - Hotfix: Renderizado y Optimizacion de IA

### Renderizado de la Pista

- **Simulador.java** (engine): Ahora renderiza `pista.png` como fondo en el Canvas.
  - Carga la imagen en el constructor usando `getResourceAsStream`.
  - En el `handle()`, dibuja la imagen antes de renderizar la poblacion.

### Ajuste de Spawn y Angulo Seguro

- **Poblacion.java** (ai): Coordenadas de spawn ajustadas a `x=150, y=400`.
  - En `siguienteGeneracion()`, se establece `angulo=0` para apuntar al camino libre.
  - Se usa `setAngulo(0)` en la creacion de nuevos vehiculos.

### Mejora de la Funcion de Fitness

- **Vehiculo.java** (model): Evita minimos locales en la evolucion.
  - Nueva metrica: `distanciaLineal` premia el avance hacia adelante usando `cos(angulo)`.
  - Penalizacion: Si `velocidad < 0.5` por mas de 60 frames, el vehiculo muere.
  - `getDistanciaRecorrida()` devuelve `distanciaLineal` si es positiva, sino `distanciaRecorrida`.
  - Velocidad inicial ajustada a `0.5` para evitar spawn estancado.

---

## 2026-04-27 - Hotfix: Spawn y Telemetria HUD

### Correccion de Coordenadas de Inicio (Spawn)

- **Poblacion.java** (ai): Ajustadas coordenadas de spawn a `x=400.0, y=500.0`.
  - Ubicacion central en area blanca de la pista para evitar colision prematura en frame 1.
  - Angulo inicial en `0` grados apuntando hacia el camino libre.

### Implementacion de HUD de Telemetria

- **Simulador.java** (engine): Renderizado de texto en Canvas con alto contraste.
  - Configuracion: `Color.BLUE` y `Font(20)` para visibilidad.
  - Posicion: Esquina superior izquierda (`x=20, y=30`).
  - Datos mostrados: Generacion actual, Autos vivos / Total, Mejor Fitness redondeado.

- **Poblacion.java** (ai): Anadido metodo `getTotal()` para exponer el tamano de la poblacion.

---

## 2026-04-27 - Hotfix: Restauracion de Simulador.java y Trigonometria

### Restauracion de Simulador.java (Fase 3)

- Reescrito `Simulador.java` (engine) siguiendo estrictamente los requisitos de Fase 3.
  - Atributos: `Image imagenPista` y `Poblacion poblacion`.
  - Constructor: Carga pista con `Objects.requireNonNull(getClass().getResourceAsStream("/pista.png"))`.
  - Instancia poblacion con `new Poblacion(400, 400)` (coordenadas centrales).
  - Eliminado todo rastro de `controladorDummy` y vehiculo individual.
  - `handle()`: Dibuja fondo, actualiza y renderiza poblacion, y muestra HUD.
  - HUD: Color blanco, textos en esquina superior izquierda (Generacion, Autos Vivos, Mejor Fitness).

### Correccion de Trigonometria en Vehiculo.java

- `angulo` ahora se maneja estrictamente en grados (0 a 360).
- Sensores inicializados con grados: `-90, -45, 0, 45, 90`.
- Actualizacion de coordenadas usa `Math.toRadians(angulo)` para Java.
- `distanciaLineal` tambien usa `Math.toRadians(angulo)`.
- Anadido metodo `getAngulo()` para exponer el angulo.

### Ajustes en Poblacion.java

- Constructor simplificado a `Poblacion(double x, double y)` con tamano fijo de 50 autos.
- Atributos `spawnX` y `spawnY` ahora variables para permitir nuevos valores.

---

## 2026-04-27 - Hotfix: Ciclo de Vida y HUD Invisible

### Reparacion del Reset de Generacion (Poblacion.java)

- Corregido el salto generacional en `siguienteGeneracion()`.
  - Se elimino el uso de coordenadas de vehiculos muertos para crear nuevos autos.
  - Ahora se instancia `Vehiculo(spawnX, spawnY, 40, 20, ci)` con coordenadas de spawn explicitas.
  - Se establece `angulo = 0` en cada nuevo vehiculo de la siguiente generacion.
  - `crossover()` ahora crea el vehiculo elite y las nuevas generaciones usando las coordenadas de spawn seguras.
  - Orden de operaciones corregido: primero se llena `nuevaGeneracion`, luego se hace `clear()` y `addAll()`.

### Correccion de Orden de Renderizado y HUD (Simulador.java)

- Orden estricto en `handle()` del AnimationTimer:
  1. `gc.clearRect(0, 0, width, height)` - Limpieza total del canvas.
  2. `gc.drawImage(imagenPista, ...)` - Dibujado de la mascara de colisiones.
  3. `poblacion.update()` y `poblacion.render(gc)` - Logica y dibujado de vehiculos.
  4. HUD - Configuracion explicita: `Color.WHITE` y `Font(18)`.
  5. Textos: "Generacion: X", "Autos Vivos: X", "Mejor Fitness: X" en esquina superior izquierda.

### Validacion de Limites (Vehiculo.java)

- En `update()`: Si el vehiculo sale de los limites del canvas (x < 0, x > 800, y < 0, y > 600), muere instantaneamente.
  - Esto evita bucles infinitos fuera del canvas.

---

## 2026-04-27 - Hotfix: Angulos en Sensores y Visibilidad de HUD

### Correccion Critica de Angulos en Sensores (Sensor.java)

- **Bug encontrado**: Los sensores usaban grados directamente en `Math.cos/sin` (que requieren radianes).
  - Linea 47-49: `anguloVehiculo + anguloRelativo` devolvia grados (ej. 90), pero `Math.cos(90)` en Java da `-0.448` en lugar de `0`.
  - **Resultado**: Los rayos apuntaban en direcciones erraticas, detectando "pared" a distancia 0 y matando a los vehiculos instantaneamente.
- **Solucion**: Se convierte la suma a radianes con `Math.toRadians()` antes de calcular `dx` y `dy`.
  - `double anguloGrados = anguloVehiculo + anguloRelativo;`
  - `double anguloRad = Math.toRadians(anguloGrados);`

### Correccion de Visibilidad del HUD (Simulador.java)

- **Bug encontrado**: El HUD usaba `Color.WHITE` sobre la pista que tiene zonas blancas, haciendo el texto invisible.
- **Solucion**: Cambiado a `Color.BLACK` para contraste contra el fondo blanco de la pista.

---

## 2026-04-27 - Hotfix: Sistema de Coordenadas de Colisiones

### Resolucion de Muerte Instantanea de Autos (Sensor.java y Simulador.java)

- **Bug Raiz**: Los autos morian instantaneamente aunque no chocaran con paredes negras.
- **Causa**: `Sensor.java` cargaba `pista.png` en su tamano natural (ej. 400x300), mientras `Simulador.java` estiraba la imagen al canvas (800x600).
  - Los sensores usaban coordenadas del canvas (0-800, 0-600) para leer pixeles de una imagen mas pequena.
  - Ejemplo: Auto en (500, 300) en canvas → Sensor verifica `x (500) < PISTA.getWidth() (400)` → FALSE → detecta "pared" inexistente.
- **Solucion Aplicada (Opcion A)**:
  - `Sensor.java`: Carga imagen con `new Image(is, 800, 600, false, true)` para igualar dimensiones del canvas.
  - `Simulador.java`: Carga imagen con `new Image(..., 800, 600, false, true)`.
  - Ahora ambas imagenes tienen exactamente 800x600 pixeles, coincidiendo con el sistema de coordenadas del canvas.

---

## 2026-04-27 - Fase 2: Nucleo de Neuroevolucion (Completada)

### Operadores Geneticos en RedNeuronal.java

- Anadido metodo `mutar(double tasaMutacion)` para encapsular la logica de mutacion.
  - Itera sobre `pesosEntradaOculta` y `pesosOcultaSalida`.
  - Si `Math.random() < tasaMutacion`, altera el peso sumando un valor entre -1.0 y 1.0.
- `getPesosComoArray()` y `setPesosDesdeArray()` ya implementados para serializacion de pesos.

### Calculo de Fitness en Vehiculo.java

- Corregido: `angulo` ya esta en radianes, eliminada conversion `toRadians()` innecesaria en `update()`.
- La distancia recorrida usa distancia euclidiana (mejor para tracks que `abs(velocidad)`).
- `getDistanciaRecorrida()` devuelve directamente `distanciaRecorrida` sin condicionales.

### Evolucion en Poblacion.java

- Corregido bug de referencia compartida en el elitismo (lineas 91-96).
  - Elite ahora tiene su propia `RedNeuronal` clonada via `setPesosDesdeArray(getPesosComoArray())`.
  - Se crea nuevo `ControladorIA` para el elite, evitando mutaciones colaterales.
- `mutacion()` refactorizada para usar `ci.getRed().mutar(0.1)` en lugar de logica hardcodeada.
- `crossover()` ahora aplica `redHijo.mutar(0.1)` a los hijos generados.

### Resultado

- Los autos ahora aprenden realmente entre generaciones.
- Elite preserva la mejor red neuronal sin mutar.
- Poblacion se regenera correctamente cada vez que todos mueren.

---

## 2026-04-27 - Hotfix: Generacion de Nuevas Poblaciones

### Bug Critico: No se Generaban Nuevas Poblaciones

- **Bug encontrado**: En `Simulador.java`, el metodo `handle()` no verificaba si todos los autos habian muerto.
- **Causa**: La logica de `if (poblacion.todosMuertos()) { poblacion.siguienteGeneracion(); }` se habia eliminado accidentalmente en una refactorizacion anterior.
- **Solucion**: Restaurada la verificacion en `initTimer()`:
  ```java
  if (poblacion.todosMuertos()) {
      poblacion.siguienteGeneracion();
  }
  ```
- **Resultado**: Ahora cuando los 50 autos mueren, automaticamente se genera una nueva generacion con elitismo, crossover y mutacion.

---

## 2026-04-27 - Hotfix: Fitness y Minimo Local

### Mejora de Funcion de Fitness en Vehiculo.java

- **Bug**: La IA encontro un minimo local girando en circulos para sobrevivir.
- **Solucion**:
  - Anadidas variables `startX`, `startY` (coordenadas iniciales) y `rotacionAcumulada`.
  - En `update()`: Se suma `Math.abs(giro)` a `rotacionAcumulada`.
  - **Nueva formula de Fitness**:
    - `distanciaAvance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));`
    - `fitness = distanciaAvance - (rotacionAcumulada * 0.5);`
  - **Penalizacion mortal**: Si `rotacionAcumulada > Math.toRadians(360)` Y `distanciaAvance < 50`, el auto muere instantaneamente.

### Impulso Inicial en Vehiculo.java

- **Bug**: La red neuronal no exploraba hacia adelante.
- **Solucion**: En `update()`, se anade un impulso base de `0.1` a la aceleracion:
  - `velocidad += aceleracion + 0.1;`
  - Esto obliga a la red a considerar el movimiento hacia adelante desde el inicio.

---

## 2026-04-27 - Fase 4: Persistencia de Modelo (JSON)

### Implementacion de GestorRed.java (ai)

- Creada clase para manejar serializacion de pesos usando Gson.
- `guardarRed(RedNeuronal red)`: Exporta pesos a `app/src/main/resources/mejor_red.json`.
- `cargarMejorRed()`: Importa pesos desde JSON, devuelve una nueva `RedNeuronal` o `null`.

### Evolucion con Persistencia (Poblacion.java)

- Anadida variable `recordFitness` para tracking del récord historico.
- Constructor ahora intenta cargar `GestorRed.cargarMejorRed()` al iniciar.
- Nuevo metodo `crearPoblacionConRed()`: Crea poblacion usando red cargada como base para el elite.
- En `siguienteGeneracion()`: Si `mejorFitness > recordFitness`, se guarda automaticamente la mejor red.

### Tecla de Emergencia (Simulador.java)

- Implementado `scene.setOnKeyPressed`: Tecla 'G' fuerza el guardado de la mejor red de la generacion actual.
- Permite al usuario preservar un modelo exitoso en cualquier momento.

---

## 2026-04-27 - Hotfix: Estancamiento Genético y Frenado

### Mutacion Dinamica en Poblacion.java

- Anadida variable `generacionesEstancadas` para detectar falta de mejora.
- En `siguienteGeneracion()`: Si `mejorFitness > recordFitness`, se reinicia contador a 0. Si no, se incrementa.
- Mutacion dinamica: Si `generacionesEstancadas > 5`, la tasa sube a 0.4 (fuerza diversidad). Si es menor, mantiene 0.1.

### Ampliacion del Rango de Percepcion en Sensor.java

- `MAX_DISTANCIA` aumentada de 200.0 a 300.0 pixeles.
- En `medirDistancia()`: El rayo ahora avanza de 2 en 2 pixeles (mas rapido) hasta 300.
- Normalizacion a 0.0-1.0 se mantiene respecto al nuevo maximo.

### Habilidad de Frenado en ControladorIA.java

- Modificado `obtenerAceleracion()`: Ahora mapea la salida de la red (0 a 1) a un rango de -1 a 1.
- Formula: `(raw - 0.5) * 2`. Valores menores a 0 actuan como freno.
- En Vehiculo.java: `velocidad += aceleracion` ahora permite reducir velocidad para tomar curvas.

### Resultado

- El algoritmo ya no se estanca en fitness 1338.
- Los autos detectan curvas con mayor anticipacion (300 pixeles).
- Capacidad de frenado implementada para curvas cerradas.
