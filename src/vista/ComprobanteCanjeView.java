package vista;

import model.Canjes;
import model.Cliente;
import model.Recompensas;
import service.CanjesService;
import service.ClienteService;
import service.RecompensaService;
import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComprobanteCanjeView extends JDialog {

    private final CanjesService canjesService;
    private final ClienteService clienteService;
    private final RecompensaService recompensaService;

    private Canjes canje;
    private Recompensas recompensa;
    private Cliente cliente;
    private Integer saldoAnterior;
    private Integer saldoNuevo;

    private final PanelTicketCanje panelTicket;
    private final JButton btnImprimir = new JButton("Imprimir", Icons.receipt(16));
    private final JButton btnGuardar = new JButton("Guardar Imagen", Icons.save(16));
    private final JButton btnCerrar = new JButton("Volver", Icons.arrowLeft(16));

    private final DateTimeFormatter formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public ComprobanteCanjeView(Window parent, int idCanje) {
        super(parent, "Comprobante de Canje #" + idCanje, ModalityType.APPLICATION_MODAL);
        this.canjesService = new CanjesService();
        this.clienteService = new ClienteService();
        this.recompensaService = new RecompensaService();
        this.panelTicket = new PanelTicketCanje();

        cargarDatosPorId(idCanje);
        iniciarComponentes();
    }

    public ComprobanteCanjeView(Window parent, Canjes canje, Recompensas recompensa, Cliente cliente, Integer saldoAnterior, Integer saldoNuevo) {
        super(parent, "Comprobante de Canje #" + (canje != null ? canje.getIdCan() : 0), ModalityType.APPLICATION_MODAL);
        this.canjesService = new CanjesService();
        this.clienteService = new ClienteService();
        this.recompensaService = new RecompensaService();
        this.canje = canje;
        this.recompensa = recompensa;
        this.cliente = cliente;
        this.saldoAnterior = saldoAnterior;
        this.saldoNuevo = saldoNuevo;
        this.panelTicket = new PanelTicketCanje();

        iniciarComponentes();
        this.panelTicket.cargarDatos(canje, recompensa, cliente, saldoAnterior, saldoNuevo);
    }

    private void cargarDatosPorId(int idCanje) {
        this.canje = canjesService.buscarPorId(idCanje);
        if (this.canje != null) {
            this.cliente = clienteService.buscarClientePorId(canje.getIdCliCan());
            for (Recompensas r : recompensaService.listar()) {
                if (r.getIdRec() == canje.getIdRecCan()) {
                    this.recompensa = r;
                    break;
                }
            }
            if (this.cliente != null && this.cliente.getSaldoPuntoCli() != null) {
                this.saldoNuevo = this.cliente.getSaldoPuntoCli().intValue();
                this.saldoAnterior = this.saldoNuevo + canje.getPuntosRecompensaCan();
            }
        }
    }

    private void iniciarComponentes() {
        setSize(460, 680);
        setMinimumSize(new Dimension(420, 580));
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).putClientProperty("FlatLaf.style", "[light]background: #f8fafc; [dark]background: #282a36");

        if (this.canje != null && this.panelTicket.getComponentCount() == 0) {
            this.panelTicket.cargarDatos(canje, recompensa, cliente, saldoAnterior, saldoNuevo);
        }

        JScrollPane scroll = new JScrollPane(panelTicket);
        scroll.setBorder(null);
        scroll.getViewport().putClientProperty("FlatLaf.style", "[light]background: #e2e8f0; [dark]background: #282a36");
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        panelBotones.putClientProperty("FlatLaf.style", "[light]border: 1,0,0,0,#cbd5e1; [dark]border: 1,0,0,0,#44475a");

        btnImprimir.setPreferredSize(new Dimension(135, 38));
        btnImprimir.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonSecundario(btnImprimir);
        btnImprimir.addActionListener(e -> imprimirComprobante());

        btnGuardar.setPreferredSize(new Dimension(150, 38));
        btnGuardar.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonPrimario(btnGuardar);
        btnGuardar.addActionListener(e -> guardarComoImagen());

        btnCerrar.setPreferredSize(new Dimension(115, 38));
        btnCerrar.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonNeutral(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnImprimir);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void imprimirComprobante() {
        PrinterJob job = PrinterJob.getPrinterJob();
        int idCan = canje != null ? canje.getIdCan() : 0;
        job.setJobName("Comprobante-Canje-" + idCan);
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double scaleX = pageFormat.getImageableWidth() / panelTicket.getWidth();
            double scale = Math.min(scaleX, 1.0);
            g2.scale(scale, scale);
            panelTicket.printAll(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                FabricaDaisyUI.mostrarToastExito(this, "Comprobante enviado a la impresora con éxito");
            } catch (PrinterException ex) {
                FabricaDaisyUI.mostrarError(this, "Error de Impresión", "No se pudo imprimir: " + ex.getMessage());
            }
        }
    }

    private void guardarComoImagen() {
        int idCan = canje != null ? canje.getIdCan() : 0;
        int w = Math.max(panelTicket.getWidth(), 380);
        int h = Math.max(panelTicket.getHeight(), 480);
        BufferedImage imagen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imagen.createGraphics();
        panelTicket.setSize(w, h);
        panelTicket.paint(g2);
        g2.dispose();

        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(String.format("Comprobante_Canje_CAN_%05d.png", idCan)));
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(imagen, "png", selector.getSelectedFile());
                FabricaDaisyUI.mostrarToastExito(this, "Comprobante guardado exitosamente en:\n" + selector.getSelectedFile().getName());
            } catch (IOException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar imagen: " + ex.getMessage());
            }
        }
    }

    private class PanelTicketCanje extends JPanel {

        public PanelTicketCanje() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(24, 24, 28, 24));
            setBackground(Color.WHITE);
        }

        public void cargarDatos(Canjes c, Recompensas r, Cliente cli, Integer saldoAnt, Integer saldoNue) {
            removeAll();

            Color colorTexto = new Color(15, 23, 42);
            Color colorSecundario = new Color(100, 116, 139);
            Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 17);
            Font fuenteSubtitulo = new Font("Segoe UI", Font.PLAIN, 11);
            Font fuenteMono = new Font("Monospaced", Font.PLAIN, 12);
            Font fuenteMonoBold = new Font("Monospaced", Font.BOLD, 12);

            JLabel lblNombre = new JLabel("PAN, PUNTOS Y PREMIOS", SwingConstants.CENTER);
            lblNombre.setFont(fuenteTitulo);
            lblNombre.setForeground(colorTexto);
            lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblNombre);

            add(crearSeparadorDoble());

            JLabel lblTipoDoc = new JLabel("COMPROBANTE DE CANJE DE PUNTOS", SwingConstants.CENTER);
            lblTipoDoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTipoDoc.setForeground(colorTexto);
            lblTipoDoc.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblTipoDoc);

            int idCan = c != null ? c.getIdCan() : 0;
            JLabel lblNumCanje = new JLabel(String.format("No. Canje: #CAN-%05d", idCan), SwingConstants.CENTER);
            lblNumCanje.setFont(fuenteMonoBold);
            lblNumCanje.setForeground(colorTexto);
            lblNumCanje.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblNumCanje);

            String fechaStr = (c != null && c.getFechaCan() != null) ? c.getFechaCan().format(formateador) : LocalDateTime.now().format(formateador);
            JLabel lblFecha = new JLabel("Fecha: " + fechaStr, SwingConstants.CENTER);
            lblFecha.setFont(fuenteSubtitulo);
            lblFecha.setForeground(colorSecundario);
            lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblFecha);

            add(crearSeparadorSimple());

            String nombreCli = (cli != null && cli.getNombreCli() != null) ? cli.getNombreCli() : "Cliente General";
            String dpiCli = (cli != null && cli.getDpiCli() != null && !cli.getDpiCli().isBlank()) ? FormatoTexto.formatearDpi(cli.getDpiCli()) : "S/D";

            add(crearFilaTexto("CLIENTE:", nombreCli, fuenteMonoBold, colorTexto));
            add(crearFilaTexto("DPI:", dpiCli, fuenteMono, colorTexto));

            add(crearSeparadorSimple());

            String nomRec = (r != null && r.getNombreRec() != null) ? r.getNombreRec() : "Recompensa Canjeada";
            int pts = (c != null) ? c.getPuntosRecompensaCan() : (r != null ? r.getPuntosRequeridosRec() : 0);

            add(crearFilaTexto("RECOMPENSA:", nomRec, fuenteMonoBold, colorTexto));
            add(crearFilaTexto("PUNTOS CANJEADOS:", "-" + pts + " pts", fuenteMonoBold, new Color(220, 38, 38)));

            add(crearSeparadorSimple());

            if (saldoAnt != null && saldoNue != null) {
                add(crearFilaTexto("SALDO ANTERIOR:", saldoAnt + " pts", fuenteMono, colorTexto));
                add(crearFilaTexto("SALDO RESTANTE:", saldoNue + " pts", fuenteMonoBold, new Color(16, 185, 129)));
            } else if (cli != null && cli.getSaldoPuntoCli() != null) {
                add(crearFilaTexto("SALDO ACTUAL:", cli.getSaldoPuntoCli().intValue() + " pts", fuenteMonoBold, new Color(16, 185, 129)));
            }

            add(crearSeparadorDoble());

            JLabel lblFelicidades = new JLabel("¡Felicidades por su recompensa!", SwingConstants.CENTER);
            lblFelicidades.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFelicidades.setForeground(new Color(16, 185, 129));
            lblFelicidades.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblFelicidades);

            JLabel lblAviso = new JLabel("Conserve este comprobante para cualquier reclamo", SwingConstants.CENTER);
            lblAviso.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblAviso.setForeground(colorSecundario);
            lblAviso.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblAviso);

            revalidate();
            repaint();
        }

        private JComponent crearSeparadorSimple() {
            JPanel p = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawLine(0, 5, getWidth(), 5);
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(340, 12));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            return p;
        }

        private JComponent crearSeparadorDoble() {
            JPanel p = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(148, 163, 184));
                    g2.drawLine(0, 3, getWidth(), 3);
                    g2.drawLine(0, 7, getWidth(), 7);
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(340, 12));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            return p;
        }

        private JPanel crearFilaTexto(String etiqueta, String valor, Font fuente, Color color) {
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

            JLabel lblEti = new JLabel(etiqueta);
            lblEti.setFont(new Font("Monospaced", Font.BOLD, 11));
            lblEti.setForeground(new Color(100, 116, 139));

            JLabel lblVal = new JLabel(valor, SwingConstants.RIGHT);
            lblVal.setFont(fuente);
            lblVal.setForeground(color);

            fila.add(lblEti, BorderLayout.WEST);
            fila.add(lblVal, BorderLayout.EAST);
            return fila;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(226, 232, 240));
            g2.drawRect(0, 0, w - 1, h - 8);

            g2.setColor(new Color(241, 245, 249));
            GeneralPath path = new GeneralPath();
            int diente = 8;
            int yBase = h - diente;
            path.moveTo(0, yBase);
            for (int x = 0; x < w; x += diente * 2) {
                path.lineTo(x + diente, h);
                path.lineTo(x + diente * 2, yBase);
            }
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.closePath();
            g2.fill(path);

            g2.dispose();
        }
    }
}
