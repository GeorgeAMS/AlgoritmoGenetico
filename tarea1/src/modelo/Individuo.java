package modelo;

import estructuras.ListaOperaciones;

public class Individuo {
    private static final int VALOR_INICIAL = 0;

    private final ListaOperaciones cromosoma;
    private int resultado;
    private int fitness;

    public Individuo(ListaOperaciones cromosoma) {
        this.cromosoma = cromosoma;
    }

    public void evaluar(int objetivo) {
        resultado = cromosoma.ejecutar(VALOR_INICIAL);
        fitness = Math.max(1, 100 - Math.abs(objetivo - resultado));
    }

    public Individuo copiar() {
        Individuo copia = new Individuo(cromosoma.copiar());
        copia.resultado = resultado;
        copia.fitness = fitness;
        return copia;
    }

    public ListaOperaciones getCromosoma() {
        return cromosoma;
    }

    public int getResultado() {
        return resultado;
    }

    public int getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return cromosoma + " | resultado = " + resultado + " | fitness = " + fitness;
    }
}
