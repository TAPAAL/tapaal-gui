package net.tapaal.gui.debug

import dk.aau.cs.model.CPN.ColorType
import dk.aau.cs.model.CPN.ProductType
import net.tapaal.gui.petrinet.editor.ConstantsPane
import pipe.gui.TAPAALGUI
import java.awt.event.ActionEvent
import java.lang.reflect.Field
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class InspectSpy : JFrame() {

    private val reloadBtn = JButton(object : AbstractAction("Reload") {
        override fun actionPerformed(e: ActionEvent?) {
            reload()
        }
    })

    private val treeRoot = DefaultMutableTreeNode("root")
    private val tree = JTree(treeRoot)

    private fun reload() {
        val m = tree.model as DefaultTreeModel
        treeRoot.removeAllChildren()

        generateGlobal()

        m.reload()
        tree.expandRow(1)
    }

    private fun <T : Any> T.getPrivateField(
        field: String,
    ): Any? {
        //val ref = this.javaClass.getDeclaredField(field)
        //val ref = this.javaClass.getField(field)
        var ref: Field? = null;

        var clz: Class<*>? = this.javaClass
        while (clz != null || clz != Any::class.java) {
            try {
                ref = clz?.getDeclaredField(field)
                break
            } catch (e: NoSuchFieldException) {
                clz = clz?.superclass
            }
        }
        if (ref == null) throw Exception("Field not found " + field)

        ref.isAccessible = true
        return ref.get(this)
    }

    private fun handleFieldWithRender(render: Render, obj: Any?, node: DefaultMutableTreeNode) {
            when(render) {
                is ToStringRender -> {
                    if (render.field != null) {
                        node.add(DefaultMutableTreeNode("${render.displayName}: ${obj?.getPrivateField(render.field)}"))
                    } else {
                        node.add(DefaultMutableTreeNode(obj.toString()))
                    }
                }
                is ListModelRender -> {
                    val top = DefaultMutableTreeNode(render.displayName)
                    val lm = (obj!!.getPrivateField(render.field)) as AbstractListModel<*>
                    lm.forEach {
                        handleFieldWithRender(render.render, it, top)
                    }
                    node.add(top)
                }
                is VectorRender -> {
                    val top = DefaultMutableTreeNode(render.displayName)
                    val lm = (obj!!.getPrivateField(render.field)) as Vector<*>
                    lm.forEach {
                        handleFieldWithRender(render.render, it, top)
                    }
                    node.add(top)
                }
                is ObjectRender -> {
                    val top = DefaultMutableTreeNode(render.displayName ?: obj)
                    render.fields.forEach {
                        handleFieldWithRender(it, obj, top)
                    }
                    node.add(top)
                }
            }
    }

    private fun generateGlobal() {
        val constantsPane = TAPAALGUI.getCurrentTab().getPrivateField("constantsPanel") as ConstantsPane

        val o = ObjectRender(
            ListModelRender("variablesListModel", ToStringRender(), "Variables"),
            ListModelRender("constantsListModel", ToStringRender(), "Constants"),
            ListModelRender("colorTypesListModel", ObjectRender(
                ToStringRender("id"),
                ToStringRender("name"),
                VectorRender("colors", ToStringRender())
            ), "Color Types"),
            displayName = "Global"
        )

        handleFieldWithRender(o, constantsPane, treeRoot)
    }

    init {
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        setSize(600, 900)
        contentPane.add(reloadBtn)

        contentPane.add(JScrollPane(tree))

        reload()
    }


}

private fun <E> AbstractListModel<E>.forEach(function: (e: E)->Unit) {
    for (i in 0 until this.size) {
        function(this.getElementAt(i))
    }
}

sealed class Render()
class ToStringRender(val field: String? = null, val displayName: String? = field): Render()
class ObjectRender(vararg val fields: Render, val displayName: String? = null): Render()
class VectorRender(val field: String, val render: Render, val displayName: String? = field): Render()
class ListModelRender(val field: String, val render: Render, val displayName: String? = field): Render()