package com.health.prontuario.service;

import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportacaoArquivoTest {

    @TempDir
    Path pasta;

    private PacienteRepository pacienteRepository;
    private SinaisService sinaisService;
    private ImportacaoService service;

    @BeforeEach
    void preparar() {
        pacienteRepository = mock(PacienteRepository.class);
        sinaisService = mock(SinaisService.class);
        service = new ImportacaoService(pacienteRepository, sinaisService);
    }

    private File arquivo(String nome, String conteudo) throws IOException {
        Path destino = pasta.resolve(nome);
        Files.writeString(destino, conteudo, StandardCharsets.UTF_8);
        return destino.toFile();
    }

    @Test
    void normal_importaPacientesDeArquivoValido() throws IOException {
        File f = arquivo("pacientes.csv",
                ExportacaoService.CABECALHO_PACIENTES + "\n99999999901;Fulano Teste;1990-05-10\n");
        when(pacienteRepository.existsByCpf("99999999901")).thenReturn(false);

        ImportacaoResponse r = service.importarPacientes(f);

        assertEquals(1, r.linhasLidas());
        assertEquals(1, r.importadas());
        assertTrue(r.erros().isEmpty());
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void normal_importaSinaisDeArquivoValido() throws IOException {
        File f = arquivo("sinais.csv",
                ExportacaoService.CABECALHO_SINAIS + "\n99999999901;120;80;70;2026-01-01T10:00:00Z\n");
        Paciente p = new Paciente("Fulano Teste", LocalDate.of(1990, 5, 10), "99999999901");
        when(pacienteRepository.findByCpf("99999999901")).thenReturn(Optional.of(p));

        ImportacaoResponse r = service.importarSinais(f);

        assertEquals(1, r.importadas());
        verify(sinaisService).registrarLeitura(any(SinaisVitais.class));
    }

    @Test
    void limite_arquivoVazioGeraErroDeDadosInvalidos() throws IOException {
        File f = arquivo("vazio.csv", "");

        DadosInvalidosException e = assertThrows(DadosInvalidosException.class,
                () -> service.importarPacientes(f));

        assertEquals("Arquivo vazio.", e.getMessage());
    }

    @Test
    void falha_caminhoInexistente() {
        File f = pasta.resolve("nao-existe.csv").toFile();

        assertThrows(DadosInvalidosException.class, () -> service.importarPacientes(f));
    }

    @Test
    void falha_caminhoEhDiretorio() {
        File diretorio = pasta.toFile();

        assertThrows(DadosInvalidosException.class, () -> service.importarSinais(diretorio));
    }
}