package com.health.prontuario.controller;

import com.health.prontuario.dto.PacienteRequest;
import com.health.prontuario.dto.PacienteResponse;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest request) {
        Paciente criado = service.cadastrar(request);
        return ResponseEntity
                .created(URI.create("/pacientes/" + criado.getId()))
                .body(PacienteResponse.de(criado));
    }
}