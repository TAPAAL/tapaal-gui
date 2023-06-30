package net.tapaal.gui.debug

import net.tapaal.gui.petrinet.TAPNLens
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
        when (render) {
            is ToStringRender -> {
                val str = if (render.field != null) {
                    "${render.displayName}: ${render.render(obj?.getPrivateField(render.field))}"
                } else {
                    render.render(obj)
                }
                node.add(DefaultMutableTreeNode(str))
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

            is RenderField -> {
                render.fields.forEach {
                    handleFieldWithRender(it, obj?.getPrivateField(render.field), node)
                }
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
        val constantsPane = TAPAALGUI.getCurrentTab()

        val render =
            ObjectRender(
                ToStringRender("lens", render = {
                    it as TAPNLens;
                    "Timed: ${it.isTimed}, Game: ${it.isGame}, Color: ${it.isColored}"
                }, displayName = "Lense"),
                RenderField(
                    "constantsPanel",
                    ListModelRender("variablesListModel", ToStringRender(), "Variables"),
                    ListModelRender("constantsListModel", ToStringRender(), "Constants"),
                    ListModelRender(
                        "colorTypesListModel", ObjectRender(
                            ToStringRender("id"),
                            ToStringRender("name"),
                            VectorRender("colors", ToStringRender())
                        ), "Color Types"
                    ),
                ),
                displayName = "Global"
            )

        handleFieldWithRender(render, constantsPane, treeRoot)
    }

    init {
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        setSize(600, 900)
        contentPane.add(reloadBtn)

        contentPane.add(JScrollPane(tree))

        reload()
    }


}

private fun <E> AbstractListModel<E>.forEach(function: (e: E) -> Unit) {
    for (i in 0 until this.size) {
        function(this.getElementAt(i))
    }
}

sealed class Render()
class ToStringRender(
    val field: String? = null,
    val displayName: String? = field,
    val render: ((Any?) -> String) = { a: Any? -> a?.toString() ?: "null" }
) : Render()

class RenderField(val field: String, vararg val fields: Render) : Render()
class ObjectRender(vararg val fields: Render, val displayName: String? = null) : Render()
class VectorRender(val field: String, val render: Render, val displayName: String? = field) : Render()
class ListModelRender(val field: String, val render: Render, val displayName: String? = field) : Render()