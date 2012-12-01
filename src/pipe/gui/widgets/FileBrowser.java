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
		this("Petri Net", "xml", path); // default parameters
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
		lastPath = fc.getDirectory();
		return fc.getFile() == null? null: lastPath + fc.getFile();
	}

}
