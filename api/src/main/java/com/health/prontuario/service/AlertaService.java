package com.health.prontuario.service;

import com.health.prontuario.dto.AlertaListaResponse;
import com.health.prontuario.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository repository;

    public AlertaService(AlertaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AlertaListaResponse> listar() {
        return repository.listarRecentesPrimeiro().stream()
                .map(AlertaListaResponse::de)
                .toList();
    }
}