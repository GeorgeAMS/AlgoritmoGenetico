package genetico;

import java.util.Random;
import estructuras.ListaOperaciones;
import estructuras.ListaPoblacion;
import modelo.Individuo;
import modelo.Operacion;

public class AlgoritmoGenetico {
    private static final int TAMANIO_TORNEO = 3;

    private final Configuracion configuracion;
    private final Random random;
    private final ObservadorEvolucion observador;
    private ListaPoblacion poblacion;

    public AlgoritmoGenetico(Configuracion configuracion) {
        this(configuracion, new Random(), null);
    }

    public AlgoritmoGenetico(Configuracion configuracion, Random random) {
        this(configuracion, random, null);
    }

    public AlgoritmoGenetico(
            Configuracion configuracion,
            Random random,
            ObservadorEvolucion observador) {
        this.configuracion = configuracion;
        this.random = random;
        this.observador = observador;
    }

    public Individuo ejecutar() {
        poblacion = crearPoblacionInicial();
        poblacion.evaluarTodos(configuracion.getObjetivo());

        for (int generacion = 0; generacion <= configuracion.getMaximoGeneraciones(); generacion++) {
            Individuo mejor = poblacion.obtenerMejor();
            System.out.printf("Generacion %3d: %s%n", generacion, mejor);
            if (observador != null) {
                observador.actualizar(generacion, poblacion, mejor.copiar());
            }

            if (mejor.getResultado() == configuracion.getObjetivo()) {
                return mejor;
            }

            if (generacion < configuracion.getMaximoGeneraciones()) {
                poblacion = crearSiguienteGeneracion();
            }
        }
        return poblacion.obtenerMejor();
    }

    private ListaPoblacion crearPoblacionInicial() {
        ListaPoblacion inicial = new ListaPoblacion();
        for (int i = 0; i < configuracion.getTamanioPoblacion(); i++) {
            ListaOperaciones cromosoma = ListaOperaciones.aleatoria(
                    configuracion.getLongitudCromosoma(), random);
            inicial.agregar(new Individuo(cromosoma));
        }
        return inicial;
    }

    private ListaPoblacion crearSiguienteGeneracion() {
        ListaPoblacion siguiente = new ListaPoblacion();

        // Elitismo: los mejores pasan completos a la siguiente generacion.
        ListaPoblacion elites = poblacion.obtenerMejores(
                configuracion.getCantidadElites());
        siguiente.agregarTodos(elites);

        while (siguiente.getTamanio() < configuracion.getTamanioPoblacion()) {
            Individuo padre = seleccionarPorTorneo();
            Individuo madre = seleccionarPorTorneo();
            ListaOperaciones cromosomaHijo;

            if (random.nextDouble() < configuracion.getProbabilidadCruce()) {
                cromosomaHijo = cruzar(padre.getCromosoma(), madre.getCromosoma());
            } else {
                cromosomaHijo = padre.getCromosoma().copiar();
            }

            mutar(cromosomaHijo);
            Individuo hijo = new Individuo(cromosomaHijo);
            hijo.evaluar(configuracion.getObjetivo());
            siguiente.agregar(hijo);
        }
        return siguiente;
    }

    private Individuo seleccionarPorTorneo() {
        Individuo ganador = null;
        for (int i = 0; i < TAMANIO_TORNEO; i++) {
            int posicion = random.nextInt(poblacion.getTamanio());
            Individuo candidato = poblacion.obtener(posicion);
            if (ganador == null || candidato.getFitness() > ganador.getFitness()) {
                ganador = candidato;
            }
        }
        return ganador;
    }

    private ListaOperaciones cruzar(ListaOperaciones padre, ListaOperaciones madre) {
        int puntoCorte = 1 + random.nextInt(configuracion.getLongitudCromosoma() - 1);
        ListaOperaciones hijo = new ListaOperaciones();

        for (int i = 0; i < configuracion.getLongitudCromosoma(); i++) {
            Operacion operacion = i < puntoCorte
                    ? padre.obtenerOperacion(i)
                    : madre.obtenerOperacion(i);
            hijo.agregar(operacion);
        }
        return hijo;
    }

    private void mutar(ListaOperaciones cromosoma) {
        if (random.nextDouble() >= configuracion.getProbabilidadMutacion()) {
            return;
        }

        int posicion = random.nextInt(cromosoma.getTamanio());
        Operacion anterior = cromosoma.obtenerOperacion(posicion);
        Operacion nueva;
        do {
            nueva = Operacion.aleatoria(random);
        } while (nueva == anterior);
        cromosoma.cambiarOperacion(posicion, nueva);
    }
}
