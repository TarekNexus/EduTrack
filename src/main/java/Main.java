import dao.AdminDAO;
import database.DBConnection;
import ui.Login;
import ui.RoundedPanel;
import ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            connectAndLaunch();
        });
    }

    private static void connectAndLaunch() {
        JWindow splash = buildSplash();
        splash.setVisible(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBConnection.initSchema();
                new AdminDAO().ensureSeedAdmin();
                return null;
            }

            @Override
            protected void done() {
                splash.dispose();
                try {
                    get();
                    new Login().setVisible(true);
                } catch (Exception ex) {
                    showFatalError(ex);
                }
            }
        }.execute();
    }

    private static JWindow buildSplash() {
        JWindow window = new JWindow();
        RoundedPanel panel = new RoundedPanel(18, Theme.CARD, Theme.BORDER);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setPreferredSize(new Dimension(340, 140));

        JLabel title = new JLabel("Connecting to Neon…", JLabel.CENTER);
        title.setFont(Theme.FONT_SECTION);
        title.setForeground(Theme.TEXT_PRIMARY);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setForeground(Theme.PRIMARY);

        panel.add(title, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.CENTER);

        window.setContentPane(panel);
        window.setBackground(new java.awt.Color(0, 0, 0, 0));
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }

    private static void showFatalError(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        JOptionPane.showMessageDialog(null,
            "Could not connect to the Neon database.\n\n" + cause.getMessage() +
                "\n\nCopy src/main/resources/config.properties.example to config.properties\n" +
                "(project root) and fill in your Neon connection string, then restart.",
            "EduTrack — startup failed",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
