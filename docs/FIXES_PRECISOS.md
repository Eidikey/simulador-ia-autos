# FIXES PRECISOS — Simulador de Autos Autónomos

## Diagnóstico Final (tras leer el código real)

### Bug 1 — Muerte instantánea: causa raíz confirmada

**Flujo exacto del problema:**

1. `Pista.encontrarSpawnConMargen()` llama `sensoresDetectanMetaCerca(vx, vy)` para validar cada posición candidata.
2. El archivo `pista.png` es JPEG estirado de 1536×1024 a 800×600 con filtro bilinear. Los píxeles rojos (meta) que están cerca del marcador azul (spawn) se "sangran" en píxeles adyacentes. Resultado: hay píxeles con R>0.9, G<0.1, B<0.1 a 2–4px del spawn.
3. `sensoresDetectanMetaCerca` dispara `true` para TODAS las posiciones candidatas (dy en {0,±5,±10,±15,±20}).
4. **Ningún margen pasa la validación** → se usa el fallback sin validación.
5. `esSpawnSeguro()` en `Simulador` también llama `sensoresDetectanMetaCerca` → siempre falla → el jugador usa `cx-20` sin garantías.
6. En el primer frame de `update()`, el sensor del vehículo detecta un píxel rojo a 2px → `dist = 2/300 = 0.0067 < 0.01` → `metaDetectada = true` → `haCruzadoMeta = true`, `velocidad = 0`, `return` inmediato.
7. El vehículo no puede moverse. Con `haCruzadoMeta=true`, la condición `if (!jugadorVivo)` no aplica, pero dependiendo del estado de la IA, puede resultar en `PERDISTE` inmediato.

**Bug secundario en `esSpawnSeguro` (Simulador.java líneas 238-246):**
El loop de márgenes es inútil porque `sensoresDetectanMetaCerca` no depende del margen. Si retorna `true` una vez, retorna `true` en todas las iteraciones del loop, haciendo que `esSpawnSeguro` siempre devuelva `false`.

### Bug 2 — IA no aprende: causas confirmadas

**Causa A**: Las mismas JPEG artifacts hacen que los vehículos del entrenamiento también mueran en el frame 1–2 (misma detección falsa de meta / colisión de pared). Todos tienen `distanciaRecorrida ≈ 0.5`. La selección por ruleta con fitness uniforme es aleatoria pura → cero presión evolutiva.

**Causa B** (`Poblacion.java`): Los hijos en `crossover()` se mutan DOS veces:
- Primera mutación: `redHijo.mutar(0.1)` dentro de `crossover()` (línea ~130)
- Segunda mutación: `mutacion(nuevaGeneracion, ...)` llamada inmediatamente después

Probabilidad real de mutar cada peso: `1 - (1-0.1)² ≈ 19%` (debería ser 10%). En modo estancado: `1 - (1-0.1)*(1-0.4) ≈ 46%` (debería ser 40%). Esto destruye soluciones buenas demasiado rápido.

**Causa C** (`Poblacion.java`): `seleccionarPadre()` usa ruleta de fitness. Cuando todos los fitness son ≈0, `totalFitness ≈ 0` → `r ≈ 0` → el loop siempre retorna `vehiculos.get(0)` en la primera iteración. Todos los hijos son clones del mismo padre (el primero en la lista, que es arbitrario). Sin diversidad genética.

---

## CAMBIOS EXACTOS

---

### ARCHIVO 1: `Vehiculo.java`

**Cambio 1.1 — Agregar campo `framesVivo`**

Ubicación: inmediatamente después de `private boolean haCruzadoMeta;`

```java
// ANTES (el campo existente):
private boolean haCruzadoMeta;

// DESPUÉS (agregar la línea siguiente):
private boolean haCruzadoMeta;
private int framesVivo;
```

---

**Cambio 1.2 — Inicializar `framesVivo` en el constructor**

Ubicación: en el constructor, inmediatamente después de `this.haCruzadoMeta = false;`

```java
// ANTES:
this.haCruzadoMeta = false;

// DESPUÉS:
this.haCruzadoMeta = false;
this.framesVivo = 0;
```

---

**Cambio 1.3 — Incrementar `framesVivo` al inicio de `update()` y agregar condición de gracia**

Ubicación: método `update()`, justo después de `if (!vivo) return;`

```java
// ANTES (primeras líneas de update()):
@Override
public void update() {
    if (!vivo) return;

    double[] inputs = new double[sensores.size()];
    boolean metaDetectada = false;

    for (int i = 0; i < sensores.size(); i++) {
        double dist = sensores.get(i).medirDistancia(x, y, angulo);
        inputs[i] = dist;
        if (sensores.get(i).isMetaDetectada() && dist < 3.0 / 300.0) {
            metaDetectada = true;
        }
    }

// DESPUÉS:
@Override
public void update() {
    if (!vivo) return;

    framesVivo++;  // ← LÍNEA NUEVA

    double[] inputs = new double[sensores.size()];
    boolean metaDetectada = false;

    for (int i = 0; i < sensores.size(); i++) {
        double dist = sensores.get(i).medirDistancia(x, y, angulo);
        inputs[i] = dist;
        if (sensores.get(i).isMetaDetectada() && dist < 3.0 / 300.0 && framesVivo > 30) {  // ← && framesVivo > 30
            metaDetectada = true;
        }
    }
```

> **Por qué 30 frames:** a 60fps son 0.5 segundos. Suficiente para alejarse del área de spawn donde están los artifacts JPEG. El jugador puede controlar el vehículo normalmente durante este período.

---

**Cambio 1.4 — Resetear `framesVivo` en `reset()`**

Ubicación: método `reset()`, al final del bloque de inicializaciones.

```java
// ANTES (últimas líneas de reset()):
    this.fitness = 0;
    this.vivo = true;
    this.haCruzadoMeta = false;
    this.startX = nuevaX;
    this.startY = nuevaY;
}

// DESPUÉS:
    this.fitness = 0;
    this.vivo = true;
    this.haCruzadoMeta = false;
    this.startX = nuevaX;
    this.startY = nuevaY;
    this.framesVivo = 0;  // ← LÍNEA NUEVA
}
```

---

### ARCHIVO 2: `Simulador.java`

**Cambio 2.1 — Reescribir `esSpawnSeguro()` completamente**

El método actual tiene un bug lógico: `sensoresDetectanMetaCerca` no depende del `margen`, por lo que el loop de márgenes es inútil. Si `sensoresDetectanMetaCerca` retorna `true`, el método siempre retorna `false` sin importar qué margen se pruebe. Además, con el grace period ya en `Vehiculo`, el chequeo de meta en spawn es redundante. Solo necesitamos verificar que no haya pared.

```java
// ANTES (líneas 238-246):
private boolean esSpawnSeguro(double x, double y) {
    double[] margenes = {10.0, 5.0, 3.0, 1.0, 0.6};
    for (double margen : margenes) {
        if (!pista.hayColisionEnTrayecto(x, y, x + margen, y, 40, 20) &&
            !pista.sensoresDetectanMetaCerca(x, y)) {
            return true;
        }
    }
    return false;
}

// DESPUÉS:
private boolean esSpawnSeguro(double x, double y) {
    // Solo verificar colisión con paredes. La detección de meta
    // tiene grace period de 30 frames en Vehiculo, así que no
    // hay riesgo de falso positivo por artifacts JPEG en spawn.
    return !pista.hayColisionEnTrayecto(x, y, x + 1.0, y, 40, 20);
}
```

---

### ARCHIVO 3: `Poblacion.java`

**Cambio 3.1 — Eliminar la mutación duplicada dentro de `crossover()`**

Ubicación: método `crossover()`, en el bloque donde se crea cada hijo (el bucle `for (int i = 1; i < tamanoPoblacion; i++)`).

```java
// ANTES (el bloque de creación del hijo dentro del for):
      RedNeuronal redHijo = new RedNeuronal(5, 4, 2);
      redHijo.setPesosDesdeArray(hijo);
      redHijo.mutar(0.1);          // ← ELIMINAR ESTA LÍNEA
      ControladorIA ci = new ControladorIA(redHijo);
      Vehiculo nv = new Vehiculo(spawnX, spawnY, 40, 20, ci, pista);
      nv.setAngulo(0);
      nv.setControladorIA(ci);
      nuevaGeneracion.add(nv);

// DESPUÉS:
      RedNeuronal redHijo = new RedNeuronal(5, 4, 2);
      redHijo.setPesosDesdeArray(hijo);
      // NO mutar aquí — la mutación la aplica mutacion() justo después
      ControladorIA ci = new ControladorIA(redHijo);
      Vehiculo nv = new Vehiculo(spawnX, spawnY, 40, 20, ci, pista);
      nv.setAngulo(0);
      nv.setControladorIA(ci);
      nuevaGeneracion.add(nv);
```

---

**Cambio 3.2 — Reemplazar `seleccionarPadre()` con selección por torneo**

La ruleta de fitness colapsa cuando todos los fitness son ≈0 (siempre retorna `vehiculos.get(0)`). El torneo funciona correctamente incluso con fitness uniformes porque introduce aleatoriedad real.

```java
// ANTES (método completo):
private Vehiculo seleccionarPadre() {
    double totalFitness = vehiculos.stream().mapToDouble(Vehiculo::getFitness).sum();
    double r = rand.nextDouble() * totalFitness;
    double acumulado = 0;
    for (Vehiculo v : vehiculos) {
        acumulado += v.getFitness();
        if (acumulado >= r)
            return v;
    }
    return vehiculos.get(0);
}

// DESPUÉS:
private Vehiculo seleccionarPadre() {
    // Torneo de tamaño 3: elige el mejor entre 3 candidatos aleatorios.
    // Funciona aunque todos los fitness sean iguales (introduce diversidad real).
    Vehiculo mejor = null;
    for (int t = 0; t < 3; t++) {
        Vehiculo candidato = vehiculos.get(rand.nextInt(vehiculos.size()));
        if (mejor == null || candidato.getFitness() > mejor.getFitness()) {
            mejor = candidato;
        }
    }
    return mejor;
}
```

---

### ARCHIVO 4: `GestorRed.java`

**Cambio 4.1 — Hacer robusta la resolución de ruta**

El bloque estático actual usa `new File("app").exists()` que depende del directorio de trabajo al lanzar la JVM. Si se lanza desde el subdirectorio `app/` (ej. desde IntelliJ), la condición falla y se usa la ruta incorrecta. La red se guarda en un lugar y se busca en otro → persistencia rota.

```java
// ANTES (bloque static completo):
static {
    File archivo = new File("app/src/main/resources/mejor_red.json");
    if (archivo.exists() || new File("app").exists()) {
        RUTA_ARCHIVO = "app/src/main/resources/mejor_red.json";
    } else {
        RUTA_ARCHIVO = "src/main/resources/mejor_red.json";
    }
}

// DESPUÉS:
static {
    // Estrategia de resolución en orden de preferencia:
    // 1. Si el archivo ya existe en alguna ruta conocida → usar esa ruta
    // 2. Si el directorio padre existe (se puede escribir) → usar esa ruta
    // 3. Fallback a directorio home del usuario (siempre funciona)
    String[] candidatos = {
        "app/src/main/resources/mejor_red.json",
        "src/main/resources/mejor_red.json"
    };
    String rutaElegida = System.getProperty("user.home")
        + java.io.File.separator + ".simulador_ia"
        + java.io.File.separator + "mejor_red.json";

    for (String ruta : candidatos) {
        File f = new File(ruta);
        if (f.exists()) {
            rutaElegida = ruta;  // archivo ya existe aquí → usar esta ruta
            break;
        }
        if (f.getParentFile() != null && f.getParentFile().isDirectory()) {
            rutaElegida = ruta;  // directorio existe, podemos escribir aquí
            break;
        }
    }
    RUTA_ARCHIVO = rutaElegida;
}
```

> **Nota:** Si ya existe un `mejor_red.json` guardado, la nueva lógica lo encontrará igual. Si no existe, se creará en la primera ruta cuyo directorio padre exista (normalmente `app/src/main/resources/`), o en `~/.simulador_ia/` como fallback universal.

---

## Resumen de cambios por archivo

| Archivo | Cambio | Bug que resuelve |
|---|---|---|
| `Vehiculo.java` | Campo `framesVivo` + incremento en `update()` + condición `&& framesVivo > 30` + reset | Muerte instantánea (principal) + IA no aprende (secundario) |
| `Vehiculo.java` | Reset `framesVivo = 0` en `reset()` | Muerte instantánea al reiniciar carrera |
| `Simulador.java` | `esSpawnSeguro()` solo verifica paredes, no meta | Muerte instantánea (elimina el bug lógico del loop de márgenes) |
| `Poblacion.java` | Eliminar `redHijo.mutar(0.1)` en `crossover()` | IA no aprende (doble mutación destruye buenas soluciones) |
| `Poblacion.java` | Reemplazar ruleta por torneo en `seleccionarPadre()` | IA no aprende (selección colapsa con fitness uniformes) |
| `GestorRed.java` | Resolución robusta de ruta para `mejor_red.json` | Persistencia de memoria de la IA entre sesiones |

## Orden de aplicación recomendado

1. `Vehiculo.java` (cambios 1.1 → 1.4) — este es el fix principal, todo depende de él
2. `Simulador.java` (cambio 2.1) — elimina el bug lógico de `esSpawnSeguro`
3. `Poblacion.java` (cambios 3.1 y 3.2) — mejora el aprendizaje de la IA
4. `GestorRed.java` (cambio 4.1) — garantiza que la memoria persista entre sesiones

## Cómo verificar que los fixes funcionan

**Para muerte instantánea:**
- Al entrar a "COMPETIR VS IA", el vehículo rojo (jugador) debe poder moverse con las teclas de dirección.
- Si en los primeros 30 frames el vehículo logra avanzar, el fix está funcionando.
- Ya no debería aparecer "PERDISTE!" o "EMPATE!" antes de que el jugador pueda mover el vehículo.

**Para persistencia de IA:**
- Entrenar la IA durante varias generaciones. Observar que el fitness en el gráfico SUBE con el tiempo (no se queda plano).
- Presionar G para guardar, salir al menú, volver a entrenar. Los vehículos de la nueva sesión deben partir de donde quedó la sesión anterior (el fitness inicial debe ser mayor a 0).
- En "COMPETIR VS IA" con dificultad DIFICIL, la IA debe comportarse de forma competente (no chocar de inmediato).
