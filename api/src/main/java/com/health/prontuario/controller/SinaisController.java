package com.health.prontuario.controller;

import com.health.prontuario.dto.SinaisRequest;
import com.health.prontuario.dto.SinaisResponse;
import com.health.prontuario.service.SinaisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes/{pacienteId}/sinais")
public class SinaisController {

    private final SinaisService service;

    public SinaisController(SinaisService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SinaisResponse> registrar(@PathVariable Long pacienteId,
                                                    @Valid @RequestBody SinaisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrar(pacienteId, request));
    }
}