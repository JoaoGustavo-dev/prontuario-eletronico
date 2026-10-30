package com.health.prontuario.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException(Long id) {
        super("Paciente " + id + " não encontrado.");
    }
}