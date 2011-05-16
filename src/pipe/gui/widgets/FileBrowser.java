package pipe.gui.widgets;

import java.io.File;
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

	private JFileChooser fc;
	private String ext;

	public FileBrowser(String filetype, String ext, String path) {
		fc = new JFileChooser();
		
		if (filetype == null) {
			filetype = "file";
		}

		if (path != null) {
			File f = new File(path);
			if (f.exists()) {
				fc.setCurrentDirectory(f);
			}
			if (!f.isDirectory()) {
				fc.setSelectedFile(f);
			}
		}

		this.ext = ext;
		ExtensionFilter filter = new ExtensionFilter(ext, filetype);

		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filter);
		// JS: copied the line above again in order to fix the mac filter issues
		// By default hide hidden files
		fc.setFileHidingEnabled(true);
	}

	public FileBrowser(String path) {
		this("Petri net", "xml", path); // default parameters
	}

	public FileBrowser() {
		this(null);
	}

	public File openFile() {
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				return fc.getSelectedFile().getCanonicalFile();
			} catch (IOException e) {
				/* gulp */
			}
		}
		return null;
	}

	public String saveFile() {
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				File f = fc.getSelectedFile();
				if (!f.getName().endsWith("." + ext)) {
					f = new File(f.getCanonicalPath() + "." + ext); // force
																	// extension
				}
							
				 if (!CreateGui.usingGTKFileBrowser() && f.exists() &&  
						 JOptionPane.showConfirmDialog(fc, f.getCanonicalPath() + "\nDo you want to overwrite this file?") != JOptionPane.YES_OPTION) {
				 return null;
				 }
				return f.getCanonicalPath();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(CreateGui.getApp(), "An error occurred while trying to save the file. Please try again", "Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

}
