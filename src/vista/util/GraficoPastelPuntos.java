package vista.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class GraficoPastelPuntos extends JPanel {

    private int puntosTotales = 0;
    private int puntosConsumo = 0;

    public GraficoPastelPuntos() {
        setOpaque(false);
        setPreferredSize(new Dimension(240, 95));
        setMinimumSize(new Dimension(200, 85));
    }

    public void configurarParaCanje(int totalCliente, int costoRecompensa, String recompensa) {
        this.puntosTotales = Math.max(0, totalCliente);
        this.puntosConsumo = Math.max(0, costoRecompensa);
        repaint();
    }

    public void configurarParaCliente(int totalCliente, int canjeadosHistorico) {
        this.puntosTotales = Math.max(0, totalCliente);
        this.puntosConsumo = Math.max(0, canjeadosHistorico);
        repaint();
    }

    public void limpiar() {
        this.puntosTotales = 0;
        this.puntosConsumo = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (puntosTotales <= 0 && puntosConsumo <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean oscuro = TemaGestor.esModoOscuro();
            Color colorTexto = oscuro ? new Color(248, 250, 252) : new Color(15, 23, 42);
            Color colorMuted = oscuro ? new Color(148, 163, 184) : new Color(100, 116, 139);

            int h = getHeight();
            int diametroExterior = Math.min(84, h - 10);
            int grosorAnillo = 16;
            int diametroInterior = diametroExterior - (grosorAnillo * 2);

            int cx = (diametroExterior / 2) + 12;
            int cy = h / 2;

            int xExt = cx - (diametroExterior / 2);
            int yExt = cy - (diametroExterior / 2);
            int xInt = cx - (diametroInterior / 2);
            int yInt = cy - (diametroInterior / 2);

            Color colorConsumo = new Color(245, 158, 11);
            Color colorRestante = new Color(16, 185, 129);
            Color colorAlerta = new Color(239, 68, 68);

            int legX = xExt + diametroExterior + 16;

            if (puntosConsumo > puntosTotales) {
                Area anillo = new Area(new Ellipse2D.Double(xExt, yExt, diametroExterior, diametroExterior));
                anillo.subtract(new Area(new Ellipse2D.Double(xInt, yInt, diametroInterior, diametroInterior)));
                g2.setColor(colorAlerta);
                g2.fill(anillo);

                int faltan = puntosConsumo - puntosTotales;
                dibujarCentro(g2, cx, cy, "-" + faltan, "faltan", colorAlerta, colorMuted);

                dibujarFila(g2, legX, cy - 14, colorAlerta, "Faltan: " + faltan + " pts", colorTexto);
                dibujarFila(g2, legX, cy + 10, colorMuted, "Costo: " + puntosConsumo + " pts", colorMuted);
            } else {
                double fraccionConsumo = (double) puntosConsumo / (double) Math.max(puntosTotales, 1);
                double anguloConsumo = fraccionConsumo * 360.0;
                double anguloRestante = 360.0 - anguloConsumo;

                Shape arcoConsumo = crearArcoDonut(xExt, yExt, diametroExterior, xInt, yInt, diametroInterior, 90.0, -anguloConsumo);
                g2.setColor(colorConsumo);
                g2.fill(arcoConsumo);

                if (anguloRestante > 0.1) {
                    Shape arcoRestante = crearArcoDonut(xExt, yExt, diametroExterior, xInt, yInt, diametroInterior, 90.0 - anguloConsumo, -anguloRestante);
                    g2.setColor(colorRestante);
                    g2.fill(arcoRestante);
                }

                int restantes = puntosTotales - puntosConsumo;
                dibujarCentro(g2, cx, cy, String.valueOf(restantes), "quedan", colorRestante, colorMuted);

                dibujarFila(g2, legX, cy - 14, colorConsumo, "Canje: " + puntosConsumo + " pts", colorTexto);
                dibujarFila(g2, legX, cy + 10, colorRestante, "Quedan: " + restantes + " pts", colorTexto);
            }

        } finally {
            g2.dispose();
        }
    }

    private Shape crearArcoDonut(double xExt, double yExt, double dExt, double xInt, double yInt, double dInt, double inicio, double extension) {
        Area areaExt = new Area(new Arc2D.Double(xExt, yExt, dExt, dExt, inicio, extension, Arc2D.PIE));
        Area areaInt = new Area(new Ellipse2D.Double(xInt, yInt, dInt, dInt));
        areaExt.subtract(areaInt);
        return areaExt;
    }

    private void dibujarCentro(Graphics2D g2, int cx, int cy, String valor, String sub, Color colorVal, Color colorSub) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fmVal = g2.getFontMetrics();
        int valX = cx - (fmVal.stringWidth(valor) / 2);
        int valY = cy + 1;

        g2.setColor(colorVal);
        g2.drawString(valor, valX, valY);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        FontMetrics fmSub = g2.getFontMetrics();
        int subX = cx - (fmSub.stringWidth(sub) / 2);
        int subY = cy + 12;

        g2.setColor(colorSub);
        g2.drawString(sub, subX, subY);
    }

    private void dibujarFila(Graphics2D g2, int x, int y, Color dotColor, String texto, Color fgTexto) {
        g2.setColor(dotColor);
        g2.fillOval(x, y - 8, 8, 8);

        g2.setColor(fgTexto);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(texto, x + 14, y);
    }
}
