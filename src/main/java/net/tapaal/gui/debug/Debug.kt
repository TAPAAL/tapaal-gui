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

    // getCurrentTab()/getApp() are deprecated for production code; fine in this debug-only menu.
    @Suppress("DEPRECATION")
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
                override fun actionPerformed(e: ActionEvent) = UndoRedoSpy().setVisible(true)
            })

            add(object : GuiAction(
                "Verify save/load",
                "Verify save/load",
                KeyStroke.getKeyStroke('L'.code, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx or InputEvent.SHIFT_DOWN_MASK)
            ) {
                override fun actionPerformed(e: ActionEvent?) {
                    val tab = TAPAALGUI.getCurrentTab();
                    val tmpFile = kotlin.io.path.createTempFile("tapaalSaveTest", ".tapn").toFile()
                    tab.writeNetToFile(tmpFile)

                    try {
                        PetriNetTab.createNewTabFromFile(tmpFile)
                    } catch (e: Exception) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "Debug: Save/Load Error","Failure to save/load, please check stack-trace on console.", JOptionPane.ERROR_MESSAGE)
                    }
                }
            })
        }

        return debugMenu
    }

}