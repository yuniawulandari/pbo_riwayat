package controller;

import database.DbConnection;
import amodels.OrderItem;
import amodels.Product;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class OrderItemController {
    private DbConnection dbConnection;

    public OrderItemController(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    public List<OrderItem> getAllOrderItemById(Integer order_id) {
        List<OrderItem> orderList = new ArrayList<>();
        
//        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        String sql = "SELECT " +
             "oi.id AS order_item_id, oi.order_id, oi.product_id, oi.price, oi.quantity, oi.is_done, oi.created_at AS item_crated_at, oi.updated_at AS item_updated_at" + 
             "p.id AS product_id, p.name, p.description, p.foto, p.category, p.is_active " +
             "FROM order_items oi " +
             "INNER JOIN products p ON oi.product_id = p.id " +
             "WHERE oi.order_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql); ){
            
            pstmt.setInt(1, order_id);
            
            try ( ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    Product product = new Product();
                    product.setId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
//                    product.setPrice(rs.getDouble("price"));
                    product.setDescription(rs.getString("description"));
                    product.setFoto(rs.getString("foto"));
                    
                    OrderItem item = new OrderItem();

                    item.setId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setPrice(rs.getDouble("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setDone(rs.getBoolean("is_done"));
                    item.setCreatedAt(rs.getTimestamp("item_created_at"));
                    item.setUpdatedAt(rs.getTimestamp("item_updated_at"));
                    
                    item.setProduct(product);

                    orderList.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil data order: " + e.getMessage());
            e.printStackTrace();
        }

        return orderList;
    }
    
}
