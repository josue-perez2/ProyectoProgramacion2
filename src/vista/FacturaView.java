package vista;

import model.Cliente;
import model.DetallesPedido;
import model.Menus;
import model.Pagos;
import model.Pedidos;
import model.Productos;
import model.Sandwich;
import service.ClienteService;
import service.DetallePedidoService;
import service.MenuService;
import service.PagoService;
import service.PedidoService;
import service.ProductoService;
import service.SandwichService;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacturaView extends JDialog {

    private final PedidoService pedidoService;
    private final PagoService pagoService;
    private final ClienteService clienteService;
    private final DetallePedidoService detallePedidoService;
    private final SandwichService sandwichService;
    private final MenuService menuService;
    private final ProductoService productoService;

    private final int idPedido;
    private final PanelTicket panelTicket;
    private final JButton btnImprimir = new JButton("Imprimir", Icons.receipt(16));
    private final JButton btnGuardar = new JButton("Guardar Imagen", Icons.save(16));
    private final JButton btnCerrar = new JButton("Volver", Icons.arrowLeft(16));

    private final DateTimeFormatter formateador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Map<Integer, String> catalogoSandwich = new HashMap<>();
    private final Map<Integer, String> catalogoMenu = new HashMap<>();
    private final Map<Integer, String> catalogoProducto = new HashMap<>();

    public FacturaView(Window parent, int idPedido) {
        super(parent, "Factura de Pedido #" + idPedido, ModalityType.APPLICATION_MODAL);
        this.idPedido = idPedido;

        this.pedidoService = new PedidoService();
        this.pagoService = new PagoService();
        this.clienteService = new ClienteService();
        this.detallePedidoService = new DetallePedidoService();
        this.sandwichService = new SandwichService();
        this.menuService = new MenuService();
        this.productoService = new ProductoService();

        this.panelTicket = new PanelTicket();

        cargarCatalogos();
        iniciarComponentes();
        construirFactura();
    }

    public FacturaView(Window parent) {
        this(parent, 1);
    }

    private void cargarCatalogos() {
        for (Sandwich s : sandwichService.listar()) {
            catalogoSandwich.put(s.getIdSan(), s.getNombreSan());
        }
        for (Menus m : menuService.listar()) {
            catalogoMenu.put(m.getIdMen(), m.getNombreMen());
        }
        for (Productos p : productoService.listar()) {
            catalogoProducto.put(p.getIdPro(), p.getNombrePro());
        }
    }

    private void iniciarComponentes() {
        setSize(480, 800);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).putClientProperty("FlatLaf.style", "[light]background: #f8fafc; [dark]background: #282a36");

        JScrollPane scroll = new JScrollPane(panelTicket);
        scroll.setBorder(null);
        scroll.getViewport().putClientProperty("FlatLaf.style", "[light]background: #e2e8f0; [dark]background: #282a36");
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panelBotones.putClientProperty("FlatLaf.style", "[light]border: 1,0,0,0,#cbd5e1; [dark]border: 1,0,0,0,#44475a");

        btnImprimir.setPreferredSize(new Dimension(145, 38));
        btnImprimir.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonSecundario(btnImprimir);
        btnImprimir.addActionListener(e -> imprimirFactura());

        btnGuardar.setPreferredSize(new Dimension(145, 38));
        btnGuardar.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonPrimario(btnGuardar);
        btnGuardar.addActionListener(e -> guardarComoImagen());

        btnCerrar.setPreferredSize(new Dimension(145, 38));
        btnCerrar.setIconTextGap(8);
        FabricaDaisyUI.aplicarBotonNeutral(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnImprimir);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void construirFactura() {
        Pedidos pedido = pedidoService.buscarPorId(idPedido);
        if (pedido == null) {
            FabricaDaisyUI.mostrarError(this, "Error", "El pedido #" + idPedido + " no existe.");
            return;
        }

        if (!"C".equalsIgnoreCase(pedido.getEstadoPed())) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El pedido #" + idPedido + " aún no ha sido cobrado. Solo los pedidos pagados cuentan con factura.");
            dispose();
            return;
        }

        Cliente cliente = clienteService.buscarClientePorId(pedido.getIdCliPed());
        Pagos pago = pagoService.buscarPorPedido(idPedido);
        List<DetallesPedido> detalles = detallePedidoService.listarPorPedido(idPedido);

        panelTicket.cargarDatos(pedido, cliente, pago, detalles);
    }

    private void imprimirFactura() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Factura-Pedido-" + idPedido);
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double factorEscala = Math.min(pageFormat.getImageableWidth() / panelTicket.getWidth(), 1.0);
            g2.scale(factorEscala, factorEscala);
            panelTicket.printAll(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
                FabricaDaisyUI.mostrarToastExito(this, "Documento enviado a impresión.");
            } catch (PrinterException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al imprimir: " + ex.getMessage());
            }
        }
    }

    private void guardarComoImagen() {
        BufferedImage imagen = new BufferedImage(panelTicket.getWidth(), panelTicket.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imagen.createGraphics();
        panelTicket.paint(g2);
        g2.dispose();

        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("Factura_Pedido_" + idPedido + ".png"));
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(imagen, "png", selector.getSelectedFile());
                FabricaDaisyUI.mostrarToastExito(this, "Factura guardada exitosamente en:\n" + selector.getSelectedFile().getAbsolutePath());
            } catch (IOException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar la imagen: " + ex.getMessage());
            }
        }
    }

    private class PanelTicket extends JPanel {

        private Pedidos pedido;
        private Cliente cliente;
        private Pagos pago;
        private List<DetallesPedido> detalles;

        public PanelTicket() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(30, 28, 30, 28));
            setBackground(Color.WHITE);
        }

        public void cargarDatos(Pedidos pedido, Cliente cliente, Pagos pago, List<DetallesPedido> detalles) {
            this.pedido = pedido;
            this.cliente = cliente;
            this.pago = pago;
            this.detalles = detalles;

            removeAll();
            armarVistaTicket();
            revalidate();
            repaint();
        }

        private void armarVistaTicket() {
            Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 17);
            Font fuenteSubtitulo = new Font("Segoe UI", Font.PLAIN, 11);
            Font fuenteMono = new Font("Monospaced", Font.PLAIN, 12);
            Font fuenteMonoBold = new Font("Monospaced", Font.BOLD, 12);

            Color colorTexto = new Color(30, 41, 59);
            Color colorSecundario = new Color(100, 116, 139);

            JLabel lblTitulo = new JLabel("COMPROBANTE DE PAGO", SwingConstants.CENTER);
            lblTitulo.setFont(fuenteTitulo);
            lblTitulo.setForeground(colorTexto);
            lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblTitulo);

            JLabel lblSub = new JLabel("DOCUMENTO TRIBUTARIO ELECTRÓNICO", SwingConstants.CENTER);
            lblSub.setFont(fuenteSubtitulo);
            lblSub.setForeground(colorSecundario);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblSub);

            JLabel lblNit = new JLabel("NIT: 8492015-3 | SISTEMA", SwingConstants.CENTER);
            lblNit.setFont(fuenteSubtitulo);
            lblNit.setForeground(colorSecundario);
            lblNit.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblNit);

            JLabel lblDir = new JLabel("Guatemala, Guatemala", SwingConstants.CENTER);
            lblDir.setFont(fuenteSubtitulo);
            lblDir.setForeground(colorSecundario);
            lblDir.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblDir);

            JLabel lblTel = new JLabel("PBX: (502) 2222-2222", SwingConstants.CENTER);
            lblTel.setFont(fuenteSubtitulo);
            lblTel.setForeground(colorSecundario);
            lblTel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblTel);

            add(crearSeparadorDoble());

            JLabel lblTipoDoc = new JLabel("FACTURA ELECTRÓNICA", SwingConstants.CENTER);
            lblTipoDoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTipoDoc.setForeground(colorTexto);
            lblTipoDoc.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblTipoDoc);

            String noFac = String.format("FAC-%06d", pedido.getIdPed());
            JLabel lblSerie = new JLabel("No: " + noFac, SwingConstants.CENTER);
            lblSerie.setFont(fuenteMonoBold);
            lblSerie.setForeground(colorTexto);
            lblSerie.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblSerie);

            String fechaStr = pedido.getFechaPed() != null ? pedido.getFechaPed().format(formateador) : "";
            JLabel lblFecha = new JLabel("Fecha: " + fechaStr, SwingConstants.CENTER);
            lblFecha.setFont(fuenteSubtitulo);
            lblFecha.setForeground(colorSecundario);
            lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblFecha);

            add(crearSeparadorSimple());

            String nombreCli = (cliente != null && cliente.getNombreCli() != null) ? cliente.getNombreCli() : "Consumidor Final";
            String dpiCli = (cliente != null && cliente.getDpiCli() != null && !cliente.getDpiCli().isBlank()) ? cliente.getDpiCli() : "CF";
            String dirCli = (cliente != null && cliente.getDireccionCli() != null) ? cliente.getDireccionCli() : "Ciudad";

            add(crearFilaTexto("CLIENTE:", nombreCli, fuenteMonoBold, colorTexto));
            add(crearFilaTexto("DPI:", dpiCli, fuenteMono, colorTexto));
            add(crearFilaTexto("DIRECCIÓN:", dirCli, fuenteMono, colorTexto));

            add(crearSeparadorSimple());

            add(crearFilaEncabezadoColumnas());
            add(crearSeparadorPunteado());

            if (detalles != null) {
                for (DetallesPedido d : detalles) {
                    String nombreItem = resolverNombreItem(d.getTipoItemDet(), d.getIdItemDet());
                    add(crearFilaItem(d.getCantidadDet(), nombreItem, d.getPrecioUnitarioDet(), d.getSubTotalDet()));
                }
            }

            add(crearSeparadorSimple());

            BigDecimal total = pedido.getTotalPed() != null ? pedido.getTotalPed() : BigDecimal.ZERO;
            add(crearFilaMonto("SUBTOTAL:", total, fuenteMono, colorTexto));
            add(crearFilaMonto("DESCUENTO:", BigDecimal.ZERO, fuenteMono, colorTexto));
            add(crearFilaMonto("TOTAL A PAGAR:", total, new Font("Monospaced", Font.BOLD, 14), new Color(15, 23, 42)));

            add(crearSeparadorSimple());

            String metodoDesc = "PENDIENTE";
            BigDecimal montoRecibido = BigDecimal.ZERO;
            BigDecimal cambio = BigDecimal.ZERO;
            String referencia = "";

            if (pago != null) {
                String met = pago.getMetodoPagoPag();
                if (met != null) {
                    String m = met.toUpperCase().trim();
                    if ("EF".equals(m) || "E".equals(m) || m.contains("EFECTIVO")) {
                        metodoDesc = "EFECTIVO";
                    } else if ("TC".equals(m) || "T".equals(m) || "TJ".equals(m) || m.contains("TARJETA")) {
                        metodoDesc = "TARJETA CRÉDITO/DÉBITO";
                    } else if ("TR".equals(m) || "TF".equals(m) || m.contains("TRANSFER")) {
                        metodoDesc = "TRANSFERENCIA";
                    }
                }
                montoRecibido = pago.getMontoRecibidoPag() != null ? pago.getMontoRecibidoPag() : BigDecimal.ZERO;
                cambio = pago.getCambioPag() != null ? pago.getCambioPag() : BigDecimal.ZERO;
                referencia = pago.getNumeroReferenciaPag() != null ? pago.getNumeroReferenciaPag() : "";
            }

            add(crearFilaTexto("MÉTODO DE PAGO:", metodoDesc, fuenteMonoBold, colorTexto));
            if (pago != null && pago.getMetodoPagoPag() != null) {
                String m = pago.getMetodoPagoPag().toUpperCase().trim();
                if ("EF".equals(m) || "E".equals(m) || m.contains("EFECTIVO")) {
                    add(crearFilaMonto("EFECTIVO:", montoRecibido, fuenteMono, colorTexto));
                    add(crearFilaMonto("CAMBIO:", cambio, fuenteMonoBold, new Color(16, 185, 129)));
                }
            }
            if (!referencia.isBlank()) {
                add(crearFilaTexto("NO. REFERENCIA:", referencia, fuenteMono, colorTexto));
            }

            add(crearSeparadorDoble());

            JPanel panelFidelizacion = new JPanel();
            panelFidelizacion.setLayout(new BoxLayout(panelFidelizacion, BoxLayout.Y_AXIS));
            panelFidelizacion.setBackground(new Color(240, 253, 244));
            panelFidelizacion.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(187, 247, 208), 1),
                    new EmptyBorder(8, 10, 8, 10)
            ));

            JLabel lblPuntosGanados = new JLabel("Puntos obtenidos: +" + pedido.getPuntosObtenidosPed() + " pts", SwingConstants.CENTER);
            lblPuntosGanados.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPuntosGanados.setForeground(new Color(21, 128, 61));
            lblPuntosGanados.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelFidelizacion.add(lblPuntosGanados);

            BigDecimal saldoActual = (cliente != null && cliente.getSaldoPuntoCli() != null) ? cliente.getSaldoPuntoCli() : BigDecimal.ZERO;
            JLabel lblSaldoTotal = new JLabel("Saldo acumulado: " + saldoActual.toPlainString() + " pts", SwingConstants.CENTER);
            lblSaldoTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSaldoTotal.setForeground(new Color(22, 101, 52));
            lblSaldoTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelFidelizacion.add(lblSaldoTotal);

            add(panelFidelizacion);

            add(crearSeparadorSimple());

            JLabel lblGracias = new JLabel("¡GRACIAS POR SU PREFERENCIA!", SwingConstants.CENTER);
            lblGracias.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblGracias.setForeground(colorTexto);
            lblGracias.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblGracias);

            JLabel lblVuelva = new JLabel("Conserve este ticket para futuros canjes", SwingConstants.CENTER);
            lblVuelva.setFont(fuenteSubtitulo);
            lblVuelva.setForeground(colorSecundario);
            lblVuelva.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblVuelva);
        }

        private String resolverNombreItem(String tipo, int idItem) {
            if ("S".equals(tipo)) {
                return catalogoSandwich.getOrDefault(idItem, "Sándwich #" + idItem);
            }
            if ("M".equals(tipo)) {
                return catalogoMenu.getOrDefault(idItem, "Menú Combo #" + idItem);
            }
            return catalogoProducto.getOrDefault(idItem, "Producto #" + idItem);
        }

        private JPanel crearFilaTexto(String etiqueta, String valor, Font fuente, Color color) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setBackground(Color.WHITE);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

            JLabel lblEti = new JLabel(etiqueta);
            lblEti.setFont(fuente);
            lblEti.setForeground(new Color(71, 85, 105));

            JLabel lblVal = new JLabel(valor);
            lblVal.setFont(fuente);
            lblVal.setForeground(color);

            panel.add(lblEti, BorderLayout.WEST);
            panel.add(lblVal, BorderLayout.EAST);
            return panel;
        }

        private JPanel crearFilaMonto(String etiqueta, BigDecimal monto, Font fuente, Color color) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setBackground(Color.WHITE);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            JLabel lblEti = new JLabel(etiqueta);
            lblEti.setFont(fuente);
            lblEti.setForeground(color);

            JLabel lblVal = new JLabel("Q " + monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            lblVal.setFont(fuente);
            lblVal.setForeground(color);

            panel.add(lblEti, BorderLayout.WEST);
            panel.add(lblVal, BorderLayout.EAST);
            return panel;
        }

        private JPanel crearFilaEncabezadoColumnas() {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setBackground(Color.WHITE);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

            JLabel lblIzq = new JLabel("CANT  DESCRIPCIÓN");
            lblIzq.setFont(new Font("Monospaced", Font.BOLD, 11));
            lblIzq.setForeground(new Color(71, 85, 105));

            JLabel lblDer = new JLabel("P.UNIT   TOTAL");
            lblDer.setFont(new Font("Monospaced", Font.BOLD, 11));
            lblDer.setForeground(new Color(71, 85, 105));

            panel.add(lblIzq, BorderLayout.WEST);
            panel.add(lblDer, BorderLayout.EAST);
            return panel;
        }

        private JPanel crearFilaItem(BigDecimal cantidad, String descripcion, BigDecimal precioUnit, BigDecimal subtotal) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setBackground(Color.WHITE);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

            String cantTexto = cantidad.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String descCorta = descripcion.length() > 20 ? descripcion.substring(0, 18) + ".." : descripcion;
            JLabel lblIzq = new JLabel(String.format("%-4s  %-20s", cantTexto, descCorta));
            lblIzq.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lblIzq.setForeground(new Color(30, 41, 59));

            String preciosTexto = String.format("%6.2f %7.2f", precioUnit.doubleValue(), subtotal.doubleValue());
            JLabel lblDer = new JLabel(preciosTexto);
            lblDer.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lblDer.setForeground(new Color(30, 41, 59));

            panel.add(lblIzq, BorderLayout.WEST);
            panel.add(lblDer, BorderLayout.EAST);
            return panel;
        }

        private JLabel crearSeparadorSimple() {
            JLabel lbl = new JLabel("------------------------------------------------", SwingConstants.CENTER);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lbl.setForeground(new Color(148, 163, 184));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            return lbl;
        }

        private JLabel crearSeparadorDoble() {
            JLabel lbl = new JLabel("================================================", SwingConstants.CENTER);
            lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
            lbl.setForeground(new Color(100, 116, 139));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            return lbl;
        }

        private JLabel crearSeparadorPunteado() {
            JLabel lbl = new JLabel("- - - - - - - - - - - - - - - - - - - - - - - - -", SwingConstants.CENTER);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lbl.setForeground(new Color(203, 213, 225));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            return lbl;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color colorFondo = TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(226, 232, 240);
            int dienteAncho = 12;
            int dienteAlto = 6;

            GeneralPath caminoArriba = new GeneralPath();
            caminoArriba.moveTo(0, 0);
            for (int x = 0; x < w; x += dienteAncho) {
                caminoArriba.lineTo(x + dienteAncho / 2.0, dienteAlto);
                caminoArriba.lineTo(x + dienteAncho, 0);
            }
            caminoArriba.closePath();
            g2.setColor(colorFondo);
            g2.fill(caminoArriba);

            GeneralPath caminoAbajo = new GeneralPath();
            caminoAbajo.moveTo(0, h);
            for (int x = 0; x < w; x += dienteAncho) {
                caminoAbajo.lineTo(x + dienteAncho / 2.0, h - dienteAlto);
                caminoAbajo.lineTo(x + dienteAncho, h);
            }
            caminoAbajo.closePath();
            g2.setColor(colorFondo);
            g2.fill(caminoAbajo);

            if (pedido != null) {
                String estado = pedido.getEstadoPed();
                String textoSello = "PAGADO";
                Color colorSello = new Color(16, 185, 129, 210);

                if ("A".equals(estado)) {
                    textoSello = "ANULADO";
                    colorSello = new Color(239, 68, 68, 210);
                } else if ("P".equals(estado)) {
                    textoSello = "PENDIENTE";
                    colorSello = new Color(245, 158, 11, 210);
                }

                AffineTransform original = g2.getTransform();
                g2.translate(w - 128, 110);
                g2.rotate(Math.toRadians(-15));

                g2.setColor(colorSello);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(-10, -18, 114, 32, 8, 8);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString(textoSello, 2, 4);

                g2.setTransform(original);
            }

            g2.dispose();
        }
    }
}
