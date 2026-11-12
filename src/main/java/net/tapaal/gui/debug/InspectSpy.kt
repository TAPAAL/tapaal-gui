package net.tapaal.gui.debug

import pipe.gui.petrinet.PetriNetTab
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import java.util.WeakHashMap
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/** Tree renderer that preserves the inspector's compact, readable labels. */
internal class InspectionTreeCellRenderer(
    private val changedKeys: () -> Set<String>,
) : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        val component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
        val node = (value as? DefaultMutableTreeNode)?.userObject as? InspectionNode
        if (node != null) {
            text = node.displayText()
            if (node.key in changedKeys()) {
                component.background = Color(255, 241, 168)
                component.foreground = Color.BLACK
                (component as? JComponent)?.isOpaque = true
            } else {
                component.background = if (selected) backgroundSelectionColor else backgroundNonSelectionColor
                component.foreground = if (selected) textSelectionColor else textNonSelectionColor
                (component as? JComponent)?.isOpaque = false
            }
        }
        return component
    }
}

/** Expands a tree recursively, including descendants added after the first row. */
internal fun expandAll(tree: JTree) {
    var row = 0
    while (row < tree.rowCount) {
        tree.expandRow(row)
        row++
    }
}

/** Thin Swing view for a model-backed inspection snapshot. */
class InspectSpy private constructor(
    private val snapshotProvider: () -> InspectionNode,
    title: String,
) : JFrame("Net inspector") {
    private val treeModel = DefaultTreeModel(DefaultMutableTreeNode("Loading"))
    private val tree = JTree(treeModel)
    private var snapshot: InspectionNode? = null
    private var changedKeys: Set<String> = emptySet()
    private var selectedNodeKey: String? = null
    private var hasRenderedTree = false
    private var trackExpansionChanges = true

    private val viewState = InspectionViewState()
    private val searchField = JTextField(24)
    private val statusLabel = JLabel()
    private val pinnedPanel = JPanel()
    private var contextMenuNodeKey: String? = null

    private val pinContextAction = object : AbstractAction("Pin node") {
        override fun actionPerformed(event: ActionEvent?) {
            contextMenuNodeKey?.let { key ->
                viewState.togglePin(key)
                renderPinnedNodes()
                updateSelectionControls()
            }
        }
    }

    private val treeContextMenu = JPopupMenu().apply {
        add(JMenuItem(pinContextAction))
    }

    private val reloadButton = JButton(object : AbstractAction("Reload") {
        override fun actionPerformed(event: ActionEvent?) {
            reload()
        }
    })

    private val pinButton = JButton(object : AbstractAction("Pin selected") {
        override fun actionPerformed(event: ActionEvent?) {
            pinSelected()
        }
    })

    private val clearPinsButton = JButton(object : AbstractAction("Clear pins") {
        override fun actionPerformed(event: ActionEvent?) {
            viewState.clearPins()
            renderPinnedNodes()
            updateSelectionControls()
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
        minimumSize = Dimension(700, 300)
        setSize(1000, 800)
        setLocationByPlatform(true)

        val controls = JPanel()
        controls.layout = BoxLayout(controls, BoxLayout.X_AXIS)
        controls.add(reloadButton)
        controls.add(pinButton)
        controls.add(clearPinsButton)
        controls.add(JLabel("  Filter: "))
        controls.add(searchField)
        controls.add(copyButton)
        pinButton.isEnabled = false
        clearPinsButton.isEnabled = false
        copyButton.isEnabled = false

        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent?) = applyFilter()
            override fun removeUpdate(event: DocumentEvent?) = applyFilter()
            override fun changedUpdate(event: DocumentEvent?) = applyFilter()
        })
        tree.addTreeSelectionListener {
            selectedNodeKey = selectedInspectionNode()?.key
            updateSelectionControls()
        }
        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(event: MouseEvent?) = showContextMenu(event)
            override fun mouseReleased(event: MouseEvent?) = showContextMenu(event)
        })
        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent?) {
                if (trackExpansionChanges) expansionKey(event)?.let { viewState.setExpanded(it, true) }
            }

            override fun treeCollapsed(event: TreeExpansionEvent?) {
                if (trackExpansionChanges) expansionKey(event)?.let { viewState.setExpanded(it, false) }
            }
        })
        tree.cellRenderer = InspectionTreeCellRenderer { changedKeys }

        pinnedPanel.layout = BoxLayout(pinnedPanel, BoxLayout.Y_AXIS)
        pinnedPanel.border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
        val pinnedScroll = JScrollPane(pinnedPanel)
        pinnedScroll.border = BorderFactory.createTitledBorder("Pinned elements")

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(tree), pinnedScroll)
        splitPane.resizeWeight = 0.68
        splitPane.setDividerLocation(0.68)

        contentPane.layout = BorderLayout()
        contentPane.add(controls, BorderLayout.PAGE_START)
        contentPane.add(splitPane, BorderLayout.CENTER)
        contentPane.add(statusLabel, BorderLayout.PAGE_END)

        reload()
    }

    private fun reload() {
        val previous = snapshot
        val loaded = runCatching { snapshotProvider() }.getOrElse { exception ->
            InspectionNode(
                "Inspector error",
                "${exception.javaClass.simpleName}: ${exception.message ?: "no message"}",
            )
        }.withStableKeys()

        changedKeys = if (previous == null) emptySet() else InspectionSnapshotDiff.changedKeys(previous, loaded)
        snapshot = loaded
        statusLabel.text = if (changedKeys.isEmpty()) {
            "Snapshot refreshed"
        } else {
            "Snapshot refreshed — ${changedKeys.size} changed"
        }
        applyFilter()
        renderPinnedNodes()
    }

    private fun applyFilter() {
        val current = snapshot ?: return
        val query = searchField.text.trim()
        val filtered = filterNode(current, query) ?: InspectionNode("No matches", query, key = "no-matches")
        val selectionKey = selectedNodeKey
        trackExpansionChanges = false
        treeModel.setRoot(toSwingNode(filtered))
        restoreExpansion(filtered, query)
        restoreSelection(selectionKey)
        trackExpansionChanges = true

        if (!hasRenderedTree && tree.rowCount > 0) {
            tree.expandRow(0)
            ((treeModel.root as? DefaultMutableTreeNode)?.userObject as? InspectionNode)?.key
                ?.let { viewState.setExpanded(it, true) }
            hasRenderedTree = true
        }
        updateSelectionControls()
    }

    private fun filterNode(node: InspectionNode, query: String): InspectionNode? {
        if (query.isEmpty()) return node

        val matchingChildren = node.children.mapNotNull { filterNode(it, query) }
        val matches = node.displayText().contains(query, ignoreCase = true)
        return if (matches) node else if (matchingChildren.isNotEmpty()) node.copy(children = matchingChildren) else null
    }

    private fun restoreExpansion(filtered: InspectionNode, query: String) {
        val keys = linkedSetOf<String>()
        collectExplicitExpandedKeys(filtered, viewState.expandedKeys(), true, keys)
        if (query.isNotEmpty()) collectSearchAncestorKeys(filtered, query, keys)
        val swingRoot = treeModel.root as? DefaultMutableTreeNode ?: return
        keys.sortedBy { it.count { character -> character == '/' } }.forEach { key ->
            findSwingNode(swingRoot, key)?.let { tree.expandPath(TreePath(it.getPath())) }
        }
    }

    private fun collectExplicitExpandedKeys(
        node: InspectionNode,
        expandedKeys: Set<String>,
        parentExpanded: Boolean,
        result: MutableSet<String>,
    ) {
        val expanded = parentExpanded && node.key in expandedKeys
        if (expanded) result += node.key
        node.children.forEach { child ->
            collectExplicitExpandedKeys(child, expandedKeys, expanded, result)
        }
    }

    private fun collectSearchAncestorKeys(node: InspectionNode, query: String, keys: MutableSet<String>): Boolean {
        if (node.displayText().contains(query, ignoreCase = true)) return true

        val childMatches = node.children.any { collectSearchAncestorKeys(it, query, keys) }
        if (childMatches) keys += node.key
        return childMatches
    }

    private fun restoreSelection(key: String?) {
        key?.let {
            val swingRoot = treeModel.root as? DefaultMutableTreeNode ?: return
            findSwingNode(swingRoot, it)?.let { tree.selectionPath = TreePath(it.getPath()) }
        }
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
        val display = (node.userObject as? InspectionNode)?.displayText() ?: node.userObject
        output.append("  ".repeat(depth)).append(display).append('\n')
        for (i in 0 until node.childCount) {
            appendSwingNode(node.getChildAt(i) as DefaultMutableTreeNode, depth + 1, output)
        }
    }

    private fun pinSelected() {
        selectedInspectionNode()?.let { node ->
            viewState.pin(node.key)
            renderPinnedNodes()
            updateSelectionControls()
        }
    }

    private fun showContextMenu(event: MouseEvent?) {
        if (event == null || !event.isPopupTrigger) return
        val path = tree.getPathForLocation(event.x, event.y) ?: return
        tree.selectionPath = path
        val node = (path.lastPathComponent as? DefaultMutableTreeNode)?.userObject as? InspectionNode ?: return
        contextMenuNodeKey = node.key
        val pinned = node.key in viewState.pinnedKeys()
        pinContextAction.putValue(AbstractAction.NAME, if (pinned) "Unpin node" else "Pin node")
        pinContextAction.isEnabled = node.key.isNotEmpty()
        treeContextMenu.show(tree, event.x, event.y)
    }

    private fun renderPinnedNodes() {
        pinnedPanel.removeAll()
        val current = snapshot
        val pinnedKeys = viewState.pinnedKeys()
        clearPinsButton.isEnabled = pinnedKeys.isNotEmpty()

        if (pinnedKeys.isEmpty()) {
            pinnedPanel.add(JLabel("Select a node and click Pin selected.", SwingConstants.CENTER))
        } else {
            pinnedKeys.forEach { key ->
                val node = current?.let { findNode(it, key) }
                pinnedPanel.add(pinnedNodeCard(key, node))
                pinnedPanel.add(Box.createVerticalStrut(6))
            }
        }

        pinnedPanel.revalidate()
        pinnedPanel.repaint()
    }

    private fun pinnedNodeCard(key: String, node: InspectionNode?): JPanel {
        val card = JPanel(BorderLayout(4, 4))
        card.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
        )
        card.preferredSize = Dimension(320, 190)
        card.maximumSize = Dimension(Int.MAX_VALUE, 190)

        val header = JPanel(BorderLayout(4, 0))
        val title = JLabel(node?.displayText() ?: "Missing: $key")
        title.toolTipText = key
        header.add(title, BorderLayout.CENTER)
        header.add(JButton(object : AbstractAction("Unpin") {
            override fun actionPerformed(event: ActionEvent?) {
                viewState.unpin(key)
                renderPinnedNodes()
                updateSelectionControls()
            }
        }), BorderLayout.LINE_END)
        card.add(header, BorderLayout.PAGE_START)

        if (node == null) {
            card.add(JLabel("This node is no longer in the snapshot."), BorderLayout.CENTER)
        } else {
            val pinnedTree = JTree(DefaultTreeModel(toSwingNode(node)))
            pinnedTree.cellRenderer = InspectionTreeCellRenderer { changedKeys }
            expandAll(pinnedTree)
            card.add(JScrollPane(pinnedTree), BorderLayout.CENTER)
        }
        return card
    }

    private fun updateSelectionControls() {
        val node = selectedInspectionNode()
        copyButton.isEnabled = node != null
        pinButton.isEnabled = node != null && node.key.isNotEmpty() && node.key !in viewState.pinnedKeys()
    }

    private fun selectedInspectionNode(): InspectionNode? =
        (tree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject as? InspectionNode

    private fun expansionKey(event: TreeExpansionEvent?): String? =
        (event?.path?.lastPathComponent as? DefaultMutableTreeNode)?.userObject
            ?.let { it as? InspectionNode }
            ?.key

    private fun findNode(root: InspectionNode, key: String): InspectionNode? {
        if (root.key == key) return root
        root.children.forEach { child -> findNode(child, key)?.let { return it } }
        return null
    }

    private fun findSwingNode(root: DefaultMutableTreeNode, key: String): DefaultMutableTreeNode? {
        val node = root.userObject as? InspectionNode
        if (node?.key == key) return root
        for (i in 0 until root.childCount) {
            findSwingNode(root.getChildAt(i) as DefaultMutableTreeNode, key)?.let { return it }
        }
        return null
    }

    private fun toSwingNode(node: InspectionNode): DefaultMutableTreeNode {
        val swingNode = DefaultMutableTreeNode(node)
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
