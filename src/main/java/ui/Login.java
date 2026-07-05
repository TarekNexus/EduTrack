package ui;

import dao.AdminDAO;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class Login extends javax.swing.JFrame {

    private final AdminDAO adminDao = new AdminDAO();

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final char defaultEchoChar = passwordField.getEchoChar();
    private final JLabel errorLabel = new JLabel(" ");
    private final PillButton loginButton = new PillButton("Login", PillButton.Style.PRIMARY);

    public Login() {
        super("EduTrack — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setSize(940, 580);
        setLocationRelativeTo(null);

        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    private JPanel buildBrandPanel() {
        GradientPanel panel = new GradientPanel(Theme.PRIMARY, new Color(0x1E3A8A));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(56, 44, 44, 44));

        JLabel logo = new JLabel("EduTrack");
        logo.setFont(Theme.FONT_TITLE.deriveFont(Font.BOLD, 28f));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Student Management System");
        subtitle.setFont(Theme.FONT_SUBTITLE.deriveFont(14f));
        subtitle.setForeground(new Color(255, 255, 255, 200));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        panel.add(logo);
        panel.add(subtitle);
        panel.add(Box.createVerticalGlue());

        panel.add(brandBullet("Manage student records in one place"));
        panel.add(Box.createVerticalStrut(14));
        panel.add(brandBullet("Track departments, semesters & CGPA"));
        panel.add(Box.createVerticalStrut(14));
        panel.add(brandBullet("Secure admin access, backed by Neon"));

        panel.add(Box.createVerticalGlue());

        JLabel version = new JLabel("EduTrack v1.0");
        version.setFont(Theme.FONT_SUBTITLE);
        version.setForeground(new Color(255, 255, 255, 130));
        version.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(version);
        panel.add(Box.createVerticalStrut(4));
        panel.add(buildDeveloperCredit());

        return panel;
    }

    private JLabel buildDeveloperCredit() {
        JLabel credit = new JLabel("<html>Developed by <b>Md. Tarek</b></html>");
        credit.setFont(Theme.FONT_SUBTITLE);
        credit.setForeground(new Color(255, 255, 255, 170));
        credit.setAlignmentX(LEFT_ALIGNMENT);
        credit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        credit.setToolTipText("https://tarekdev59.vercel.app/");
        credit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://tarekdev59.vercel.app/"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Login.this,
                        "Visit: https://tarekdev59.vercel.app/", "Developer", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        return credit;
    }

    private JPanel brandBullet(String text) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JPanel dotWrap = new JPanel(new BorderLayout());
        dotWrap.setOpaque(false);
        dotWrap.setPreferredSize(new Dimension(8, 22));
        dotWrap.add(new BulletDot(), BorderLayout.CENTER);

        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY);
        label.setForeground(new Color(255, 255, 255, 225));

        row.add(dotWrap, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    /** A small drawn dot -- avoids relying on any Unicode bullet/checkmark glyph rendering. */
    private static final class BulletDot extends JPanel {
        BulletDot() {
            setOpaque(false);
            setPreferredSize(new Dimension(8, 8));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
            g2.dispose();
        }
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new java.awt.GridBagLayout());
        outer.setBackground(Theme.CARD);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to manage your student records");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 30, 0));

        form.add(title);
        form.add(subtitle);

        form.add(fieldLabel("Username"));
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        styleField(usernameField);
        usernameField.addActionListener(e -> attemptLogin());
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));

        form.add(fieldLabel("Password"));
        form.add(buildPasswordRow());
        form.add(Box.createVerticalStrut(8));

        errorLabel.setFont(Theme.FONT_SUBTITLE);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(10));

        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(320, 46));
        loginButton.setMaximumSize(new Dimension(320, 46));
        loginButton.addActionListener(e -> attemptLogin());
        form.add(loginButton);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(0, 70, 0, 70);
        outer.add(form, gbc);

        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
        return outer;
    }

    private JPanel buildPasswordRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder());

        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        styleField(passwordField);
        passwordField.addActionListener(e -> attemptLogin());

        JLabel toggle = new JLabel("Show");
        toggle.setFont(Theme.FONT_SUBTITLE);
        toggle.setForeground(Theme.PRIMARY);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.setToolTipText("Show/hide password");
        toggle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        toggle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean hidden = passwordField.getEchoChar() != (char) 0;
                passwordField.setEchoChar(hidden ? (char) 0 : defaultEchoChar);
                toggle.setText(hidden ? "Hide" : "Show");
            }
        });

        row.add(passwordField);
        row.add(toggle);
        return row;
    }

    private void styleField(javax.swing.JTextField field) {
        field.setFont(Theme.FONT_BODY);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(320, 38));
        field.setMaximumSize(new Dimension(320, 38));
        field.putClientProperty("JComponent.minimumWidth", 0);
        field.setMargin(new java.awt.Insets(4, 10, 4, 10));
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return label;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return adminDao.verifyLogin(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                try {
                    if (get()) {
                        dispose();
                        new Dashboard(username).setVisible(true);
                    } else {
                        errorLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Login.this,
                        "Could not reach the database:\n" + rootMessage(ex),
                        "Neon connection error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}
