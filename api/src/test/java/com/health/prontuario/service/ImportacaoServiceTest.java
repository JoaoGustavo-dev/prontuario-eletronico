package com.health.prontuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.dto.ImportacaoResponse.ErroLinha;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.repository.AlertaRepository;
import com.health.prontuario.repository.PacienteRepository;
import com.health.prontuario.repository.SinaisVitaisRepository;

@SpringBootTest
@Transactional
class ImportacaoServiceTest {

    private static final String CPF_PACIENTE = "99999999931";

    @Autowired
    private ImportacaoService service;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SinaisVitaisRepository sinaisRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    private static String csv(String cabecalho, String... linhas) {
        return cabecalho + "\n" + String.join("\n", linhas) + "\n";
    }

    private static List<Integer> numerosDasLinhas(ImportacaoResponse resposta) {
        return resposta.erros().stream().map(ErroLinha::linha).toList();
    }

    private Paciente salvarPaciente() {
        return pacienteRepository.save(new Paciente("Paciente Teste", LocalDate.of(1990, 1, 1), CPF_PACIENTE));
    }

    @Test
    void importaPacientesValidos() {
        ImportacaoResponse r = service.importarPacientes(csv(ExportacaoService.CABECALHO_PACIENTES,
                "99999999911;Ana Teste;1990-01-01",
                "99999999912;\"Souza; Bia\";1985-12-31"));

        assertEquals(2, r.linhasLidas());
        assertEquals(2, r.importadas());
        assertTrue(r.erros().isEmpty());
        assertTrue(pacienteRepository.existsByCpf("99999999912"));
    }

    @Test
    void rejeitaLinhasInvalidasDePacientesEImportaAsValidas() {
        ImportacaoResponse r = service.importarPacientes(csv(ExportacaoService.CABECALHO_PACIENTES,
                "99999999921;Valido Um;1990-01-01",
                "123;CPF Curto;1990-01-01",
                "99999999922;Data Ruim;01/01/1990",
                "99999999923;Faltando Coluna",
                "99999999921;Duplicado;1991-02-02",
                "99999999924;Valido Dois;1985-12-31"));

        assertEquals(6, r.linhasLidas());
        assertEquals(2, r.importadas());
        assertEquals(List.of(3, 4, 5, 6), numerosDasLinhas(r));
    }

    @Test
    void cabecalhoInvalidoRejeitaOArquivoInteiro() {
        assertThrows(DadosInvalidosException.class,
                () -> service.importarPacientes(csv("cpf;nome", "99999999911;Ana;1990-01-01")));
    }

    @Test
    void arquivoVazioEhRejeitado() {
        assertThrows(DadosInvalidosException.class, () -> service.importarPacientes(""));
    }

    @Test
    void importaSinaisDePacienteExistente() {
        Paciente paciente = salvarPaciente();

        ImportacaoResponse r = service.importarSinais(csv(ExportacaoService.CABECALHO_SINAIS,
                CPF_PACIENTE + ";120;80;70;2026-01-01T10:00:00Z"));

        assertEquals(1, r.importadas());
        assertEquals(1, sinaisRepository.findByPacienteIdOrderByInstanteDesc(paciente.getId()).size());
    }

    @Test
    void rejeitaLinhasInvalidasDeSinaisEAceitaOLimiteDaFrequencia() {
        Paciente paciente = salvarPaciente();

        ImportacaoResponse r = service.importarSinais(csv(ExportacaoService.CABECALHO_SINAIS,
                CPF_PACIENTE + ";120;80;70;2026-01-01T10:00:00Z",
                CPF_PACIENTE + ";80;120;70;2026-01-01T10:01:00Z",
                "99999999999;120;80;70;2026-01-01T10:02:00Z",
                CPF_PACIENTE + ";abc;80;70;2026-01-01T10:03:00Z",
                CPF_PACIENTE + ";120;80;70;ontem",
                CPF_PACIENTE + ";120;80;300;2026-01-01T10:05:00Z"));

        assertEquals(6, r.linhasLidas());
        assertEquals(2, r.importadas());
        assertEquals(List.of(3, 4, 5, 6), numerosDasLinhas(r));
        assertEquals(2, sinaisRepository.findByPacienteIdOrderByInstanteDesc(paciente.getId()).size());
    }

    @Test
    void sinaisImportadosCriticosGeramAlerta() {
        salvarPaciente();
        int antes = alertaRepository.listarRecentesPrimeiro().size();

        service.importarSinais(csv(ExportacaoService.CABECALHO_SINAIS,
                CPF_PACIENTE + ";190;130;80;2026-01-01T10:00:00Z"));

        assertTrue(alertaRepository.listarRecentesPrimeiro().size() > antes);
    }
}