package com.health.prontuario.gerador;

import java.time.LocalDate;

public record PacienteGerado(String cpf, String nome, LocalDate dataNascimento) {
}