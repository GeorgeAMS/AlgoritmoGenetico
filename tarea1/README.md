# Tarea 1 — Algoritmo genético con listas simplemente ligadas

## Problema

El programa debe encontrar una lista de seis operaciones que convierta el
número `0` en `11`. Cada operación puede ser `+1`, `+2` o `x2`.

Una solución posible es:

```text
0 → +1 → x2 → x2 → +1 → x2 → +1 = 11
```

El algoritmo no recibe esa respuesta: comienza con 40 listas aleatorias y las
mejora mediante selección, cruce, mutación y elitismo.

## Organización

```text
tarea1/
├── README.md
└── src/
    ├── app/
    │   └── Main.java
    ├── estructuras/
    │   ├── ListaOperaciones.java
    │   ├── ListaPoblacion.java
    │   ├── NodoIndividuo.java
    │   └── NodoOperacion.java
    ├── genetico/
    │   ├── AlgoritmoGenetico.java
    │   ├── Configuracion.java
    │   └── ObservadorEvolucion.java
    ├── modelo/
        ├── Individuo.java
        └── Operacion.java
    └── vista/
        └── VistaPrincipal.java
```

`ListaOperaciones` es el cromosoma de un individuo. Sus nodos guardan una
operación y una referencia al nodo siguiente. `ListaPoblacion` enlaza todos los
individuos de una generación. No se utilizan arreglos ni `ArrayList` para
guardar estas listas.

## Funcionamiento del algoritmo

1. Crea 40 individuos aleatorios.
2. Ejecuta la lista de cada individuo desde cero.
3. Calcula su fitness con `100 - |11 - resultado|`.
4. Selecciona padres mediante torneos de tres individuos.
5. Cruza el inicio de un padre con el final del otro.
6. Puede cambiar la operación de un nodo por mutación.
7. Conserva la cantidad de mejores individuos indicada mediante elitismo.
8. Termina cuando una lista produce 11 o alcanza 100 generaciones.

## Compilar y ejecutar

Desde la carpeta `tarea1`, en PowerShell:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out app.Main
```

El segundo comando abre una ventana donde se pueden modificar:

- objetivo;
- cantidad de nodos;
- tamaño de la población;
- límite de generaciones;
- probabilidad de cruce;
- probabilidad de mutación;
- cantidad de individuos conservados por elitismo;
- pausa de la animación.

La parte superior dibuja la mejor lista de la generación. Cada recuadro
representa un nodo, cada flecha es la referencia `siguiente` y el último nodo
apunta a `null`. La tabla inferior conserva todas las cadenas evaluadas en
todas las generaciones, junto con su resultado y fitness.

Para ejecutarlo únicamente en la consola:

```powershell
java -cp out app.Main --consola
```

Se requiere JDK 8 o una versión posterior.
