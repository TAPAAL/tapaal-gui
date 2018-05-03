package pipe.gui.widgets.filebrowser;

import java.io.File;
import pipe.gui.CreateGui;


public abstract class FileBrowser  {
	String lastPath = null;

	public static FileBrowser constructor(String filetype, final String ext) {
		return constructor(filetype, ext, null);
	}

	public static FileBrowser constructor(String filetype, final String ext, String path) {

		if(CreateGui.getAppGui().getJRE() >= 7){
			FileBrowser newObject = new NativeFileBrowser(filetype, ext, path);
			if(path != null) {
				newObject.lastPath = path;
			}

			return newObject;
		}else{
			FileBrowser newObject = new NativeFileBrowserFallback(filetype, ext, path);
			if(path != null) {
				newObject.lastPath = path;
			}
			return newObject;
		}


	}

	public abstract File openFile();

	public abstract File[] openFiles();

	public String saveFile(){
		return saveFile(CreateGui.getAppGui().getCurrentTabName().replaceAll(".xml", ""));
	}

	public abstract String saveFile(String suggestedName);

}
