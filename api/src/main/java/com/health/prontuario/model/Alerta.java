package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sinais_id")
    private SinaisVitais sinais;

    @Column(nullable = false)
    private String regra;

    @Column(nullable = false, length = 500)
    private String justificativa;

    @Column(name = "gerado_em", nullable = false)
    private Instant geradoEm;

    protected Alerta() {
    }

    public Alerta(SinaisVitais sinais, String regra, String justificativa) {
        if (sinais == null || regra == null || regra.isBlank()
                || justificativa == null || justificativa.isBlank()) {
            throw new DadosInvalidosException("Alerta exige sinais, regra e justificativa.");
        }
        this.sinais = sinais;
        this.regra = regra;
        this.justificativa = justificativa;
        this.geradoEm = Instant.now();
    }

    public Long getId() { return id; }
    public SinaisVitais getSinais() { return sinais; }
    public String getRegra() { return regra; }
    public String getJustificativa() { return justificativa; }
    public Instant getGeradoEm() { return geradoEm; }

    @Override
    public String toString() {
        return "Alerta{" + regra + ": " + justificativa + "}";
    }
}