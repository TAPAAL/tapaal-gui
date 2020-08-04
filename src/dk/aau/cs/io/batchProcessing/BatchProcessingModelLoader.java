package dk.aau.cs.io.batchProcessing;

import dk.aau.cs.io.TapnLegacyXmlLoader;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.util.FormatException;

import java.io.File;

public class BatchProcessingModelLoader {	
	public BatchProcessingModelLoader(){ }
	
	public LoadedBatchProcessingModel load(File file) throws FormatException {
		var newFormatLoader = new TapnXmlLoader();
		try{
			return newFormatLoader.load(file);
		}catch(Exception e1){
            var oldFormatLoader = new TapnLegacyXmlLoader();
            return oldFormatLoader.load(file);
        }
	}
}
