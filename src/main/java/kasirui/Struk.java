/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kasirui;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author ARY DWIS
 */
public class Struk extends javax.swing.JFrame {

    /**
     * Creates new form struk
     */
    static int orderId = 123;
    
    public Struk(int orderId) {
        initComponents();
        this.orderId = orderId;
        loadReceiptData();
    }
    private void exportToPdf() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Simpan Struk sebagai PDF");
    fileChooser.setSelectedFile(new java.io.File("struk_" + orderId + ".pdf"));

    int userSelection = fileChooser.showSaveDialog(this);
    if (userSelection != JFileChooser.APPROVE_OPTION) {
        return;
    }

    java.io.File fileToSave = fileChooser.getSelectedFile();
    String filePath = fileToSave.getAbsolutePath();
    if (!filePath.toLowerCase().endsWith(".pdf")) {
        filePath += ".pdf";
    }

    try {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Judul
        document.add(new Paragraph("MIE GACOGAN")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));

        // Nomor & Info
        document.add(new Paragraph("No. Transaksi: " + jLabel1.getText().replace("#", ""))
                .setFontSize(10));
        document.add(new Paragraph("Pelanggan: " + jLabel2.getText())
                .setFontSize(10));
        document.add(new Paragraph("Tanggal: " + jLabel3.getText())
                .setFontSize(10));
        document.add(new Paragraph("================================")
                .setFontSize(10)
                .setMarginBottom(5));

        // Ambil data item dari database (ulang query)
        try (Connection conn = database.DbConnection.getConnection()) {
            String sql = """
                SELECT p.name, oi.quantity, oi.price
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            // Buat tabel 3 kolom
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Produk")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Qty")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Harga")).setBold());

            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");

                table.addCell(new Cell().add(new Paragraph(name)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(qty))));
                table.addCell(new Cell().add(new Paragraph("Rp " + (int) price)));
            }

            document.add(table);
        }

        // Garis pemisah
        document.add(new Paragraph("================================")
                .setFontSize(10)
                .setMarginTop(10));

        // Total
        document.add(new Paragraph("Subtotal : " + jLabel21.getText()));
        document.add(new Paragraph("Pembayaran : " + jLabel22.getText()));
        document.add(new Paragraph("Kembalian : " + jLabel23.getText()));

        // Penutup
        document.add(new Paragraph("\nTerima kasih atas kunjungan Anda!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontSize(10));

        document.close();

        JOptionPane.showMessageDialog(this, "Struk berhasil disimpan ke:\n" + filePath, "Sukses", JOptionPane.INFORMATION_MESSAGE);

    } catch (FileNotFoundException e) {
        JOptionPane.showMessageDialog(this, "File tidak dapat disimpan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal mengambil data item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat membuat PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void loadReceiptData() {
        try (Connection conn = database.DbConnection.getConnection()) {
            // === Ambil data order ===
            String orderSql = "SELECT order_number, name, total_price, amount_payment, created_at FROM orders WHERE id = ?";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                orderStmt.setInt(1, orderId);
                ResultSet orderRs = orderStmt.executeQuery();

                if (orderRs.next()) {
                    String orderNumber = orderRs.getString("order_number");
                    String customerName = orderRs.getString("name");
                    double totalPrice = orderRs.getDouble("total_price");
                    double payment = orderRs.getDouble("amount_payment");
                    Timestamp createdAt = orderRs.getTimestamp("created_at");

                    // Format tanggal Bahasa Indonesia
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", new java.util.Locale("id", "ID"));
                    String formattedDate = sdf.format(createdAt);

                    // Isi ke label
                    jLabel1.setText("#" + orderNumber);
                    jLabel2.setText(customerName);
                    jLabel3.setText(formattedDate);

                    double change = payment - totalPrice;
                    jLabel21.setText("Rp " + (int) totalPrice);
                    jLabel22.setText("Rp " + (int) payment);
                    jLabel23.setText("Rp " + (int) change);
                }
            }

            // === Buat panel untuk daftar item ===
            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            itemsPanel.setBackground(Color.WHITE);

            // === Ambil item transaksi ===
            String itemSql = """
                SELECT p.name, oi.quantity, oi.price
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                """;
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                itemStmt.setInt(1, orderId);
                ResultSet itemRs = itemStmt.executeQuery();

                boolean hasItem = false;
                while (itemRs.next()) {
                    hasItem = true;
                    String productName = itemRs.getString("name");
                    int qty = itemRs.getInt("quantity");
                    double price = itemRs.getDouble("price");

                    // Buat baris item
                    JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                    itemRow.setBackground(Color.WHITE);
                    itemRow.setMaximumSize(new Dimension(400, 25));
                    
                    JLabel nameLbl = new JLabel(String.format("%-25s", productName));
                    JLabel qtyLbl = new JLabel(String.format("%3d", qty));
                    JLabel priceLbl = new JLabel("Rp " + (int) price);
                    
                    // Atur lebar agar alignment rapi
                    nameLbl.setPreferredSize(new Dimension(200, 20));
                    qtyLbl.setPreferredSize(new Dimension(40, 20));
                    priceLbl.setPreferredSize(new Dimension(100, 20));
                    
                    itemRow.add(nameLbl);
                    itemRow.add(qtyLbl);
                    itemRow.add(priceLbl);
                    
                    itemsPanel.add(itemRow);
                }

                if (!hasItem) {
                    itemsPanel.add(new JLabel("Tidak ada item."));
                }
            }

            // === Masukkan ke JScrollPane ===
            jScrollPane1.setViewportView(itemsPanel); // ✅ ini yang penting!

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat struk: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("#TR202508039239");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 8, -1, -1));

        jLabel2.setText(" Mande");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 34, -1, -1));

        jLabel3.setText("Minggu, 28 April 2012");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 34, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Detail Transaksi");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 62, -1, -1));
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 256, -1, -1));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("SubTotal :");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Pembayaran :");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("Kembalian :");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel21.setText("Rp. 80.000");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("Rp. 100.000");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("Rp. 20.000");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20))
                .addGap(28, 28, 28)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21))
                .addGap(0, 108, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel23))
                .addContainerGap())
        );

        jPanel2.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, -1));
        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 410, 100));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 112, 460, -1));

        jButton1.setBackground(new java.awt.Color(250, 165, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Kembali");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 63, 100, 31));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(250, 165, 51));
        jButton2.setText("Cetak Struk");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 63, -1, 31));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ownerui.Riwayat riwayat = new ownerui.Riwayat();
        riwayat.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
exportToPdf();
    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(Struk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Struk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Struk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Struk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Struk(orderId).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
