package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTASymmetryTransformer extends TAPNToNTATransformer{
	private int numberOfInitChannels = 0;
	private static final String TokenTAName = "Token";

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
			for(Arc presetArc : transition.getPreset()){
				for(Arc postsetArc : transition.getPostset()){
					String sourceName = presetArc.getSource().getName();
					String targetName = postsetArc.getTarget().getName();
					
					if(isPartOfLockTemplate(sourceName)){
						if(isPartOfLockTemplate(targetName)){
							Edge e = new Edge(getLocationByName(sourceName),
									getLocationByName(targetName),
									"",
									createSyncExpression(transition, '!'),
									"");
							lock.addTransition(e);
							break;
						}
					}else{
						if(!isPartOfLockTemplate(targetName)){
							Edge e = new Edge(getLocationByName(sourceName),
									getLocationByName(targetName),
									createTransitionGuard(((TAPNArc)presetArc).getGuard()),
									createSyncExpression(transition, '?'),
									createUpdateExpression((TAPNArc)presetArc));
							token.addTransition(e);
							break;
						}
					}
				}
			}
		}
	}


	private void createLocations(TimedArcPetriNet model, TimedAutomata token,
			TimedAutomata lock) {

		for(TAPNPlace place : model.getPlaces()){
			if(isPartOfLockTemplate(place.getName())){
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
		builder.append(";\ntypedef scalar[N] pid_t;\n");
		

		for(int i = 0; i < numberOfInitChannels; i++){
			builder.append("chan c");
			builder.append(i);
			builder.append(";\n");
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
		Location plock = getLocationByName("P_lock");
		ta.setName(TokenTAName);
		ta.setParameters("const pid_t id");
		ta.setInitLocation(pcapacity);

		List<Token> tokens = model.getTokens();
		for(int i = 0; i < tokens.size(); i++){
			Token token = tokens.get(i);
			Location destination = getLocationByName(token.getPlace().getName());
			
			if(destination != pcapacity && destination != plock){
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

		for(int i = 1; i < numberOfInitChannels; i++){
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

	private boolean isPartOfLockTemplate(String name){
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	@Override
	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		String query = tapnQuery.toString();
		Pattern pattern = Pattern.compile(QUERY_PATTERN);
		Matcher matcher = pattern.matcher(query);
		
		StringBuilder builder = new StringBuilder("(sum(i:pid_t)");
		builder.append(TokenTAName);
		builder.append("(i).$1) $2 $3");
		
		StringBuilder uppaalQuery = new StringBuilder();
		uppaalQuery.append(matcher.replaceAll(builder.toString()));
		uppaalQuery.append(" and ( Lock.P_lock == 1  && Control.finish == 1)");
		return new StandardUPPAALQuery(uppaalQuery.toString());
	}
}
