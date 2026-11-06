package com.health.prontuario.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String CORPO_INVALIDO = "{}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void semAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/alertas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorPodeLer() throws Exception {
        mockMvc.perform(get("/alertas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorNaoPodeCadastrarPaciente() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_INVALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorNaoPodeCriarRegistro() throws Exception {
        mockMvc.perform(post("/pacientes/1/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_INVALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void leitorNaoPodeEnviarSinais() throws Exception {
        mockMvc.perform(post("/pacientes/1/sinais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_INVALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void profissionalPassaPelaSegurancaAoEscrever() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_INVALIDO))
                .andExpect(status().isBadRequest());
    }
}