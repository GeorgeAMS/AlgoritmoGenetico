package estructuras;

import java.util.Random;
import modelo.Operacion;

public class ListaOperaciones {
    private NodoOperacion cabeza;
    private NodoOperacion cola;
    private int tamanio;

    public void agregar(Operacion operacion) {
        NodoOperacion nuevo = new NodoOperacion(operacion);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamanio++;
    }

    public int ejecutar(int valorInicial) {
        int resultado = valorInicial;
        NodoOperacion actual = cabeza;

        while (actual != null) {
            resultado = actual.getOperacion().aplicar(resultado);
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    public void cambiarOperacion(int posicion, Operacion nuevaOperacion) {
        nodoEn(posicion).setOperacion(nuevaOperacion);
    }

    public Operacion obtenerOperacion(int posicion) {
        return nodoEn(posicion).getOperacion();
    }

    public ListaOperaciones copiar() {
        ListaOperaciones copia = new ListaOperaciones();
        NodoOperacion actual = cabeza;
        while (actual != null) {
            copia.agregar(actual.getOperacion());
            actual = actual.getSiguiente();
        }
        return copia;
    }

    public static ListaOperaciones aleatoria(int longitud, Random random) {
        ListaOperaciones lista = new ListaOperaciones();
        for (int i = 0; i < longitud; i++) {
            lista.agregar(Operacion.aleatoria(random));
        }
        return lista;
    }

    public int getTamanio() {
        return tamanio;
    }

    private NodoOperacion nodoEn(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException("Posicion invalida: " + posicion);
        }

        NodoOperacion actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSiguiente();
        }
        return actual;
    }

    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder();
        NodoOperacion actual = cabeza;

        while (actual != null) {
            texto.append(actual.getOperacion()).append(" -> ");
            actual = actual.getSiguiente();
        }
        return texto.append("null").toString();
    }
}
