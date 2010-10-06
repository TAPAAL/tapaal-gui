package dk.aau.cs.TA;

import java.io.PrintStream;

public class KBoundOptmizerUPPAALQuery implements UPPAALQuery {
	public UPPAALQuery query;
	public UPPAALQuery supQuery;
	
	public KBoundOptmizerUPPAALQuery(UPPAALQuery query){
		this.query = query;
		this.supQuery = new SupQuery("usedExtraTokens");
	}
	
	public void output(PrintStream file) {
		query.output(file);
		supQuery.output(file);
	}
}
