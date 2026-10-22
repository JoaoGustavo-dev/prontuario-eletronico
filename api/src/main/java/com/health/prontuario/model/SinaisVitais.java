package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sinais_vitais")
public class SinaisVitais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(name = "pressao_sistolica_mmhg", nullable = false)
    private int pressaoSistolica;

    @Column(name = "pressao_diastolica_mmhg", nullable = false)
    private int pressaoDiastolica;

    @Column(name = "frequencia_cardiaca_bpm", nullable = false)
    private int frequenciaCardiaca;

    @Column(nullable = false)
    private Instant instante;

    protected SinaisVitais() {
    }

    public SinaisVitais(Paciente paciente, int pressaoSistolica, int pressaoDiastolica,
                        int frequenciaCardiaca, Instant instante) {
        if (paciente == null) {
            throw new DadosInvalidosException("Paciente é obrigatório.");
        }
        if (instante == null) {
            throw new DadosInvalidosException("Instante da leitura é obrigatório.");
        }
        if (!pressaoPlausivel(pressaoSistolica, pressaoDiastolica)) {
            throw new DadosInvalidosException("Pressão arterial fora da faixa plausível (mmHg).");
        }
        if (frequenciaCardiaca < 20 || frequenciaCardiaca > 300) {
            throw new DadosInvalidosException("Frequência cardíaca fora da faixa plausível (bpm).");
        }
        this.paciente = paciente;
        this.pressaoSistolica = pressaoSistolica;
        this.pressaoDiastolica = pressaoDiastolica;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.instante = instante;
    }

    public SinaisVitais(Paciente paciente, int pressaoSistolica, int pressaoDiastolica,
                        int frequenciaCardiaca) {
        this(paciente, pressaoSistolica, pressaoDiastolica, frequenciaCardiaca, Instant.now());
    }

    public static boolean pressaoPlausivel(int sistolica, int diastolica) {
        return sistolica >= 40 && sistolica <= 300
                && diastolica >= 20 && diastolica <= 200
                && sistolica > diastolica;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public int getPressaoSistolica() { return pressaoSistolica; }
    public int getPressaoDiastolica() { return pressaoDiastolica; }
    public int getFrequenciaCardiaca() { return frequenciaCardiaca; }
    public Instant getInstante() { return instante; }

    @Override
    public String toString() {
        return "SinaisVitais{" + pressaoSistolica + "/" + pressaoDiastolica + " mmHg, "
                + frequenciaCardiaca + " bpm, " + instante + "}";
    }
}