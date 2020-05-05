package pipe.gui.widgets.filebrowser;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import pipe.gui.CreateGui;

class NativeFileBrowserFallback extends FileBrowser {
	private final FileDialog fc;
	private final String ext;
        private final String optionalExt;
	private String specifiedPath;
	private final JFileChooser fileChooser;
	/**
		This show native open/save dialogs for all type of dialogs except multifile open,
	 	Java before 7, have problems with multiselect for native dialogs.
	 */
	NativeFileBrowserFallback(String filetype, final String ext, String path) {
		this(filetype, ext, "", path);
	}
	NativeFileBrowserFallback(String filetype, final String ext, final String optionalExt, String path) {
		fc = new FileDialog(CreateGui.getAppGui(), filetype);
		
		specifiedPath = path;
		if (filetype == null) {
			filetype = "file";
		}
		//if(path == null) path = lastPath;

		this.ext = ext;
                this.optionalExt = optionalExt;
		//fc.setDirectory(path);

		/* Setup JFileChooser for multi file selection */
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter;

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
				filter = new FileNameExtensionFilter(
						filetype, new String[] { ext, optionalExt });
			}else {
				fc.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith( ext );
					}
				});
				filter = new FileNameExtensionFilter(
						filetype, new String[] { ext });
			}

			
			fileChooser.setFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(filter);
			fileChooser.setMultiSelectionEnabled(true);
		}
	}

	public File openFile() {
		if(specifiedPath == null) specifiedPath = lastOpenPath;
		fc.setDirectory(specifiedPath);
                //This is needed for windows
		if(optionalExt.equals("")) fc.setFile(ext.equals("")? "":("*."+ext));
                else fc.setFile(ext.equals("")? "":("*."+ext+";*."+optionalExt));
		fc.setMode(FileDialog.LOAD);
		fc.setVisible(true);
		String selectedFile = fc.getFile();
		String selectedDir = fc.getDirectory();
		lastOpenPath = selectedDir;
		File file = selectedFile == null? null:new File(selectedDir + selectedFile);
		return file;
	}

	public File[] openFiles() {
		if(specifiedPath == null) specifiedPath = lastOpenPath;
		if(new File(specifiedPath).exists()) fileChooser.setCurrentDirectory(new File(specifiedPath));
		/*if (lastPath != null) {
			File path = new File(lastPath);
			if (path.exists()) {
				fileChooser.setCurrentDirectory(path);
			}
		}*/
		File[] filesArray = new File[0];
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			filesArray = fileChooser.getSelectedFiles();
		}
		//They should all come from the same directory so we just take one
		lastOpenPath = filesArray[0].getAbsolutePath();
		return filesArray;
	}


	public String saveFile(String suggestedName) {
		if(specifiedPath == null) specifiedPath = lastSavePath;
		fc.setDirectory(specifiedPath);
		fc.setFile(suggestedName + (suggestedName.endsWith("."+ext)? "":"."+ext));
		fc.setMode(FileDialog.SAVE);
		fc.setVisible(true);

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
