package pipe.gui.widgets;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import dk.aau.cs.gui.TabContent;

import pipe.gui.CreateGui;
import pipe.gui.ExtensionFilter;
import pipe.gui.GuiFrame;

public class NativeFileBrowserFallback extends FileBrowserImplementation {
	private FileDialog fc;
	private String ext;
	private JFileChooser fileChooser;
	
	public NativeFileBrowserFallback(String filetype, final String ext, String path) {
		fc = new FileDialog(CreateGui.appGui, filetype);

		if (filetype == null) {
			filetype = "file";
		}
		if(path == null) path = lastPath;

		this.ext = ext;
		fc.setDirectory(path);

		/* Setup JFileChooser for multi file selection */
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// Setup filter if extension specified
		if(!ext.equals("")){
			fc.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith( ext );
				}
			});
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					filetype, new String[] { ext });
			fileChooser.setFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(filter);
			fileChooser.setMultiSelectionEnabled(true);
		}
	}

	public File openFile() {
		fc.setFile(ext.equals("")? "":"*."+ext);
		fc.setMode(FileDialog.LOAD);
		fc.setVisible(true);
		String selectedFile = fc.getFile();
		String selectedDir = fc.getDirectory();
		lastPath = selectedDir;
		File file = selectedFile == null? null:new File(selectedDir + selectedFile);
		return file;
	}
	
	public File[] openFiles() {
		if (lastPath != null) {
			File path = new File(lastPath);
			if (path.exists()) {
				fileChooser.setCurrentDirectory(path);
			}
		}
		File[] filesArray = new File[0];
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			filesArray = fileChooser.getSelectedFiles();
		}
		return filesArray;
	}
	
	
	public String saveFile(String suggestedName) {
		fc.setFile(suggestedName + (suggestedName.endsWith("."+ext)? "":"."+ext));
		fc.setMode(FileDialog.SAVE);
		fc.setVisible(true);

		String file = fc.getFile() == null? null: fc.getDirectory() + fc.getFile();
		lastPath = fc.getDirectory();
		
		if(file == null){
			return file;
		}
		
		// Windows does not enforce file ending on save
		else if (!file.endsWith("."+ext)) {
			File source = new File(file);
			File destination = new File(file+"."+ext);

			if(destination.exists()){
				int overRide = JOptionPane.showConfirmDialog(CreateGui.appGui, file + "." + ext + "\nDo you want to overwrite this file?");
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
