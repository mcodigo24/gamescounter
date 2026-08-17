<div align="center">

# 🎲 Kounta

**Anotador de puntajes para tus juegos de cartas y dados favoritos.**

Truco · Generala · Chin Chon · 10Mil · Comodín

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-26-blue)
![Target SDK](https://img.shields.io/badge/targetSdk-35-blue)
![License](https://img.shields.io/badge/license-unspecified-lightgrey)

</div>

---

## ✨ Qué es

**Kounta** es una app nativa de Android, escrita 100% en Kotlin + Jetpack Compose, para llevar el puntaje de partidas de cartas y dados sin lápiz, papel ni conexión a internet. Todo el estado se guarda localmente en el dispositivo y se recupera automáticamente si cerrás la app a mitad de partida.

Sin backend. Sin cuentas. Sin anuncios. Solo abrís el juego, elegís tu partida y jugás.

## 🎮 Juegos disponibles

| Juego | Descripción |
|---|---|
| 🃏 **Truco** | Marcador clásico Nosotros vs. Ellos, de 0 a 30 puntos. |
| 🎲 **Generala** | Planilla completa con hasta 12 jugadores, todas las filas (1 a 6, Escalera, Full, Póker, Generala, Doble Generala) y totales automáticos. |
| 🀄 **Chin Chon** | Planilla por rondas con valores positivos y negativos, total acumulado y aviso visual al llegar a 100+. |
| 🎯 **10Mil** | Planilla por rondas arrancando en 750, con meta exacta de 10.000 puntos y aviso visual al ganar. |
| 🃏 **Comodín** | Anotador genérico por rondas para cualquier otro juego que se te ocurra. |

## 🧩 Características

- **Autoguardado** — cada partida se persiste sola 30 segundos después del último cambio, y también al minimizar la app.
- **Expiración automática** — las partidas guardadas sin actividad caducan solas después de 1 día, para que siempre arranques limpio.
- **Sin conexión** — funciona 100% offline, todo el estado vive en el dispositivo.
- **Diseño propio** — interfaz Material 3 con una paleta cálida (claymorfismo oscuro ámbar/durazno) pensada para leerse bien en la mesa.
- **Reinicio con confirmación** — nunca perdés una partida por accidente.

## 🛠️ Stack técnico

- **Kotlin** + **Jetpack Compose** (Material 3) para toda la UI
- **AndroidX DataStore Preferences** para persistencia local por juego
- **StateFlow** + `AndroidViewModel` para el manejo de estado reactivo
- Sin librería de navegación: un `when` simple sobre un estado `rememberSaveable`
- Arquitectura de **vertical slices**: cada juego es autocontenido en `Models` + `Repository` + `ViewModel` + `Screen`

## 🚀 Cómo correrlo

### Requisitos

1. [Android Studio](https://developer.android.com/studio) (Ladybug o más reciente)
2. **JDK 17** (Android Studio suele instalarlo junto con el SDK)
3. Un emulador Android o un teléfono físico con depuración USB activada

### Desde Android Studio

1. `File → Open` → seleccioná la carpeta del proyecto.
2. Esperá a que Gradle sincronice (descarga dependencias la primera vez).
3. Creá un dispositivo virtual en **Device Manager** (por ejemplo Pixel 6, API 34) o conectá tu teléfono.
4. Presioná **Run** ▶️ (o `Shift+F10`).

### Desde terminal

Con `JAVA_HOME` y el Android SDK configurados:

```bash
./gradlew installDebug     # compila e instala el APK debug en el dispositivo conectado
./gradlew assembleDebug    # solo compila el APK debug
./gradlew build            # build completo (compilar + lint + assemble)
./gradlew lint             # Android lint
```

> En Windows, si no tenés `gradlew`, Android Studio genera el wrapper al sincronizar por primera vez.

## 📁 Estructura del proyecto

Cada juego vive como una porción vertical autocontenida bajo `app/src/main/java/com/gamescounter/truco/`:

```
com/gamescounter/truco/
├── MainActivity.kt              # Entrada de la app, navegación simple, tema Compose
├── TrucoViewModel.kt            # Truco vive en el paquete raíz junto a los archivos compartidos
├── SaveStatus.kt                # Estado de guardado compartido entre juegos
├── data/                        # Modelo y repositorio de Truco + constantes globales
├── generala/                    # Models · Repository · ViewModel de Generala
├── chinchon/                    # Models · Repository · ViewModel de Chin Chon
├── diezmil/                     # Models · Repository · ViewModel de 10Mil
├── comodin/                     # Models · Repository · ViewModel de Comodín
└── ui/
    ├── HomeScreen.kt            # Selector de juegos
    ├── TrucoScreen.kt / GeneralaScreen.kt / ChinChonScreen.kt / DiezMilScreen.kt / ComodinScreen.kt
    ├── components/              # Piezas reutilizables (top bar, setup, grillas)
    └── theme/                   # Paleta y tipografía Material 3
```

Cada juego sigue el mismo patrón de 3 archivos:

- **`<Juego>Models.kt`** — data classes, enums, constantes de validación y normalización
- **`<Juego>Repository.kt`** — persistencia en DataStore, carga/guardado/borrado
- **`<Juego>ViewModel.kt`** — estado UI reactivo (`StateFlow`) y autoguardado programado

## 🗺️ Roadmap

- [ ] Exportar o compartir planillas de puntaje
- [ ] Vibración o sonido al ganar
- [ ] Nombres completos de jugadores en lugar de iniciales
- [ ] Tests automatizados (unitarios e instrumentados)

---

<div align="center">

Hecho con 🧉 y Kotlin.

</div>
