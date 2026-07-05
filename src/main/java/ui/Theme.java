package ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/** Central light/blue "professional" palette and typography (EduTrack look). */
public final class Theme {

    public static final Color BG = new Color(0xF3F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color CARD_HOVER = new Color(0xF1F5F9);
    public static final Color BORDER = new Color(0xE2E8F0);

    public static final Color SIDEBAR = new Color(0x111827);
    public static final Color SIDEBAR_HOVER = new Color(0x1F2937);
    public static final Color SIDEBAR_TEXT = new Color(0xE5E7EB);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x9CA3AF);

    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_HOVER = new Color(0xB91C1C);
    public static final Color WARNING = new Color(0xD97706);

    public static final Color TEXT_PRIMARY = new Color(0x111827);
    public static final Color TEXT_MUTED = new Color(0x6B7280);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_NAV = new Font("Segoe UI", Font.PLAIN, 14);

    private Theme() {
    }

    public static void install() {
        FlatLightLaf.setup();

        UIManager.put("@accentColor", toHex(PRIMARY));
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Component.focusedBorderColor", PRIMARY);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 12);
        UIManager.put("ProgressBar.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 12);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.background", BG);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 40);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 28));
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TableHeader.background", new Color(0xF8FAFC));
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("defaultFont", FONT_BODY);
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
