package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

/**
 * Builds consistently styled, sortable tables: zebra rows, status badges, right-aligned money,
 * formatted dates and a light header.
 */
public final class TableHelper {
    private TableHelper() {
    }

    /** Creates a read-only table with the given column names and an empty model. */
    public static JTable createTable(String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, column);
                    if (v != null) {
                        return v instanceof Enum<?> ? Enum.class : v.getClass();
                    }
                }
                return Object.class;
            }
        };
        JTable table = new JTable(model);
        style(table);
        return table;
    }

    /** Applies the standard visual style to a table. */
    public static void style(JTable table) {
        table.setSelectionBackground(Theme.GREEN_LIGHT);
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.DIVIDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(Theme.TABLE_ROW);
        table.setForeground(Theme.TEXT_DARK);
        table.setBackground(Theme.SURFACE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(100, 40));
        header.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new CellRenderer(SwingConstants.LEFT));
        table.setDefaultRenderer(Long.class, new CellRenderer(SwingConstants.LEFT) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void decorate(JLabel l, Object value, boolean selected) {
                l.setForeground(selected ? Theme.TEXT_DARK : Theme.TEXT_MUTED);
                l.setText(value == null ? "" : "#" + value);
            }
        });
        table.setDefaultRenderer(Integer.class, table.getDefaultRenderer(Long.class));
        table.setDefaultRenderer(BigDecimal.class, new CellRenderer(SwingConstants.RIGHT) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void decorate(JLabel l, Object value, boolean selected) {
                l.setText(Format.number((BigDecimal) value));
                l.setFont(Theme.font(java.awt.Font.BOLD, 13));
            }
        });
        table.setDefaultRenderer(LocalDateTime.class, new CellRenderer(SwingConstants.LEFT) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void decorate(JLabel l, Object value, boolean selected) {
                l.setText(Format.date((LocalDateTime) value));
                l.setForeground(Theme.TEXT_MUTED);
            }
        });
        table.setDefaultRenderer(Enum.class, new BadgeRenderer());
    }

    /** Sets the table's data from rows of objects. */
    public static void setData(JTable table, Object[][] rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        if (rows != null) {
            for (Object[] row : rows) {
                model.addRow(row);
            }
        }
        fit(table);
    }

    /**
     * Sizes columns to their content. If everything fits, columns stretch to fill the width;
     * otherwise they keep their natural width and the table scrolls horizontally.
     */
    public static void fit(JTable table) {
        TableColumnModel cm = table.getColumnModel();
        int total = 0;
        int[] widths = new int[cm.getColumnCount()];
        for (int c = 0; c < widths.length; c++) {
            widths[c] = preferredWidth(table, c);
            total += widths[c];
        }
        java.awt.Container parent = table.getParent();
        int available = parent instanceof javax.swing.JViewport ? parent.getWidth() : 0;
        boolean overflow = available > 0 && total > available;
        table.setAutoResizeMode(overflow ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_ALL_COLUMNS);
        for (int c = 0; c < widths.length; c++) {
            cm.getColumn(c).setMinWidth(40);
            cm.getColumn(c).setPreferredWidth(widths[c]);
            if (overflow) {
                cm.getColumn(c).setWidth(widths[c]);
            }
        }
        table.revalidate();
        table.repaint();
    }

    private static int preferredWidth(JTable table, int col) {
        TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
        int w = hr.getTableCellRendererComponent(table, table.getColumnName(col), false, false, -1, col)
                .getPreferredSize().width + 18; // room for the sort arrow
        int rows = Math.min(table.getRowCount(), 200);
        for (int r = 0; r < rows; r++) {
            Component comp = table.prepareRenderer(table.getCellRenderer(r, col), r, col);
            w = Math.max(w, comp.getPreferredSize().width);
        }
        return Math.max(56, Math.min(w, 320));
    }

    /** Wraps a table in a borderless scroll pane with a white viewport. */
    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBackground(Theme.SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                fit(table);
            }
        });
        return sp;
    }

    /** A card with a title (and optional toolbar on the right) above a table. */
    public static UI.Card tableCard(String title, JTable table, JComponent toolbar) {
        UI.Card card = new UI.Card().padding(16, 0);
        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 22, 12, 22));
        head.add(UI.label(title, Theme.H3_FONT, Theme.TEXT_DARK), BorderLayout.WEST);
        if (toolbar != null) {
            head.add(toolbar, BorderLayout.EAST);
        }
        card.add(head, BorderLayout.NORTH);
        card.add(scroll(table), BorderLayout.CENTER);
        return card;
    }

    /** Filters the table live (case-insensitive, any column) as the user types in {@code box}. */
    public static void attachFilter(JTable table, JTextComponent box) {
        box.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() {
                @SuppressWarnings("unchecked")
                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
                String q = box.getText().trim();
                sorter.setRowFilter(q.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                apply();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                apply();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                apply();
            }
        });
    }

    /** Value of a cell addressed in view coordinates (safe with sorting / filtering). */
    public static Object valueAt(JTable table, int viewRow, int column) {
        return table.getValueAt(viewRow, column);
    }

    // -------------------------------------------------------------- renderers

    private static class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final int align;

        CellRenderer(int align) {
            this.align = align;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, selected, false, row, col);
            l.setHorizontalAlignment(align);
            l.setFont(Theme.TABLE_ROW);
            l.setForeground(Theme.TEXT_DARK);
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setBackground(selected ? Theme.GREEN_LIGHT : row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT);
            decorate(l, value, selected);
            return l;
        }

        protected void decorate(JLabel l, Object value, boolean selected) {
            // default: plain text
        }
    }

    private static final class BadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            JPanel cell = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 7));
            cell.setBackground(selected ? Theme.GREEN_LIGHT : row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT);
            if (value != null) {
                cell.add(UI.badge(value.toString()));
            }
            return cell;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, col);
            String h = value == null ? "" : value.toString().toUpperCase();
            l.setText(t.getColumnClass(col) == BigDecimal.class ? h + " (DZD)" : h);
            l.setFont(Theme.TABLE_HEADER);
            l.setForeground(Theme.TEXT_MUTED);
            l.setBackground(Theme.ROW_ALT);
            Class<?> cls = t.getColumnClass(col);
            l.setHorizontalAlignment(cls == BigDecimal.class ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(0, 12, 0, 12)));
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g);
        }
    }
}
