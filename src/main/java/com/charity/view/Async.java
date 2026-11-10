package com.charity.view;

import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Runs database / hashing work off the Swing event thread so the window never freezes
 * (for example when the database is slow or unreachable), then reports back on the event thread.
 */
public final class Async {
    private Async() {
    }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    onError.accept(e.getCause() == null ? e : e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    onError.accept(e);
                }
            }
        }.execute();
    }

    /** Turns any exception into a short message that is safe to show to a person. */
    public static String message(Throwable t) {
        Throwable root = t;
        if (t instanceof IllegalArgumentException || t instanceof IllegalStateException
                || t instanceof SecurityException) {
            return t.getMessage();
        }
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
        if (root instanceof java.sql.SQLException || t instanceof RuntimeException && root != t) {
            return "Cannot connect to the database: " + firstLine(msg)
                    + "\nCheck db.url, db.username and db.password in db.properties, and that Oracle is running.";
        }
        return firstLine(msg);
    }

    private static String firstLine(String s) {
        int i = s.indexOf('\n');
        return i < 0 ? s : s.substring(0, i);
    }

    /** Shows the wait cursor on a component while work is running. */
    public static void busy(java.awt.Component c, boolean busy) {
        if (c != null) {
            c.setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        }
    }
}
