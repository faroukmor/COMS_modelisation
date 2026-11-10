package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import com.charity.model.CharityAssociation;
import com.charity.model.Individual;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Beneficiary management: individuals and charity associations (type-specific fields).
 */
public class BeneficiaryView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Type", "Name", "ID / Reg. no.", "Email", "Phone", "Address"});
    private final JComboBox<BeneficiaryType> cmbType = UI.enumCombo(BeneficiaryType.values());
    private final JTextField txtEmail = UI.styleField(new JTextField(), "name@example.com");
    private final JTextField txtPhone = UI.styleField(new JTextField());
    private final JTextField txtAddress = UI.styleField(new JTextField());
    private final JTextField txtFullName = UI.styleField(new JTextField());
    private final JTextField txtNationalId = UI.styleField(new JTextField());
    private final JTextField txtOrgName = UI.styleField(new JTextField());
    private final JTextField txtRegNo = UI.styleField(new JTextField());
    private final JTextField txtRep = UI.styleField(new JTextField());
    private final JTextField txtSearch = UI.searchField("Search beneficiaries...");
    private final UI.FormGrid individualBox = new UI.FormGrid(1);
    private final UI.FormGrid associationBox = new UI.FormGrid(1);
    private final JLabel formTitle = cardTitle("Add beneficiary");
    private final JButton btnSave = UI.button("Add beneficiary", UI.Style.PRIMARY, Icons.Kind.PLUS);
    private final JButton btnDelete = UI.button("Delete", UI.Style.DANGER, Icons.Kind.TRASH);
    private final JButton btnClear = UI.button("Clear", UI.Style.SECONDARY);
    private final Map<Long, Beneficiary> cache = new HashMap<>();
    private Long editingId = null;

    public BeneficiaryView(AppController app) {
        super(app);
        add(UI.pageHeader("Beneficiaries", "Individuals and charity associations that receive aid.", txtSearch),
                BorderLayout.NORTH);
        TableHelper.attachFilter(table, txtSearch);
        UI.Card list = TableHelper.tableCard("All beneficiaries", table, null);

        individualBox.add("Full name", txtFullName);
        individualBox.add("National ID", txtNationalId);
        associationBox.add("Organization name", txtOrgName);
        associationBox.add("Registration number", txtRegNo);
        associationBox.add("Representative", txtRep);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        UI.FormGrid typeBox = new UI.FormGrid(1);
        typeBox.add("Type", cmbType);
        UI.FormGrid common = new UI.FormGrid(1);
        common.add("Email", txtEmail);
        common.add("Phone", txtPhone);
        common.add("Address", txtAddress);
        for (java.awt.Component c : new java.awt.Component[]{typeBox, individualBox, associationBox, common}) {
            body.add(c);
        }
        JPanel btns = UI.buttonRow(btnSave, btnDelete, btnClear);
        add(masterDetail(list, formCard(formTitle, body, btns), 390), BorderLayout.CENTER);

        btnDelete.setEnabled(false);
        cmbType.addActionListener(e -> switchType());
        btnSave.addActionListener(e -> onSave());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected(table.getSelectedRow());
            }
        });
        switchType();
        load();
    }

    private void switchType() {
        boolean individual = cmbType.getSelectedItem() == BeneficiaryType.INDIVIDUAL;
        individualBox.setVisible(individual);
        associationBox.setVisible(!individual);
        revalidate();
    }

    private void load() {
        async(app.beneficiaryService::findAll, list -> fill(list));
    }

    private void fill(List<Beneficiary> list) {
        cache.clear();
        Object[][] data = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            Beneficiary b = list.get(i);
            cache.put(b.getId(), b);
            String idNum = b instanceof Individual ind ? ind.getNationalId()
                    : ((CharityAssociation) b).getRegistrationNumber();
            data[i] = new Object[]{b.getId(), b.getType(), b.getDisplayName(), idNum, b.getEmail(), b.getPhone(), b.getAddress()};
        }
        TableHelper.setData(table, data);
    }

    private Beneficiary fromForm() {
        Beneficiary b;
        if (cmbType.getSelectedItem() == BeneficiaryType.INDIVIDUAL) {
            Individual ind = new Individual();
            ind.setFullName(txtFullName.getText());
            ind.setNationalId(txtNationalId.getText());
            b = ind;
        } else {
            CharityAssociation a = new CharityAssociation();
            a.setOrganizationName(txtOrgName.getText());
            a.setRegistrationNumber(txtRegNo.getText());
            a.setRepresentativeName(txtRep.getText());
            b = a;
        }
        b.setId(editingId);
        b.setEmail(txtEmail.getText());
        b.setPhone(txtPhone.getText());
        b.setAddress(txtAddress.getText());
        return b;
    }

    void onSave() {
        Beneficiary b = fromForm();
        boolean creating = editingId == null;
        async(() -> {
            if (creating) {
                app.beneficiaryService.create(b);
            } else {
                app.beneficiaryService.update(b);
            }
            return Boolean.TRUE;
        }, ok -> {
            toast(creating ? "Beneficiary added." : "Beneficiary updated.");
            clearForm();
            load();
        });
    }

    void onDelete() {
        if (editingId == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete this beneficiary?", "Confirm delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        Long id = editingId;
        async(() -> {
            app.beneficiaryService.delete(id);
            return Boolean.TRUE;
        }, ok -> {
            toast("Beneficiary deleted.");
            clearForm();
            load();
        });
    }

    void onRowSelected(int row) {
        if (row < 0) {
            return;
        }
        Beneficiary b = cache.get((Long) table.getValueAt(row, 0));
        if (b == null) {
            return;
        }
        editingId = b.getId();
        cmbType.setSelectedItem(b.getType());
        cmbType.setEnabled(false); // the type of an existing beneficiary cannot change
        txtEmail.setText(nz(b.getEmail()));
        txtPhone.setText(nz(b.getPhone()));
        txtAddress.setText(nz(b.getAddress()));
        if (b instanceof Individual ind) {
            txtFullName.setText(nz(ind.getFullName()));
            txtNationalId.setText(nz(ind.getNationalId()));
        } else if (b instanceof CharityAssociation a) {
            txtOrgName.setText(nz(a.getOrganizationName()));
            txtRegNo.setText(nz(a.getRegistrationNumber()));
            txtRep.setText(nz(a.getRepresentativeName())); // loaded from the record, not blanked
        }
        formTitle.setText("Edit beneficiary #" + editingId);
        btnSave.setText("Save changes");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        for (JTextField f : new JTextField[]{txtEmail, txtPhone, txtAddress, txtFullName, txtNationalId,
                txtOrgName, txtRegNo, txtRep}) {
            f.setText("");
        }
        editingId = null;
        cmbType.setEnabled(true);
        table.clearSelection();
        formTitle.setText("Add beneficiary");
        btnSave.setText("Add beneficiary");
        btnDelete.setEnabled(false);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
