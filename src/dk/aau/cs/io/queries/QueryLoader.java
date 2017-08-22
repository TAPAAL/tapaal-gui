package dk.aau.cs.io.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.TCTL.visitors.VerifyTransitionNamesVisitor;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;

public abstract class QueryLoader {
	
	protected static final String ERROR_PARSING_QUERY_MESSAGE = "TAPAAL encountered an error trying to parse one or more of the queries in the model.\n\nThe queries that could not be parsed will not show up in the query list.";
	
	protected boolean firstQueryParsingWarning = true;
	boolean queryUsingNonexistentPlaceFound = false;
	boolean queryUsingNonexistentTransitionFound = false;

        protected boolean showErrorMessage = true;
	
	protected TimedArcPetriNetNetwork network;
	
	public QueryLoader(TimedArcPetriNetNetwork network) {
		this.network = network;
	}
	
	public Collection<TAPNQuery> parseQueries() {
		ArrayList<TAPNQuery> queries = getQueries();
		
		ArrayList<Tuple<String, String>> templatePlaceNames = getPlaceNames(network);
		ArrayList<Tuple<String, String>> templateTransitionNames = getTransitionNames(network);

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
			} else if(!doesTransitionsUsedInQueryExist(query, templateTransitionNames)){
				queryUsingNonexistentTransitionFound = true;
				iterator.remove();
			}
		}
		
		if(queryUsingNonexistentPlaceFound && firstQueryParsingWarning && showErrorMessage) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), ERROR_PARSING_QUERY_MESSAGE, "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
			firstQueryParsingWarning = false;
		} else if(queryUsingNonexistentTransitionFound && firstQueryParsingWarning && showErrorMessage){
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
	
	private ArrayList<Tuple<String, String>> getTransitionNames(TimedArcPetriNetNetwork network) {
		ArrayList<Tuple<String,String>> templateTransitionNames = new ArrayList<Tuple<String,String>>();
		for(TimedArcPetriNet tapn : network.allTemplates()) {
			for(TimedTransition t : tapn.transitions()) {
				templateTransitionNames.add(new Tuple<String, String>(tapn.name(), t.name()));
			}
		}
		
		for(SharedTransition t : network.sharedTransitions()) {
			templateTransitionNames.add(new Tuple<String, String>("", t.name()));
		}
		return templateTransitionNames;
	}
	
	private boolean doesPlacesUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templatePlaceNames) {
		VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

		VerifyPlaceNamesVisitor.Context c = nameChecker.verifyPlaceNames(query.getProperty());
		
		return c.getResult();
		
	}
	
	private boolean doesTransitionsUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templateTransitionNames) {
		VerifyTransitionNamesVisitor nameChecker = new VerifyTransitionNamesVisitor(templateTransitionNames);

		VerifyTransitionNamesVisitor.Context c = nameChecker.verifyTransitionNames(query.getProperty());
		
		return c.getResult();
		
	}
	
	protected abstract ArrayList<TAPNQuery> getQueries();
}
