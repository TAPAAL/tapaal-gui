package pipe.gui.swingcomponents.filebrowser;

import pipe.gui.TAPAALGUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class FileBrowser {
    //Used for latest open dialog path
    //Default value null makes the open dialog open default folder, For Windows, My Documents, For *nix  ~ , etc
    //XXX 2018-05-23 moved from CreateGUI, refactor with regards to usage with lastPath local var in this class
    public static String userPath = null;
    static String lastSavePath = ".";
    static String lastOpenPath = ".";

    protected final FileDialog fileDialog;
    private final String[] fileExtensions;
    protected String specifiedPath;

    private FileBrowser(String filetype, String[] extensions, String path) {
        fileDialog = new FileDialog(TAPAALGUI.getApp(), filetype);
        this.fileExtensions = extensions;
        this.specifiedPath = path;

        // Setup filter if extension specified used on Linux and MacOS
        if (fileExtensions.length > 0) {
            // FilenameFilter is used on Linux and MacOS, but not working on windows
            fileDialog.setFilenameFilter((dir, name) -> {
                for (String fileExtension : fileExtensions) {
                    if (name.endsWith("." + fileExtension)) {
                        return true;
                    } else if (fileExtension.isBlank()) {
                        return true;
                    }
                }
                return false;
            });
            // Workaround for Windows to filter files in open dialog, overwritten in save menu
            String filter = Arrays.stream(fileExtensions).map(ext -> "*." + ext).collect(Collectors.joining(";"));
            fileDialog.setFile(filter);
        }
    }

    public static FileBrowser constructor(String filetype, final String ext) {
        return constructor(filetype, ext, null);
    }

    public static FileBrowser constructor(String filetype, final String ext, String path) {
        return constructor(filetype, ext, "", path);
    }

    public static FileBrowser constructor(String filetype, final String ext, final String optionalExt, String path) {
        return new FileBrowser(filetype, new String[]{ext, optionalExt}, path);
    }

    public static FileBrowser constructor(String[] extensions, String path) {
        return new FileBrowser(null, extensions, path);
    }

    public File openFile() {
        if (specifiedPath == null) specifiedPath = lastOpenPath;
        fileDialog.setDirectory(specifiedPath);
        //This is needed for Windows

        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);
        String selectedFile = fileDialog.getFile();
        String selectedDir = fileDialog.getDirectory();
        lastOpenPath = selectedDir;
        File file = selectedFile == null ? null : new File(selectedDir + selectedFile);
        return file;
    }

    public File[] openFiles() {
        if (specifiedPath == null) specifiedPath = lastOpenPath;
        fileDialog.setDirectory(specifiedPath);
        //This is needed for Windows

        fileDialog.setMultipleMode(true);
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setVisible(true);
        File[] selectedFiles = fileDialog.getFiles();
        lastOpenPath = fileDialog.getDirectory();
        return selectedFiles;
    }

    public String saveFile() {
        if (TAPAALGUI.getAppGui().getCurrentTabName().endsWith(".tapn")) {
            return saveFile(TAPAALGUI.getAppGui().getCurrentTabName().replaceAll(".tapn", ""));
        } else {
            return saveFile(TAPAALGUI.getAppGui().getCurrentTabName().replaceAll(".xml", ""));
        }
    }

    public String saveFile(String suggestedName) {
        String ext = fileExtensions[0];
        if (specifiedPath == null) specifiedPath = lastSavePath;

        fileDialog.setDirectory(specifiedPath);
        fileDialog.setFile(suggestedName + (suggestedName.endsWith("." + ext) ? "" : "." + ext));
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.setVisible(true);

        // user canceled
        if (fileDialog.getFile() == null) {
            return null;
        }

        // Fixes bug:1648076 for OS X
        if (fileDialog.getDirectory().endsWith(suggestedName + "." + ext + "/")) {
            fileDialog.setDirectory(fileDialog.getDirectory().replaceAll(suggestedName + "." + ext + "/", ""));
        }

        String file = fileDialog.getDirectory() + fileDialog.getFile();
        lastSavePath = fileDialog.getDirectory();

        // Windows does not enforce file ending on save
        if (!file.endsWith("." + ext)) {

            Pattern p = Pattern.compile(".*\\.(.*)");
            Matcher m = p.matcher(file);
            String newName = file + "." + ext;

            // I guess this tries to replace any existing file ending? I don't think this is safe --kyrke
            if (m.matches()) {
                newName = file.substring(0, file.length() - m.group(1).length()) + ext;
            }
            File destination = new File(newName);

            // Overwrite dialog is already shown, but since we changed the named file, we need to show it again
            if (destination.exists()) {
                int overRide = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(), newName + "\nDo you want to overwrite this file?");
                switch (overRide) {
                    case JOptionPane.NO_OPTION:
                        return saveFile(suggestedName); // Reopen dialog to select new name
                    case JOptionPane.YES_OPTION:
                        file = newName;
                        break;
                    default:
                        return null;
                }
            } else  {
                file = newName;
            }
        }
        return file;
    }

    public File saveFileToDir() {
        //In Windows the native FileDialog only works with files
        //So we make a JFileChooser in which we can control it
        if (System.getProperty("os.name").startsWith("Windows")) {
            File selectedDir = null;
            if (specifiedPath == null) specifiedPath = lastSavePath;
            JFileChooser c = new JFileChooser(specifiedPath);
            c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            c.setDialogTitle("Choose target directory for export");
            int rVal = c.showSaveDialog(c);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                selectedDir = c.getSelectedFile();
                lastSavePath = selectedDir.getPath();
            }

            return selectedDir;
        } else {
            //For Mac we can set Directories only
            //For linux a save dialog only shows directories
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            String selection = saveFile("Choose Directory");
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if (selection != null) {
                return new File(fileDialog.getDirectory());
            } else {
                return null;
            }
        }

    }

}
