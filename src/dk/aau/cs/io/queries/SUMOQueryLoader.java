package dk.aau.cs.io.queries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.SUMOParsing.TokenMgrError;
import dk.aau.cs.TCTL.SUMOParsing.ParseException;
import dk.aau.cs.TCTL.SUMOParsing.SUMOQueryParser;
import dk.aau.cs.TCTL.visitors.RenameTemplateVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;

public class SUMOQueryLoader extends QueryLoader{

	private File file;

	public SUMOQueryLoader(File file, TimedArcPetriNetNetwork network) {
		super(network);
		this.file = file;
	}

	@Override
	protected ArrayList<TAPNQuery> getQueries() {
		ArrayList<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		Pattern p = Pattern.compile("Property\\s*([^\\n]*)[^:]*:(.*)", Pattern.DOTALL);
		byte[] encoded = null;
		try{
			 encoded = Files.readAllBytes(file.toPath());
		} catch (IOException e) {	e.printStackTrace(); }
		String queriesString = new String(encoded);
		String[] textQueries = queriesString.split("\\n\\s*end\\.");
		for(String queryAndNameString : textQueries){
			Matcher m = p.matcher(queryAndNameString);
			if(!m.find() && firstQueryParsingWarning){
				queries.add(null);
				continue;
			}
				
			String name = m.group(1).trim();
			String queryString = m.group(2).trim();

			TCTLAbstractProperty property;
			try{
				property = SUMOQueryParser.parse(queryString);
			} catch (ParseException e){ Logger.log(e); queries.add(null); continue; }
			catch (TokenMgrError e){ Logger.log(e); queries.add(null); continue; }
					
			//The number 9999 is the number of extra tokens allowed, this is set high s.t. we don't have to change it manually
			TAPNQuery query = new TAPNQuery(name, 9999, property, 
					TraceOption.NONE, SearchOption.HEURISTIC, ReductionOption.VerifyPN, 
					true, false, true, true, true, true, HashTableSize.MB_16, ExtrapolationOption.AUTOMATIC, new InclusionPlaces());
					
			RenameTemplateVisitor rt = new RenameTemplateVisitor("", network.activeTemplates().get(0).name());
			query.getProperty().accept(rt, null);
				
			queries.add(query);
		}
		return queries;

	}

	public static void importQueries(File file, TimedArcPetriNetNetwork network){
		SUMOQueryLoader loader = new SUMOQueryLoader(file, network);
		Collection<TAPNQuery> queries = loader.parseQueries();

		for(TAPNQuery query : queries){
			CreateGui.getCurrentTab().addQuery(query);
		}
	}
}
