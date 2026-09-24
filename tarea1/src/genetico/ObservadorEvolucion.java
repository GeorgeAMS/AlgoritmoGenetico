package genetico;

import estructuras.ListaPoblacion;
import modelo.Individuo;

@FunctionalInterface
public interface ObservadorEvolucion {
    void actualizar(int generacion, ListaPoblacion poblacion, Individuo mejor);
}
