package com.health.prontuario.service;

import com.health.prontuario.dto.RegistroRequest;
import com.health.prontuario.dto.RegistroResponse;
import com.health.prontuario.exception.DadosInvalidosException;
import com.health.prontuario.model.Exame;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.Receita;
import com.health.prontuario.model.RegistroClinico;
import com.health.prontuario.repository.RegistroClinicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final PacienteService pacienteService;
    private final RegistroClinicoRepository repository;

    public RegistroService(PacienteService pacienteService, RegistroClinicoRepository repository) {
        this.pacienteService = pacienteService;
        this.repository = repository;
    }

    @Transactional
    public RegistroResponse registrar(Long pacienteId, RegistroRequest request) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);

        RegistroClinico registro = switch (request.tipo().trim().toUpperCase()) {
            case "EXAME" -> new Exame(paciente, request.notaClinica(),
                    request.tipoExame(), request.resultado());
            case "RECEITA" -> new Receita(paciente, request.notaClinica(),
                    request.medicamento(), request.posologia());
            default -> throw new DadosInvalidosException("Tipo de registro inválido. Use EXAME ou RECEITA.");
        };

        return RegistroResponse.de(repository.save(registro));
    }
}