package com.health.prontuario.repository;

import com.health.prontuario.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);

    Optional<Paciente> findByCpf(String cpf);
}