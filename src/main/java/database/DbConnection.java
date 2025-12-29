package database;
import java.sql.*;

public class DbConnection {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3308/kasir_pbo", "root", "");
    }
}