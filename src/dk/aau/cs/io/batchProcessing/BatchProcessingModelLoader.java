package dk.aau.cs.io.batchProcessing;

import java.io.File;

public class BatchProcessingModelLoader {	
	public BatchProcessingModelLoader(){ }
	
	public LoadedBatchProcessingModel load(File file) throws Exception{
		BatchProcessingLoader newFormatLoader = new BatchProcessingLoader();
		try{
			LoadedBatchProcessingModel loadedModel = newFormatLoader.load(file);
			return loadedModel;
		}catch(Exception e1){
			try {
				BatchProcessingLegacyLoader oldFormatLoader = new BatchProcessingLegacyLoader();
				LoadedBatchProcessingModel loadedModel = oldFormatLoader.load(file);
				return loadedModel;
			} catch(Exception e2) {
				throw e2;
			}
		}
	}
}
