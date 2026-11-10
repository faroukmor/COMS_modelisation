package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

/**
 * Reusable, self-painted building blocks (cards, buttons, fields, badges, form grid).
 * They are drawn with Java2D, so they look the same whichever look and feel is active.
 */
public final class UI {
    private UI() {
    }

    // ------------------------------------------------------------------ cards

    /** White rounded panel with a soft shadow. */
    public static class Card extends JPanel {
        private static final long serialVersionUID = 1L;
        private Color fill = Theme.SURFACE;
        private Color stroke = Theme.BORDER;
        private final int arc;

        public Card() {
            this(Theme.RADIUS);
        }

        public Card(int arc) {
            this.arc = arc;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(20, 22, 20, 22));
        }

        public Card fill(Color c) {
            this.fill = c;
            return this;
        }

        public Card stroke(Color c) {
            this.stroke = c;
            return this;
        }

        public Card padding(int v, int h) {
            setBorder(new EmptyBorder(v, h, v, h));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 2;
            int h = getHeight() - 3;
            for (int i = 3; i >= 1; i--) { // soft shadow
                g2.setColor(new Color(15, 23, 42, 5 + (3 - i) * 2));
                g2.fill(new RoundRectangle2D.Double(1 - i * 0.5, 1 + i, w + i, h, arc + i, arc + i));
            }
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Double(1, 1, w, h, arc, arc));
            g2.setColor(stroke);
            g2.draw(new RoundRectangle2D.Double(1, 1, w - 1, h - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------- buttons

    public enum Style { PRIMARY, SECONDARY, DANGER, GHOST }

    /** Rounded button with hover / pressed / disabled states. */
    public static class RoundButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Style style;
        private boolean hover;
        private boolean pressed;

        RoundButton(String text, Style style, Icon icon) {
            super(text, icon);
            this.style = style;
            setFont(Theme.BUTTON_FONT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setForeground(foreground(true));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        private Color foreground(boolean enabled) {
            if (!enabled) {
                return Theme.TEXT_FAINT;
            }
            return switch (style) {
                case PRIMARY, DANGER -> Color.WHITE;
                case SECONDARY -> Theme.TEXT_DARK;
                case GHOST -> Theme.GREEN_DARK;
            };
        }

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setForeground(foreground(b));
            setCursor(Cursor.getPredefinedCursor(b ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg;
            Color border = null;
            if (!isEnabled()) {
                bg = style == Style.GHOST ? new Color(0, 0, 0, 0) : Theme.HOVER;
                if (style == Style.SECONDARY) {
                    border = Theme.BORDER;
                }
            } else {
                switch (style) {
                    case PRIMARY -> bg = pressed ? Theme.GREEN_DARKER : hover ? Theme.GREEN_DARK : Theme.GREEN;
                    case DANGER -> bg = pressed ? new Color(0x99, 0x1B, 0x1B) : hover ? new Color(0xB9, 0x1C, 0x1C) : Theme.RED;
                    case SECONDARY -> {
                        bg = pressed ? Theme.SLATE_LIGHT : hover ? Theme.HOVER : Theme.SURFACE;
                        border = new Color(0xCB, 0xD5, 0xE1);
                    }
                    default -> bg = pressed ? Theme.GREEN_LIGHT : hover ? Theme.GREEN_FAINT : new Color(0, 0, 0, 0);
                }
            }
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1,
                    Theme.RADIUS_SMALL, Theme.RADIUS_SMALL);
            g2.setColor(bg);
            g2.fill(shape);
            if (border != null) {
                g2.setColor(border);
                g2.draw(shape);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton button(String text, Style style) {
        return new RoundButton(text, style, null);
    }

    public static JButton button(String text, Style style, Icons.Kind icon) {
        Color c = style == Style.SECONDARY ? Theme.TEXT_DARK : style == Style.GHOST ? Theme.GREEN_DARK : Color.WHITE;
        return new RoundButton(text, style, Icons.of(icon, 16, c));
    }

    // ----------------------------------------------------------------- fields

    /** Rounded field border that turns green while focused. */
    private static final class FieldBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focus = c.hasFocus() || (c instanceof JComponent jc && jc.getClientProperty("ui.focus") == Boolean.TRUE);
            g2.setColor(focus ? Theme.GREEN : new Color(0xCB, 0xD5, 0xE1));
            g2.setStroke(new java.awt.BasicStroke(focus ? 2f : 1f));
            g2.draw(new RoundRectangle2D.Double(x + 1, y + 1, w - 2.5, h - 2.5, Theme.RADIUS_SMALL, Theme.RADIUS_SMALL));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(9, 12, 9, 12);
        }
    }

    /** Applies the standard field look to a text component. */
    public static <T extends JTextComponent> T styleField(T f) {
        f.setFont(Theme.BODY_FONT);
        f.setForeground(Theme.TEXT_DARK);
        f.setBorder(new FieldBorder());
        f.setCaretColor(Theme.GREEN_DARK);
        f.setPreferredSize(new Dimension(160, 42));
        f.putClientProperty("JTextField.placeholderForeground", Theme.TEXT_FAINT);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.repaint();
            }
        });
        return f;
    }

    public static <T extends JTextComponent> T styleField(T f, String placeholder) {
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return styleField(f);
    }

    public static <T extends JComboBox<?>> T styleCombo(T c) {
        c.setFont(Theme.BODY_FONT);
        c.setPreferredSize(new Dimension(160, 42));
        c.setBackground(Color.WHITE);
        return c;
    }

    /** Combo box listing enum constants with readable labels (BANK_TRANSFER -> "Bank transfer"). */
    public static <E extends Enum<E>> JComboBox<E> enumCombo(E[] values) {
        JComboBox<E> c = new JComboBox<>(values);
        c.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                return super.getListCellRendererComponent(list, Format.label(value), index, sel, focus);
            }
        });
        return styleCombo(c);
    }

    /** Password field with a show/hide toggle drawn on top of its right edge. */
    public static JComponent passwordField(JPasswordField field, String placeholder) {
        styleField(field, placeholder);
        final char echo = field.getEchoChar() == 0 ? '\u2022' : field.getEchoChar();
        field.setEchoChar(echo);
        JButton eye = new JButton(Icons.of(Icons.Kind.EYE, 18, Theme.TEXT_MUTED));
        eye.setBorder(new EmptyBorder(0, 8, 0, 12));
        eye.setContentAreaFilled(false);
        eye.setFocusPainted(false);
        eye.setFocusable(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.setToolTipText("Show / hide password");
        eye.addActionListener(e -> {
            boolean hidden = field.getEchoChar() != 0;
            field.setEchoChar(hidden ? (char) 0 : echo);
            eye.setIcon(Icons.of(hidden ? Icons.Kind.EYE_OFF : Icons.Kind.EYE, 18, Theme.TEXT_MUTED));
        });
        javax.swing.JLayeredPane layered = new javax.swing.JLayeredPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return field.getPreferredSize();
            }

            @Override
            public void doLayout() {
                field.setBounds(0, 0, getWidth(), getHeight());
                Dimension d = eye.getPreferredSize();
                eye.setBounds(getWidth() - d.width, (getHeight() - 30) / 2, d.width, 30);
            }
        };
        layered.add(field, javax.swing.JLayeredPane.DEFAULT_LAYER);
        layered.add(eye, javax.swing.JLayeredPane.PALETTE_LAYER);
        return layered;
    }

    // ----------------------------------------------------------------- labels

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel muted(String text) {
        return label(text, Theme.BODY_SMALL, Theme.TEXT_MUTED);
    }

    /** Search box used above tables. */
    public static javax.swing.JTextField searchField(String placeholder) {
        javax.swing.JTextField f = styleField(new javax.swing.JTextField(), placeholder);
        f.setPreferredSize(new Dimension(260, 38));
        return f;
    }

    /** A label above a component, used by all forms. */
    public static JPanel labeled(String caption, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(caption, Theme.LABEL_FONT, Theme.TEXT_MUTED), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    /** Page title with optional subtitle and right-aligned actions. */
    public static JPanel pageHeader(String title, String subtitle, JComponent... actions) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        text.add(label(title, Theme.H1_FONT, Theme.TEXT_DARK));
        if (subtitle != null && !subtitle.isEmpty()) {
            text.add(javax.swing.Box.createVerticalStrut(4));
            text.add(label(subtitle, Theme.BODY_FONT, Theme.TEXT_MUTED));
        }
        header.add(text, BorderLayout.WEST);
        if (actions.length > 0) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            right.setOpaque(false);
            for (JComponent a : actions) {
                right.add(a);
            }
            header.add(right, BorderLayout.EAST);
        }
        header.setBorder(new EmptyBorder(0, 0, 18, 0));
        return header;
    }

    // ----------------------------------------------------------------- badges

    /** Background / foreground colours for a status-like word (PENDING, APPROVED, CASH ...). */
    public static Color[] badgeColors(String value) {
        String v = value == null ? "" : value.toUpperCase();
        return switch (v) {
            case "CONFIRMED", "APPROVED", "FULFILLED", "ADMIN" -> new Color[]{Theme.GREEN_LIGHT, Theme.GREEN_DARK};
            case "PENDING" -> new Color[]{Theme.AMBER_LIGHT, new Color(0x92, 0x40, 0x0E)};
            case "UNDER_REVIEW", "BANK_TRANSFER", "DONOR" -> new Color[]{Theme.BLUE_LIGHT, new Color(0x1E, 0x40, 0xAF)};
            case "CANCELLED", "REJECTED" -> new Color[]{Theme.RED_LIGHT, new Color(0x99, 0x1B, 0x1B)};
            default -> new Color[]{Theme.SLATE_LIGHT, new Color(0x33, 0x41, 0x55)};
        };
    }

    /** Small rounded pill label. */
    public static JLabel badge(String raw) {
        Color[] c = badgeColors(raw);
        JLabel l = new JLabel(Format.label(raw), SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setBackground(c[0]);
        l.setForeground(c[1]);
        l.setFont(Theme.LABEL_FONT);
        l.setBorder(new EmptyBorder(4, 12, 4, 12));
        return l;
    }

    // -------------------------------------------------------------- form grid

    /** Lays labelled fields out in a responsive grid ({@code columns} per row). */
    public static final class FormGrid extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int columns;
        private int col;
        private int row;

        public FormGrid(int columns) {
            super(new GridBagLayout());
            this.columns = columns;
            setOpaque(false);
        }

        public FormGrid add(String caption, JComponent field) {
            return add(caption, field, 1);
        }

        public FormGrid add(String caption, JComponent field, int span) {
            return addRaw(labeled(caption, field), span);
        }

        /** Adds an already-built component (e.g. one with its own caption) spanning {@code span} columns. */
        public FormGrid addRaw(JComponent component, int span) {
            span = Math.min(span, columns);
            if (col + span > columns) {
                col = 0;
                row++;
            }
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.gridwidth = span;
            gbc.weightx = span;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(0, col == 0 ? 0 : 7, 14, col + span >= columns ? 0 : 7);
            add(component, gbc);
            col += span;
            if (col >= columns) {
                col = 0;
                row++;
            }
            return this;
        }
    }

    /** Horizontal button row. */
    public static JPanel buttonRow(JComponent... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        for (JComponent b : buttons) {
            p.add(b);
        }
        return p;
    }

    public static javax.swing.border.Border pad(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}
