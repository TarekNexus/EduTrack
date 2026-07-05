package ui;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** A sidebar navigation item: icon glyph + label, with an active accent bar. */
public class NavButton extends JButton {

    private boolean active = false;
    private boolean hover = false;
    private final boolean indented;

    public NavButton(String icon, String label) {
        this(icon, label, false);
    }

    public NavButton(String icon, String label, boolean indented) {
        super((icon == null ? "" : icon + "   ") + label);
        this.indented = indented;
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(Theme.FONT_NAV);
        setForeground(Theme.SIDEBAR_TEXT_MUTED);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(11, indented ? 44 : 20, 11, 16));
        setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        setAlignmentX(LEFT_ALIGNMENT);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : Theme.SIDEBAR_TEXT_MUTED);
        setFont(active ? Theme.FONT_NAV.deriveFont(java.awt.Font.BOLD) : Theme.FONT_NAV);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (active) {
            g2.setColor(Theme.PRIMARY);
            g2.fill(new RoundRectangle2D.Float(6, 2, getWidth() - 12, getHeight() - 4, 10, 10));
        } else if (hover) {
            g2.setColor(Theme.SIDEBAR_HOVER);
            g2.fill(new RoundRectangle2D.Float(6, 2, getWidth() - 12, getHeight() - 4, 10, 10));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
