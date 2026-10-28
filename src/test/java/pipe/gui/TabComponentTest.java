package pipe.gui;

import org.junit.jupiter.api.Test;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TabComponentTest {

    @Test
    void leftClickingTabTitleSelectsTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane pane = new JTabbedPane();
            pane.addTab("first", new JPanel());
            pane.addTab("second", new JPanel());
            TabComponent header = header(pane, new ArrayList<>(), null);
            pane.setTabComponentAt(1, header);
            click(header.getComponent(0), MouseEvent.BUTTON1, 0, false);
            assertEquals(1, pane.getSelectedIndex());
        });
    }

    @Test
    void middleClickClosesBackgroundTabWithoutChangingSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Fixture fixture = new Fixture();
            fixture.pane.setSelectedIndex(2);
            click(fixture.header.getComponent(0), MouseEvent.BUTTON2, 0, false);
            assertEquals(Arrays.asList(fixture.tabs.get(1)), fixture.attempts);
            assertEquals(fixture.tabs.get(2), fixture.pane.getSelectedComponent());
        });
    }

    @Test
    void popupGesturesPreserveSelectionAndDoNotCloseTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Fixture fixture = new Fixture();
            JFrame frame = new JFrame();
            try {
                frame.add(fixture.pane);
                frame.setSize(500, 300);
                frame.setVisible(true);
                Component title = fixture.header.getComponent(0);
                JButton button = (JButton) fixture.header.getComponent(1);
                click(title, MouseEvent.BUTTON3, 0, true);
                click(title, MouseEvent.BUTTON1, InputEvent.CTRL_DOWN_MASK, true);
                click(button, MouseEvent.BUTTON1, InputEvent.CTRL_DOWN_MASK, true);
                assertEquals(fixture.tabs.get(0), fixture.pane.getSelectedComponent());
                assertEquals(0, fixture.attempts.size());
                assertFalse(button.getModel().isArmed());
                assertFalse(button.getModel().isPressed());
                // A subsequent ordinary button click must still close its tab.
                click(button, MouseEvent.BUTTON1, 0, false);
                assertEquals(Arrays.asList(fixture.tabs.get(1)), fixture.attempts);
            } finally {
                frame.dispose();
            }
        });
    }

    @Test
    void bulkCloseStopsAtCancelledTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Fixture fixture = new Fixture();
            fixture.cancelledTab = fixture.tabs.get(1);
            invokeMenu(fixture.header, "Close All");
            assertEquals(fixture.tabs.subList(0, 2), fixture.attempts);
            assertEquals(2, fixture.pane.getTabCount());
            assertEquals(fixture.tabs.get(1), fixture.pane.getComponentAt(0));
            assertEquals(fixture.tabs.get(2), fixture.pane.getComponentAt(1));
        });
    }

    @Test
    void bulkCloseContinuesAfterSuccessfulCloses() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Fixture fixture = new Fixture();
            invokeMenu(fixture.header, "Close All");
            assertEquals(fixture.tabs, fixture.attempts);
            assertEquals(0, fixture.pane.getTabCount());
        });
    }

    private static TabComponent header(JTabbedPane pane, List<PetriNetTab> attempts,
                                       java.util.function.Supplier<PetriNetTab> cancelledTab) {
        return new TabComponent(pane) {
            @Override
            protected void closeTab(PetriNetTab tab) {
                attempts.add(tab);
                if (cancelledTab == null || tab != cancelledTab.get()) {
                    pane.remove(tab);
                }
            }
        };
    }

    private static void click(Component component, int button, int modifiers, boolean popup) {
        component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(), modifiers, 1, 1, 1, popup, button));
        component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis(), modifiers, 1, 1, 1, false, button));
        component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(), modifiers, 1, 1, 1, false, button));
    }

    private static void invokeMenu(TabComponent header, String label) {
        try {
            Method method = TabComponent.class.getDeclaredMethod("createContextMenu");
            method.setAccessible(true);
            JPopupMenu menu = (JPopupMenu) method.invoke(header);
            for (Component component : menu.getComponents()) {
                JMenuItem item = (JMenuItem) component;
                if (label.equals(item.getText())) {
                    item.doClick();
                    return;
                }
            }
            throw new AssertionError("Missing menu item: " + label);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static class Fixture {
        final JTabbedPane pane = new JTabbedPane();
        final List<PetriNetTab> tabs = new ArrayList<>();
        final List<PetriNetTab> attempts = new ArrayList<>();
        PetriNetTab cancelledTab;
        final TabComponent header;

        Fixture() {
            for (int i = 0; i < 3; i++) {
                PetriNetTab tab = PetriNetTab.createNewEmptyTab("tab" + i, true, false, false, false);
                tabs.add(tab);
                pane.addTab("tab" + i, tab);
            }
            header = header(pane, attempts, () -> cancelledTab);
            pane.setTabComponentAt(1, header);
        }
    }
}
