package com.health.prontuario.dto;

import com.health.prontuario.model.SinaisVitais;

import java.time.Instant;
import java.util.List;

public record SinaisResponse(
        Long id,
        Long pacienteId,
        int pressaoSistolica,
        int pressaoDiastolica,
        int frequenciaCardiaca,
        Instant instante,
        List<AlertaResponse> alertas) {

    public static SinaisResponse de(SinaisVitais s, List<AlertaResponse> alertas) {
        return new SinaisResponse(s.getId(), s.getPaciente().getId(),
                s.getPressaoSistolica(), s.getPressaoDiastolica(),
                s.getFrequenciaCardiaca(), s.getInstante(), alertas);
    }
}