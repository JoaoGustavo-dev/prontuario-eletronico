package com.health.prontuario.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.health.prontuario.config.SecurityConfig;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.repository.PacienteRepository;
import com.health.prontuario.repository.SinaisVitaisRepository;
import com.health.prontuario.service.ExportacaoService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExportacaoControllerTest {

    private static final String CPF_TESTE = "99999999901";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SinaisVitaisRepository sinaisRepository;

    @BeforeEach
    void prepararDados() {
        Paciente paciente = pacienteRepository.save(
                new Paciente("Teste; Exportacao", LocalDate.of(1990, 5, 20), CPF_TESTE));
        sinaisRepository.save(new SinaisVitais(paciente, 120, 80, 70, Instant.parse("2026-01-01T10:00:00Z")));
    }

    private String baixar(String caminho) throws Exception {
        return mockMvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorExportaPacientesComCabecalhoEEscape() throws Exception {
        String csv = baixar("/exportacao/pacientes.csv");

        assertTrue(csv.startsWith(ExportacaoService.CABECALHO_PACIENTES + "\n"));
        assertTrue(csv.contains(CPF_TESTE + ";\"Teste; Exportacao\";1990-05-20\n"));
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void profissionalExportaSinaisComCpfUnidadesETimestamp() throws Exception {
        String csv = baixar("/exportacao/sinais.csv");

        assertTrue(csv.startsWith(ExportacaoService.CABECALHO_SINAIS + "\n"));
        assertTrue(csv.contains(CPF_TESTE + ";120;80;70;2026-01-01T10:00:00Z\n"));
    }

    @Test
    void semCredenciaisRecebe401() throws Exception {
        mockMvc.perform(get("/exportacao/pacientes.csv"))
                .andExpect(status().isUnauthorized());
    }
}