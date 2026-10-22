package com.health.prontuario.model.regra;

import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Alerta;
import com.health.prontuario.model.SinaisVitais;

import java.util.Optional;

public class FrequenciaCardiacaExtremaRegra implements RegraAlerta {

    public static final int LIMITE_SUPERIOR_PADRAO_BPM = 150;
    public static final int LIMITE_INFERIOR_PADRAO_BPM = 40;

    private final int limiteSuperior;
    private final int limiteInferior;

    public FrequenciaCardiacaExtremaRegra() {
        this(LIMITE_SUPERIOR_PADRAO_BPM, LIMITE_INFERIOR_PADRAO_BPM);
    }

    public FrequenciaCardiacaExtremaRegra(int limiteSuperior, int limiteInferior) {
        if (limiteInferior <= 0 || limiteSuperior <= limiteInferior) {
            throw new DadosInvalidosException("Limites de frequência cardíaca inválidos.");
        }
        this.limiteSuperior = limiteSuperior;
        this.limiteInferior = limiteInferior;
    }

    @Override
    public String nome() {
        return "FREQUENCIA_CARDIACA_EXTREMA";
    }

    @Override
    public Optional<Alerta> avaliar(SinaisVitais sinais) {
        if (sinais == null) {
            throw new DadosInvalidosException("Sinais vitais são obrigatórios.");
        }
        int fc = sinais.getFrequenciaCardiaca();
        if (fc >= limiteSuperior) {
            return Optional.of(new Alerta(sinais, nome(),
                    String.format("Taquicardia: %d bpm (limite %d bpm).", fc, limiteSuperior)));
        }
        if (fc <= limiteInferior) {
            return Optional.of(new Alerta(sinais, nome(),
                    String.format("Bradicardia: %d bpm (limite %d bpm).", fc, limiteInferior)));
        }
        return Optional.empty();
    }
}