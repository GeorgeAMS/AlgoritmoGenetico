package genetico;

public class Configuracion {
    private final int objetivo;
    private final int longitudCromosoma;
    private final int tamanioPoblacion;
    private final int maximoGeneraciones;
    private final double probabilidadCruce;
    private final double probabilidadMutacion;
    private final int cantidadElites;

    public Configuracion(
            int objetivo,
            int longitudCromosoma,
            int tamanioPoblacion,
            int maximoGeneraciones,
            double probabilidadCruce,
            double probabilidadMutacion,
            int cantidadElites) {
        if (longitudCromosoma < 2 || tamanioPoblacion < 2 || maximoGeneraciones < 1) {
            throw new IllegalArgumentException("Longitud, poblacion o generaciones invalidas");
        }
        if (probabilidadCruce < 0 || probabilidadCruce > 1
                || probabilidadMutacion < 0 || probabilidadMutacion > 1) {
            throw new IllegalArgumentException("Las probabilidades deben estar entre 0 y 1");
        }
        if (cantidadElites < 0 || cantidadElites >= tamanioPoblacion) {
            throw new IllegalArgumentException(
                    "El elitismo debe estar entre 0 y poblacion - 1");
        }
        this.objetivo = objetivo;
        this.longitudCromosoma = longitudCromosoma;
        this.tamanioPoblacion = tamanioPoblacion;
        this.maximoGeneraciones = maximoGeneraciones;
        this.probabilidadCruce = probabilidadCruce;
        this.probabilidadMutacion = probabilidadMutacion;
        this.cantidadElites = cantidadElites;
    }

    public int getObjetivo() {
        return objetivo;
    }

    public int getLongitudCromosoma() {
        return longitudCromosoma;
    }

    public int getTamanioPoblacion() {
        return tamanioPoblacion;
    }

    public int getMaximoGeneraciones() {
        return maximoGeneraciones;
    }

    public double getProbabilidadCruce() {
        return probabilidadCruce;
    }

    public double getProbabilidadMutacion() {
        return probabilidadMutacion;
    }

    public int getCantidadElites() {
        return cantidadElites;
    }
}
