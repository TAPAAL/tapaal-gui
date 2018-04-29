package pipe.gui.widgets;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import pipe.gui.CreateGui;

public class NativeFileBrowser extends FileBrowserImplementation {
	private FileDialog fc;
	private String ext;
	
	public NativeFileBrowser(String filetype, final String ext, String path) {
		fc = new FileDialog(CreateGui.getAppGui(), filetype);

		if (filetype == null) {
			filetype = "file";
		}
		if(path == null) path = lastPath;

		this.ext = ext;
		fc.setDirectory(path);

		// Setup filter if extension specified
		if(!ext.equals("")){
			fc.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith( ext );
				}
			});
		}
	}

	public File openFile() {
		fc.setFile(ext.equals("")? "":"*."+ext);
		fc.setMode(FileDialog.LOAD);
		fc.setMultipleMode(false);
		fc.setVisible(true);
		String selectedFile = fc.getFile();
		String selectedDir = fc.getDirectory();
		lastPath = selectedDir;
		File file = selectedFile == null? null:new File(selectedDir + selectedFile);
		return file;
	}
	
	public File[] openFiles() {
		fc.setFile(ext.equals("")? "":"*."+ext);
		fc.setMultipleMode(true);
		fc.setMode(FileDialog.LOAD);
		fc.setVisible(true);
		File[] selectedFiles = fc.getFiles();
		String selectedDir = fc.getDirectory();
		lastPath = selectedDir;
		return selectedFiles;
	}
	
	public String saveFile(String suggestedName) {
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
		lastPath = fc.getDirectory();
		
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

}
