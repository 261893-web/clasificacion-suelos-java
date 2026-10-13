package io.github.zevallos.suelos;

/**
 * Clasificación AASHTO M 145 para suelos de subrasante.
 *
 * <p>Los grupos se evalúan de izquierda a derecha en la tabla de la norma;
 * el primero que cumple todas las condiciones es la clasificación.</p>
 */
public final class Aashto {

    public record Resultado(String grupo, int indiceGrupo, String calificacion) {
        @Override
        public String toString() {
            return grupo + "(" + indiceGrupo + ") — " + calificacion;
        }
    }

    private Aashto() {
    }

    public static Resultado clasificar(Muestra m) {
        String grupo = grupo(m);
        int ig = indiceDeGrupo(m, grupo);
        return new Resultado(grupo, ig, calificacion(grupo));
    }

    static String grupo(Muestra m) {
        double f = m.pasaN200();
        double ll = m.ll();
        double ip = m.indicePlasticidad();

        if (f <= 35) {
            if (m.pasaN10() <= 50 && m.pasaN40() <= 30 && f <= 15 && ip <= 6) {
                return "A-1-a";
            }
            if (m.pasaN40() <= 50 && f <= 25 && ip <= 6) {
                return "A-1-b";
            }
            if (m.pasaN40() >= 51 && f <= 10 && m.esNoPlastico()) {
                return "A-3";
            }
            if (ip <= 10) {
                return ll <= 40 ? "A-2-4" : "A-2-5";
            }
            return ll <= 40 ? "A-2-6" : "A-2-7";
        }

        if (ip <= 10) {
            return ll <= 40 ? "A-4" : "A-5";
        }
        if (ll <= 40) {
            return "A-6";
        }
        return ip <= ll - 30 ? "A-7-5" : "A-7-6";
    }

    /**
     * Índice de grupo:
     * IG = (F − 35)·[0.2 + 0.005·(LL − 40)] + 0.01·(F − 15)·(IP − 10).
     * Para A-2-6 y A-2-7 solo se usa el término del IP. Se redondea al entero
     * más cercano y los valores negativos se toman como cero.
     */
    static int indiceDeGrupo(Muestra m, String grupo) {
        if (grupo.startsWith("A-1") || grupo.equals("A-3")
                || grupo.equals("A-2-4") || grupo.equals("A-2-5")) {
            return 0;
        }
        double f = m.pasaN200();
        double ll = m.ll();
        double ip = m.indicePlasticidad();
        double terminoIp = 0.01 * (f - 15) * (ip - 10);
        double ig = grupo.startsWith("A-2")
                ? terminoIp
                : (f - 35) * (0.2 + 0.005 * (ll - 40)) + terminoIp;
        return (int) Math.max(0, Math.round(ig));
    }

    static String calificacion(String grupo) {
        return switch (grupo) {
            case "A-1-a", "A-1-b", "A-3", "A-2-4", "A-2-5", "A-2-6", "A-2-7" ->
                    "Excelente a buena como subrasante";
            default -> "Regular a mala como subrasante";
        };
    }
}
