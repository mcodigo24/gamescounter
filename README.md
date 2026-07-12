# Kounta (Android)

Aplicación nativa Android para anotar puntajes de juegos: **Truco**, **Generala**, **Chin Chon** y **10Mil**.

## Requisitos en tu PC

1. **Android Studio** (Ladybug o más reciente): https://developer.android.com/studio  
2. **JDK 17** (Android Studio suele instalarlo con el SDK).  
3. Un **emulador Android** o un **teléfono** con depuración USB activada.

## Cómo abrir y ejecutar el proyecto

1. Abrí Android Studio → **File → Open** → carpeta `gamescounter`.  
2. Esperá a que Gradle sincronice (descarga dependencias la primera vez).  
3. Creá un dispositivo virtual: **Device Manager → Create Device** (por ejemplo Pixel 6, API 34).  
4. Pulsá **Run** (triángulo verde) o `Shift+F10`.  
5. La app se instala y abre en el emulador o en el teléfono conectado.

### Desde terminal (opcional)

Con `JAVA_HOME` y Android SDK configurados:

```bash
cd c:/Dev/vibecode/gamescounter
./gradlew installDebug
```

En Windows, si no tenés `gradlew`, Android Studio genera el wrapper al sincronizar; también podés usar **Build → Make Project** desde el IDE.

## Comportamiento actual

- **Inicio:** selector de 4 juegos.
- **Truco:** equipos Nosotros/Ellos, puntaje 0 a 30, botones +/-, reinicio con confirmación.
- **Generala:** grilla editable con hasta 12 jugadores, filas 1,2,3,4,5,6,E,F,P,G,2G y total automático.
- **Chin Chon:** planilla por rondas con valores positivos/negativos, total acumulado, columna roja al llegar a 100+ y reinicio al cargar un nuevo valor.
- **10Mil:** planilla por rondas, entrada desde 750 en la primera casilla, meta exacta en 10000, columna verde al ganar y fila “Faltan”.
- **Guardado:** DataStore por juego, autoguardado luego de 30 s sin cambios.
- **Retención:** los datos expiran después de 5 días sin actividad.

## Estructura del código

```
app/src/main/java/com/gamescounter/truco/
├── MainActivity.kt          # Entrada de la app, tema Compose
├── TrucoViewModel.kt        # Estado UI, límites 0–30, auto-guardado
├── SaveStatus.kt            # Estado de guardado compartido
├── generala/
│   ├── GeneralaModels.kt    # Filas, reglas y modelo de planilla
│   ├── GeneralaRepository.kt# Persistencia de generala con caducidad
│   └── GeneralaViewModel.kt # Edición de celdas y auto-guardado
├── chinchon/
│   ├── ChinChonModels.kt
│   ├── ChinChonRepository.kt
│   └── ChinChonViewModel.kt
├── diezmil/
│   ├── DiezMilModels.kt
│   ├── DiezMilRepository.kt
│   └── DiezMilViewModel.kt
├── data/
│   ├── TrucoScore.kt        # Modelo y constantes
│   └── TrucoRepository.kt   # Lectura/escritura DataStore + expiración 5 días
└── ui/
    ├── HomeScreen.kt        # Inicio con selector de juegos
    ├── TrucoScreen.kt       # Pantalla dividida y controles
    ├── GeneralaScreen.kt    # Grilla editable de generala
    ├── ChinChonScreen.kt    # Planilla por rondas con negativos
    ├── DiezMilScreen.kt     # Planilla 10Mil con validaciones
    ├── components/          # Setup, top bar y celdas reutilizables
    └── theme/               # Colores Material 3 (verde minimalista)
```

## Próximos pasos posibles

- Exportar/compartir planillas.
- Vibración o sonido al ganar/perder.
- Nombres completos de jugadores en lugar de iniciales.
