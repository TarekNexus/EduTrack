package ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A rounded stat card: small colored accent dot, caption, and a big value. */
public class StatCard extends RoundedPanel {

    private final JLabel valueLabel;

    public StatCard(String caption, String value, Color accent) {
        super(14, Theme.CARD, Theme.BORDER);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setPreferredSize(new Dimension(200, 110));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        top.add(new AccentDot(accent), BorderLayout.WEST);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(Theme.FONT_SUBTITLE);
        captionLabel.setForeground(Theme.TEXT_MUTED);
        top.add(captionLabel, BorderLayout.CENTER);

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_STAT);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        add(top, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /** A small filled circle used as a color accent instead of an icon glyph. */
    private static final class AccentDot extends JPanel {
        private final Color color;

        AccentDot(Color color) {
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 3, 10, 10);
            g2.dispose();
        }
    }
}
