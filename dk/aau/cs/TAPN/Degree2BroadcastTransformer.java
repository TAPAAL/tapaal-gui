package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TAPN.Pairing.ArcType;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.degree2converters.InhibDegree2Converter;

public class Degree2BroadcastTransformer implements
ModelTransformer<TimedArcPetriNet, NTA>,
QueryTransformer<TAPNQuery, UPPAALQuery>{

	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	private static final String PLOCK = "P_lock";
	private static final String P_CAPACITY = "P_capacity";
	private static final String TOKEN_TEMPLATE_NAME = "Token";
	private static final String CONTROL_TEMPLATE_NAME = "Control";
	private static final String ID_TYPE = "pid_t";
	private static final String ID_PARAMETER_NAME = "id";
	private static final String CLOCK_NAME = "x";
	private static final String COUNTER_NAME = "count%1$d";
	private static final String COUNTER_UPDATE = "%1$s%2$s";
	private static final String TEST_CHANNEL = "%1$s_test%2$s";
	private static final String INIT_CHANNEL = "c%1$d%2$s";
	protected static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	protected static final String T_MAX_FORMAT = "%1$s_%2$d";
	protected static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;

	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	private Hashtable<Arc, String> arcsToCounters = new Hashtable<Arc, String>();


	private int numberOfInitChannels = 0;
	private int extraTokens = 0;
	private int largestPresetSize = 0;

	public Degree2BroadcastTransformer(int extraTokens) {
		this.extraTokens = extraTokens;
	}

	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		arcsToCounters.clear();
		clearLocationMappings();
		numberOfInitChannels = 0;
		largestPresetSize = 0;

		TimedArcPetriNet degree2Net = new InhibDegree2Converter().transform((TAPN)model);

		NTA nta = new NTA();
		nta.addTimedAutomaton(createTokenAutomaton(degree2Net, model));
		nta.addTimedAutomaton(createControlAutomaton(degree2Net, model));
		nta.setSystemDeclarations("system " + CONTROL_TEMPLATE_NAME + "," + TOKEN_TEMPLATE_NAME + ";");
		nta.setGlobalDeclarations(createGlobalDeclarations(degree2Net, model));

		return nta;
	}

	private String createGlobalDeclarations(TimedArcPetriNet degree2Net, TimedArcPetriNet originalModel) {
		StringBuilder builder = new StringBuilder("const int N = ");
		builder.append(degree2Net.getTokens().size() + extraTokens);
		builder.append(";\ntypedef ");
		builder.append("scalar[N] ");
		builder.append(ID_TYPE);
		builder.append(";\n");

		for(int i = 0; i < numberOfInitChannels; i++){
			builder.append("chan ");
			builder.append(String.format(INIT_CHANNEL, i,""));
			builder.append(";\n");
		}

		for(TAPNTransition t : degree2Net.getTransitions()){
			builder.append("chan ");
			builder.append(t.getName());
			builder.append(";\n");
		}

		for(TAPNTransition t : originalModel.getTransitions()){
			builder.append("broadcast chan ");
			builder.append(String.format(TEST_CHANNEL, t.getName(),""));
			builder.append(";\n");
		}		

		for(int i = 0; i < largestPresetSize; i++){
			builder.append("bool ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}

		return builder.toString();
	}

	protected String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = CLOCK_NAME + " " + invariant;
		}

		return inv;
	}

	protected Location getLocationByName(String name){
		return namesToLocations.get(name);
	}

	protected void addLocationMapping(String name, Location location){
		namesToLocations.put(name, location);
	}

	protected void clearLocationMappings(){
		namesToLocations.clear();
	}

	private TimedAutomaton createControlAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();
		createInitialLocationsForControlAutomaton(degree2Net, control);
		createEdgesForControlAutomaton(degree2Net, model, control);
		Location initial = createInitializationTransitionsForControlAutomaton(degree2Net,control);

		control.setName(CONTROL_TEMPLATE_NAME);
		control.setInitLocation(initial);
		return control;
	}

	private Location createInitializationTransitionsForControlAutomaton(
			TimedArcPetriNet degree2Net, TimedAutomaton control) {
		Location first = new Location("","");
		first.setCommitted(true);
		control.addLocation(first);
		Location prev = first;

		for(int i = 0; i < degree2Net.getNumberOfTokens()-2; i++){
			Location l = new Location("","");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(prev,
					l,
					"",
					String.format(INIT_CHANNEL, i, "!"),
			"");
			control.addTransition(e);
			prev = l;
		}

		Edge e = new Edge(prev,
				getLocationByName(PLOCK),
				"",
				String.format(INIT_CHANNEL, degree2Net.getNumberOfTokens()-2,"!"),
		"");
		control.addTransition(e);
		return first;
	}

	private void createEdgesForControlAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet originalModel,
			TimedAutomaton control) {
		for(TAPNTransition transition : degree2Net.getTransitions()){
			Pairing pairing = createPairing(transition, false);

			if(!pairing.getInput().getName().equals(PLOCK)){
				Edge e = new Edge(getLocationByName(pairing.getInput().getName()),
						getLocationByName(pairing.getOutput().getName()),
						"",
						transition.getName() + "!",
						"");
				control.addTransition(e);
			}
		}	
		
		for(TAPNTransition transition : originalModel.getTransitions()){
			Location ptest = new Location("",createInvariantForControl(transition));
			ptest.setCommitted(true);
			control.addLocation(ptest);

			Edge first = new Edge(getLocationByName(PLOCK),
					ptest,
					"",
					String.format(TEST_CHANNEL, transition.getName(), "!"),
			"");
			control.addTransition(first);

			if(transition.getPreset().size() != 1){
			Edge second = new Edge(ptest,
					getLocationByName(String.format(P_T_IN_FORMAT, transition.getName(), 1)),
					"",
					String.format(T_I_IN_FORMAT+"%3$s", transition.getName(), 1, "!"),
					createResetExpressionForControl(transition));
			control.addTransition(second);
			}else{
				Edge second = new Edge(ptest,
						getLocationByName(PLOCK),
						"",
						String.format(T_MAX_FORMAT+"%3$s", transition.getName(), 1, "!"),
						createResetExpressionForControl(transition));
				control.addTransition(second);
			}
		}
	}

	private void createInitialLocationsForControlAutomaton(
			TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for(TAPNPlace place: degree2Net.getPlaces()){			
			if(isPartOfLockTemplate(place.getName())){
				Location l = new Location(place.getName(),"");
				
				if(!place.getName().equals(PLOCK)){
					l.setCommitted(true);
				}
				
				ta.addLocation(l);
				addLocationMapping(place.getName(), l);
			}
		}
	}

	private TimedAutomaton createTokenAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet originalModel) {
		TimedAutomaton token = new TimedAutomaton();
		createInitialLocationsForTokenAutomata(degree2Net, token);
		createTestingEdgesForTokenAutomata(originalModel, token);
		createEdgesForTokenAutomata(degree2Net, token);
		createInitializationTransitionsForTokenAutomata(degree2Net, token);
		
		token.setName(TOKEN_TEMPLATE_NAME);
		token.setDeclarations("clock " + CLOCK_NAME + ";");
		token.setInitLocation(getLocationByName(P_CAPACITY));
		token.setParameters("const " + ID_TYPE + " " + ID_PARAMETER_NAME);

		return token;
	}

	private void createInitializationTransitionsForTokenAutomata(TimedArcPetriNet degree2Net,
			TimedAutomaton ta) {
		int i = 0;
		for(Token token : degree2Net.getTokens()){
			if(!token.getPlace().getName().equals(PLOCK) && !token.getPlace().getName().equals(P_CAPACITY)){
				Edge e = new Edge(getLocationByName(P_CAPACITY),
						getLocationByName(token.getPlace().getName()),
						"",
						String.format(INIT_CHANNEL, i, "?"),
				"");
				ta.addTransition(e);
				i++;
				numberOfInitChannels++;
			}
		}
	}

	private void createEdgesForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton token) {
		for(TAPNTransition transition : degree2Net.getTransitions()){
			Pairing pairing = createPairing(transition, true);

			Edge e = new Edge(getLocationByName(pairing.getInput().getName()),
					getLocationByName(pairing.getOutput().getName()),
					createTransitionGuard(pairing.getInterval()),
					transition.getName() + "?",
					CreateResetExpressionIfNormalArc(pairing.getArcType()));
			token.addTransition(e);
		}		
	}

	private Pairing createPairing(TAPNTransition transition, boolean isForTokenAutomaton) {
		Arc source = null;
		Arc dest = null;

		for(Arc presetArc : transition.getPreset()){
			if(isForTokenAutomaton && !isPartOfLockTemplate(presetArc.getSource().getName()))
			{
				source = presetArc;
				break;
			}else if(!isForTokenAutomaton && isPartOfLockTemplate(presetArc.getSource().getName())){
				source = presetArc;
				break;
			}
		}

		for(Arc postsetArc : transition.getPostset()){
			if(isForTokenAutomaton && !isPartOfLockTemplate(postsetArc.getTarget().getName()))
			{
				dest = postsetArc;
				break;
			}else if(!isForTokenAutomaton && isPartOfLockTemplate(postsetArc.getTarget().getName())){
				dest = postsetArc;
				break;
			}
		}

		boolean isTransportArc = source instanceof TAPNTransportArc;
		ArcType type = isTransportArc ? ArcType.TARC : ArcType.NORMAL;

		return new Pairing(source,((TAPNArc)source).getGuard(), dest, type);
	}

	private void createTestingEdgesForTokenAutomata(TimedArcPetriNet originalModel, TimedAutomaton ta) {
		
		for(TAPNTransition transition : originalModel.getTransitions()){
			int size = transition.getPreset().size() + transition.getInhibitorArcs().size();
			if(size > largestPresetSize) largestPresetSize = size;
			
			int i = 0;
			for(Arc arc : transition.getPreset()){
				String source = arc.getSource().getName();
				String counter = String.format(COUNTER_NAME, i);
				arcsToCounters.put(arc, counter);

				Edge e = new Edge(getLocationByName(source),
						getLocationByName(source),
						createTransitionGuard(((TAPNArc)arc).getGuard()),
						String.format(TEST_CHANNEL, transition.getName(), "?"),
						String.format(COUNTER_UPDATE, counter, "= true"));
				ta.addTransition(e);		
				i++;
			}
			
			for(TAPNInhibitorArc arc : transition.getInhibitorArcs()){
				String source = arc.getSource().getName();
				String counter = String.format(COUNTER_NAME, i);
				arcsToCounters.put(arc, counter);
				
				Edge e = new Edge(getLocationByName(source),
						getLocationByName(source),
						createTransitionGuard(arc.getGuard()),
						String.format(TEST_CHANNEL, arc.getTarget().getName(), "?"),
						String.format(COUNTER_UPDATE, counter, "=true"));
				ta.addTransition(e);
				i++;
			}
		}
	}

	private void createInitialLocationsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for(TAPNPlace place: degree2Net.getPlaces()){			
			if(!isPartOfLockTemplate(place.getName())){
				Location l = new Location(place.getName(), convertInvariant(place.getInvariant()));
				ta.addLocation(l);
				addLocationMapping(place.getName(), l);
			}
		}
	}

	private String createInvariantForControl(TAPNTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==");
	}

	private String createBooleanExpressionForControl(TAPNTransition transition, String comparison, String inhibComparison)
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for(Arc presetArc : transition.getPreset()){
			if(!first){
				builder.append(" && ");
			}

			String counter = arcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append("true");
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(" && ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(inhibComparison);
			builder.append("false");
		}

		return builder.toString();
	}

	private String createResetExpressionForControl(TAPNTransition transition) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for(Arc presetArc : transition.getPreset()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(":=false");
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(":=false");
		}

		return builder.toString();
	}

	protected String createTransitionGuard(String guard) {
		if(guard.equals("[0,inf)")) return "";

		String[] splitGuard = guard.substring(1, guard.length()-1).split(",");
		char firstDelim = guard.charAt(0);
		char secondDelim = guard.charAt(guard.length()-1);

		StringBuilder builder = new StringBuilder();
		builder.append(CLOCK_NAME);
		builder.append(" ");

		if(firstDelim == '('){
			builder.append(">");
		} else {
			builder.append(">=");
		}

		builder.append(splitGuard[0]);

		if(!splitGuard[1].equals("inf")){
			builder.append(" && ");
			builder.append(CLOCK_NAME);
			builder.append(" ");

			if(secondDelim == ')'){
				builder.append("<");
			}else {
				builder.append("<=");
			}
			builder.append(splitGuard[1]);
		}

		return builder.toString();
	}	

	private String CreateResetExpressionIfNormalArc(ArcType arcType) {
		if(arcType.equals(ArcType.NORMAL)){
			return String.format("%1s := 0", CLOCK_NAME);
		}else{
			return "";
		}
	}

	private boolean isPartOfLockTemplate(String name){
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		String query = tapnQuery.toString();
		Pattern pattern = Pattern.compile(QUERY_PATTERN);
		Matcher matcher = pattern.matcher(query);

		StringBuilder builder = new StringBuilder("(sum(i:");
		builder.append(ID_TYPE);
		builder.append(")");
		builder.append(TOKEN_TEMPLATE_NAME);
		builder.append("(i).$1) $2 $3");

		StringBuilder uppaalQuery = new StringBuilder();
		uppaalQuery.append(matcher.replaceAll(builder.toString()));
			
		if(tapnQuery.isEFQuery() || tapnQuery.isAFQuery()){
			uppaalQuery.append(" and ");
		}else{
			uppaalQuery.append(" or !");
		}
		uppaalQuery.append("Control.");
		uppaalQuery.append(PLOCK);
		
		return new StandardUPPAALQuery(uppaalQuery.toString());
	}
}
