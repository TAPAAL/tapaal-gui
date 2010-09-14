package pipe.gui;

import java.io.File;

public interface ModelChecker<TModel, TQuery> {
	boolean setup();
		
	String getVersion();
	boolean isCorrectVersion();
	
	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	// TODO: MJ -- get rid of xmlFile and queryFile.. Legacy stuff to support older reductions
	VerificationResult Verify(TModel model, TQuery query, VerificationOptions options, File xmlFile, File queryFile);
}
