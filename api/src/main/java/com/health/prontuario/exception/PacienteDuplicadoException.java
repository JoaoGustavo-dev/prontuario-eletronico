package com.health.prontuario.exception;

public class PacienteDuplicadoException extends RuntimeException {
    public PacienteDuplicadoException(String mensagem) {
        super(mensagem);
    }
}