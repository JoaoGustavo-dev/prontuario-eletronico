package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "registros_clinicos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class RegistroClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @Column(name = "nota_clinica", length = 2000)
    private String notaClinica;

    protected RegistroClinico() {
    }

    protected RegistroClinico(Paciente paciente, String notaClinica) {
        if (paciente == null) {
            throw new DadosInvalidosException("Paciente é obrigatório.");
        }
        this.paciente = paciente;
        this.notaClinica = notaClinica;
        this.registradoEm = Instant.now();
    }

    public abstract String descricao();

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Instant getRegistradoEm() { return registradoEm; }
    public String getNotaClinica() { return notaClinica; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", " + descricao() + "}";
    }
}