package com.health.prontuario.repository;

import com.health.prontuario.model.SinaisVitais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {

    List<SinaisVitais> findByPacienteIdOrderByInstanteDesc(Long pacienteId);

    @Query("select s from SinaisVitais s join fetch s.paciente order by s.instante asc, s.id asc")
    List<SinaisVitais> listarTodosComPaciente();
}