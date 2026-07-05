package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A diagonal gradient panel with a couple of soft translucent circles for depth. */
public class GradientPanel extends JPanel {

    private final Color from;
    private final Color to;

    public GradientPanel(Color from, Color to) {
        this.from = from;
        this.to = to;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, from, w, h, to));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillOval(w - 160, -80, 260, 260);
        g2.setColor(new Color(255, 255, 255, 16));
        g2.fillOval(-100, h - 180, 260, 260);
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillOval(w / 2 - 60, h / 2 + 40, 180, 180);

        g2.dispose();
    }
}
