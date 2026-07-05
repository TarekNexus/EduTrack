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

/** A flat, rounded "pill" button with a few pre-set visual styles. */
public class PillButton extends JButton {

    public enum Style { PRIMARY, GHOST, DANGER }

    private final Style style;
    private boolean hover = false;

    public PillButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.FONT_BODY_BOLD);
        setForeground(foregroundFor(style));
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));

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

    private Color foregroundFor(Style style) {
        return switch (style) {
            case PRIMARY -> Color.WHITE;
            case GHOST -> Theme.TEXT_PRIMARY;
            case DANGER -> Color.WHITE;
        };
    }

    private Color backgroundFor(Style style) {
        return switch (style) {
            case PRIMARY -> hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
            case GHOST -> hover ? Theme.CARD_HOVER : Theme.CARD;
            case DANGER -> hover ? Theme.DANGER_HOVER : Theme.DANGER;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundFor(style));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
        if (style == Style.GHOST) {
            g2.setColor(Theme.BORDER);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
