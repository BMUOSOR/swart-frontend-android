# PRIMER MVP — SWART

---

## 5. Cronología del TFG

Como se detalló anteriormente, nuestro proyecto se guía por un enfoque híbrido que combina
Lean Startup con metodologías ágiles. Para ello, la planificación temporal se divide en
sprints: ciclos de trabajo cortos y de duración fija (generalmente entre una y cuatro semanas)
en los que un equipo Scrum trabaja para completar un conjunto determinado de tareas y
generar un incremento de producto funcional y utilizable.

Los sprints se organizan en función de la prioridad de desarrollo de los requisitos de la
aplicación, los cuales se incorporan inicialmente al backlog, entendido como la lista dinámica,
ordenada y pública que recoge todas las necesidades del producto. Esta priorización guía la
organización de los sprints y, en nuestro caso, se basa en factores como la implementación
de funcionalidades básicas de la aplicación, la complejidad del desarrollo y el impacto
esperado en el usuario.

Para establecer estas prioridades se utilizará la técnica MoSCoW, metodología que permite
clasificar los requisitos según su nivel de importancia y necesidad dentro del proyecto.
Asimismo, dado el tiempo limitado de desarrollo, decidimos que los sprints tendrían una
duración estimada de entre dos y tres semanas.

El desarrollo se organizó en dos MVPs sucesivos. El primero, compuesto por tres sprints,
tenía como objetivo construir y validar las funcionalidades esenciales de la plataforma. El
segundo MVP, de un sprint de duración, incorporó las mejoras derivadas del primer
experimento y amplió las capacidades de la aplicación para alcanzar un producto más pulido
y representativo.

El primer paso consistió en delimitar las características que queríamos incluir en el primer
MVP utilizando la técnica MoSCoW.

> **Figura 31:** Diagrama aplicando técnica MoSCoW para el primer MVP de SWART.

---

## 5.1 Sprint 1 — Investigación, planificación y configuración del entorno

**Duración:** [fecha inicio] – [fecha fin]

El primer sprint no estuvo orientado a la producción de funcionalidades visibles, sino a
sentar las bases sobre las que se construiría toda la aplicación. Esta fase es crítica en
cualquier proyecto de desarrollo de software, ya que las decisiones técnicas y arquitectónicas
tomadas en este momento condicionan la evolución del producto a lo largo de todas las
iteraciones posteriores.

El sprint se estructuró en torno a tres ejes principales: la investigación y definición del
producto, la selección del stack tecnológico y la configuración completa del entorno de
trabajo.

### Investigación y definición del producto

El equipo dedicó tiempo a analizar el mercado de aplicaciones culturales y de descubrimiento
artístico, identificando referentes tanto nacionales como internacionales. Se estudiaron
aplicaciones como Artsy, Fever o Google Arts & Culture con el objetivo de detectar sus
fortalezas, sus limitaciones y los nichos que no cubrían adecuadamente, en especial la
visibilización del artista emergente local. Este análisis sirvió de base para refinar la propuesta
de valor diferencial de SWART y fundamentar las decisiones de diseño de experiencia de
usuario.

Paralelamente, se elaboró una primera versión del mapa de usuario (user journey) para cada
uno de los dos perfiles de la plataforma —artista e interesado—, con el fin de anticipar los
flujos de interacción principales y detectar posibles fricciones antes de comenzar el
desarrollo.

### Selección y documentación del stack tecnológico

Tras el análisis de alternativas, el equipo acordó el stack tecnológico definitivo:

- **Frontend móvil:** Android nativo con Kotlin y Jetpack Compose, por su capacidad para
  construir interfaces declarativas modernas y su integración nativa con el ecosistema Android.
- **Backend y base de datos:** Supabase como plataforma Backend-as-a-Service (BaaS),
  que proporciona autenticación, base de datos PostgreSQL en tiempo real, almacenamiento de
  ficheros y APIs REST generadas automáticamente, reduciendo significativamente el tiempo de
  desarrollo de la capa de servidor.
- **API de mapas:** integración con OpenStreetMap mediante la librería OSMDroid, por ser
  una alternativa de código abierto que no requiere clave de pago y ofrece suficiente
  flexibilidad para la personalización de marcadores y estilos de mapa.

Se documentaron las decisiones técnicas adoptadas y las razones que las justificaban, de modo
que cualquier integrante del equipo pudiese incorporarse al desarrollo con contexto suficiente.

### Configuración del entorno de desarrollo

Una vez definida la arquitectura, se procedió a la configuración completa del entorno:

- **Creación y configuración de repositorios:** se crearon los repositorios de frontend y
  backend en GitHub, aplicando una estrategia de ramas basada en Git Flow (ramas main,
  develop y ramas de feature por funcionalidad). Se garantizó que todas las integrantes del
  equipo dispusieran de acceso y de un entorno de trabajo colaborativo estable.
- **Configuración del proyecto Android:** se inicializó el proyecto en Android Studio,
  configurando las dependencias principales (Jetpack Compose, Retrofit, Coil, OSMDroid,
  coroutines), los ficheros de construcción (build.gradle) y las variables de entorno necesarias
  para conectar con Supabase.
- **Configuración de Supabase:** se creó el proyecto en la consola de Supabase, se
  diseñaron las tablas iniciales de la base de datos (usuarios, exposiciones, obras, tags e
  interacciones), se configuraron las políticas de seguridad a nivel de fila (Row Level Security)
  y se generaron las claves de API necesarias para la comunicación segura entre cliente y
  servidor.
- **Configuración del entorno de pruebas:** se realizó una carga inicial de datos de prueba
  (usuarios ficticios, exposiciones de ejemplo y obras con imágenes placeholder) para poder
  comenzar el desarrollo de las primeras pantallas con datos reales desde el primer día.

### Desafíos y decisiones relevantes del Sprint 1

Durante este sprint surgieron varias dificultades de carácter técnico y organizativo. La
principal fue la curva de aprendizaje asociada a Jetpack Compose, ya que el equipo
contaba con experiencia previa en Android con Views pero no en el paradigma declarativo.
Se dedicaron varias sesiones de formación interna y consulta de documentación oficial antes
de iniciar el desarrollo de las primeras pantallas.

Asimismo, la configuración de las políticas de Row Level Security en Supabase requirió más
tiempo del previsto, dado que un error en dichas políticas puede bloquear completamente el
acceso a los datos desde el cliente. Este aprendizaje resultó valioso para el resto del
desarrollo.

---

## 5.2 Sprint 2 — Funcionalidades esenciales del MVP

**Duración:** [fecha inicio] – [fecha fin]

Con el entorno de trabajo completamente configurado, el Sprint 2 estuvo dedicado al
desarrollo de las funcionalidades esenciales de la aplicación: aquellas sin las cuales el
producto no puede ser utilizado por un usuario real. El objetivo al final de este sprint era
disponer de una versión de la aplicación mínimamente funcional que pudiese ser sometida
a una primera ronda de pruebas internas.

Las principales tareas desarrolladas fueron:

**Desarrollo del sistema de autenticación:** se implementaron las pantallas y la lógica
de negocio asociadas al registro e inicio de sesión de usuarios. Durante el proceso de
registro, los usuarios deben indicar su rol dentro de la plataforma —interesado o artista—,
pudiendo posteriormente acceder a ambos perfiles desde una misma cuenta mediante sus
credenciales. La autenticación se integró con el módulo Auth de Supabase, que gestiona de
forma segura el almacenamiento de contraseñas y la emisión de tokens de sesión.

**Implementación de la navegación principal:** se integró un menú de navegación inferior
que permite a los usuarios desplazarse de forma sencilla entre las diferentes secciones de
la aplicación. La navegación se adaptó en función del rol activo del usuario, mostrando
opciones específicas para artistas (como la creación de exposiciones) o para interesados
(como el feed de descubrimiento).

**Creación de la pantalla de descubrimiento (swipe):** se desarrolló la estructura inicial
de la interfaz y la mecánica de interacción basada en gestos de deslizamiento (swipe),
utilizando un conjunto reducido de imágenes de prueba para simular el comportamiento del
feed. Esta pantalla constituye el núcleo diferencial de SWART y, por tanto, fue tratada como
la funcionalidad de mayor prioridad del sprint.

**Diseño de la pantalla de detalle de exposiciones:** se creó una vista destinada a mostrar
información relevante sobre cada evento, incluyendo descripción, precio de entrada, fechas
de apertura y cierre, localización y obras destacadas. El diseño se realizó siguiendo las
guías de estilo definidas para la aplicación, con especial atención a la legibilidad y la
jerarquía visual.

**Desarrollo de la primera versión del mapa:** se implementó una visualización geográfica
inicial con marcadores genéricos para representar la ubicación de las diferentes exposiciones
activas, utilizando la librería OSMDroid sobre un tile oscuro tipo CartoDB Dark Matter, en
consonancia con la estética general de la aplicación.

### Desafíos y decisiones relevantes del Sprint 2

La implementación del sistema de swipe presentó retos técnicos significativos en cuanto
a la gestión del estado durante el arrastre (drag state) y la interpolación de colores en
función de la dirección del gesto. Se evaluaron varias aproximaciones antes de optar por
una solución basada en `Modifier.pointerInput` y animaciones de Compose que ofrecía el
mejor equilibrio entre rendimiento y personalización visual.

Por otro lado, la dualidad de roles supuso un desafío de diseño de experiencia de usuario:
era necesario que el sistema de registro propusiese los dos perfiles de forma clara sin
generar la falsa impresión de que se trataba de cuentas separadas e incompatibles. Se
realizaron varias iteraciones de diseño en esta pantalla para minimizar la confusión, aunque,
como reflejarán los resultados del primer experimento, este punto seguía sin estar
completamente resuelto al final del MVP.

---

## 5.3 Sprint 3 — Funcionalidades avanzadas y cierre del primer MVP

**Duración:** [fecha inicio] – [fecha fin]

El Sprint 3 estuvo enfocado en la gestión de contenido por parte de los creadores, la
implementación de la lógica interna del algoritmo de recomendaciones y el refinamiento
general de la experiencia de usuario. Asimismo, durante esta iteración se abordaron mejoras
y tareas pendientes del sprint anterior, entre las que destacan el perfeccionamiento de la
pantalla de descubrimiento, la mejora de la visualización y funcionalidad del mapa, y la
incorporación de la posibilidad de asignar artistas colaboradores durante la creación y
edición de exposiciones.

Las principales tareas desarrolladas fueron:

**Desarrollo del perfil de artista:** se implementó una página específica donde los usuarios
pueden consultar la información detallada del creador: exposiciones realizadas y activas,
obras publicadas y métodos de contacto disponibles. Este perfil actúa como escaparate
digital del artista dentro de la plataforma.

**Creación de herramientas de gestión de exposiciones:** se desarrollaron las interfaces
necesarias para permitir a los autores crear y editar eventos, incluyendo la incorporación de
información relevante (título, descripción, fechas, precio, ubicación) y las obras asociadas.
Paralelamente, se implementó la sección *Mis exposiciones*, destinada a que cada creador
pueda visualizar y gestionar su catálogo propio de forma centralizada.

**Implementación de la pestaña de configuración:** se desarrolló una sección de ajustes
desde la cual el usuario puede modificar la información de su perfil, cerrar sesión, cambiar
el tipo de cuenta activo entre interesado y artista, y gestionar las preferencias de
notificaciones de la aplicación.

**Desarrollo del algoritmo de match:** se integró la lógica subyacente al sistema de
swipe, permitiendo generar recomendaciones personalizadas en función de los tags
asociados a las exposiciones con las que el usuario había interactuado positivamente. Este
algoritmo constituye la base del valor diferencial de SWART frente a plataformas de
descubrimiento cultural genéricas.

**Incorporación de tags de categorización:** se añadieron los tags necesarios para
clasificar tanto las obras como las exposiciones (por disciplina artística, estilo, precio y
franja geográfica), de forma que el algoritmo pudiera operar sobre ellos. Los tags son
asimismo visibles en la pantalla de descubrimiento como elemento de información rápida
para el usuario.

**Diseño de la pestaña de creación y edición de obras:** se desarrolló una interfaz que
permite a los autores añadir nuevas piezas artísticas a su perfil, incorporar imágenes y
completar información complementaria (título, técnica, dimensiones, año).

**Integración de un sistema provisional de compra mediante QR:** se añadió una
funcionalidad temporal que muestra un código QR genérico al pulsar el botón de compra
de entradas, como placeholder que permite validar el flujo de usuario sin implementar aún
una pasarela de pago real.

### Desafíos y decisiones relevantes del Sprint 3

El principal desafío técnico de este sprint fue el diseño del algoritmo de recomendación.
Se evaluaron distintos enfoques (filtrado colaborativo, filtrado basado en contenido e
híbridos), optando finalmente por una solución de filtrado basado en contenido apoyada
en los tags de interacción, por ser la más adecuada para un MVP con un volumen de datos
de usuario aún reducido. Esta decisión se documentó explícitamente en el backlog para
ser revisada en iteraciones futuras a medida que crezca la base de usuarios.

---

## 5.4 Primer experimento

El primer experimento se realizó en el contexto del desarrollo del primer MVP de SWART,
una vez que la aplicación contaba con las funcionalidades básicas operativas: registro con
doble perfil (artista e interesado), pantalla de descubrimiento mediante swipe, mapa de
exposiciones geolocalizado, gestión de exposiciones y chat entre usuarios.

Siguiendo las recomendaciones metodológicas para el primer experimento, se decidió
involucrar a personas muy cercanas al equipo emprendedor: estudiantes y egresados de la
Facultad de Bellas Artes que conocían personalmente a alguno de los miembros del equipo
de desarrollo. Esta proximidad resultaba especialmente adecuada dado el estado preliminar
del producto: aún existían aspectos de UX sin pulir, flujos de onboarding susceptibles de
generar confusión y ausencia de un sistema de pagos completo. Contar con personas de
confianza permitió recibir feedback sincero y detallado sin que el estado imperfecto del
producto supusiese un freno para la participación.

El objetivo del experimento fue validar la propuesta de valor de SWART con usuarios del
segmento objetivo (artistas emergentes e interesados en arte) y detectar los principales
puntos de fricción del primer MVP antes del inicio del segundo ciclo de desarrollo.

### Diseño del experimento

Para llevar a cabo el experimento se diseñó un cuestionario digital distribuido mediante
Microsoft Forms, estructurado en cinco bloques temáticos:

- **Perfil de los participantes:** segmentación de la muestra según el rol desempeñado
  dentro de la plataforma (artista o interesado), rango de edad y disciplina artística o área
  de interés.
- **Hábitos y comportamiento cultural previos:** análisis de las costumbres de consumo
  cultural, frecuencia de asistencia a exposiciones y familiaridad previa con aplicaciones o
  plataformas similares.
- **Valoración del sistema de autenticación:** evaluación de la experiencia relacionada
  con los procesos de registro e inicio de sesión, considerando aspectos como facilidad de
  uso, claridad y accesibilidad.
- **Valoración de las funcionalidades principales:** análisis de la percepción de los
  usuarios sobre las funcionalidades centrales de la aplicación, incluyendo el sistema de
  swipe, el mapa interactivo, la gestión de exposiciones y el sistema de chat.
- **Evaluación global y propuestas de mejora:** valoración general de la experiencia de
  uso junto con la identificación de sugerencias y funcionalidades prioritarias para futuras
  iteraciones del sistema.

El formulario fue enviado de forma directa a través de grupos de WhatsApp y canales de
comunicación del equipo con estudiantes de Bellas Artes, solicitando específicamente
perfiles artistas y personas asiduas visitantes de espacios culturales. El número total de
respuestas recibidas fue de 17.

### Análisis de resultados

#### Perfil de la muestra

Antes de analizar la experiencia con la aplicación, el cuestionario recabó información sobre
el perfil de cada participante para asegurar que la muestra era representativa del público
objetivo de SWART.

> **Tabla 3:** Respuestas a encuesta del primer experimento: ¿Eres artista?

| Respuesta             | Frecuencia | Porcentaje |
|-----------------------|------------|------------|
| Sí                    | 12         | 70,6 %     |
| No (perfil interesado)| 5          | 29,4 %     |
| **Total**             | **17**     | **100,0 %**|

En cuanto a las plataformas usadas actualmente para darse a conocer, Instagram emerge
como la herramienta dominante, apareciendo en el 83,3 % de las respuestas. Le siguen
TikTok y las webs propias. Destaca que algunos participantes declaran no utilizar ninguna
plataforma digital, lo que evidencia una brecha de visibilidad que SWART aspira a cubrir.

Respecto a los interesados, al preguntarles cómo descubren las nuevas exposiciones, los
canales más citados fueron el boca a boca, los carteles físicos y las redes sociales,
frecuentemente en combinación. Este resultado valida uno de los puntos clave de la propuesta
de SWART: la ausencia de una plataforma centralizada y digital hace que el descubrimiento
de exposiciones esté fragmentado y resulte ineficiente para ambos perfiles.

> **Tabla 4:** Respuestas a encuesta del primer experimento: ¿Cuál es el factor que considera
> más decisivo para visitar una exposición?

| Factor                          | Frecuencia | Porcentaje |
|---------------------------------|------------|------------|
| Que me impacte visualmente una obra | 4      | 80,0 %     |
| Que conozca al autor            | 1          | 20,0 %     |
| **Total**                       | **5**      | **100,0 %**|

Este resultado respalda directamente el modelo de descubrimiento basado en swipe de SWART,
cuya mecánica central es precisamente presentar la obra de forma visual e inmediata, antes
de cualquier otro dato textual.

#### Valoración del sistema de autenticación

Para la pantalla de inicio y registro, el 88,2 % de los participantes valoró el proceso con
4 o 5 sobre 5, lo que indica que fue percibido como claro y accesible por la gran mayoría.
No obstante, casi el 30 % tuvo dudas o confusión respecto a la distinción de perfiles.

Las respuestas abiertas apuntan a dos fricciones concretas: la denominación *Interesado*
resultó poco intuitiva para algunos participantes (uno de ellos sugirió usar *Coleccionista*
como alternativa) y la posibilidad de iniciar sesión con ambos roles desde una misma cuenta
no quedaba suficientemente clara. Esto tiene sentido, dado que los botones de la pantalla de
registro sugieren una dicotomía que en la práctica no existe: un mismo usuario puede acceder
a ambos perfiles desde una única cuenta. Este punto se identificó como una mejora prioritaria
para el segundo MVP.

> **Tabla 5:** Respuestas a encuesta del primer experimento: ¿Te parece que el proceso de
> registro es sencillo e intuitivo?

| Puntuación     | Frecuencia | Porcentaje |
|----------------|------------|------------|
| 5 (Máxima)     | 10         | 58,8 %     |
| 4              | 5          | 29,4 %     |
| 3              | 1          | 5,9 %      |
| 2              | 1          | 5,9 %      |
| **Total**      | **17**     | **100,0 %**|

> **Tabla 6:** Respuestas a encuesta del primer experimento: ¿La diferenciación entre perfil
> de artista y perfil de interesado te parece clara?

| Respuesta               | Frecuencia | Porcentaje |
|-------------------------|------------|------------|
| Sí, queda claro         | 12         | 70,6 %     |
| Más o menos             | 3          | 17,6 %     |
| No, me genera confusión | 2          | 11,8 %     |
| **Total**               | **17**     | **100,0 %**|

#### Valoración del sistema de swipe

El sistema de swipe generó respuestas más polarizadas que el proceso de registro: el 64,7 %
lo consideró atractivo (puntuaciones de 4 o 5 sobre 5), mientras que el 35,3 % restante
mostró reservas. Las respuestas abiertas revelaron que los participantes más escépticos
echaban en falta una descripción breve de la obra directamente visible en la tarjeta, así
como la posibilidad de ver múltiples fotografías o incluso vídeo. También se detectó
preocupación por la privacidad de los datos personales utilizados en las recomendaciones.

Asimismo, casi la mitad de los participantes no tenía claro si el modelo de recomendación
basado en interacciones era justo. Esto no refleja rechazo al sistema, sino que la propuesta
de valor diferencial del algoritmo no había sido comunicada con suficiente claridad. La mejora
de esta comunicación se identificó como una prioridad explícita para el segundo MVP.

> **Tabla 7:** Respuestas a encuesta del primer experimento: ¿Te parece que el sistema de
> swipe es una forma atractiva de descubrir arte?

| Puntuación     | Frecuencia | Porcentaje |
|----------------|------------|------------|
| 5 (Máxima)     | 9          | 52,9 %     |
| 4              | 2          | 11,8 %     |
| 3              | 4          | 23,5 %     |
| 2              | 2          | 11,8 %     |
| **Total**      | **17**     | **100,0 %**|

> **Tabla 8:** Respuestas a encuesta del primer experimento: ¿Crees que el sistema de
> recomendación basado en interacciones (y no en seguidores) es justo para los artistas
> emergentes?

| Respuesta          | Frecuencia | Porcentaje |
|--------------------|------------|------------|
| Sí, es más justo   | 9          | 52,9 %     |
| No lo tengo claro  | 8          | 47,1 %     |
| **Total**          | **17**     | **100,0 %**|

#### Valoración del mapa

El mapa fue, con diferencia, la funcionalidad mejor valorada. El 88,2 % de los participantes
puntuó con 4 o 5 la utilidad general del mapa, otro 88,2 % valoró positivamente la
personalización de los marcadores según afinidad y un 94,1 % respondió que el mapa le
ayudaría a descubrir exposiciones que de otro modo no encontraría. Estos resultados validan
que la funcionalidad de localización geográfica cubre una necesidad real y relevante del
público objetivo.

Las sugerencias abiertas recogidas incluyeron la incorporación de iconos específicos para
señalar inauguraciones próximas, así como la posibilidad de marcar espacios donde artistas
emergentes puedan celebrar exposiciones futuras, no únicamente aquellos donde ya hay una
en curso. Estas propuestas fueron trasladadas al backlog y priorizadas para el segundo MVP.

> **Tabla 9:** Respuestas a encuesta del primer experimento: ¿Te parece útil poder ver
> exposiciones cercanas en el mapa?

| Puntuación     | Frecuencia | Porcentaje |
|----------------|------------|------------|
| 5 (Máxima)     | 11         | 64,7 %     |
| 4              | 4          | 23,5 %     |
| 3              | 1          | 5,9 %      |
| 2              | 1          | 5,9 %      |
| **Total**      | **17**     | **100,0 %**|

### Conclusiones del primer experimento y decisiones para el segundo MVP

Los resultados del primer experimento permitieron extraer un conjunto claro de prioridades
para el siguiente ciclo de desarrollo:

1. **Rediseño del flujo de onboarding:** simplificar y clarificar la selección de rol en el
   registro, eliminando la apariencia de dicotomía excluyente entre perfiles.
2. **Mejora de las tarjetas de swipe:** incorporar una descripción breve de la obra y
   soporte para múltiples imágenes en cada tarjeta.
3. **Comunicación del algoritmo:** añadir una explicación accesible del funcionamiento del
   sistema de recomendación para generar confianza en el usuario.
4. **Ampliación del mapa:** explorar la incorporación de marcadores diferenciados para
   inauguraciones y espacios disponibles para exposiciones futuras.
5. **Refinamiento general de la UX:** atender los puntos de fricción menores detectados
   en navegación y consistencia visual.

Estas decisiones se trasladaron al backlog del segundo MVP, dando inicio a un nuevo ciclo
de desarrollo orientado a consolidar y mejorar el producto antes del segundo experimento.
