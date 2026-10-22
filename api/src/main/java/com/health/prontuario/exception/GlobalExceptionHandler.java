package com.health.prontuario.exception;

import com.health.prontuario.dto.ErroResposta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, "Dados inválidos.", campos));
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(DadosInvalidosException ex) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, ex.getMessage(), null));
    }

    @ExceptionHandler(PacienteDuplicadoException.class)
    public ResponseEntity<ErroResposta> duplicado(PacienteDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta(409, ex.getMessage(), null));
    }
}