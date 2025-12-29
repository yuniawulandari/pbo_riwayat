/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ownerui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
/**
 *
 * @author ARY DWIS
 */
public class Riwayat extends javax.swing.JFrame {

    /**
     * Creates new form coba2
     */
    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;
    private String filterStatus = "all";
    private String startDate = "";
    private String endDate = "";
    public Riwayat() {
        initComponents();
        loadOrders();
        jPanel3.setVisible(false); // atau hapus dari parent
        jPanel3.getParent().remove(jPanel3);
    }
private void loadOrders() {
    panel_isiriwayat.removeAll();
    panel_isiriwayat.setLayout(new BoxLayout(panel_isiriwayat, BoxLayout.Y_AXIS));

    // ✅ Tambahkan header yang sejajar
    panel_isiriwayat.add(createHeaderPanel());

    List<Order> orders = fetchOrdersFromDB();
    if (orders.isEmpty()) {
        panel_isiriwayat.add(new JLabel("Tidak ada data transaksi."));
    } else {
        for (Order order : orders) {
            JPanel row = createOrderRow(order);
            panel_isiriwayat.add(row);
            panel_isiriwayat.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    panel_isiriwayat.revalidate();
    panel_isiriwayat.repaint();

    // Update pagination
    int total = getTotalOrderCount();
    int totalPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
    jLabel36.setText(String.format("Showing %d to %d of %d results",
        (currentPage - 1) * ITEMS_PER_PAGE + 1,
        Math.min(currentPage * ITEMS_PER_PAGE, total),
        total));
}

    private List<Order> fetchOrdersFromDB() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, order_number, total_price, status, created_at FROM orders WHERE 1=1";
        
        if (!filterStatus.equals("all")) {
            sql += " AND status = ?";
        }
        if (!startDate.isEmpty()) {
            sql += " AND created_at >= ?";
        }
        if (!endDate.isEmpty()) {
            sql += " AND created_at <= ?";
        }
        sql += " ORDER BY created_at DESC LIMIT ?, ?";

        try (Connection conn = database.DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (!filterStatus.equals("all")) {
                stmt.setString(paramIndex++, filterStatus);
            }
            if (!startDate.isEmpty()) {
                stmt.setString(paramIndex++, startDate + " 00:00:00");
            }
            if (!endDate.isEmpty()) {
                stmt.setString(paramIndex++, endDate + " 23:59:59");
            }
            stmt.setInt(paramIndex++, (currentPage - 1) * ITEMS_PER_PAGE);
            stmt.setInt(paramIndex++, ITEMS_PER_PAGE);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.id = rs.getInt("id");
                order.orderNumber = rs.getString("order_number");
                order.totalPrice = rs.getDouble("total_price");
                order.status = rs.getString("status");
                order.createdAt = rs.getTimestamp("created_at");
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return orders;
    }

    private int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE 1=1";
        if (!filterStatus.equals("all")) sql += " AND status = ?";
        if (!startDate.isEmpty()) sql += " AND created_at >= ?";
        if (!endDate.isEmpty()) sql += " AND created_at <= ?";

        try (Connection conn = database.DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (!filterStatus.equals("all")) stmt.setString(paramIndex++, filterStatus);
            if (!startDate.isEmpty()) stmt.setString(paramIndex++, startDate + " 00:00:00");
            if (!endDate.isEmpty()) stmt.setString(paramIndex++, endDate + " 23:59:59");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private JPanel createOrderRow(Order order) {
    JPanel row = new JPanel(new GridBagLayout());
    row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.LIGHT_GRAY));
    row.setBackground(java.awt.Color.WHITE);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    String waktuText = sdf.format(order.createdAt);
    String kodeText = order.orderNumber;
    String subtotalText = "Rp " + String.format("%.0f", order.totalPrice);
    String statusText = order.status;

    // Tentukan lebar tetap (dalam piksel)
    int colWaktu = 150;
    int colKode = 120;
    int colSubtotal = 100;
    int colStatus = 80;
    int colAksi = 100; // untuk 2 tombol

    // Kolom 0: Waktu
    gbc.gridx = 0;
    gbc.weightx = 0.0; // non-weighted
    JLabel waktuLabel = new JLabel(waktuText);
    waktuLabel.setPreferredSize(new Dimension(colWaktu, 25));
    row.add(waktuLabel, gbc);

    // Kolom 1: Kode
    gbc.gridx = 1;
    JLabel kodeLabel = new JLabel(kodeText);
    kodeLabel.setPreferredSize(new Dimension(colKode, 25));
    row.add(kodeLabel, gbc);

    // Kolom 2: Subtotal
    gbc.gridx = 2;
    JLabel subtotalLabel = new JLabel(subtotalText);
    subtotalLabel.setPreferredSize(new Dimension(colSubtotal, 25));
    row.add(subtotalLabel, gbc);

    // Kolom 3: Status
    gbc.gridx = 3;
    JLabel statusLabel = new JLabel(statusText);
    statusLabel.setForeground(order.status.equals("success") ? java.awt.Color.GREEN : java.awt.Color.ORANGE);
    statusLabel.setPreferredSize(new Dimension(colStatus, 25));
    row.add(statusLabel, gbc);

    // Kolom 4: Aksi (rata kanan)
    gbc.gridx = 4;
    gbc.anchor = GridBagConstraints.EAST;
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    actionPanel.setOpaque(false);
    JButton viewBtn = new JButton("👁️");
    JButton printBtn = new JButton("🖨️");
    styleActionButton(viewBtn);
    styleActionButton(printBtn);
    int orderId = order.id;
    viewBtn.addActionListener(e -> openReceipt(orderId));
    printBtn.addActionListener(e -> openReceipt(orderId));
    actionPanel.add(viewBtn);
    actionPanel.add(printBtn);
    actionPanel.setPreferredSize(new Dimension(colAksi, 25));
    row.add(actionPanel, gbc);

    // Agar baris mengisi lebar penuh
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    return row;
}

// Helper untuk styling tombol aksi
private void styleActionButton(JButton btn) {
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setFocusable(false);
    btn.setMargin(new Insets(0, 0, 0, 0));
}

    private void openReceipt(int orderId) {
        // Arahkan ke halaman struk (bisa buat kelas baru: StrukFrame)
        StrukOwner struk = new StrukOwner(orderId);
        struk.setVisible(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        btn_produk = new javax.swing.JButton();
        btn_home = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        panel_isiriwayat = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        panel_footer = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        btn_previous = new javax.swing.JButton();
        btn_next = new javax.swing.JButton();
        btn_filter = new javax.swing.JButton();
        btn_download = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 153, 0));

        btn_produk.setBackground(new java.awt.Color(255, 153, 51));
        btn_produk.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btn_produk.setForeground(new java.awt.Color(255, 255, 255));
        btn_produk.setText("Produk");
        btn_produk.setBorder(null);
        btn_produk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_produkActionPerformed(evt);
            }
        });

        btn_home.setBackground(new java.awt.Color(255, 153, 51));
        btn_home.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btn_home.setForeground(new java.awt.Color(255, 255, 255));
        btn_home.setText("Home");
        btn_home.setBorder(null);
        btn_home.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_homeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel18)
                .addGap(55, 55, 55)
                .addComponent(btn_home, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btn_produk, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(474, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_home, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_produk, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBackground(new java.awt.Color(255, 153, 0));

        jLabel1.setFont(new java.awt.Font("ITF Devanagari", 1, 13)); // NOI18N
        jLabel1.setText("Waktu");

        jLabel2.setFont(new java.awt.Font("ITF Devanagari", 1, 13)); // NOI18N
        jLabel2.setText("Kode");

        jLabel3.setFont(new java.awt.Font("ITF Devanagari", 1, 13)); // NOI18N
        jLabel3.setText("Subtotal");

        jLabel4.setFont(new java.awt.Font("ITF Devanagari", 1, 13)); // NOI18N
        jLabel4.setText("Status");

        jLabel5.setFont(new java.awt.Font("ITF Devanagari", 1, 13)); // NOI18N
        jLabel5.setText("Aksi");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(107, 107, 107)
                .addComponent(jLabel2)
                .addGap(103, 103, 103)
                .addComponent(jLabel3)
                .addGap(79, 79, 79)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 169, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(38, 38, 38))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_isiriwayat.setBackground(new java.awt.Color(255, 255, 255));

        jLabel14.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel14.setText("👁️");

        jLabel15.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel15.setText("10/06/2018 15:00:15");

        jLabel16.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel16.setText("MG20250603");

        jLabel17.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel17.setText("Rp 15.000");

        jLabel34.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel34.setText("Sukses");

        jLabel35.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel35.setText("🖨");

        jLabel6.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel6.setText("👁️");

        jLabel7.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel7.setText("10/06/2018 15:00:15");

        jLabel8.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel8.setText("MG20250603");

        jLabel9.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel9.setText("Rp 15.000");

        jLabel30.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel30.setText("Sukses");

        jLabel31.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel31.setText("🖨");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel7)
                .addGap(36, 36, 36)
                .addComponent(jLabel8)
                .addGap(90, 90, 90)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                .addComponent(jLabel30)
                .addGap(80, 80, 80)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panel_isiriwayatLayout = new javax.swing.GroupLayout(panel_isiriwayat);
        panel_isiriwayat.setLayout(panel_isiriwayatLayout);
        panel_isiriwayatLayout.setHorizontalGroup(
            panel_isiriwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_isiriwayatLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel15)
                .addGap(36, 36, 36)
                .addComponent(jLabel16)
                .addGap(90, 90, 90)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel34)
                .addGap(80, 80, 80)
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_isiriwayatLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panel_isiriwayatLayout.setVerticalGroup(
            panel_isiriwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_isiriwayatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_isiriwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panel_footer.setBackground(new java.awt.Color(255, 153, 0));

        jLabel36.setFont(new java.awt.Font("ITF Devanagari", 0, 13)); // NOI18N
        jLabel36.setText("Showing 1 to 3 result");

        btn_previous.setBackground(new java.awt.Color(255, 102, 0));
        btn_previous.setFont(new java.awt.Font("Gujarati MT", 1, 13)); // NOI18N
        btn_previous.setText("<prev");
        btn_previous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_previousActionPerformed(evt);
            }
        });

        btn_next.setBackground(new java.awt.Color(255, 102, 0));
        btn_next.setFont(new java.awt.Font("Gujarati MT", 1, 13)); // NOI18N
        btn_next.setText("next>");
        btn_next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_footerLayout = new javax.swing.GroupLayout(panel_footer);
        panel_footer.setLayout(panel_footerLayout);
        panel_footerLayout.setHorizontalGroup(
            panel_footerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_footerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_previous, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(91, 91, 91)
                .addComponent(btn_next)
                .addContainerGap())
        );
        panel_footerLayout.setVerticalGroup(
            panel_footerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_footerLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(panel_footerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(btn_previous)
                    .addComponent(btn_next))
                .addContainerGap())
        );

        btn_filter.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_filter.setForeground(new java.awt.Color(255, 153, 0));
        btn_filter.setText("Filter");
        btn_filter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0)));
        btn_filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_filterActionPerformed(evt);
            }
        });

        btn_download.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_download.setForeground(new java.awt.Color(255, 153, 0));
        btn_download.setText("Download");
        btn_download.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0)));
        btn_download.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_downloadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(panel_isiriwayat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 717, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(panel_footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(btn_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(507, 507, 507)
                    .addComponent(btn_download, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_download, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(panel_isiriwayat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(panel_footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_previousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_previousActionPerformed
if (currentPage > 1) {
            currentPage--;
            loadOrders();
        }
    }//GEN-LAST:event_btn_previousActionPerformed

    private void btn_nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nextActionPerformed
        int total = getTotalOrderCount();
        int totalPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            loadOrders();
        }
    }//GEN-LAST:event_btn_nextActionPerformed

    private void btn_homeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_homeActionPerformed
        Owner home = new Owner();
        home.setVisible(true);
        this.dispose();            // TODO add your handling code here:
    }//GEN-LAST:event_btn_homeActionPerformed

    private void btn_filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_filterActionPerformed
String status = (String) JOptionPane.showInputDialog(
            this,
            "Filter berdasarkan status:",
            "Filter",
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"all", "success", "pending"},
            filterStatus
        );

        if (status != null) {
            filterStatus = status;
        }

        // Opsional: tambahkan filter tanggal
        startDate = JOptionPane.showInputDialog(this, "Tanggal mulai (YYYY-MM-DD):", "");
        endDate = JOptionPane.showInputDialog(this, "Tanggal akhir (YYYY-MM-DD):", "");

        currentPage = 1;
        loadOrders();
    }//GEN-LAST:event_btn_filterActionPerformed

    private void btn_produkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_produkActionPerformed
        OwnerMenu menu = new OwnerMenu();
        menu.setVisible(true);
        this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_btn_produkActionPerformed

    private void btn_downloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_downloadActionPerformed
String format = (String) JOptionPane.showInputDialog(
            this,
            "Pilih format download:",
            "Download Riwayat",
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"PDF", "Excel"},
            "PDF"
        );

        if ("PDF".equals(format)) {
            exportToPDF();
        } else if ("Excel".equals(format)) {
            exportToExcel();
        }
    }//GEN-LAST:event_btn_downloadActionPerformed
private void exportToPDF() {
    // Pilih lokasi simpan file
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Simpan Riwayat Transaksi sebagai PDF");
    fileChooser.setSelectedFile(new File("riwayat_transaksi.pdf"));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
    fileChooser.setFileFilter(filter);

    int userSelection = fileChooser.showSaveDialog(this);
    if (userSelection != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = fileChooser.getSelectedFile();
    String filePath = file.getAbsolutePath();
    if (!filePath.toLowerCase().endsWith(".pdf")) {
        filePath += ".pdf";
    }

    try {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Judul laporan
        document.add(new Paragraph("LAPORAN RIWAYAT TRANSAKSI")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));

        // Ambil semua order
        String sql = """
            SELECT o.id, o.order_number, o.name, o.total_price, o.amount_payment, o.status, o.created_at
            FROM orders o
            ORDER BY o.created_at DESC
            """;

        try (Connection conn = database.DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("id");
                String orderNumber = rs.getString("order_number");
                String customer = rs.getString("name");
                double total = rs.getDouble("total_price");
                double payment = rs.getDouble("amount_payment");
                String status = rs.getString("status");
                Timestamp createdAt = rs.getTimestamp("created_at");

                // Format tanggal
                String dateStr = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm", 
                    new java.util.Locale("id", "ID")).format(createdAt);

                // Header transaksi
                document.add(new Paragraph("_transaksi # " + orderNumber)
                        .setBold()
                        .setFontSize(12)
                        .setUnderline()
                        .setMarginTop(10)
                        .setMarginBottom(5));
                document.add(new Paragraph("Pelanggan: " + customer));
                document.add(new Paragraph("Tanggal: " + dateStr));
                document.add(new Paragraph("Status: " + (status.equals("success") ? "Sukses" : "Pending"))
                        .setFontColor(status.equals("success") ? com.itextpdf.kernel.colors.ColorConstants.GREEN 
                                                              : com.itextpdf.kernel.colors.ColorConstants.ORANGE));
                document.add(new Paragraph(""));

                // Ambil item transaksi
                String itemSql = """
                    SELECT p.name AS product_name, oi.quantity, oi.price
                    FROM order_items oi
                    JOIN products p ON oi.product_id = p.id
                    WHERE oi.order_id = ?
                    """;
                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    itemStmt.setInt(1, orderId);
                    ResultSet itemRs = itemStmt.executeQuery();

                    // Tabel item
                    com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(UnitValue.createPercentArray(new float[]{3, 1, 2}))
                            .setWidth(UnitValue.createPercentValue(100))
                            .setMarginBottom(10);

                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Produk")).setBold());
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Qty")).setBold());
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Harga")).setBold());

                    while (itemRs.next()) {
                        String prodName = itemRs.getString("product_name");
                        int qty = itemRs.getInt("quantity");
                        double price = itemRs.getDouble("price");

                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(prodName)));
                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(qty))));
                        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Rp " + (int) price)));
                    }

                    document.add(table);
                }

                // Total & pembayaran
                double change = payment - total;
                document.add(new Paragraph("Subtotal: Rp " + (int) total));
                document.add(new Paragraph("Pembayaran: Rp " + (int) payment));
                document.add(new Paragraph("Kembalian: Rp " + (int) change));
                
            }
        }

        document.close();
        JOptionPane.showMessageDialog(this, "File PDF berhasil disimpan:\n" + filePath, 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal membuat PDF:\n" + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void exportToExcel() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Simpan Riwayat sebagai Excel");
    fileChooser.setSelectedFile(new java.io.File("riwayat_transaksi.xlsx"));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files (.xlsx)", "xlsx");
    fileChooser.setFileFilter(filter);

    int userSelection = fileChooser.showSaveDialog(this);
    if (userSelection != JFileChooser.APPROVE_OPTION) {
        return;
    }

    java.io.File file = fileChooser.getSelectedFile();
    String filePath = file.getAbsolutePath();
    if (!filePath.toLowerCase().endsWith(".xlsx")) {
        filePath += ".xlsx";
    }

    try (Workbook workbook = new XSSFWorkbook()) {
        // === Sheet 1: Daftar Transaksi (orders) ===
        Sheet orderSheet = workbook.createSheet("Transaksi");
        Row headerRow = orderSheet.createRow(0);

        String[] headers = {"ID", "No. Transaksi", "Pelanggan", "Total", "Pembayaran", "Status", "Tanggal"};
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Ambil data orders
        String orderSql = """
            SELECT id, order_number, name, total_price, amount_payment, status, created_at
            FROM orders
            ORDER BY created_at DESC
            """;

        int rowNum = 1;
        try (Connection conn = database.DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(orderSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Row row = orderSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getInt("id"));
                row.createCell(1).setCellValue(rs.getString("order_number"));
                row.createCell(2).setCellValue(rs.getString("name"));
                row.createCell(3).setCellValue(rs.getDouble("total_price"));
                row.createCell(4).setCellValue(rs.getDouble("amount_payment"));
                row.createCell(5).setCellValue(rs.getString("status"));
                row.createCell(6).setCellValue(rs.getTimestamp("created_at").toString());
            }
        }

        // Atur lebar kolom otomatis
        for (int i = 0; i < headers.length; i++) {
            orderSheet.autoSizeColumn(i);
        }

        // === Sheet 2: Detail Item Transaksi ===
        Sheet itemSheet = workbook.createSheet("Detail Item");
        Row itemHeaderRow = itemSheet.createRow(0);
        String[] itemHeaders = {"ID Transaksi", "No. Transaksi", "Produk", "Qty", "Harga Satuan", "Subtotal"};
        
        for (int i = 0; i < itemHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = itemHeaderRow.createCell(i);
            cell.setCellValue(itemHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // Ambil data item
        String itemSql = """
            SELECT o.id AS order_id, o.order_number, p.name AS product_name, 
                   oi.quantity, oi.price, (oi.quantity * oi.price) AS subtotal
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            JOIN products p ON oi.product_id = p.id
            ORDER BY o.created_at DESC, o.id
            """;

        int itemRowNum = 1;
        try (Connection conn = database.DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(itemSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Row row = itemSheet.createRow(itemRowNum++);
                row.createCell(0).setCellValue(rs.getInt("order_id"));
                row.createCell(1).setCellValue(rs.getString("order_number"));
                row.createCell(2).setCellValue(rs.getString("product_name"));
                row.createCell(3).setCellValue(rs.getInt("quantity"));
                row.createCell(4).setCellValue(rs.getDouble("price"));
                row.createCell(5).setCellValue(rs.getDouble("subtotal"));
            }
        }

        for (int i = 0; i < itemHeaders.length; i++) {
            itemSheet.autoSizeColumn(i);
        }

        // Simpan file
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }

        JOptionPane.showMessageDialog(this, "File Excel berhasil disimpan:\n" + filePath,
                "Sukses", JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal mengambil data dari database: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal menyimpan file Excel: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    private JPanel createHeaderPanel() {
    JPanel header = new JPanel(new GridBagLayout());
    header.setBackground(new java.awt.Color(255, 153, 0));
    header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, java.awt.Color.WHITE));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    int colWaktu = 150;
    int colKode = 120;
    int colSubtotal = 100;
    int colStatus = 80;
    int colAksi = 100;

    java.awt.Font boldFont = new java.awt.Font("ITF Devanagari", java.awt.Font.BOLD, 13);

    gbc.gridx = 0;
    JLabel waktu = new JLabel("Waktu");
    waktu.setFont(boldFont);
    waktu.setPreferredSize(new Dimension(colWaktu, 25));
    header.add(waktu, gbc);

    gbc.gridx = 1;
    JLabel kode = new JLabel("Kode");
    kode.setFont(boldFont);
    kode.setPreferredSize(new Dimension(colKode, 25));
    header.add(kode, gbc);

    gbc.gridx = 2;
    JLabel subtotal = new JLabel("Subtotal");
    subtotal.setFont(boldFont);
    subtotal.setPreferredSize(new Dimension(colSubtotal, 25));
    header.add(subtotal, gbc);

    gbc.gridx = 3;
    JLabel status = new JLabel("Status");
    status.setFont(boldFont);
    status.setPreferredSize(new Dimension(colStatus, 25));
    header.add(status, gbc);

    gbc.gridx = 4;
    gbc.anchor = GridBagConstraints.EAST;
    JLabel aksi = new JLabel("Aksi");
    aksi.setFont(boldFont);
    aksi.setPreferredSize(new Dimension(colAksi, 25));
    header.add(aksi, gbc);

    return header;
}
    private static class Order {
        int id;
        String orderNumber;
        double totalPrice;
        String status;
        Timestamp createdAt;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Riwayat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Riwayat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Riwayat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Riwayat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Riwayat().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_download;
    private javax.swing.JButton btn_filter;
    private javax.swing.JButton btn_home;
    private javax.swing.JButton btn_next;
    private javax.swing.JButton btn_previous;
    private javax.swing.JButton btn_produk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panel_footer;
    private javax.swing.JPanel panel_isiriwayat;
    // End of variables declaration//GEN-END:variables
}
