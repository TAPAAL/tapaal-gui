package dk.aau.cs.gui.debug

import dk.aau.cs.gui.TabContentActions
import pipe.gui.GuiFrame
import pipe.gui.action.GuiAction
import java.awt.event.ActionEvent
import java.util.function.Consumer
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JMenu
import javax.swing.KeyStroke

fun noOp() {}
object DEBUG {

    @JvmStatic fun buildMenuDEBUG(): JMenu? {

        val debugMenu = JMenu("DEBUG")

        with(debugMenu) {

            add(object : AbstractAction("Break Point") {
                override fun actionPerformed(e: ActionEvent) = noOp()
            })

            add(object : AbstractAction("Throw Exception") {
                override fun actionPerformed(e: ActionEvent) = throw RuntimeException("Casted Exception from DEBUG")
            })

            add(object : AbstractAction("Show undo/redo stack") {
                override fun actionPerformed(e: ActionEvent) = UndoRedoSpy().show()
            })
        }

        return debugMenu
    }

}