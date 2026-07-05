package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.net.URI;

public class About extends JPanel {

    public About() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        JLabel title = new JLabel("About");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        JLabel appName = new JLabel("EduTrack — Student Management System");
        appName.setFont(Theme.FONT_SECTION);
        appName.setForeground(Theme.TEXT_PRIMARY);
        appName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel version = new JLabel("Version 1.0.0");
        version.setFont(Theme.FONT_SUBTITLE);
        version.setForeground(Theme.TEXT_MUTED);
        version.setAlignmentX(LEFT_ALIGNMENT);

        JLabel description = new JLabel(
            "<html><body style='width:420px'>Built with Java Swing and a Neon " +
            "(serverless PostgreSQL) database. Manage student records, departments, " +
            "and enrollment details from a single desktop dashboard.</body></html>");
        description.setFont(Theme.FONT_BODY);
        description.setForeground(Theme.TEXT_PRIMARY);
        description.setAlignmentX(LEFT_ALIGNMENT);
        description.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JLabel stack = new JLabel(
            "<html><body style='width:420px'>Tech stack: Java 17, Swing, FlatLaf, " +
            "PostgreSQL JDBC driver, Neon.</body></html>");
        stack.setFont(Theme.FONT_SUBTITLE);
        stack.setForeground(Theme.TEXT_MUTED);
        stack.setAlignmentX(LEFT_ALIGNMENT);
        stack.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel developer = new JLabel(
            "<html>Developed by <b>Md. Tarek</b> &mdash; " +
            "<span style='color:#2563EB'>tarekdev59.vercel.app</span></html>");
        developer.setFont(Theme.FONT_BODY);
        developer.setForeground(Theme.TEXT_PRIMARY);
        developer.setAlignmentX(LEFT_ALIGNMENT);
        developer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        developer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        developer.setToolTipText("https://tarekdev59.vercel.app/");
        developer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openPortfolio(developer);
            }
        });

        card.add(appName);
        card.add(Box.createVerticalStrut(4));
        card.add(version);
        card.add(description);
        card.add(stack);
        card.add(developer);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        add(header, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    private void openPortfolio(JLabel anchor) {
        try {
            Desktop.getDesktop().browse(URI.create("https://tarekdev59.vercel.app/"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(anchor,
                "Could not open the link automatically.\nVisit: https://tarekdev59.vercel.app/",
                "Open link", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
