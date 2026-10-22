package com.health.prontuario.dto;

import java.util.Map;

public record ErroResposta(int status, String mensagem, Map<String, String> campos) {
}