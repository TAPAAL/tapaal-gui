package net.tapaal.gui.debug

import pipe.gui.TAPAALGUI
import pipe.gui.petrinet.PetriNetTab
import pipe.gui.petrinet.action.GuiAction
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import javax.swing.AbstractAction
import javax.swing.JMenu
import javax.swing.JOptionPane
import javax.swing.KeyStroke

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

            add(object : GuiAction("Inspect net", "Inspect net", KeyStroke.getKeyStroke('I'.code, Toolkit.getDefaultToolkit().menuShortcutKeyMask + InputEvent.SHIFT_MASK)) {
                override fun actionPerformed(e: ActionEvent) = InspectSpy().show()
            })

            add(object : AbstractAction("Show undo/redo stack") {
                override fun actionPerformed(e: ActionEvent) = UndoRedoSpy().show()
            })

            add(object : GuiAction(
                "Verify save/load",
                "Verify save/load",
                KeyStroke.getKeyStroke('L'.code, Toolkit.getDefaultToolkit().menuShortcutKeyMask + InputEvent.SHIFT_MASK)
            ) {
                override fun actionPerformed(e: ActionEvent?) {
                    val tab = TAPAALGUI.getCurrentTab();
                    val tmpFile = kotlin.io.path.createTempFile("tapaalSaveTest", ".tapn").toFile()
                    tab.writeNetToFile(tmpFile)

                    try {
                        PetriNetTab.createNewTabFromFile(tmpFile)
                    } catch (e: Exception) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Debug: Save/Load Error","Failure to save/load, please check stack-trace on console.", JOptionPane.ERROR_MESSAGE)
                    }


                }
            })

        }

        return debugMenu
    }

}