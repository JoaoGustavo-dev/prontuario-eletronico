package com.health.prontuario.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    private static final String ORIGEM_PERMITIDA = "http://localhost:5500";
    private static final String ORIGEM_PROIBIDA = "http://site-malicioso.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDeOrigemPermitidaPassaSemCredenciais() throws Exception {
        mockMvc.perform(options("/pacientes")
                        .header("Origin", ORIGEM_PERMITIDA)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEM_PERMITIDA));
    }

    @Test
    @WithMockUser(roles = SecurityConfig.LEITOR)
    void getDeOrigemPermitidaRecebeCabecalhoCors() throws Exception {
        mockMvc.perform(get("/alertas").header("Origin", ORIGEM_PERMITIDA))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEM_PERMITIDA));
    }

    @Test
    void preflightDeOrigemNaoPermitidaEhRejeitado() throws Exception {
        mockMvc.perform(options("/pacientes")
                        .header("Origin", ORIGEM_PROIBIDA)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void preflightComMetodoNaoPermitidoEhRejeitado() throws Exception {
        mockMvc.perform(options("/pacientes/1")
                        .header("Origin", ORIGEM_PERMITIDA)
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isForbidden());
    }
}