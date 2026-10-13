package io.github.zevallos.suelos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AashtoTest {

    private static Muestra m(double n10, double n40, double n200, Double ll, Double lp) {
        return new Muestra("T", 100, n10, n40, n200, ll, lp, null, null, null);
    }

    @Test
    void ejemploDasIndiceDeGrupo42() {
        // Das, Fundamentos de Ingeniería Geotécnica: F = 95 %, LL = 60, IP = 40 -> A-7-6(42)
        Aashto.Resultado r = Aashto.clasificar(m(100, 98, 95, 60.0, 20.0));
        assertEquals("A-7-6", r.grupo());
        assertEquals(42, r.indiceGrupo());
    }

    @Test
    void gravaArenosaA1a() {
        Muestra g = new Muestra("T", 60, 40, 20, 8, 20.0, 17.0, null, null, null);
        assertEquals("A-1-a(0) — Excelente a buena como subrasante",
                Aashto.clasificar(g).toString());
    }

    @Test
    void arenaFinaA3() {
        assertEquals("A-3", Aashto.clasificar(m(100, 80, 5, null, null)).grupo());
    }

    @Test
    void a26UsaSoloTerminoDelIp() {
        // 0.01·(30 − 15)·(15 − 10) = 0.75 -> 1
        Aashto.Resultado r = Aashto.clasificar(m(90, 60, 30, 35.0, 20.0));
        assertEquals("A-2-6", r.grupo());
        assertEquals(1, r.indiceGrupo());
    }

    @Test
    void limoA4ConIndiceRedondeado() {
        // 25·0.15 + 0.01·45·(−2) = 2.85 -> 3
        Aashto.Resultado r = Aashto.clasificar(m(100, 90, 60, 30.0, 22.0));
        assertEquals("A-4", r.grupo());
        assertEquals(3, r.indiceGrupo());
    }

    @Test
    void a75cuandoIpEsMenorQueLLMenos30() {
        assertEquals("A-7-5", Aashto.clasificar(m(100, 95, 80, 60.0, 40.0)).grupo());
    }

    @Test
    void indiceNegativoSeTomaComoCero() {
        // F = 36, LL = 20, IP = 2 -> 0.1 + 0.01·21·(−8) = −1.58 -> 0
        Aashto.Resultado r = Aashto.clasificar(m(100, 80, 36, 20.0, 18.0));
        assertEquals("A-4", r.grupo());
        assertEquals(0, r.indiceGrupo());
    }

    @Test
    void granulometriaInconsistenteSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> new Muestra("X", 80, 90, 50, 10, null, null, null, null, null));
    }

    @Test
    void lectorCsvInterpretaNP() {
        Muestra x = LectorCsv.parsear("M-1, 100, 100, 80, 5, NP, NP,,,");
        assertNull(x.limiteLiquido());
        assertEquals("A-3", Aashto.clasificar(x).grupo());
    }
}
