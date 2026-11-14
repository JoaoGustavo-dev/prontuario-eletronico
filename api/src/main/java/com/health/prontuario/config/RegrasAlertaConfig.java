package com.health.prontuario.config;

import com.health.prontuario.model.regra.ConfiguracaoAlerta;
import com.health.prontuario.model.regra.FrequenciaCardiacaExtremaRegra;
import com.health.prontuario.model.regra.PressaoCriticaRegra;
import com.health.prontuario.model.regra.RegraAlerta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

@Configuration
public class RegrasAlertaConfig {

    @Bean
    List<RegraAlerta> regrasAlerta(@Value("${app.config.arquivo}") String caminho) {
        ConfiguracaoAlerta c = ArquivoConfiguracaoAlerta.lerOuCriarPadrao(Path.of(caminho));
        return List.of(
                new PressaoCriticaRegra(c.getLimiteSistolicaMmhg(), c.getLimiteDiastolicaMmhg()),
                new FrequenciaCardiacaExtremaRegra(c.getLimiteFcSuperiorBpm(), c.getLimiteFcInferiorBpm()));
    }
}