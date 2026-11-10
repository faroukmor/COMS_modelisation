package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Role;
import com.charity.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Main application shell: sidebar navigation + content area. The sidebar is only visible once a
 * user is signed in, and shows only the pages that user's role may open.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int SIDEBAR_WIDTH = 252;

    private final JPanel content = new JPanel(new BorderLayout());
    private final JPanel sidebar = new Sidebar();
    private final JPanel nav = new JPanel();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final JPanel userChip = new JPanel(new BorderLayout(12, 0));
    private JLabel userName;
    private JLabel userRole;
    private JLabel avatar;
    private AppController appController;

    public MainFrame() {
        Theme.init();
        setTitle("CharityFund - Charity Fund Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1360, 860);
        setLocationRelativeTo(null);
        setIconImage(appIcon());
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        buildSidebar();
        content.setBackground(Theme.BG);
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        sidebar.setVisible(false); // hidden until someone signs in
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    // ------------------------------------------------------------------ sidebar

    private void buildSidebar() {
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 18, 16));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(0, 6, 28, 6));
        JLabel mark = new JLabel(Icons.of(Icons.Kind.HEART, 22, Color.WHITE), JLabel.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.GREEN_MAIN, getWidth(), getHeight(), Theme.GREEN));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mark.setPreferredSize(new Dimension(42, 42));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(UI.label("CharityFund", Theme.font(java.awt.Font.BOLD, 18), Color.WHITE));
        names.add(UI.label("Management System", Theme.BODY_SMALL, new Color(0xA7, 0xF3, 0xD0)));
        brand.add(mark, BorderLayout.WEST);
        brand.add(names, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        sidebar.add(nav, BorderLayout.CENTER);

        userChip.setOpaque(false);
        userChip.setBorder(new EmptyBorder(14, 10, 4, 6));
        avatar = new JLabel("?", JLabel.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.GREEN_MAIN);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setFont(Theme.font(java.awt.Font.BOLD, 15));
        avatar.setForeground(Color.WHITE);
        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        userName = UI.label("Not signed in", Theme.font(java.awt.Font.BOLD, 13), Color.WHITE);
        userRole = UI.label(" ", Theme.BODY_SMALL, new Color(0xA7, 0xF3, 0xD0));
        who.add(userName);
        who.add(userRole);
        userChip.add(avatar, BorderLayout.WEST);
        userChip.add(who, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(new JPanel() {
            private static final long serialVersionUID = 1L;

            {
                setOpaque(false);
                setPreferredSize(new Dimension(1, 1));
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 40));
                g.fillRect(0, 0, getWidth(), 1);
            }
        }, BorderLayout.NORTH);
        footer.add(userChip, BorderLayout.CENTER);
        NavButton logout = new NavButton("Sign out", Icons.Kind.LOGOUT);
        logout.addActionListener(() -> {
            if (appController != null) {
                appController.logout();
            }
        });
        JPanel logoutWrap = new JPanel(new BorderLayout());
        logoutWrap.setOpaque(false);
        logoutWrap.setBorder(new EmptyBorder(8, 0, 0, 0));
        logoutWrap.add(logout, BorderLayout.CENTER);
        footer.add(logoutWrap, BorderLayout.SOUTH);
        sidebar.add(footer, BorderLayout.SOUTH);
    }

    private void rebuildNav(User user) {
        nav.removeAll();
        navButtons.clear();
        nav.add(UI.label("MENU", Theme.font(java.awt.Font.BOLD, 11), new Color(0x6E, 0xE7, 0xB7)));
        nav.add(Box.createVerticalStrut(10));
        if (user == null) {
            return;
        }
        if (user.getRole() == Role.ADMIN) {
            addNav("dashboard", "Dashboard", Icons.Kind.DASHBOARD, () -> appController.showDashboard());
            addNav("donors", "Donors", Icons.Kind.HEART, () -> appController.showDonors());
            addNav("donations", "Donations", Icons.Kind.COIN, () -> appController.showDonations());
            addNav("beneficiaries", "Beneficiaries", Icons.Kind.USERS, () -> appController.showBeneficiaries());
            addNav("aidrequests", "Aid Requests", Icons.Kind.FILE, () -> appController.showAidRequests());
            addNav("distributions", "Distributions", Icons.Kind.SEND, () -> appController.showDistributions());
            addNav("reports", "Reports", Icons.Kind.CHART, () -> appController.showReports());
            nav.add(Box.createVerticalStrut(14));
            nav.add(UI.label("ADMINISTRATION", Theme.font(java.awt.Font.BOLD, 11), new Color(0x6E, 0xE7, 0xB7)));
            nav.add(Box.createVerticalStrut(10));
            addNav("users", "Users", Icons.Kind.USER_PLUS, () -> appController.showUsers());
        } else {
            addNav("portal", "My Account", Icons.Kind.USER, () -> appController.showPortal());
        }
        nav.revalidate();
        nav.repaint();
    }

    private void addNav(String id, String label, Icons.Kind icon, Runnable action) {
        NavButton b = new NavButton(label, icon);
        b.addActionListener(action);
        navButtons.put(id, b);
        nav.add(b);
        nav.add(Box.createVerticalStrut(4));
    }

    private void highlight(String viewId) {
        navButtons.forEach((id, b) -> b.setActive(id.equals(viewId)));
    }

    // ------------------------------------------------------------------ content

    /** Replaces the page shown in the content area. */
    public void showView(String viewId, JComponent view) {
        if (view == null) {
            return;
        }
        content.removeAll();
        content.add(view, BorderLayout.CENTER);
        highlight(viewId);
        content.revalidate();
        content.repaint();
    }

    public JPanel getContent() {
        return content;
    }

    /** Signed-in user: shows the sidebar with the right menu. {@code null} hides it (login screen). */
    public void setUser(User user) {
        sidebar.setVisible(user != null);
        rebuildNav(user);
        if (user != null) {
            String name = user.getFullName() == null || user.getFullName().isBlank()
                    ? user.getUsername() : user.getFullName();
            userName.setText(name);
            userRole.setText(Format.label(user.getRole()));
            avatar.setText(name.substring(0, 1).toUpperCase());
        }
        revalidate();
        repaint();
    }

    public void toast(String message) {
        Toast.show(this, message, Toast.Kind.SUCCESS);
    }

    // ------------------------------------------------------------------- bits

    private static BufferedImage appIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, Theme.GREEN_MAIN, 64, 64, Theme.GREEN_DARK));
        g.fill(new RoundRectangle2D.Double(0, 0, 64, 64, 18, 18));
        g.translate(14, 14);
        Icons.of(Icons.Kind.HEART, 36, Color.WHITE).paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    /** Dark green gradient panel. */
    private static final class Sidebar extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, Theme.GREEN_DARKER, 0, getHeight(), new Color(0x02, 0x2C, 0x22)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** Sidebar entry: icon + label, hover and active states. */
    static final class NavButton extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String text;
        private final Icons.Kind icon;
        private boolean hover;
        private boolean active;
        private Runnable action = () -> { };

        NavButton(String text, Icons.Kind icon) {
            this.text = text;
            this.icon = icon;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(200, 44));
            setAlignmentX(LEFT_ALIGNMENT);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        void addActionListener(Runnable r) {
            this.action = r;
        }

        void setActive(boolean a) {
            this.active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) {
                g2.setColor(Theme.GREEN);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            }
            Color fg = active ? Color.WHITE : new Color(0xD1, 0xFA, 0xE5);
            Icons.of(icon, 20, fg).paintIcon(this, g2, 14, (getHeight() - 20) / 2);
            g2.setColor(fg);
            g2.setFont(active ? Theme.font(java.awt.Font.BOLD, 14) : Theme.NAV_FONT);
            int ty = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
            g2.drawString(text, 46, ty);
            g2.dispose();
        }
    }
}
