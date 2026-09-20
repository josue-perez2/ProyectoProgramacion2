package config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexion {

    public Connection conectar()  {
        // Si van a cambiar las credenciales para la base de datos no lo suban
        // que cada quiern lo cambie cuando tengan que trabajar
        Connection conexion = null;
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String usuario = "SYSTEM";
        String password = "Administrador1";

        try {
            conexion = DriverManager.getConnection(
                    url,
                    usuario,
                    password
            );
            System.out.println("Si se pudo");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return conexion;
    }

}
