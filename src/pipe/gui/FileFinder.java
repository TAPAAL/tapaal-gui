package pipe.gui;

import pipe.gui.widgets.filebrowser.FileBrowser;

import java.io.File;

/**
 * @deprecated use FileBrowser.constructor instead
 */
@Deprecated
public class FileFinder {

	public File ShowFileBrowserDialog(String description, String extension, String path) {
		if (path==null || path.equals("")) {
			path= System.getProperty("user.home");
		}

		FileBrowser fileBrowser = FileBrowser.constructor(description, extension, path);
		return fileBrowser.openFile();
	}

}
