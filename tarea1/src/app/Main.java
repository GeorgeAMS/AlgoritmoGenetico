package app;

import genetico.AlgoritmoGenetico;
import genetico.Configuracion;
import modelo.Individuo;
import vista.VistaPrincipal;

public class Main {
    public static void main(String[] args) {
        Configuracion configuracion = crearConfiguracion();

        if (args.length > 0 && "--consola".equalsIgnoreCase(args[0])) {
            ejecutarEnConsola(configuracion);
        } else {
            VistaPrincipal.abrir(configuracion);
        }
    }

    private static Configuracion crearConfiguracion() {
        return new Configuracion(
                11,    // Numero al que queremos llegar
                6,     // Cantidad de nodos en cada lista
                40,    // Individuos de la poblacion
                100,   // Limite de generaciones
                0.80,  // Probabilidad de cruce
                0.15,  // Probabilidad de mutacion
                1      // Individuos conservados por elitismo
        );
    }

    private static void ejecutarEnConsola(Configuracion configuracion) {
        System.out.println("OBJETIVO");
        System.out.println("Encontrar una lista de 6 operaciones que transforme 0 en 11.");
        System.out.println("Operaciones disponibles: +1, +2 y x2.\n");

        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(configuracion);
        Individuo solucion = algoritmo.ejecutar();

        System.out.println("\nMEJOR SOLUCION");
        System.out.println(solucion);
        System.out.println(solucion.getResultado() == configuracion.getObjetivo()
                ? "Objetivo alcanzado."
                : "No se alcanzo el objetivo dentro del limite.");
    }
}
