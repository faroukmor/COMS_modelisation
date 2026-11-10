package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Donor;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Donor management: add, edit, delete and filter donors.
 */
public class DonorView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Full name", "Email", "Phone", "Address", "Donated"});
    private final JTextField txtName = UI.styleField(new JTextField());
    private final JTextField txtEmail = UI.styleField(new JTextField(), "name@example.com");
    private final JTextField txtPhone = UI.styleField(new JTextField());
    private final JTextField txtAddress = UI.styleField(new JTextField());
    private final JTextField txtSearch = UI.searchField("Search donors...");
    private final JLabel formTitle = cardTitle("Add donor");
    private final JButton btnSave = UI.button("Add donor", UI.Style.PRIMARY, Icons.Kind.PLUS);
    private final JButton btnDelete = UI.button("Delete", UI.Style.DANGER, Icons.Kind.TRASH);
    private final JButton btnClear = UI.button("Clear", UI.Style.SECONDARY);
    private Long editingId = null;

    public DonorView(AppController app) {
        super(app);
        add(UI.pageHeader("Donors", "People and organisations who give to the fund.", txtSearch), BorderLayout.NORTH);

        TableHelper.attachFilter(table, txtSearch);
        UI.Card list = TableHelper.tableCard("All donors", table, null);

        UI.FormGrid form = new UI.FormGrid(1);
        form.add("Full name", txtName);
        form.add("Email", txtEmail);
        form.add("Phone", txtPhone);
        form.add("Address", txtAddress);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        JPanel buttons = UI.buttonRow(btnSave, btnDelete, btnClear);
        add(masterDetail(list, formCard(formTitle, body, buttons), 390), BorderLayout.CENTER);

        btnDelete.setEnabled(false);
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected(table.getSelectedRow());
            }
        });
        load();
    }

    private void load() {
        async(app.donorService::findAll, list -> {
            Object[][] data = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                Donor d = list.get(i);
                data[i] = new Object[]{d.getId(), d.getFullName(), d.getEmail(), d.getPhone(), d.getAddress(),
                        d.getTotalDonated()};
            }
            TableHelper.setData(table, data);
        });
    }

    private Donor fromForm() {
        Donor d = new Donor();
        d.setId(editingId);
        d.setFullName(txtName.getText());
        d.setEmail(txtEmail.getText());
        d.setPhone(txtPhone.getText());
        d.setAddress(txtAddress.getText());
        return d;
    }

    void onSave() {
        Donor d = fromForm();
        boolean creating = editingId == null;
        async(() -> {
            if (creating) {
                app.donorService.create(d);
            } else {
                app.donorService.update(d);
            }
            return Boolean.TRUE;
        }, ok -> {
            toast(creating ? "Donor added." : "Donor updated.");
            clearForm();
            load();
        });
    }

    void onDelete() {
        if (editingId == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this donor? This cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        Long id = editingId;
        async(() -> {
            app.donorService.delete(id);
            return Boolean.TRUE;
        }, ok -> {
            toast("Donor deleted.");
            clearForm();
            load();
        });
    }

    void onRowSelected(int row) {
        if (row < 0) {
            return;
        }
        editingId = (Long) table.getValueAt(row, 0);
        txtName.setText(text(table.getValueAt(row, 1)));
        txtEmail.setText(text(table.getValueAt(row, 2)));
        txtPhone.setText(text(table.getValueAt(row, 3)));
        txtAddress.setText(text(table.getValueAt(row, 4)));
        formTitle.setText("Edit donor #" + editingId);
        btnSave.setText("Save changes");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        editingId = null;
        table.clearSelection();
        formTitle.setText("Add donor");
        btnSave.setText("Add donor");
        btnDelete.setEnabled(false);
    }

    private static String text(Object o) {
        return o == null ? "" : o.toString();
    }
}
