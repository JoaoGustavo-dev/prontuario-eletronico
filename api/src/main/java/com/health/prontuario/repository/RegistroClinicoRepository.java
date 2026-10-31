package com.health.prontuario.repository;

import com.health.prontuario.model.RegistroClinico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroClinicoRepository extends JpaRepository<RegistroClinico, Long> {
}