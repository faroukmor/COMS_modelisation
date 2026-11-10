package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.AidRequest;
import com.charity.model.Distribution;
import com.charity.model.Donation;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Financial and operational reports with CSV export (the filter box applies to the export too).
 */
public class ReportView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final StatCard cConfirmed = new StatCard("Confirmed donations", Icons.Kind.ARROW_DOWN, Theme.GREEN, Theme.GREEN_LIGHT);
    private final StatCard cDistributed = new StatCard("Distributed funds", Icons.Kind.ARROW_UP, Theme.RED, Theme.RED_LIGHT);
    private final StatCard cBalance = new StatCard("Available balance", Icons.Kind.WALLET, Theme.GREEN_DARK, Theme.GREEN_LIGHT);
    private final JTable donationsTable = TableHelper.createTable(
            new String[]{"ID", "Donor #", "Amount", "Date", "Method", "Status"});
    private final JTable distributionsTable = TableHelper.createTable(
            new String[]{"ID", "Request #", "Amount", "Date", "Method"});
    private final JTable requestsTable = TableHelper.createTable(
            new String[]{"ID", "Beneficiary #", "Requested", "Approved", "Status"});
    private final JTextField txtFilter = UI.searchField("Filter all tables...");

    public ReportView(AppController app) {
        super(app);
        JButton export = UI.button("Export CSV", UI.Style.PRIMARY, Icons.Kind.DOWNLOAD);
        export.addActionListener(e -> exportCsv());
        add(UI.pageHeader("Reports", "Financial summary and full transaction history.", txtFilter, export),
                BorderLayout.NORTH);
        for (JTable t : new JTable[]{donationsTable, distributionsTable, requestsTable}) {
            TableHelper.attachFilter(t, txtFilter);
        }
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(cConfirmed);
        stats.add(cDistributed);
        stats.add(cBalance);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BUTTON_FONT);
        tabs.addTab("Donations", TableHelper.tableCard("Donation history", donationsTable, null));
        tabs.addTab("Distributions", TableHelper.tableCard("Distribution history", distributionsTable, null));
        tabs.addTab("Aid requests", TableHelper.tableCard("Aid requests", requestsTable, null));
        tabs.setBackground(Theme.BG);
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(stats, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        refresh();
    }

    private record Data(BigDecimal confirmed, BigDecimal distributed, List<Donation> donations,
                        List<Distribution> distributions, List<AidRequest> requests) {
    }

    public void refresh() {
        async(() -> new Data(app.financialService.getTotalConfirmedDonations(),
                app.financialService.getTotalDistributed(), app.getDonationDAO().findAll(),
                app.getDistributionDAO().findAll(), app.getRequestDAO().findAll()), d -> {
            cConfirmed.setValue(Format.money(d.confirmed()));
            cDistributed.setValue(Format.money(d.distributed()));
            BigDecimal bal = d.confirmed().subtract(d.distributed());
            cBalance.setValue(Format.money(bal));
            cBalance.setValueColor(bal.signum() < 0 ? Theme.RED : Theme.GREEN_DARK);

            Object[][] a = new Object[d.donations().size()][];
            for (int i = 0; i < a.length; i++) {
                Donation x = d.donations().get(i);
                a[i] = new Object[]{x.getId(), x.getDonorId(), x.getAmount(), x.getDonationDate(),
                        x.getPaymentMethod(), x.getStatus()};
            }
            TableHelper.setData(donationsTable, a);
            Object[][] b = new Object[d.distributions().size()][];
            for (int i = 0; i < b.length; i++) {
                Distribution x = d.distributions().get(i);
                b[i] = new Object[]{x.getId(), x.getAidRequestId(), x.getAmount(), x.getDistributionDate(), x.getPaymentMethod()};
            }
            TableHelper.setData(distributionsTable, b);
            Object[][] c = new Object[d.requests().size()][];
            for (int i = 0; i < c.length; i++) {
                AidRequest x = d.requests().get(i);
                c[i] = new Object[]{x.getId(), x.getBeneficiaryId(), x.getRequestedAmount(), x.getApprovedAmount(), x.getStatus()};
            }
            TableHelper.setData(requestsTable, c);
        });
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("charity-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        String filter = txtFilter.getText().trim();
        try {
            writeCsv(file, filter);
            toast("Report exported to " + file.getName() + ".");
        } catch (IOException ex) {
            toastError("Export failed: " + ex.getMessage());
        }
    }

    /** Writes the summary plus the currently visible rows of every table. UTF-8 with BOM so Excel opens it correctly. */
    void writeCsv(File file, String filter) throws IOException {
        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Charity Fund Management System - Report\r\n");
            w.write("Filter," + Csv.escape(filter.isEmpty() ? "none" : filter) + "\r\n\r\n");
            section(w, "Donations", donationsTable);
            section(w, "Distributions", distributionsTable);
            section(w, "Aid requests", requestsTable);
        }
    }

    private static void section(Writer w, String title, JTable table) throws IOException {
        w.write(Csv.escape(title) + "\r\n");
        StringBuilder head = new StringBuilder();
        for (int c = 0; c < table.getColumnCount(); c++) {
            head.append(c > 0 ? "," : "").append(Csv.escape(table.getColumnName(c)));
        }
        w.write(head + "\r\n");
        for (int r = 0; r < table.getRowCount(); r++) { // view rows: respects the filter and sort order
            StringBuilder line = new StringBuilder();
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object v = table.getValueAt(r, c);
                String text = v instanceof BigDecimal bd ? bd.toPlainString()
                        : v instanceof java.time.LocalDateTime t ? t.toString() : v == null ? "" : v.toString();
                line.append(c > 0 ? "," : "").append(Csv.escape(text));
            }
            w.write(line + "\r\n");
        }
        w.write("\r\n");
    }
}
