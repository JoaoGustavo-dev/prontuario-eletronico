package com.health.prontuario.model.regra;

import com.health.prontuario.model.Alerta;
import com.health.prontuario.model.SinaisVitais;

import java.util.Optional;

public interface RegraAlerta {

    String nome();

    Optional<Alerta> avaliar(SinaisVitais sinais);
}