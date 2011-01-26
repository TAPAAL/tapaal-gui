package pipe.gui;

import java.io.File;

import pipe.gui.widgets.FileBrowser;

public class FileFinderImpl implements FileFinder {

	public File ShowFileBrowserDialog(String description, String extension) {
		FileBrowser fileBrowser = new FileBrowser(description, extension, null);
		return fileBrowser.openFile();
	}

}
