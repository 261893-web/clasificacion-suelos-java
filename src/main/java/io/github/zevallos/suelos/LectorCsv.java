package io.github.zevallos.suelos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee muestras desde un CSV con las columnas:
 * codigo, N4, N10, N40, N200, LL, LP, D10, D30, D60
 *
 * <p>LL y LP pueden escribirse como "NP" o dejarse vacíos. D10/D30/D60 son
 * opcionales. Las líneas que empiezan con '#' se ignoran.</p>
 */
public final class LectorCsv {

    private LectorCsv() {
    }

    public static List<Muestra> leer(Path archivo) throws IOException {
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        List<Muestra> muestras = new ArrayList<>();
        boolean encabezado = true;
        int numero = 0;
        for (String linea : lineas) {
            numero++;
            String l = linea.strip();
            if (l.isEmpty() || l.startsWith("#")) {
                continue;
            }
            if (encabezado) {
                encabezado = false;
                continue;
            }
            try {
                muestras.add(parsear(l));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Línea " + numero + ": " + e.getMessage(), e);
            }
        }
        return muestras;
    }

    static Muestra parsear(String linea) {
        String[] c = linea.split(",", -1);
        if (c.length < 7) {
            throw new IllegalArgumentException("se esperaban al menos 7 columnas, hay " + c.length);
        }
        return new Muestra(
                c[0].strip(),
                Double.parseDouble(c[1].strip()),
                Double.parseDouble(c[2].strip()),
                Double.parseDouble(c[3].strip()),
                Double.parseDouble(c[4].strip()),
                opcional(c, 5),
                opcional(c, 6),
                opcional(c, 7),
                opcional(c, 8),
                opcional(c, 9));
    }

    private static Double opcional(String[] c, int i) {
        if (i >= c.length) {
            return null;
        }
        String v = c[i].strip();
        return v.isEmpty() || v.equalsIgnoreCase("NP") ? null : Double.valueOf(v);
    }
}
