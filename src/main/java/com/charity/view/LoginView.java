package com.charity.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import com.charity.controller.AppController;

/**
 * Login screen: username, password, login button, validation messages.
 */
public class LoginView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    public LoginView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel();
        card.setBackground(Theme.SURFACE);
        card.setLayout(new GridBagLayout());
        card.setBorder(Theme.CARD_BORDER);
        card.setPreferredSize(new Dimension(380, 420));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Charity Fund");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.GREEN_DARK);
        gbc.gridy = 0;
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(Theme.BODY_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        gbc.gridy = 1;
        card.add(subtitle, gbc);

        gbc.insets = new Insets(22, 30, 8, 30);
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(Theme.H3_FONT);
        userLabel.setForeground(Theme.TEXT_DARK);
        gbc.gridy = 2;
        card.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(Theme.BODY_FONT);
        usernameField.setBorder(Theme.FIELD_BORDER);
        usernameField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 3;
        card.add(usernameField, gbc);

        gbc.insets = new Insets(18, 30, 8, 30);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.H3_FONT);
        passLabel.setForeground(Theme.TEXT_DARK);
        gbc.gridy = 4;
        card.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.BODY_FONT);
        passwordField.setBorder(Theme.FIELD_BORDER);
        passwordField.setPreferredSize(new Dimension(300, 42));
        gbc.gridy = 5;
        card.add(passwordField, gbc);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(Theme.BODY_SMALL);
        messageLabel.setForeground(Theme.RED);
        gbc.insets = new Insets(4, 30, 10, 30);
        gbc.gridy = 6;
        card.add(messageLabel, gbc);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(Theme.GREEN);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(Theme.BUTTON_FONT);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginBtn.setPreferredSize(new Dimension(300, 44));
        gbc.insets = new Insets(10, 30, 0, 30);
        gbc.gridy = 7;
        card.add(loginBtn, gbc);

        loginBtn.addActionListener((ActionEvent e) -> attemptLogin());
        passwordField.addActionListener((ActionEvent e) -> attemptLogin());

        // center the card
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 40));
        add(card);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password.", true);
            return;
        }
        if (app.login(username, password)) {
            showMessage("Welcome back!", false);
        } else {
            showMessage("Invalid username or password.", true);
            passwordField.setText("");
        }
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setText(text);
        messageLabel.setForeground(error ? Theme.RED : Theme.GREEN);
    }
}
