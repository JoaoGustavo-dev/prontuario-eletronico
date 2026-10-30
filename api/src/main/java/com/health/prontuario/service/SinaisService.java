package com.health.prontuario.service;

import com.health.prontuario.dto.AlertaResponse;
import com.health.prontuario.dto.SinaisRequest;
import com.health.prontuario.dto.SinaisResponse;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.model.regra.RegraAlerta;
import com.health.prontuario.repository.AlertaRepository;
import com.health.prontuario.repository.SinaisVitaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SinaisService {

    private final PacienteService pacienteService;
    private final SinaisVitaisRepository sinaisRepository;
    private final AlertaRepository alertaRepository;
    private final List<RegraAlerta> regras;

    public SinaisService(PacienteService pacienteService,
                         SinaisVitaisRepository sinaisRepository,
                         AlertaRepository alertaRepository,
                         List<RegraAlerta> regras) {
        this.pacienteService = pacienteService;
        this.sinaisRepository = sinaisRepository;
        this.alertaRepository = alertaRepository;
        this.regras = regras;
    }

    @Transactional
    public SinaisResponse registrar(Long pacienteId, SinaisRequest request) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);

        SinaisVitais novo = request.instante() == null
                ? new SinaisVitais(paciente, request.pressaoSistolica(),
                request.pressaoDiastolica(), request.frequenciaCardiaca())
                : new SinaisVitais(paciente, request.pressaoSistolica(),
                request.pressaoDiastolica(), request.frequenciaCardiaca(),
                request.instante());
        SinaisVitais salvo = sinaisRepository.save(novo);

        List<AlertaResponse> alertas = regras.stream()
                .map(regra -> regra.avaliar(salvo))
                .flatMap(java.util.Optional::stream)
                .map(alertaRepository::save)
                .map(AlertaResponse::de)
                .toList();

        return SinaisResponse.de(salvo, alertas);
    }
}