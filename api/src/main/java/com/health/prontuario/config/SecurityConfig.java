package com.health.prontuario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    public static final String PROFISSIONAL = "PROFISSIONAL";
    public static final String LEITOR = "LEITOR";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole(PROFISSIONAL, LEITOR)
                        .anyRequest().hasRole(PROFISSIONAL));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${app.security.profissional.usuario}") String profUsuario,
            @Value("${app.security.profissional.senha}") String profSenha,
            @Value("${app.security.leitor.usuario}") String leitorUsuario,
            @Value("${app.security.leitor.senha}") String leitorSenha) {

        UserDetails profissional = User.withUsername(profUsuario)
                .password(encoder.encode(profSenha))
                .roles(PROFISSIONAL)
                .build();

        UserDetails leitor = User.withUsername(leitorUsuario)
                .password(encoder.encode(leitorSenha))
                .roles(LEITOR)
                .build();

        return new InMemoryUserDetailsManager(profissional, leitor);
    }
}