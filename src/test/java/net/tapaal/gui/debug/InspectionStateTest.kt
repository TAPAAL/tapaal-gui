package net.tapaal.gui.debug

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class InspectionStateTest {
    @Test
    fun `diff marks changed values while keeping node identity`() {
        val previous = InspectionNode(
            "Net",
            children = listOf(
                InspectionNode("Place", "input", children = listOf(
                    InspectionNode("Token count", "1"),
                )),
            ),
        ).withStableKeys()
        val current = InspectionNode(
            "Net",
            children = listOf(
                InspectionNode("Place", "input", children = listOf(
                    InspectionNode("Token count", "2"),
                )),
            ),
        ).withStableKeys()

        assertEquals(
            setOf("root/Place=input#0/Token count#0"),
            InspectionSnapshotDiff.changedKeys(previous, current),
        )
        assertEquals(previous.children.single().key, current.children.single().key)
    }

    @Test
    fun `view state retains expansion and pin choices`() {
        val state = InspectionViewState()

        state.setExpanded("root/notes", true)
        state.pin("root/place-input")
        state.pin("root/transition-fire")
        state.unpin("root/place-input")

        assertEquals(setOf("root/notes"), state.expandedKeys())
        assertEquals(listOf("root/transition-fire"), state.pinnedKeys())
        assertTrue(state.pinnedKeys().none { it == "root/place-input" })
    }

    @Test
    fun `tree renderer displays the readable node text`() {
        val node = DefaultMutableTreeNode(InspectionNode("Place", "input", key = "root/place"))
        val rendered = InspectionTreeCellRenderer { emptySet() }
            .getTreeCellRendererComponent(JTree(), node, false, false, true, 0, false) as JLabel

        assertEquals("Place: input", rendered.text)
    }

    @Test
    fun `tree renderer does not carry a changed background to the next row`() {
        val changed = setOf("root/changed")
        val renderer = InspectionTreeCellRenderer { changed }
        val tree = JTree()
        val changedNode = DefaultMutableTreeNode(InspectionNode("Value", "changed", key = "root/changed"))
        val unchangedNode = DefaultMutableTreeNode(InspectionNode("Value", "same", key = "root/unchanged"))

        renderer.getTreeCellRendererComponent(tree, changedNode, false, false, true, 0, false)
        val renderedUnchanged = renderer.getTreeCellRendererComponent(
            tree,
            unchangedNode,
            false,
            false,
            true,
            1,
            false,
        ) as JLabel

        assertNotEquals(Color(255, 241, 168), renderedUnchanged.background)
    }

    @Test
    fun `expand all opens every pinned descendant`() {
        val root = DefaultMutableTreeNode("root")
        val child = DefaultMutableTreeNode("child")
        child.add(DefaultMutableTreeNode("grandchild"))
        root.add(child)
        val tree = JTree(root)

        expandAll(tree)

        assertEquals(3, tree.rowCount)
    }

    @Test
    fun `toggle pin adds and removes a node`() {
        val state = InspectionViewState()

        assertTrue(state.togglePin("root/place-input"))
        assertEquals(listOf("root/place-input"), state.pinnedKeys())
        assertTrue(!state.togglePin("root/place-input"))
        assertTrue(state.pinnedKeys().isEmpty())
    }
}
