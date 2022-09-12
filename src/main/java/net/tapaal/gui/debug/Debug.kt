package net.tapaal.gui.debug

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JMenu

fun noOp() {}
object DEBUG {

    @JvmStatic fun buildMenuDEBUG(): JMenu {

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