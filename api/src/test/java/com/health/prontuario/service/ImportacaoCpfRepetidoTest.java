package com.health.prontuario.service;

import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ImportacaoCpfRepetidoTest {

    private PacienteRepository pacienteRepository;
    private ImportacaoService service;

    @BeforeEach
    void preparar() {
        pacienteRepository = mock(PacienteRepository.class);
        service = new ImportacaoService(pacienteRepository, mock(SinaisService.class));
    }

    private static String csv(String... linhas) {
        return ExportacaoService.CABECALHO_PACIENTES + "\n" + String.join("\n", linhas) + "\n";
    }

    @Test
    void normal_cpfsDiferentesSaoTodosImportados() {
        ImportacaoResponse r = service.importarPacientes(csv(
                "99999999901;Ana Teste;1990-01-01",
                "99999999902;Bia Teste;1991-02-02"));

        assertEquals(2, r.importadas());
        assertTrue(r.erros().isEmpty());
        verify(pacienteRepository, times(2)).save(any(Paciente.class));
    }

    @Test
    void falha_cpfRepetidoNoMesmoArquivoRejeitaASegundaOcorrencia() {
        ImportacaoResponse r = service.importarPacientes(csv(
                "99999999901;Ana Teste;1990-01-01",
                "99999999901;Ana Outra;1992-03-03"));

        assertEquals(2, r.linhasLidas());
        assertEquals(1, r.importadas());
        assertEquals(1, r.erros().size());
        assertEquals(3, r.erros().get(0).linha());
        assertEquals("CPF repetido neste arquivo.", r.erros().get(0).motivo());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void limite_linhaRejeitadaNaoQueimaOCpfParaUmaLinhaCorrigidaDepois() {
        ImportacaoResponse r = service.importarPacientes(csv(
                "99999999901;Ana Teste;data-ruim",
                "99999999901;Ana Teste;1990-01-01"));

        assertEquals(2, r.linhasLidas());
        assertEquals(1, r.importadas());
        assertEquals(1, r.erros().size());
        assertEquals(2, r.erros().get(0).linha());
    }
}