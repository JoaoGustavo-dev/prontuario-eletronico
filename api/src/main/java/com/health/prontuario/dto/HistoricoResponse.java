package com.health.prontuario.dto;

import java.util.List;

public record HistoricoResponse(
        PacienteResponse paciente,
        List<RegistroResponse> registros,
        List<SinaisResponse> sinais) {
}