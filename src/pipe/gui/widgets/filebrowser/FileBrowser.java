package pipe.gui.widgets.filebrowser;

import java.io.File;

import dk.aau.cs.util.JavaUtil;
import pipe.gui.CreateGui;


public abstract class FileBrowser  {
    //Used for latest open dialog path
    //Default value null makes the open dialog open default folder, For Windows, My Documents, For *nix  ~ , etc
	//XXX 2018-05-23 moved from CreateGUI, refactor with regards to usage with lastPath local var in this class
    public static String userPath = null;
    static String lastSavePath = ".";
    static String lastOpenPath = ".";

	public static FileBrowser constructor(String filetype, final String ext) {
		return constructor(filetype, ext, null);
	}

	public static FileBrowser constructor(String filetype, final String ext, String path) {
		return constructor(filetype, ext, "", path);
	}
	public static FileBrowser constructor(String filetype, final String ext, final String optionalExt, String path) {

		if(JavaUtil.getJREMajorVersion() >= 7){

            return new NativeFileBrowser(filetype, ext, optionalExt, path);
		}else{
			/*if(path != null) {
				newObject.lastPath = path;
			}*/
			return new NativeFileBrowserFallback(filetype, ext, optionalExt, path);
		}


	}

	public abstract File openFile();

	public abstract File[] openFiles();

	public String saveFile(){
		if(CreateGui.getAppGui().getCurrentTabName().endsWith(".tapn"))
			return saveFile(CreateGui.getAppGui().getCurrentTabName().replaceAll(".tapn", ""));
		else
			return saveFile(CreateGui.getAppGui().getCurrentTabName().replaceAll(".xml", ""));
	}

	public abstract String saveFile(String suggestedName);
	public abstract File saveFileToDir();

}
