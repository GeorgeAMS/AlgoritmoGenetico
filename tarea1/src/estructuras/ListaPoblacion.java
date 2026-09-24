package estructuras;

import modelo.Individuo;

public class ListaPoblacion {
    private NodoIndividuo cabeza;
    private NodoIndividuo cola;
    private int tamanio;

    public void agregar(Individuo individuo) {
        NodoIndividuo nuevo = new NodoIndividuo(individuo);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamanio++;
    }

    public Individuo obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException("Posicion invalida: " + posicion);
        }

        NodoIndividuo actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getIndividuo();
    }

    public void evaluarTodos(int objetivo) {
        NodoIndividuo actual = cabeza;
        while (actual != null) {
            actual.getIndividuo().evaluar(objetivo);
            actual = actual.getSiguiente();
        }
    }

    public Individuo obtenerMejor() {
        if (cabeza == null) {
            throw new IllegalStateException("La poblacion esta vacia");
        }

        Individuo mejor = cabeza.getIndividuo();
        NodoIndividuo actual = cabeza.getSiguiente();
        while (actual != null) {
            if (actual.getIndividuo().getFitness() > mejor.getFitness()) {
                mejor = actual.getIndividuo();
            }
            actual = actual.getSiguiente();
        }
        return mejor;
    }

    public ListaPoblacion obtenerMejores(int cantidad) {
        if (cantidad < 0 || cantidad > tamanio) {
            throw new IllegalArgumentException("Cantidad de elites invalida");
        }

        ListaPoblacion ordenada = new ListaPoblacion();
        NodoIndividuo actual = cabeza;
        while (actual != null) {
            ordenada.agregarOrdenado(actual.getIndividuo());
            actual = actual.getSiguiente();
        }

        ListaPoblacion mejores = new ListaPoblacion();
        actual = ordenada.cabeza;
        for (int i = 0; i < cantidad; i++) {
            mejores.agregar(actual.getIndividuo().copiar());
            actual = actual.getSiguiente();
        }
        return mejores;
    }

    public void agregarTodos(ListaPoblacion otra) {
        NodoIndividuo actual = otra.cabeza;
        while (actual != null) {
            agregar(actual.getIndividuo());
            actual = actual.getSiguiente();
        }
    }

    private void agregarOrdenado(Individuo individuo) {
        NodoIndividuo nuevo = new NodoIndividuo(individuo);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else if (individuo.getFitness() > cabeza.getIndividuo().getFitness()) {
            nuevo.setSiguiente(cabeza);
            cabeza = nuevo;
        } else {
            NodoIndividuo actual = cabeza;
            while (actual.getSiguiente() != null
                    && actual.getSiguiente().getIndividuo().getFitness()
                    >= individuo.getFitness()) {
                actual = actual.getSiguiente();
            }
            nuevo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevo);
            if (nuevo.getSiguiente() == null) {
                cola = nuevo;
            }
        }
        tamanio++;
    }

    public int getTamanio() {
        return tamanio;
    }
}
