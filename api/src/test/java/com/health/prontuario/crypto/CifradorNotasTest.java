package com.health.prontuario.crypto;

import com.health.prontuario.exception.CriptografiaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CifradorNotasTest {

    private CifradorNotas cifrador;

    private static String chaveAleatoria(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    @BeforeEach
    void preparar() {
        cifrador = new CifradorNotas(chaveAleatoria(32));
    }


    @Test
    void cifrarEDecifrarDevolveOTextoOriginal() {
        String nota = "Paciente refere dor torácica há 2 dias. Conduta: ECG e observação.";
        assertEquals(nota, cifrador.decifrar(cifrador.cifrar(nota)));
    }

    @Test
    void textoCifradoNaoContemOTextoOriginal() {
        String nota = "hipertensão";
        assertFalse(cifrador.cifrar(nota).contains(nota));
    }

    @Test
    void mesmoTextoGeraCifradosDiferentesPorCausaDoIvUnico() {
        String nota = "mesma nota";
        assertNotEquals(cifrador.cifrar(nota), cifrador.cifrar(nota));
    }


    @Test
    void textoVazioEhSuportado() {
        assertEquals("", cifrador.decifrar(cifrador.cifrar("")));
    }

    @Test
    void nuloPassaDireto() {
        assertNull(cifrador.cifrar(null));
        assertNull(cifrador.decifrar(null));
    }

    @Test
    void textoNoTamanhoMaximoDaNotaEhSuportado() {
        String nota = "a".repeat(2000);
        assertEquals(nota, cifrador.decifrar(cifrador.cifrar(nota)));
    }


    @Test
    void dadoAdulteradoFalhaNaDecifragem() {
        byte[] bytes = Base64.getDecoder().decode(cifrador.cifrar("nota"));
        bytes[bytes.length - 1] ^= 1;
        String adulterado = Base64.getEncoder().encodeToString(bytes);
        assertThrows(CriptografiaException.class, () -> cifrador.decifrar(adulterado));
    }

    @Test
    void chaveDiferenteNaoDecifra() {
        String cifrado = cifrador.cifrar("nota");
        CifradorNotas outro = new CifradorNotas(chaveAleatoria(32));
        assertThrows(CriptografiaException.class, () -> outro.decifrar(cifrado));
    }

    @Test
    void chaveComTamanhoErradoEhRejeitada() {
        String chaveCurta = chaveAleatoria(16);
        assertThrows(CriptografiaException.class, () -> new CifradorNotas(chaveCurta));
    }

    @Test
    void chaveForaDoBase64EhRejeitada() {
        assertThrows(CriptografiaException.class, () -> new CifradorNotas("isso nao e base64!!"));
    }

    @Test
    void textoCifradoCurtoDemaisEhRejeitado() {
        String curto = Base64.getEncoder().encodeToString(new byte[5]);
        assertThrows(CriptografiaException.class, () -> cifrador.decifrar(curto));
    }
}