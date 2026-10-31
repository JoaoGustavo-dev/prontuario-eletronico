package com.health.prontuario.repository;

import com.health.prontuario.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    @Query("select a from Alerta a join fetch a.sinais s join fetch s.paciente order by a.geradoEm desc")
    List<Alerta> listarRecentesPrimeiro();
}