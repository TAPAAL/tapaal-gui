package net.tapaal.gui

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.io.IOException

abstract class FileDropTarget(private val supportedFileExtension: Array<String> = arrayOf("tapn", /*"xml", "pnml"*/)) : DropTarget() {

    abstract fun onDrop(files: List<File>)

    @Synchronized
    override fun drop(dropEvent: DropTargetDropEvent) {
        dropEvent.acceptDrop(DnDConstants.ACTION_REFERENCE)
        val files: List<File> = getDraggedFileOrNull(dropEvent.transferable)
        if (files.isNotEmpty()) {
            onDrop(files)
        }

    }

    @Synchronized
    override fun dragOver(dragEvent: DropTargetDragEvent) {
        try {
            val file = getDraggedFileOrNull(dragEvent.transferable)
            if (file.isNotEmpty()) {
                dragEvent.acceptDrag(DnDConstants.ACTION_COPY)
            } else {
                dragEvent.rejectDrag()
            }
        } catch (e: UnsupportedFlavorException) {
            throw java.lang.RuntimeException(e)
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    @Throws(UnsupportedFlavorException::class, IOException::class)
    private fun getDraggedFileOrNull(transferable: Transferable): List<File> {
        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>

        return files.filter {
                file -> supportedFileExtension.contains(file.extension)
        }

    }

}