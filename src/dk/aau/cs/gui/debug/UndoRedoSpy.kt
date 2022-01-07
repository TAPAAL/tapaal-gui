package dk.aau.cs.gui.debug

import dk.aau.cs.gui.undo.Command
import pipe.gui.TAPAALGUI
import pipe.gui.undo.UndoManager
import java.awt.event.ActionEvent
import java.util.ArrayList
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class UndoRedoSpy() : JFrame() {

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

        val edits = UndoManager::class.java.getDeclaredField("edits")
        edits.isAccessible = true
        val e = edits.get(TAPAALGUI.getCurrentTab().undoManager) as ArrayList<ArrayList<Command>?>

        e.forEach {
            if (it != null && it.size > 0) {
                treeRoot.add(DefaultMutableTreeNode(it))
            }
        }
        m.reload()
        tree.expandRow(0)
    }


    init {
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.add(reloadBtn)

        contentPane.add(JScrollPane(tree))

        reloadUndoRedoStack()
        pack()

    }


}