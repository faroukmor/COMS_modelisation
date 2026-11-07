package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.charity.controller.AppController;
import com.charity.model.Donor;

/**
 * Donor management: register, update, delete, search donors.
 */
public class DonorView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Full Name", "Email", "Phone", "Address", "Total Donated"});
    private final JTextField txtName = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtSearch = new JTextField();
    private Long editingId = null;

    public DonorView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        load();
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("Manage Donors");
        title.setFont(Theme.H2_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);

        txtName.setPreferredSize(new Dimension(160, 40));
        txtEmail.setPreferredSize(new Dimension(160, 40));
        txtPhone.setPreferredSize(new Dimension(120, 40));
        txtAddress.setPreferredSize(new Dimension(200, 40));
        txtSearch.setPreferredSize(new Dimension(160, 40));

        form.add(field("Full Name", txtName));
        form.add(field("Email", txtEmail));
        form.add(field("Phone", txtPhone));
        form.add(field("Address", txtAddress));
        form.add(field("Search", txtSearch));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnAdd = button("Add", Theme.GREEN);
        JButton btnUpdate = button("Update", Theme.BLUE);
        JButton btnDelete = button("Delete", Theme.RED);
        JButton btnClear = button("Clear", Theme.TEXT_MUTED);
        JButton btnSearch = button("Search", Theme.GREEN_DARK);
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> onSearch());
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete);
        btns.add(btnClear); btns.add(btnSearch);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected(table.getSelectedRow());
        });

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel field(String label, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.SURFACE);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    private JButton button(String label, Color color) {
        JButton b = new JButton(label);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    private JPanel buildTable() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel title = new JLabel("Donors");
        title.setFont(Theme.H3_FONT);
        title.setForeground(Theme.TEXT_DARK);
        title.setBorder(new EmptyBorder(8, 0, 4, 0));
        outer.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.SURFACE);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private void load() {
        List<Donor> list = app.donorService.findAll();
        Object[][] data = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            Donor d = list.get(i);
            data[i][0] = d.getId();
            data[i][1] = d.getFullName();
            data[i][2] = d.getEmail();
            data[i][3] = d.getPhone();
            data[i][4] = d.getAddress();
            data[i][5] = themeMoney(d.getTotalDonated());
        }
        TableHelper.setData(table, data);
    }

    private String themeMoney(java.math.BigDecimal v) {
        return v == null ? "0.00" : v.toString();
    }

    private void clearForm() {
        txtName.setText(""); txtEmail.setText(""); txtPhone.setText(""); txtAddress.setText("");
        editingId = null;
    }

    // ---- Actions wired via controller ----
    void onAdd() {
        try {
            Donor d = new Donor();
            d.setFullName(txtName.getText().trim());
            d.setEmail(txtEmail.getText().trim());
            d.setPhone(txtPhone.getText().trim());
            d.setAddress(txtAddress.getText().trim());
            app.donorService.create(d);
            clearForm();
            load();
            JOptionPane.showMessageDialog(this, "Donor added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onUpdate() {
        if (editingId == null) {
            JOptionPane.showMessageDialog(this, "Select a donor to update.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Donor d = new Donor();
            d.setId(editingId);
            d.setFullName(txtName.getText().trim());
            d.setEmail(txtEmail.getText().trim());
            d.setPhone(txtPhone.getText().trim());
            d.setAddress(txtAddress.getText().trim());
            app.donorService.update(d);
            clearForm();
            load();
            JOptionPane.showMessageDialog(this, "Donor updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onDelete() {
        if (editingId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this donor? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                app.donorService.delete(editingId);
                clearForm();
                load();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void onRowSelected(int row) {
        if (row < 0) return;
        editingId = Long.valueOf(table.getValueAt(row, 0).toString());
        txtName.setText(table.getValueAt(row, 1) == null ? "" : table.getValueAt(row, 1).toString());
        txtEmail.setText(table.getValueAt(row, 2) == null ? "" : table.getValueAt(row, 2).toString());
        txtPhone.setText(table.getValueAt(row, 3) == null ? "" : table.getValueAt(row, 3).toString());
        txtAddress.setText(table.getValueAt(row, 4) == null ? "" : table.getValueAt(row, 4).toString());
    }

    void onSearch() {
        String kw = txtSearch.getText().trim();
        List<Donor> list = kw.isEmpty() ? app.donorService.findAll() : app.donorService.search(kw);
        Object[][] data = new Object[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            Donor d = list.get(i);
            data[i][0] = d.getId(); data[i][1] = d.getFullName();
            data[i][2] = d.getEmail(); data[i][3] = d.getPhone();
            data[i][4] = d.getAddress(); data[i][5] = themeMoney(d.getTotalDonated());
        }
        TableHelper.setData(table, data);
    }
}
