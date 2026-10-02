package net.tapaal.gui.petrinet.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import pipe.gui.petrinet.PetriNetTab;

@Tag("gui")
class ConstantsPaneTest {

    @Test
    void variableRemovalNotifiesItsListAcrossUndoRedoAndPreservesSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PetriNetTab tab = PetriNetTab.createNewEmptyTab("variables.tapn", true, false, true, false);
            Variable survivor = new Variable("survivor", ColorType.COLORTYPE_DOT);
            Variable removed = new Variable("removed", ColorType.COLORTYPE_DOT);
            tab.network().add(survivor);
            tab.network().add(removed);

            checkRemoval(tab, "Variables", removed, survivor);
        });
    }

    @Test
    void colorTypeRemovalNotifiesItsListAcrossUndoRedoAndPreservesSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PetriNetTab tab = PetriNetTab.createNewEmptyTab("colors.tapn", true, false, true, false);
            ColorType survivor = new ColorType("Survivor");
            survivor.addColor("red");
            ColorType removed = new ColorType("Removed");
            removed.addColor("blue");
            tab.network().add(survivor);
            tab.network().add(removed);

            checkRemoval(tab, "Color type", removed, survivor);
        });
    }

    private static void checkRemoval(PetriNetTab tab, String view, Object removed, Object survivor) {
        tab.network().addConstant("first", 1);
        tab.network().addConstant("second", 2);
        ConstantsPane pane = findComponent(tab, ConstantsPane.class, component -> true);
        assertNotNull(pane);
        JComboBox<?> viewSelector = findComponent(pane, JComboBox.class,
            combo -> "Switch between constants, colors and variables".equals(combo.getToolTipText()));
        JButton remove = findComponent(pane, JButton.class, button -> "Remove".equals(button.getText()));
        assertNotNull(viewSelector);
        assertNotNull(remove);

        viewSelector.setSelectedItem(view);
        JList<?> list = pane.getJList();
        ListModel<?> declarations = list.getModel();
        int originalSize = declarations.getSize();
        list.setSelectedValue(removed, true);
        int removedIndex = list.getSelectedIndex();
        assertTrue(removedIndex >= 0);
        assertTrue(remove.isEnabled());
        List<ListDataEvent> events = observe(declarations);

        remove.doClick();
        assertEquals(originalSize - 1, declarations.getSize());
        assertContentsChanged(events, declarations, "removal");

        list.setSelectedValue(survivor, true);
        events.clear();
        tab.getUndoManager().undo();
        assertEquals(originalSize, declarations.getSize());
        assertSame(removed, declarations.getElementAt(removedIndex));
        assertContentsChanged(events, declarations, "undo with declarations visible");
        assertSame(survivor, list.getSelectedValue());

        events.clear();
        tab.getUndoManager().redo();
        assertEquals(originalSize - 1, declarations.getSize());
        assertContentsChanged(events, declarations, "redo with declarations visible");
        assertSame(survivor, list.getSelectedValue());

        viewSelector.setSelectedItem("Constants");
        ListModel<?> constants = list.getModel();
        list.setSelectedIndex(1);
        Object selectedConstant = list.getSelectedValue();
        for (Runnable action : List.<Runnable>of(tab.getUndoManager()::undo, tab.getUndoManager()::redo)) {
            events.clear();
            action.run();
            assertContentsChanged(events, declarations, "undo/redo with constants visible");
            assertEquals("Constants", viewSelector.getSelectedItem());
            assertSame(constants, list.getModel());
            assertSame(selectedConstant, list.getSelectedValue());
        }
    }

    private static List<ListDataEvent> observe(ListModel<?> model) {
        List<ListDataEvent> events = new ArrayList<>();
        model.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(ListDataEvent event) { events.add(event); }

            @Override
            public void intervalAdded(ListDataEvent event) { events.add(event); }

            @Override
            public void intervalRemoved(ListDataEvent event) { events.add(event); }
        });
        return events;
    }

    private static void assertContentsChanged(List<ListDataEvent> events, ListModel<?> model, String action) {
        assertTrue(events.stream().anyMatch(event -> event.getSource() == model
            && event.getType() == ListDataEvent.CONTENTS_CHANGED), action + " must notify the declaration list");
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type, Predicate<T> matches) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component) && matches.test(type.cast(component))) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                T found = findComponent((Container) component, type, matches);
                if (found != null) return found;
            }
        }
        return null;
    }
}
