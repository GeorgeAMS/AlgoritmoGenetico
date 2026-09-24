package modelo;

import java.util.Random;

public enum Operacion {
    SUMAR_UNO("+1") {
        @Override
        public int aplicar(int numero) {
            return numero + 1;
        }
    },
    SUMAR_DOS("+2") {
        @Override
        public int aplicar(int numero) {
            return numero + 2;
        }
    },
    MULTIPLICAR_DOS("x2") {
        @Override
        public int aplicar(int numero) {
            return numero * 2;
        }
    };

    private final String simbolo;

    Operacion(String simbolo) {
        this.simbolo = simbolo;
    }

    public abstract int aplicar(int numero);

    public static Operacion aleatoria(Random random) {
        Operacion[] operaciones = values();
        return operaciones[random.nextInt(operaciones.length)];
    }

    @Override
    public String toString() {
        return simbolo;
    }
}
