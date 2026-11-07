package com.health.prontuario.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter
@Component
public class NotaClinicaConverter implements AttributeConverter<String, String> {

    private final CifradorNotas cifrador;

    public NotaClinicaConverter(CifradorNotas cifrador) {
        this.cifrador = cifrador;
    }

    @Override
    public String convertToDatabaseColumn(String notaPura) {
        return cifrador.cifrar(notaPura);
    }

    @Override
    public String convertToEntityAttribute(String notaCifrada) {
        return cifrador.decifrar(notaCifrada);
    }
}