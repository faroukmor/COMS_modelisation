package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.charity.controller.AppController;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import com.charity.model.CharityAssociation;
import com.charity.model.Individual;

/**
 * Beneficiary management: individuals and charity associations (type-specific fields).
 */
public class BeneficiaryView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Type", "Name", "ID/Reg Number", "Email", "Phone", "Address"});
    private final JComboBox<BeneficiaryType> cmbType = new JComboBox<>(new DefaultComboBoxModel<>(BeneficiaryType.values()));
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    // individual fields
    private final JTextField txtFullName = new JTextField();
    private final JTextField txtNationalId = new JTextField();
    // association fields
    private final JTextField txtOrgName = new JTextField();
    private final JTextField txtRegNo = new JTextField();
    private final JTextField txtRep = new JTextField();
    private final JPanel individualPanel = new JPanel(new GridLayout(2, 2, 8, 8));
    private final JPanel associationPanel = new JPanel(new GridLayout(3, 2, 8, 8));
    private Long editingId = null;

    public BeneficiaryView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        cmbType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) switchType();
        });
        switchType();
        load();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected(table.getSelectedRow());
        });
    }

    private void switchType() {
        BeneficiaryType t = (BeneficiaryType) cmbType.getSelectedItem();
        boolean individual = t == BeneficiaryType.INDIVIDUAL;
        individualPanel.setVisible(individual);
        associationPanel.setVisible(!individual);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel title = new JLabel("Manage Beneficiaries");
        title.setFont(Theme.H2_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);

        JPanel row1 = new JPanel(new GridLayout(2, 4, 8, 8));
        row1.setBackground(Theme.SURFACE);
        row1.add(combo("Beneficiary Type", cmbType));
        row1.add(field("Email", txtEmail));
        row1.add(field("Phone", txtPhone));
        row1.add(field("Address", txtAddress));

        // individual specific
        individualPanel.setBackground(Theme.SURFACE);
        individualPanel.add(field("Full Name", txtFullName));
        individualPanel.add(field("National ID", txtNationalId));

        // association specific
        associationPanel.setBackground(Theme.SURFACE);
        associationPanel.add(field("Organization Name", txtOrgName));
        associationPanel.add(field("Registration No.", txtRegNo));
        associationPanel.add(field("Representative", txtRep));

        JPanel fields = new JPanel(new BorderLayout());
        fields.setBackground(Theme.SURFACE);
        fields.add(row1, BorderLayout.NORTH);
        fields.add(individualPanel, BorderLayout.CENTER);
        fields.add(associationPanel, BorderLayout.SOUTH);
        associationPanel.setVisible(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnAdd = button("Add", Theme.GREEN);
        JButton btnUpdate = button("Update", Theme.BLUE);
        JButton btnDelete = button("Delete", Theme.RED);
        JButton btnClear = button("Clear", Theme.TEXT_MUTED);
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete); btns.add(btnClear);
        fields.add(btns, BorderLayout.SOUTH);

        form.add(fields, BorderLayout.CENTER);
        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel field(String label, JTextField f) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.SURFACE);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    private JPanel combo(String label, JComboBox<?> c) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.SURFACE);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
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
        JLabel title = new JLabel("Beneficiaries");
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
        List<Beneficiary> list = app.beneficiaryService.findAll();
        Object[][] data = new Object[list.size()][7];
        for (int i = 0; i < list.size(); i++) {
            Beneficiary b = list.get(i);
            String name = b.getDisplayName();
            String idNum = b instanceof Individual ? ((Individual) b).getNationalId()
                    : ((CharityAssociation) b).getRegistrationNumber();
            data[i][0] = b.getId();
            data[i][1] = b.getType();
            data[i][2] = name;
            data[i][3] = idNum;
            data[i][4] = b.getEmail();
            data[i][5] = b.getPhone();
            data[i][6] = b.getAddress();
        }
        TableHelper.setData(table, data);
    }

    private void clearForm() {
        txtEmail.setText(""); txtPhone.setText(""); txtAddress.setText("");
        txtFullName.setText(""); txtNationalId.setText("");
        txtOrgName.setText(""); txtRegNo.setText(""); txtRep.setText("");
        editingId = null;
    }

    {
        btnWire();
    }

    private void btnWire() {
        // wired lazily from constructor after fields ready
    }

    public void wireActions() {
        // add/update/delete/clear wired from MainFrame/AppController after construction
    }

    // Getters for controller to wire
    public JComboBox<BeneficiaryType> getCmbType() { return cmbType; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtPhone() { return txtPhone; }
    public JTextField getTxtAddress() { return txtAddress; }
    public JTextField getTxtFullName() { return txtFullName; }
    public JTextField getTxtNationalId() { return txtNationalId; }
    public JTextField getTxtOrgName() { return txtOrgName; }
    public JTextField getTxtRegNo() { return txtRegNo; }
    public JTextField getTxtRep() { return txtRep; }
    public JTable getTable() { return table; }
    public Long getEditingId() { return editingId; }
    public void setEditingId(Long editingId) { this.editingId = editingId; }

    void onAdd() {
        try {
            BeneficiaryType t = (BeneficiaryType) cmbType.getSelectedItem();
            if (t == BeneficiaryType.INDIVIDUAL) {
                Individual ind = new Individual();
                ind.setFullName(txtFullName.getText().trim());
                ind.setNationalId(txtNationalId.getText().trim());
                ind.setEmail(txtEmail.getText().trim());
                ind.setPhone(txtPhone.getText().trim());
                ind.setAddress(txtAddress.getText().trim());
                app.beneficiaryService.create(ind);
            } else {
                CharityAssociation a = new CharityAssociation();
                a.setOrganizationName(txtOrgName.getText().trim());
                a.setRegistrationNumber(txtRegNo.getText().trim());
                a.setRepresentativeName(txtRep.getText().trim());
                a.setEmail(txtEmail.getText().trim());
                a.setPhone(txtPhone.getText().trim());
                a.setAddress(txtAddress.getText().trim());
                app.beneficiaryService.create(a);
            }
            clearForm();
            load();
            JOptionPane.showMessageDialog(this, "Beneficiary added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onUpdate() {
        if (editingId == null) {
            JOptionPane.showMessageDialog(this, "Select a beneficiary to update.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Beneficiary b = app.beneficiaryService.findById(editingId);
            b.setEmail(txtEmail.getText().trim());
            b.setPhone(txtPhone.getText().trim());
            b.setAddress(txtAddress.getText().trim());
            if (b instanceof Individual) {
                ((Individual) b).setFullName(txtFullName.getText().trim());
                ((Individual) b).setNationalId(txtNationalId.getText().trim());
            } else {
                ((CharityAssociation) b).setOrganizationName(txtOrgName.getText().trim());
                ((CharityAssociation) b).setRegistrationNumber(txtRegNo.getText().trim());
                ((CharityAssociation) b).setRepresentativeName(txtRep.getText().trim());
            }
            app.beneficiaryService.update(b);
            clearForm();
            load();
            JOptionPane.showMessageDialog(this, "Beneficiary updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onDelete() {
        if (editingId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this beneficiary?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                app.beneficiaryService.delete(editingId);
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
        BeneficiaryType t = BeneficiaryType.valueOf(table.getValueAt(row, 1).toString());
        cmbType.setSelectedItem(t);
        switchType();
        txtEmail.setText(toStr(table.getValueAt(row, 4)));
        txtPhone.setText(toStr(table.getValueAt(row, 5)));
        txtAddress.setText(toStr(table.getValueAt(row, 6)));
        if (t == BeneficiaryType.INDIVIDUAL) {
            txtFullName.setText(toStr(table.getValueAt(row, 2)));
            txtNationalId.setText(toStr(table.getValueAt(row, 3)));
        } else {
            txtOrgName.setText(toStr(table.getValueAt(row, 2)));
            txtRegNo.setText(toStr(table.getValueAt(row, 3)));
            txtRep.setText("");
        }
    }

    private String toStr(Object o) { return o == null ? "" : o.toString(); }
}
