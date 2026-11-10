package com.charity.view;

import com.charity.controller.AppController;
import javax.swing.JOptionPane;

/**
 * Application entry point. Launches the Charity Fund Management System desktop app.
 */
public class CharityApp {
    public static void main(String[] args) {
        // Anything unexpected on the event thread becomes a message instead of a silent stack trace.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            java.awt.EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + Async.message(e), "CharityFund", JOptionPane.ERROR_MESSAGE));
        });
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            AppController app = AppController.INSTANCE;
            app.init(frame);
            frame.setAppController(app);
            app.showLogin();
            frame.setVisible(true);
        });
    }
}
