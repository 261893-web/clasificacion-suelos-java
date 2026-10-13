# Clasificación de suelos — SUCS y AASHTO (Java)

Librería y programa de consola en **Java 17** que clasifica muestras de suelo con los dos sistemas que se usan en el Perú para obras viales y de edificación:

- **SUCS** (Sistema Unificado, ASTM D2487): GW, SP-SM, CL, MH, etc.
- **AASHTO M 145**: grupos A-1 a A-7 con su **índice de grupo**.

En laboratorio esto se hace a mano con la carta de plasticidad y la tabla AASHTO, y es fácil equivocarse en los casos de borde (símbolos dobles, zona CL-ML, A-7-5 vs A-7-6). La idea fue escribir las reglas una sola vez, probarlas con casos conocidos y poder clasificar un lote completo de ensayos desde un CSV.

## Ejemplo

```bash
mvn package
java -jar target/clasificacion-suelos.jar ejemplos/muestras.csv
```

```
Muestra    %Gr   %Ar    %F    IP  SUCS     Descripción                        AASHTO
------------------------------------------------------------------------------------------
C-01         0    12    88    26  CL       Arcilla de baja plasticidad        A-7-6(24)
C-02         8    61    31    15  SC       Arena arcillosa                    A-2-6(1)
C-03        62    34     4    NP  GW       Grava bien graduada                A-1-a(0)
C-04         5    86     9    NP  SP-SM    Arena mal graduada con limo        A-3(0)
C-05         0     7    93    37  CH       Arcilla de alta plasticidad        A-7-5(41)
C-06        26    60    14     5  SC-SM    Arena limo-arcillosa               A-1-b(0)
C-07         0    95     5    NP  SP-SM    Arena mal graduada con limo        A-3(0)
C-08         0    28    72    19  MH       Limo elástico con arena            A-7-5(16)
C-09        12    78    10    17  SW-SC    Arena bien graduada con arcilla    A-2-6(0)
C-10         4    56    40     4  SM       Arena limosa                       A-4(0)
```

*(Los datos de `ejemplos/muestras.csv` son ilustrativos, no ensayos reales.)*

También se puede usar como librería:

```java
Muestra m = new Muestra("M-1", 100, 98, 95, 85, 45.0, 20.0, null, null, null);
Sucs.clasificar(m);    // CL — Arcilla de baja plasticidad con arena
Aashto.clasificar(m);  // A-7-6(...)
```

## Formato del CSV

```
codigo,N4,N10,N40,N200,LL,LP,D10,D30,D60
```

- `N4 … N200`: % que pasa cada tamiz.
- `LL`, `LP`: límites de Atterberg; vacío o `NP` si el suelo es no plástico.
- `D10`, `D30`, `D60` [mm]: solo necesarios en suelos gruesos con menos de 12 % de finos (para Cu y Cc).

## Diseño

| Clase | Responsabilidad |
|---|---|
| `Muestra` (record) | Datos de entrada, validación (granulometría decreciente, LP ≤ LL) y propiedades derivadas: IP, % grava, % arena, Cu, Cc |
| `Sucs` | Carta de plasticidad (línea A), gradación y símbolos dobles |
| `Aashto` | Grupos evaluados de izquierda a derecha según la norma e índice de grupo |
| `LectorCsv` | Lectura del archivo con mensajes de error que indican la línea |
| `App` | Programa de consola |

Las clases de clasificación no tienen estado y no dependen del formato de entrada, así que se pueden reutilizar desde una interfaz gráfica o un servicio web sin cambiar nada.

## Pruebas

23 pruebas con **JUnit 5** (`mvn test`), entre ellas:

- El ejemplo del libro de Das (F = 95 %, LL = 60, IP = 40 → **A-7-6(42)**).
- Cada rama de la carta de plasticidad: CL, CH, ML, MH y la zona CL-ML.
- Gradación: Cu y Cc dentro y fuera de los límites para gravas y arenas.
- Símbolos dobles con 5–12 % de finos (SP-SM, SW-SC).
- Índice de grupo parcial para A-2-6, redondeo y valores negativos.
- Validación de datos de entrada inconsistentes.

## Limitaciones

- No clasifica suelos orgánicos (OL, OH, Pt): requieren el LL secado al horno.
- Las descripciones son simplificadas respecto a los nombres completos de la ASTM D2487 (por ejemplo, no distingue "con grava" en todos los casos).

## Referencias

- ASTM D2487 — *Standard Practice for Classification of Soils for Engineering Purposes (USCS)*.
- AASHTO M 145 — *Classification of Soils and Soil-Aggregate Mixtures for Highway Construction Purposes*.
- Das, B. M. *Fundamentos de Ingeniería Geotécnica*. Cengage.
