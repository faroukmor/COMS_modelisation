package com.charity.view;

import com.charity.controller.AppController;
import java.awt.BorderLayout;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Base class for the main content screens: consistent background, padding and helpers for
 * background work and notifications.
 */
public abstract class BaseView extends JPanel {
    private static final long serialVersionUID = 1L;
    protected final transient AppController app;

    protected BaseView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new java.awt.BorderLayout(0, 16));
        setBorder(new EmptyBorder(26, 32, 24, 32));
    }

    /**
     * Runs {@code work} on a background thread and hands the result to {@code ok} on the
     * event thread. Failures are shown as an error toast.
     */
    protected <T> void async(Callable<T> work, Consumer<T> ok) {
        Async.busy(this, true);
        Async.run(work, result -> {
            Async.busy(this, false);
            ok.accept(result);
        }, err -> {
            Async.busy(this, false);
            toastError(err);
        });
    }

    protected void toast(String message) {
        Toast.show(app.getMainFrame(), message, Toast.Kind.SUCCESS);
    }

    protected void toastError(Throwable t) {
        Toast.error(app.getMainFrame(), t);
    }

    protected void toastError(String message) {
        Toast.show(app.getMainFrame(), message, Toast.Kind.ERROR);
    }

    /** Table (or list) on the left, edit/entry form on the right. */
    protected JComponent masterDetail(JComponent master, JComponent detail, int detailWidth) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(detail, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setPreferredSize(new java.awt.Dimension(detailWidth, 100));
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setOpaque(false);
        p.add(master, BorderLayout.CENTER);
        p.add(sp, BorderLayout.EAST);
        return p;
    }

    /** Titled white card holding a form body and a button row. */
    protected UI.Card formCard(JLabel title, JComponent body, JComponent buttons) {
        UI.Card card = new UI.Card();
        card.setLayout(new BorderLayout(0, 16));
        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    protected static JLabel cardTitle(String text) {
        return UI.label(text, Theme.H2_FONT, Theme.TEXT_DARK);
    }
}
