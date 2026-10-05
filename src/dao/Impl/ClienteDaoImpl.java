package dao.Impl;

import config.Conexion;
import dao.ClienteDao;
import model.Cliente;
import util.FormatoTexto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDaoImpl implements ClienteDao {

    private final Conexion conexion;

    private static final String SELECT_BASE =
            "SELECT ID_CLI, DPI_CLI, NOMBRE_CLI, TELEFONO_CLI, CORREO_CLI, DIRECCION_CLI, SALDO_PUNTO_CLI, ESTADO_CLI FROM CLIENTES";

    public ClienteDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = SELECT_BASE;
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return clientes;
    }

    @Override
    public Cliente buscarClientePorId(int id) {
        String sql = SELECT_BASE + " WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void insertar(Cliente cliente) {
        String sql = "INSERT INTO CLIENTES (DPI_CLI, NOMBRE_CLI, TELEFONO_CLI, CORREO_CLI, DIRECCION_CLI, SALDO_PUNTO_CLI, ESTADO_CLI) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getDpiCli());
            ps.setString(2, cliente.getNombreCli());
            ps.setString(3, cliente.getTelefonoCli());
            ps.setString(4, cliente.getCorreoCli());
            ps.setString(5, cliente.getDireccionCli());
            ps.setBigDecimal(6, cliente.getSaldoPuntoCli());
            ps.setString(7, cliente.getEstadoCli());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE CLIENTES SET DPI_CLI = ?, NOMBRE_CLI = ?, TELEFONO_CLI = ?, CORREO_CLI = ?, DIRECCION_CLI = ?, " +
                "SALDO_PUNTO_CLI = ?, ESTADO_CLI = ? WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getDpiCli());
            ps.setString(2, cliente.getNombreCli());
            ps.setString(3, cliente.getTelefonoCli());
            ps.setString(4, cliente.getCorreoCli());
            ps.setString(5, cliente.getDireccionCli());
            ps.setBigDecimal(6, cliente.getSaldoPuntoCli());
            ps.setString(7, cliente.getEstadoCli());
            ps.setInt(8, cliente.getIdCli());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM CLIENTES WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void integridad(int id) {
        String sql = "UPDATE CLIENTES SET ESTADO_CLI = 'I' WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cliente buscarPorDpi(String dpi) {
        if (dpi == null || dpi.trim().isEmpty()) {
            return null;
        }
        String soloDigitos = FormatoTexto.soloDigitos(dpi);
        if (!soloDigitos.isEmpty()) {
            String sql = SELECT_BASE + " WHERE REPLACE(REPLACE(REPLACE(DPI_CLI, ' ', ''), '-', ''), '.', '') = ?";
            Cliente c = consultarUnico(sql, soloDigitos);
            if (c != null) {
                return c;
            }
        }
        String sqlDirecto = SELECT_BASE + " WHERE TRIM(DPI_CLI) = ?";
        return consultarUnico(sqlDirecto, dpi.trim());
    }

    @Override
    public Cliente buscarPorCorreo(String correo) {
        String sql = SELECT_BASE + " WHERE UPPER(CORREO_CLI) = UPPER(?)";
        return consultarUnico(sql, correo);
    }

    @Override
    public Cliente buscarPorDpiYCcorreo(String dpi, String correo) {
        String soloDigitos = FormatoTexto.soloDigitos(dpi);
        String sql = SELECT_BASE + " WHERE (REPLACE(REPLACE(REPLACE(DPI_CLI, ' ', ''), '-', ''), '.', '') = ? OR TRIM(DPI_CLI) = ?) AND UPPER(CORREO_CLI) = UPPER(?)";
        return consultarUnico(sql, soloDigitos, dpi != null ? dpi.trim() : "", correo);
    }

    @Override
    public Cliente buscarPorToken(String token) {
        String sql = "SELECT ID_CLI, DPI_CLI, NOMBRE_CLI, TELEFONO_CLI, CORREO_CLI, DIRECCION_CLI, SALDO_PUNTO_CLI, ESTADO_CLI, PASSWORD_CLI, TOKEN_CLI FROM CLIENTES WHERE TOKEN_CLI = ?";
        try {
            return consultarUnico(sql, token);
        } catch (Exception e) {
            return null;
        }
    }

    private Cliente consultarUnico(String sql, String... valores) {
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setString(i + 1, valores[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void registrarAcceso(int id, String passwordHash) {
        String sql = "UPDATE CLIENTES SET PASSWORD_CLI = ? WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void actualizarToken(int id, String token) {
        String sql = "UPDATE CLIENTES SET TOKEN_CLI = ? WHERE ID_CLI = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCli(rs.getInt("ID_CLI"));
        cliente.setDpiCli(rs.getString("DPI_CLI"));
        cliente.setNombreCli(rs.getString("NOMBRE_CLI"));
        cliente.setTelefonoCli(rs.getString("TELEFONO_CLI"));
        cliente.setCorreoCli(rs.getString("CORREO_CLI"));
        cliente.setDireccionCli(rs.getString("DIRECCION_CLI"));
        cliente.setSaldoPuntoCli(rs.getBigDecimal("SALDO_PUNTO_CLI"));
        cliente.setEstadoCli(rs.getString("ESTADO_CLI"));
        try {
            cliente.setPasswordCli(rs.getString("PASSWORD_CLI"));
        } catch (SQLException ignored) {
        }
        try {
            cliente.setTokenCli(rs.getString("TOKEN_CLI"));
        } catch (SQLException ignored) {
        }
        return cliente;
    }
}