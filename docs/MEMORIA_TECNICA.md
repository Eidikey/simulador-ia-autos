# Memoria Técnica — Simulador de Autos Autónomos

## 1. Algoritmo de IA Seleccionado: Neuroevolución (Algoritmo Genético)

### 1.1 Explicación del Algoritmo

Se implementó un **Algoritmo Genético (GA)** para entrenar una **Red Neuronal Artificial (RNA)** que controla un vehículo autónomo en un entorno 2D. Este enfoque pertenece a la familia de **Neuroevolución**, donde los pesos de la red neuronal evolucionan a través de generaciones mediante selección natural, crossover y mutación.

**Arquitectura de la Red Neuronal:**
- **Capa de entrada**: 5 neuronas (lectura de 5 sensores de distancia)
- **Capa oculta**: 4 neuronas con activación sigmoide
- **Capa de salida**: 2 neuronas (1 para giro, 1 para aceleración)
- **Pesos totales**: (5×4) + (4×2) = 28 pesos sinápticos

**Arquitectura del Algoritmo Genético:**
- **Población**: 50 individuos por generación
- **Selección**: Ruleta proporcional al fitness (fitness-proportionate selection)
- **Elitismo**: El mejor individuo pasa intacto a la siguiente generación
- **Crossover**: Cruce de un punto (single-point crossover) entre dos padres
- **Mutación**: Tasa dinámica (0.1 base, 0.4 si hay estancamiento >5 generaciones)
- **Tasa de mutación dinámica**: Se incrementa cuando el fitness no mejora para evitar mínimos locales

### 1.2 Justificación de su Uso

| Criterio | Evaluación |
|----------|------------|
| **Problema continuo** | El control de un vehículo requiere salidas continuas (giro, aceleración), no acciones discretas |
| **Espacio de estados grande** | Los sensores devuelven valores continuos 0-1, creando un espacio de estados virtualmente infinito |
| **Sin supervisión** | No hay datos etiquetados de "cómo conducir correctamente" |
| **Eficiencia computacional** | El GA procesa 50 individuos en paralelo por generación, mucho más rápido que DDQN que requiere miles de episodios secuenciales |
| **Simplicidad de implementación** | GA con RNA es significativamente más simple de implementar que DDQN (sin replay buffer, target network, o actualización por gradientes) |

**¿Por qué NO se eligió DDQN?**
- DDQN requiere un entorno donde las acciones sean discretas o un actor-crítico para acciones continuas (DDPG), lo que aumenta la complejidad
- El entrenamiento de DDQN es secuencial (un episodio a la vez), mientras que el GA evalúa 50 individuos en paralelo
- DDQN necesita miles de episodios para converger; el GA muestra mejora visible en decenas de generaciones
- El proyecto tiene recursos computacionales limitados (CPU, sin GPU)

### 1.3 Integración en el Sistema

**Representación del agente (estado-acción):**

```
Estado (input): [distanciaSensor1, distanciaSensor2, ..., distanciaSensor5]
    Cada sensor: 0.0 (pared) a 1.0 (300px libre)
    
Acción (output): [giro, aceleracion]
    Giro: -1.0 (izquierda) a +1.0 (derecha)
    Aceleración: -1.0 (freno) a +1.0 (acelerar)
```

**Flujo de entrenamiento por generación:**

```
1. Crear 50 redes neuronales con pesos aleatorios [-1, 1]
2. Para cada red:
   a. Asignarla a un vehículo
   b. Ejecutar simulación hasta que el vehículo muera
   c. Calcular fitness = distanciaAvance + (velocidad × 10) - (rotaciónAcumulada × 0.3)
3. Ordenar vehículos por fitness (mayor a menor)
4. Elitismo: copiar el mejor individuo a la siguiente generación
5. Seleccionar padres por ruleta (probabilidad proporcional al fitness)
6. Crossover de un punto entre pares de padres → 49 hijos
7. Mutar hijos con tasa dinámica
8. Repetir desde paso 2
```

**Ciclo de vida de un vehículo durante la simulación:**

```
Cada frame (~60 fps):
1. Leer 5 sensores (raycasting en 5 direcciones: -90°, -45°, 0°, 45°, 90°)
2. Normalizar distancias a [0, 1]
3. Forward pass por la red neuronal → [giro, aceleracion]
4. Aplicar física: velocidad += aceleracion + 0.1; ángulo += giro
5. Calcular nueva posición: x += velocidad × cos(ángulo); y += velocidad × sin(ángulo)
6. Verificar colisiones (4 esquinas del rectángulo, paso cada 2px)
7. Si colisiona con pared → muerto
8. Si detecta línea roja (meta) distancia < 5/300 → haCruzadoMeta = true
```

### 1.4 Función de Fitness

```java
fitness = distanciaAvance + (velocidad * 10) - (rotacionAcumulada * 0.3)
```

**Componentes:**
- `distanciaAvance`: Distancia euclidiana desde el punto de spawn (recompensa progreso)
- `velocidad * 10`: Recompensa por mantener velocidad alta (evita estancamiento)
- `rotacionAcumulada * 0.3`: Penaliza giros excesivos (evita comportamientos erráticos)

**Bonificación por cruzar la meta:** +50000 al fitness

### 1.5 Resultados del Entrenamiento

| Métrica | Valor |
|---------|-------|
| Generaciones para mejora visible | 5-10 |
| Generaciones para comportamiento estable | 20-50 |
| Fitness máximo observado (sin meta) | ~5000 |
| Fitness con cruce de meta | ~55000 |
| Tamaño de población | 50 |
| Tasa de mutación base | 0.1 (10%) |
| Tasa de mutación (estancado) | 0.4 (40%) |

**Comportamiento observado durante el entrenamiento:**
1. **Generaciones 1-5**: Los vehículos giran en círculos o se estrellan inmediatamente
2. **Generaciones 5-15**: Los vehículos aprenden a avanzar en línea recta y girar suavemente
3. **Generaciones 15-30**: Los vehículos navegan curvas y esquinas correctamente
4. **Generaciones 30+**: Los vehículos siguen el carril de manera consistente

---

## 2. Principios SOLID Aplicados

### SRP (Single Responsibility Principle)
- **`view/`**: Responsable exclusivamente del renderizado
- **`controller/`**: Responsable exclusivamente de la captura de entrada
- **`model/`**: Responsable de la lógica de negocio y entidades
- **`engine/`**: Responsable del ciclo de juego y máquina de estados
- **`ai/`**: Responsable de la lógica de inteligencia artificial

### OCP (Open/Closed Principle)
- Nuevos controladores implementan `Controlador` sin modificar `Vehiculo`
- Nuevos algoritmos de IA pueden añadirse implementando `Controlador`
- `Renderizador` puede extenderse añadiendo nuevos métodos de dibujo

### LSP (Liskov Substitution Principle)
- `ControladorIA` y `ControladorJugador` son intercambiables como `Controlador`
- Cualquier implementación de `Controlador` puede controlar un `Vehiculo`

### ISP (Interface Segregation Principle)
- `Controlador`: Solo 2 métodos específicos (`obtenerGiro`, `obtenerAceleracion`)
- Cada clase view tiene métodos específicos sin interfaces generales

### DIP (Dependency Inversion Principle)
- `Vehiculo` depende de `Controlador` (abstracción) y `Pista` (abstracción)
- `Simulador` depende de `GestorEntradas`, `Renderizador` y `Pista`
- `Sensor` depende de `Pista` en lugar de acceder directamente a PixelReader

---

## 3. Estructura del Sistema (MVC)

```
┌─────────────────────────────────────────────────────────┐
│                     main/Main.java                       │
│                 (Punto de entrada JavaFX)                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  engine/Simulador                        │
│           (Controlador del ciclo de juego)               │
│           State machine: MENU/ENTRENAMIENTO/CARRERA      │
└──┬──────────┬──────────┬──────────┬─────────────────────┘
   │          │          │          │
   ▼          ▼          ▼          ▼
┌──────┐ ┌────────┐ ┌────────┐ ┌─────────┐
│ view │ │controller│ │ model  │ │   ai    │
│Render│ │GestEntr │ │Entidad │ │RedNeur  │
│ HUD  │ │CtrlJug │ │Vehiculo│ │CtrlIA   │
│Graf  │ │         │ │Sensor  │ │Poblacion│
│RendV │ │         │ │Pista   │ │GestRed  │
│      │ │         │ │Jugador │ │         │
└──────┘ └────────┘ └────────┘ └─────────┘
```

---

## 4. Mapa de Colores (Pista)

| Color | Función | Condición |
|-------|---------|-----------|
| Negro (#000) | Muro/Pared | R<0.1, G<0.1, B<0.1 |
| Rojo (#F00) | Línea de Meta | R>0.9, G<0.1, B<0.1 |
| Verde (#0F0) | Pista transitable | No negro |
| Azul (#00F) | Zona de Spawn | B>0.5, R<0.5, G<0.5 |
