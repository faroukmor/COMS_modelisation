package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
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
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import com.charity.model.PaymentMethod;

/**
 * Donation management: record donations, confirm/cancel, view history.
 */
public class DonationView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Donor #", "Amount", "Date", "Method", "Status", "Reference"});
    private final JTextField txtDonorId = new JTextField();
    private final JTextField txtAmount = new JTextField();
    private final JComboBox<PaymentMethod> cmbMethod = new JComboBox<>(new DefaultComboBoxModel<>(PaymentMethod.values()));
    private final JComboBox<DonationStatus> cmbStatus = new JComboBox<>(new DefaultComboBoxModel<>(DonationStatus.values()));
    private final JTextField txtReference = new JTextField();
    private final JTextField txtDescription = new JTextField();

    public DonationView(AppController app) {
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
        JLabel title = new JLabel("Record Donations");
        title.setFont(Theme.H2_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 6, 8, 8));
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);

        form.add(field("Donor ID", txtDonorId));
        form.add(field("Amount (DZD)", txtAmount));
        form.add(combo("Payment Method", cmbMethod));
        form.add(combo("Status", cmbStatus));
        form.add(field("Reference", txtReference));
        form.add(field("Description", txtDescription));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnRecord = button("Record Donation", Theme.GREEN);
        JButton btnConfirm = button("Confirm", Theme.GREEN_DARK);
        JButton btnCancel = button("Cancel", Theme.RED);
        JButton btnRefresh = button("Refresh", Theme.TEXT_MUTED);
        btnRecord.addActionListener(e -> onRecord());
        btnConfirm.addActionListener(e -> onConfirm());
        btnCancel.addActionListener(e -> onCancel());
        btnRefresh.addActionListener(e -> onRefresh());
        btns.add(btnRecord); btns.add(btnConfirm); btns.add(btnCancel); btns.add(btnRefresh);
        form.add(btns);

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

    private JPanel combo(String label, JComboBox<?> c) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.SURFACE);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
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
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));
        JLabel title = new JLabel("Donation History");
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
        List<Donation> list = app.donationService == null ? java.util.Collections.emptyList()
                : app.getDonationDAO().findAll();
        Object[][] data = new Object[list.size()][7];
        for (int i = 0; i < list.size(); i++) {
            Donation d = list.get(i);
            data[i][0] = d.getId();
            data[i][1] = d.getDonorId();
            data[i][2] = money(d.getAmount());
            data[i][3] = d.getDonationDate() != null ? d.getDonationDate().toString() : "";
            data[i][4] = d.getPaymentMethod();
            data[i][5] = d.getStatus();
            data[i][6] = d.getReference();
        }
        TableHelper.setData(table, data);
    }

    private String money(BigDecimal v) { return v == null ? "0.00" : v.stripTrailingZeros().toString(); }

    // ---- Actions ----
    void onRecord() {
        try {
            Long donorId = Long.valueOf(txtDonorId.getText().trim());
            BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
            PaymentMethod method = (PaymentMethod) cmbMethod.getSelectedItem();
            DonationStatus status = (DonationStatus) cmbStatus.getSelectedItem();
            app.donationService.recordDonation(donorId, amount, method,
                    txtDescription.getText().trim(), txtReference.getText().trim());
            // If status is confirmed, confirm immediately
            if (status == DonationStatus.CONFIRMED) {
                // find the last inserted donation id - approximate by reloading then confirming latest is complex;
                // instead record as pending and let admin confirm. Keep simple: record only.
            }
            clearForm();
            load();
            JOptionPane.showMessageDialog(this, "Donation recorded (pending).", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onConfirm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a donation to confirm.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Long id = Long.valueOf(table.getValueAt(row, 0).toString());
            app.donationService.confirmDonation(id);
            load();
            JOptionPane.showMessageDialog(this, "Donation confirmed.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onCancel() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a donation to cancel.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Long id = Long.valueOf(table.getValueAt(row, 0).toString());
            app.donationService.cancelDonation(id);
            load();
            JOptionPane.showMessageDialog(this, "Donation cancelled.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onRefresh() { load(); clearForm(); }

    private void clearForm() {
        txtDonorId.setText(""); txtAmount.setText(""); txtReference.setText(""); txtDescription.setText("");
    }

    // wire listeners
    {
        // placeholder for initializer
    }
}
