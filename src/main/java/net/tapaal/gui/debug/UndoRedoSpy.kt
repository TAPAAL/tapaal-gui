package net.tapaal.gui.debug

import net.tapaal.gui.petrinet.undo.Command
import pipe.gui.TAPAALGUI
import pipe.gui.petrinet.undo.UndoManager
import java.awt.event.ActionEvent
import java.util.ArrayList
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class UndoRedoSpy : JFrame() {
    private val reloadBtn = JButton(object : AbstractAction("Reload") {
        override fun actionPerformed(e: ActionEvent?) {
            reloadUndoRedoStack()
        }
    })

    private val treeRoot = DefaultMutableTreeNode("root")
    private val tree = JTree(treeRoot)

    private fun reloadUndoRedoStack() {
        val m = tree.model as DefaultTreeModel
        treeRoot.removeAllChildren()

        val undoManager = TAPAALGUI.getCurrentTab().undoManager
        
        // Normal edits
        val normalEditsField = UndoManager::class.java.getDeclaredField("normalEdits")
        normalEditsField.isAccessible = true
        val normalEdits = normalEditsField.get(undoManager) as ArrayList<ArrayList<Command>?>
        
        // Animation edits
        val animEditsField = UndoManager::class.java.getDeclaredField("animEdits")
        animEditsField.isAccessible = true
        val animEdits = animEditsField.get(undoManager) as ArrayList<ArrayList<Command>?>

        val normalNode = DefaultMutableTreeNode("Normal Mode")
        normalEdits.forEach {
            if (it != null && it.size > 0) {
                normalNode.add(DefaultMutableTreeNode(it))
            }
        }

        treeRoot.add(normalNode)

        val animNode = DefaultMutableTreeNode("Animation Mode")
        animEdits.forEach {
            if (it != null && it.size > 0) {
                animNode.add(DefaultMutableTreeNode(it))
            }
        }

        treeRoot.add(animNode)

        m.reload()
        tree.expandRow(0)
        tree.expandRow(1)
        tree.expandRow(2)
    }

    init {
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.add(reloadBtn)

        contentPane.add(JScrollPane(tree))

        reloadUndoRedoStack()
        pack()
    }
}