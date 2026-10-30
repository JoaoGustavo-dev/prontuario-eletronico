package com.health.prontuario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SinaisRequest(
        @NotNull(message = "Pressão sistólica é obrigatória.") Integer pressaoSistolica,
        @NotNull(message = "Pressão diastólica é obrigatória.") Integer pressaoDiastolica,
        @NotNull(message = "Frequência cardíaca é obrigatória.") Integer frequenciaCardiaca,
        Instant instante) {
}