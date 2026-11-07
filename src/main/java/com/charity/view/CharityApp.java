package com.charity.view;

import com.charity.controller.AppController;

/**
 * Application entry point. Launches the Charity Fund Management System desktop app.
 */
public class CharityApp {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            AppController app = AppController.INSTANCE;
            app.init(frame);
            frame.setAppController(app);
            // Show login screen initially
            frame.showView("login", new LoginView(app));
            frame.setVisible(true);
        });
    }
}
