package com.health.prontuario.repository;

import com.health.prontuario.model.SinaisVitais;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {

    List<SinaisVitais> findByPacienteIdOrderByInstanteDesc(Long pacienteId);
}