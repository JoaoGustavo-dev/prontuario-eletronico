package com.health.prontuario.model.regra;

import com.health.prontuario.exception.DadosInvalidosException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ConfiguracaoAlerta {

    public static final int LIMITE_MAXIMO = 300;

    private final int limiteSistolicaMmhg;
    private final int limiteDiastolicaMmhg;
    private final int limiteFcSuperiorBpm;
    private final int limiteFcInferiorBpm;
    private final Instant gravadoEm;

    public ConfiguracaoAlerta(int limiteSistolicaMmhg, int limiteDiastolicaMmhg,
                              int limiteFcSuperiorBpm, int limiteFcInferiorBpm, Instant gravadoEm) {
        validarFaixa(limiteSistolicaMmhg, "Limite de pressão sistólica");
        validarFaixa(limiteDiastolicaMmhg, "Limite de pressão diastólica");
        validarFaixa(limiteFcSuperiorBpm, "Limite superior de frequência cardíaca");
        validarFaixa(limiteFcInferiorBpm, "Limite inferior de frequência cardíaca");
        if (limiteFcSuperiorBpm <= limiteFcInferiorBpm) {
            throw new DadosInvalidosException(
                    "Limite superior de frequência cardíaca deve ser maior que o inferior.");
        }
        if (gravadoEm == null) {
            throw new DadosInvalidosException("Instante de gravação é obrigatório.");
        }
        this.limiteSistolicaMmhg = limiteSistolicaMmhg;
        this.limiteDiastolicaMmhg = limiteDiastolicaMmhg;
        this.limiteFcSuperiorBpm = limiteFcSuperiorBpm;
        this.limiteFcInferiorBpm = limiteFcInferiorBpm;
        this.gravadoEm = gravadoEm.truncatedTo(ChronoUnit.MILLIS);
    }

    public ConfiguracaoAlerta(int limiteSistolicaMmhg, int limiteDiastolicaMmhg,
                              int limiteFcSuperiorBpm, int limiteFcInferiorBpm) {
        this(limiteSistolicaMmhg, limiteDiastolicaMmhg, limiteFcSuperiorBpm, limiteFcInferiorBpm,
                Instant.now());
    }

    public static ConfiguracaoAlerta padrao() {
        return new ConfiguracaoAlerta(
                PressaoCriticaRegra.SISTOLICA_PADRAO_MMHG,
                PressaoCriticaRegra.DIASTOLICA_PADRAO_MMHG,
                FrequenciaCardiacaExtremaRegra.LIMITE_SUPERIOR_PADRAO_BPM,
                FrequenciaCardiacaExtremaRegra.LIMITE_INFERIOR_PADRAO_BPM);
    }

    private static void validarFaixa(int valor, String nome) {
        if (valor < 1 || valor > LIMITE_MAXIMO) {
            throw new DadosInvalidosException(nome + " deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
    }

    public int getLimiteSistolicaMmhg() { return limiteSistolicaMmhg; }
    public int getLimiteDiastolicaMmhg() { return limiteDiastolicaMmhg; }
    public int getLimiteFcSuperiorBpm() { return limiteFcSuperiorBpm; }
    public int getLimiteFcInferiorBpm() { return limiteFcInferiorBpm; }
    public Instant getGravadoEm() { return gravadoEm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfiguracaoAlerta outra)) return false;
        return limiteSistolicaMmhg == outra.limiteSistolicaMmhg
                && limiteDiastolicaMmhg == outra.limiteDiastolicaMmhg
                && limiteFcSuperiorBpm == outra.limiteFcSuperiorBpm
                && limiteFcInferiorBpm == outra.limiteFcInferiorBpm
                && Objects.equals(gravadoEm, outra.gravadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limiteSistolicaMmhg, limiteDiastolicaMmhg,
                limiteFcSuperiorBpm, limiteFcInferiorBpm, gravadoEm);
    }

    @Override
    public String toString() {
        return "ConfiguracaoAlerta{pressao=" + limiteSistolicaMmhg + "/" + limiteDiastolicaMmhg
                + " mmHg, fc=" + limiteFcInferiorBpm + ".." + limiteFcSuperiorBpm
                + " bpm, gravadoEm=" + gravadoEm + "}";
    }
}