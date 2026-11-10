package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.User;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Administrator page: list all accounts and add new users (any role, including administrators).
 */
public class UsersView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JTable table = TableHelper.createTable(
            new String[]{"ID", "Username", "Full name", "Role", "Email", "Phone", "Created"});
    private final JTextField txtSearch = UI.searchField("Search users...");

    public UsersView(AppController app) {
        super(app);
        JButton add = UI.button("Add user", UI.Style.PRIMARY, Icons.Kind.USER_PLUS);
        add.addActionListener(e -> SignUpDialog.open(app.getMainFrame(), app, true, user -> {
            toast("User '" + user.getUsername() + "' created.");
            load();
        }));
        add(UI.pageHeader("Users", "Everyone who can sign in to the system.", txtSearch, add), BorderLayout.NORTH);
        TableHelper.attachFilter(table, txtSearch);
        add(TableHelper.tableCard("Accounts", table, null), BorderLayout.CENTER);
        load();
    }

    private void load() {
        async(app.userService::findAll, (List<User> list) -> {
            Object[][] rows = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                User u = list.get(i);
                rows[i] = new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                        u.getEmail(), u.getPhone(), u.getCreatedAt()};
            }
            TableHelper.setData(table, rows);
        });
    }
}
