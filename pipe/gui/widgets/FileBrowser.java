package pipe.gui.widgets;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import pipe.gui.ExtensionFilter;

/**
 * @author Maxim
 * 
 *         Opens a file browser with appropriate settings for the given
 *         filetype/extension 2010-05-07: Kenneth Yrke Jørgensen: changed
 *         behaviour of the class from extending JFileChooser, to have a static
 *         instance. This is an ugly hack to handle speed problems with the
 *         java-reimplementation of the GTKJFileChooser. The
 *         java-reimplementation of GTKJFileChooser is needed as the default
 *         implementation is completly useless.
 */
public class FileBrowser {

	static JFileChooser fc = new JFileChooser();
	private String ext;

	public FileBrowser(String filetype, String ext, String path) {
		super();
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
				// if (f.exists() &&
				// JOptionPane.showConfirmDialog(fc, f.getCanonicalPath() +
				// "\nDo you want to overwrite this file?") !=
				// JOptionPane.YES_OPTION) {
				// return null;
				// }
				return f.getCanonicalPath();
			} catch (IOException e) {
				/* gulp */
			}
			;
		}
		return null;
	}

}
