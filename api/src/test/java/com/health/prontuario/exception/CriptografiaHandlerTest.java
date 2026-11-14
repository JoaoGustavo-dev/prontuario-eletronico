package com.health.prontuario.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import com.health.prontuario.config.SecurityConfig;
import com.health.prontuario.dto.ErroResposta;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.Receita;
import com.health.prontuario.repository.PacienteRepository;
import com.health.prontuario.repository.RegistroClinicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriptografiaHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private RegistroClinicoRepository registroRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private GlobalExceptionHandler handler;

    @Autowired
    private EntityManager entityManager;

    @Test
    void handlerDevolve500ComMensagemGenericaSemVazarACausa() {
        CriptografiaException ex = new CriptografiaException("detalhe interno secreto");

        ResponseEntity<ErroResposta> resposta = handler.criptografia(ex);

        assertEquals(500, resposta.getStatusCode().value());
        assertEquals(500, resposta.getBody().status());
        assertFalse(resposta.getBody().mensagem().contains("secreto"));
        assertNull(resposta.getBody().campos());
    }

    @Test
    @WithMockUser(roles = SecurityConfig.PROFISSIONAL)
    void notaAdulteradaNoBancoDevolve500Controlado() throws Exception {
        Paciente paciente = pacienteRepository.save(
                new Paciente("Teste Cripto", LocalDate.of(1990, 1, 1), "99999999961"));
        Receita receita = registroRepository.save(
                new Receita(paciente, "nota original", "Dipirona", "1 comprimido"));
        registroRepository.flush();

        jdbc.update("update registros_clinicos set nota_clinica = ? where id = ?",
                "dado-adulterado-que-nao-decifra", receita.getId());
        entityManager.clear();

        String corpo = mockMvc.perform(get("/pacientes/" + paciente.getId() + "/historico"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertFalse(corpo.contains("adulterado"));
    }
}