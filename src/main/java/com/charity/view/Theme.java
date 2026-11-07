package com.charity.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Centralized theme constants: charitable green palette, typography, and borders.
 * Initializes the FlatLaf LookAndFeel.
 */
public final class Theme {
    // ---- Palette (charitable green) ----
    public static final Color GREEN_DARK = new Color(27, 94, 32);      // #1B5E20
    public static final Color GREEN = new Color(46, 125, 50);          // #2E7D32
    public static final Color GREEN_MAIN = new Color(67, 160, 71);     // #43A047
    public static final Color GREEN_LIGHT = new Color(232, 245, 233);  // #E8F5E9
    public static final Color GREEN_FAINT = new Color(244, 249, 245);  // #F4F9F5

    public static final Color BG = new Color(245, 247, 250);           // #F5F7FA
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(33, 33, 33);       // #212121
    public static final Color TEXT_MUTED = new Color(102, 102, 102);   // #666666
    public static final Color BORDER = new Color(222, 226, 230);       // #DEE2E6
    public static final Color DIVIDER = new Color(209, 213, 218);      // #D1D5DA

    public static final Color RED = new Color(192, 57, 43);            // #C0392B
    public static final Color AMBER = new Color(211, 143, 24);         // #D38F18
    public static final Color BLUE = new Color(33, 113, 181);          // #2171B5

    // ---- Typography ----
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font H2_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font H3_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TABLE_ROW = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font CARD_VALUE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font CARD_LABEL = new Font("Segoe UI", Font.BOLD, 13);

    // ---- Borders ----
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16));
    public static final Border FIELD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10));
    public static final Border FIELD_FOCUS = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GREEN, 2),
            BorderFactory.createEmptyBorder(7, 9, 7, 9));
    public static final Border PANEL_EMPTY = BorderFactory.createEmptyBorder(20, 20, 20, 20);

    private Theme() {
    }

    /**
     * Initializes FlatLaf with the charitable theme. Call once before creating UI.
     */
    public static void init() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("defaultFont", Theme.BODY_FONT);
            UIManager.put("Button.font", Theme.BUTTON_FONT);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("Button.arc", 8);
            UIManager.put("SelectionBadge.arc", 8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FlatLaf theme", e);
        }
    }
}

