package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.dialog.ExportBatchDialog;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class AddFileExportBatchCommand implements Command{
    private final DefaultListModel<File> listModel;
    final List<File> files;
    final File file;
    private final ExportBatchDialog dialog;

    public AddFileExportBatchCommand(DefaultListModel<File> listModel, File file, List<File> files, ExportBatchDialog dialog){
        this.listModel = listModel;
        this.file = file;
        this.files = files;
        this.dialog = dialog;
    }

    @Override
    public void redo() {
        files.add(file);
        listModel.addElement(file);
        dialog.enableButtons();
    }

    @Override
    public void undo() {
        files.remove(file);
        listModel.removeElement(file);
        dialog.enableButtons();
    }
}
