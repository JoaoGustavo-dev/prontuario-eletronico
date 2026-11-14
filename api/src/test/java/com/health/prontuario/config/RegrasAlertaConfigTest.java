package com.health.prontuario.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.health.prontuario.exception.ArquivoConfiguracaoException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.model.regra.ConfiguracaoAlerta;
import com.health.prontuario.model.regra.RegraAlerta;

class RegrasAlertaConfigTest {

    @TempDir
    Path pasta;

    private final Paciente paciente = new Paciente("Teste", LocalDate.of(1990, 1, 1), "12345678901");

    @Test
    void regrasUsamOsLimitesDoArquivo() {
        Path arquivo = pasta.resolve("config.bin");
        ArquivoConfiguracaoAlerta.gravar(arquivo,
                new ConfiguracaoAlerta(200, 130, 150, 40, Instant.parse("2026-01-01T10:00:00Z")));

        List<RegraAlerta> regras = new RegrasAlertaConfig().regrasAlerta(arquivo.toString());
        RegraAlerta pressao = regras.get(0);

        assertTrue(pressao.avaliar(new SinaisVitais(paciente, 190, 100, 70)).isEmpty());
        assertTrue(pressao.avaliar(new SinaisVitais(paciente, 200, 100, 70)).isPresent());
    }

    @Test
    void semArquivoCriaOPadraoEAplicaOsLimitesPadrao() {
        Path arquivo = pasta.resolve("novo.bin");

        List<RegraAlerta> regras = new RegrasAlertaConfig().regrasAlerta(arquivo.toString());

        assertTrue(Files.exists(arquivo));
        assertTrue(regras.get(0).avaliar(new SinaisVitais(paciente, 180, 100, 70)).isPresent());
    }

    @Test
    void arquivoCorrompidoImpedeACriacaoDasRegras() throws IOException {
        Path arquivo = pasta.resolve("config.bin");
        Files.writeString(arquivo, "lixo");

        assertThrows(ArquivoConfiguracaoException.class,
                () -> new RegrasAlertaConfig().regrasAlerta(arquivo.toString()));
    }
}