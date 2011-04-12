package dk.aau.cs.io;

import java.io.File;

import pipe.gui.DrawingSurfaceImpl;

public class ModelLoader {
	private DrawingSurfaceImpl drawingSurface;
	
	public ModelLoader(DrawingSurfaceImpl drawingSurface){
		this.drawingSurface = drawingSurface;
	}
	
	public LoadedModel load(File file) throws Exception{
		TapnXmlLoader newFormatLoader = new TapnXmlLoader(drawingSurface);
		try{
			LoadedModel loadedModel = newFormatLoader.load(file);
			return loadedModel;
		}catch(Exception e1){
			try {
				TapnLegacyXmlLoader oldFormatLoader = new TapnLegacyXmlLoader(drawingSurface);
				LoadedModel loadedModel = oldFormatLoader.load(file);
				return loadedModel;
			} catch(Exception e2) {
				throw e2;
			}
		}
	}
}
