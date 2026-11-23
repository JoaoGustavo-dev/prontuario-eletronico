package com.health.prontuario.service;

import com.health.prontuario.csv.CsvUtil;
import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.dto.ImportacaoResponse.ErroLinha;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.repository.PacienteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class ImportacaoService {

    private static final int COLUNAS_PACIENTES = 3;
    private static final int COLUNAS_SINAIS = 5;

    private final PacienteRepository pacienteRepository;
    private final SinaisService sinaisService;

    public ImportacaoService(PacienteRepository pacienteRepository, SinaisService sinaisService) {
        this.pacienteRepository = pacienteRepository;
        this.sinaisService = sinaisService;
    }

    public ImportacaoResponse importarPacientes(String csv) {
        return processar(csv, ExportacaoService.CABECALHO_PACIENTES,
                COLUNAS_PACIENTES, this::importarLinhaPaciente);
    }

    public ImportacaoResponse importarSinais(String csv) {
        return processar(csv, ExportacaoService.CABECALHO_SINAIS,
                COLUNAS_SINAIS, this::importarLinhaSinais);
    }

    public ImportacaoResponse importarPacientes(File arquivo) {
        return importarPacientes(lerArquivo(arquivo));
    }

    public ImportacaoResponse importarSinais(File arquivo) {
        return importarSinais(lerArquivo(arquivo));
    }

    private ImportacaoResponse processar(String csv, String cabecalhoEsperado,
                                         int colunasEsperadas, Consumer<List<String>> tratador) {
        List<String> linhas = csv.lines().toList();
        if (linhas.isEmpty()) {
            throw new DadosInvalidosException("Arquivo vazio.");
        }

        String cabecalho = linhas.get(0).replace("\uFEFF", "").trim();
        if (!cabecalho.equals(cabecalhoEsperado)) {
            throw new DadosInvalidosException("Cabeçalho inválido. Esperado: " + cabecalhoEsperado);
        }

        int lidas = 0;
        int importadas = 0;
        List<ErroLinha> erros = new ArrayList<>();

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) {
                continue;
            }
            lidas++;
            int numeroLinha = i + 1;
            try {
                List<String> campos = CsvUtil.separar(linha);
                if (campos.size() != colunasEsperadas) {
                    throw new DadosInvalidosException("Esperadas " + colunasEsperadas
                            + " colunas, encontradas " + campos.size() + ".");
                }
                tratador.accept(campos);
                importadas++;
            } catch (DadosInvalidosException e) {
                erros.add(new ErroLinha(numeroLinha, e.getMessage()));
            } catch (DataIntegrityViolationException e) {
                erros.add(new ErroLinha(numeroLinha, "Violação de integridade no banco (registro duplicado?)."));
            }
        }
        return new ImportacaoResponse(lidas, importadas, erros);
    }

    private void importarLinhaPaciente(List<String> c) {
        String cpf = c.get(0).trim();
        String nome = c.get(1);
        LocalDate nascimento;
        try {
            nascimento = LocalDate.parse(c.get(2).trim());
        } catch (DateTimeParseException e) {
            throw new DadosInvalidosException("Data de nascimento deve estar no formato aaaa-MM-dd.");
        }

        Paciente paciente = new Paciente(nome, nascimento, cpf);
        if (pacienteRepository.existsByCpf(cpf)) {
            throw new DadosInvalidosException("CPF já cadastrado.");
        }
        pacienteRepository.save(paciente);
    }

    private void importarLinhaSinais(List<String> c) {
        String cpf = c.get(0).trim();
        int sistolica = inteiro(c.get(1), "pressao_sistolica_mmhg");
        int diastolica = inteiro(c.get(2), "pressao_diastolica_mmhg");
        int frequencia = inteiro(c.get(3), "frequencia_cardiaca_bpm");
        Instant instante;
        try {
            instante = Instant.parse(c.get(4).trim());
        } catch (DateTimeParseException e) {
            throw new DadosInvalidosException("instante_utc deve estar em ISO-8601 UTC (ex.: 2026-01-01T10:00:00Z).");
        }

        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new DadosInvalidosException("Nenhum paciente cadastrado com este CPF."));

        SinaisVitais leitura = new SinaisVitais(paciente, sistolica, diastolica, frequencia, instante);
        sinaisService.registrarLeitura(leitura);
    }

    private static int inteiro(String texto, String coluna) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("Valor inválido na coluna " + coluna + ".");
        }
    }

    private static String lerArquivo(File arquivo) {
        if (arquivo == null || !arquivo.isFile()) {
            throw new DadosInvalidosException(
                    "Arquivo não encontrado ou não é um arquivo regular. Confira o caminho e tente novamente.");
        }
        if (!arquivo.canRead()) {
            throw new DadosInvalidosException(
                    "Sem permissão para ler o arquivo " + arquivo.getName() + ".");
        }
        try {
            return Files.readString(arquivo.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DadosInvalidosException(
                    "Não foi possível ler " + arquivo.getName() + " como texto UTF-8.");
        }
    }
}