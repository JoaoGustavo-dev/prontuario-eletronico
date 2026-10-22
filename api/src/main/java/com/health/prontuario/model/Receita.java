package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RECEITA")
public class Receita extends RegistroClinico {

    private String medicamento;
    private String posologia;

    protected Receita() {
    }

    public Receita(Paciente paciente, String notaClinica, String medicamento, String posologia) {
        super(paciente, notaClinica);
        if (medicamento == null || medicamento.isBlank()) {
            throw new DadosInvalidosException("Medicamento é obrigatório.");
        }
        this.medicamento = medicamento;
        this.posologia = posologia;
    }

    public String getMedicamento() { return medicamento; }
    public String getPosologia() { return posologia; }

    @Override
    public String descricao() {
        return "Receita de " + medicamento;
    }
}