package ownerui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {

    private int cornerRadius = 20;
    private int shadowSize = 8;
    private int shadowOffsetX = 3;
    private int shadowOffsetY = 5;
    private Color shadowColor = new Color(0, 0, 0, 50);

    // --- PROPERTI BARU ---
    private boolean hasShadow = true; // Defaultnya ada shadow
    // --- AKHIR PROPERTI BARU ---

    public RoundedPanel() {
        super();
        setOpaque(false); 
    }

    public RoundedPanel(int radius) {
        super();
        this.cornerRadius = radius;
        setOpaque(false);
    }

    // --- GETTER & SETTER BARU ---
    public boolean isHasShadow() { return hasShadow; }
    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        // Jika shadow dihilangkan, kita perlu atur ulang Insets
        // dan repaint agar margin kembali normal.
        revalidate(); 
        repaint();
    }
    // --- AKHIR GETTER & SETTER BARU ---

    @Override
    public Insets getInsets() {
        // Hanya beri margin untuk shadow JIKA hasShadow adalah true
        if (hasShadow) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        } else {
            return new Insets(0, 0, 0, 0); // Tanpa shadow, tanpa margin
        }
    }

    // Getter & Setter lainnya...
    public int getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(int cornerRadius) { 
        this.cornerRadius = cornerRadius;
        repaint();
    }
    // ... (getter/setter lain untuk shadowSize, shadowOffsetX, dll. jika diperlukan)


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        Insets insets = getInsets(); // Ambil insets yang sudah dinamis

        // Hitung area untuk panel utama (di dalam margin)
        int panelX = insets.left;
        int panelY = insets.top;
        int panelWidth = width - insets.left - insets.right;
        int panelHeight = height - insets.top - insets.bottom;

        // ===========================================
        // 1. GAMBAR BAYANGAN (HANYA JIKA hasShadow true)
        // ===========================================
        if (hasShadow) {
            g2d.setColor(shadowColor);
            g2d.fill(new RoundRectangle2D.Double(
                    panelX + shadowOffsetX, 
                    panelY + shadowOffsetY, 
                    panelWidth, 
                    panelHeight, 
                    cornerRadius, cornerRadius));
        }

        // ===========================================
        // 2. GAMBAR PANEL UTAMA (BACKGROUND)
        // ===========================================
        g2d.setComposite(AlphaComposite.SrcOver); 
        g2d.setColor(getBackground()); // Ini akan mengambil warna yang kamu set (orange, putih, dll.)

        g2d.fill(new RoundRectangle2D.Double(
                panelX, panelY, 
                panelWidth, panelHeight, 
                cornerRadius, cornerRadius));

        // ===========================================
        // 3. SET KLIP & GAMBAR CHILDREN
        // ===========================================
        g2d.setClip(new RoundRectangle2D.Double(
                panelX, panelY, 
                panelWidth, panelHeight, 
                cornerRadius, cornerRadius));

        super.paintComponent(g2d);

        g2d.dispose();
    }
}