package com.health.prontuario.controller;

import com.health.prontuario.dto.RegistroRequest;
import com.health.prontuario.dto.RegistroResponse;
import com.health.prontuario.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes/{pacienteId}/registros")
public class RegistroController {

    private final RegistroService service;

    public RegistroController(RegistroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RegistroResponse> registrar(@PathVariable Long pacienteId,
                                                      @Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrar(pacienteId, request));
    }
}