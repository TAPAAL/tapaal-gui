package dk.aau.cs.io.batchProcessing;

import dk.aau.cs.util.FormatException;

import java.io.File;

public class BatchProcessingModelLoader {	
	public BatchProcessingModelLoader(){ }
	
	public LoadedBatchProcessingModel load(File file) throws FormatException {
		BatchProcessingLoader newFormatLoader = new BatchProcessingLoader();
		try{
			return newFormatLoader.load(file);
		}catch(Exception e1){
            BatchProcessingLegacyLoader oldFormatLoader = new BatchProcessingLegacyLoader();
            return oldFormatLoader.load(file);
        }
	}
}
