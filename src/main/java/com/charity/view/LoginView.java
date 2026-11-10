package com.charity.view;

import com.charity.controller.AppController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Sign-in screen: brand panel on the left, credentials form on the right.
 * Authentication runs on a background thread so the window never freezes.
 */
public class LoginView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final transient AppController app;
    private final JTextField usernameField = UI.styleField(new JTextField(), "Your username");
    private final JPasswordField passwordField = new JPasswordField();
    private final JComponent passwordWrap = UI.passwordField(passwordField, "Your password");
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton loginBtn = UI.button("Sign in", UI.Style.PRIMARY);

    public LoginView(AppController app) {
        this.app = app;
        setBackground(Theme.SURFACE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.46;
        add(buildHero(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.54;
        add(buildForm(), gbc);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());
        loginBtn.addActionListener(e -> attemptLogin());
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    // ------------------------------------------------------------------- hero

    private JComponent buildHero() {
        JPanel hero = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.GREEN_DARKER, getWidth(), getHeight(), Theme.GREEN));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fillOval(-120, getHeight() - 260, 420, 420);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 220, -140, 380, 390);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() / 2 - 60, getHeight() / 2 + 40, 220, 220);
                g2.dispose();
            }
        };
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(48, 56, 48, 56));
        hero.setPreferredSize(new Dimension(460, 600));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel(Icons.of(Icons.Kind.HEART, 24, Color.WHITE), JLabel.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mark.setPreferredSize(new Dimension(46, 46));
        brand.add(mark, BorderLayout.WEST);
        brand.add(UI.label("CharityFund", Theme.font(Font_BOLD, 22), Color.WHITE), BorderLayout.CENTER);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        top.add(brand);
        hero.add(top, BorderLayout.NORTH);

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.add(Box.createVerticalGlue());
        JLabel h1 = UI.label("<html>Giving, managed<br>with care.</html>", Theme.font(Font_BOLD, 38), Color.WHITE);
        h1.setAlignmentX(LEFT_ALIGNMENT);
        middle.add(h1);
        middle.add(Box.createVerticalStrut(14));
        JLabel p = UI.label("<html>One place to record donations, review aid requests and distribute funds transparently.</html>",
                Theme.font(0, 15), new Color(0xD1, 0xFA, 0xE5));
        p.setAlignmentX(LEFT_ALIGNMENT);
        middle.add(p);
        middle.add(Box.createVerticalStrut(30));
        middle.add(feature("Every donation tracked and confirmed"));
        middle.add(Box.createVerticalStrut(12));
        middle.add(feature("Aid approved against the available balance"));
        middle.add(Box.createVerticalStrut(12));
        middle.add(feature("Clear reports you can export anytime"));
        middle.add(Box.createVerticalGlue());
        hero.add(middle, BorderLayout.CENTER);
        return hero;
    }

    private static final int Font_BOLD = java.awt.Font.BOLD;

    private JComponent feature(String text) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel tick = new JLabel(Icons.of(Icons.Kind.CHECK, 16, Color.WHITE), JLabel.CENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tick.setPreferredSize(new Dimension(26, 26));
        row.add(tick, BorderLayout.WEST);
        row.add(UI.label(text, Theme.font(0, 14), Color.WHITE), BorderLayout.CENTER);
        return row;
    }

    // ------------------------------------------------------------------- form

    private JComponent buildForm() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.SURFACE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 440));

        form.add(left(UI.label("Welcome back", Theme.font(Font_BOLD, 28), Theme.TEXT_DARK)));
        form.add(Box.createVerticalStrut(6));
        form.add(left(UI.label("Sign in to continue to your account", Theme.BODY_FONT, Theme.TEXT_MUTED)));
        form.add(Box.createVerticalStrut(30));

        form.add(left(UI.labeled("Username", usernameField)));
        form.add(Box.createVerticalStrut(16));
        form.add(left(UI.labeled("Password", passwordWrap)));
        form.add(Box.createVerticalStrut(10));

        messageLabel.setFont(Theme.BODY_SMALL);
        messageLabel.setForeground(Theme.RED);
        form.add(left(messageLabel));
        form.add(Box.createVerticalStrut(10));

        loginBtn.setPreferredSize(new Dimension(380, 46));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(left(loginBtn));
        form.add(Box.createVerticalStrut(26));

        JPanel signUpRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
        signUpRow.setOpaque(false);
        signUpRow.add(UI.label("New here?", Theme.BODY_FONT, Theme.TEXT_MUTED));
        JButton create = UI.button("Create an account", UI.Style.GHOST);
        create.addActionListener(e -> openSignUp());
        signUpRow.add(create);
        form.add(left(signUpRow));

        outer.add(form);
        return outer;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    // ---------------------------------------------------------------- actions

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password.");
            return;
        }
        setBusy(true);
        showMessage(" ");
        Async.run(() -> app.authenticate(username, password), user -> {
            setBusy(false);
            if (user == null) {
                showMessage("Invalid username or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            } else {
                app.onLoggedIn(user);
            }
        }, err -> {
            setBusy(false);
            showMessage(Async.message(err));
        });
    }

    private void setBusy(boolean busy) {
        loginBtn.setEnabled(!busy);
        loginBtn.setText(busy ? "Signing in..." : "Sign in");
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
    }

    private void openSignUp() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        SignUpDialog.open(frame, app, false, user -> {
            usernameField.setText(user.getUsername());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            showMessage(" ");
            Toast.show(frame, "Account created. You can sign in now.", Toast.Kind.SUCCESS);
        });
    }

    private void showMessage(String text) {
        messageLabel.setText(text.trim().isEmpty() ? " "
                : "<html><body style='width:370px'>" + text.replace("&", "&amp;").replace("<", "&lt;")
                        .replace("\n", "<br>") + "</body></html>");
    }
}
