package dk.aau.cs.translations;

import java.io.PrintStream;

import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNQuery;

/**
 * @deprecated use ModelTranslator and QueryTranslator interfaces instead
 */
@Deprecated
public interface UppaalTransformer {

	public void autoTransform(TAPN model, PrintStream uppaalXML,
			PrintStream queryFile, TAPNQuery query, int numberOfTokens);

	public TAPN transform(TAPN model);

	public void transformToUppaal(TAPN model, PrintStream uppaalXML,
			int numberOfTokens);

	public void transformQueriesToUppaal(TAPN model, int numberOfEkstraTokens,
			TAPNQuery inputQuery, PrintStream stream) throws Exception;

}
