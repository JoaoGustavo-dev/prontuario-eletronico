package com.health.prontuario.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CsvUtilTest {

    @Test
    void textoSimplesNaoRecebeAspas() {
        assertEquals("Maria Silva", CsvUtil.escapar("Maria Silva"));
    }

    @Test
    void campoComSeparadorFicaEntreAspas() {
        assertEquals("\"Silva; Maria\"", CsvUtil.escapar("Silva; Maria"));
    }

    @Test
    void aspasInternasSaoDobradas() {
        assertEquals("\"Disse \"\"oi\"\"\"", CsvUtil.escapar("Disse \"oi\""));
    }

    @Test
    void nuloViraCampoVazio() {
        assertEquals("", CsvUtil.escapar(null));
    }

    @Test
    void linhaJuntaCamposComSeparadorEEscapaCadaUm() {
        assertEquals("123;\"a;b\";42;", CsvUtil.linha("123", "a;b", 42, null));
    }
}