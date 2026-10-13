package io.github.zevallos.suelos;

import java.nio.file.Path;
import java.util.List;

/** Programa de consola: clasifica todas las muestras de un archivo CSV. */
public final class App {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java -jar clasificacion-suelos.jar <muestras.csv>");
            System.exit(1);
        }
        List<Muestra> muestras = LectorCsv.leer(Path.of(args[0]));

        String formato = "%-8s %5s %5s %5s %5s  %-8s %-34s %-10s%n";
        System.out.printf(formato, "Muestra", "%Gr", "%Ar", "%F", "IP", "SUCS", "Descripción", "AASHTO");
        System.out.println("-".repeat(90));
        for (Muestra m : muestras) {
            try {
                Sucs.Resultado s = Sucs.clasificar(m);
                Aashto.Resultado a = Aashto.clasificar(m);
                System.out.printf(formato, m.codigo(),
                        entero(m.porcentajeGrava()), entero(m.porcentajeArena()), entero(m.pasaN200()),
                        m.esNoPlastico() ? "NP" : entero(m.indicePlasticidad()),
                        s.simbolo(), s.descripcion(), a.grupo() + "(" + a.indiceGrupo() + ")");
            } catch (IllegalArgumentException e) {
                System.out.printf("%-8s  no clasificada: %s%n", m.codigo(), e.getMessage());
            }
        }
    }

    private static String entero(double v) {
        return String.valueOf(Math.round(v));
    }
}
