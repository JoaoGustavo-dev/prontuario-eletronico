package com.health.prontuario.crypto;

import com.health.prontuario.model.Exame;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.RegistroClinico;
import com.health.prontuario.repository.PacienteRepository;
import com.health.prontuario.repository.RegistroClinicoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class NotaClinicaPersistenciaTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private RegistroClinicoRepository registroRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private Paciente paciente;

    @BeforeEach
    void criarPaciente() {
        String cpf = String.format("%011d", ThreadLocalRandom.current().nextLong(100_000_000_000L));
        paciente = pacienteRepository.saveAndFlush(
                new Paciente("Paciente Teste Cripto", LocalDate.of(1990, 1, 1), cpf));
    }

    private Long salvarExame(String nota) {
        Exame exame = new Exame(paciente, nota, "ECG", "Ritmo sinusal");
        Long id = registroRepository.saveAndFlush(exame).getId();
        entityManager.clear();
        return id;
    }

    private String lerColunaCrua(Long id) {
        return jdbc.queryForObject(
                "select nota_clinica from registros_clinicos where id = ?", String.class, id);
    }


    @Test
    void bancoGuardaTextoCifradoELeituraDevolveOOriginal() {
        String nota = "Paciente refere dor toracica. Conduta: ECG.";
        Long id = salvarExame(nota);

        String crua = lerColunaCrua(id);
        assertNotEquals(nota, crua);
        assertFalse(crua.contains("dor toracica"));

        RegistroClinico lido = registroRepository.findById(id).orElseThrow();
        assertEquals(nota, lido.getNotaClinica());
    }

    @Test
    void mesmaNotaGravadaDuasVezesGeraValoresDiferentesNoBanco() {
        String nota = "mesma nota";
        Long id1 = salvarExame(nota);
        Long id2 = salvarExame(nota);

        assertNotEquals(lerColunaCrua(id1), lerColunaCrua(id2));
    }


    @Test
    void notaNulaContinuaNulaNoBanco() {
        Long id = salvarExame(null);

        assertNull(lerColunaCrua(id));
        assertNull(registroRepository.findById(id).orElseThrow().getNotaClinica());
    }

    @Test
    void notaDeDoisMilCaracteresEhGravadaELida() {
        String nota = "a".repeat(2000);
        Long id = salvarExame(nota);

        assertEquals(nota, registroRepository.findById(id).orElseThrow().getNotaClinica());
    }


    @Test
    void valorAdulteradoNoBancoFalhaNaLeitura() {
        Long id = salvarExame("nota original");

        jdbc.update("update registros_clinicos set nota_clinica = ? where id = ?",
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", id);
        entityManager.clear();

        assertThrows(RuntimeException.class, () -> registroRepository.findById(id));
    }
}