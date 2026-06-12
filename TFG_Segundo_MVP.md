# SEGUNDO MVP — SWART

---

## 6. Segundo MVP

El segundo MVP nació directamente de las conclusiones del primer experimento. Una vez
analizados los resultados de la primera ronda de validación, el equipo priorizó las mejoras
más demandadas por los usuarios y las funcionalidades que habían quedado pendientes o
en estado de placeholder durante los tres sprints anteriores. El objetivo de este MVP era
doble: por un lado, refinar la experiencia de usuario para reducir los puntos de fricción
detectados; por otro, incorporar funcionalidades que elevasen el nivel de completitud del
producto y lo acercasen a una versión comercialmente presentable.

Al igual que en el primer MVP, el punto de partida fue una nueva aplicación de la técnica
MoSCoW, esta vez partiendo del backlog actualizado con las aportaciones del primer
experimento.

> **Figura X:** Diagrama aplicando técnica MoSCoW para el segundo MVP de SWART.

Los requisitos clasificados como *Must Have* para este ciclo fueron aquellos directamente
derivados del feedback recibido: la mejora del flujo de registro y selección de rol, el
enriquecimiento de las tarjetas de swipe y la comunicación del algoritmo de
recomendaciones. Los *Should Have* incluyeron la incorporación del chat entre usuario e
artista, el filtrado avanzado en el mapa y el sistema de estadísticas para el perfil de
artista. Los *Could Have* recogieron funcionalidades más ambiciosas como la integración
de vídeo en las obras o las notificaciones push personalizadas, cuya implementación
quedó supeditada a la disponibilidad de tiempo al final del sprint.

---

## 6.1 Sprint 4 — Refinamiento y ampliación del producto

**Duración:** [fecha inicio] – [fecha fin]

El cuarto sprint (y único del segundo MVP) se centró en tres grandes líneas de trabajo:
la corrección de los puntos de fricción detectados en el experimento 1, la incorporación
de nuevas funcionalidades que completaran la propuesta de valor de SWART y el
enriquecimiento de la capa visual de la aplicación para acercarla a un acabado de
producto final.

Las principales tareas desarrolladas fueron:

**Rediseño del flujo de onboarding y selección de rol:** se revisó por completo la pantalla
de registro para eliminar la apariencia de dicotomía excluyente entre los perfiles de artista
e interesado. Se incorporó una pantalla explicativa que clarifica que ambos perfiles son
accesibles desde una misma cuenta y pueden alternarse en cualquier momento desde la
sección de configuración. Asimismo, se revisó la terminología de la interfaz, sustituyendo
la etiqueta *Interesado* por *Visitante* tras evaluar las sugerencias recogidas en el
experimento anterior.

**Mejora de las tarjetas de descubrimiento:** se enriqueció el componente de tarjeta en
la pantalla de swipe con la incorporación de una descripción breve de la obra visible
directamente en la tarjeta, sin necesidad de navegar al detalle. Además, se añadió soporte
para que cada tarjeta pudiese mostrar un carrusel de hasta cinco imágenes de la misma
obra o exposición, mejorando así la capacidad del usuario para evaluar el contenido antes
de interactuar.

**Comunicación del algoritmo de recomendaciones:** se diseñó e implementó una pantalla
de bienvenida al feed de descubrimiento que explica de forma visual y accesible el
funcionamiento del sistema de recomendación. Se hizo especial hincapié en destacar que
las recomendaciones se basan en las interacciones del propio usuario y no en la popularidad
o el número de seguidores del artista, reforzando así la propuesta de valor diferencial de
SWART para los artistas emergentes.

**Implementación del sistema de chat:** se desarrolló la funcionalidad de mensajería directa
entre usuarios e artistas, accesible tanto desde el perfil del artista como desde la pantalla
de detalle de una exposición. El sistema de chat incluye una bandeja de entrada centralizada
y notificaciones de nuevos mensajes, permitiendo que el interesado pueda contactar con el
creador para obtener información adicional, concertar visitas o gestionar adquisiciones.

**Filtrado avanzado en el mapa:** se incorporaron filtros combinables sobre el mapa de
exposiciones: por disciplina artística, por rango de precios de entrada, por fechas y por
distancia al usuario. Además, se añadió un tipo de marcador diferenciado para señalar
inauguraciones próximas, una de las sugerencias más repetidas en el primer experimento.

**Perfil de artista ampliado con estadísticas:** se añadió a la sección de perfil del artista
un panel de estadísticas que recoge métricas básicas de visibilidad dentro de la plataforma:
número de veces que sus obras han aparecido en el feed, ratio de interacciones positivas
(likes) sobre impresiones totales y número de veces que el perfil ha sido visitado. Este
panel responde a una necesidad expresada por los artistas participantes en el primer
experimento, que demandaban mayor transparencia sobre el alcance de sus contenidos.

**Refinamiento general de la interfaz:** se realizó una pasada transversal de mejoras
de UX sobre el conjunto de la aplicación, corrigiendo inconsistencias en la tipografía,
los espaciados, los estados vacíos de las pantallas (empty states) y los mensajes de error.
Se revisaron también los iconos de la navegación inferior para aumentar su claridad e
introducir etiquetas de texto junto a cada icono.

**Integración de notificaciones push básicas:** se implementó un sistema básico de
notificaciones push para informar al usuario de nuevos mensajes recibidos, exposiciones
próximas a finalizar de artistas que sigue y nuevas publicaciones de artistas con los que
ha interactuado positivamente en el feed.

### Desafíos y decisiones relevantes del Sprint 4

El principal reto de este sprint fue la amplitud del alcance: el backlog derivado del primer
experimento era extenso y el equipo debía priorizar con criterio para no comprometer la
calidad del resultado. Se tomó la decisión de posponer la integración de vídeo en las
fichas de obra, dado que su implementación correcta habría consumido un tiempo
desproporcionado respecto a su impacto inmediato en la experiencia de usuario. Esta
decisión se documentó explícitamente como trabajo futuro.

La implementación del sistema de chat presentó complejidad adicional derivada de la
necesidad de garantizar la actualización en tiempo real de los mensajes. Se utilizó el canal
de suscripciones en tiempo real de Supabase (Realtime), que simplificó considerablemente
la lógica de sincronización.

---

## 6.2 Segundo experimento

El segundo experimento se diseñó con un alcance significativamente más ambicioso que
el primero, tanto en lo relativo al perfil de los participantes como a la profundidad del
análisis. El producto en este punto era más completo, más pulido y más representativo de
la visión final de SWART, lo que permitía involucrar a usuarios menos cercanos al equipo
sin el riesgo de que el estado del producto condicionase negativamente su evaluación.

### Objetivo del experimento

El objetivo del segundo experimento fue doble. Por un lado, validar las mejoras
introducidas en el segundo MVP —especialmente las relativas al onboarding, el sistema
de swipe y el chat— con una muestra de usuarios más amplia y heterogénea. Por otro,
obtener una primera medición del potencial de adopción de SWART entre su público
objetivo más amplio, más allá del entorno inmediato del equipo emprendedor.

### Diseño del experimento

#### Selección de participantes

A diferencia del primer experimento, en el que los participantes eran personas cercanas
al equipo, el segundo experimento buscó deliberadamente una muestra más representativa
y menos sesgada. Los participantes se reclutaron a través de tres canales:

- **Facultades de Bellas Artes y Diseño:** se contactó con delegados de alumnos y
  profesores de varias facultades para difundir el formulario entre estudiantes que no
  conocían personalmente al equipo de desarrollo.
- **Galerías de arte y espacios culturales independientes:** se distribuyó el formulario
  en galerías de arte contemporáneo y centros culturales de la ciudad, alcanzando tanto
  a visitantes habituales como a artistas con presencia expositora activa.
- **Redes sociales:** se realizó una difusión controlada del cuestionario a través de
  perfiles de Instagram relacionados con el arte emergente local.

El tamaño objetivo de la muestra se estableció en un mínimo de 40 respuestas, con el fin
de obtener resultados estadísticamente más robustos que los del primer experimento.

#### Estructura del cuestionario

El cuestionario del segundo experimento mantuvo la estructura general del primero, con
las siguientes modificaciones:

- Se añadieron preguntas específicas sobre las funcionalidades nuevas del segundo MVP
  (chat, filtros del mapa, estadísticas del perfil de artista).
- Se incorporó una escala de intención de uso (likelihood to use) tipo Likert de 5 puntos,
  con el objetivo de obtener una medición más precisa del potencial de adopción.
- Se introdujo una pregunta abierta específica sobre la propuesta de valor del algoritmo
  de recomendaciones, para verificar si la nueva comunicación implementada en el MVP
  había mejorado la comprensión del mismo respecto al primer experimento.
- Se incluyó una sección de disposición a pagar (willingness to pay), relevante para
  evaluar la viabilidad de modelos de monetización futuros.

#### Metodología de análisis

El análisis de los resultados del segundo experimento se realizó con mayor profundidad
que en el primero. Además del análisis descriptivo de frecuencias y porcentajes aplicado
en el primer experimento, se llevaron a cabo:

- **Segmentación por perfil:** comparación sistemática de las respuestas entre el
  subgrupo de artistas y el de interesados, con el fin de detectar divergencias significativas
  en la valoración de las distintas funcionalidades.
- **Análisis de correlaciones:** exploración de posibles relaciones entre variables como
  la frecuencia de asistencia previa a exposiciones y la valoración del sistema de swipe,
  o entre el perfil artista y la valoración de las estadísticas del perfil.
- **Análisis temático de respuestas abiertas:** codificación y agrupación de los
  comentarios cualitativos en categorías temáticas recurrentes, permitiendo identificar
  patrones de feedback más allá de las respuestas cerradas.

### Análisis de resultados

#### Perfil de la muestra

El segundo experimento contó con un total de [N] respuestas, superando el objetivo mínimo
establecido. La distribución por perfil fue más equilibrada que en el primer experimento,
con un mayor peso del perfil interesado, lo que contribuye a una visión más completa de
ambos lados de la plataforma.

> **Tabla X:** Perfil de participantes en el segundo experimento.

| Perfil              | Frecuencia | Porcentaje |
|---------------------|------------|------------|
| Artista             | [n]        | [%]        |
| Interesado/Visitante| [n]        | [%]        |
| **Total**           | **[N]**    | **100,0 %**|

#### Valoración del onboarding rediseñado

La mejora en el flujo de registro y selección de rol tuvo un impacto positivo medible: el
porcentaje de participantes que declaró no tener clara la distinción entre perfiles descendió
respecto al primer experimento. Las respuestas abiertas confirman que la nueva pantalla
explicativa y la revisión de la terminología contribuyeron a reducir la confusión inicial.

> **Tabla X:** ¿La diferenciación entre perfil de artista y perfil visitante te parece clara?
> (Segundo experimento)

| Respuesta               | Frecuencia | Porcentaje |
|-------------------------|------------|------------|
| Sí, queda claro         | [n]        | [%]        |
| Más o menos             | [n]        | [%]        |
| No, me genera confusión | [n]        | [%]        |
| **Total**               | **[N]**    | **100,0 %**|

#### Valoración del sistema de swipe mejorado

La incorporación de la descripción breve y el carrusel de imágenes en las tarjetas de
descubrimiento resultó en una valoración notablemente más positiva del sistema de swipe.
El porcentaje de participantes que lo consideró una forma atractiva de descubrir arte
(puntuaciones de 4 o 5 sobre 5) aumentó respecto al primer experimento.

> **Tabla X:** ¿Te parece que el sistema de swipe es una forma atractiva de descubrir arte?
> (Segundo experimento)

| Puntuación     | Frecuencia | Porcentaje |
|----------------|------------|------------|
| 5 (Máxima)     | [n]        | [%]        |
| 4              | [n]        | [%]        |
| 3              | [n]        | [%]        |
| 2              | [n]        | [%]        |
| 1              | [n]        | [%]        |
| **Total**      | **[N]**    | **100,0 %**|

#### Comprensión del algoritmo de recomendaciones

La nueva pantalla de comunicación del algoritmo logró reducir significativamente la
proporción de usuarios que declaraba no tener claro si el sistema de recomendación era
justo para los artistas emergentes. Este resultado es especialmente relevante, dado que
la propuesta de valor diferencial de SWART reside precisamente en este mecanismo.

> **Tabla X:** ¿Crees que el sistema de recomendación basado en interacciones es justo
> para los artistas emergentes? (Segundo experimento)

| Respuesta          | Frecuencia | Porcentaje |
|--------------------|------------|------------|
| Sí, es más justo   | [n]        | [%]        |
| No lo tengo claro  | [n]        | [%]        |
| **Total**          | **[N]**    | **100,0 %**|

#### Valoración del chat y las nuevas funcionalidades

El sistema de chat fue bien recibido por ambos perfiles, aunque con matices distintos.
Los artistas valoraron especialmente la posibilidad de recibir consultas directas de
potenciales visitantes o compradores, mientras que los interesados destacaron la utilidad
del chat para obtener información adicional antes de desplazarse a una exposición. Los
filtros del mapa también recibieron valoraciones positivas, consolidando el mapa como
la funcionalidad mejor puntuada de la aplicación en ambos experimentos.

#### Intención de uso y disposición a pagar

La escala de intención de uso reveló que [porcentaje]% de los participantes declara que
utilizaría SWART con frecuencia si estuviese disponible en el mercado. En cuanto a la
disposición a pagar, la mayoría de los interesados se mostró dispuesta a asumir el coste
de funcionalidades premium opcionales, mientras que los artistas mostraron mayor
receptividad a modelos de suscripción mensual que ofreciesen mayor visibilidad dentro de
la plataforma.

### Conclusiones del segundo experimento

Los resultados del segundo experimento confirmaron que las mejoras introducidas en el
segundo MVP habían resuelto los principales puntos de fricción detectados en el primero.
La propuesta de valor central de SWART —el descubrimiento visual de arte emergente
mediante swipe combinado con la geolocalización de exposiciones— fue valorada de forma
consistentemente positiva por una muestra más amplia y heterogénea de usuarios.

Las principales líneas de trabajo identificadas para iteraciones futuras son:

1. **Integración de un sistema de pago real:** la funcionalidad de compra de entradas
   mediante QR fue identificada como una carencia relevante por un porcentaje significativo
   de participantes, especialmente entre los artistas, que ven en ella un canal potencial
   de ingresos directos.
2. **Soporte para vídeo en las fichas de obra:** la demanda de contenido en movimiento
   en las tarjetas de swipe se mantiene elevada y se consolida como la mejora más solicitada
   para una tercera iteración.
3. **Funcionalidades de comunidad:** varios participantes sugirieron la incorporación
   de elementos de red social ligeros (seguir a un artista, guardar exposiciones favoritas,
   compartir contenido externo), que podrían aumentar la retención y la frecuencia de uso
   de la aplicación.
4. **Expansión geográfica:** algunos participantes expresaron interés en poder explorar
   exposiciones en otras ciudades, lo que apunta a una futura funcionalidad de búsqueda
   geográfica ampliada más allá de la localización inmediata del usuario.

Estas conclusiones, junto con los aprendizajes acumulados a lo largo de los cuatro sprints
de desarrollo, configuran la base sobre la que se asentaría un tercer ciclo de desarrollo
de haberse dispuesto de más tiempo dentro del período del Trabajo de Fin de Grado.
