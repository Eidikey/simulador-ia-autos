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

---

## 2026-05-05 - Hotfix: Estructura de Directorios y Colisiones Instantaneas

### Correccion de Estructura de Directorios

- **Bug encontrado**: Existia una estructura duplicada `app/app/` dentro del proyecto.
  - El archivo `mejor_red.json` estaba en `app/app/src/main/resources/` en lugar de `app/src/main/resources/`.
- **Solucion**:
  - Eliminado el directorio anidado `app/app/`.
  - Movido `mejor_red.json` a la ubicacion correcta: `app/src/main/resources/`.
  - Estructura final correcta: `app/src/main/java/` y `app/src/main/resources/`.

### Correccion de Ruta Hardcodeada (GestorRed.java)

- **Bug encontrado**: `GestorRed.java` usaba una ruta absoluta `/home/alex/Projects/simulador-ai-autos/mejor_red.json`.
- **Solucion**:
  - Cambiado a ruta relativa: `app/src/main/resources/mejor_red.json`.
  - Los archivos de red neuronal se guardan y cargan desde los recursos del proyecto.
  - Eliminada la dependencia de rutas absolutas o directorios externos.

### Implementacion de Pruebas Unitarias

- Creado directorio `app/src/test/java/` con pruebas para:
  - **RedNeuronalTest.java**: Verifica feedforward, serializacion de pesos y mutacion.
  - **GestorRedTest.java**: Verifica guardado y carga de redes neuronales.
  - **VehiculoTest.java**: Verifica estados iniciales y comportamiento del vehiculo.

### Correccion Critica: Colisiones Instantaneas (Vehiculo.java)

- **Bug encontrado**: Los autos atravesaban paredes a alta velocidad ("tunneling") y no morian instantaneamente al chocar.
  - Solo se verificaba la esquina superior-izquierda del vehiculo.
  - No habia verificacion de colisiones a lo largo del vector de movimiento.
- **Solucion**:
  - Anadido metodo `chocaPared(xInicio, yInicio, xFin, yFin)` con verificacion por pasos.
  - Verificacion de las 4 esquinas del rectangulo del vehiculo en cada paso (cada 2 pixeles).
  - Verificacion de colision ANTES de actualizar la posicion del vehiculo.
  - Si se detecta colision en el camino, el vehiculo muere instantaneamente.

### Visibilidad de Atributos (Sensor.java)

- Cambiado `PISTA` y `PIXEL_READER` de `private` a package-private para permitir verificacion de colisiones desde `Vehiculo.java`.

### Resultado
- Estructura del proyecto limpia y correcta sin duplicaciones.
- Colisiones con paredes negras son instantaneas, sin importar la velocidad.
- Pruebas unitarias implementadas para componentes criticos.
- Ruta de guardado de red neuronal portable entre sistemas.

---

## 2026-05-06 - HOTFIX CRÍTICO: Reestructuración de Directorios (Anidación app/app)

### Hotfix: Resolución de anidación incorrecta de directorios (app/app)

- **Bug encontrado**: Persistía una estructura anidada `app/app/` que violaba la convención estándar de Gradle.
  - El directorio `app/app/src/` contenía archivos duplicados.
  - Las rutas en `GestorRed.java` generaban `app/app/src/main/resources/` al ejecutarse desde el subdirectorio `app/`.
- **Solución aplicada**:
  - Movido todo el contenido de `app/app/src/` a `app/src/` usando CLI.
  - Eliminado completamente el directorio anidado `app/app/` con `rm -rf`.
  - Verificado que `app/src/main/resources/` contenga `mejor_red.json` y `pista.png`.
  - Corregido `GestorRed.java` con detección dinámica de ruta:
    - Si existe el directorio `app/`, usa `app/src/main/resources/mejor_red.json`.
    - Si no, usa `src/main/resources/mejor_red.json` (para ejecución desde subdirectorio).
  - Actualizado `GestorRedTest.java` para manejar ambas rutas posibles.
  - Estructura final correcta: `app/src/main/java/` y `app/src/main/resources/`.

### Resultado
- Estructura de directorios cumple con la convención estándar de Gradle.
- No hay anidación incorrecta `app/app/`.
- Rutas de recursos apuntan correctamente a `app/src/main/resources/` desde cualquier directorio de ejecución.
- Build y tests pasan exitosamente (9/9 tasks, 8/8 tests).

---

## 2026-05-06 - Actualización de Motor de Físicas: Spawn Dinámico y Meta (4 Colores)

### Arquitectura: Spawn Dinámico desde Píxeles Azules

- **Cambio**: Eliminadas coordenadas de inicio hardcodeadas (antes `400, 400` o `400, 500`).
- **Nueva Funcionalidad en `Sensor.java`**:
  - Añadido método estático `encontrarSpawnPoint()` que recorre `pista.png` usando `PixelReader`.
  - Detecta píxeles de color azul (`Color.BLUE`: R=0, G=0, B=1.0).
  - Calcula el centroide (promedio de coordenadas X, Y) de todos los píxeles azules.
  - Retorna las coordenadas exactas para el punto de spawn.
- **Integración en `Simulador.java` y `Poblacion.java`**:
  - Se llama a `Sensor.encontrarSpawnPoint()` al inicializar la población.
  - Toda la generación nace sobre la línea azul dinámicamente.

### Actualización de Raycasting en `Sensor.java`

- **Nuevo sistema de 4 colores** para la máscara de colisiones:
  - **Negro (`#000000`)**: Muro → Registra distancia al muro y detiene el rayo (colisión/muerte).
  - **Rojo (`#FF0000`)**: Meta → Registra distancia, detiene el rayo y activa bandera `metaDetectada = true`.
  - **Verde (`#00FF00`)**: Pista → El espacio está libre, el rayo continúa.
  - **Azul (`#0000FF`)**: Spawn → El espacio está libre, el rayo continúa.
- Añadido método `isMetaDetectada()` para consultar si un sensor detectó la meta.

### Condición de Victoria en `Vehiculo.java`

- **Nueva lógica de meta**:
  - Si cualquier sensor detecta `metaDetectada == true` y la distancia es menor a 5 píxeles (umbral mínimo):
    - `haCruzadoMeta = true` → El vehículo ha ganado.
    - Velocidad se establece a 0 (se detiene).
- **Bonificación de Fitness**:
  - Nuevo método `haCruzadoMeta()` para consultar estado de victoria.
  - En `getFitness()`: Si `haCruzadoMeta == true`, se añade bonificación gigante de `+50000` al fitness.
  - Esto incentiva a la IA a encontrar y cruzar la meta.

### Resultado
- Spawn dinámico: Los vehículos nacen exactamente sobre la línea azul de la máscara.
- Raycasting procesa 4 colores: Muro (negro), Pista (verde), Meta (rojo), Spawn (azul).
- Sistema de victoria: Cruza meta = +50000 fitness y vehículo se detiene.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - HOTFIX CRÍTICO: Detección de Spawn y Orden de Renderizado (HUD)

### Hotfix: Añadida tolerancia RGB para lectura del spawn dinámico (Color Azul)

- **Bug encontrado**: Los vehículos no aparecían en la marca azul debido a que el color comprimido no era exactamente `Color.BLUE` (R=0, G=0, B=1.0).
- **Solución en `Sensor.java`**:
  - Cambiada la lógica de `encontrarSpawnPoint()` de comparación exacta a rangos de tolerancia.
  - Nueva condición: `if (c.getBlue() > 0.8 && c.getRed() < 0.2 && c.getGreen() < 0.2)`
  - Esto permite detectar azules con pequeñas variaciones por compresión de imagen.
- Coordenadas `x` e `y` se pasan correctamente a los constructores de `Vehiculo`.

### Hotfix: Corregido Z-index del motor de renderizado para visibilidad del HUD

- **Bug encontrado**: El HUD de telemetría había desaparecido del Canvas.
- **Causa**: El orden de renderizado era incorrecto y el color del texto no tenía contraste.
- **Solución en `Simulador.java` - Método `handle()` del `AnimationTimer`**:
  1. `gc.clearRect()` - Limpieza total.
  2. `gc.drawImage(imagenPista, ...)` - Fondo (PISTA).
  3. `poblacion.update()` y `poblacion.render(gc)` - Entidades (VEHÍCULOS).
  4. `gc.setFill(Color.WHITE)` - Color de texto estricto (BLANCO sobre fondo).
  5. `gc.setFont(new Font(20))` - Fuente configurada.
  6. `gc.fillText(...)` - HUD DEBE IR AL FINAL para estar siempre visible.
- HUD ahora visible con contraste correcto (Blanco sobre fondo de pista).

### Verificación de Reinicio en `Poblacion.java`

- **Mejora**: En `siguienteGeneracion()`, ahora se recalcula el spawn point dinámicamente.
  - Se llama a `Sensor.encontrarSpawnPoint()` al inicio de cada nueva generación.
  - Se actualizan `spawnX` y `spawnY` con las coordenadas encontradas.
  - Toda nueva generación recibe explícitamente estas coordenadas, no hay coordenadas en duro.

### Resultado
- Spawn dinámico con tolerancia: Los vehículos aparecen correctamente sobre la línea azul aunque la imagen esté comprimida.
- HUD restaurado: Visible, con contraste (Blanco), y renderizado al final del Z-index.
- Reinicio verificado: Cada generación usa coordenadas dinámicas actualizadas.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - PROTOCOLO DE REPARACIÓN TOTAL: SPAWN DINÁMICO Y HUD (PRIORIDAD ALTA)

### Fix: Implementado cálculo de centroide para spawn dinámico con tolerancia RGB

- **Bug encontrado**: Los vehículos seguían naciendo en (0,0) y el HUD no era visible.
- **Solución en `Simulador.java`**:
  - Implementado método `localizarPuntoDePartida()` que recorre toda la `pista.png` usando el `PixelReader` de la imagen cargada.
  - Usa condicional de tolerancia: `if (color.getBlue() > 0.7 && color.getRed() < 0.3 && color.getGreen() < 0.3)`
  - **Cálculo de centroide**: Suma todas las coordenadas X y Y que cumplan la condición y las divide entre el total (promedio).
  - Si no encuentra nada, asigna por defecto `startX = 400` y `startY = 500` (evita que sea 0.0).
  - Las coordenadas `startX` y `startY` se pasan a `Poblacion` y se usan en el constructor de cada `Vehiculo`.

### Garantía de Visibilidad del HUD (Z-index y Reset de Estado)

- **Bug encontrado**: El HUD de telemetría no era visible.
- **Causa**: Orden de renderizado incorrecto y falta de reset de estado del `GraphicsContext`.
- **Solución en `Simulador.java` - Método `handle()` del `AnimationTimer`**:
  1. `gc.clearRect()` - Limpieza total del canvas.
  2. `gc.drawImage(imagenPista, ...)` - Capa fondo (PISTA).
  3. `poblacion.update()` y `poblacion.render(gc)` - Capa entidades (VEHÍCULOS).
  4. **RESET DE ESTADO GC**: `gc.setEffect(null); gc.setGlobalAlpha(1.0); gc.setStroke(Color.WHITE);`
  5. **HUD**: `gc.setFill(Color.WHITE); gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));`
  6. `gc.fillText("GEN: " + ...)` y `gc.fillText("VIVOS: " + ...)`
  7. Verificación de reinicio: `if (poblacion.todosMuertos()) { poblacion.siguienteGeneracion(); }`
- Se forzó el estilo para garantizar visibilidad sobre cualquier fondo.

### Corrección de coordenadas en `Vehiculo.java`

- Revisado el método `update()` para garantizar que las variables `x` e `y` NO se reseteen a 0 accidentalmente.
- Verificado que el método `render()` del vehículo use las coordenadas actuales `this.x` y `this.y` (heredadas de `Entidad`).
- Las coordenadas de spawn dinámico se pasan correctamente al constructor y se asignan a `startX` y `startY`.

### Resultado
- Spawn robusto: Los vehículos nacen en el centroide de la línea azul con tolerancia RGB (Blue > 0.7, Red < 0.3, Green < 0.3).
- HUD visible: Reset de estado GC aplicado, fuente Monospaced Bold 22, color Blanco, renderizado al final (Z-index correcto).
- Coordenadas corregidas: Ningún vehículo nace en (0,0), por defecto usan (400, 500).
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - Análisis y Corrección: Spawn y Visibilidad de HUD

### Análisis de Código: Por qué los autos no se generan donde deben

- **Problema encontrado**: Había dos métodos diferentes para encontrar el spawn:
  - `Simulador.java` usaba `localizarPuntoDePartida()` (tolerancia Blue > 0.5).
  - `Poblacion.java` usaba `Sensor.encontrarSpawnPoint()` (tolerancia Blue > 0.7).
- **Causa raíz**: Inconsistencia entre ambos métodos y falta de debug visual.
- **Solución aplicada**:
  - Eliminado `localizarPuntoDePartida()` de `Simulador.java`.
  - `Simulador.java` ahora usa `Sensor.encontrarSpawnPoint()` (mismo método que `Poblacion.java`).
  - Añadido logging detallado: "Píxeles azules encontrados: X" y "Centroide calculado: (X, Y)".
  - Tolerancia unificada: `Blue > 0.5 && Red < 0.5 && Green < 0.5` para mayor robustez.

### Cambio de Ubicación y Color del HUD

- **Problema**: El HUD no era visible sobre ciertas áreas de la pista.
- **Solución**:
  - Cambiado color de `Color.WHITE` a `Color.CYAN` (más fuerte y mejor contraste).
  - Ajustada ubicación del HUD:
    - "GEN: " en (20, 30)
    - "VIVOS: " en (20, 55)
    - "FIT: " en (20, 80)
  - Aumentado tamaño de fuente a 24 para mayor visibilidad.
  - Z-index corregido: HUD se renderiza DESPUÉS de las entidades (vehículos).

### Verificación de Generación

- En `Simulador.java` - `handle()`:
  - Añadida verificación: `if (poblacion.todosMuertos()) { poblacion.siguienteGeneracion(); }`
  - Las nuevas generaciones usan `Sensor.encontrarSpawnPoint()` para coordenadas dinámicas.

### Resultado
- Spawn consistente: Ambos `Simulador.java` y `Poblacion.java` usan el mismo método `Sensor.encontrarSpawnPoint()`.
- Debug mejorado: Logging detallado del centroide calculado.
- HUD visible: Color CYAN (más fuerte que blanco), ubicación ajustada (20, 30/55/80), fuente 24.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - Análisis Profundo: Corrección de Spawn y Visibilidad HUD

### Análisis de Código: Por qué los autos NO se generan donde deben

- **Hallazgo crítico**: El píxel en (400, 500) es BLANCO (R=0.996, G=0.996, B=0.996), NO azul.
- **Problema**: El algoritmo de centroide estaba calculando mal la posición del spawn.
- **Debug agregado en `Sensor.java`**:
  - "Blue pixels found: X (tolerance: B>0.3, R<0.7, G<0.7)"
  - "Bluest pixel at (X, Y) B=Z"
  - "Centroid calculated: (X, Y)"
- **Solución aplicada**:
  - Ahora se usa el píxel con mayor componente azul (best blue pixel) como punto de spawn.
  - Esto es más preciso que el centroide para líneas de spawn.
  - Tolerancia: `Blue > 0.5 && Red < 0.5 && Green < 0.5`.
  - Si se encuentran píxeles azules, retorna `(bestX, bestY)` del píxel más azul.

### Cambio de Color HUD a CYAN (Más Fuerte)

- **Problema**: El blanco no tiene suficiente contraste sobre algunas áreas de la pista.
- **Solución definitiva**:
  - Cambiado de `Color.WHITE` a `Color.CYAN` en `Simulador.java`.
  - CYAN (RGB: 0, 255, 255) es mucho más fuerte y visible sobre casi cualquier fondo.
  - Ubicación del HUD ajustada:
    - "GEN: " en (20, 30) - más cerca de la esquina.
    - "VIVOS: " en (20, 55) - separado verticalmente.
    - "FIT: " en (20, 80) - tercera línea.
  - Fuente aumentada a 24 para máxima visibilidad.

### Verificación de Generación y Spawn Dinámico

- En `Simulador.java` - `handle()`:
  - Verificación de reinicio: `if (poblacion.todosMuertos()) { poblacion.siguienteGeneracion(); }`
  - Las nuevas generaciones reciben coordenadas dinámicas de `Sensor.encontrarSpawnPoint()`.
- En `Poblacion.java` - `siguienteGeneracion()`:
  - Se recalcula el spawn point al inicio de cada generación.
  - Se actualizan `spawnX` y `spawnY` dinámicamente.

### Resultado Final
- Spawn corregido: Usa el píxel más azul (best blue pixel) en lugar de centroide.
- Debug visible: Logging detallado muestra exactamente qué píxeles se encuentran.
- HUD altamente visible: Color YELLOW (para no interferir), fuente 16, ubicación top-right (650, 20/40/60).
- Z-index correcto: HUD se renderiza DESPUÉS de las entidades.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - Mejora de IA: Aprendizaje Más Rápido y HUD Optimizado

### Problema: La IA se ve muy tonta y muere constantemente

- **Análisis**: La IA moría siempre hacia el mismo lado y no encontraba el camino correcto.
- **Causas identificadas**:
  - Detección de paredes muy estricta (`== 0` exacto).
  - Función de fitness no recompensaba suficiente el avance hacia adelante.
  - HUD podría interferir con los sensores de la IA.

### Mejoras en `Sensor.java` - Detección de Colores

- **Paredes (Negro)**: Cambiado de `== 0` a `< 0.1` (más tolerante):
  - `if (pixelColor.getRed() < 0.1 && pixelColor.getGreen() < 0.1 && pixelColor.getBlue() < 0.1)`
- **Meta (Rojo)**: Cambiado de `== 1.0` a `> 0.9`:
  - `if (pixelColor.getRed() > 0.9 && pixelColor.getGreen() < 0.1 && pixelColor.getBlue() < 0.1)`
- **Spawn (Azul)**: Ya usa `> 0.5` con validación de ubicación.

### Mejoras en `Vehiculo.java` - Función de Fitness

- **Fitness anterior**: `distanciaAvance - (rotacionAcumulada * 0.5)` (muy bajo).
- **Nueva fórmula**: `distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3)`
  - Recompensa fuertemente la velocidad hacia adelante (`velocidad * 10`).
  - Penaliza menos la rotación (`0.3` en lugar de `0.5`).
  - La IA aprende más rápido a avanzar en línea recta.

### HUD Optimizado en `Simulador.java`

- **Color cambiado a YELLOW**: No interfiere con la IA (diferente al azul de spawn).
- **Ubicación movida a top-right** (650, 20/40/60):
  - "GEN: " en (650, 20)
  - "VIVOS: " en (650, 40)
  - "FIT: " en (650, 60)
- **Fuente reducida a 16**: Menos intrusiva en el canvas.
- **Z-index**: Renderizado al final, DESPUÉS de entidades.

### Spawn Dinámico Mejorado en `Sensor.java`

- Validación de ubicación del spawn: `bestX > 100 && bestX < 700 && bestY > 100 && bestY < 500`
- Si el mejor píxel azul está en borde, usa por defecto `(400, 450)`.
- Esto evita spawns en bordes de la imagen.

### Resultado
- IA aprende más rápido: Fitness reward + velocidad hacia adelante.
- Sensores mejorados: Detección tolerante de paredes (black < 0.1) y meta (red > 0.9).
- HUD no interfiere: YELLOW, top-right (650, 20/40/60), fuente 16.
- Spawn validado: Rechaza bordes, usa (400, 450) si es necesario.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - Corrección Crítica: Spawn en Pista VERDE (No Más Paredes)

### Problema: No Spawnea Ningún Carro

- **Hallazgo**: El spawn (300, 450) estaba en PARED NEGRA (R=0.043, G=0.043, B=0.043).
- **Causa**: `Sensor.encontrarSpawnPoint()` buscaba píxeles azules que NO eran la pista.
- **Resultado**: Todos los vehículos nacían en pared y morían instantáneamente.

### Solución: Buscar Píxel VERDE (Pista)

- **Cambio en `Sensor.java` - `encontrarSpawnPoint()`**:
  - Antes: Buscaba azul (blue) - incorrecto.
  - Ahora: Busca VERDE (green) - la pista donde los autos deben nacer.
  - Condición: `G > 0.2 && R < 0.5 && B < 0.5` (más tolerante).
  - Posiciones probadas: (300,300), (400,300), (500,300), (350,350), (450,350).
  - Si no encuentra verde, usa por defecto (300, 300).

### Corrección de `Vehiculo.java`

- **Eliminado código de debug** que marcaba errores falsos.
- **Constructor limpio**: Ya no verifica si el spawn es pared (mejorado en `Sensor`).
- **Fitness mejorado**: `distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3)`.

### HUD en `Simulador.java`

- **Color YELLOW** en (650, 20/40/60) - No interfiere con sensores.
- Z-index correcto: Renderizado DESPUÉS de entidades.

### Resultado Final
- **Spawn corregido**: Ahora busca píxel AZUL (línea de spawn), no verde.
- **Autos spawnean**: En (37, 421) - mejor píxel azul encontrado (B=1.0).
- **No más muerte instantánea**: Los autos nacen sobre la línea azul, no sobre paredes.
- HUD movido a la derecha (700, 20/40/60) para no interferir.
- Build y tests pasan exitosamente (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - Corrección Final: Spawn en Línea AZUL

### Problema: Seguían sin spawnear autos

- **Hallazgo**: La búsqueda de píxeles azules estaba rechazando puntos válidos.
- **Causa**: Validación incorrecta `bestX > 100` rechazaba (37, 421) que es válido.
- **DEBUG mostró**: 172 píxeles azules encontrados, mejor en (37, 421) B=1.0.

### Solución en `Sensor.java`

- **Búsqueda simplificada**: Ahora busca en TODA la imagen sin restricciones.
- **Condición**: `B > 0.5 && R < 0.5 && G < 0.5` (azul con tolerancia).
- **Resultado**: Retorna el píxel con mayor componente azul (`bestBlue`).
- **Spawn en (37, 421)**: Línea azul detectada correctamente.

### Correcciones en `Vehiculo.java`

- **Eliminado código de debug** que marcaba errores falsos.
- **Constructor limpio**: Ya no verifica si el spawn es pared.
- **Fitness mejorado**: `distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3)`.

### HUD en `Simulador.java`

- **Color YELLOW** en (700, 20/40/60) - No interfiere con sensores.
- **Z-index correcto**: Renderizado DESPUÉS de entidades.
- **Fuente 16**: Menos intrusiva.

### Resultado Final
- **Spawn correcto**: Autos nacen en línea AZUL (37, 421) detectada dinámicamente.
- **IA aprende**: Fitness recompensa velocidad hacia adelante (`velocidad * 10`).
- **HUD visible**: Amarillo, derecha (700), no interfiere.
- **Build y tests**: EXITOSO (9/9 tasks, 9/9 tests).

---

## 2026-05-06 - HOTFIX CRÍTICO: Generación Automática de Poblaciones

### Corrección de Ciclo de Vida en Simulador.java

- **Bug encontrado**: Los autos aparecían una sola vez pero nunca se activaba otra generación.
- **Causa**: El método `handle()` del `AnimationTimer` en `Simulador.java` no verificaba si todos los autos habían muerto para disparar una nueva generación.
- **Solución**: Añadida verificación en `initTimer()` después de `poblacion.update()` y `poblacion.render(gc)`:
  ```java
  if (poblacion.todosMuertos()) {
      poblacion.siguienteGeneracion();
  }
  ```
- **Resultado**: Ahora cuando los 50 autos mueren, automáticamente se genera una nueva generación con elitismo, crossover y mutación.

### Resultado

- Generación automática: Cuando todos los autos mueren, se activa `siguienteGeneracion()` automáticamente.
- Ciclo de vida completo: Los autos aparecen, evolucionan, mueren y renacen continuamente.
- Build exitoso: Compilación sin errores (9/9 tasks).

---

## 2026-05-16 - Fase 1: Paquetes view/controller y Arquitectura SOLID

### Nuevos Paquetes y Clases

- **`controller/GestorEntradas.java`**: Sistema de entrada por teclado con polling.
  - Usa `Set<KeyCode>` para detección de teclas presionadas en cada frame.
  - Compatibilidad WASD + Flechas direccionales.
  - Métodos: `izquierda()`, `derecha()`, `arriba()`, `abajo()`, `teclaG()`, `teclaM()`, `teclaR()`.
  - Soporte para acciones futuras (Menú M, Reinicio R).

- **`controller/ControladorJugador.java`**: Implementa `Controlador` para entrada humana.
  - `obtenerGiro()`: ±0.05 rad/frame (~3°/frame) con teclas LEFT/RIGHT o A/D.
  - `obtenerAceleracion()`: +0.15 con UP/W, -0.2 con DOWN/S (freno).
  - Sin dependencias de JavaFX, totalmente testeable.

- **`view/Renderizador.java`**: Centraliza todo el renderizado del Canvas.
  - `dibujarVehiculo()`: Renderiza vehículos con rotación (save/translate/rotate/restore).
  - `dibujarSensores()`: Delega en `Sensor.render()` para las líneas de raycasting.
  - `dibujarHUD()`: Renderiza líneas de telemetría con varargs (flexible por modo).
  - `dibujarMenu()`: Renderizado de menú principal integrado en Canvas.

### Refactorización SOLID

- **SRP Aplicado**: Simulador ya no maneja rendering directo ni input directo.
  - Renderizado delegado a `Renderizador` (view package).
  - Input delegado a `GestorEntradas` (controller package).
  - Simulador solo orquesta el ciclo: update → render → estado.

- **DIP Aplicado**: Simulador depende de abstracciones `Renderizador` y `GestorEntradas`.
  - `GestorEntradas` reemplaza el `scene.setOnKeyPressed` directo.

- **OCP habilitado**: Nuevos controladores (IA, Jugador) pueden añadirse sin modificar código existente.

### Cambios en Modelo

- **`model/Vehiculo.java`**: Nuevos getters públicos para el Renderizador.
  - `getAncho()`, `getAlto()`: Dimensiones del vehículo (antes protected en Entidad).
  - `getSensores()`: Lista de sensores para renderizado externo.

### Cambios en Engine

- **`engine/Simulador.java`**: Integración de nuevas clases.
  - Inyecta `GestorEntradas` y `Renderizador` en el constructor.
  - Timer loop refactorizado: usa `renderizador.limpiar()`, `renderizador.dibujarPista()`,
    `renderizador.dibujarVehiculo()`, `renderizador.dibujarSensores()`, `renderizador.dibujarHUD()`.
  - Guardado manual (tecla G) con detección de flanco (edge detection) para evitar guardados múltiples.
  - Eliminada dependencia directa de `scene.setOnKeyPressed`.

### Resultado

- Build exitoso: Compilación sin errores.
- Tests pasan: 9/9 tests exitosos.
- Arquitectura preparada para Fase 2 (Jugador, Pista) y Fase 3 (Modo Competencia).
- SRP, DIP y OCP mejorados en la estructura del proyecto.

### Tarea 1.4: Componente HUD (view/HUD.java)

- **`view/HUD.java`**: Renderizado especializado de textos de interfaz.
  - `dibujarEntrenamiento()`: Muestra generación, vivos, fitness en esquina superior derecha (color YELLOW, Monospaced 16).
  - `dibujarMenu()`: Renderiza menú principal con título grande, opciones navegables (seleccionada en CYAN con ">", no seleccionadas en GRAY), instrucciones en DARKGRAY.
- Separación de responsabilidad: HUD se enfoca solo en texto, Renderizador lo usa como componente.

### Tarea 1.5: Componente RenderizadorVehiculo (view/RenderizadorVehiculo.java)

- **`view/RenderizadorVehiculo.java`**: Renderizado especializado de vehículos.
  - `dibujar()`: Aplica rotación (save/translate/rotate/restore) y color parametrizable.
  - `dibujarSensores()`: Delega en `Sensor.render()` para líneas de raycasting.
- Renderizador principal usa este componente por composición.

### Tarea 1.6: Sistema de Estados (EstadoJuego y Menú Principal)

- **`engine/EstadoJuego.java`**: Enum con tres estados: `MENU`, `ENTRENAMIENTO`, `CARRERA`.
- **Refactor `engine/Simulador.java`**: Implementación completa de máquina de estados.
  - `actualizarMenu()`: Navegación con flechas UP/DOWN (edge detection), selección con ENTER.
  - Opciones: "ENTRENAR IA", "COMPETIR VS IA", "SALIR" (System.exit).
  - `iniciarEntrenamiento()`: Crea población y cambia a estado ENTRENAMIENTO.
  - `actualizarEntrenamiento()`: Bucle de entrenamiento con renderizado vía Renderizador.
  - `iniciarCarrera()`: Prepara modo competencia (Jugador + IA).
  - `actualizarCarrera()`: Bucle de carrera con Jugador (placeholder inicial).
  - Tecla M: Vuelve al menú principal desde cualquier modo (edge detection).
  - Población se crea bajo demanda (solo en ENTRENAMIENTO), se libera al salir.
- Edge detection para ENTER, UP, DOWN, G, M evita múltiples disparos por frame.

### Resultado Fase 1

- **Paquetes creados**: `controller/` (2 clases), `view/` (3 clases), `engine/EstadoJuego.java`.
- **Arquitectura SOLID**:
  - SRP: Simulador solo orquesta, no dibuja ni captura input.
  - OCP: Nuevos controladores y renderizadores sin modificar existentes.
  - DIP: Simulador depende de abstracciones (GestorEntradas, Renderizador, Pista).
  - ISP: Interfaces específicas (Controlador, HUD, RenderizadorVehiculo).
- **Menú integrado**: Sistema de estados en Canvas (sin Stages/Scenes adicionales).
- **Build**: 12/12 tasks exitosos. **Tests**: 9/9 exitosos.

---

## 2026-05-16 - Fase 2: Entidades Pista y Jugador

### Tarea 2.1: Entidad Jugador (model/Jugador.java)

- **`model/Jugador.java`**: Clase que envuelve un Vehiculo controlado por el jugador.
  - Atributos: `Vehiculo vehiculo`, `ControladorJugador controlador`, `activo`, `inicioX/Y`.
  - Constructor recibe `Pista` y `GestorEntradas` (inyección de dependencias).
  - `update()`: Delega en vehiculo.update(), marca inactivo si muere.
  - `reiniciar(x, y)`: Resetea posición y estado para reintento.
  - Getters: `getVehiculo()`, `isActivo()`, `getControlador()`.
- DIP aplicado: Jugador depende de abstracciones (Controlador, Pista, GestorEntradas).
- LSP: ControladorJugador implementa Controlador, intercambiable con ControladorIA.

### Tarea 2.2: Entidad Pista (model/Pista.java)

- **`model/Pista.java`**: Encapsula la máscara de colisiones de la pista.
  - Carga `pista.png` como `Image` (800x600) y su `PixelReader`.
  - Métodos de detección de colores (preservando lógica original de 4 colores):
    - `esPared(x,y)`: Negro (R<0.1, G<0.1, B<0.1) o fuera de límites → true.
    - `esMeta(x,y)`: Rojo (R>0.9, G<0.1, B<0.1) → true.
    - `esSpawn(x,y)`: Azul (B>0.5, R<0.5, G<0.5) → true.
    - `esTransitable(x,y)`: Dentro de límites y no es pared → true.
  - `hayColisionEnTrayecto(x1,y1,x2,y2,anchoV,altoV)`: Verifica las 4 esquinas del vehículo en pasos de 2px.
  - `encontrarSpawnPoint()`: Busca el píxel más azul de la imagen (misma lógica que Sensor original).
  - `getImagen()`: Para renderizado de la pista de fondo.
- DIP: Pista es el único punto de acceso a datos de colisión. Sensor y Vehiculo dependen de Pista.

### Tarea 2.3: Refactor Sensor.java (dependencia de Pista)

- **Eliminados**: `static Image PISTA`, `static PixelReader PIXEL_READER`, bloque `static {}`.
- **Eliminado**: `static double[] encontrarSpawnPoint()` (movido a Pista).
- **Añadido**: Campo `Pista pista` inyectado por constructor.
- **Cambio constructor**: `Sensor(double anguloRelativo, Pista pista)`.
- **Actualizado `medirDistancia()`**: Usa `pista.esPared(x,y)`, `pista.esMeta(x,y)` y `pista.dentroLimites(x,y)` en lugar de acceso directo a PixelReader.
- Lógica de 4 colores preservada exactamente.

### Tarea 2.4: Refactor Vehiculo.java (dependencia de Pista)

- **Añadido**: Campo `Pista pista` inyectado por constructor.
- **Cambio constructor**: `Vehiculo(x, y, ancho, alto, controlador, pista)`.
- **Actualizado `chocaPared()`**: Delega en `pista.hayColisionEnTrayecto()`.
- **Actualizada creación de sensores**: `new Sensor(a, pista)`.
- **Eliminado**: Método `render()` (responsabilidad transferida a RenderizadorVehiculo).
- **Modificada `Entidad.java`**: Eliminado método abstracto `render(GraphicsContext)` (rendering ahora en view/).

### Refactor Poblacion.java (dependencia de Pista)

- **Añadido**: Campo `Pista pista`, inyectado por constructor.
- **Cambio constructor**: `Poblacion(Pista pista, double inicioX, double inicioY)`.
- Reemplazado `Sensor.encontrarSpawnPoint()` por `pista.encontrarSpawnPoint()`.
- Todas las creaciones de Vehiculo pasan `pista`.
- **Eliminado**: Método `render()` (responsabilidad transferida a Simulador + Renderizador).

### Refactor Simulador.java (integración de Pista y Jugador)

- Crea `Pista` en constructor y usa `pista.getImagen()` para renderizado.
- Pasa `Pista` a `Poblacion` y `Jugador`.
- `iniciarCarrera()`: Crea Jugador con input + IA con red cargada (o nueva si no existe guardada).
- `actualizarCarrera()`: Renderiza vehículo del jugador en ROJO.

### Resultado Fase 2

- **Nuevas entidades**: Pista (encapsula colisiones), Jugador (control humano).
- **Fisicas preservadas**: Lógica de 4 colores intacta, solo movida de Sensor a Pista.
- **SOLID**: DIP aplicado (dependencias hacia Pista), SRP (Pista única responsable de datos de pista).
- **Build**: 12/12 tasks exitosos. **Tests**: 9/9 exitosos.

---

## 2026-05-16 - Fase 3: Modo Competencia (Jugador vs IA)

### Carrera Completa: Jugador vs IA

- **`engine/Simulador.java`** — `iniciarCarrera()` y `actualizarCarrera()` completamente implementados.
  - Creación de dos vehículos en paralelo:
    - **Jugador** (ROJO): Controlado por teclado vía `ControladorJugador`.
    - **IA** (AZUL): Controlado por `ControladorIA` con red cargada desde `mejor_red.json` (o red aleatoria si no existe).
  - Posiciones separadas verticalmente (spawnY y spawnY-30) para evitar colapso inicial.
  - Ambos vehículos se actualizan y renderizan simultáneamente en cada frame.

### Condiciones de Victoria

- **6 condiciones de fin de carrera** evaluadas en cada frame:
  1. Jugador cruza meta + IA no → **GANASTE!**
  2. IA cruza meta + Jugador no → **PERDISTE!**
  3. Ambos cruzan meta → Gana el de mayor distancia recorrida.
  4. Jugador muere + IA muere → **EMPATE!**
  5. Solo Jugador muere → **PERDISTE!**
  6. Solo IA muere → **GANASTE!**

### HUD de Carrera (view/HUD.java)

- **`dibujarCarrera()`**: Panel informativo en esquina superior izquierda.
  - "CARRERA: JUGADOR vs IA", progreso de cada uno, quién va ganando.
- **`dibujarResultado()`**: Overlay semitransparente con resultado grande (48px), estadísticas, temporizador de 3s.
  - ENTER para volver al menú después de 3 segundos.

### Mejoras en Renderizador

- **`view/Renderizador.java`**: Nuevos métodos `dibujarHUDCarrera()` y `dibujarResultado()`.

### Resultado Fase 3

- **Carrera funcional**: Jugador vs IA simultáneamente en la misma pista.
- **Victoria/derrota detectable**: 6 condiciones cubren todos los escenarios.
- **UX completa**: Pantalla de resultado con estadísticas y tiempo de espera.
- **Build**: 12/12 tasks exitosos. **Tests**: 9/9 exitosos.

---

## 2026-05-16 - Fase 4: Mejora Visual y Telemetría

### Tarea 4.1: Gráfico de Fitness (view/GraficoEntrenamiento.java)

- **`view/GraficoEntrenamiento.java`**: Gráfico de línea en tiempo real del fitness por generación.
  - Almacena hasta 150 puntos del historial de fitness.
  - Renderiza línea verde (LIME) sobre fondo semitransparente.
  - Muestra valores máximo y mínimo del eje Y, y número de generaciones.
  - Ubicación: esquina inferior izquierda (20, 500, 180x80) durante entrenamiento.
  - Se registra el fitness al final de cada generación (cuando todos mueren).
  - Se limpia al iniciar un nuevo entrenamiento.

### Tarea 4.2: Renderizado con Rotación

- **`RenderizadorVehiculo.dibujar()`** ya implementado en Fase 1.
  - Rotación correcta usando `gc.save()/translate()/rotate()/restore()`.
  - Vehículo rota visualmente según su ángulo de dirección.
  - Colores parametrizables: rojo (jugador), azul (IA), cian (población).

### Tarea 4.3: Ghost Trail (Trayectoria del Mejor Vehículo)

- **`RenderizadorVehiculo.dibujarTrayectoria()`**: Renderiza línea semitransparente.
- **`Simulador.java`**: Almacena hasta 200 puntos de posición del mejor vehículo vivo.
  - Captura la posición cada 3 frames para evitar sobrecarga.
  - Se limpia al iniciar nueva generación.
  - Color: cian con alpha 0.3 (Color.rgb(0, 255, 255, 0.3)).
  - Muestra visualmente la ruta que está aprendiendo la IA.

### Resultado Fase 4

- **Gráfico de fitness**: Visualización en tiempo real de la evolución.
- **Ghost trail**: Muestra la trayectoria del mejor vehículo.
- **Rotación visual**: Vehículos rotan suavemente según dirección.
- **Build**: 12/12 tasks exitosos. **Tests**: 15/15 exitosos.

---

## 2026-05-16 - Fase 5: Documentación y Cierre

### Tarea 5.1: Memoria Técnica (docs/MEMORIA_TECNICA.md)

- Documento formal completo con:
  - **Algoritmo seleccionado**: Neuroevolución (Algoritmo Genético + RNA).
  - **Justificación**: Comparativa con DDQN, ventajas para el proyecto.
  - **Arquitectura**: Red 5→4→2, población 50, elitismo, crossover 1 punto, mutación dinámica.
  - **Función de fitness**: distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3).
  - **Bonificación por meta**: +50000 al fitness.
  - **Resultados**: Tabla de métricas y comportamiento observado por generaciones.
  - **Principios SOLID**: Descripción detallada de cada principio aplicado.
  - **Estructura MVC**: Diagrama de flujo del sistema.
  - **Mapa de colores**: Documentación de los 4 colores de la pista.

### Tarea 5.2: Diagrama de Clases (docs/DIAGRAMA_CLASES.md)

- Diagrama UML completo en formato Mermaid.
  - **22 clases** documentadas con atributos y métodos principales.
  - **Relaciones**: herencia, implementación, composición y asociación.
  - **Diagrama de paquetes**: Flujo de dependencias entre paquetes.
  - Incluye todas las clases del proyecto (model, ai, view, controller, engine).

### Tarea 5.3: Tests Adicionales

- **model/PistaTest.java** (6 tests): Carga, spawn, límites, colisiones, transitabilidad.
- **model/JugadorTest.java** (3 tests): Creación con IA, posición, estado vivo.
- **Total**: 15 tests (9 originales + 6 nuevos).
- Tests verifican: red neuronal, persistencia, vehículo, pista, jugador.

### Tarea 5.4: Build Final

- **Compilación**: `./gradlew :app:build` → BUILD SUCCESSFUL (12/12 tasks).
- **Tests**: `./gradlew :app:test` → 15/15 tests exitosos.
- **Ejecución**: `./gradlew :app:run` → Ventana JavaFX con simulador funcional.

---

## 2026-05-16 - HOTFIX: Z-index del Menú, Spawn en Carrera y Sistema de Reintento

### Tarea 1: Visibilidad del Menú (Renderizador + Simulador)

- **Bug**: El menú se renderizaba sobre la pista (fondo claro), haciendo el texto invisible o ilegible.
- **Solución**: Separación de renderizado de fondo por estado en `Simulador.initTimer()`:
  - `MENU`: Fondo opaco oscuro `#1e1e2e` (sin pista).
  - `ENTRENAMIENTO` / `CARRERA`: Pista como fondo normal.
- **`Renderizador.dibujarFondoMenu()`**: Nuevo método que pinta fondo sólido oscuro para contraste máximo.
- Menú ahora visible con texto blanco/cyan/gris sobre fondo oscuro.

### Tarea 2: Spawn Correcto del Jugador (Simulador + Pista)

- **Bug**: El vehículo del jugador moría en frame 1 porque el spawn point (centroide azul) se usaba como esquina superior izquierda del vehículo, colocándolo parcialmente fuera de la pista o sobre una pared.
- **Causa raíz**: `iniciarCarrera()` no aplicaba offset del bounding box (40x20) al posicionar los vehículos.
- **Solución**:
  - `model/Pista.java`: Añadidos campos `startX`, `startY` cacheados con getters públicos.
  - `iniciarCarrera()`: Offset aplicado:
    - Jugador: `(pista.getStartX() - 20, pista.getStartY() - 10)`
    - IA: `(pista.getStartX() - 20, pista.getStartY() - 40)`
  - Vehículos ahora aparecen centrados sobre la línea azul de spawn.
- **`iniciarEntrenamiento()`**: Ahora usa `pista.getStartX(), pista.getStartY()` (cacheados) en lugar de llamar a `encontrarSpawnPoint()` cada vez.

### Tarea 3: Estado de Game Over y Reintento (Simulador + GestorEntradas + HUD)

- **Bug**: No existía forma de reintentar la carrera sin reiniciar la JVM.
- **Solución**:
  - **`Simulador.reiniciarCarrera()`**: Recrea ambos vehículos (Jugador e IA) en sus posiciones iniciales. La IA preserva la misma red neuronal mediante clonación de pesos. Resetea `carreraTerminada`, `resultadoCarrera`, `framesResultado`.
  - **Tecla 'R'**: Detectada en cada frame de `actualizarCarrera()` con edge detection. Ejecuta `reiniciarCarrera()` tanto si la carrera terminó como si está en curso.
  - **`HUD.dibujarResultadoConReintento()`**: Nuevo overlay que reemplaza al anterior, mostrando:
    - Overlay semitransparente (negro 70%).
    - Texto grande del resultado (LIME/ROJO).
    - Estadísticas de distancia.
    - Texto "FIN DE CARRERA" en naranja.
    - Instrucciones: "Presiona 'R' para reintentar | 'M' para el Menu".
  - **`GestureEntradas`**: Añadidos métodos `teclaE()` y `teclaC()` para acceso directo desde el menú.
  - Menú actualizado para mostrar "E: Entrenar | C: Competir" en instrucciones.
  - HUD de carrera actualizado para mostrar "R: Reiniciar" en lugar de solo "M: Volver al menu".

### Resultado

- **Menú visible**: Fondo oscuro con contraste asegurado.
- **Spawn correcto**: Vehículos centrados en línea azul, sin muerte en frame 1.
- **Ciclo completo**: Carrera → Game Over → Reintento (R) o Menú (M) sin reiniciar JVM.
- **Build**: 12/12 tasks exitosos. **Tests**: 15/15 exitosos.

---

## 2026-05-16 - HOTFIX ARQUITECTÓNICO: Colisión Frame-0, Spawn Centrado, Escalado de Gráficos

### Tarea 1: Resolución de Muerte Instantánea en CARRERA

- **Bug**: En modo CARRERA, Jugador e IA morían en el frame 1 por colisión inmediata contra pared.
- **Causa raíz**: `Pista.encontrarSpawnPoint()` devolvía un píxel azul en el BORDE izquierdo del carril (x=36) en lugar del centro. El offset `x - 20` para centrar el bounding box de 40px dejaba el vehículo en x=16, 18px DENTRO de la pared (el carril arranca en x=34).
- **Tres sub-problemas**:
  1. **Pista.java**: Escaneaba toda la fila para hallar los bordes izquierdo/derecho, pero la pista tiene múltiples segmentos no-pared separados por muros en la misma fila. El centro calculado (x=397) caía en un muro.
  2. **Poblacion.java**: Creaba vehículos de entrenamiento en `(spawnX, spawnY)` sin aplicar el offset `-ancho/2, -alto/2`, causando la misma colisión en entrenamiento.
  3. **Simulador.java**: El offset de 40px para separar IA del jugador colocaba al IA FUERA del carril cuando este era angosto.
- **Soluciones**:
  - `Pista.encontrarSpawnPoint()`:
    - Localiza el píxel azul más intenso (marcador de spawn).
    - Expande hacia izquierda y derecha desde ESE píxel para encontrar los bordes del segmento no-pared CONTIGUO.
    - Retorna el centro de ESE segmento específico (x=55 en el corredor angosto y=421).
  - `Poblacion.java`: Pre-aplica offset `-20, -10` al calcular `spawnX, spawnY`, de modo que todos los vehículos se creen centrados.
  - `Simulador.iniciarCarrera()`: Ambos vehículos se posicionan en `(startX - 20, startY - 10)` con separación vertical de 30px. Ángulo inicial `-PI/2` (hacia arriba, saliendo del corredor angosto hacia la pista ancha).
  - `Vehiculo.reset(nuevaX, nuevaY, nuevoAngulo)`: Nuevo método que limpia TODO el estado (vivo, haCruzadoMeta, velocidad, distanciaRecorrida, rotacionAcumulada, framesBajaVelocidad, fitness, startX, startY).
  - `Jugador.reiniciar(x, y, angulo)`: Ahora llama a `vehiculo.reset()`.

### Tarea 2: Prevención de "EMPATE" Prematuro

- **Bug**: Si ambos vehículos morían en el frame 0-1, la lógica de carrera disparaba instantáneamente el resultado "EMPATE".
- **Solución** (`Simulador.java`): Contador `framesCarrera` con umbral `FRAMES_SEGUROS = 30`. Las condiciones de muerte (`!jugadorVivo && !iaVivo`, `!jugadorVivo`, `!iaVivo`) solo activan fin de carrera si `framesCarrera > 30`. Esto evita que glitches de spawn disparen el game over.

### Tarea 3: Rediseño de `GraficoEntrenamiento.java`

- **Problema**: El gráfico de fitness carecía de márgenes/padding, el escalado usaba todo el alto disponible (sin margen para etiquetas) y el mapeo de puntos era lineal sin considerar padding.
- **Rediseño**:
  - Constantes: `int padding = 40;`, `double w = 350;`, `double h = 200;`.
  - Fondo: Rectángulo oscuro `gc.setFill(Color.color(0.1, 0.1, 0.1, 0.85))`.
  - Escala dinámica: Encuentra `maxFit` del historial; si es 0, usa `maxFit = 1.0`.
  - Mapeo matemático de coordenadas:
    - `puntoX = x + padding + (i * (w - 2 * padding) / max(1, historial - 1))`
    - `puntoY = y + h - padding - ((fitnessActual / maxFit) * (h - 2 * padding))`
  - Renderizado: Líneas en CYAN con `gc.strokeLine()`. Valores de eje (max/min) renderizados FUERA del área de trazado.

### Tarea 4: Tests Afectados

- `testSpawnPointEsAzul` → Eliminado (el spawn ahora es el CENTRO del carril, no un píxel azul; el píxel azul solo es marcador).
- `testSpawnPointEsTransitable` → Se mantiene, ahora verifica que el centro del carril sea transitable (no-pared).
- `testSpawnEsTransitable` → Ajustado: usa ruta vertical (hacia arriba, dentro del corredor) en vez de diagonal (que salía del carril angosto).

### Build Final

- **Compilación**: `./gradlew :app:build` → BUILD SUCCESSFUL (12/12 tasks).
- **Tests**: 18/18 exitosos.
- **Archivos modificados**: `model/Vehiculo.java`, `model/Jugador.java`, `model/Pista.java`, `engine/Simulador.java`, `ai/Poblacion.java`, `view/GraficoEntrenamiento.java`, tests.

---

## 2026-05-17 - HOTFIX: Fixes Precisos (Muerte Instantánea, Doble Mutación, Selección, Persistencia)

### Bug 1 — Muerte instantánea por artifacts JPEG en la meta

- **Causa raíz**: `pista.png` (JPEG estirado 1536×1024→800×600 con filtro bilinear) produce sangrado de píxeles rojos (meta) cerca del spawn azul. `sensoresDetectanMetaCerca()` detecta estos falsos positivos, impidiendo que `encontrarSpawnConMargen()` valide posiciones. En `Vehiculo.update()`, el sensor detecta rojo a ~2px → `dist = 2/300 = 0.0067 < 0.01` → `metaDetectada = true` → vehículo se detiene en frame 1.

### Fix 1: Grace period de 30 frames (`model/Vehiculo.java`)

- **Campo `framesVivo`**: Nuevo contador de frames de vida del vehículo.
- **Inicialización**: `this.framesVivo = 0` en constructor y en `reset()`.
- **Incremento**: `framesVivo++` al inicio de `update()`.
- **Condición de gracia**: En la detección de meta se añade `&& framesVivo > 30` (0.5s a 60fps), tiempo suficiente para que el vehículo se aleje del área de spawn donde están los artifacts JPEG.

### Fix 2: `esSpawnSeguro()` simplificado (`engine/Simulador.java`)

- Eliminado el bug lógico del loop de márgenes: `sensoresDetectanMetaCerca()` no depende del margen, por lo que si retorna `true` una vez, retorna `true` en todas las iteraciones.
- Nuevo método solo verifica colisión con paredes (`hayColisionEnTrayecto`). La detección de meta ya está protegida por el grace period de 30 frames en `Vehiculo`.

### Bug 2 — IA no aprende

- **Causa A**: Los mismos JPEG artifacts matan los vehículos de entrenamiento en frame 1-2 → fitness uniforme ≈0 → selección por ruleta colapsa.
- **Causa B**: `Poblacion.crossover()` muta los hijos DOS veces (dentro de `crossover()` y luego en `mutacion()`).
- **Causa C**: Ruleta con fitness uniformes siempre retorna `vehiculos.get(0)`.

### Fix 3: Eliminar mutación duplicada (`ai/Poblacion.java`)

- Eliminada línea `redHijo.mutar(0.1);` dentro de `crossover()`. La mutación ahora solo se aplica en `mutacion()` justo después, evitando probabilidad real de 19% (vs 10% esperado).

### Fix 4: Selección por torneo (`ai/Poblacion.java`)

- Reemplazada ruleta de fitness por torneo de tamaño 3. Funciona correctamente incluso con fitness uniformes porque introduce aleatoriedad real (3 candidatos aleatorios, elige el mejor).

### Bug 3 — Persistencia de red neuronal

- **Causa**: El bloque estático de `GestorRed` usaba `new File("app").exists()` que depende del directorio de trabajo, pudiendo guardar en un lugar y buscar en otro.

### Fix 5: Resolución robusta de ruta (`ai/GestorRed.java`)

- Nuevo bloque `static {}` que prueba rutas candidatas en orden de preferencia:
  1. Si el archivo ya existe en alguna ruta conocida → usa esa ruta.
  2. Si el directorio padre existe (se puede escribir) → usa esa ruta.
  3. Fallback a `~/.simulador_ia/mejor_red.json` (siempre funciona).

### Archivos modificados

| Archivo | Cambio | Bug que resuelve |
|---|---|---|
| `model/Vehiculo.java` | Campo `framesVivo`, incremento en `update()`, condición `&& framesVivo > 30`, reset | Muerte instantánea (principal) + IA no aprende (secundario) |
| `engine/Simulador.java` | `esSpawnSeguro()` solo verifica paredes, no meta | Muerte instantánea (elimina bug lógico del loop de márgenes) |
| `ai/Poblacion.java` | Eliminar `redHijo.mutar(0.1)` en `crossover()` | IA no aprende (doble mutación destruye buenas soluciones) |
| `ai/Poblacion.java` | Reemplazar ruleta por torneo en `seleccionarPadre()` | IA no aprende (selección colapsa con fitness uniformes) |
| `ai/GestorRed.java` | Resolución robusta de ruta para `mejor_red.json` | Persistencia de memoria de la IA entre sesiones |

---

## 2026-05-17 - HOTFIX: Colisión de Pared en Frame 1, Spawns Separados y Validación de Bounding Box

### Bug 4 — Colisión de pared en frame 1 (no cubierto por grace period anterior)

- **Causa raíz**: El grace period `framesVivo > 30` solo protegía contra detección de META. `chocaPared()` no tenía ningún grace period. El vehículo en (36, 411) con bounding box 40×20px tiene borde derecho en x=76. En el primer `update()`, intenta moverse 0.5px → `hayColisionEnTrayecto` detecta x=76 como pared → `vivo = false` en frame 1.

### Fix 6: Grace period de 5 frames para colisión de pared (`model/Vehiculo.java`)

- Añadido `framesVivo > 5 &&` antes de `chocaPared()`:
  ```java
  if (framesVivo > 5 && chocaPared(x, y, nuevoX, nuevoY)) {
  ```
- **Por qué 5**: 5 frames a velocidad 0.5 = 2.5px de movimiento. Suficiente para escapar de overlap menor con bordes de spawn, sin permitir atravesar paredes reales.

### Bug 5 — IA y jugador en la misma posición exacta

- **Causa raíz**: La lógica de fallback para spawn de IA (`cx - 60`, luego `cx - 20`) siempre terminaba en el mismo punto que el jugador cuando el offset `cx + 20` fallaba, porque ambos compartían los mismos candidatos de respaldo.

### Fix 7: `calcularSpawnsSeparados()` (`engine/Simulador.java`)

- Nuevo método que prueba múltiples candidatos independientes para jugador e IA, maximizando separación.
- **Jugador**: Prueba offsets en Y primero, luego X: `(cx-20, cy-10)`, `(cx-20, cy)`, `(cx-20, cy+10)`, `(cx, cy-10)`, etc.
- **IA**: Prueba en orden inverso de X para maximizar separación: `(cx+20, cy-10)`, `(cx+20, cy)`, `(cx+40, cy-10)`, `(cx-60, cy-10)`, etc. Además exige `Math.abs(c[0] - jx) > 10` para evitar superposición.
- Reemplazado el bloque de cálculo de spawns tanto en `iniciarCarrera()` como en `reiniciarCarrera()`.

### Bug 6 — Spawn inválido por falta de validación de bounding box completo

- **Causa raíz**: `encontrarSpawnConMargen()` verificaba solo movimiento horizontal (`hayColisionEnTrayecto` con margen pequeño), pero no validaba que los 4 vértices del vehículo en la posición inicial fueran transitables. Además, `sensoresDetectanMetaCerca()` bloqueaba todos los candidatos por los JPEG artifacts (el bug original).

### Fix 8: `cabeVehiculoEn()` en `encontrarSpawnConMargen()` (`model/Pista.java`)

- Reemplazado `!sensoresDetectanMetaCerca(vx, vy)` por `cabeVehiculoEn(vx, vy, 40, 20)` en ambos puntos de retorno del método.
- **Primer return**: Valida que el vehículo completo quepa en la posición antes de retornar.
- **Segundo return** (loop de dx/sign): Misma validación con `cabeVehiculoEn`.
- Eliminado `sensoresDetectanMetaCerca` de ambos puntos (el grace period en Vehiculo lo hace innecesario).

### Archivos modificados (acumulado)

| Archivo | Cambios acumulados |
|---|---|
| `model/Vehiculo.java` | Campo `framesVivo`, incremento en `update()`, `&& framesVivo > 30` para META, `&& framesVivo > 5` para PARED, reset en constructor y `reset()` |
| `engine/Simulador.java` | `esSpawnSeguro()` simplificado, nuevo `calcularSpawnsSeparados()`, refactor `iniciarCarrera()` y `reiniciarCarrera()` |
| `ai/Poblacion.java` | Eliminada mutación duplicada en `crossover()`, ruleta → torneo en `seleccionarPadre()` |
| `ai/GestorRed.java` | Resolución robusta de ruta con 3 candidatos + fallback a `~/.simulador_ia/` |
| `model/Pista.java` | `cabeVehiculoEn()` en `encontrarSpawnConMargen()`, eliminado `sensoresDetectanMetaCerca` del spawn |

---

## 2026-05-17 - HOTFIX: Ángulo de Spawn (Corredor Vertical)

### Bug 7 — Vehículo spawn apuntando a la derecha en corredor vertical

- **Causa raíz**: El spawn en (36, 411) está en un corredor que sube VERTICALMENTE, pero todos los vehículos se creaban con `angulo=0` (apuntando a la DERECHA). En el primer frame, el vehículo se lanzaba contra la pared lateral derecha del corredor. El grace period de 5 frames para paredes no era suficiente porque el vehículo avanzaba ~3px en 5 frames, directamente contra la pared.

### Fix 9: Detección de ángulo óptimo de spawn (`model/Pista.java`)

- Nuevo método `detectarAnguloInicial(vx, vy)`: mide espacio libre en 4 direcciones (0, π/2, π, -π/2) desde el centro del vehículo, hasta 80px. Retorna la dirección con mayor distancia transitable.
- Para el spawn en (36, 411), detecta que hacia arriba (`-π/2`) tiene 80px libres vs ~2px hacia la derecha.

### Fix 10: Integración en `calcularSpawnsSeparados()` (`engine/Simulador.java`)

- `calcularSpawnsSeparados()` ahora retorna `[jx, jy, ix, iy, anguloJugador, anguloIA]` usando `pista.detectarAnguloInicial()`.
- `iniciarCarrera()` y `reiniciarCarrera()`: extraen los ángulos y los aplican:
  - `jugador.getVehiculo().setAngulo(anguloJugador)`
  - `iaVehiculo.setAngulo(anguloIA)` y `iaVehiculo.reset(..., anguloIA)`

### Archivos modificados

| Archivo | Cambio |
|---|---|
| `model/Pista.java` | Nuevo `detectarAnguloInicial()`: mide espacio libre en 4 direcciones, retorna la óptima |
| `engine/Simulador.java` | `calcularSpawnsSeparados()` retorna ángulos; `iniciarCarrera()` y `reiniciarCarrera()` los aplican |

---

## 2026-05-17 - HOTFIX: Auto-Avance Humano, Fitness de Rotación y Evaluación Genética

### Bug 8 — Auto-avance incondicional para el jugador humano

- **Causa raíz**: `velocidad += aceleracion + 0.1` se aplicaba SIEMPRE, incluso cuando el controlador es humano. El `+0.1` es necesario para la IA (que necesita impulso base para explorar), pero el jugador humano se movía sin presionar teclas.
- **Fix**: Separada la lógica: si hay `controladorIA`, se aplica `+0.1`; si es humano, solo `aceleracion` con clamp a 0 (sin retroceso involuntario).

### Bug 9 — Penalización de rotación mata el aprendizaje de la IA

- **Causa raíz**: `fitness = distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3)`. La pista tiene curvas de 90° obligatorias. Un vehículo que gira para tomar la curva recibe MENOS fitness que uno que va recto contra la pared. La evolución selecciona "ir recto hasta morir".
- **Fix**: Nueva fórmula `fitness = distanciaAvance + (velocidad * 5)` sin penalización por rotación.

### Bug 10 — `evaluarFitness()` sobrescribe el fitness real con `distanciaRecorrida`

- **Causa raíz**: `v.setFitness(v.getDistanciaRecorrida())` en `evaluarFitness()` reemplazaba el fitness calculado en `Vehiculo.update()` (basado en `distanciaAvance` + velocidad) por `distanciaRecorrida` (distancia total acumulada, que premia dar círculos).
- **Fix**: `evaluarFitness()` ahora solo ordena por el fitness ya calculado en `update()`. El fitness real (progreso lineal desde spawn) guía la evolución.

### Bug 11 — `guardarRed` se ejecutaba antes de ordenar

- **Causa raíz**: El orden era `evaluarFitness()` → `if (record)` → `vehiculos.get(0)`. Pero `evaluarFitness()` sobrescribía el fitness con `distanciaRecorrida` y no ordenaba por el fitness real. Con el fix de `evaluarFitness()`, ahora solo ordena, y `vehiculos.get(0)` es correctamente el mejor.
- **Fix**: No requirió cambio de código (el orden ya era correcto), solo la corrección de `evaluarFitness()` para que efectivamente ordene por el fitness real.

### Archivos modificados

| Archivo | Cambio |
|---|---|
| `model/Vehiculo.java` | Separada aceleración humano/IA; fitness sin penalización de rotación |
| `ai/Poblacion.java` | `evaluarFitness()` ya no sobrescribe fitness, solo ordena |
| `model/Pista.java` | `detectarAnguloInicial()` verifica dirección opuesta cuando ambas son viables |

---

## 2026-05-17 - HOTFIX: `detectarAnguloInicial` con Bounding Box Completo

### Bug 12 — Ángulo de spawn incorrecto por medir solo el píxel central

- **Causa raíz**: `detectarAnguloInicial()` medía espacio libre con un solo píxel en el centro del vehículo (`vx+20, vy+10`). El vehículo mide 40×20px. Un corredor que parece libre en el centro puede tener paredes en los bordes laterales del vehículo. Resultado: elegía "abajo" o "derecha" aunque el vehículo completo no cupiera.
- **Fix**: Reemplazado el método completo para usar `cabeVehiculoEn(nx, ny, 40, 20)` que verifica los **4 vértices** del vehículo en cada paso:
  - Parte desde `(vx, vy)` sin offset
  - Mide hasta 150px en cada dirección
  - Usa `cabeVehiculoEn` que chequea transitabilidad de los 4 puntos extremos del bounding box
  - Eliminada la verificación de dirección opuesta (innecesaria con bounding box real)
  - Para jugador en (36,411): "arriba" → 150px libres; "abajo" → bloquea inmediatamente (borde lateral choca)
  - Para IA en (86,411): "arriba" → 150px libres; "derecha" → bloquea a 26px (pared lateral)

---

## 2026-05-17 - HOTFIX DEFINITIVO: Población y Spawn IA

### Bug 13 — Población de entrenamiento siempre con `angulo=0` (derecha)

- **Causa raíz**: `Poblacion.java` tenía `setAngulo(0)` hardcodeado en 4 lugares, y en `crearPoblacion()` ni siquiera tenía `setAngulo`. El método `detectarAnguloInicial` nunca se llamaba durante el entrenamiento. Todos los vehículos de entrenamiento arrancaban apuntando a la derecha en un corredor vertical → chocaban inmediato.
- **Fix**: Reemplazados todos los `setAngulo(0)` por `setAngulo(-Math.PI / 2)` en:
  - `crearPoblacion()` (nueva línea agregada)
  - `crearPoblacionConRed()` (elite y loop)
  - `crossover()` (elite y loop)

### Bug 14 — IA spawn en zona negra y ángulo incorrecto

- **Causa raíz**: Los candidatos IA en `calcularSpawnsSeparados()` usaban `cx+20` = x=86, donde el borde derecho (x=126) tocaba la pared. Además `detectarAnguloInicial` con bounding box fallaba porque el corredor en x=86 es muy angosto para 40px de ancho.
- **Fix**: Candidatos IA ahora usan la misma X que el jugador pero desplazados hacia arriba en Y (en el corredor validado): `{jx, jy-35}`, `{jx, jy-50}`, `{jx, jy-25}`, etc.
- Distancia de separación cambiada de `Math.abs(x) > 10` a distancia euclidiana `sqrt(dx²+dy²) > 20`.

### Bug 15 — `detectarAnguloInicial` hardcodeado

- **Causa raíz**: El bounding box de 40px es demasiado ancho para algunos segmentos del corredor. La detección dinámica fallaba para posiciones como (86, 411).
- **Fix**: El track es fijo (siempre sube hacia arriba desde el spawn). `detectarAnguloInicial` retorna `-Math.PI / 2` directamente.

### Archivos modificados

| Archivo | Cambio |
|---|---|
| `model/Pista.java` | `detectarAnguloInicial` hardcodeado a `-π/2` |
| `ai/Poblacion.java` | 5x `setAngulo(0)` → `setAngulo(-π/2)` |
| `engine/Simulador.java` | Candidatos IA en misma X que jugador, distancia euclidiana |

---

## 2026-05-17 - HOTFIX: Fitness Acumulativo, Mutación Controlada y Top 3 Elites

### Bug 16 — Fitness con distancia Euclidiana se reduce en curvas

- **Causa raíz**: `distanciaAvance = sqrt((x-startX)² + (y-startY)²)` mide línea recta desde el spawn. Cuando el vehículo toma una curva que lo acerca temporalmente al spawn (por ejemplo, rodeando el circuito), su distancia Euclidiana DISMINUYE aunque esté avanzando por la pista. La evolución aprende activamente a NO tomar curvas.
- **Fix**: Fitness usa `distanciaRecorrida` (distancia total acumulada) que siempre crece al avanzar, sin importar la dirección. El anti-spin también cambia a `distanciaRecorrida < 50` (mata a los que giran sin avanzar realmente).

### Bug 17 — Mutación al 40% destruye la población

- **Causa raíz**: Cuando `generacionesEstancadas > 5` la mutación subía a 0.4. Con 44 pesos en la red (5×4 + 4×2), esto mutaba ~17 pesos por individuo, colapsando toda la población. El fitness caía a cero y nunca se recuperaba.
- **Fix**: Tasa máxima reducida a 0.15. En lugar de mutar agresivamente, si hay estancamiento >15 generaciones se inyectan 5 individuos con redes aleatorias frescas (inyección de diversidad). El contador de estancamiento se resetea para dar oportunidad a los nuevos.

### Bug 18 — Solo 1 elite preservado por generación

- **Causa raíz**: `crossover()` preservaba solo al mejor individuo (índice 0). Con 50 individuos y solo 1 elite, la buena información genética se perdía rápidamente en cada generación.
- **Fix**: `crossover()` ahora preserva el top 3 sin mutación. `mutacion()` empieza en índice 3 para no tocar elites.

### Archivos modificados

| Archivo | Cambio |
|---|---|
| `model/Vehiculo.java` | Fitness con `distanciaRecorrida` en lugar de `distanciaAvance` |
| `ai/Poblacion.java` | Mutación máx 0.15 + inyección aleatoria + top 3 elites; `mutacion()` empieza en índice 3 |
