package com.health.prontuario.csv;

import com.health.prontuario.exception.DadosInvalidosException;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {

    public static final char SEPARADOR = ';';

    private CsvUtil() {
    }

    public static String escapar(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();
        boolean precisaAspas = texto.indexOf(SEPARADOR) >= 0
                || texto.indexOf('"') >= 0
                || texto.indexOf('\n') >= 0
                || texto.indexOf('\r') >= 0;
        if (!precisaAspas) {
            return texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    public static String linha(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    public static List<String> separar(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        boolean campoComAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (entreAspas) {
                if (c == '"') {
                    if (i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                        atual.append('"');
                        i++;
                    } else {
                        entreAspas = false;
                    }
                } else {
                    atual.append(c);
                }
            } else if (c == '"' && atual.length() == 0 && !campoComAspas) {
                entreAspas = true;
                campoComAspas = true;
            } else if (c == SEPARADOR) {
                campos.add(atual.toString());
                atual.setLength(0);
                campoComAspas = false;
            } else if (campoComAspas) {
                throw new DadosInvalidosException("Texto após aspas de fechamento.");
            } else if (c == '"') {
                throw new DadosInvalidosException("Aspas no meio de um campo sem aspas.");
            } else {
                atual.append(c);
            }
        }
        if (entreAspas) {
            throw new DadosInvalidosException("Aspas não fechadas.");
        }
        campos.add(atual.toString());
        return campos;
    }
}