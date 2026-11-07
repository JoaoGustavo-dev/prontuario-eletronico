package com.health.prontuario.crypto;

import com.health.prontuario.exception.CriptografiaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class CifradorNotas {

    private static final String TRANSFORMACAO = "AES/GCM/NoPadding";
    private static final int TAMANHO_CHAVE_BYTES = 32;
    private static final int TAMANHO_IV_BYTES = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecretKey chave;
    private final SecureRandom aleatorio = new SecureRandom();

    public CifradorNotas(@Value("${app.crypto.chave}") String chaveBase64) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(chaveBase64);
        } catch (IllegalArgumentException e) {
            throw new CriptografiaException("A chave AES não está em Base64 válido.", e);
        }
        if (bytes.length != TAMANHO_CHAVE_BYTES) {
            throw new CriptografiaException(
                    "A chave AES deve ter 32 bytes (256 bits), mas tem " + bytes.length + ".");
        }
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    public String cifrar(String textoPuro) {
        if (textoPuro == null) {
            return null;
        }
        try {
            byte[] iv = new byte[TAMANHO_IV_BYTES];
            aleatorio.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPuro.getBytes(StandardCharsets.UTF_8));

            byte[] saida = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, saida, 0, iv.length);
            System.arraycopy(cifrado, 0, saida, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(saida);
        } catch (GeneralSecurityException e) {
            throw new CriptografiaException("Falha ao cifrar o texto.", e);
        }
    }

    public String decifrar(String textoCifrado) {
        if (textoCifrado == null) {
            return null;
        }
        byte[] entrada;
        try {
            entrada = Base64.getDecoder().decode(textoCifrado);
        } catch (IllegalArgumentException e) {
            throw new CriptografiaException("O texto cifrado não está em Base64 válido.", e);
        }
        if (entrada.length < TAMANHO_IV_BYTES + TAMANHO_TAG_BITS / 8) {
            throw new CriptografiaException("O texto cifrado é curto demais para ser válido.");
        }
        try {
            byte[] iv = Arrays.copyOfRange(entrada, 0, TAMANHO_IV_BYTES);
            byte[] cifrado = Arrays.copyOfRange(entrada, TAMANHO_IV_BYTES, entrada.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new CriptografiaException(
                    "Falha ao decifrar: dado adulterado ou chave incorreta.", e);
        }
    }
}