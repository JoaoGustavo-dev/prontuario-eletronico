package com.health.prontuario.dto;

import com.health.prontuario.model.Alerta;

import java.time.Instant;

public record AlertaResponse(Long id, String regra, String justificativa, Instant geradoEm) {

    public static AlertaResponse de(Alerta a) {
        return new AlertaResponse(a.getId(), a.getRegra(), a.getJustificativa(), a.getGeradoEm());
    }
}