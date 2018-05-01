package pipe.gui;

import java.io.File;

import pipe.gui.widgets.FileBrowser;

public class FileFinder {

	public File ShowFileBrowserDialog(String description, String extension, String path) {
		if (path==null || path=="") { path= System.getProperty("user.home"); }
		FileBrowser fileBrowser = new FileBrowser(description, extension, path);
		return fileBrowser.openFile();
	}

}
