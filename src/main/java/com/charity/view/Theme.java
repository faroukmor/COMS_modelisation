package com.charity.view;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Centralized design tokens: emerald palette, slate neutrals, typography and spacing.
 * Also initializes the FlatLaf look and feel.
 */
public final class Theme {
    // ---- Brand (emerald) ----
    public static final Color GREEN_DARKER = new Color(0x06, 0x4E, 0x3B);   // sidebar / hero
    public static final Color GREEN_DARK = new Color(0x04, 0x78, 0x57);     // #047857
    public static final Color GREEN = new Color(0x05, 0x96, 0x69);          // #059669 primary
    public static final Color GREEN_MAIN = new Color(0x10, 0xB9, 0x81);     // #10B981
    public static final Color GREEN_LIGHT = new Color(0xD1, 0xFA, 0xE5);    // #D1FAE5
    public static final Color GREEN_FAINT = new Color(0xEC, 0xFD, 0xF5);    // #ECFDF5

    // ---- Neutrals (slate) ----
    public static final Color BG = new Color(0xF4, 0xF6, 0xF9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    public static final Color TEXT_FAINT = new Color(0x94, 0xA3, 0xB8);
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    public static final Color DIVIDER = new Color(0xEE, 0xF2, 0xF6);
    public static final Color ROW_ALT = new Color(0xF8, 0xFA, 0xFC);
    public static final Color HOVER = new Color(0xF1, 0xF5, 0xF9);

    // ---- Semantic ----
    public static final Color RED = new Color(0xDC, 0x26, 0x26);
    public static final Color RED_LIGHT = new Color(0xFE, 0xE2, 0xE2);
    public static final Color AMBER = new Color(0xD9, 0x77, 0x06);
    public static final Color AMBER_LIGHT = new Color(0xFE, 0xF3, 0xC7);
    public static final Color BLUE = new Color(0x25, 0x63, 0xEB);
    public static final Color BLUE_LIGHT = new Color(0xDB, 0xEA, 0xFE);
    public static final Color SLATE_LIGHT = new Color(0xE2, 0xE8, 0xF0);

    // ---- Radii / spacing ----
    public static final int RADIUS = 14;
    public static final int RADIUS_SMALL = 10;

    private static final String FAMILY = pickFamily();

    // ---- Typography ----
    public static final Font TITLE_FONT = font(Font.BOLD, 28);
    public static final Font H1_FONT = font(Font.BOLD, 24);
    public static final Font H2_FONT = font(Font.BOLD, 18);
    public static final Font H3_FONT = font(Font.BOLD, 14);
    public static final Font BODY_FONT = font(Font.PLAIN, 14);
    public static final Font BODY_SMALL = font(Font.PLAIN, 12);
    public static final Font LABEL_FONT = font(Font.BOLD, 12);
    public static final Font BUTTON_FONT = font(Font.BOLD, 13);
    public static final Font TABLE_HEADER = font(Font.BOLD, 12);
    public static final Font TABLE_ROW = font(Font.PLAIN, 13);
    public static final Font CARD_VALUE = font(Font.BOLD, 26);
    public static final Font CARD_LABEL = font(Font.PLAIN, 13);
    public static final Font NAV_FONT = font(Font.PLAIN, 14);

    // ---- Legacy borders (kept for compatibility) ----
    public static final Border CARD_BORDER = BorderFactory.createEmptyBorder(20, 20, 20, 20);
    public static final Border PANEL_EMPTY = BorderFactory.createEmptyBorder(24, 28, 24, 28);

    private Theme() {
    }

    /** Creates a font in the best available UI family. */
    public static Font font(int style, int size) {
        return new Font(FAMILY, style, size);
    }

    private static String pickFamily() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String candidate : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue",
                "Noto Sans", "Roboto", "Ubuntu", "Cantarell", "DejaVu Sans", "Arial"}) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Returns the colour with the given alpha (0-255). */
    public static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * Initializes FlatLaf with the charity theme. Call once before creating UI.
     */
    public static void init() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FlatLaf theme", e);
        }
        UIManager.put("defaultFont", BODY_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusColor", GREEN);
        UIManager.put("Component.focusedBorderColor", GREEN);
        UIManager.put("Component.borderColor", new Color(0xCB, 0xD5, 0xE1));
        UIManager.put("TextField.caretForeground", GREEN_DARK);
        UIManager.put("TextComponent.selectionBackground", GREEN_LIGHT);
        UIManager.put("TextComponent.selectionForeground", TEXT_DARK);
        UIManager.put("ComboBox.selectionBackground", GREEN_LIGHT);
        UIManager.put("ComboBox.selectionForeground", TEXT_DARK);
        UIManager.put("List.selectionBackground", GREEN_LIGHT);
        UIManager.put("List.selectionForeground", TEXT_DARK);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumb", new Color(0xCB, 0xD5, 0xE1));
        UIManager.put("ScrollBar.hoverThumbColor", new Color(0x94, 0xA3, 0xB8));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("ToolTip.font", BODY_SMALL);
        UIManager.put("TabbedPane.selectedBackground", SURFACE);
    }
}
