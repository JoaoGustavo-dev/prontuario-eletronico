package com.health.prontuario.gerador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.health.prontuario.dto.ImportacaoResponse;
import com.health.prontuario.repository.AlertaRepository;
import com.health.prontuario.service.ImportacaoService;

@SpringBootTest
@Transactional
class GeradorDadosImportacaoTest {

    @Autowired
    private ImportacaoService importacao;

    @Autowired
    private AlertaRepository alertaRepository;

    @Test
    void dadosSinteticosImportamSemErroEGeramOsAlertasEsperados() {
        GeradorDados gerador = new GeradorDados(7L, "9100000", GeradorDados.BASE_PADRAO);
        List<PacienteGerado> pacientes = gerador.pacientes(GeradorDados.QTD_PACIENTES);
        List<LeituraGerada> leituras = gerador.leituras(pacientes, GeradorDados.LEITURAS_POR_PACIENTE);
        int alertasAntes = alertaRepository.listarRecentesPrimeiro().size();

        ImportacaoResponse rp = importacao.importarPacientes(GeradorDados.csvPacientes(pacientes));
        ImportacaoResponse rs = importacao.importarSinais(GeradorDados.csvSinais(leituras));

        assertEquals(20, rp.importadas());
        assertTrue(rp.erros().isEmpty());
        assertEquals(100, rs.importadas());
        assertTrue(rs.erros().isEmpty());

        long esperados = leituras.stream()
                .filter(l -> !GeradorDados.NENHUMA.equals(l.regraEsperada()))
                .count();
        int alertasDepois = alertaRepository.listarRecentesPrimeiro().size();
        assertEquals(esperados, alertasDepois - alertasAntes);
    }
}