# AGENTS.md — Criterios del proyecto NoCuelguesApp

Este archivo es la referencia común para el equipo y para cualquier agente de IA que trabaje en el repositorio. Antes de cambiar código, leerlo completo. Si una tarea contradice algo de acá, preguntar antes de hacerla.

## Proyecto

App Android para registrar vencimientos personales (documentos, carnets, seguros, garantías, salud, suscripciones) y verlos ordenados por días restantes. Trabajo Práctico 2 de Aplicaciones Móviles. Equipo: Díaz Lucas, López Luciano, Vallejos Matías.

| Fecha | Entrega |
|-------|---------|
| 15-10-2026 | Parcial: ícono, desarrollo parcial y horas reales |
| 22-10-2026 | Final: app completa con notificaciones y traducción |
| 29-10-2026 | Presentación |

Requisitos de la consigna que no se negocian: al menos dos Activities, español e inglés, persistencia local. No se programa acceso a base de datos; las tablas solo se documentan.

## Forma de trabajo

- Un cambio por vez, chico y verificable. No reescribir pantallas enteras ni mezclar varias tareas en un commit.
- Todo cambio tiene que poder explicarse en la defensa: preferir la solución simple que entiende el equipo antes que la sofisticada.
- Antes de dar algo por terminado: `cd app-android && ./gradlew testDebugUnitTest assembleDebug` sin errores y prueba manual en emulador o celular.
- No agregar bibliotecas, permisos, pantallas ni funcionalidades fuera del alcance sin acordarlo con el equipo.
- No cambiar `minSdk` (26), el tema claro único ni el diseño visual.

## Alcance

| Versión | Incluye |
|---------|---------|
| Parcial (15-10) | Pantalla principal con contadores, listado ordenado y estado vacío; formulario de alta, edición y eliminación con confirmación; persistencia; español e inglés; ícono; pantalla de carga (`SplashScreen`) |
| Final (22-10) | Lo anterior más el aviso local antes del vencimiento y el recuadro de recordatorio del formulario |

Fuera de alcance: modo oscuro, cuentas, sincronización en la nube, adjuntos, base de datos.

## Arquitectura

Paquete `com.iessf.nocuelguesapp`, organizado en capas. Cada capa solo conoce a las de más adentro.

| Paquete | Contiene | Regla |
|---------|----------|-------|
| `domain.model` | `ExpirationItem`, `Category`, `Urgency` | Java puro, sin nada de Android (ni `R`, ni `Context`) |
| `domain.repository` | `ExpirationRepository` (interfaz) | Contrato de datos, sin detalles de almacenamiento |
| `data` | `ExpirationItemRepository` | Implementa el contrato con `SharedPreferences` + JSON (`org.json`) |
| `application` | `ExpirationService` | Lógica de la app: orden, resumen, alta, baja, búsqueda |
| `presentation` | Activities, adapter, `ExpirationResources` | Pantallas. `ExpirationResources` traduce enums a textos, íconos y fondos |
| `notifications` | `ReminderScheduler`, `ReminderReceiver`, `ReminderRestoreReceiver` | Avisos con `AlarmManager`. Cuándo avisar lo decide `ReminderPolicy` (en `application`, con test) |

- Las Activities usan `ExpirationService`, nunca el repositorio directo.
- La lógica que se pueda probar sin Android va en `application` y lleva test en `app/src/test/` (ver `ExpirationServiceTest`).
- Los enums se guardan por su nombre (`name()`): no renombrar constantes de `Category` ni `Urgency`, porque se pierden los datos guardados.
- Nunca depender de la posición (`ordinal()`, `values()[i]`) para mapear vistas a enums. Usar `setTag` o un `switch` explícito.

## Convenciones de código

- Todo identificador en inglés: clases, métodos, variables, ids, nombres de archivo y recursos. Nada de `FormularioActivity`, `fechaSeleccionada`, `edtNombre`.
- Comentarios y Javadoc en español (en Java y en XML), para que el equipo los lea y los explique sin esfuerzo. Solo cuando aportan algo que el código no muestra: una restricción, un motivo o un detalle de Android poco obvio.
- Ningún texto visible en Java ni en layouts: todo en `res/values/strings.xml` (español, por defecto) y `res/values-en/strings.xml` (inglés), siempre en los dos. Cantidades con `plurals`. Antes de crear un texto, buscar si ya existe.
- Ningún color, tamaño o margen suelto en layouts si existe recurso: `@color/...`, `@dimen/...`, `@integer/...`, estilos `Widget.NoCuelgues.*` y `TextAppearance.NoCuelgues.*`.
- Ids de vistas en `snake_case` con prefijo por tipo:

| Prefijo | Vista |
|---------|-------|
| `text_` | `TextView` de solo lectura |
| `input_` | Campo editable o seleccionable (`EditText`, campo de fecha) |
| `button_` | Botones e `ImageButton` |
| `image_` | `ImageView` |
| `layout_` | Contenedores |
| `list_` | `RecyclerView` |
| `chip_`, `chip_group_` | `Chip` y `ChipGroup` |
| `radio_`, `radio_group_` | `RadioButton` y `RadioGroup` |

- Estilo Java: cuerpo de cada método en líneas propias (nada de `int getTotal() { return total; }` en una línea), imports en lugar de nombres completos (`Intent`, no `android.content.Intent`), campos privados.
- Commits en español, descriptivos, solo con archivos de `app-android/` y archivos de raíz del repo. No commitear `.idea/` modificado, `build/`, `docs/` ni archivos de Cursor.

## Diseño visual

Estilo neobrutalista: bordes negros gruesos, sombras duras desplazadas sin desenfoque, colores planos, tipografía Space Grotesk. La especificación completa está en `docs/desarrollo/sistema-de-diseno.md` (pedirla al equipo si no la tenés) y el prototipo en `prototype/index.html`. Lo esencial:

- Fondo de pantalla `@color/paper`. Colores en `res/values/colors.xml`; no inventar colores nuevos.
- Sombras con drawables `bg_*` (`layer-list`), elevación 0. No usar `elevation` ni `CardView`.
- Botones: `androidx.appcompat.widget.AppCompatButton` con estilo `Widget.NoCuelgues.Button` y sus variantes (`.Surface`, `.Danger`, `.Small`). No usar `<Button>` (el tema lo convierte en `MaterialButton` y pierde el fondo) ni `MaterialButton` ni `FloatingActionButton`.
- Campos: `EditText` con `Widget.NoCuelgues.Input`, etiqueta `Widget.NoCuelgues.FieldLabel`, contador `FieldCounter` y error `FieldError` debajo (con `bg_input_error`). No usar `TextInputLayout` ni `setError()`.
- Categoría: `ChipGroup` con `Chip` de estilo `Widget.NoCuelgues.Chip`, generados desde `Category.values()`. No usar `Spinner`.
- Urgencia: control segmentado con `RadioGroup` y `Widget.NoCuelgues.UrgencyOption`.
- Diálogos: layout propio con `bg_dialog` y ventana transparente, como `dialog_confirm_delete.xml`.
- Logo: usar `ic_logo_small` donde vaya la marca. No usar íconos de campana.
- Íconos: vectores `ic_*` de Material Symbols ya incluidos en `res/drawable/`. Si falta uno, agregarlo con el mismo formato.

## Estado actual

Hecho: pantalla principal, formulario completo (alta, edición, eliminación con confirmación), persistencia, ícono de la app, pantalla de carga animada, español e inglés, avisos locales con recuadro de recordatorio configurable (días de anticipo y hora).
Pendiente: presentación (29-10).
