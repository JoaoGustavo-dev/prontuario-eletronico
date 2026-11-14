package com.health.prontuario.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.health.prontuario.exception.ArquivoConfiguracaoException;
import com.health.prontuario.model.regra.ConfiguracaoAlerta;

class ArquivoConfiguracaoAlertaTest {

    private static final Instant INSTANTE = Instant.parse("2026-01-01T10:00:00Z");

    @TempDir
    Path pasta;

    private static byte[] conteudo(int magico, int versao, int sistolica, int diastolica,
                                   int fcSuperior, int fcInferior) {
        return ByteBuffer.allocate(32)
                .putInt(magico).putInt(versao)
                .putInt(sistolica).putInt(diastolica)
                .putInt(fcSuperior).putInt(fcInferior)
                .putLong(INSTANTE.toEpochMilli())
                .array();
    }

    private Path escrever(byte[] bytes) throws IOException {
        Path arquivo = pasta.resolve("config.bin");
        Files.write(arquivo, bytes);
        return arquivo;
    }

    @Test
    void gravarELerDevolveOMesmoObjeto() {
        Path arquivo = pasta.resolve("config.bin");
        ConfiguracaoAlerta original = new ConfiguracaoAlerta(170, 110, 140, 45, INSTANTE);

        ArquivoConfiguracaoAlerta.gravar(arquivo, original);

        assertEquals(original, ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void arquivoGravadoTemTamanhoFixoDe32Bytes() throws IOException {
        Path arquivo = pasta.resolve("config.bin");

        ArquivoConfiguracaoAlerta.gravar(arquivo, ConfiguracaoAlerta.padrao());

        assertEquals(32, Files.size(arquivo));
    }

    @Test
    void lerOuCriarPadraoCriaOArquivoQuandoNaoExiste() {
        Path arquivo = pasta.resolve("novo.bin");

        ConfiguracaoAlerta lida = ArquivoConfiguracaoAlerta.lerOuCriarPadrao(arquivo);

        assertTrue(Files.exists(arquivo));
        assertEquals(180, lida.getLimiteSistolicaMmhg());
        assertEquals(lida, ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void lerOuCriarPadraoUsaOArquivoExistente() {
        Path arquivo = pasta.resolve("config.bin");
        ArquivoConfiguracaoAlerta.gravar(arquivo, new ConfiguracaoAlerta(200, 130, 160, 50, INSTANTE));

        ConfiguracaoAlerta lida = ArquivoConfiguracaoAlerta.lerOuCriarPadrao(arquivo);

        assertEquals(200, lida.getLimiteSistolicaMmhg());
        assertEquals(50, lida.getLimiteFcInferiorBpm());
    }

    @Test
    void aceitaLimitesNosExtremosDaFaixaAoLerDoArquivo() throws IOException {
        Path arquivo = escrever(conteudo(ArquivoConfiguracaoAlerta.NUMERO_MAGICO,
                ArquivoConfiguracaoAlerta.VERSAO_ATUAL, 300, 300, 300, 299));

        ConfiguracaoAlerta lida = ArquivoConfiguracaoAlerta.ler(arquivo);

        assertEquals(300, lida.getLimiteSistolicaMmhg());
        assertEquals(299, lida.getLimiteFcInferiorBpm());
    }

    @Test
    void numeroMagicoErradoEhRejeitado() throws IOException {
        Path arquivo = escrever(conteudo(0x12345678, ArquivoConfiguracaoAlerta.VERSAO_ATUAL, 180, 120, 150, 40));

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void versaoDesconhecidaEhRejeitada() throws IOException {
        Path arquivo = escrever(conteudo(ArquivoConfiguracaoAlerta.NUMERO_MAGICO, 99, 180, 120, 150, 40));

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void arquivoTruncadoEhRejeitado() throws IOException {
        byte[] completo = conteudo(ArquivoConfiguracaoAlerta.NUMERO_MAGICO,
                ArquivoConfiguracaoAlerta.VERSAO_ATUAL, 180, 120, 150, 40);
        Path arquivo = escrever(Arrays.copyOf(completo, 20));

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void bytesSobrandoSaoRejeitados() throws IOException {
        byte[] completo = conteudo(ArquivoConfiguracaoAlerta.NUMERO_MAGICO,
                ArquivoConfiguracaoAlerta.VERSAO_ATUAL, 180, 120, 150, 40);
        Path arquivo = escrever(Arrays.copyOf(completo, 33));

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void valoresInvalidosNoArquivoSaoRejeitados() throws IOException {
        Path arquivo = escrever(conteudo(ArquivoConfiguracaoAlerta.NUMERO_MAGICO,
                ArquivoConfiguracaoAlerta.VERSAO_ATUAL, 0, 120, 150, 40));

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }

    @Test
    void lerArquivoInexistenteLancaExcecao() {
        Path arquivo = pasta.resolve("nao-existe.bin");

        assertThrows(ArquivoConfiguracaoException.class, () -> ArquivoConfiguracaoAlerta.ler(arquivo));
    }
}