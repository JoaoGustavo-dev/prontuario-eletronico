package com.health.prontuario.gerador;

import com.health.prontuario.csv.CsvUtil;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.regra.FrequenciaCardiacaExtremaRegra;
import com.health.prontuario.model.regra.PressaoCriticaRegra;
import com.health.prontuario.service.ExportacaoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeradorDados {

    public static final int QTD_PACIENTES = 20;
    public static final int LEITURAS_POR_PACIENTE = 5;
    public static final int MAX_PACIENTES = 9999;

    public static final long SEMENTE_PADRAO = 42L;
    public static final String PREFIXO_CPF_PADRAO = "9000000";
    public static final Instant BASE_PADRAO = Instant.parse("2026-11-01T08:00:00Z");

    public static final String REGRA_PRESSAO = "PRESSAO_CRITICA";
    public static final String REGRA_FC = "FREQUENCIA_CARDIACA_EXTREMA";
    public static final String NENHUMA = "NENHUMA";

    public static final String CABECALHO_REGRAS =
            "linha;cpf;instante_utc;regra_esperada;criterio_disparo";

    private static final String[] NOMES = {
            "Ana", "Bruno", "Carla", "Diego", "Elisa", "Fabio", "Gabriela", "Heitor",
            "Isabela", "Joao", "Karina", "Lucas", "Marina", "Nicolas", "Olivia", "Paulo"};
    private static final String[] SOBRENOMES = {
            "Almeida", "Barbosa", "Cardoso", "Dias", "Ferreira", "Gomes", "Lima", "Martins",
            "Nogueira", "Oliveira", "Pereira", "Ribeiro", "Santos", "Teixeira", "Vieira"};

    private final Random random;
    private final String prefixoCpf;
    private final Instant base;

    public GeradorDados() {
        this(SEMENTE_PADRAO, PREFIXO_CPF_PADRAO, BASE_PADRAO);
    }

    public GeradorDados(long semente, String prefixoCpf, Instant base) {
        if (prefixoCpf == null || !prefixoCpf.matches("\\d{7}")) {
            throw new DadosInvalidosException("Prefixo de CPF deve ter 7 dígitos numéricos.");
        }
        if (base == null) {
            throw new DadosInvalidosException("Instante base é obrigatório.");
        }
        this.random = new Random(semente);
        this.prefixoCpf = prefixoCpf;
        this.base = base;
    }

    public List<PacienteGerado> pacientes(int quantidade) {
        if (quantidade < 1 || quantidade > MAX_PACIENTES) {
            throw new DadosInvalidosException("Quantidade de pacientes deve estar entre 1 e " + MAX_PACIENTES + ".");
        }
        List<PacienteGerado> lista = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            String cpf = prefixoCpf + String.format("%04d", i);
            String nome = NOMES[random.nextInt(NOMES.length)] + " "
                    + SOBRENOMES[random.nextInt(SOBRENOMES.length)] + " "
                    + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
            LocalDate nascimento = LocalDate.of(1940, 1, 1).plusDays(random.nextInt(65 * 365));
            lista.add(new PacienteGerado(cpf, nome, nascimento));
        }
        return lista;
    }

    public List<LeituraGerada> leituras(List<PacienteGerado> pacientes, int porPaciente) {
        if (pacientes == null || pacientes.isEmpty()) {
            throw new DadosInvalidosException("Lista de pacientes não pode ser vazia.");
        }
        if (porPaciente < 1) {
            throw new DadosInvalidosException("Leituras por paciente deve ser pelo menos 1.");
        }
        List<LeituraGerada> lista = new ArrayList<>();
        for (int rodada = 0; rodada < porPaciente; rodada++) {
            for (int j = 0; j < pacientes.size(); j++) {
                Instant instante = base.plus(Duration.ofHours(6L * rodada)).plus(Duration.ofSeconds(j));
                lista.add(gerarLeitura(pacientes.get(j).cpf(), instante));
            }
        }
        return lista;
    }

    private LeituraGerada gerarLeitura(String cpf, Instant instante) {
        int sorteio = random.nextInt(100);
        if (sorteio < 70) {
            return new LeituraGerada(cpf, entre(100, 139), entre(60, 89), entre(55, 100), instante, NENHUMA);
        }
        if (sorteio < 82) {
            return new LeituraGerada(cpf, entre(PressaoCriticaRegra.SISTOLICA_PADRAO_MMHG, 220),
                    entre(100, 130), entre(55, 100), instante, REGRA_PRESSAO);
        }
        if (sorteio < 91) {
            return new LeituraGerada(cpf, entre(100, 139), entre(60, 89),
                    entre(FrequenciaCardiacaExtremaRegra.LIMITE_SUPERIOR_PADRAO_BPM, 190), instante, REGRA_FC);
        }
        return new LeituraGerada(cpf, entre(100, 139), entre(60, 89),
                entre(30, FrequenciaCardiacaExtremaRegra.LIMITE_INFERIOR_PADRAO_BPM), instante, REGRA_FC);
    }

    private int entre(int minimo, int maximo) {
        return minimo + random.nextInt(maximo - minimo + 1);
    }

    public static String csvPacientes(List<PacienteGerado> pacientes) {
        StringBuilder sb = new StringBuilder(ExportacaoService.CABECALHO_PACIENTES).append('\n');
        for (PacienteGerado p : pacientes) {
            sb.append(CsvUtil.linha(p.cpf(), p.nome(), p.dataNascimento())).append('\n');
        }
        return sb.toString();
    }

    public static String csvSinais(List<LeituraGerada> leituras) {
        StringBuilder sb = new StringBuilder(ExportacaoService.CABECALHO_SINAIS).append('\n');
        for (LeituraGerada l : leituras) {
            sb.append(CsvUtil.linha(l.cpf(), l.pressaoSistolica(), l.pressaoDiastolica(),
                    l.frequenciaCardiaca(), l.instante())).append('\n');
        }
        return sb.toString();
    }

    public static String csvRegrasEsperadas(List<LeituraGerada> leituras) {
        StringBuilder sb = new StringBuilder(CABECALHO_REGRAS).append('\n');
        for (int i = 0; i < leituras.size(); i++) {
            LeituraGerada l = leituras.get(i);
            sb.append(CsvUtil.linha(i + 2, l.cpf(), l.instante(), l.regraEsperada(), criterio(l))).append('\n');
        }
        return sb.toString();
    }

    private static String criterio(LeituraGerada l) {
        if (REGRA_PRESSAO.equals(l.regraEsperada())) {
            return String.format("sistolica >= %d mmHg ou diastolica >= %d mmHg",
                    PressaoCriticaRegra.SISTOLICA_PADRAO_MMHG, PressaoCriticaRegra.DIASTOLICA_PADRAO_MMHG);
        }
        if (REGRA_FC.equals(l.regraEsperada())) {
            if (l.frequenciaCardiaca() >= FrequenciaCardiacaExtremaRegra.LIMITE_SUPERIOR_PADRAO_BPM) {
                return String.format("fc >= %d bpm (taquicardia)",
                        FrequenciaCardiacaExtremaRegra.LIMITE_SUPERIOR_PADRAO_BPM);
            }
            return String.format("fc <= %d bpm (bradicardia)",
                    FrequenciaCardiacaExtremaRegra.LIMITE_INFERIOR_PADRAO_BPM);
        }
        return "nenhum limite atingido";
    }

    public static void main(String[] args) throws IOException {
        Path pasta = Path.of(args.length > 0 ? args[0] : "dados");
        long semente = args.length > 1 ? Long.parseLong(args[1]) : SEMENTE_PADRAO;

        GeradorDados gerador = new GeradorDados(semente, PREFIXO_CPF_PADRAO, BASE_PADRAO);
        List<PacienteGerado> pacientes = gerador.pacientes(QTD_PACIENTES);
        List<LeituraGerada> leituras = gerador.leituras(pacientes, LEITURAS_POR_PACIENTE);

        Files.createDirectories(pasta);
        Files.writeString(pasta.resolve("pacientes.csv"), csvPacientes(pacientes), StandardCharsets.UTF_8);
        Files.writeString(pasta.resolve("sinais.csv"), csvSinais(leituras), StandardCharsets.UTF_8);
        Files.writeString(pasta.resolve("sinais_regras_esperadas.csv"),
                csvRegrasEsperadas(leituras), StandardCharsets.UTF_8);

        long pressao = leituras.stream().filter(l -> REGRA_PRESSAO.equals(l.regraEsperada())).count();
        long fc = leituras.stream().filter(l -> REGRA_FC.equals(l.regraEsperada())).count();
        System.out.println(pacientes.size() + " pacientes e " + leituras.size() + " leituras em " + pasta);
        System.out.println("Alertas esperados: " + (pressao + fc)
                + " (pressao critica: " + pressao + ", frequencia cardiaca: " + fc + ")");
    }
}