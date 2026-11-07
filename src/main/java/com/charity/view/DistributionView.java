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
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;

/**
 * Financial distributions: disburse aid against approved requests and view history.
 */
public class DistributionView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Request #", "Beneficiary #", "Amount", "Date", "Method", "Reference"});
    private final JTextField txtRequestId = new JTextField();
    private final JTextField txtAmount = new JTextField();
    private final JComboBox<PaymentMethod> cmbMethod = new JComboBox<>(new DefaultComboBoxModel<>(PaymentMethod.values()));
    private final JTextField txtReference = new JTextField();
    private final JTextField txtDescription = new JTextField();

    public DistributionView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        load();
        wireTable();
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel title = new JLabel("Disburse Financial Aid");
        title.setFont(Theme.H2_FONT);
        title.setForeground(Theme.TEXT_DARK);
        outer.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBackground(Theme.SURFACE);
        form.setBorder(Theme.CARD_BORDER);
        form.add(field("Approved Request ID", txtRequestId));
        form.add(field("Amount (DZD)", txtAmount));
        form.add(combo("Payment Method", cmbMethod));
        form.add(field("Reference", txtReference));
        form.add(field("Description", txtDescription));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(Theme.SURFACE);
        JButton btnDisburse = button("Disburse Aid", Theme.GREEN_DARK);
        JButton btnRefresh = button("Refresh", Theme.TEXT_MUTED);
        btnDisburse.addActionListener(e -> onDisburse());
        btnRefresh.addActionListener(e -> load());
        btns.add(btnDisburse); btns.add(btnRefresh);
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
        JLabel title = new JLabel("Distribution History");
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
        List<Distribution> list = app.distributionService == null ? java.util.Collections.emptyList()
                : app.getDistributionDAO().findAll();
        Object[][] data = new Object[list.size()][7];
        for (int i = 0; i < list.size(); i++) {
            Distribution d = list.get(i);
            data[i][0] = d.getId();
            data[i][1] = d.getAidRequestId();
            data[i][2] = d.getBeneficiaryId();
            data[i][3] = money(d.getAmount());
            data[i][4] = d.getDistributionDate() != null ? d.getDistributionDate().toString() : "";
            data[i][5] = d.getPaymentMethod();
            data[i][6] = d.getReference();
        }
        TableHelper.setData(table, data);
    }

    private String money(BigDecimal v) { return v == null ? "0.00" : v.stripTrailingZeros().toString(); }

    private void wireTable() {
        table.getSelectionModel().addListSelectionListener(e -> {});
    }

    void onDisburse() {
        try {
            Long requestId = Long.valueOf(txtRequestId.getText().trim());
            BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
            PaymentMethod method = (PaymentMethod) cmbMethod.getSelectedItem();
            Long distId = app.distributionService.disburse(requestId, amount, method,
                    txtReference.getText().trim(), txtDescription.getText().trim());
            txtRequestId.setText(""); txtAmount.setText(""); txtReference.setText(""); txtDescription.setText("");
            load();
            JOptionPane.showMessageDialog(this, "Aid disbursed (distribution #" + distId + ").", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
