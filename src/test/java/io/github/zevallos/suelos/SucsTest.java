package io.github.zevallos.suelos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SucsTest {

    /** Atajo para crear muestras en los tests. LL/LP = null -> NP. */
    private static Muestra m(double n4, double n10, double n40, double n200,
                             Double ll, Double lp, Double d10, Double d30, Double d60) {
        return new Muestra("T", n4, n10, n40, n200, ll, lp, d10, d30, d60);
    }

    private static String sucs(Muestra m) {
        return Sucs.clasificar(m).simbolo();
    }

    @Test
    void arenaBienGraduada() {
        // Cu = 12, Cc = 1.33
        assertEquals("SW", sucs(m(90, 85, 50, 3, null, null, 0.1, 0.4, 1.2)));
    }

    @Test
    void arenaMalGraduadaPorCuBajo() {
        // Cu = 2.5 < 6
        assertEquals("SP", sucs(m(90, 85, 50, 3, null, null, 0.2, 0.3, 0.5)));
    }

    @Test
    void gravaBienGraduada() {
        // 65 % grava, Cu = 20, Cc = 1.8
        assertEquals("GW", sucs(m(35, 28, 15, 2, null, null, 0.5, 3.0, 10.0)));
    }

    @Test
    void gravaMalGraduadaPorCcFueraDeRango() {
        // Cu = 20 pero Cc = 0.2
        assertEquals("GP", sucs(m(35, 28, 15, 2, null, null, 0.5, 1.0, 10.0)));
    }

    @Test
    void arenaArcillosa() {
        // IP = 17 sobre la línea A (10.95)
        assertEquals("SC", sucs(m(95, 90, 70, 30, 35.0, 18.0, null, null, null)));
    }

    @Test
    void arenaLimosa() {
        // IP = 3 < 4
        assertEquals("SM", sucs(m(95, 90, 70, 25, 30.0, 27.0, null, null, null)));
    }

    @Test
    void arenaLimoArcillosaEnZonaCLML() {
        // IP = 6 entre 4 y 7, sobre la línea A
        assertEquals("SC-SM", sucs(m(95, 90, 70, 25, 25.0, 19.0, null, null, null)));
    }

    @Test
    void simboloDobleConFinosLimosos() {
        assertEquals("SP-SM", sucs(m(92, 88, 60, 8, null, null, 0.08, 0.15, 0.3)));
    }

    @Test
    void simboloDobleConFinosArcillosos() {
        assertEquals("SW-SC", sucs(m(90, 85, 50, 10, 40.0, 20.0, 0.07, 0.3, 1.1)));
    }

    @Test
    void arcillaBajaPlasticidad() {
        Sucs.Resultado r = Sucs.clasificar(m(100, 98, 95, 85, 45.0, 20.0, null, null, null));
        assertEquals("CL", r.simbolo());
        assertEquals("Arcilla de baja plasticidad con arena", r.descripcion());
    }

    @Test
    void arcillaAltaPlasticidad() {
        // IP = 40 sobre la línea A (36.5)
        assertEquals("CH", sucs(m(100, 100, 98, 90, 70.0, 30.0, null, null, null)));
    }

    @Test
    void limoElastico() {
        // IP = 20 bajo la línea A (29.2)
        assertEquals("MH", sucs(m(100, 100, 98, 90, 60.0, 40.0, null, null, null)));
    }

    @Test
    void limoNoPlastico() {
        assertEquals("ML", sucs(m(100, 99, 90, 60, null, null, null, null, null)));
    }

    @Test
    void faltaCurvaGranulometrica() {
        Muestra sinDiametros = m(90, 85, 50, 3, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> Sucs.clasificar(sinDiametros));
    }
}
