package com.health.prontuario.model.regra;

import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PressaoCriticaRegraTest {

    private final RegraAlerta regra = new PressaoCriticaRegra();
    private final Paciente paciente =
            new Paciente("Maria Teste", LocalDate.of(1980, 5, 10), "12345678901");

    private SinaisVitais sinais(int sistolica, int diastolica) {
        return new SinaisVitais(paciente, sistolica, diastolica, 80);
    }

    @Test
    void normal_pressaoNormalNaoGeraAlerta() {
        assertTrue(regra.avaliar(sinais(120, 80)).isEmpty());
    }

    @Test
    void limite_sistolicaExatamenteNoLimiteGeraAlerta() {
        assertTrue(regra.avaliar(sinais(180, 90)).isPresent());
    }

    @Test
    void limite_umAbaixoDoLimiteNaoGeraAlerta() {
        assertTrue(regra.avaliar(sinais(179, 119)).isEmpty());
    }

    @Test
    void limite_diastolicaNoLimiteGeraAlerta() {
        assertTrue(regra.avaliar(sinais(150, 120)).isPresent());
    }

    @Test
    void falha_sinaisNulosLancamExcecao() {
        assertThrows(DadosInvalidosException.class, () -> regra.avaliar(null));
    }

    @Test
    void falha_valorImplausivelNaoCriaSinais() {
        assertThrows(DadosInvalidosException.class, () -> sinais(-5, 80));
    }
}