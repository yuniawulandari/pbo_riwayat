package controller;

import database.DbConnection;
import amodels.Order;
import amodels.OrderStatus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class OrderController {

    private DbConnection dbConnection;

    public OrderController(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<Order> getAllPendingOrders() {
        List<Order> orderList = new ArrayList<>();
        
        String sql = "SELECT * FROM orders WHERE status = 'pending' ORDER BY created_at DESC";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order();
                
                order.setId(rs.getInt("id"));
                order.setOrderNumber(rs.getString("order_number"));
                order.setTotalPrice(rs.getDouble("total_price"));
                order.setAmountPayment(rs.getDouble("amount_payment"));
                order.setStatus(OrderStatus.fromString(rs.getString("status")));
                order.setNotes(rs.getString("notes"));
                order.setReceipt(rs.getString("receipt"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                order.setUpdatedAt(rs.getTimestamp("updated_at"));

                orderList.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil data order: " + e.getMessage());
            e.printStackTrace();
        }

        return orderList;
    }

    //methode lain
}