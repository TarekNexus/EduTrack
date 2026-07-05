package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/** A JPanel with a rounded, filled background (optionally with a thin border). */
public class RoundedPanel extends JPanel {

    private int radius;
    private Color backgroundColor;
    private Color borderColor;

    public RoundedPanel(int radius, Color backgroundColor) {
        this(radius, backgroundColor, null);
    }

    public RoundedPanel(int radius, Color backgroundColor, Color borderColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setCardBackground(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.fill(shape);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.draw(shape);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
