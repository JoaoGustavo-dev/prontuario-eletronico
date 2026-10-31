package com.health.prontuario.service;

import com.health.prontuario.dto.AlertaResponse;
import com.health.prontuario.dto.HistoricoResponse;
import com.health.prontuario.dto.PacienteResponse;
import com.health.prontuario.dto.RegistroResponse;
import com.health.prontuario.dto.SinaisResponse;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.repository.AlertaRepository;
import com.health.prontuario.repository.RegistroClinicoRepository;
import com.health.prontuario.repository.SinaisVitaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HistoricoService {

    private final PacienteService pacienteService;
    private final RegistroClinicoRepository registroRepository;
    private final SinaisVitaisRepository sinaisRepository;
    private final AlertaRepository alertaRepository;

    public HistoricoService(PacienteService pacienteService,
                            RegistroClinicoRepository registroRepository,
                            SinaisVitaisRepository sinaisRepository,
                            AlertaRepository alertaRepository) {
        this.pacienteService = pacienteService;
        this.registroRepository = registroRepository;
        this.sinaisRepository = sinaisRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public HistoricoResponse consultar(Long pacienteId) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);

        Map<Long, List<AlertaResponse>> alertasPorSinais =
                alertaRepository.listarPorPaciente(pacienteId).stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getSinais().getId(),
                                Collectors.mapping(AlertaResponse::de, Collectors.toList())));

        List<SinaisResponse> sinais = sinaisRepository
                .findByPacienteIdOrderByInstanteDesc(pacienteId).stream()
                .map(s -> SinaisResponse.de(s, alertasPorSinais.getOrDefault(s.getId(), List.of())))
                .toList();

        List<RegistroResponse> registros = registroRepository
                .findByPacienteIdOrderByRegistradoEmDesc(pacienteId).stream()
                .map(RegistroResponse::de)
                .toList();

        return new HistoricoResponse(PacienteResponse.de(paciente), registros, sinais);
    }
}