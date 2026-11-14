package com.health.prontuario.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.health.prontuario.config.SecurityConfig;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.repository.PacienteRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertaVisibilidadeTest {

    private static final Duration LIMITE = Duration.ofSeconds(5);
    private static final String MARCA_PRESSAO = "\"regra\":\"PRESSAO_CRITICA\"";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Long pacienteId;

    @BeforeEach
    void criarPaciente() {
        pacienteId = pacienteRepository.save(
                new Paciente("Teste Alerta", LocalDate.of(1990, 1, 1), "99999999951")).getId();
    }

    private void registrarSinais(int sistolica, int diastolica) throws Exception {
        String corpo = String.format(
                "{\"pressaoSistolica\":%d,\"pressaoDiastolica\":%d,\"frequenciaCardiaca\":70}",
                sistolica, diastolica);
        mockMvc.perform(post("/pacientes/" + pacienteId + "/sinais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().is2xxSuccessful());
    }

    private int contarAlertasDePressao() throws Exception {
        String json = mockMvc.perform(get("/alertas"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.split(java.util.regex.Pattern.quote(MARCA_PRESSAO), -1).length - 1;
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void alertaCriticoFicaVisivelEmMenosDeCincoSegundos() throws Exception {
        int antes = contarAlertasDePressao();

        long inicio = System.nanoTime();
        registrarSinais(190, 130);
        int depois = contarAlertasDePressao();
        Duration decorrido = Duration.ofNanos(System.nanoTime() - inicio);

        assertEquals(antes + 1, depois);
        assertTrue(decorrido.compareTo(LIMITE) < 0, "Demorou " + decorrido.toMillis() + " ms");
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void limiteDaPressaoDisparaNoValorExatoENaoAbaixo() throws Exception {
        int inicial = contarAlertasDePressao();

        registrarSinais(179, 119);
        assertEquals(inicial, contarAlertasDePressao());

        registrarSinais(180, 100);
        assertEquals(inicial + 1, contarAlertasDePressao());
    }

    @Test
    void semCredenciaisNaoEnxergaAlertas() throws Exception {
        mockMvc.perform(get("/alertas")).andExpect(status().isUnauthorized());
    }
}