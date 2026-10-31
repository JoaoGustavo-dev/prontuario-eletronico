package com.health.prontuario.controller;

import com.health.prontuario.dto.HistoricoResponse;
import com.health.prontuario.service.HistoricoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/{pacienteId}/historico")
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public HistoricoResponse consultar(@PathVariable Long pacienteId) {
        return service.consultar(pacienteId);
    }
}