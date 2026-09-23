package net.tapaal.swinghelpers;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedJTabbedPaneTest {

    @Test
    void moveTabPreservesContentHeaderMetadataAndSelection() {
        var pane = new TestTabbedPane();
        var first = new JPanel();
        var second = new JPanel();
        var third = new JPanel();

        pane.addTab("First", first);
        pane.addTab("Second", second);
        pane.addTab("Third", third);
        pane.setToolTipTextAt(0, "First tooltip");
        Component firstHeader = pane.getTabComponentAt(0);
        pane.setSelectedComponent(second);

        org.junit.jupiter.api.Assertions.assertTrue(pane.moveTab(0, 2));

        assertSame(second, pane.getComponentAt(0));
        assertSame(third, pane.getComponentAt(1));
        assertSame(first, pane.getComponentAt(2));
        assertSame(firstHeader, pane.getTabComponentAt(2));
        assertEquals("First", pane.getTitleAt(2));
        assertEquals("First tooltip", pane.getToolTipTextAt(2));
        assertSame(second, pane.getSelectedComponent());
        assertEquals(List.of(2), pane.changedIndexes);
    }

    @Test
    void moveTabRejectsInvalidOrUnnecessaryMoves() {
        var pane = new TestTabbedPane();
        pane.addTab("First", new JPanel());
        pane.addTab("Second", new JPanel());

        assertFalse(pane.moveTab(0, 0));
        assertFalse(pane.moveTab(-1, 1));
        assertFalse(pane.moveTab(0, 2));
        assertEquals(List.of(), pane.changedIndexes);
    }

    private static final class TestTabbedPane extends ExtendedJTabbedPane<JPanel> {
        private final List<Integer> changedIndexes = new ArrayList<>();

        @Override
        public Component generator() {
            return new JPanel();
        }

        @Override
        protected void tabOrderChanged(JPanel tab, int oldIndex, int newIndex) {
            changedIndexes.add(newIndex);
        }
    }
}
