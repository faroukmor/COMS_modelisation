package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import com.charity.model.Donor;
import com.charity.model.PaymentMethod;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Donation management: record donations (pending or already confirmed), confirm / cancel pending ones.
 */
public class DonationView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Donor", "Amount", "Date", "Method", "Status", "Reference"});
    private final JComboBox<Item> cmbDonor = UI.styleCombo(new JComboBox<>());
    private final JTextField txtAmount = UI.styleField(new JTextField(), "0.00");
    private final JComboBox<PaymentMethod> cmbMethod = UI.enumCombo(PaymentMethod.values());
    private final JComboBox<String> cmbRecordAs = UI.styleCombo(new JComboBox<>(new String[]{"Pending (confirm later)", "Confirmed (money received)"}));
    private final JTextField txtReference = UI.styleField(new JTextField());
    private final JTextField txtDescription = UI.styleField(new JTextField());
    private final JTextField txtSearch = UI.searchField("Search donations...");
    private final JButton btnConfirm = UI.button("Confirm", UI.Style.PRIMARY, Icons.Kind.CHECK);
    private final JButton btnCancel = UI.button("Cancel donation", UI.Style.SECONDARY);

    public DonationView(AppController app) {
        super(app);
        add(UI.pageHeader("Donations", "Record money received and confirm it to add it to the fund.", txtSearch),
                BorderLayout.NORTH);
        TableHelper.attachFilter(table, txtSearch);
        UI.Card list = TableHelper.tableCard("Donation history", table, UI.buttonRow(btnConfirm, btnCancel));

        UI.FormGrid g = new UI.FormGrid(1);
        g.add("Donor", cmbDonor);
        g.add("Amount (DZD)", txtAmount);
        g.add("Payment method", cmbMethod);
        g.add("Record as", cmbRecordAs);
        g.add("Reference", txtReference);
        g.add("Description", txtDescription);
        JButton btnRecord = UI.button("Record donation", UI.Style.PRIMARY, Icons.Kind.PLUS);
        btnRecord.addActionListener(e -> onRecord());
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(g, BorderLayout.NORTH);
        add(masterDetail(list, formCard(cardTitle("New donation"), body, UI.buttonRow(btnRecord)), 390), BorderLayout.CENTER);

        btnConfirm.addActionListener(e -> onConfirm());
        btnCancel.addActionListener(e -> onCancel());
        load();
    }

    private record Data(List<Donation> donations, List<Donor> donors) {
    }

    private void load() {
        async(() -> new Data(app.getDonationDAO().findAll(), app.donorService.findAll()), data -> {
            Map<Long, String> names = new HashMap<>();
            Item selected = (Item) cmbDonor.getSelectedItem();
            DefaultComboBoxModel<Item> model = new DefaultComboBoxModel<>();
            for (Donor d : data.donors()) {
                names.put(d.getId(), d.getFullName());
                model.addElement(new Item(d.getId(), d.getFullName() + "  (#" + d.getId() + ")"));
            }
            cmbDonor.setModel(model);
            if (selected != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).id().equals(selected.id())) {
                        cmbDonor.setSelectedIndex(i);
                    }
                }
            }
            List<Donation> list = data.donations();
            Object[][] rows = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                Donation d = list.get(i);
                rows[i] = new Object[]{d.getId(), names.getOrDefault(d.getDonorId(), "Donor #" + d.getDonorId()),
                        d.getAmount(), d.getDonationDate(), d.getPaymentMethod(), d.getStatus(), d.getReference()};
            }
            TableHelper.setData(table, rows);
        });
    }

    void onRecord() {
        Item donor = (Item) cmbDonor.getSelectedItem();
        if (donor == null) {
            toastError("Add a donor first (Donors page).");
            return;
        }
        final BigDecimal amount;
        try {
            amount = Format.parseAmount(txtAmount.getText());
        } catch (IllegalArgumentException ex) {
            toastError(ex.getMessage());
            return;
        }
        PaymentMethod method = (PaymentMethod) cmbMethod.getSelectedItem();
        DonationStatus status = cmbRecordAs.getSelectedIndex() == 1 ? DonationStatus.CONFIRMED : DonationStatus.PENDING;
        async(() -> app.donationService.recordDonation(donor.id(), amount, method, status,
                txtDescription.getText(), txtReference.getText()), id -> {
            toast(status == DonationStatus.CONFIRMED ? "Donation recorded and confirmed." : "Donation recorded as pending.");
            txtAmount.setText("");
            txtReference.setText("");
            txtDescription.setText("");
            load();
        });
    }

    private Long selectedId(String what) {
        int row = table.getSelectedRow();
        if (row < 0) {
            toastError("Select a donation to " + what + " first.");
            return null;
        }
        return (Long) table.getValueAt(row, 0);
    }

    void onConfirm() {
        Long id = selectedId("confirm");
        if (id == null) {
            return;
        }
        async(() -> {
            app.donationService.confirmDonation(id);
            return Boolean.TRUE;
        }, ok -> {
            toast("Donation confirmed.");
            load();
        });
    }

    void onCancel() {
        Long id = selectedId("cancel");
        if (id == null) {
            return;
        }
        async(() -> {
            app.donationService.cancelDonation(id);
            return Boolean.TRUE;
        }, ok -> {
            toast("Donation cancelled.");
            load();
        });
    }
}
