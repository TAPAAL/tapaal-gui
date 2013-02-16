package pipe.gui.widgets;

import java.io.File;
import pipe.gui.CreateGui;


public class FileBrowser extends FileBrowserImplementation {
	private FileBrowserImplementation fb = null;

	public FileBrowser(String path) {
		this("Timed-Arc Petri Net", "xml", path); // default parameters
	}
	
	public FileBrowser(String filetype, final String ext) {
		this(filetype, ext, null);
	}
	
	public FileBrowser(String filetype, final String ext, String path) {
		if(fb == null){
			fb = new NativeFileBrowserFallback(filetype, ext, path);
		}
		if(path != null) fb.lastPath = path;
	}

	public FileBrowser() {
		this(null);
	}

	public File openFile() {
		return fb.openFile();
	}
	
	public File[] openFiles() {
		return fb.openFiles();
	}
	
	public String saveFile(){
		return saveFile(CreateGui.appGui.getCurrentTabName());
	}

	public String saveFile(String suggestedName) {
		return fb.saveFile(suggestedName);
	}

}
