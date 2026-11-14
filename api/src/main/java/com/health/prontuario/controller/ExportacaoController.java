package com.health.prontuario.controller;

import com.health.prontuario.service.ExportacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/exportacao")
public class ExportacaoController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ExportacaoService service;

    public ExportacaoController(ExportacaoService service) {
        this.service = service;
    }

    @GetMapping("/pacientes.csv")
    public ResponseEntity<String> pacientes() {
        return csv("pacientes.csv", service.exportarPacientes());
    }

    @GetMapping("/sinais.csv")
    public ResponseEntity<String> sinais() {
        return csv("sinais.csv", service.exportarSinais());
    }

    private ResponseEntity<String> csv(String nomeArquivo, String conteudo) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}