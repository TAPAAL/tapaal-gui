package dk.aau.cs.io;

import java.io.File;
import java.io.InputStream;

import dk.aau.cs.TCTL.Parsing.ParseException;
import pipe.gui.DrawingSurfaceImpl;

public class ModelLoader {

	public ModelLoader(){
	}
	
	public LoadedModel load(File file) throws Exception{		
		TapnXmlLoader newFormatLoader = new TapnXmlLoader();
		try{
			LoadedModel loadedModel = newFormatLoader.load(file);
			return loadedModel;
		}catch(Throwable e1){
			try {
				TapnLegacyXmlLoader oldFormatLoader = new TapnLegacyXmlLoader();
				LoadedModel loadedModel = oldFormatLoader.load(file);
				return loadedModel;
			} catch(Throwable e2) {
				throw new ParseException(e1.getMessage());
			}
		}
	}
	
	
	public LoadedModel load(InputStream file) throws Exception{
		TapnXmlLoader newFormatLoader = new TapnXmlLoader();
		try{
			LoadedModel loadedModel = newFormatLoader.load(file);
			return loadedModel;
		}catch(Throwable e1){
			try {
				TapnLegacyXmlLoader oldFormatLoader = new TapnLegacyXmlLoader();
				LoadedModel loadedModel = oldFormatLoader.load(file);
				return loadedModel;
			} catch(Throwable e2) {
				throw new ParseException(e1.getMessage());
			}
		}
	}
	
}
