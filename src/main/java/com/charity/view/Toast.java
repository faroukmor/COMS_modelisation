package com.charity.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Non-blocking notification shown in the top-right corner of the window.
 */
public final class Toast {
    public enum Kind { SUCCESS, ERROR, INFO }

    private Toast() {
    }

    public static void show(JFrame frame, String message, Kind kind) {
        if (frame == null || message == null) {
            return;
        }
        JLayeredPane layer = frame.getLayeredPane();
        Color accent = kind == Kind.SUCCESS ? Theme.GREEN : kind == Kind.ERROR ? Theme.RED : Theme.BLUE;
        Icons.Kind icon = kind == Kind.SUCCESS ? Icons.Kind.CHECK : kind == Kind.ERROR ? Icons.Kind.CLOCK : Icons.Kind.CHECK;

        JPanel toast = new JPanel(new java.awt.BorderLayout(12, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 30));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 16, 16);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 6, getHeight() - 4, 6, 6);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 16, 16);
                g2.dispose();
            }
        };
        toast.setOpaque(false);
        toast.setBorder(new EmptyBorder(14, 20, 16, 18));
        JLabel ic = new JLabel(Icons.of(icon, 20, accent));
        JLabel text = new JLabel("<html><body style='width:280px'>" + escape(message).replace("\n", "<br>")
                + "</body></html>");
        text.setFont(Theme.BODY_FONT);
        text.setForeground(Theme.TEXT_DARK);
        toast.add(ic, java.awt.BorderLayout.WEST);
        toast.add(text, java.awt.BorderLayout.CENTER);

        Dimension d = toast.getPreferredSize();
        int x = layer.getWidth() - d.width - 24;
        int y = 20;
        // stack below existing toasts
        for (java.awt.Component c : layer.getComponentsInLayer(JLayeredPane.POPUP_LAYER)) {
            if (c.getName() != null && c.getName().equals("toast")) {
                y = Math.max(y, c.getY() + c.getHeight() + 8);
            }
        }
        toast.setName("toast");
        toast.setBounds(x, y, d.width, d.height);
        layer.add(toast, JLayeredPane.POPUP_LAYER);
        layer.repaint();

        Timer t = new Timer(kind == Kind.ERROR ? 6500 : 3500, e -> {
            layer.remove(toast);
            layer.repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Shows a toast for a failure, using a readable message. */
    public static void error(JFrame frame, Throwable t) {
        show(frame, Async.message(t), Kind.ERROR);
    }
}
