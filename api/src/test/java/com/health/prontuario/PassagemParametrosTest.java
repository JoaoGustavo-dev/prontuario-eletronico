package com.health.prontuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Java passa tudo por valor: para tipos primitivos, copia-se o valor;
 * para objetos, copia-se o valor da referência. Por isso mutar o objeto
 * é visível ao chamador, mas reatribuir o parâmetro não é.
 */
class PassagemParametrosTest {

    static class Caixa {
        int valor;

        Caixa(int valor) {
            this.valor = valor;
        }
    }

    static void mutar(Caixa c) {
        c.valor = 99;
    }

    static void reatribuir(Caixa c) {
        c = new Caixa(99);
    }

    static void alterarPrimitivo(int n) {
        n = 99;
    }

    @Test
    void normal_mutarOObjetoEhVisivelAoChamador() {
        Caixa caixa = new Caixa(1);

        mutar(caixa);

        assertEquals(99, caixa.valor);
    }

    @Test
    void normal_primitivoChegaComoCopia() {
        int numero = 1;

        alterarPrimitivo(numero);

        assertEquals(1, numero);
    }

    @Test
    void limite_reatribuirOParametroNaoAfetaAReferenciaDoChamador() {
        Caixa caixa = new Caixa(1);
        Caixa original = caixa;

        reatribuir(caixa);

        assertEquals(1, caixa.valor);
        assertEquals(original, caixa);
    }

    @Test
    void limite_reatribuirNullNaoAnulaAVariavelDoChamador() {
        Caixa caixa = new Caixa(1);

        anular(caixa);

        assertNotNull(caixa);
    }

    @Test
    void falha_mutarReferenciaNulaLancaNullPointerException() {
        assertThrows(NullPointerException.class, () -> mutar(null));
    }

    static void anular(Caixa c) {
        c = null;
    }
}