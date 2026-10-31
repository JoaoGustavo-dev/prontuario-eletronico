package com.health.prontuario.dto;

import com.health.prontuario.model.Alerta;

import java.time.Instant;

public record AlertaListaResponse(
        Long id,
        String regra,
        String justificativa,
        Instant geradoEm,
        Long pacienteId,
        String pacienteNome) {

    public static AlertaListaResponse de(Alerta a) {
        return new AlertaListaResponse(
                a.getId(), a.getRegra(), a.getJustificativa(), a.getGeradoEm(),
                a.getSinais().getPaciente().getId(),
                a.getSinais().getPaciente().getNome());
    }
}