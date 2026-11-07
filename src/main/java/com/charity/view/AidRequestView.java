package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import com.charity.model.AidRequest;
import com.charity.model.RequestStatus;

/**
 * Aid request management: submit requests, review/approve/reject.
 */
public class AidRequestView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Beneficiary #", "Type", "Requested", "Approved", "Distributed", "Status", "Description"});
    private final JTextField txtBeneficiaryId = new JTextField();
    private final JTextField txtRequested = new JTextField();
    private final JTextField txtDescription = new JTextField();
    private final JTextField txtReviewId = new JTextField();
    private final JComboBox<RequestStatus> cmbReviewStatus = new JComboBox<>(
            new DefaultComboBoxModel<>(new RequestStatus[]{RequestStatus.APPROVED, RequestStatus.REJECTED}));
    private final JTextField txtApprovedAmount = new JTextField();
    private final JTextField txtAdminNotes = new JTextField();
    private Long editingId = null;

    public AidRequestView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        add(buildSubmitForm(), BorderLayout.NORTH);
        add(buildReviewForm(), BorderLayout.CENTER);
        add(buildTable(), BorderLayout.SOUTH);
        load();
        wireTable();
    }

    private JPanel buildSubmitForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel title = new JLabel("Submit Aid Request");
        title.setFont(Theme.H3_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(1, 6, 8, 8));
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);
        form.add(field("Beneficiary ID", txtBeneficiaryId));
        form.add(field("Requested Amount (DZD)", txtRequested));
        form.add(field("Description", txtDescription));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnSubmit = button("Submit Request", Theme.GREEN);
        btnSubmit.addActionListener(e -> onSubmit());
        btns.add(btnSubmit);
        form.add(btns);
        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildReviewForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel title = new JLabel("Review Request (Approve / Reject)");
        title.setFont(Theme.H3_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);
        form.add(field("Request ID", txtReviewId));
        form.add(combo("Decision", cmbReviewStatus));
        form.add(field("Approved Amount (DZD)", txtApprovedAmount));
        form.add(field("Admin Notes", txtAdminNotes));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnApprove = button("Approve", Theme.GREEN_DARK);
        JButton btnReject = button("Reject", Theme.RED);
        JButton btnClear = button("Clear", Theme.TEXT_MUTED);
        btnApprove.addActionListener(e -> onReview(RequestStatus.APPROVED));
        btnReject.addActionListener(e -> onReview(RequestStatus.REJECTED));
        btnClear.addActionListener(e -> clearReview());
        btns.add(btnApprove); btns.add(btnReject); btns.add(btnClear);
        form.add(btns);
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
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));
        JLabel title = new JLabel("Aid Requests");
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
        List<AidRequest> list = app.requestService == null ? java.util.Collections.emptyList()
                : app.getRequestDAO().findAll();
        Object[][] data = new Object[list.size()][8];
        for (int i = 0; i < list.size(); i++) {
            AidRequest r = list.get(i);
            data[i][0] = r.getId();
            data[i][1] = r.getBeneficiaryId();
            data[i][2] = r.getBeneficiaryType();
            data[i][3] = money(r.getRequestedAmount());
            data[i][4] = money(r.getApprovedAmount());
            data[i][5] = money(r.getDistributedAmount());
            data[i][6] = r.getStatus();
            data[i][7] = r.getDescription();
        }
        TableHelper.setData(table, data);
    }

    private String money(BigDecimal v) { return v == null ? "0.00" : v.stripTrailingZeros().toString(); }

    private void wireTable() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                editingId = Long.valueOf(table.getValueAt(row, 0).toString());
                txtReviewId.setText(table.getValueAt(row, 0).toString());
            }
        });
    }

    private void clearReview() {
        txtReviewId.setText(""); txtApprovedAmount.setText(""); txtAdminNotes.setText("");
        cmbReviewStatus.setSelectedItem(RequestStatus.APPROVED);
        editingId = null;
    }

    void onSubmit() {
        try {
            Long beneficiaryId = Long.valueOf(txtBeneficiaryId.getText().trim());
            BigDecimal amount = new BigDecimal(txtRequested.getText().trim());
            app.requestService.submitRequest(beneficiaryId, amount, txtDescription.getText().trim());
            txtBeneficiaryId.setText(""); txtRequested.setText(""); txtDescription.setText("");
            load();
            JOptionPane.showMessageDialog(this, "Aid request submitted.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void onReview(RequestStatus status) {
        try {
            Long id = Long.valueOf(txtReviewId.getText().trim());
            BigDecimal approved = new BigDecimal(txtApprovedAmount.getText().trim());
            app.requestService.reviewRequest(id, status, app.getCurrentUser().getId(),
                    txtAdminNotes.getText().trim(), approved);
            clearReview();
            load();
            JOptionPane.showMessageDialog(this, "Request " + status + ".", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
