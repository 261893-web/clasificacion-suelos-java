package io.github.zevallos.suelos;

/**
 * Sistema Unificado de Clasificación de Suelos (SUCS / USCS, ASTM D2487).
 *
 * <p>No incluye suelos orgánicos (OL, OH, Pt): para identificarlos hace falta
 * el límite líquido secado al horno, que no forma parte de los datos de entrada.</p>
 */
public final class Sucs {

    public record Resultado(String simbolo, String descripcion) {
        @Override
        public String toString() {
            return simbolo + " — " + descripcion;
        }
    }

    private Sucs() {
    }

    /** Línea A de la carta de plasticidad de Casagrande: IP = 0.73·(LL − 20). */
    static double lineaA(double ll) {
        return 0.73 * (ll - 20);
    }

    public static Resultado clasificar(Muestra m) {
        return m.pasaN200() >= 50 ? clasificarFino(m) : clasificarGrueso(m);
    }

    // ---------------------------------------------------------------- finos

    /** Símbolo de la fracción fina según la carta de plasticidad. */
    static String simboloFino(Muestra m) {
        double ll = m.ll();
        double ip = m.indicePlasticidad();
        boolean sobreLineaA = !m.esNoPlastico() && ip >= lineaA(ll);

        if (ll >= 50) {
            return sobreLineaA ? "CH" : "MH";
        }
        if (sobreLineaA && ip > 7) {
            return "CL";
        }
        if (sobreLineaA && ip >= 4) {
            return "CL-ML";
        }
        return "ML";
    }

    private static Resultado clasificarFino(Muestra m) {
        String s = simboloFino(m);
        String desc = switch (s) {
            case "CH" -> "Arcilla de alta plasticidad";
            case "MH" -> "Limo elástico";
            case "CL" -> "Arcilla de baja plasticidad";
            case "CL-ML" -> "Arcilla limosa";
            default -> "Limo";
        };
        return new Resultado(s, desc + complementoGrueso(m));
    }

    private static String complementoGrueso(Muestra m) {
        double grueso = 100 - m.pasaN200();
        if (grueso < 15) {
            return "";
        }
        String tipo = m.porcentajeArena() >= m.porcentajeGrava() ? "arena" : "grava";
        return grueso < 30 ? " con " + tipo : (tipo.equals("arena") ? " arenosa" : " gravosa");
    }

    // ------------------------------------------------------------- gruesos

    private static Resultado clasificarGrueso(Muestra m) {
        boolean esGrava = m.porcentajeGrava() > m.porcentajeArena();
        String base = esGrava ? "G" : "S";
        String nombre = esGrava ? "Grava" : "Arena";
        double finos = m.pasaN200();

        if (finos > 12) {
            return clasificarConFinos(m, base, nombre);
        }

        boolean bienGraduado = bienGraduado(m, esGrava);
        String grad = bienGraduado ? "W" : "P";
        String descGrad = bienGraduado ? " bien graduada" : " mal graduada";

        if (finos < 5) {
            return new Resultado(base + grad, nombre + descGrad);
        }

        // 5 % a 12 % de finos: símbolo doble
        String fino = simboloFino(m);
        boolean arcilloso = fino.startsWith("C");
        String segundo = base + (arcilloso ? "C" : "M");
        String desc = nombre + descGrad + (arcilloso ? " con arcilla" : " con limo");
        return new Resultado(base + grad + "-" + segundo, desc);
    }

    private static Resultado clasificarConFinos(Muestra m, String base, String nombre) {
        String fino = simboloFino(m);
        return switch (fino) {
            case "CL", "CH" -> new Resultado(base + "C", nombre + " arcillosa");
            case "CL-ML" -> new Resultado(base + "C-" + base + "M", nombre + " limo-arcillosa");
            default -> new Resultado(base + "M", nombre + " limosa");
        };
    }

    private static boolean bienGraduado(Muestra m, boolean esGrava) {
        if (!m.tieneCurvaGranulometrica()) {
            throw new IllegalArgumentException(m.codigo()
                    + ": con menos de 12 % de finos se necesitan D10, D30 y D60");
        }
        double cuMin = esGrava ? 4 : 6;
        double cc = m.cc();
        return m.cu() >= cuMin && cc >= 1 && cc <= 3;
    }
}
