package com.health.prontuario.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.health.prontuario.config.SecurityConfig;
import com.health.prontuario.service.ExportacaoService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImportacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static MockMultipartFile arquivoDePacientes() {
        String conteudo = ExportacaoService.CABECALHO_PACIENTES + "\n"
                + "99999999941;Maria Teste;1985-03-10\n"
                + "123;CPF Curto;1990-01-01\n";
        return new MockMultipartFile("arquivo", "pacientes.csv", "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void profissionalImportaERecebeRelatorioDeLinhas() throws Exception {
        mockMvc.perform(multipart("/importacao/pacientes").file(arquivoDePacientes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linhasLidas").value(2))
                .andExpect(jsonPath("$.importadas").value(1))
                .andExpect(jsonPath("$.erros[0].linha").value(3));
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorNaoPodeImportar() throws Exception {
        mockMvc.perform(multipart("/importacao/pacientes").file(arquivoDePacientes()))
                .andExpect(status().isForbidden());
    }

    @Test
    void semCredenciaisRecebe401() throws Exception {
        mockMvc.perform(multipart("/importacao/pacientes").file(arquivoDePacientes()))
                .andExpect(status().isUnauthorized());
    }
}