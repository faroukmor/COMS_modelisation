package com.charity.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.charity.controller.AppController;
import com.charity.model.User;

/**
 * Main application shell: sidebar navigation + content area.
 * Views are swapped into the center content panel via showView().
 */
public class MainFrame extends JFrame {
    private static final int SIDEBAR_WIDTH = 240;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JPanel sidebar = new JPanel();
    private JLabel userLabel;
    private AppController appController;

    public MainFrame() {
        Theme.init();
        setTitle("Charity Fund Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);

        buildSidebar();
        buildContent();

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        // Show login by default
        showView("login", null);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    private void buildSidebar() {
        sidebar.setBackground(Theme.GREEN_DARK);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel header = new JPanel();
        header.setBackground(Theme.GREEN_DARK);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 26, 20));

        JLabel logo = new JLabel("CharityFund");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(Theme.BODY_SMALL);
        subtitle.setForeground(new Color(160, 200, 165));

        JPanel left = new JPanel();
        left.setBackground(Theme.GREEN_DARK);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(logo);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);
        header.add(left, BorderLayout.WEST);

        userLabel = new JLabel("Not logged in");
        userLabel.setFont(Theme.BODY_SMALL);
        userLabel.setForeground(new Color(160, 200, 165));
        userLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        header.add(userLabel, BorderLayout.SOUTH);

        sidebar.add(header, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setBackground(Theme.GREEN_DARK);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        addNavButton(nav, "Dashboard", e -> appController.showDashboard());
        addNavButton(nav, "Donors", e -> appController.showDonors());
        addNavButton(nav, "Donations", e -> appController.showDonations());
        addNavButton(nav, "Beneficiaries", e -> appController.showBeneficiaries());
        addNavButton(nav, "Aid Requests", e -> appController.showAidRequests());
        addNavButton(nav, "Distributions", e -> appController.showDistributions());
        addNavButton(nav, "Reports", e -> appController.showReports());

        sidebar.add(nav, BorderLayout.CENTER);

        JPanel logoutWrap = new JPanel();
        logoutWrap.setBackground(Theme.GREEN_DARK);
        logoutWrap.setBorder(new EmptyBorder(12, 12, 12, 12));
        JButton logout = new JButton("Logout");
        styleSidebarButton(logout, true);
        logout.setAlignmentX(0f);
        logout.addActionListener(e -> appController.logout());
        logoutWrap.add(logout);
        sidebar.add(logoutWrap, BorderLayout.SOUTH);
    }

    private void addNavButton(JPanel nav, String label, ActionListener action) {
        JButton b = new JButton(label);
        b.setAlignmentX(0f);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.addActionListener(action);
        styleSidebarButton(b, false);
        nav.add(b);
        nav.add(Box.createVerticalStrut(6));
    }

    private void buildContent() {
        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    public void showView(String viewId, JComponent view) {
        if (view != null) {
            // Remove existing views and add the new one with constraint
            content.removeAll();
            content.add(view, viewId);
            cardLayout.show(content, viewId);
        }
    }

    public JPanel getContent() {
        return content;
    }

    public void setUser(User user) {
        if (user != null) {
            userLabel.setText(user.getFullName() + "  (" + user.getRole() + ")");
        } else {
            userLabel.setText("Not logged in");
        }
    }

    /** Style a sidebar navigation button. */
    public static void styleSidebarButton(JButton b, boolean active) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(active ? Theme.GREEN_MAIN : Theme.GREEN_DARK);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.BUTTON_FONT);
        b.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
    }
}
