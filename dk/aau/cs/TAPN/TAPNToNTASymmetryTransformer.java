package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTASymmetryTransformer extends TAPNToNTATransformer{
	private int numberOfInitChannels = 0;
		
	public TAPNToNTASymmetryTransformer(int extraNumberOfTokens) {
		super(extraNumberOfTokens);
	}
	

	@Override
	protected List<TimedAutomata> createAutomata(TimedArcPetriNet model){
		List<TimedAutomata> tas = createTokenAutomata(model);
		
		TimedAutomata control = createControlTemplate(model);
		tas.add(control);

		return tas;
	}
	
	private List<TimedAutomata> createTokenAutomata(TimedArcPetriNet model) {
		TimedAutomata token = new TimedAutomata();
		TimedAutomata lock = new TimedAutomata();
		
		createLocations(model, token, lock);
		token.setName("Token");
		token.setParameters("const pid_t id");
		token.setDeclarations("clock x;");
		token.setInitLocation(getLocationByName("P_capacity"));
		
		lock.setName("Lock");
		lock.setInitLocation(getLocationByName("P_lock"));
		
		createTransitions(model, token, lock);
		
		addSymmetricInitialization(token, model);
		
		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();
		tas.add(token);
		tas.add(lock);
		return tas;
	}

	private void createTransitions(TimedArcPetriNet model, TimedAutomata token,
			TimedAutomata lock) {
		for(TAPNTransition transition : model.getTransitions()){
			
		}
	}


	private void createLocations(TimedArcPetriNet model, TimedAutomata token,
			TimedAutomata lock) {
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_[0-9]*_(?:in|out)|P_lock)$");
			
		for(TAPNPlace place : model.getPlaces()){
			Matcher matcher = pattern.matcher(place.getName());
			if(matcher.find()){
				Location l = new Location(place.getName(), "");
				if(place.getInvariant().equals("<=0")){
					l.setCommitted(true);
				}
				lock.addLocation(l);
				addLocationMapping(l.getName(), l);
			}else{
				Location l = new Location(place.getName(), convertInvariant(place.getInvariant()));
				token.addLocation(l);
				addLocationMapping(l.getName(), l);
			}
		}
	}


	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder("const int N = ");
		builder.append(model.getTokens().size());
		builder.append(";typedef scalar[N] pid_t;");
		
		for(int i = 0; i < numberOfInitChannels; i++){
			builder.append("chan c");
			builder.append(i);
			builder.append(";");
		}
		
		builder.append(super.createGlobalDeclarations(model));
		
		return builder.toString();
	}
	
	@Override
	protected String createSystemDeclaration() {
		return "system Control, Token, Lock;";
	}

	private void addSymmetricInitialization(TimedAutomata ta, TimedArcPetriNet model){
		numberOfInitChannels = 0;
		Location pcapacity = getLocationByName("P_capacity");

		ta.setName("Token");
		ta.setParameters("const pid_t id");
		ta.setInitLocation(pcapacity);
		
		List<Token> tokens = model.getTokens();
		for(int i = 0; i < tokens.size(); i++){
			Token token = tokens.get(i);
			Location destination = getLocationByName(token.getPlace().getName());
			
			if(destination != pcapacity){
				numberOfInitChannels++;
				Edge e = new Edge(pcapacity, destination, "", "c" + i + "?", "");
				ta.addTransition(e);
			}
		}
	}
	
	private TimedAutomata createControlTemplate(TimedArcPetriNet model) {
		TimedAutomata control = new TimedAutomata();
		
		Location lastLocation = new Location("","");
		lastLocation.setCommitted(true);
		control.addLocation(lastLocation);
		control.setInitLocation(lastLocation);
		
		for(int i = 1; i < numberOfInitChannels-1; i++){
			Location l = new Location("","");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(lastLocation, l, "", "c" + (i-1) + "!", "");
			control.addTransition(e);
			
			lastLocation = l;
		}
		
		Location finish = new Location("finish","");
		control.addLocation(finish);
		
		Edge e = new Edge(lastLocation, finish, "", "c" + (numberOfInitChannels-1) + "!", "");
		control.addTransition(e);
		
		control.setName("Control");
		
		return control;		
	}
	
	@Override
	public UPPAALQuery transformQuery(TAPNQuery query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
