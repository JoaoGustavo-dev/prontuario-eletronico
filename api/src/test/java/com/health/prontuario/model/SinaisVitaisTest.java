package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinaisVitaisTest {

    private final Paciente paciente =
            new Paciente("Ana Teste", LocalDate.of(1990, 5, 10), "99999999901");
    private final Instant instante = Instant.parse("2026-11-01T08:00:00Z");

    @Test
    void normal_construtorDeCincoParametrosGuardaOInstanteInformado() {
        SinaisVitais s = new SinaisVitais(paciente, 120, 80, 70, instante);

        assertEquals(120, s.getPressaoSistolica());
        assertEquals(80, s.getPressaoDiastolica());
        assertEquals(70, s.getFrequenciaCardiaca());
        assertEquals(instante, s.getInstante());
        assertEquals(paciente, s.getPaciente());
    }

    @Test
    void normal_construtorSobrecarregadoDeQuatroParametrosUsaOMomentoAtual() {
        Instant antes = Instant.now();

        SinaisVitais s = new SinaisVitais(paciente, 120, 80, 70);

        assertFalse(s.getInstante().isBefore(antes));
        assertFalse(s.getInstante().isAfter(Instant.now()));
    }

    @Test
    void normal_metodoEstaticoPressaoPlausivelNaoPrecisaDeInstancia() {
        assertTrue(SinaisVitais.pressaoPlausivel(120, 80));
        assertFalse(SinaisVitais.pressaoPlausivel(80, 120));
    }

    @Test
    void normal_toStringMostraValoresEUnidades() {
        SinaisVitais s = new SinaisVitais(paciente, 120, 80, 70, instante);

        String texto = s.toString();

        assertTrue(texto.contains("120/80 mmHg"));
        assertTrue(texto.contains("70 bpm"));
    }

    @Test
    void limite_pressaoNasFronteirasDaFaixa() {
        assertTrue(SinaisVitais.pressaoPlausivel(40, 20));
        assertTrue(SinaisVitais.pressaoPlausivel(300, 200));
        assertFalse(SinaisVitais.pressaoPlausivel(39, 20));
        assertFalse(SinaisVitais.pressaoPlausivel(301, 200));
        assertFalse(SinaisVitais.pressaoPlausivel(100, 19));
        assertFalse(SinaisVitais.pressaoPlausivel(250, 201));
    }

    @Test
    void limite_sistolicaIgualADiastolicaNaoEPlausivel() {
        assertFalse(SinaisVitais.pressaoPlausivel(100, 100));
    }

    @Test
    void limite_frequenciaNasFronteirasDaFaixa() {
        assertEquals(20, new SinaisVitais(paciente, 120, 80, 20, instante).getFrequenciaCardiaca());
        assertEquals(300, new SinaisVitais(paciente, 120, 80, 300, instante).getFrequenciaCardiaca());
    }

    @Test
    void falha_frequenciaForaDaFaixa() {
        assertThrows(DadosInvalidosException.class,
                () -> new SinaisVitais(paciente, 120, 80, 19, instante));
        assertThrows(DadosInvalidosException.class,
                () -> new SinaisVitais(paciente, 120, 80, 301, instante));
    }

    @Test
    void falha_pressaoImplausivelNoConstrutor() {
        assertThrows(DadosInvalidosException.class,
                () -> new SinaisVitais(paciente, 80, 120, 70, instante));
    }

    @Test
    void falha_pacienteOuInstanteNulos() {
        assertThrows(DadosInvalidosException.class,
                () -> new SinaisVitais(null, 120, 80, 70, instante));
        assertThrows(DadosInvalidosException.class,
                () -> new SinaisVitais(paciente, 120, 80, 70, null));
    }
}