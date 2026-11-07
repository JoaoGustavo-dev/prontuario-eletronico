package com.health.prontuario.exception;

public class CriptografiaException extends RuntimeException {

    public CriptografiaException(String mensagem) {
        super(mensagem);
    }

    public CriptografiaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}