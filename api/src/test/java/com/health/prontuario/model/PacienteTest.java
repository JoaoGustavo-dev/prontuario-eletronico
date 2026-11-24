package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 10);

    @Test
    void normal_construtorGuardaDadosENormalizaONome() {
        Paciente p = new Paciente("  Ana Teste  ", NASCIMENTO, "99999999901");

        assertEquals("Ana Teste", p.getNome());
        assertEquals(NASCIMENTO, p.getDataNascimento());
        assertEquals("99999999901", p.getCpf());
    }

    @Test
    void normal_equalsEHashCodeComparamPeloCpf() {
        Paciente a = new Paciente("Ana Teste", NASCIMENTO, "99999999901");
        Paciente b = new Paciente("Outro Nome", LocalDate.of(1980, 1, 1), "99999999901");
        Paciente c = new Paciente("Ana Teste", NASCIMENTO, "99999999902");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void normal_setNaoDuplicaPacientesComMesmoCpf() {
        Set<Paciente> conjunto = new HashSet<>();
        conjunto.add(new Paciente("Ana", NASCIMENTO, "99999999901"));
        conjunto.add(new Paciente("Ana Repetida", NASCIMENTO, "99999999901"));

        assertEquals(1, conjunto.size());
    }

    @Test
    void normal_toStringMostraIdENomeSemExporCpf() {
        Paciente p = new Paciente("Ana Teste", NASCIMENTO, "99999999901");

        String texto = p.toString();

        assertTrue(texto.contains("Ana Teste"));
        assertFalse(texto.contains("99999999901"));
    }

    @Test
    void limite_equalsComNuloEOutroTipoEhFalso() {
        Paciente p = new Paciente("Ana Teste", NASCIMENTO, "99999999901");

        assertNotEquals(null, p);
        assertNotEquals("99999999901", p);
        assertEquals(p, p);
    }

    @Test
    void limite_nascimentoHojeEhAceito() {
        Paciente p = new Paciente("Ana Teste", LocalDate.now(), "99999999901");

        assertEquals(LocalDate.now(), p.getDataNascimento());
    }

    @Test
    void limite_cpfComDezDigitosEOnzeDigitosMaisUm() {
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", NASCIMENTO, "9999999990"));
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", NASCIMENTO, "999999999011"));
    }

    @Test
    void falha_nomeVazioOuNulo() {
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("   ", NASCIMENTO, "99999999901"));
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente(null, NASCIMENTO, "99999999901"));
    }

    @Test
    void falha_nascimentoNuloOuFuturo() {
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", null, "99999999901"));
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", LocalDate.now().plusDays(1), "99999999901"));
    }

    @Test
    void falha_cpfNuloOuComLetras() {
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", NASCIMENTO, null));
        assertThrows(DadosInvalidosException.class,
                () -> new Paciente("Ana", NASCIMENTO, "9999999990a"));
    }
}