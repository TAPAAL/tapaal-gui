package pipe.gui.widgets;

import java.io.File;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import pipe.gui.CreateGui;

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
	private Display display;
	private Shell shell;
	private FileDialog fc;
	private static String lastPath;
	private String ext;
	private String filetype;
	
	public FileBrowser(String filetype, final String ext, String path) {				
		if(path == null) path = lastPath;
		this.ext = ext;
		this.filetype = filetype;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				display = new Display();
				shell = new Shell(display);
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
		// File standard dialog
	    fc = new FileDialog(shell, SWT.OPEN);
	    // Set the text
	    fc.setText(filetype);
	    // Set filter on .txt files
	    fc.setFilterExtensions(new String[] { "*.txt" });
	    // Put in a readable name for the filter
	    fc.setFilterNames(new String[] { "Textfiles(*.txt)" });
		// Show dialog
		String selected = fc.open();
		File f = new File(selected);
		lastPath = f.get;
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
		fc.setFile("*."+ext);
		fc.setVisible(true);
		lastPath = fc.getDirectory();
		String file = fc.getFile() == null? null: lastPath + fc.getFile();
		if(!file.endsWith("."+ext)){
			
		}
		return file;
	}

}
