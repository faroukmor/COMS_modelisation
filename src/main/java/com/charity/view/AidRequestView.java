package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.AidRequest;
import com.charity.model.Beneficiary;
import com.charity.model.RequestStatus;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Aid request management: submit requests on behalf of beneficiaries and review them.
 */
public class AidRequestView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(new String[]{"ID", "Beneficiary", "Type", "Requested",
            "Approved", "Distributed", "Status", "Description"});
    private final JComboBox<Item> cmbBeneficiary = UI.styleCombo(new JComboBox<>());
    private final JTextField txtRequested = UI.styleField(new JTextField(), "0.00");
    private final JTextField txtDescription = UI.styleField(new JTextField());
    private final JTextField txtApproved = UI.styleField(new JTextField(), "0.00");
    private final JTextField txtNotes = UI.styleField(new JTextField());
    private final JTextField txtSearch = UI.searchField("Search requests...");
    private final JLabel reviewTitle = cardTitle("Review request");
    private final JLabel reviewHint = UI.muted("Select a request in the table to review it.");
    private final JButton btnApprove = UI.button("Approve", UI.Style.PRIMARY, Icons.Kind.CHECK);
    private final JButton btnReject = UI.button("Reject", UI.Style.DANGER);
    private final JButton btnReview = UI.button("Mark under review", UI.Style.SECONDARY);
    private Long selectedId;

    public AidRequestView(AppController app) {
        super(app);
        add(UI.pageHeader("Aid requests", "Receive requests, then approve or reject them.", txtSearch), BorderLayout.NORTH);
        TableHelper.attachFilter(table, txtSearch);
        UI.Card list = TableHelper.tableCard("All requests", table, null);

        UI.FormGrid submit = new UI.FormGrid(1);
        submit.add("Beneficiary", cmbBeneficiary);
        submit.add("Requested amount (DZD)", txtRequested);
        submit.add("Description", txtDescription);
        JButton btnSubmit = UI.button("Submit request", UI.Style.PRIMARY, Icons.Kind.PLUS);
        btnSubmit.addActionListener(e -> onSubmit());
        UI.Card submitCard = formCard(cardTitle("New request"), submit, UI.buttonRow(btnSubmit));

        UI.FormGrid review = new UI.FormGrid(1);
        review.add("Approved amount (DZD)", txtApproved);
        review.add("Admin notes", txtNotes);
        JPanel reviewBody = new JPanel(new BorderLayout(0, 12));
        reviewBody.setOpaque(false);
        reviewBody.add(reviewHint, BorderLayout.NORTH);
        reviewBody.add(review, BorderLayout.CENTER);
        JPanel reviewButtons = new JPanel(new java.awt.GridLayout(2, 2, 10, 10));
        reviewButtons.setOpaque(false);
        reviewButtons.add(btnApprove);
        reviewButtons.add(btnReject);
        reviewButtons.add(btnReview);
        UI.Card reviewCard = formCard(reviewTitle, reviewBody, reviewButtons);

        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);
        right.add(submitCard, BorderLayout.NORTH);
        right.add(reviewCard, BorderLayout.CENTER);
        add(masterDetail(list, right, 390), BorderLayout.CENTER);

        setReviewEnabled(false);
        btnApprove.addActionListener(e -> onReview(RequestStatus.APPROVED));
        btnReject.addActionListener(e -> onReview(RequestStatus.REJECTED));
        btnReview.addActionListener(e -> onReview(RequestStatus.UNDER_REVIEW));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected(table.getSelectedRow());
            }
        });
        load();
    }

    private record Data(List<AidRequest> requests, List<Beneficiary> beneficiaries) {
    }

    private void load() {
        async(() -> new Data(app.getRequestDAO().findAll(), app.beneficiaryService.findAll()), data -> {
            Map<Long, String> names = new HashMap<>();
            DefaultComboBoxModel<Item> model = new DefaultComboBoxModel<>();
            for (Beneficiary b : data.beneficiaries()) {
                names.put(b.getId(), b.getDisplayName());
                model.addElement(new Item(b.getId(), b.getDisplayName() + "  (#" + b.getId() + ")"));
            }
            cmbBeneficiary.setModel(model);
            List<AidRequest> list = data.requests();
            Object[][] rows = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                AidRequest r = list.get(i);
                rows[i] = new Object[]{r.getId(), names.getOrDefault(r.getBeneficiaryId(), "#" + r.getBeneficiaryId()),
                        r.getBeneficiaryType(), r.getRequestedAmount(), r.getApprovedAmount(),
                        r.getDistributedAmount(), r.getStatus(), r.getDescription()};
            }
            TableHelper.setData(table, rows);
            onRowSelected(-1);
        });
    }

    private void setReviewEnabled(boolean on) {
        btnApprove.setEnabled(on);
        btnReject.setEnabled(on);
        btnReview.setEnabled(on);
        txtApproved.setEnabled(on);
        txtNotes.setEnabled(on);
    }

    void onRowSelected(int row) {
        if (row < 0) {
            selectedId = null;
            reviewTitle.setText("Review request");
            reviewHint.setText("Select a request in the table to review it.");
            setReviewEnabled(false);
            return;
        }
        selectedId = (Long) table.getValueAt(row, 0);
        RequestStatus status = (RequestStatus) table.getValueAt(row, 6);
        BigDecimal requested = (BigDecimal) table.getValueAt(row, 3);
        boolean reviewable = status == RequestStatus.PENDING || status == RequestStatus.UNDER_REVIEW;
        reviewTitle.setText("Review request #" + selectedId);
        reviewHint.setText(reviewable ? "Requested: " + Format.money(requested)
                : "This request is already " + Format.label(status).toLowerCase() + ".");
        setReviewEnabled(reviewable);
        btnReview.setEnabled(status == RequestStatus.PENDING);
        txtApproved.setText(reviewable ? requested.stripTrailingZeros().toPlainString() : "");
        txtNotes.setText("");
    }

    void onSubmit() {
        Item ben = (Item) cmbBeneficiary.getSelectedItem();
        if (ben == null) {
            toastError("Add a beneficiary first (Beneficiaries page).");
            return;
        }
        final BigDecimal amount;
        try {
            amount = Format.parseAmount(txtRequested.getText());
        } catch (IllegalArgumentException ex) {
            toastError(ex.getMessage());
            return;
        }
        async(() -> app.requestService.submitRequest(ben.id(), amount, txtDescription.getText()), id -> {
            toast("Request #" + id + " submitted.");
            txtRequested.setText("");
            txtDescription.setText("");
            load();
        });
    }

    void onReview(RequestStatus status) {
        if (selectedId == null) {
            return;
        }
        Long id = selectedId;
        BigDecimal approved;
        try {
            // only an approval needs an amount; rejecting or marking under review must not demand one
            approved = status == RequestStatus.APPROVED ? Format.parseAmount(txtApproved.getText()) : BigDecimal.ZERO;
        } catch (IllegalArgumentException ex) {
            toastError(ex.getMessage());
            return;
        }
        Long reviewer = app.getCurrentUser().getId();
        String notes = txtNotes.getText().trim();
        async(() -> {
            if (status == RequestStatus.UNDER_REVIEW) {
                app.requestService.markUnderReview(id, reviewer);
            } else {
                app.requestService.reviewRequest(id, status, reviewer, notes, approved);
            }
            return Boolean.TRUE;
        }, ok -> {
            toast("Request #" + id + " " + Format.label(status).toLowerCase() + ".");
            load();
        });
    }
}
