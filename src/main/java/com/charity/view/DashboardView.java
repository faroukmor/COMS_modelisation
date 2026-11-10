package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Distribution;
import com.charity.model.Donation;
import com.charity.model.Donor;
import com.charity.model.RequestStatus;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Administrator dashboard: key financial/operational cards and recent activity tables.
 */
public class DashboardView extends BaseView {
    private static final long serialVersionUID = 1L;

    private record Snapshot(BigDecimal confirmed, BigDecimal distributed, BigDecimal balance,
                            int donors, int beneficiaries, int pending,
                            List<Donation> donations, List<Distribution> distributions,
                            Map<Long, String> donorNames) {
    }

    private final StatCard cConfirmed = new StatCard("Confirmed donations", Icons.Kind.ARROW_DOWN, Theme.GREEN, Theme.GREEN_LIGHT);
    private final StatCard cDistributed = new StatCard("Distributed funds", Icons.Kind.ARROW_UP, Theme.RED, Theme.RED_LIGHT);
    private final StatCard cBalance = new StatCard("Available balance", Icons.Kind.WALLET, Theme.GREEN_DARK, Theme.GREEN_LIGHT);
    private final StatCard cDonors = new StatCard("Donors", Icons.Kind.HEART, Theme.BLUE, Theme.BLUE_LIGHT);
    private final StatCard cBeneficiaries = new StatCard("Beneficiaries", Icons.Kind.USERS, Theme.BLUE, Theme.BLUE_LIGHT);
    private final StatCard cPending = new StatCard("Pending aid requests", Icons.Kind.CLOCK, Theme.AMBER, Theme.AMBER_LIGHT);
    private final UtilisationBar bar = new UtilisationBar();
    private final JTable donationsTable = TableHelper.createTable(new String[]{"Date", "Donor", "Amount", "Status"});
    private final JTable distributionsTable = TableHelper.createTable(new String[]{"Date", "Request", "Amount", "Method"});

    public DashboardView(AppController app) {
        super(app);
        JButton refresh = UI.button("Refresh", UI.Style.SECONDARY, Icons.Kind.REFRESH);
        refresh.addActionListener(e -> refresh());
        String name = app.getCurrentUser() == null ? "" : ", " + app.getCurrentUser().getFullName();
        add(UI.pageHeader("Dashboard", "Welcome back" + name + ". Here is where the fund stands today.", refresh),
                BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(2, 3, 16, 16));
        stats.setOpaque(false);
        for (StatCard c : new StatCard[]{cConfirmed, cDistributed, cBalance, cDonors, cBeneficiaries, cPending}) {
            stats.add(c);
        }
        stats.setPreferredSize(new Dimension(100, 240));

        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);
        tables.add(TableHelper.tableCard("Recent donations", donationsTable, null));
        tables.add(TableHelper.tableCard("Recent distributions", distributionsTable, null));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(stats, BorderLayout.NORTH);
        top.add(bar, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(tables, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        async(() -> {
            BigDecimal confirmed = app.financialService.getTotalConfirmedDonations();
            BigDecimal distributed = app.financialService.getTotalDistributed();
            List<Donor> donors = app.donorService.findAll();
            Map<Long, String> names = new HashMap<>();
            donors.forEach(d -> names.put(d.getId(), d.getFullName()));
            return new Snapshot(confirmed, distributed, confirmed.subtract(distributed),
                    donors.size(), app.beneficiaryService.findAll().size(),
                    app.getRequestDAO().countByStatus(RequestStatus.PENDING),
                    app.getDonationDAO().findAll(), app.getDistributionDAO().findAll(), names);
        }, this::show);
    }

    private void show(Snapshot s) {
        cConfirmed.setValue(Format.money(s.confirmed()));
        cDistributed.setValue(Format.money(s.distributed()));
        cBalance.setValue(Format.money(s.balance()));
        cBalance.setValueColor(s.balance().signum() < 0 ? Theme.RED : Theme.GREEN_DARK);
        cDonors.setValue(String.valueOf(s.donors()));
        cBeneficiaries.setValue(String.valueOf(s.beneficiaries()));
        cPending.setValue(String.valueOf(s.pending()));
        bar.set(s.confirmed(), s.distributed());

        int n = Math.min(5, s.donations().size());
        Object[][] d = new Object[n][4];
        for (int i = 0; i < n; i++) {
            Donation x = s.donations().get(i);
            d[i] = new Object[]{x.getDonationDate(), s.donorNames().getOrDefault(x.getDonorId(), "Donor #" + x.getDonorId()),
                    x.getAmount(), x.getStatus()};
        }
        TableHelper.setData(donationsTable, d);

        int m = Math.min(5, s.distributions().size());
        Object[][] r = new Object[m][4];
        for (int i = 0; i < m; i++) {
            Distribution x = s.distributions().get(i);
            r[i] = new Object[]{x.getDistributionDate(), "Request #" + x.getAidRequestId(), x.getAmount(), x.getPaymentMethod()};
        }
        TableHelper.setData(distributionsTable, r);
        donationsTable.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
        distributionsTable.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
    }

    /** "How much of the confirmed money has already been given out" progress bar. */
    private static final class UtilisationBar extends UI.Card {
        private static final long serialVersionUID = 1L;
        private double ratio;
        private final JLabel caption = UI.label("Fund utilisation", Theme.H3_FONT, Theme.TEXT_DARK);
        private final JLabel percent = UI.label("0%", Theme.H3_FONT, Theme.GREEN_DARK);

        UtilisationBar() {
            padding(16, 22);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.add(caption, BorderLayout.WEST);
            row.add(percent, BorderLayout.EAST);
            JComponent track = new JComponent() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int h = 10;
                    int y = (getHeight() - h) / 2;
                    g2.setColor(Theme.SLATE_LIGHT);
                    g2.fillRoundRect(0, y, getWidth(), h, h, h);
                    int w = (int) Math.round(getWidth() * Math.max(0, Math.min(1, ratio)));
                    if (w > 0) {
                        g2.setColor(ratio > 0.9 ? Theme.AMBER : Theme.GREEN);
                        g2.fillRoundRect(0, y, Math.max(w, h), h, h, h);
                    }
                    g2.dispose();
                }
            };
            track.setPreferredSize(new Dimension(100, 22));
            setLayout(new BorderLayout(0, 6));
            add(row, BorderLayout.NORTH);
            add(track, BorderLayout.CENTER);
        }

        void set(BigDecimal confirmed, BigDecimal distributed) {
            ratio = confirmed.signum() <= 0 ? 0
                    : distributed.divide(confirmed, 4, RoundingMode.HALF_UP).doubleValue();
            percent.setText(Math.round(ratio * 100) + "% of confirmed donations distributed");
            repaint();
        }
    }
}
