package com.health.prontuario.config;

import com.health.prontuario.model.regra.FrequenciaCardiacaExtremaRegra;
import com.health.prontuario.model.regra.PressaoCriticaRegra;
import com.health.prontuario.model.regra.RegraAlerta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RegrasAlertaConfig {

    @Bean
    List<RegraAlerta> regrasAlerta() {
        return List.of(new PressaoCriticaRegra(), new FrequenciaCardiacaExtremaRegra());
    }
}