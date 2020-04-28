package dk.aau.cs.io.batchProcessing;

import java.io.File;

public class BatchProcessingModelLoader {	
	public BatchProcessingModelLoader(){ }
	
	public LoadedBatchProcessingModel load(File file) throws Exception{
		BatchProcessingLoader newFormatLoader = new BatchProcessingLoader();
		try{
			return newFormatLoader.load(file);
		}catch(Exception e1){
			try {
				BatchProcessingLegacyLoader oldFormatLoader = new BatchProcessingLegacyLoader();
				return oldFormatLoader.load(file);
			} catch(Exception e2) {
				throw e2;
			}
		}
	}
}
