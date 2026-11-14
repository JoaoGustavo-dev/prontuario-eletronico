package com.health.prontuario.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.health.prontuario.exception.DadosInvalidosException;

class CsvUtilSepararTest {

    @Test
    void separaCamposSimples() {
        assertEquals(List.of("a", "b", "c"), CsvUtil.separar("a;b;c"));
    }

    @Test
    void campoEntreAspasPodeConterSeparador() {
        assertEquals(List.of("123", "Silva; Maria", "x"), CsvUtil.separar("123;\"Silva; Maria\";x"));
    }

    @Test
    void aspasDobradasViramUmaAspas() {
        assertEquals(List.of("Disse \"oi\""), CsvUtil.separar("\"Disse \"\"oi\"\"\""));
    }

    @Test
    void camposVaziosSaoPreservados() {
        assertEquals(List.of("a", "", ""), CsvUtil.separar("a;;"));
    }

    @Test
    void aspasNaoFechadasLancamExcecao() {
        assertThrows(DadosInvalidosException.class, () -> CsvUtil.separar("a;\"sem fim"));
    }

    @Test
    void separarDesfazOQueLinhaFaz() {
        String linha = CsvUtil.linha("99", "Nome; com \"aspas\"", 7);
        assertEquals(List.of("99", "Nome; com \"aspas\"", "7"), CsvUtil.separar(linha));
    }
}