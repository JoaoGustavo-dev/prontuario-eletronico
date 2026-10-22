package com.health.prontuario.dto;

import com.health.prontuario.model.Paciente;

import java.time.LocalDate;

public record PacienteResponse(Long id, String nome, LocalDate dataNascimento, String cpf) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getDataNascimento(), p.getCpf());
    }
}