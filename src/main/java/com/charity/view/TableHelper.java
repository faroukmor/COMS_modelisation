package com.charity.view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Helper to build consistently styled tables across the application.
 */
public final class TableHelper {
    private TableHelper() {
    }

    /**
     * Creates a table with the given column names and empty data model.
     */
    public static JTable createTable(String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.GREEN_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(Theme.TABLE_HEADER);
        header.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setPreferredSize(new java.awt.Dimension(header.getWidth(), 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            cm.getColumn(i).setHeaderRenderer(new HeaderRenderer());
            if (i == 0) {
                cm.getColumn(i).setCellRenderer(center);
            }
        }

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.GREEN_FAINT);
                }
                setFont(Theme.TABLE_ROW);
                return c;
            }
        });
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
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            setBackground(Theme.GREEN_DARK);
            setForeground(Color.WHITE);
            setFont(Theme.TABLE_HEADER);
            setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
            return c;
        }
    }
}

