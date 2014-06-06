package dk.aau.cs.io.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.InclusionPlaces;
import pipe.gui.widgets.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Tuple;

public abstract class QueryLoader {
	
	protected static final String ERROR_PARSING_QUERY_MESSAGE = "TAPAAL encountered an error trying to parse one or more of the queries in the model.\n\nThe queries that could not be parsed will not show up in the query list.";
	
	protected boolean firstQueryParsingWarning = true;
	
	protected TimedArcPetriNetNetwork network;
	
	public QueryLoader(TimedArcPetriNetNetwork network) {
		this.network = network;
	}
	
	public Collection<TAPNQuery> parseQueries() {
		ArrayList<TAPNQuery> queries = getQueries();
		
		ArrayList<Tuple<String, String>> templatePlaceNames = getPlaceNames(network);
		
		boolean queryUsingNonexistentPlaceFound = false;
		Iterator<TAPNQuery> iterator = queries.iterator();
		while(iterator.hasNext()){
			TAPNQuery query = iterator.next();	
			if (query == null) {
				iterator.remove();
				continue;
			}
			
			if(!doesPlacesUsedInQueryExist(query, templatePlaceNames)) {
				queryUsingNonexistentPlaceFound = true;
				iterator.remove();
			}
		}
		
		if(queryUsingNonexistentPlaceFound && firstQueryParsingWarning) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), ERROR_PARSING_QUERY_MESSAGE, "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
			firstQueryParsingWarning = false;
		}
		
		return queries;
	}
	
	private ArrayList<Tuple<String, String>> getPlaceNames(TimedArcPetriNetNetwork network) {
		ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
		for(TimedArcPetriNet tapn : network.allTemplates()) {
			for(TimedPlace p : tapn.places()) {
				templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
			}
		}
		
		for(TimedPlace p : network.sharedPlaces()) {
			templatePlaceNames.add(new Tuple<String, String>("", p.name()));
		}
		return templatePlaceNames;
	}
	
	private boolean doesPlacesUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templatePlaceNames) {
		VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

		VerifyPlaceNamesVisitor.Context c = nameChecker.verifyPlaceNames(query.getProperty());
		
		return c.getResult();
		
	}
	
	protected abstract ArrayList<TAPNQuery> getQueries();
}
