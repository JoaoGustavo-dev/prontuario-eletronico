package com.health.prontuario.model;

import com.health.prontuario.exception.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    protected Paciente() {
    }

    public Paciente(String nome, LocalDate dataNascimento, String cpf) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Nome é obrigatório.");
        }
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new DadosInvalidosException("Data de nascimento inválida.");
        }
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new DadosInvalidosException("CPF deve conter 11 dígitos numéricos.");
        }
        this.nome = nome.trim();
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getCpf() { return cpf; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente outro)) return false;
        return Objects.equals(cpf, outro.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return "Paciente{id=" + id + ", nome='" + nome + "'}";
    }
}