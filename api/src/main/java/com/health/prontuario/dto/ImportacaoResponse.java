package com.health.prontuario.dto;

import java.util.List;

public record ImportacaoResponse(int linhasLidas, int importadas, List<ErroLinha> erros) {

    public record ErroLinha(int linha, String motivo) {
    }
}