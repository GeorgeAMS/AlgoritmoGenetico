package estructuras;

import modelo.Individuo;

public class NodoIndividuo {
    private final Individuo individuo;
    private NodoIndividuo siguiente;

    public NodoIndividuo(Individuo individuo) {
        this.individuo = individuo;
    }

    public Individuo getIndividuo() {
        return individuo;
    }

    public NodoIndividuo getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoIndividuo siguiente) {
        this.siguiente = siguiente;
    }
}
