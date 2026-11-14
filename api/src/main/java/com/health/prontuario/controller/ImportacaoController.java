package com.health.prontuario.controller;

import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.service.ImportacaoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/importacao")
public class ImportacaoController {

    private final ImportacaoService service;

    public ImportacaoController(ImportacaoService service) {
        this.service = service;
    }

    @PostMapping("/pacientes")
    public ImportacaoResponse pacientes(@RequestParam("arquivo") MultipartFile arquivo) {
        return service.importarPacientes(ler(arquivo));
    }

    @PostMapping("/sinais")
    public ImportacaoResponse sinais(@RequestParam("arquivo") MultipartFile arquivo) {
        return service.importarSinais(ler(arquivo));
    }

    private String ler(MultipartFile arquivo) {
        try {
            return new String(arquivo.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DadosInvalidosException("Não foi possível ler o arquivo enviado.");
        }
    }
}