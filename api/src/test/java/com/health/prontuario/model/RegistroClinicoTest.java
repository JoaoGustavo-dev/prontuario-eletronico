package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroClinicoTest {

    private final Paciente paciente =
            new Paciente("Ana Teste", LocalDate.of(1990, 5, 10), "99999999901");

    @Test
    void normal_polimorfismoCadaSubclasseResponderComSuaDescricao() {
        List<RegistroClinico> registros = List.of(
                new Exame(paciente, "nota", "Hemograma", "Normal"),
                new Receita(paciente, "nota", "Losartana", "1 por dia"));

        assertEquals("Exame Hemograma", registros.get(0).descricao());
        assertEquals("Receita de Losartana", registros.get(1).descricao());
    }

    @Test
    void normal_heranca_subclassesSaoRegistroClinico() {
        RegistroClinico exame = new Exame(paciente, null, "Hemograma", null);
        RegistroClinico receita = new Receita(paciente, null, "Losartana", null);

        assertTrue(exame instanceof Exame);
        assertTrue(receita instanceof Receita);
        assertFalse(exame instanceof Receita);
        assertEquals(paciente, exame.getPaciente());
    }

    @Test
    void normal_construtorDaBaseDefineInstanteEGuardaNota() {
        Instant antes = Instant.now();

        Exame exame = new Exame(paciente, "nota secreta", "Hemograma", "Normal");

        assertEquals("nota secreta", exame.getNotaClinica());
        assertNotNull(exame.getRegistradoEm());
        assertFalse(exame.getRegistradoEm().isBefore(antes));
        assertEquals("Hemograma", exame.getTipoExame());
        assertEquals("Normal", exame.getResultado());
    }

    @Test
    void normal_toStringHerdadoUsaONomeDaSubclasseEADescricao() {
        Exame exame = new Exame(paciente, null, "Hemograma", null);
        Receita receita = new Receita(paciente, null, "Losartana", null);

        assertTrue(exame.toString().startsWith("Exame{"));
        assertTrue(exame.toString().contains("Exame Hemograma"));
        assertTrue(receita.toString().startsWith("Receita{"));
        assertTrue(receita.toString().contains("Receita de Losartana"));
    }

    @Test
    void limite_notaEResultadoPodemSerNulos() {
        Exame exame = new Exame(paciente, null, "Hemograma", null);
        Receita receita = new Receita(paciente, null, "Losartana", null);

        assertNull(exame.getNotaClinica());
        assertNull(exame.getResultado());
        assertNull(receita.getPosologia());
    }

    @Test
    void falha_pacienteNuloNaBase() {
        assertThrows(DadosInvalidosException.class,
                () -> new Exame(null, "nota", "Hemograma", "Normal"));
        assertThrows(DadosInvalidosException.class,
                () -> new Receita(null, "nota", "Losartana", "1 por dia"));
    }

    @Test
    void falha_tipoDoExameEMedicamentoSaoObrigatorios() {
        assertThrows(DadosInvalidosException.class,
                () -> new Exame(paciente, "nota", "  ", "Normal"));
        assertThrows(DadosInvalidosException.class,
                () -> new Exame(paciente, "nota", null, "Normal"));
        assertThrows(DadosInvalidosException.class,
                () -> new Receita(paciente, "nota", "", "1 por dia"));
        assertThrows(DadosInvalidosException.class,
                () -> new Receita(paciente, "nota", null, "1 por dia"));
    }
}