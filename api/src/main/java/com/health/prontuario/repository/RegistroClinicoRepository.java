package com.health.prontuario.repository;

import com.health.prontuario.model.RegistroClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroClinicoRepository extends JpaRepository<RegistroClinico, Long> {

    List<RegistroClinico> findByPacienteIdOrderByRegistradoEmDesc(Long pacienteId);
}