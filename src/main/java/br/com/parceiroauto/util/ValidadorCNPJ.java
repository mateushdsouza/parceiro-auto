package br.com.parceiroauto.util;

public class ValidadorCNPJ {

    public static boolean isCNPJ(String cnpj) {
        // Remove caracteres especiais: .-/
        cnpj = cnpj.replaceAll("[^0-9]","");

        // Verifica se tem tamanho 14 ou se é uma sequência de dígitos iguais (inválido)
        if (cnpj.length() != 14 ||
                cnpj.equals("00000000000000") || cnpj.equals("11111111111111") ||
                cnpj.equals("22222222222222") || cnpj.equals("33333333333333") ||
                cnpj.equals("44444444444444") || cnpj.equals("55555555555555") ||
                cnpj.equals("66666666666666") || cnpj.equals("77777777777777") ||
                cnpj.equals("88888888888888") || cnpj.equals("99999999999999"))
            return false;

        char dig13, dig14;
        int sm, i, r, num, peso;

        try {
            // Calculo do 1º dígito verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) peso = 2;
            }
            r = sm % 11;
            if ((r == 0) || (r == 1)) dig13 = '0';
            else dig13 = (char) ((11 - r) + 48);

            // Calculo do 2º dígito verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) peso = 2;
            }
            r = sm % 11;
            if ((r == 0) || (r == 1)) dig14 = '0';
            else dig14 = (char) ((11 - r) + 48);

            // Verifica se os dígitos calculados conferem com os informados
            return (dig13 == cnpj.charAt(12) && dig14 == cnpj.charAt(13));
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String cnpj = "11.222.333/0001-81"; // Exemplo
        System.out.println("CNPJ " + cnpj + " é válido? " + isCNPJ(cnpj));
    }
}
