package com.health.prontuario.exception;

public class ArquivoConfiguracaoException extends RuntimeException {

    public ArquivoConfiguracaoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoConfiguracaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}