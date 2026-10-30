package com.health.prontuario.service;

import com.health.prontuario.dto.PacienteRequest;
import com.health.prontuario.exception.PacienteDuplicadoException;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.health.prontuario.exception.PacienteNaoEncontradoException;

@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Paciente cadastrar(PacienteRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new PacienteDuplicadoException("Já existe paciente com este CPF.");
        }
        return repository.save(new Paciente(request.nome(), request.dataNascimento(), request.cpf()));
    }

    @Transactional(readOnly = true)
    public Paciente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));
    }
}