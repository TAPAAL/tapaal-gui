package pipe.gui.widgets;

import java.io.File;

/**
 * This class bounces to the 
 */

public abstract class FileBrowserImplementation {
	String lastPath = null;

	public abstract File openFile();
	public abstract File[] openFiles();
	public abstract String saveFile(String suggestedName);

}
