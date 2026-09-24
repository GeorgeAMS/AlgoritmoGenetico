package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import genetico.AlgoritmoGenetico;
import genetico.Configuracion;
import estructuras.ListaPoblacion;
import modelo.Individuo;
import modelo.Operacion;

public class VistaPrincipal extends JFrame {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color PANEL = new Color(30, 41, 59);
    private static final Color AZUL = new Color(56, 189, 248);
    private static final Color VERDE = new Color(52, 211, 153);
    private static final Color TEXTO = new Color(226, 232, 240);

    private final JSpinner campoObjetivo;
    private final JSpinner campoNodos;
    private final JSpinner campoPoblacion;
    private final JSpinner campoGeneraciones;
    private final JSpinner campoCruce;
    private final JSpinner campoMutacion;
    private final JSpinner campoElitismo;
    private final JSpinner campoPausa;
    private final JSpinner[] controlesNumericos;

    private final PanelLista panelLista = new PanelLista();
    private final JLabel valorGeneracion = crearValor("-");
    private final JLabel valorResultado = crearValor("-");
    private final JLabel valorFitness = crearValor("-");
    private final JLabel estado =
            new JLabel("Configura los parámetros y presiona «Iniciar»",
                    SwingConstants.CENTER);
    private final JButton botonIniciar = new JButton("Iniciar evolución");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Generación", "#", "Cadena", "Resultado", "Fitness", "Tipo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaPoblaciones = new JTable(modeloTabla);
    private Configuracion configuracionActual;

    public VistaPrincipal(Configuracion inicial) {
        campoObjetivo = spinner(inicial.getObjetivo(), -100, 1000, 1);
        campoNodos = spinner(inicial.getLongitudCromosoma(), 2, 15, 1);
        campoPoblacion = spinner(inicial.getTamanioPoblacion(), 2, 200, 1);
        campoGeneraciones = spinner(inicial.getMaximoGeneraciones(), 1, 1000, 1);
        campoCruce = spinner(
                (int) Math.round(inicial.getProbabilidadCruce() * 100), 0, 100, 5);
        campoMutacion = spinner(
                (int) Math.round(inicial.getProbabilidadMutacion() * 100), 0, 100, 5);
        campoElitismo = spinner(inicial.getCantidadElites(), 0, 199, 1);
        campoPausa = spinner(250, 0, 2000, 50);
        controlesNumericos = new JSpinner[]{
            campoObjetivo, campoNodos, campoPoblacion, campoGeneraciones,
            campoCruce, campoMutacion, campoElitismo, campoPausa
        };

        setTitle("Algoritmo genético con listas simplemente ligadas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 760));
        construirInterfaz();
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout(14, 14));
        raiz.setBackground(FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        setContentPane(raiz);

        JPanel superior = new JPanel(new BorderLayout(0, 12));
        superior.setOpaque(false);
        JLabel titulo = new JLabel("Algoritmo genético: cadenas enlazadas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        superior.add(titulo, BorderLayout.NORTH);

        JPanel parametros = new JPanel(new GridLayout(1, 8, 8, 0));
        parametros.setOpaque(false);
        parametros.add(crearCampo("OBJETIVO", campoObjetivo, ""));
        parametros.add(crearCampo("NODOS", campoNodos, ""));
        parametros.add(crearCampo("POBLACIÓN", campoPoblacion, ""));
        parametros.add(crearCampo("GENERACIONES", campoGeneraciones, ""));
        parametros.add(crearCampo("CRUCE", campoCruce, "%"));
        parametros.add(crearCampo("MUTACIÓN", campoMutacion, "%"));
        parametros.add(crearCampo("ELITISMO", campoElitismo, ""));
        parametros.add(crearCampo("PAUSA", campoPausa, "ms"));
        superior.add(parametros, BorderLayout.CENTER);
        raiz.add(superior, BorderLayout.NORTH);

        panelLista.setBackground(PANEL);
        panelLista.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                "Mejor cadena de la generación",
                0, 0, new Font("SansSerif", Font.BOLD, 12), TEXTO));

        configurarTabla();
        JScrollPane scrollTabla = new JScrollPane(tablaPoblaciones);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                "Todas las cadenas creadas",
                0, 0, new Font("SansSerif", Font.BOLD, 12), TEXTO));
        scrollTabla.getViewport().setBackground(PANEL);

        JSplitPane divisor = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, panelLista, scrollTabla);
        divisor.setResizeWeight(0.38);
        divisor.setDividerLocation(245);
        divisor.setBorder(null);
        divisor.setBackground(FONDO);
        raiz.add(divisor, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(12, 10));
        inferior.setOpaque(false);
        JPanel metricas = new JPanel(new GridLayout(1, 3, 10, 0));
        metricas.setOpaque(false);
        metricas.add(crearTarjeta("GENERACIÓN ACTUAL", valorGeneracion));
        metricas.add(crearTarjeta("MEJOR RESULTADO", valorResultado));
        metricas.add(crearTarjeta("MEJOR FITNESS", valorFitness));
        inferior.add(metricas, BorderLayout.NORTH);

        estado.setForeground(TEXTO);
        estado.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inferior.add(estado, BorderLayout.CENTER);

        botonIniciar.setFont(new Font("SansSerif", Font.BOLD, 14));
        botonIniciar.setBackground(AZUL);
        botonIniciar.setForeground(new Color(8, 47, 73));
        botonIniciar.setFocusPainted(false);
        botonIniciar.addActionListener(evento -> iniciarEvolucion());
        inferior.add(botonIniciar, BorderLayout.EAST);
        raiz.add(inferior, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        tablaPoblaciones.setBackground(PANEL);
        tablaPoblaciones.setForeground(TEXTO);
        tablaPoblaciones.setGridColor(new Color(51, 65, 85));
        tablaPoblaciones.setSelectionBackground(new Color(12, 74, 110));
        tablaPoblaciones.setSelectionForeground(Color.WHITE);
        tablaPoblaciones.setRowHeight(25);
        tablaPoblaciones.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablaPoblaciones.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tablaPoblaciones.getColumnModel().getColumn(0).setPreferredWidth(75);
        tablaPoblaciones.getColumnModel().getColumn(1).setPreferredWidth(35);
        tablaPoblaciones.getColumnModel().getColumn(2).setPreferredWidth(570);
        tablaPoblaciones.getColumnModel().getColumn(3).setPreferredWidth(70);
        tablaPoblaciones.getColumnModel().getColumn(4).setPreferredWidth(60);
        tablaPoblaciones.getColumnModel().getColumn(5).setPreferredWidth(70);
        tablaPoblaciones.setAutoCreateRowSorter(true);
    }

    private JPanel crearCampo(String nombre, JSpinner control, String unidad) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel etiqueta = new JLabel(nombre, SwingConstants.CENTER);
        etiqueta.setForeground(new Color(148, 163, 184));
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 10));
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        if (!unidad.isEmpty()) {
            JLabel sufijo = new JLabel(unidad);
            sufijo.setForeground(TEXTO);
            sufijo.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            panel.add(sufijo, BorderLayout.EAST);
        }
        return panel;
    }

    private JPanel crearTarjeta(String nombre, JLabel valor) {
        JPanel tarjeta = new JPanel(new GridLayout(2, 1, 0, 2));
        tarjeta.setBackground(PANEL);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        JLabel etiqueta = new JLabel(nombre, SwingConstants.CENTER);
        etiqueta.setForeground(new Color(148, 163, 184));
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 10));
        tarjeta.add(etiqueta);
        tarjeta.add(valor);
        return tarjeta;
    }

    private static JSpinner spinner(int valor, int minimo, int maximo, int paso) {
        return new JSpinner(new SpinnerNumberModel(valor, minimo, maximo, paso));
    }

    private static JLabel crearValor(String texto) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 19));
        return etiqueta;
    }

    private Configuracion leerConfiguracion() {
        return new Configuracion(
                numero(campoObjetivo),
                numero(campoNodos),
                numero(campoPoblacion),
                numero(campoGeneraciones),
                numero(campoCruce) / 100.0,
                numero(campoMutacion) / 100.0,
                numero(campoElitismo));
    }

    private static int numero(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    private void iniciarEvolucion() {
        try {
            configuracionActual = leerConfiguracion();
        } catch (IllegalArgumentException error) {
            estado.setText("Configuración inválida: " + error.getMessage());
            return;
        }

        habilitarControles(false);
        botonIniciar.setText("Evolucionando...");
        estado.setText("Creando la población inicial");
        panelLista.mostrar(null, configuracionActual.getObjetivo());
        modeloTabla.setRowCount(0);
        valorGeneracion.setText("-");
        valorResultado.setText("-");
        valorFitness.setText("-");
        int pausa = numero(campoPausa);

        SwingWorker<Individuo, EstadoGeneracion> trabajador =
                new SwingWorker<Individuo, EstadoGeneracion>() {
                    @Override
                    protected Individuo doInBackground() {
                        AlgoritmoGenetico algoritmo = new AlgoritmoGenetico(
                                configuracionActual,
                                new Random(),
                                (generacion, poblacion, mejor) -> {
                                    publish(new EstadoGeneracion(
                                            generacion, poblacion, mejor));
                                    pausarAnimacion(pausa);
                                });
                        return algoritmo.ejecutar();
                    }

                    @Override
                    protected void process(List<EstadoGeneracion> estados) {
                        for (EstadoGeneracion actual : estados) {
                            actualizarVista(actual);
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            Individuo solucion = get();
                            boolean alcanzo = solucion.getResultado()
                                    == configuracionActual.getObjetivo();
                            estado.setText(alcanzo
                                    ? "¡Objetivo alcanzado! La tabla conserva todas las cadenas."
                                    : "Terminó el límite de generaciones.");
                        } catch (Exception error) {
                            estado.setText("No se pudo ejecutar: " + error.getMessage());
                        }
                        habilitarControles(true);
                        botonIniciar.setText("Ejecutar de nuevo");
                    }
                };
        trabajador.execute();
    }

    private void actualizarVista(EstadoGeneracion actual) {
        valorGeneracion.setText(String.valueOf(actual.generacion));
        valorResultado.setText(String.valueOf(actual.mejor.getResultado()));
        valorFitness.setText(String.valueOf(actual.mejor.getFitness()));
        estado.setText("Generación " + actual.generacion + ": "
                + actual.poblacion.getTamanio() + " cadenas evaluadas");
        panelLista.mostrar(actual.mejor, configuracionActual.getObjetivo());
        agregarPoblacionATabla(actual);
    }

    private void agregarPoblacionATabla(EstadoGeneracion estadoGeneracion) {
        for (int i = 0; i < estadoGeneracion.poblacion.getTamanio(); i++) {
            Individuo individuo = estadoGeneracion.poblacion.obtener(i);
            String tipo;
            if (individuo.getFitness() == estadoGeneracion.mejor.getFitness()) {
                tipo = "MEJOR";
            } else if (estadoGeneracion.generacion > 0
                    && i < configuracionActual.getCantidadElites()) {
                tipo = "ÉLITE";
            } else {
                tipo = "";
            }
            modeloTabla.addRow(new Object[]{
                estadoGeneracion.generacion,
                i + 1,
                individuo.getCromosoma().toString(),
                individuo.getResultado(),
                individuo.getFitness(),
                tipo
            });
        }
        int ultimaFila = modeloTabla.getRowCount() - 1;
        if (ultimaFila >= 0) {
            tablaPoblaciones.scrollRectToVisible(
                    tablaPoblaciones.getCellRect(ultimaFila, 0, true));
        }
    }

    private void habilitarControles(boolean habilitados) {
        for (JSpinner control : controlesNumericos) {
            control.setEnabled(habilitados);
        }
        botonIniciar.setEnabled(habilitados);
    }

    private static void pausarAnimacion(int milisegundos) {
        if (milisegundos <= 0) {
            return;
        }
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static class EstadoGeneracion {
        private final int generacion;
        private final ListaPoblacion poblacion;
        private final Individuo mejor;

        EstadoGeneracion(
                int generacion, ListaPoblacion poblacion, Individuo mejor) {
            this.generacion = generacion;
            this.poblacion = poblacion;
            this.mejor = mejor;
        }
    }

    private static class PanelLista extends JPanel {
        private Individuo individuo;
        private int objetivo;

        PanelLista() {
            setPreferredSize(new Dimension(1000, 225));
        }

        void mostrar(Individuo individuo, int objetivo) {
            this.individuo = individuo;
            this.objetivo = objetivo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graficos) {
            super.paintComponent(graficos);
            Graphics2D g = (Graphics2D) graficos.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (individuo == null) {
                g.setColor(new Color(148, 163, 184));
                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                dibujarCentrado(g, "Aquí aparecerá la mejor lista enlazada",
                        getWidth() / 2, getHeight() / 2);
                g.dispose();
                return;
            }

            int cantidad = individuo.getCromosoma().getTamanio();
            int espacioDisponible = Math.max(500, getWidth() - 110);
            int celda = espacioDisponible / cantidad;
            int anchoNodo = Math.max(45, Math.min(78, celda - 24));
            int separacion = Math.max(18, celda - anchoNodo);
            int anchoTotal = cantidad * (anchoNodo + separacion) + 54;
            int x = Math.max(18, (getWidth() - anchoTotal) / 2);
            int y = 68;
            int valor = 0;

            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(AZUL);
            g.drawString("cabeza", x + 10, y - 20);
            dibujarFlecha(g, x + anchoNodo / 2, y - 15,
                    x + anchoNodo / 2, y - 3);

            for (int i = 0; i < cantidad; i++) {
                Operacion operacion = individuo.getCromosoma().obtenerOperacion(i);
                int nuevoValor = operacion.aplicar(valor);
                boolean solucion = individuo.getResultado() == objetivo;

                g.setColor(solucion ? new Color(6, 95, 70) : new Color(12, 74, 110));
                g.fillRoundRect(x, y, anchoNodo, 68, 16, 16);
                g.setStroke(new BasicStroke(2f));
                g.setColor(solucion ? VERDE : AZUL);
                g.drawRoundRect(x, y, anchoNodo, 68, 16, 16);

                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));
                dibujarCentrado(g, operacion.toString(), x + anchoNodo / 2, y + 28);
                g.setColor(new Color(186, 230, 253));
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                dibujarCentrado(g, valor + " → " + nuevoValor,
                        x + anchoNodo / 2, y + 51);

                dibujarFlecha(g, x + anchoNodo + 3, y + 34,
                        x + anchoNodo + separacion - 5, y + 34);
                valor = nuevoValor;
                x += anchoNodo + separacion;
            }

            g.setColor(new Color(71, 85, 105));
            g.fillRoundRect(x - 5, y + 12, 52, 43, 10, 10);
            g.setColor(TEXTO);
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            dibujarCentrado(g, "null", x + 21, y + 39);
            g.dispose();
        }

        private static void dibujarFlecha(
                Graphics2D g, int x1, int y1, int x2, int y2) {
            g.setColor(new Color(100, 116, 139));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x1, y1, x2, y2);
            if (x1 == x2) {
                g.drawLine(x2, y2, x2 - 4, y2 - 7);
                g.drawLine(x2, y2, x2 + 4, y2 - 7);
            } else {
                g.drawLine(x2, y2, x2 - 7, y2 - 4);
                g.drawLine(x2, y2, x2 - 7, y2 + 4);
            }
        }

        private static void dibujarCentrado(
                Graphics2D g, String texto, int centroX, int baseY) {
            int ancho = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, centroX - ancho / 2, baseY);
        }
    }

    public static void abrir(Configuracion configuracion) {
        SwingUtilities.invokeLater(() -> {
            VistaPrincipal ventana = new VistaPrincipal(configuracion);
            ventana.setVisible(true);
        });
    }
}
