package pipe.gui;

import java.io.File;

public interface FileFinder {
	File ShowFileBrowserDialog(String description, String extension, String path);
}
