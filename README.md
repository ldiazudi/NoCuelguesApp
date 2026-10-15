# NoCuelguesApp

Aplicación Android en Java para registrar vencimientos personales (documentos, carnets, seguros, garantías, salud, suscripciones y otros) y consultarlos ordenados por días restantes.

**Trabajo Práctico 2 — Aplicaciones Móviles**  
IES · Equipo: Díaz Lucas, López Luciano, Vallejos Matías

## Características

- Pantalla principal con contadores (vencidos, esta semana, total), listado ordenado y estado vacío
- Formulario de alta, edición y eliminación con diálogo de confirmación
- Categorías fijas y nivel de urgencia (alta / media / baja)
- Persistencia local con `SharedPreferences` y JSON (`org.json`)
- Interfaz en español e inglés
- Pantalla de carga (`SplashScreen`) con logo animado
- Ícono de la aplicación

## Stack técnico

| Pieza | Valor |
|-------|--------|
| Lenguaje | Java 11 |
| UI | Activities + XML (AppCompat, Material Components, ConstraintLayout) |
| minSdk / targetSdk / compileSdk | 26 / 37 / 37 |
| AGP | 9.4.1 |
| Persistencia | `SharedPreferences` + JSON |
| Splash | AndroidX Core SplashScreen |
| Tests unitarios | JUnit (`ExpirationServiceTest`) |

Paquete base: `com.iessf.nocuelguesapp`

## Estructura del proyecto

```
app-android/
├── app/src/main/java/com/iessf/nocuelguesapp/
│   ├── domain/model/          # ExpirationItem, Category, Urgency
│   ├── domain/repository/     # Contrato ExpirationRepository
│   ├── data/                  # ExpirationItemRepository (SharedPreferences)
│   ├── application/           # ExpirationService (casos de uso)
│   └── presentation/          # MainActivity, FormActivity, adapter, recursos UI
├── app/src/main/res/          # Layouts, drawables, strings (es / en), temas
└── app/src/test/              # Tests unitarios de la capa application
```

Cada capa solo depende de las más internas. Las Activities usan `ExpirationService`; no acceden al repositorio directamente.

## Cómo compilar y ejecutar

### Requisitos

- Android Studio (versión compatible con AGP 9.4.1)
- JDK 11 o superior
- Emulador o dispositivo con API 26+

### Desde Android Studio

1. Abrir la carpeta `app-android/` como proyecto.
2. Esperar la sincronización de Gradle.
3. Ejecutar el módulo `app` en un emulador o dispositivo.

### Desde la terminal

```bash
cd app-android
./gradlew testDebugUnitTest assembleDebug
```

El APK de depuración queda en `app/build/outputs/apk/debug/`. Para instalarlo en un dispositivo o emulador conectado:

```bash
./gradlew installDebug
```
