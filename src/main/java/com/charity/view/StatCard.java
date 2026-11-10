package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Dashboard metric: coloured icon badge, caption and a large value.
 */
public final class StatCard extends UI.Card {
    private static final long serialVersionUID = 1L;
    private final JLabel value = UI.label("-", Theme.CARD_VALUE, Theme.TEXT_DARK);

    public StatCard(String caption, Icons.Kind icon, Color accent, Color accentLight) {
        super();
        padding(18, 20);
        setLayout(new BorderLayout(16, 0));
        JLabel badge = new JLabel(Icons.of(icon, 24, accent), JLabel.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentLight);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(new EmptyBorder(2, 0, 0, 0));
        text.add(UI.label(caption, Theme.CARD_LABEL, Theme.TEXT_MUTED));
        text.add(javax.swing.Box.createVerticalStrut(4));
        text.add(value);
        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(badge, BorderLayout.NORTH);
        add(iconWrap, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    public void setValue(String text) {
        value.setText(text);
    }

    public void setValueColor(Color c) {
        value.setForeground(c);
    }
}
