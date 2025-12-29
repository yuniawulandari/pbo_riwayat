package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class Coba {
//    public static void main(String[] args) {
//        DbConnection db = new DbConnection();
//        Connection conn = db.getConnection(); 
//        
//        if (conn == null) {
//            System.out.println("Tidak dapat melanjutkan karena koneksi GAGAL.");
//            return; 
//        }
//
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            String sql = "SELECT * FROM orders";
//            stmt = conn.createStatement();
//            rs = stmt.executeQuery(sql);
//
//            while (rs.next()) {
//                int id = rs.getInt("id");
//                String number = rs.getString("order_number");
//                System.out.printf("ID: %d, number: %s%n", id, number);
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Kesalahan saat melakukan query: " + e.getMessage());
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (stmt != null) stmt.close();
//            } catch (SQLException se) {
//                se.printStackTrace();
//            }
//        
//            db.closeConnection();
//        }
//    }
}
