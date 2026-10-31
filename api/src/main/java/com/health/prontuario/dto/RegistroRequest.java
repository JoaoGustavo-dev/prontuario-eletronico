package com.health.prontuario.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
        @NotBlank(message = "Tipo é obrigatório (EXAME ou RECEITA).") String tipo,
        String notaClinica,
        String tipoExame,
        String resultado,
        String medicamento,
        String posologia) {
}