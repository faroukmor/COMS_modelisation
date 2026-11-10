package com.charity.view;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

/** Helpers for driving Swing components from tests without opening a window. */
final class UiTestSupport {
    private UiTestSupport() {
    }

    static <T extends Component> List<T> all(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, type, found);
        return found;
    }

    private static <T extends Component> void collect(Component c, Class<T> type, List<T> out) {
        if (type.isInstance(c)) {
            out.add(type.cast(c));
        }
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                collect(child, type, out);
            }
        }
    }

    static AbstractButton button(Container root, String text) {
        return all(root, AbstractButton.class).stream()
                .filter(b -> text.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("no button '" + text + "'"));
    }

    /** Lets background SwingWorkers finish and their results be applied on the event thread. */
    static void waitUntil(BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            SwingUtilities.invokeAndWait(() -> { });
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(40);
        }
        throw new AssertionError("condition not reached within 15 s");
    }

    static void onEdt(Runnable r) throws Exception {
        SwingUtilities.invokeAndWait(r);
    }
}
