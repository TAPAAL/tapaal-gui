package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TCTL.visitors.BroadcastTranslationQueryVisitor;
import dk.aau.cs.model.NTA.Edge;
import dk.aau.cs.model.NTA.Location;
import dk.aau.cs.model.NTA.NTA;
import dk.aau.cs.model.NTA.StandardUPPAALQuery;
import dk.aau.cs.model.NTA.TimedAutomaton;
import dk.aau.cs.model.NTA.UPPAALQuery;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.Pairing;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;

public class BroadcastTranslation implements ModelTranslator<TimedArcPetriNet, TAPNQuery, NTA, UPPAALQuery> {

	private int extraTokens;
	private int largestPresetSize = 0;
	private int initTransitions = 0;
	protected boolean useSymmetry = false;

	protected static final String ID_TYPE = "id_t";
	protected static final String ID_TYPE_NAME = "id";
	protected static final String TOKEN_INTERMEDIATE_PLACE = "%1$s_%2$s_%3$d";
	protected static final String TEST_CHANNEL_NAME = "%1$s_test%2$s";
	protected static final String FIRE_CHANNEL_NAME = "%1$s_fire%2$s";
	protected static final String COUNTER_NAME = "count%1$d";
	protected static final String COUNTER_UPDATE = "%1$s%2$s";
	protected static final String TOKEN_CLOCK_NAME = "x";
	protected static final String PLOCK = "P_lock";
	protected static final String PCAPACITY = "_BOTTOM_";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	protected static final String LOCK_BOOL = "lock";

	private final Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	protected Hashtable<TimedInputArc, String> inputArcsToCounters = new Hashtable<TimedInputArc, String>();
	protected Hashtable<TimedInhibitorArc, String> inhibitorArcsToCounters = new Hashtable<TimedInhibitorArc, String>();
	protected Hashtable<TransportArc, String> transportArcsToCounters = new Hashtable<TransportArc, String>();

	public BroadcastTranslation(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}
	
	public Tuple<NTA, UPPAALQuery> translate(TimedArcPetriNet model, TAPNQuery query) throws Exception {
		if(!supportsModel(model))
			throw new UnsupportedModelException("Broadcast Translation does not support the given model.");
		
		if(!supportsQuery(model, query)) // has no effect atm since broadcast translation supports all queries
			throw new UnsupportedQueryException("Broadcast Translation does not support the given query.");
		
		extraTokens = query.getExtraTokens();
		NTA nta = transformModel(model);
		UPPAALQuery uppaalQuery = transformQuery(query, model);
		
		return new Tuple<NTA, UPPAALQuery>(nta, uppaalQuery);
	}

	private NTA transformModel(TimedArcPetriNet model) {
		// if there are no tokens in the model, add an extra place with a token
		if(model.marking().size() + extraTokens == 0){
			LocalTimedPlace extraPlace = new LocalTimedPlace("EXTRA434723_324"); 
			model.add(extraPlace);
			model.addToken(new TimedToken(extraPlace));
		}
		
		clearLocationMappings();
		clearArcMappings();
		largestPresetSize = 0;
		initTransitions = model.marking().size();

		TimedArcPetriNet conservativeModel = null;
		try {
			TAPNToConservativeTAPNConverter converter = new TAPNToConservativeTAPNConverter();
			conservativeModel = converter.makeConservative(model);
		} catch (Exception e) {
			return null;
		}
		
		NTA nta = new NTA();

		if (useSymmetry || conservativeModel.marking().size() + extraTokens == 0) {
			TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel, null);
			addInitializationStructure(tokenTemplate, conservativeModel);
			tokenTemplate.setName(TOKEN_TEMPLATE_NAME);
			if (useSymmetry)
				tokenTemplate.setParameters("const " + ID_TYPE + " " + ID_TYPE_NAME);
			tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
			nta.addTimedAutomaton(tokenTemplate);
		} else {
			int j = 0;
			for(TimedPlace p : conservativeModel.places()) {
				for (TimedToken token : conservativeModel.marking().getTokensFor(p)) {
					clearLocationMappings();
					clearArcMappings();
					TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel, token);
					tokenTemplate.setInitLocation(getLocationByName(token.place().name()));
					nta.addTimedAutomaton(tokenTemplate);
					tokenTemplate.setName(TOKEN_TEMPLATE_NAME + j);
					j++;
				}
			}

			TimedPlace bottom = conservativeModel.getPlaceByName(PCAPACITY);
			for (int i = 0; i < extraTokens; i++) {
				clearLocationMappings();
				clearArcMappings();
				TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel, new TimedToken(bottom));
				tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME + (conservativeModel.marking().size() + i));
			}
		}

		TimedAutomaton controlTemplate = createControlTemplate(conservativeModel);
		nta.addTimedAutomaton(controlTemplate);

		nta.setSystemDeclarations(createSystemDeclaration(conservativeModel.marking().size()));

		String globalDecl = createGlobalDeclarations(conservativeModel);
		nta.setGlobalDeclarations(globalDecl);

		return nta;
	}

	

	private String createSystemDeclaration(int tokensInModel) {
		if (useSymmetry || tokensInModel + extraTokens == 0) {
			return "system " + CONTROL_TEMPLATE_NAME + "," + TOKEN_TEMPLATE_NAME + ";";
		} else {
			StringBuilder builder = new StringBuilder("system ");
			builder.append(CONTROL_TEMPLATE_NAME);

			for (int i = 0; i < extraTokens + tokensInModel; i++) {
				builder.append(", ");
				builder.append(TOKEN_TEMPLATE_NAME);
				builder.append(i);
			}
			builder.append(';');

			return builder.toString();
		}
	}

	private String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder();
		builder.append("const int N = ");
		builder.append(model.marking().size() + extraTokens);
		builder.append(";\n");

		if (useSymmetry) {
			builder.append("typedef ");
			builder.append("scalar[N] ");
			builder.append(ID_TYPE);
			builder.append(";\n");

			for (int i = 0; i < initTransitions; i++) {
				builder.append("chan c");
				builder.append(i);
				builder.append(";\n");
			}
		}

		for (TimedTransition t : model.transitions()) {
			if(t.presetSizeWithoutInhibitorArcs() == 0 && !t.hasInhibitorArcs()) {
				continue;
			} else if (isTransitionDegree1(t) && !t.hasInhibitorArcs()) {
				builder.append("broadcast chan ");
				builder.append(t.name());
				builder.append(';');
			} else if (isTransitionDegree2(t) && !t.hasInhibitorArcs()) {
				builder.append("chan ");
				builder.append(t.name());
				builder.append(";\n");
			} else {
				builder.append("broadcast chan ");
				builder.append(String.format(TEST_CHANNEL_NAME, t.name(), ""));
				builder.append(',');
				builder.append(String.format(FIRE_CHANNEL_NAME, t.name(), ""));
				builder.append(";\n");
			}
		}

		for (int i = 0; i < largestPresetSize; i++) {
			builder.append("int[0,N] ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}

		builder.append("bool ");
		builder.append(LOCK_BOOL);
		builder.append(" = false;\n");

		return builder.toString();
	}

	private boolean isTransitionDegree1(TimedTransition t) {
		return t.presetSizeWithoutInhibitorArcs() == 1 && t.postsetSize() == 1;
	}
	
	private boolean isTransitionDegree2(TimedTransition t) {
		return t.presetSizeWithoutInhibitorArcs() == 2 && t.postsetSize() == 2;
	}

	private TimedAutomaton createControlTemplate(TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();
		control.setName(CONTROL_TEMPLATE_NAME);

		Location lock = new Location(PLOCK, "");
		control.addLocation(lock);

		if (useSymmetry) {
			Location last = createInitializationStructure(control);

			if (last == null) {
				control.setInitLocation(lock);
			} else {
				Edge e = new Edge(last, lock, "", String.format(INITIALIZE_CHANNEL, initTransitions - 1, "!"), "");
				control.addTransition(e);
			}
		} else {
			control.setInitLocation(lock);
		}

		createTransitionSimulations(control, lock, model);

		return control;
	}

	protected void createTransitionSimulations(TimedAutomaton control, Location lock, TimedArcPetriNet model) {

		for (TimedTransition transition : model.transitions()) {
			if(transition.presetSizeWithoutInhibitorArcs() == 0 && !transition.hasInhibitorArcs())
				continue;
			
			if (!(isTransitionDegree1(transition) || isTransitionDegree2(transition)) || transition.hasInhibitorArcs()) {
				String invariant = createInvariantForControl(transition);

				Location tempLoc = new Location("", invariant);
				tempLoc.setCommitted(true);
				control.addLocation(tempLoc);

				Edge testEdge = new Edge(lock, tempLoc, "", String.format(TEST_CHANNEL_NAME, transition.name(), "!"), lockUpdateStatement(true));
				control.addTransition(testEdge);

				Edge fireEdge = new Edge(tempLoc, lock,	createGuardForControl(transition), 
						String.format(FIRE_CHANNEL_NAME, transition.name(), "!"),
						createResetExpressionForControl(transition));
				control.addTransition(fireEdge);
			}
		}
	}

	private String lockUpdateStatement(boolean value) {
		return LOCK_BOOL + " = " + value;
	}

	protected String createResetExpressionForControl(TimedTransition transition) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for (TimedInputArc presetArc : transition.getInputArcs()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = inputArcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(":=0");
			first = false;
		}
		
		for (TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = transportArcsToCounters.get(transArc);
			builder.append(counter);
			builder.append(":=0");
			first = false;
		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = inhibitorArcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(":=0");
			first = false;
		}

		if (!first) {
			builder.append(", ");
		}
		builder.append(lockUpdateStatement(false));

		return builder.toString();
	}

	private String createGuardForControl(TimedTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==", 1);
	}

	protected String createInvariantForControl(TimedTransition transition) {
		return createBooleanExpressionForControl(transition, ">=", "==", 1);
	}

	protected String createBooleanExpressionForControl(TimedTransition transition, String comparison, String inhibComparison, int number) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for (TimedInputArc presetArc : transition.getInputArcs()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = inputArcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append(number);
			first = false;
		}
		
		for (TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = transportArcsToCounters.get(transArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append(number);
			first = false;
		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = inhibitorArcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(inhibComparison);
			builder.append('0');
		}

		return builder.toString();
	}

	private Location createInitializationStructure(TimedAutomaton control) {

		Location previous = null;

		for (int i = 0; i <= initTransitions - 1; i++) {
			Location loc = new Location("", "");
			loc.setCommitted(true);
			control.addLocation(loc);

			if (previous != null) {
				Edge e = new Edge(previous, loc, "", String.format(
						INITIALIZE_CHANNEL, i - 1, "!"), "");
				control.addTransition(e);
			} else {
				control.setInitLocation(loc);
			}

			previous = loc;
		}

		return previous;
	}

	private TimedAutomaton createTokenTemplate(TimedArcPetriNet model, TimedToken token) {
		TimedAutomaton ta = new TimedAutomaton();

		String declarations = createLocalDeclarations();
		ta.setDeclarations(declarations);
		createTemplateStructure(ta, model);

		return ta;
	}

	protected String createLocalDeclarations() {
		return "clock " + TOKEN_CLOCK_NAME + ";";
	}

	protected void addInitializationStructure(TimedAutomaton ta, TimedArcPetriNet model) {
		int j = 0;
		for(TimedPlace p : model.places()) {
			for (int i = 0; i < p.numberOfTokens(); i++) {
				Edge initEdge = new Edge(getLocationByName(PCAPACITY), getLocationByName(p.name()), "",	String.format(INITIALIZE_CHANNEL, j, "?"), "");
				ta.addTransition(initEdge);
				j++;
			}
		}
	}
	
	private void createTemplateStructure(TimedAutomaton ta,	TimedArcPetriNet model) {
		ta.setLocations(CreateLocationsFromModel(model));

		for (TimedTransition t : model.transitions()) {
			int presetSize = t.presetSizeWithoutInhibitorArcs() + t.getInhibitorArcs().size();
			
			if(presetSize == 0)
				continue;
			
			if (presetSize > largestPresetSize) {
				largestPresetSize = presetSize;
			}
			
			Pairing pairing = new Pairing(t);
			if(isTransitionDegree1(t) && !t.hasInhibitorArcs()) {
				createDegree1Structure(ta,t, pairing);
			} else if (isTransitionDegree2(t) && !t.hasInhibitorArcs()) {
				createDegree2Structure(ta, t, pairing);
			} else {
				createStructureForPairing(ta, t, pairing);
			}
		}
	}

	private void createDegree1Structure(TimedAutomaton ta, TimedTransition t, Pairing pairing) {
		for(TimedInputArc inputArc : t.getInputArcs()) {
			TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
			Edge e = new Edge(getLocationByName(inputArc.source().name()),
					getLocationByName(outputArc.destination().name()),
					createTransitionGuardWithLock(inputArc, outputArc, outputArc.destination(), false), t.name() + "!",
					createResetExpressionForNormalArc());

			ta.addTransition(e);
		}
		
		for(TransportArc transArc : t.getTransportArcsGoingThrough()) {
			Edge e = new Edge(getLocationByName(transArc.source().name()),
					getLocationByName(transArc.destination().name()),
					createTransitionGuardWithLock(transArc, transArc.destination()), 
					t.name() + "!",
					"");

			ta.addTransition(e);
		}
	}

	private void createDegree2Structure(TimedAutomaton ta, TimedTransition t, Pairing pairing) {
		boolean first = true;
		
		for(TimedInputArc inputArc : t.getInputArcs()) {
			TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
			Edge e = new Edge(getLocationByName(inputArc.source().name()),
					getLocationByName(outputArc.destination().name()),
					createTransitionGuardWithLock(inputArc, outputArc, outputArc.destination(), false), t.name() + (first ? "?" : "!"),
					createResetExpressionForNormalArc());
			ta.addTransition(e);
			first = false;
		}
		
		for(TransportArc transArc : t.getTransportArcsGoingThrough()) {
			Edge e = new Edge(getLocationByName(transArc.source().name()),
					getLocationByName(transArc.destination().name()),
					createTransitionGuardWithLock(transArc, transArc.destination()), 
					t.name() + (first ? "?" : "!"),
					"");
			
			ta.addTransition(e);
			first = false;
		}
	}

	private String createTransitionGuardWithLock(TimedInputArc inputArc, TimedOutputArc outputArc, TimedPlace timedPlace, boolean isTransportArc) {
		String guard = convertGuard(inputArc.interval());

		if (guard == null || guard.isEmpty()) {
			guard = LOCK_BOOL + " == 0";
		} else {
			guard += " && " + LOCK_BOOL + " == 0";
		}

		return guard;
	}
	
	private String createTransitionGuardWithLock(TransportArc transArc,	TimedPlace destination) {
		String guard = "";
		
		try {
			TimeInterval newInterval = transArc.interval().intersect(destination.invariant());
			guard = convertGuard(newInterval);
		} catch(Exception e) {
			guard = "false";
		}

		if (guard == null || guard.isEmpty()) {
			guard = LOCK_BOOL + " == 0";
		} else {
			guard += " && " + LOCK_BOOL + " == 0";
		}

		return guard;
	}

	

	protected void createStructureForPairing(TimedAutomaton ta,	TimedTransition t, Pairing pairing) {
		int i = 0;
		
		for(TimedInputArc inputArc : t.getInputArcs()) {
			String inputPlaceName = inputArc.source().name();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.name(), i);

			Location intermediate = new Location(locationName, "");
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			String counter = String.format(COUNTER_NAME, i);
			inputArcsToCounters.put(inputArc, counter);

			createTestFireStructure(ta, t, pairing, inputArc.source(), 
					convertGuard(inputArc.interval()), 
					pairing.getOutputArcFor(inputArc).destination(), 
					intermediate, counter,false);

			i++;
		}
		
		for(TransportArc transArc : t.getTransportArcsGoingThrough()) {
			String inputPlaceName = transArc.source().name();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.name(), i);

			Location intermediate = new Location(locationName, "");
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			String counter = String.format(COUNTER_NAME, i);
			transportArcsToCounters.put(transArc, counter);

			String guard = "";
			
			try {
				TimeInterval newInterval = transArc.interval().intersect(transArc.destination().invariant());
				guard = convertGuard(newInterval);
			} catch(Exception e) {
				guard = "false";
			}
			createTestFireStructure(ta, t, pairing, transArc.source(), 
					guard, 
					transArc.destination(), 
					intermediate, counter, true);

			i++;
		}

		createStructureForInhibitorArcs(ta, t, i);
	}

	private void createTestFireStructure(TimedAutomaton ta, TimedTransition t, Pairing pairing, TimedPlace inputPlace, String testTransitionGuard, TimedPlace OutputPlace,  Location intermediate, String counter, boolean isTransportArc) {
		Edge testEdge = new Edge(getLocationByName(inputPlace.name()),
				intermediate, testTransitionGuard, 
				String.format(TEST_CHANNEL_NAME, t.name(), "?"),
				String.format(COUNTER_UPDATE, counter, "++"));
		ta.addTransition(testEdge);

		Edge fireEdge = new Edge(intermediate, getLocationByName(OutputPlace.name()),
				"",
				String.format(FIRE_CHANNEL_NAME, t.name(), "?"),
				isTransportArc ? "" : createResetExpressionForNormalArc());
		ta.addTransition(fireEdge);

		String guard = String.format(COUNTER_UPDATE, counter, ">1");

		Edge backEdge = new Edge(intermediate, getLocationByName(inputPlace.name()), guard, "", String.format(COUNTER_UPDATE, counter, "--"));
		ta.addTransition(backEdge);
	}

	protected void createStructureForInhibitorArcs(TimedAutomaton ta, TimedTransition t, int i) {
		for (TimedInhibitorArc inhibArc : t.getInhibitorArcs()) {
			String inputPlace = inhibArc.source().name();

			String counter = String.format(COUNTER_NAME, i);
			inhibitorArcsToCounters.put(inhibArc, counter);

			Location location = getLocationByName(inputPlace);
			Edge inhibEdge = new Edge(location, location,
					convertGuard(inhibArc.interval()), 
					String.format(TEST_CHANNEL_NAME, t.name(), "?"),
					String.format(COUNTER_UPDATE, counter, "++"));
			ta.addTransition(inhibEdge);
			i++;
		}
	}

	private String createResetExpressionForNormalArc() {
		return String.format("%1s := 0", TOKEN_CLOCK_NAME);
	}

	private ArrayList<Location> CreateLocationsFromModel(TimedArcPetriNet model) {
		clearLocationMappings();

		ArrayList<Location> locations = new ArrayList<Location>();
		for (TimedPlace p : model.places()) {
			Location l = new Location(p.name(), convertInvariant(p));

			locations.add(l);
			addLocationMapping(p.name(), l);
		}

		return locations;
	}
	
	private String convertGuard(TimeInterval interval) {
		if(interval.equals(TimeInterval.ZERO_INF))
			return "";
		
		StringBuilder builder = new StringBuilder();
		boolean lowerBoundAdded = false;
		if(!(interval.lowerBound().value() == 0 && interval.isLowerBoundNonStrict())) {
			builder.append(TOKEN_CLOCK_NAME);
			if(interval.isLowerBoundNonStrict())
				builder.append(" >= ");
			else
				builder.append(" > ");
		
			builder.append(interval.lowerBound().value());
			lowerBoundAdded = true;
		}
		
		if(!interval.upperBound().equals(Bound.Infinity)) {
			if(lowerBoundAdded) builder.append(" && ");
			builder.append(TOKEN_CLOCK_NAME);
			
			if(interval.isUpperBoundNonStrict())
				builder.append(" <= ");
			else
				builder.append(" < ");
			
			builder.append(interval.upperBound().value());
		}
		
		return builder.toString();
	}
	
	protected String convertInvariant(TimedPlace p) {
		String inv = "";
		TimeInvariant invariant = p.invariant();
		if (!invariant.equals(TimeInvariant.LESS_THAN_INFINITY)) {
			inv = TOKEN_CLOCK_NAME + " " + invariant.toString(false);
		}

		return inv;
	}

	protected Location getLocationByName(String name) {
		return namesToLocations.get(name);
	}

	protected void addLocationMapping(String name, Location location) {
		namesToLocations.put(name, location);
	}

	protected void clearLocationMappings() {
		namesToLocations.clear();
	}
	
	private void clearArcMappings() {
		inputArcsToCounters.clear();
		inhibitorArcsToCounters.clear();
		transportArcsToCounters.clear();	
	}

	private UPPAALQuery transformQuery(TAPNQuery tapnQuery, TimedArcPetriNet model) {
		BroadcastTranslationQueryVisitor visitor = new BroadcastTranslationQueryVisitor(useSymmetry, model.marking().size() + tapnQuery.getExtraTokens());

		return new StandardUPPAALQuery(visitor.getUppaalQueryFor(tapnQuery));
	}

	public TranslationNamingScheme namingScheme() {
		return new BroadcastNamingScheme();
	}

	protected static class BroadcastNamingScheme implements TranslationNamingScheme {
		private static final int NOT_FOUND = -1;
		private final String TAU = "tau";
		private final String START_OF_SEQUENCE_PATTERN = "^(\\w+?)(_test)?$";
		private final String END_OF_SEQUENCE_PATTERN = "^(\\w+?)_fire$";
		private final Pattern startPattern = Pattern.compile(START_OF_SEQUENCE_PATTERN);
		private final Pattern endPattern = Pattern.compile(END_OF_SEQUENCE_PATTERN);
		private final SequenceInfo seqInfo = SequenceInfo.END;

		public TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence) {
			List<TransitionTranslation> transitionTranslations = new ArrayList<TransitionTranslation>();

			int startIndex = 0;
			int endIndex = NOT_FOUND;
			String originalTransitionName = null;
			for (int i = 0; i < firingSequence.size(); i++) {
				String transitionName = firingSequence.get(i);
				if (!isInitializationTransition(transitionName)) {
					Matcher startMatcher = startPattern.matcher(transitionName);
					Matcher endMatcher = endPattern.matcher(transitionName);

					boolean isTau = transitionName.equals(TAU);
					boolean isEndTransition = endMatcher.matches();
					boolean isStartTransition = !isTau && !isEndTransition
							&& startMatcher.matches();
					boolean isDegree2Optimization = isStartTransition
							&& (startMatcher.group(2) == null || startMatcher
									.group(2).isEmpty());

					if (isStartTransition) {
						startIndex = i;
						originalTransitionName = startMatcher.group(1);
					}
					if (isEndTransition || isDegree2Optimization)
						endIndex = i;

					if (endIndex != NOT_FOUND) {
						transitionTranslations.add(new TransitionTranslation(
								startIndex, endIndex, originalTransitionName,
								seqInfo));
						endIndex = NOT_FOUND;
						originalTransitionName = null;
					}
				}
			}

			TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
			transitionTranslations.toArray(array);
			return array;
		}

		private boolean isInitializationTransition(String transitionName) {
			Pattern pattern = Pattern.compile("^c\\d+$");
			Matcher matcher = pattern.matcher(transitionName);
			return matcher.find();
		}

		public boolean isIgnoredTransition(String string) {
			Pattern pattern = Pattern.compile("^tau|\\w+?_test$");
			Matcher matcher = pattern.matcher(string);
			return matcher.find();
		}

		public String tokenClockName() {
			return TOKEN_CLOCK_NAME;
		}

		public boolean isIgnoredPlace(String location) {
			return location.equals(PLOCK) || location.equals(PCAPACITY);
		}

		public boolean isIgnoredAutomata(String automata) {
			return automata.equals(CONTROL_TEMPLATE_NAME);
		}
	}

	
	public boolean supportsModel(TimedArcPetriNet model) {
		return !(model.hasWeights());
	}


	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query) {
		return true;
	}
}
