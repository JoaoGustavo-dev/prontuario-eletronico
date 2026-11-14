package com.health.prontuario.csv;

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
}