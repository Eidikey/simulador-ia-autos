# Simulador de Autos Autónomos (IA) 🚗🧠

Proyecto final de programación. Simulador 2D construido con **JavaFX** donde un agente de Inteligencia Artificial aprende a conducir usando **Neuroevolución** (Algoritmos Genéticos).

## 🛠️ Stack Tecnológico
- **Lenguaje:** Java 17+
- **Interfaz Gráfica:** JavaFX
- **Gestor de Dependencias:** Gradle
- **Estructura:** MVC y principios SOLID

## 🚀 Cómo ejecutar el proyecto (Para el equipo)

No necesitas instalar Gradle en tu sistema, el proyecto incluye un Wrapper (`gradlew`). 

1. Clona este repositorio:
   git clone [URL_DEL_REPO]

2. Abre una terminal en la carpeta del proyecto.

3. Compila y ejecuta:
   - **En Windows (PowerShell/CMD):**
     .\gradlew run
   - **En Linux/Mac:**
     ./gradlew run

## 📂 Estructura del Código
- `src/main/java/main`: Punto de entrada de la aplicación.
- `src/main/java/model`: Entidades lógicas (Vehículo, Pista) aplicando SRP.
- `src/main/java/view`: Renderizado en el Canvas de JavaFX.
- `src/main/java/controller`: Entradas del usuario.
- `src/main/java/engine`: Ciclo del simulador (AnimationTimer).
- `src/main/java/ai`: Lógica de la Red Neuronal y Algoritmo Genético.
