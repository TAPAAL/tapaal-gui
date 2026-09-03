package net.tapaal.gui.debug

import pipe.gui.petrinet.PetriNetTab
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import java.util.WeakHashMap

/** Thin Swing view for a model-backed inspection snapshot. */
class InspectSpy private constructor(
    private val snapshotProvider: () -> InspectionNode,
    title: String,
) : JFrame("Net inspector") {
    private val treeModel = DefaultTreeModel(DefaultMutableTreeNode("Loading"))
    private val tree = JTree(treeModel)
    private var snapshot: InspectionNode? = null

    private val searchField = JTextField(24)
    private val statusLabel = JLabel()

    private val reloadButton = JButton(object : AbstractAction("Reload") {
        override fun actionPerformed(event: ActionEvent?) {
            reload()
        }
    })

    private val copyButton = JButton(object : AbstractAction("Copy selected") {
        override fun actionPerformed(event: ActionEvent?) {
            copySelection()
        }
    })

    init {
        setTitle(title)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = Dimension(500, 300)
        setSize(700, 800)
        setLocationByPlatform(true)

        val controls = JPanel()
        controls.layout = BoxLayout(controls, BoxLayout.X_AXIS)
        controls.add(reloadButton)
        controls.add(JLabel("  Filter: "))
        controls.add(searchField)
        controls.add(copyButton)
        copyButton.isEnabled = false

        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent?) = applyFilter()
            override fun removeUpdate(event: DocumentEvent?) = applyFilter()
            override fun changedUpdate(event: DocumentEvent?) = applyFilter()
        })
        tree.addTreeSelectionListener {
            copyButton.isEnabled = tree.lastSelectedPathComponent is DefaultMutableTreeNode
        }

        contentPane.layout = BorderLayout()
        contentPane.add(controls, BorderLayout.PAGE_START)
        contentPane.add(JScrollPane(tree), BorderLayout.CENTER)
        contentPane.add(statusLabel, BorderLayout.PAGE_END)

        reload()
    }

    private fun reload() {
        snapshot = runCatching { snapshotProvider() }.getOrElse { exception ->
            InspectionNode(
                "Inspector error",
                "${exception.javaClass.simpleName}: ${exception.message ?: "no message"}",
            )
        }
        statusLabel.text = "Snapshot refreshed"
        applyFilter()
    }

    private fun applyFilter() {
        val current = snapshot ?: return
        val query = searchField.text.trim()
        val filtered = filterNode(current, query) ?: InspectionNode("No matches", query)
        treeModel.setRoot(toSwingNode(filtered))
        if (tree.rowCount > 0) tree.expandRow(0)
    }

    private fun filterNode(node: InspectionNode, query: String): InspectionNode? {
        if (query.isEmpty()) return node

        val matchingChildren = node.children.mapNotNull { filterNode(it, query) }
        val matches = node.displayText().contains(query, ignoreCase = true)
        return if (matches) node else if (matchingChildren.isNotEmpty()) node.copy(children = matchingChildren) else null
    }

    private fun copySelection() {
        val selected = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
        val text = StringBuilder()
        appendSwingNode(selected, 0, text)
        runCatching {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text.toString()), null)
            statusLabel.text = "Copied selected subtree"
        }.onFailure { exception ->
            statusLabel.text = "Could not copy: ${exception.message ?: exception.javaClass.simpleName}"
        }
    }

    private fun appendSwingNode(node: DefaultMutableTreeNode, depth: Int, output: StringBuilder) {
        output.append("  ".repeat(depth)).append(node.userObject).append('\n')
        for (i in 0 until node.childCount) {
            appendSwingNode(node.getChildAt(i) as DefaultMutableTreeNode, depth + 1, output)
        }
    }

    private fun toSwingNode(node: InspectionNode): DefaultMutableTreeNode {
        val swingNode = DefaultMutableTreeNode(node.displayText())
        node.children.forEach { swingNode.add(toSwingNode(it)) }
        return swingNode
    }

    companion object {
        private val openInspectors = WeakHashMap<PetriNetTab, InspectSpy>()

        fun open(tab: PetriNetTab): InspectSpy {
            val existing = openInspectors[tab]
            if (existing != null && existing.isDisplayable) {
                existing.toFront()
                existing.requestFocus()
                return existing
            }

            val inspector = InspectSpy(
                { NetInspectionSnapshot.capture(tab) },
                "Net inspector — ${tab.getTabTitle()}",
            )
            openInspectors[tab] = inspector
            inspector.addWindowListener(object : WindowAdapter() {
                override fun windowClosed(event: WindowEvent?) {
                    openInspectors.remove(tab)
                }
            })
            inspector.isVisible = true
            return inspector
        }
    }
}
