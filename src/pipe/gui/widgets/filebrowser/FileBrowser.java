package pipe.gui.widgets.filebrowser;

import java.io.File;

import dk.aau.cs.util.JavaUtil;
import pipe.gui.CreateGui;


public abstract class FileBrowser  {
    //Used for latest open dialog path
    //Default value null makes the open dialog open default folder, For Windows, My Documents, For *nix  ~ , etc
	//XXX 2018-05-23 moved from CreateGUI, refactor with regards to usage with lastPath local var in this class
    public static String userPath = null;
    String lastPath = null;

	public static FileBrowser constructor(String filetype, final String ext) {
		return constructor(filetype, ext, null);
	}

	public static FileBrowser constructor(String filetype, final String ext, String path) {

		if(JavaUtil.getJREMajorVersion() >= 7){
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
