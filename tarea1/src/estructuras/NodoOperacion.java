package estructuras;

import modelo.Operacion;

public class NodoOperacion {
    private Operacion operacion;
    private NodoOperacion siguiente;

    public NodoOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public NodoOperacion getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoOperacion siguiente) {
        this.siguiente = siguiente;
    }
}
