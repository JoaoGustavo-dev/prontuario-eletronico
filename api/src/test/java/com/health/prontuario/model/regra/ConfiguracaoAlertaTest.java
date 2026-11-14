package com.health.prontuario.model.regra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.health.prontuario.exception.DadosInvalidosException;

class ConfiguracaoAlertaTest {

    private static final Instant INSTANTE = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void padraoUsaOsLimitesDasRegras() {
        ConfiguracaoAlerta c = ConfiguracaoAlerta.padrao();

        assertEquals(180, c.getLimiteSistolicaMmhg());
        assertEquals(120, c.getLimiteDiastolicaMmhg());
        assertEquals(150, c.getLimiteFcSuperiorBpm());
        assertEquals(40, c.getLimiteFcInferiorBpm());
    }

    @Test
    void aceitaLimitesNosExtremosDaFaixa() {
        ConfiguracaoAlerta minimo = new ConfiguracaoAlerta(1, 1, 2, 1, INSTANTE);
        ConfiguracaoAlerta maximo = new ConfiguracaoAlerta(300, 300, 300, 299, INSTANTE);

        assertEquals(1, minimo.getLimiteSistolicaMmhg());
        assertEquals(300, maximo.getLimiteFcSuperiorBpm());
    }

    @Test
    void rejeitaValorForaDaFaixa() {
        assertThrows(DadosInvalidosException.class, () -> new ConfiguracaoAlerta(0, 120, 150, 40, INSTANTE));
        assertThrows(DadosInvalidosException.class, () -> new ConfiguracaoAlerta(180, 301, 150, 40, INSTANTE));
    }

    @Test
    void rejeitaFcSuperiorMenorOuIgualAInferior() {
        assertThrows(DadosInvalidosException.class, () -> new ConfiguracaoAlerta(180, 120, 40, 40, INSTANTE));
        assertThrows(DadosInvalidosException.class, () -> new ConfiguracaoAlerta(180, 120, 30, 40, INSTANTE));
    }

    @Test
    void rejeitaInstanteNulo() {
        assertThrows(DadosInvalidosException.class, () -> new ConfiguracaoAlerta(180, 120, 150, 40, null));
    }

    @Test
    void equalsEHashCodeComparamPorValor() {
        ConfiguracaoAlerta a = new ConfiguracaoAlerta(180, 120, 150, 40, INSTANTE);
        ConfiguracaoAlerta b = new ConfiguracaoAlerta(180, 120, 150, 40, INSTANTE);
        ConfiguracaoAlerta diferente = new ConfiguracaoAlerta(200, 120, 150, 40, INSTANTE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, diferente);
    }
}