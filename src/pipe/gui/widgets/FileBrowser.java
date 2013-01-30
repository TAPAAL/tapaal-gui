package pipe.gui.widgets;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import pipe.gui.CreateGui;
import pipe.gui.ExtensionFilter;

/**
 * @author Maxim
 *
 * Opens a file browser with appropriate settings for the given filetype/extension
 * 2010-05-07: Kenneth Yrke Jørgensen: changed behaviour of the class from extending JFileChooser,
 * to have a static instance. This is an ugly hack to handle speed problems with the java-reimplementation
 * of the GTKJFileChooser. The java-reimplementation of GTKJFileChooser is needed as the default implementation
 * is completly useless. 
 * 2011-01-02: Kenneth Yrke Jørgensen: Changed back to orginal behaviour as GTKJFileChooser seems to be fixed. 
 * We might want to change this class so the save/load dialog remembers the last folder it was in (like it did
 * when it was static)
 */

public class FileBrowser {
	private static String lastPath;
	private FileDialog fc;

	public FileBrowser(String filetype, final String ext, String path) {
		fc = new FileDialog(CreateGui.appGui, filetype);

		if (filetype == null) {
			filetype = "file";
		}
		if(path == null) path = lastPath;

		fc.setFile("*."+ext);
		fc.setDirectory(path);

		fc.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith( "." + ext );
			}
		});
	}

	public FileBrowser(String path) {
		this("Timed-Arc Petri Net", "xml", path); // default parameters
	}

	public FileBrowser() {
		this(null);
	}

	public File openFile() {
		setupLastPath();
		fc.setMode(FileDialog.LOAD);
		fc.setVisible(true);
		String selectedFile = fc.getFile();
		String selectedDir = fc.getDirectory();
		lastPath = selectedDir;
		File file = new File(selectedDir + selectedFile);
		return file;
	}

	private void setupLastPath() {
		if(lastPath != null){
			fc.setDirectory(lastPath);
		}
	}

	public String saveFile() {
		setupLastPath();
		fc.setMode(FileDialog.SAVE);
		fc.setVisible(true);

		String file = fc.getFile() == null? null: fc.getDirectory() + fc.getFile();
		lastPath = fc.getDirectory();
		
		if(file == null){
			return file;
		}
		
		// Windows does not enforce file ending on save
		else if (!file.endsWith(".xml")) {
			File source = new File(file);
			File destination = new File(file+".xml");

			if(destination.exists()){
				int overRide = JOptionPane.showConfirmDialog(CreateGui.appGui, file + ".xml" + "\nDo you want to overwrite this file?");
				switch (overRide) {
				case JOptionPane.NO_OPTION:
					source.delete();
					return saveFile();
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
