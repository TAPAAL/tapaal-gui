package pipe.gui.widgets.filebrowser;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import pipe.gui.CreateGui;

class NativeFileBrowser extends FileBrowser {
	private final FileDialog fc;
	private final String ext;
        private final String optionalExt;
	private String specifiedPath;
	NativeFileBrowser(String filetype, final String ext, String path) {
		this(filetype, ext, "", path);
	}
	
	NativeFileBrowser(String filetype, final String ext, final String optionalExt, String path) {
		fc = new FileDialog(CreateGui.getAppGui(), filetype);
		this.specifiedPath = path;

		if (filetype == null) {
			filetype = "file";
		}
		//if(path == null) path = lastPath;

		this.ext = ext;
        this.optionalExt = optionalExt;
		//fc.setDirectory(path);

		// Setup filter if extension specified
                //This is needed for Linux and Mac
		if(!ext.equals("")){
			if(!optionalExt.equals("")) {
				fc.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith( ext ) || name.endsWith(optionalExt);
					}
				});
			}
			else {
				fc.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith( ext );
					}
				});
			}
		}
	}
	

	public File openFile() {
            if(specifiedPath == null) specifiedPath = lastOpenPath;
            fc.setDirectory(specifiedPath);
            //This is needed for Windows
            if(optionalExt.equals("")) fc.setFile(ext.equals("")? "":("*."+ext));
            else fc.setFile(ext.equals("")? "":("*."+ext+";*."+optionalExt));
            fc.setMode(FileDialog.LOAD);
            fc.setMultipleMode(false);
            fc.setVisible(true);
            String selectedFile = fc.getFile();
            String selectedDir = fc.getDirectory();
            lastOpenPath = selectedDir;
            File file = selectedFile == null? null:new File(selectedDir + selectedFile);
            return file;
	}
	
	public File[] openFiles() {
            if(specifiedPath == null) specifiedPath = lastOpenPath;
            fc.setDirectory(specifiedPath);
            //This is needed for Windows
            if(optionalExt.equals("")) fc.setFile(ext.equals("")? "":("*."+ext));
            else fc.setFile(ext.equals("")? "":("*."+ext+";*."+optionalExt));
            fc.setMultipleMode(true);
            fc.setMode(FileDialog.LOAD);
            fc.setVisible(true);
            File[] selectedFiles = fc.getFiles();
        lastOpenPath = fc.getDirectory();
            return selectedFiles;
	}

	public String saveFile(String suggestedName) {
		if(specifiedPath == null) specifiedPath = lastSavePath;
		fc.setDirectory(specifiedPath);
		fc.setFile(suggestedName + (suggestedName.endsWith("."+ext)? "":"."+ext));
		fc.setMode(FileDialog.SAVE);
		fc.setVisible(true);

		// user canceled
		if (fc.getFile() == null) {
			return null;
		}

		// Fixes bug:1648076 for OS X 
		if(fc.getDirectory().endsWith(suggestedName+"."+ext+"/")){
			fc.setDirectory(fc.getDirectory().replaceAll(suggestedName+"."+ext+"/", ""));
		}

		String file = fc.getFile() == null? null: fc.getDirectory() + fc.getFile();
		lastSavePath = fc.getDirectory();
		
		if(file == null){
			return file;
		}
		
		// Windows does not enforce file ending on save
		else if (!file.endsWith("."+ext)) {
			File source = new File(file);
			Pattern p = Pattern.compile(".*\\.(.*)");
			Matcher m = p.matcher(file);
			String newName = file + "." + ext;
			if(m.matches()){
				newName = file.substring(0, file.length()-m.group(1).length()) + ext;
			}
			File destination = new File(newName);

			if(destination.exists()){
				int overRide = JOptionPane.showConfirmDialog(CreateGui.getAppGui(), newName + "\nDo you want to overwrite this file?");
				switch (overRide) {
				case JOptionPane.NO_OPTION:
					source.delete();
					return saveFile(suggestedName);
				case JOptionPane.YES_OPTION:
					destination.delete();
					break;
				default:
					return null;
				}
			}
			
			source.renameTo(destination);
			source.delete();
			try {
				file = destination.getCanonicalPath();
			} catch (IOException e) {
				return null;
			}
		}

		return file;
	}
    public File saveFileToDir(){
	    //In Windows the native FileDialog only works with files
        //So we make a JFileChooser in which we can control it
	    if(System.getProperty("os.name").startsWith("Windows")) {
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
        } else{
	        //For Mac we can set Directories only
            //For linux a save dialog only shows directories
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            String selection = saveFile("Choose Directory");
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if(selection != null) {
                return new File(fc.getDirectory());
            } else{
                return null;
            }
        }

    }
}
