package com.health.prontuario.gerador;

import java.time.Instant;

public record LeituraGerada(String cpf, int pressaoSistolica, int pressaoDiastolica,
                            int frequenciaCardiaca, Instant instante, String regraEsperada) {
}