package com.health.prontuario.model.regra;

import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Alerta;
import com.health.prontuario.model.SinaisVitais;

import java.util.Optional;

public class PressaoCriticaRegra implements RegraAlerta {

    public static final int SISTOLICA_PADRAO_MMHG = 180;
    public static final int DIASTOLICA_PADRAO_MMHG = 120;

    private final int limiteSistolica;
    private final int limiteDiastolica;

    public PressaoCriticaRegra() {
        this(SISTOLICA_PADRAO_MMHG, DIASTOLICA_PADRAO_MMHG);
    }

    public PressaoCriticaRegra(int limiteSistolica, int limiteDiastolica) {
        if (limiteSistolica <= 0 || limiteDiastolica <= 0) {
            throw new DadosInvalidosException("Limites de pressão devem ser positivos.");
        }
        this.limiteSistolica = limiteSistolica;
        this.limiteDiastolica = limiteDiastolica;
    }

    @Override
    public String nome() {
        return "PRESSAO_CRITICA";
    }

    @Override
    public Optional<Alerta> avaliar(SinaisVitais sinais) {
        if (sinais == null) {
            throw new DadosInvalidosException("Sinais vitais são obrigatórios.");
        }
        boolean critica = sinais.getPressaoSistolica() >= limiteSistolica
                || sinais.getPressaoDiastolica() >= limiteDiastolica;
        if (!critica) {
            return Optional.empty();
        }
        String justificativa = String.format(
                "Pressão %d/%d mmHg atingiu o limite crítico (%d/%d mmHg).",
                sinais.getPressaoSistolica(), sinais.getPressaoDiastolica(),
                limiteSistolica, limiteDiastolica);
        return Optional.of(new Alerta(sinais, nome(), justificativa));
    }
}