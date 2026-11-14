package com.health.prontuario.config;

import com.health.prontuario.exception.ArquivoConfiguracaoException;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.regra.ConfiguracaoAlerta;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class ArquivoConfiguracaoAlerta {

    public static final int NUMERO_MAGICO = 0x50524F4E;
    public static final int VERSAO_ATUAL = 1;

    private ArquivoConfiguracaoAlerta() {
    }

    public static void gravar(Path caminho, ConfiguracaoAlerta configuracao) {
        try {
            Path pasta = caminho.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            try (DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(caminho)))) {
                out.writeInt(NUMERO_MAGICO);
                out.writeInt(VERSAO_ATUAL);
                out.writeInt(configuracao.getLimiteSistolicaMmhg());
                out.writeInt(configuracao.getLimiteDiastolicaMmhg());
                out.writeInt(configuracao.getLimiteFcSuperiorBpm());
                out.writeInt(configuracao.getLimiteFcInferiorBpm());
                out.writeLong(configuracao.getGravadoEm().toEpochMilli());
            }
        } catch (IOException e) {
            throw new ArquivoConfiguracaoException("Não foi possível gravar " + caminho, e);
        }
    }

    public static ConfiguracaoAlerta ler(Path caminho) {
        if (!Files.isRegularFile(caminho)) {
            throw new ArquivoConfiguracaoException("Arquivo de configuração não encontrado: " + caminho);
        }
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(caminho)))) {
            int magico = in.readInt();
            if (magico != NUMERO_MAGICO) {
                throw new ArquivoConfiguracaoException(
                        "Arquivo não é um config.bin válido (número mágico incorreto): " + caminho);
            }
            int versao = in.readInt();
            if (versao != VERSAO_ATUAL) {
                throw new ArquivoConfiguracaoException(
                        "Versão de formato desconhecida: " + versao + " (suportada: " + VERSAO_ATUAL + ").");
            }
            int sistolica = in.readInt();
            int diastolica = in.readInt();
            int fcSuperior = in.readInt();
            int fcInferior = in.readInt();
            long gravadoEmMs = in.readLong();
            if (in.read() != -1) {
                throw new ArquivoConfiguracaoException(
                        "Arquivo com bytes além do esperado (corrompido?): " + caminho);
            }
            return new ConfiguracaoAlerta(sistolica, diastolica, fcSuperior, fcInferior,
                    Instant.ofEpochMilli(gravadoEmMs));
        } catch (EOFException e) {
            throw new ArquivoConfiguracaoException("Arquivo truncado: " + caminho, e);
        } catch (IOException e) {
            throw new ArquivoConfiguracaoException("Não foi possível ler " + caminho, e);
        } catch (DadosInvalidosException e) {
            throw new ArquivoConfiguracaoException(
                    "Valores inválidos no arquivo " + caminho + ": " + e.getMessage(), e);
        }
    }

    public static ConfiguracaoAlerta lerOuCriarPadrao(Path caminho) {
        if (Files.exists(caminho)) {
            return ler(caminho);
        }
        ConfiguracaoAlerta padrao = ConfiguracaoAlerta.padrao();
        gravar(caminho, padrao);
        return padrao;
    }
}