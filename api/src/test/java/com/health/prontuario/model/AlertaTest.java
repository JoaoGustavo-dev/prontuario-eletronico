package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertaTest {

    private final Paciente paciente =
            new Paciente("Ana Teste", LocalDate.of(1990, 5, 10), "99999999901");
    private final SinaisVitais sinais =
            new SinaisVitais(paciente, 190, 100, 70, Instant.parse("2026-11-01T08:00:00Z"));

    @Test
    void normal_construtorGuardaDadosEMarcaOInstanteDeGeracao() {
        Instant antes = Instant.now();

        Alerta alerta = new Alerta(sinais, "PRESSAO_CRITICA", "Pressão alta.");

        assertEquals(sinais, alerta.getSinais());
        assertEquals("PRESSAO_CRITICA", alerta.getRegra());
        assertEquals("Pressão alta.", alerta.getJustificativa());
        assertFalse(alerta.getGeradoEm().isBefore(antes));
    }

    @Test
    void normal_toStringMostraRegraEJustificativa() {
        Alerta alerta = new Alerta(sinais, "PRESSAO_CRITICA", "Pressão alta.");

        assertEquals("Alerta{PRESSAO_CRITICA: Pressão alta.}", alerta.toString());
    }

    @Test
    void limite_regraEJustificativaComUmCaractereSaoAceitas() {
        Alerta alerta = new Alerta(sinais, "R", "J");

        assertEquals("R", alerta.getRegra());
        assertEquals("J", alerta.getJustificativa());
    }

    @Test
    void falha_sinaisNulos() {
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(null, "PRESSAO_CRITICA", "Pressão alta."));
    }

    @Test
    void falha_regraOuJustificativaVaziasOuNulas() {
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(sinais, " ", "Pressão alta."));
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(sinais, null, "Pressão alta."));
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(sinais, "PRESSAO_CRITICA", ""));
        assertThrows(DadosInvalidosException.class,
                () -> new Alerta(sinais, "PRESSAO_CRITICA", null));
    }
}