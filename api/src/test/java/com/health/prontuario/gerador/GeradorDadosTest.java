package com.health.prontuario.gerador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.health.prontuario.csv.CsvUtil;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.model.regra.FrequenciaCardiacaExtremaRegra;
import com.health.prontuario.model.regra.PressaoCriticaRegra;
import com.health.prontuario.service.ExportacaoService;

class GeradorDadosTest {

    private static Paciente pacienteDe(String cpf) {
        return new Paciente("Teste", LocalDate.of(1990, 1, 1), cpf);
    }

    private static List<LeituraGerada> leituras(GeradorDados gerador) {
        return gerador.leituras(gerador.pacientes(GeradorDados.QTD_PACIENTES),
                GeradorDados.LEITURAS_POR_PACIENTE);
    }

    @Test
    void gera20PacientesValidosComCpfsDistintos() {
        List<PacienteGerado> pacientes = new GeradorDados().pacientes(GeradorDados.QTD_PACIENTES);

        assertEquals(20, pacientes.size());
        assertEquals(20L, pacientes.stream().map(PacienteGerado::cpf).distinct().count());
        pacientes.forEach(p -> assertNotNull(new Paciente(p.nome(), p.dataNascimento(), p.cpf())));
    }

    @Test
    void gera100LeiturasAceitasPeloDominio() {
        List<LeituraGerada> leituras = leituras(new GeradorDados());

        assertEquals(100, leituras.size());
        for (LeituraGerada l : leituras) {
            assertNotNull(new SinaisVitais(pacienteDe(l.cpf()), l.pressaoSistolica(),
                    l.pressaoDiastolica(), l.frequenciaCardiaca(), l.instante()));
        }
    }

    @Test
    void mesmaSementeGeraOsMesmosDados() {
        GeradorDados a = new GeradorDados(42L, GeradorDados.PREFIXO_CPF_PADRAO, GeradorDados.BASE_PADRAO);
        GeradorDados b = new GeradorDados(42L, GeradorDados.PREFIXO_CPF_PADRAO, GeradorDados.BASE_PADRAO);

        assertEquals(GeradorDados.csvSinais(leituras(a)), GeradorDados.csvSinais(leituras(b)));
    }

    @Test
    void sementesDiferentesGeramDadosDiferentes() {
        GeradorDados a = new GeradorDados(1L, GeradorDados.PREFIXO_CPF_PADRAO, GeradorDados.BASE_PADRAO);
        GeradorDados b = new GeradorDados(2L, GeradorDados.PREFIXO_CPF_PADRAO, GeradorDados.BASE_PADRAO);

        assertNotEquals(GeradorDados.csvSinais(leituras(a)), GeradorDados.csvSinais(leituras(b)));
    }

    @Test
    void regraEsperadaConcordaComAsRegrasReais() {
        PressaoCriticaRegra pressao = new PressaoCriticaRegra();
        FrequenciaCardiacaExtremaRegra fc = new FrequenciaCardiacaExtremaRegra();
        assertEquals(pressao.nome(), GeradorDados.REGRA_PRESSAO);
        assertEquals(fc.nome(), GeradorDados.REGRA_FC);

        for (LeituraGerada l : leituras(new GeradorDados())) {
            SinaisVitais s = new SinaisVitais(pacienteDe(l.cpf()), l.pressaoSistolica(),
                    l.pressaoDiastolica(), l.frequenciaCardiaca(), l.instante());
            assertEquals(GeradorDados.REGRA_PRESSAO.equals(l.regraEsperada()), pressao.avaliar(s).isPresent());
            assertEquals(GeradorDados.REGRA_FC.equals(l.regraEsperada()), fc.avaliar(s).isPresent());
        }
    }

    @Test
    void geraTodasAsCategoriasDeLeitura() {
        List<LeituraGerada> leituras = leituras(new GeradorDados());

        assertTrue(leituras.stream().anyMatch(l -> GeradorDados.NENHUMA.equals(l.regraEsperada())));
        assertTrue(leituras.stream().anyMatch(l -> GeradorDados.REGRA_PRESSAO.equals(l.regraEsperada())));
        assertTrue(leituras.stream().anyMatch(l -> GeradorDados.REGRA_FC.equals(l.regraEsperada())
                && l.frequenciaCardiaca() >= 150));
        assertTrue(leituras.stream().anyMatch(l -> GeradorDados.REGRA_FC.equals(l.regraEsperada())
                && l.frequenciaCardiaca() <= 40));
    }

    @Test
    void csvDeSinaisTemCabecalhoDoImportadorECincoColunas() {
        List<String> linhas = GeradorDados.csvSinais(leituras(new GeradorDados())).lines().toList();

        assertEquals(101, linhas.size());
        assertEquals(ExportacaoService.CABECALHO_SINAIS, linhas.get(0));
        linhas.stream().skip(1).forEach(l -> assertEquals(5, CsvUtil.separar(l).size()));
    }

    @Test
    void csvDeRegrasEsperadasTemUmaLinhaPorLeituraComNumeroDaLinhaDoSinais() {
        List<String> linhas = GeradorDados.csvRegrasEsperadas(leituras(new GeradorDados())).lines().toList();

        assertEquals(101, linhas.size());
        assertEquals(GeradorDados.CABECALHO_REGRAS, linhas.get(0));
        assertTrue(linhas.get(1).startsWith("2;"));
        assertTrue(linhas.get(100).startsWith("101;"));
        linhas.stream().skip(1).forEach(l -> assertEquals(5, CsvUtil.separar(l).size()));
    }

    @Test
    void quantidadeDePacientesForaDaFaixaLancaExcecao() {
        GeradorDados gerador = new GeradorDados();

        assertThrows(DadosInvalidosException.class, () -> gerador.pacientes(0));
        assertThrows(DadosInvalidosException.class, () -> gerador.pacientes(GeradorDados.MAX_PACIENTES + 1));
    }

    @Test
    void argumentosInvalidosLancamExcecao() {
        GeradorDados gerador = new GeradorDados();
        List<PacienteGerado> um = gerador.pacientes(1);

        assertThrows(DadosInvalidosException.class, () -> gerador.leituras(List.of(), 5));
        assertThrows(DadosInvalidosException.class, () -> gerador.leituras(um, 0));
        assertThrows(DadosInvalidosException.class,
                () -> new GeradorDados(1L, "123", GeradorDados.BASE_PADRAO));
        assertThrows(DadosInvalidosException.class,
                () -> new GeradorDados(1L, GeradorDados.PREFIXO_CPF_PADRAO, (Instant) null));
    }

    @Test
    void limiteMaximoDePacientesAindaGeraCpfDe11Digitos() {
        List<PacienteGerado> pacientes = new GeradorDados().pacientes(GeradorDados.MAX_PACIENTES);

        String ultimo = pacientes.get(pacientes.size() - 1).cpf();
        assertEquals(11, ultimo.length());
        assertEquals("90000009999", ultimo);
    }
}