package com.health.prontuario.dto;

import com.health.prontuario.model.Exame;
import com.health.prontuario.model.RegistroClinico;

import java.time.Instant;

public record RegistroResponse(
        Long id,
        Long pacienteId,
        String tipo,
        String descricao,
        String notaClinica,
        Instant registradoEm) {

    public static RegistroResponse de(RegistroClinico r) {
        String tipo = r instanceof Exame ? "EXAME" : "RECEITA";
        return new RegistroResponse(r.getId(), r.getPaciente().getId(), tipo,
                r.descricao(), r.getNotaClinica(), r.getRegistradoEm());
    }
}