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
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.PetriNetUtil;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.TA.*;
import dk.aau.cs.TAPN.Pairing.ArcType;

public class TAPNToNTABroadcastTransformer implements
ModelTransformer<TimedArcPetriNet, NTA>,
QueryTransformer<TAPNQuery, UPPAALQuery>{

	private int extraTokens;
	private int largestPresetSize = 0;
	private int initTransitions = 0;
	private boolean useSymmetry = false;
	
	protected static final String ID_TYPE = "id_t";
	protected static final String ID_TYPE_NAME = "id";
	protected static final String TOKEN_INTERMEDIATE_PLACE = "%1$s_%2$s_%3$d";
	protected static final String TEST_CHANNEL_NAME = "%1$s_test%2$s";
	protected static final String FIRE_CHANNEL_NAME = "%1$s_fire%2$s";
	protected static final String COUNTER_NAME = "count%1$d";
	protected static final String COUNTER_UPDATE = "%1$s%2$s";
	protected static final String TOKEN_CLOCK_NAME = "x";
	protected static final String PLOCK = "P_lock";
	protected static final String PCAPACITY = "P_capacity";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";

	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	protected Hashtable<Arc, String> arcsToCounters = new Hashtable<Arc, String>();

	public TAPNToNTABroadcastTransformer(int extraTokens, boolean useSymmetry){
		this.extraTokens = extraTokens;
		this.useSymmetry = useSymmetry;
	}

	
	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		clearLocationMappings();
		arcsToCounters.clear();
		largestPresetSize = 0;	
		initTransitions = model.getNumberOfTokens();
		
		try{
			model.convertToConservative();
		}catch(Exception e){
			return null;
		}
		
		NTA nta = new NTA();

		TimedAutomaton tokenTemplate = createTokenTemplate(model);
		nta.addTimedAutomaton(tokenTemplate);
		TimedAutomaton controlTemplate = createControlTemplate(model);
		nta.addTimedAutomaton(controlTemplate);

		nta.setSystemDeclarations("system " + CONTROL_TEMPLATE_NAME + "," + TOKEN_TEMPLATE_NAME + ";");

		String globalDecl = createGlobalDeclarations(model);
		nta.setGlobalDeclarations(globalDecl);

		return nta;
	}



	private String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder("const int N = ");
		builder.append(model.getTokens().size() + extraTokens);
		builder.append(";\ntypedef ");
		
		if(useSymmetry){
			builder.append("scalar[N] ");
		}else{
			builder.append("int[0,N-1] ");
		}
		
		builder.append(ID_TYPE);
		builder.append(";\n");

		for(int i = 0; i < initTransitions; i++){
			builder.append("chan c");
			builder.append(i);
			builder.append(";\n");
		}

		for(TAPNTransition t : model.getTransitions()){
			builder.append("broadcast chan ");
			builder.append(String.format(TEST_CHANNEL_NAME, t.getName(),""));
			builder.append(",");
			builder.append(String.format(FIRE_CHANNEL_NAME, t.getName(),""));
			builder.append(";\n");
		}
		
		for(int i = 0; i < largestPresetSize; i++){
			builder.append("int[0,N] ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}
		
		return builder.toString();
	}

	private TimedAutomaton createControlTemplate(TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();
		control.setName(CONTROL_TEMPLATE_NAME);

		Location last = createInitializationStructure(control, initTransitions);
		Location lock = new Location(PLOCK, ""); 
		control.addLocation(lock);

		Edge e = new Edge(last,
				lock,
				"",
				String.format(INITIALIZE_CHANNEL, initTransitions-1, "!"),
		"");
		control.addTransition(e);

		createTransitionSimulations(control, lock, model);

		return control;
	}

	protected void createTransitionSimulations(TimedAutomaton control, Location lock,
			TimedArcPetriNet model) {

		for(TAPNTransition transition : model.getTransitions()){
			String invariant = createInvariantForControl(transition);

			Location tempLoc = new Location("",invariant);
			tempLoc.setCommitted(true);
			control.addLocation(tempLoc);

			Edge testEdge = new Edge(lock,
					tempLoc,
					"",
					String.format(TEST_CHANNEL_NAME, transition.getName(), "!"),
			"");
			control.addTransition(testEdge);

			Edge fireEdge = new Edge(tempLoc,
					lock,
					createGuardForControl(transition),
					String.format(FIRE_CHANNEL_NAME, transition.getName(), "!"),
					createResetExpressionForControl(transition));
			control.addTransition(fireEdge);
		}
	}

	protected String createResetExpressionForControl(TAPNTransition transition) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for(Arc presetArc : transition.getPreset()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(":=0");
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(":=0");
		}

		return builder.toString();
	}

	private String createGuardForControl(TAPNTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==", 1);
	}

	protected String createInvariantForControl(TAPNTransition transition) {
		return createBooleanExpressionForControl(transition, ">=", "==",1);
	}

	protected String createBooleanExpressionForControl(TAPNTransition transition, String comparison, String inhibComparison, int number)
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
			builder.append(number);
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(" && ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(inhibComparison);
			builder.append("0");
		}

		return builder.toString();
	}

	private Location createInitializationStructure(TimedAutomaton control,
			int numberOfTokens) {

		Location previous = null;

		for(int i = 0; i <= numberOfTokens-1; i++){
			Location loc = new Location("","");
			loc.setCommitted(true);
			control.addLocation(loc);

			if(previous != null){
				Edge e = new Edge(previous, 
						loc, 
						"", 
						String.format(INITIALIZE_CHANNEL, i-1, "!"),
				"");
				control.addTransition(e);
			}else{
				control.setInitLocation(loc);
			}

			previous = loc;
		}

		return previous;
	}

	private TimedAutomaton createTokenTemplate(TimedArcPetriNet model) {		
		TimedAutomaton ta = new TimedAutomaton();
		ta.setName(TOKEN_TEMPLATE_NAME);
		ta.setParameters("const " + ID_TYPE + " " + ID_TYPE_NAME);
		ta.setDeclarations("clock " + TOKEN_CLOCK_NAME + ";");
		createTemplateStructure(ta, model);
		addInitializationStructure(ta, model);
		ta.setInitLocation(getLocationByName(PCAPACITY));
		
		return ta;
	}

	private void addInitializationStructure(TimedAutomaton ta,
			TimedArcPetriNet model) {
		int i = 0;

		for(Token token : model.getTokens()){
			Edge initEdge = new Edge(getLocationByName(PCAPACITY),
					getLocationByName(token.getPlace().getName()),
					"",
					String.format(INITIALIZE_CHANNEL, i, "?"),
			"");
			ta.addTransition(initEdge);
			i++;
		}		
	}

	private void createTemplateStructure(TimedAutomaton ta,
			TimedArcPetriNet model) {
		ta.setLocations(CreateLocationsFromModel(model));

		for(TAPNTransition t : model.getTransitions()){
			int presetSize = t.getPreset().size() + t.getInhibitorArcs().size();
			if(presetSize > largestPresetSize){
				largestPresetSize = presetSize;
			}	

			List<Pairing> pairing = CreatePairing(t);

			createStructureForPairing(ta, t, pairing);
		}	
	}


	protected void createStructureForPairing(TimedAutomaton ta, TAPNTransition t,
			List<Pairing> pairing) {
		int i = 0;
		for(Pairing pair : pairing){
			String inputPlaceName = pair.getInput().getName();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.getName(), i);
			
			Location intermediate = new Location(locationName, "");
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			String counter = String.format(COUNTER_NAME, i);
			arcsToCounters.put(pair.getInputArc(), counter);

			Edge testEdge = new Edge(getLocationByName(inputPlaceName), 
					intermediate, 
					createTransitionGuard(pair.getInterval(), pair.getOutput(), pair.getArcType()==ArcType.TARC),
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					String.format(COUNTER_UPDATE, counter, "++"));
			ta.addTransition(testEdge);

			Edge fireEdge = new Edge(intermediate,
					getLocationByName(pair.getOutput().getName()),
					"", //String.format(COUNTER_UPDATE, i, "==1"),
					String.format(FIRE_CHANNEL_NAME, t.getName(), "?"),
					createResetExpressionIfNormalArc(pair.getArcType()));
			ta.addTransition(fireEdge);

			String guard = String.format(COUNTER_UPDATE, counter,">1");
						
			Edge backEdge = new Edge(intermediate,
					getLocationByName(inputPlaceName),
					guard,
					"",
					String.format(COUNTER_UPDATE, counter, "--"));
			ta.addTransition(backEdge);

			i++;
		}

		createStructureForInhibitorArcs(ta, t, i);
	}


	protected void createStructureForInhibitorArcs(TimedAutomaton ta,
			TAPNTransition t, int i) {
		for(TAPNInhibitorArc inhibArc : t.getInhibitorArcs()){
			String inputPlace = inhibArc.getSource().getName();

			String counter = String.format(COUNTER_NAME, i);
			arcsToCounters.put(inhibArc, counter);

			Location location = getLocationByName(inputPlace);
			Edge inhibEdge = new Edge(location,
					location,
					createTransitionGuard(inhibArc.getGuard()),
					String.format(TEST_CHANNEL_NAME, t.getName(),"?"),
					String.format(COUNTER_UPDATE, counter, "++"));
			ta.addTransition(inhibEdge);
			i++;
		}
	}

	protected String createResetExpressionIfNormalArc(ArcType arcType) {
		if(arcType.equals(ArcType.NORMAL)){
			return String.format("%1s := 0", TOKEN_CLOCK_NAME);
		}else{
			return "";
		}
	}

	private List<Pairing> CreatePairing(TAPNTransition t) {
		List<Pairing> pairing = new ArrayList<Pairing>();
		HashSet<Arc> usedPostSetArcs = new HashSet<Arc>();

		for(Arc inputArc : t.getPreset()){
			for(Arc outputArc : t.getPostset()){
				if(!usedPostSetArcs.contains(outputArc)){
					if(inputArc instanceof TAPNTransportArc && outputArc instanceof TAPNTransportArc && inputArc == outputArc){
						Pairing p = new Pairing(inputArc,
								((TAPNArc)inputArc).getGuard(),
								outputArc,
								ArcType.TARC);
						pairing.add(p);

						usedPostSetArcs.add(outputArc);
						break;
					}else if(!(inputArc instanceof TAPNTransportArc) && !(outputArc instanceof TAPNTransportArc)){
						Pairing p = new Pairing(inputArc,
								((TAPNArc)inputArc).getGuard(),
								outputArc,
								ArcType.NORMAL);
						pairing.add(p);

						usedPostSetArcs.add(outputArc);
						break;
					}
				}
			}
		}

		return pairing;
	}

	private ArrayList<Location> CreateLocationsFromModel(TimedArcPetriNet model) {
		clearLocationMappings();

		ArrayList<Location> locations = new ArrayList<Location>();
		for(TAPNPlace p : model.getPlaces()){
			Location l = new Location(p.getName(), convertInvariant(p.getInvariant()));

			locations.add(l);	
			addLocationMapping(p.getName(), l);
		}

		return locations;
	}
	
	protected String createTransitionGuard(String guard, TAPNPlace target, boolean isTransportArc) {
		String newGuard = PetriNetUtil.createGuard(guard, target, isTransportArc);
		return createTransitionGuard(newGuard);
	}

	protected String createTransitionGuard(String guard) {
		if(guard.equals("false")) return guard;
		if(guard.equals("[0,inf)")) return "";

		String[] splitGuard = guard.substring(1, guard.length()-1).split(",");
		char firstDelim = guard.charAt(0);
		char secondDelim = guard.charAt(guard.length()-1);

		StringBuilder builder = new StringBuilder();
		builder.append(TOKEN_CLOCK_NAME);
		builder.append(" ");

		if(firstDelim == '('){
			builder.append(">");
		} else {
			builder.append(">=");
		}

		builder.append(splitGuard[0]);

		if(!splitGuard[1].equals("inf")){
			builder.append(" && ");
			builder.append(TOKEN_CLOCK_NAME);
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

	protected String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = TOKEN_CLOCK_NAME + " " + invariant;
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