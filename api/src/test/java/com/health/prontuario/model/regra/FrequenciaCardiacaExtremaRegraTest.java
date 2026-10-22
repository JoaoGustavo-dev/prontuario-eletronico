package com.health.prontuario.model.regra;

import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FrequenciaCardiacaExtremaRegraTest {

    private final RegraAlerta regra = new FrequenciaCardiacaExtremaRegra();
    private final Paciente paciente =
            new Paciente("João Teste", LocalDate.of(1975, 3, 2), "10987654321");

    private SinaisVitais comFc(int fc) {
        return new SinaisVitais(paciente, 120, 80, fc);
    }

    @Test
    void normal_frequenciaNormalNaoGeraAlerta() {
        assertTrue(regra.avaliar(comFc(75)).isEmpty());
    }

    @Test
    void limite_taquicardiaExatamenteNoLimite() {
        assertTrue(regra.avaliar(comFc(150)).isPresent());
        assertTrue(regra.avaliar(comFc(149)).isEmpty());
    }

    @Test
    void limite_bradicardiaExatamenteNoLimite() {
        assertTrue(regra.avaliar(comFc(40)).isPresent());
        assertTrue(regra.avaliar(comFc(41)).isEmpty());
    }

    @Test
    void falha_sinaisNulosLancamExcecao() {
        assertThrows(DadosInvalidosException.class, () -> regra.avaliar(null));
    }

    @Test
    void falha_limitesInvalidosNaoCriamRegra() {
        assertThrows(DadosInvalidosException.class,
                () -> new FrequenciaCardiacaExtremaRegra(30, 40));
    }
}