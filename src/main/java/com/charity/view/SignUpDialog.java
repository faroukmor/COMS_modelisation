package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.service.UserService.NewUser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * "Create account" dialog. Used for public sign-up (Donor / Beneficiary / Association) and, with
 * {@code adminMode}, by an administrator adding any kind of user including other administrators.
 */
public final class SignUpDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final transient AppController app;
    private final boolean adminMode;
    private final transient Consumer<User> onCreated;

    private final JComboBox<Role> cmbRole;
    private final JTextField txtName = UI.styleField(new JTextField(), "");
    private final JTextField txtEmail = UI.styleField(new JTextField(), "name@example.com");
    private final JTextField txtPhone = UI.styleField(new JTextField(), "+213 ...");
    private final JTextField txtNationalId = UI.styleField(new JTextField());
    private final JTextField txtRegNo = UI.styleField(new JTextField());
    private final JTextField txtRep = UI.styleField(new JTextField());
    private final JTextField txtAddress = UI.styleField(new JTextField());
    private final JTextField txtUsername = UI.styleField(new JTextField(), "letters, digits, . _ -");
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirm = new JPasswordField();
    private final JLabel nameCaption = UI.label("Full name", Theme.LABEL_FONT, Theme.TEXT_MUTED);
    private final JLabel emailCaption = UI.label("Email", Theme.LABEL_FONT, Theme.TEXT_MUTED);
    private final JPanel nationalIdBox;
    private final JPanel assocBox;
    private final JPanel addressBox;
    private final JLabel error = new JLabel(" ");
    private final JButton btnCreate;
    private final JButton btnCancel = UI.button("Cancel", UI.Style.SECONDARY);

    private SignUpDialog(JFrame owner, AppController app, boolean adminMode, Consumer<User> onCreated) {
        super(owner, adminMode ? "Add user" : "Create account", true);
        this.app = app;
        this.adminMode = adminMode;
        this.onCreated = onCreated;

        Role[] roles = adminMode
                ? new Role[]{Role.ADMIN, Role.DONOR, Role.BENEFICIARY, Role.ASSOCIATION}
                : new Role[]{Role.DONOR, Role.BENEFICIARY, Role.ASSOCIATION};
        cmbRole = UI.styleCombo(new JComboBox<>(new DefaultComboBoxModel<>(roles)));
        cmbRole.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                                   boolean sel, boolean focus) {
                return super.getListCellRendererComponent(list, roleLabel((Role) value), index, sel, focus);
            }
        });
        btnCreate = UI.button(adminMode ? "Add user" : "Create account", UI.Style.PRIMARY);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.SURFACE);
        root.setBorder(new EmptyBorder(26, 30, 22, 30));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new javax.swing.BoxLayout(head, javax.swing.BoxLayout.Y_AXIS));
        head.add(UI.label(adminMode ? "Add a new user" : "Create your account", Theme.font(java.awt.Font.BOLD, 22), Theme.TEXT_DARK));
        head.add(javax.swing.Box.createVerticalStrut(4));
        head.add(UI.label(adminMode ? "The user can sign in right away with these details."
                : "It only takes a minute. Choose how you want to take part.", Theme.BODY_FONT, Theme.TEXT_MUTED));
        root.add(head, BorderLayout.NORTH);

        // profile-specific boxes
        nationalIdBox = caption("National ID", txtNationalId);
        assocBox = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        assocBox.setOpaque(false);
        assocBox.add(caption("Registration number", txtRegNo));
        assocBox.add(caption("Representative", txtRep));
        addressBox = caption("Address (optional)", txtAddress);

        UI.FormGrid grid = new UI.FormGrid(2);
        grid.add(adminMode ? "Role" : "I am joining as", cmbRole, 2);
        grid.addRaw(wrap(nameCaption, txtName), 2);
        grid.addRaw(wrap(emailCaption, txtEmail), 1);
        grid.add("Phone (optional)", txtPhone, 1);
        grid.addRaw(nationalIdBox, 2);
        grid.addRaw(assocBox, 2);
        grid.addRaw(addressBox, 2);
        grid.add("Username", txtUsername, 2);
        grid.add("Password", UI.passwordField(txtPassword, "At least 8 characters"), 1);
        grid.add("Confirm password", UI.passwordField(txtConfirm, "Repeat password"), 1);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(grid, BorderLayout.NORTH);
        JLabel hint = UI.muted("Password: 8+ characters with at least one letter and one digit.");
        error.setFont(Theme.BODY_SMALL);
        error.setForeground(Theme.RED);
        JPanel notes = new JPanel(new BorderLayout(0, 6));
        notes.setOpaque(false);
        notes.add(hint, BorderLayout.NORTH);
        notes.add(error, BorderLayout.SOUTH);
        body.add(notes, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(btnCancel);
        buttons.add(btnCreate);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        cmbRole.addActionListener(e -> syncRole());
        btnCancel.addActionListener(e -> dispose());
        btnCreate.addActionListener(e -> submit());
        txtConfirm.addActionListener(e -> submit());
        getRootPane().setDefaultButton(null);
        syncRole();
        setPreferredSize(new Dimension(600, Math.min(760, java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - 80)));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Opens the dialog; {@code onCreated} runs on the event thread after the account exists. */
    public static void open(JFrame owner, AppController app, boolean adminMode, Consumer<User> onCreated) {
        new SignUpDialog(owner, app, adminMode, onCreated).setVisible(true);
    }

    private static String roleLabel(Role r) {
        return switch (r) {
            case ADMIN -> "Administrator";
            case DONOR -> "Donor - I want to donate";
            case BENEFICIARY -> "Beneficiary - I need support (individual)";
            case ASSOCIATION -> "Association - we represent a charity";
        };
    }

    private static JPanel caption(String text, JComponent c) {
        return wrap(UI.label(text, Theme.LABEL_FONT, Theme.TEXT_MUTED), c);
    }

    private static JPanel wrap(JLabel caption, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(caption, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void syncRole() {
        Role role = (Role) cmbRole.getSelectedItem();
        boolean assoc = role == Role.ASSOCIATION;
        boolean profile = role != Role.ADMIN;
        nameCaption.setText(assoc ? "Organization name" : "Full name");
        emailCaption.setText(profile ? "Email" : "Email (optional)");
        nationalIdBox.setVisible(role == Role.BENEFICIARY);
        assocBox.setVisible(assoc);
        addressBox.setVisible(profile);
        error.setText(" ");
        revalidate();
        repaint();
    }

    private void submit() {
        if (!btnCreate.isEnabled()) {
            return;
        }
        String pw = new String(txtPassword.getPassword());
        if (!pw.equals(new String(txtConfirm.getPassword()))) {
            error.setText("The two passwords do not match.");
            return;
        }
        NewUser data = new NewUser(txtUsername.getText(), pw, txtName.getText(), txtEmail.getText(),
                txtPhone.getText(), (Role) cmbRole.getSelectedItem(), txtNationalId.getText(),
                txtRegNo.getText(), txtRep.getText(), txtAddress.getText());
        error.setText(" ");
        setBusy(true);
        User actor = app.getCurrentUser();
        Async.run(() -> adminMode ? app.userService.createByAdmin(actor, data) : app.userService.register(data),
                user -> {
                    dispose();
                    onCreated.accept(user);
                },
                err -> {
                    setBusy(false);
                    error.setText("<html><body style='width:480px'>" + Async.message(err).replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>") + "</body></html>");
                });
    }

    private void setBusy(boolean busy) {
        btnCreate.setEnabled(!busy);
        btnCancel.setEnabled(!busy);
        btnCreate.setText(busy ? "Please wait..." : adminMode ? "Add user" : "Create account");
    }
}
