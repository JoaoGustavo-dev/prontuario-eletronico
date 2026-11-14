package com.health.prontuario.service;

import com.health.prontuario.csv.CsvUtil;
import com.health.prontuario.model.Paciente;
import com.health.prontuario.model.SinaisVitais;
import com.health.prontuario.repository.PacienteRepository;
import com.health.prontuario.repository.SinaisVitaisRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportacaoService {

    public static final String CABECALHO_PACIENTES = "cpf;nome;data_nascimento";
    public static final String CABECALHO_SINAIS =
            "cpf;pressao_sistolica_mmhg;pressao_diastolica_mmhg;frequencia_cardiaca_bpm;instante_utc";

    private final PacienteRepository pacienteRepository;
    private final SinaisVitaisRepository sinaisRepository;

    public ExportacaoService(PacienteRepository pacienteRepository,
                             SinaisVitaisRepository sinaisRepository) {
        this.pacienteRepository = pacienteRepository;
        this.sinaisRepository = sinaisRepository;
    }

    @Transactional(readOnly = true)
    public String exportarPacientes() {
        StringBuilder sb = new StringBuilder(CABECALHO_PACIENTES).append('\n');
        for (Paciente p : pacienteRepository.findAll(Sort.by("id"))) {
            sb.append(CsvUtil.linha(p.getCpf(), p.getNome(), p.getDataNascimento())).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportarSinais() {
        StringBuilder sb = new StringBuilder(CABECALHO_SINAIS).append('\n');
        for (SinaisVitais s : sinaisRepository.listarTodosComPaciente()) {
            sb.append(CsvUtil.linha(
                    s.getPaciente().getCpf(),
                    s.getPressaoSistolica(),
                    s.getPressaoDiastolica(),
                    s.getFrequenciaCardiaca(),
                    s.getInstante())).append('\n');
        }
        return sb.toString();
    }
}