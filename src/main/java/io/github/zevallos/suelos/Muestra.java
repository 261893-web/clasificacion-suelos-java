package io.github.zevallos.suelos;

/**
 * Datos de laboratorio de una muestra de suelo.
 *
 * <p>Los porcentajes son "% que pasa" cada tamiz. Los límites de Atterberg
 * se dejan en {@code null} cuando el suelo es no plástico (NP). Los diámetros
 * D10, D30 y D60 [mm] solo hacen falta para suelos gruesos con pocos finos.</p>
 */
public record Muestra(
        String codigo,
        double pasaN4,
        double pasaN10,
        double pasaN40,
        double pasaN200,
        Double limiteLiquido,
        Double limitePlastico,
        Double d10,
        Double d30,
        Double d60) {

    public Muestra {
        validarPorcentaje("N°4", pasaN4);
        validarPorcentaje("N°10", pasaN10);
        validarPorcentaje("N°40", pasaN40);
        validarPorcentaje("N°200", pasaN200);
        if (pasaN4 < pasaN10 || pasaN10 < pasaN40 || pasaN40 < pasaN200) {
            throw new IllegalArgumentException(codigo
                    + ": la granulometría debe ser decreciente (N°4 ≥ N°10 ≥ N°40 ≥ N°200)");
        }
        if ((limiteLiquido == null) != (limitePlastico == null)) {
            throw new IllegalArgumentException(codigo
                    + ": indica LL y LP juntos, o ambos vacíos si el suelo es NP");
        }
        if (limiteLiquido != null && limitePlastico > limiteLiquido) {
            throw new IllegalArgumentException(codigo + ": LP no puede ser mayor que LL");
        }
    }

    private static void validarPorcentaje(String tamiz, double valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException("% que pasa el tamiz " + tamiz + " fuera de rango: " + valor);
        }
    }

    public boolean esNoPlastico() {
        return limiteLiquido == null || indicePlasticidad() == 0;
    }

    public double ll() {
        return limiteLiquido == null ? 0 : limiteLiquido;
    }

    public double indicePlasticidad() {
        return limiteLiquido == null ? 0 : limiteLiquido - limitePlastico;
    }

    public double porcentajeGrava() {
        return 100 - pasaN4;
    }

    public double porcentajeArena() {
        return pasaN4 - pasaN200;
    }

    public boolean tieneCurvaGranulometrica() {
        return d10 != null && d30 != null && d60 != null;
    }

    /** Coeficiente de uniformidad Cu = D60 / D10. */
    public double cu() {
        return d60 / d10;
    }

    /** Coeficiente de curvatura Cc = D30² / (D10·D60). */
    public double cc() {
        return d30 * d30 / (d10 * d60);
    }
}
