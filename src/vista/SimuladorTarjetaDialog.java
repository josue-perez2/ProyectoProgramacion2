package vista;

import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class SimuladorTarjetaDialog extends JDialog {

    private final int idPedido;
    private final BigDecimal totalAPagar;

    private final JTextField txtNumeroTarjeta = FabricaDaisyUI.crearCampoTexto("4532 0000 0000 0000", 19);
    private final JTextField txtVencimiento = FabricaDaisyUI.crearCampoTexto("MM/AA", 6);
    private final JPasswordField txtCvv = new JPasswordField(4);
    private final JTextField txtTitular = FabricaDaisyUI.crearCampoTexto("NOMBRE DEL TITULAR", 20);

    private final JButton btnAprobar = FabricaDaisyUI.crearBotonPrimario("Aprobar Pago", Icons.check(16), e -> simularAprobacion());
    private final JButton btnRechazar = FabricaDaisyUI.crearBotonPeligro("Rechazar Pago", Icons.x(16), e -> simularRechazo());
    private final JButton btnCancelar = FabricaDaisyUI.crearBotonNeutral("Cancelar", Icons.arrowLeft(16), e -> dispose());

    private boolean aprobada = false;
    private String codigoAutorizacion = null;
    private String tarjetaEnmascarada = null;

    public SimuladorTarjetaDialog(Window parent, int idPedido, BigDecimal totalAPagar) {
        super(parent, "Cobro con Tarjeta", ModalityType.APPLICATION_MODAL);
        this.idPedido = idPedido;
        this.totalAPagar = totalAPagar != null ? totalAPagar : BigDecimal.ZERO;

        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 490);
        setMinimumSize(new Dimension(480, 450));
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedor = new JPanel(new BorderLayout(12, 12));
        panelContenedor.setBorder(new EmptyBorder(16, 22, 16, 22));
        panelContenedor.setOpaque(false);

        JPanel panelCabecera = new JPanel(new GridLayout(2, 1, 2, 2));
        panelCabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("Pago con Tarjeta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitulo.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        BigDecimal totalMostrar = totalAPagar.setScale(2, RoundingMode.HALF_UP);
        JLabel lblSubtitulo = new JLabel("Pedido #" + idPedido + "  •  Total: Q" + totalMostrar.toPlainString());
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSubtitulo.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #2563eb; [dark]foreground: #8be9fd");

        panelCabecera.add(lblTitulo);
        panelCabecera.add(lblSubtitulo);
        panelContenedor.add(panelCabecera, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(3, 1, 0, 10));
        panelForm.setOpaque(false);

        ((AbstractDocument) txtNumeroTarjeta.getDocument()).setDocumentFilter(FormatoTexto.filtroTarjeta());
        txtNumeroTarjeta.setPreferredSize(new Dimension(340, 38));

        ((AbstractDocument) txtVencimiento.getDocument()).setDocumentFilter(FormatoTexto.filtroVencimiento());
        txtVencimiento.setPreferredSize(new Dimension(140, 38));

        ((AbstractDocument) txtCvv.getDocument()).setDocumentFilter(FormatoTexto.filtroEnteros(4));
        txtCvv.setPreferredSize(new Dimension(140, 38));
        FabricaDaisyUI.estilizarCampo(txtCvv);

        txtTitular.setPreferredSize(new Dimension(340, 38));

        panelForm.add(FabricaDaisyUI.crearCampoConEtiqueta("Número de Tarjeta:", txtNumeroTarjeta));

        JPanel panelFilaExpiracion = new JPanel(new GridLayout(1, 2, 16, 0));
        panelFilaExpiracion.setOpaque(false);
        panelFilaExpiracion.add(FabricaDaisyUI.crearCampoConEtiqueta("Vencimiento:", txtVencimiento));
        panelFilaExpiracion.add(FabricaDaisyUI.crearCampoConEtiqueta("CVV:", txtCvv));
        panelForm.add(panelFilaExpiracion);

        panelForm.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre del Titular:", txtTitular));

        JPanel tarjetaForm = FabricaDaisyUI.crearTarjetaSeccion("Datos de la Tarjeta", panelForm);
        panelContenedor.add(tarjetaForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 8, 8));
        panelBotones.setOpaque(false);

        JPanel panelAccionesSimuladas = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        panelAccionesSimuladas.setOpaque(false);
        btnAprobar.setPreferredSize(new Dimension(190, 38));
        btnRechazar.setPreferredSize(new Dimension(190, 38));
        panelAccionesSimuladas.add(btnAprobar);
        panelAccionesSimuladas.add(btnRechazar);

        JPanel panelCancelar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelCancelar.setOpaque(false);
        btnCancelar.setPreferredSize(new Dimension(140, 36));
        panelCancelar.add(btnCancelar);

        panelBotones.add(panelAccionesSimuladas);
        panelBotones.add(panelCancelar);

        panelContenedor.add(panelBotones, BorderLayout.SOUTH);

        add(panelContenedor, BorderLayout.CENTER);
    }

    private void simularAprobacion() {
        String numTarjeta = FormatoTexto.soloDigitos(txtNumeroTarjeta.getText());
        if (numTarjeta.length() != 16) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Ingrese los 16 dígitos de la tarjeta.");
            txtNumeroTarjeta.requestFocus();
            return;
        }

        String venc = txtVencimiento.getText().trim();
        if (venc.length() != 5 || !venc.contains("/")) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Fecha de vencimiento inválida (use MM/AA).");
            txtVencimiento.requestFocus();
            return;
        }
        try {
            int mes = Integer.parseInt(venc.substring(0, 2));
            if (mes < 1 || mes > 12) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Ingrese un mes de vencimiento válido (01 al 12).");
                txtVencimiento.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Fecha de vencimiento inválida.");
            return;
        }

        String cvv = new String(txtCvv.getPassword()).trim();
        if (cvv.length() < 3 || cvv.length() > 4) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Ingrese el código CVV (3 o 4 dígitos).");
            txtCvv.requestFocus();
            return;
        }

        String titular = txtTitular.getText().trim();
        if (titular.isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Datos incompletos", "Ingrese el nombre del titular.");
            txtTitular.requestFocus();
            return;
        }

        int rndAuth = 100000 + new Random().nextInt(900000);
        this.codigoAutorizacion = "AUTH-" + rndAuth;
        this.tarjetaEnmascarada = "**** **** **** " + numTarjeta.substring(12);
        this.aprobada = true;

        FabricaDaisyUI.mostrarExito(this,
                "Pago Aprobado",
                "Pago aprobado exitosamente.\n\n"
                        + "Autorización: " + codigoAutorizacion + "\n"
                        + "Tarjeta: " + tarjetaEnmascarada + "\n"
                        + "Titular: " + titular.toUpperCase());

        dispose();
    }

    private void simularRechazo() {
        this.aprobada = false;
        this.codigoAutorizacion = null;
        this.tarjetaEnmascarada = null;

        FabricaDaisyUI.mostrarError(this,
                "Pago Rechazado",
                "El pago fue rechazado por el banco (fondos insuficientes o tarjeta declinada).");

        dispose();
    }

    public boolean isAprobada() {
        return aprobada;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    public String getTarjetaEnmascarada() {
        return tarjetaEnmascarada;
    }
}
