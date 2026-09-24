package documentacion;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import genetico.Configuracion;
import vista.VistaPrincipal;

public class CapturarVista {
    private static VistaPrincipal ventana;

    public static void main(String[] args) throws Exception {
        String salida = args.length > 0
                ? args[0]
                : "documentacion/vista_aplicacion.png";

        SwingUtilities.invokeAndWait(() -> {
            Configuracion configuracion = new Configuracion(
                    25, 6, 20, 30, 0.80, 0.15, 2);
            ventana = new VistaPrincipal(configuracion);
            ventana.setSize(1120, 760);
            ventana.setVisible(true);
            JButton boton = buscarBoton(ventana.getContentPane());
            if (boton != null) {
                boton.doClick();
            }
        });

        Thread.sleep(1800);

        SwingUtilities.invokeAndWait(() -> {
            try {
                BufferedImage imagen = new BufferedImage(
                        ventana.getWidth(),
                        ventana.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D graficos = imagen.createGraphics();
                ventana.paintAll(graficos);
                graficos.dispose();
                ImageIO.write(imagen, "png", new File(salida));
            } catch (Exception error) {
                throw new RuntimeException(error);
            } finally {
                ventana.dispose();
            }
        });
    }

    private static JButton buscarBoton(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton) {
                JButton boton = (JButton) componente;
                if (boton.getText() != null
                        && boton.getText().startsWith("Iniciar")) {
                    return boton;
                }
            }
            if (componente instanceof Container) {
                JButton encontrado = buscarBoton((Container) componente);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
