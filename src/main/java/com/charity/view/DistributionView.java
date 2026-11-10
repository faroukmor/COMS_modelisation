package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.AidRequest;
import com.charity.model.Beneficiary;
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;
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
 * Financial distributions: disburse aid against approved requests and view history.
 */
public class DistributionView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Request", "Beneficiary", "Amount", "Date", "Method", "Reference"});
    private final JComboBox<Item> cmbRequest = UI.styleCombo(new JComboBox<>());
    private final JTextField txtAmount = UI.styleField(new JTextField(), "0.00");
    private final JComboBox<PaymentMethod> cmbMethod = UI.enumCombo(PaymentMethod.values());
    private final JTextField txtReference = UI.styleField(new JTextField());
    private final JTextField txtDescription = UI.styleField(new JTextField());
    private final JTextField txtSearch = UI.searchField("Search distributions...");
    private final JLabel balance = UI.label(" ", Theme.H2_FONT, Theme.GREEN_DARK);
    private final Map<Long, BigDecimal> remaining = new HashMap<>();

    public DistributionView(AppController app) {
        super(app);
        add(UI.pageHeader("Distributions", "Give approved aid to beneficiaries from the available balance.", txtSearch),
                BorderLayout.NORTH);
        TableHelper.attachFilter(table, txtSearch);
        UI.Card list = TableHelper.tableCard("Distribution history", table, null);

        UI.FormGrid g = new UI.FormGrid(1);
        g.add("Approved request", cmbRequest);
        g.add("Amount (DZD)", txtAmount);
        g.add("Payment method", cmbMethod);
        g.add("Reference", txtReference);
        g.add("Description", txtDescription);
        JPanel head = new JPanel(new BorderLayout(0, 2));
        head.setOpaque(false);
        head.add(UI.muted("Available balance"), BorderLayout.NORTH);
        head.add(balance, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(head, BorderLayout.NORTH);
        body.add(g, BorderLayout.CENTER);
        JButton btn = UI.button("Disburse aid", UI.Style.PRIMARY, Icons.Kind.SEND);
        btn.addActionListener(e -> onDisburse());
        add(masterDetail(list, formCard(cardTitle("New distribution"), body, UI.buttonRow(btn)), 390), BorderLayout.CENTER);

        cmbRequest.addActionListener(e -> {
            Item it = (Item) cmbRequest.getSelectedItem();
            if (it != null && remaining.containsKey(it.id())) {
                txtAmount.setText(remaining.get(it.id()).stripTrailingZeros().toPlainString());
            }
        });
        load();
    }

    private record Data(List<Distribution> distributions, List<AidRequest> approved,
                        List<Beneficiary> beneficiaries, BigDecimal balance) {
    }

    private void load() {
        async(() -> new Data(app.getDistributionDAO().findAll(),
                app.getRequestDAO().findByStatus(RequestStatus.APPROVED),
                app.beneficiaryService.findAll(), app.financialService.getAvailableBalance()), data -> {
            Map<Long, String> names = new HashMap<>();
            data.beneficiaries().forEach(b -> names.put(b.getId(), b.getDisplayName()));
            balance.setText(Format.money(data.balance()));
            balance.setForeground(data.balance().signum() < 0 ? Theme.RED : Theme.GREEN_DARK);

            remaining.clear();
            DefaultComboBoxModel<Item> model = new DefaultComboBoxModel<>();
            for (AidRequest r : data.approved()) {
                BigDecimal left = r.getRemainingAmount();
                if (left.signum() > 0) {
                    remaining.put(r.getId(), left);
                    model.addElement(new Item(r.getId(), "#" + r.getId() + "  "
                            + names.getOrDefault(r.getBeneficiaryId(), "Beneficiary #" + r.getBeneficiaryId())
                            + "  -  " + Format.money(left) + " left"));
                }
            }
            cmbRequest.setModel(model);

            List<Distribution> list = data.distributions();
            Object[][] rows = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                Distribution d = list.get(i);
                rows[i] = new Object[]{d.getId(), "Request #" + d.getAidRequestId(),
                        names.getOrDefault(d.getBeneficiaryId(), "#" + d.getBeneficiaryId()),
                        d.getAmount(), d.getDistributionDate(), d.getPaymentMethod(), d.getReference()};
            }
            TableHelper.setData(table, rows);
        });
    }

    void onDisburse() {
        Item req = (Item) cmbRequest.getSelectedItem();
        if (req == null) {
            toastError("There is no approved request waiting for a distribution.");
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
        async(() -> app.distributionService.disburse(req.id(), amount, method,
                txtReference.getText().trim(), txtDescription.getText().trim()), id -> {
            toast("Aid disbursed (distribution #" + id + ").");
            txtAmount.setText("");
            txtReference.setText("");
            txtDescription.setText("");
            load();
        });
    }
}
