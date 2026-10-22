package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EXAME")
public class Exame extends RegistroClinico {

    private String tipoExame;
    private String resultado;

    protected Exame() {
    }

    public Exame(Paciente paciente, String notaClinica, String tipoExame, String resultado) {
        super(paciente, notaClinica);
        if (tipoExame == null || tipoExame.isBlank()) {
            throw new DadosInvalidosException("Tipo do exame é obrigatório.");
        }
        this.tipoExame = tipoExame;
        this.resultado = resultado;
    }

    public String getTipoExame() { return tipoExame; }
    public String getResultado() { return resultado; }

    @Override
    public String descricao() {
        return "Exame " + tipoExame;
    }
}