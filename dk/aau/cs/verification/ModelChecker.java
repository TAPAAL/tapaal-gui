package dk.aau.cs.verification;

import java.io.File;

// TODO: MJ -- This interface is getting somewhat bloated -- Try to fix it
public interface ModelChecker<TModel, TQuery> {
	boolean setup();
		
	String getVersion();
	boolean isCorrectVersion();
	
	String getPath(); // TODO: MJ -- Delete me when refactoring is done

	VerificationResult verify(VerificationOptions options, TModel model, TQuery... queries);
	// TODO: MJ -- get rid of xmlFile and queryFile.. Legacy stuff to support older reductions
	VerificationResult verify(VerificationOptions options, File modelFile, File queryFile);
	
	void kill();
}
