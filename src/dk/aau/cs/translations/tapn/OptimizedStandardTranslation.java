package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.visitors.OptimizedStandardTranslationQueryVisitor;
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
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.Degree2Converter;
import dk.aau.cs.translations.Degree2Pairing;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;

public class OptimizedStandardTranslation implements ModelTranslator<TimedArcPetriNet, TAPNQuery, NTA, UPPAALQuery>{

	protected static final String ID_TYPE = "pid_t";
	protected static final String ID_TYPE_NAME = "pid";
	protected static final String TOKEN_CLOCK_NAME = "x";
	protected static final String PLOCK = "P_lock";
	protected static final String PCAPACITY = "_BOTTOM_";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";
	private static final String DEG2_SUFFIX = "_deg2";
	private static final String DEG1_SUFFIX = "_single";
	protected static final String LOCK_BOOL = "lock";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String LOCK_TEMPLATE_NAME = "Lock";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	
	private int extraTokens;
	private boolean useSymmetry;
	private int numberOfInitChannels;
	
	private List<TimedTransition> retainedTransitions;
	
	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	
	public OptimizedStandardTranslation(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}
	
	public Tuple<NTA, UPPAALQuery> translate(TimedArcPetriNet model, TAPNQuery query) throws Exception {
		if(!supportsModel(model))
			throw new UnsupportedModelException("Optimized Standard Translation does not support the given model.");
		
		if(!supportsQuery(model, query))
			throw new UnsupportedQueryException("Optimized Standard Translation does not support the given query.");
		
		extraTokens = query.getExtraTokens();
		NTA nta = transformModel(model);
		UPPAALQuery uppaalQuery = transformQuery(model, query);
		
		return new Tuple<NTA, UPPAALQuery>(nta, uppaalQuery);
	}
	
	private NTA transformModel(TimedArcPetriNet model) throws Exception {
		// if there are no tokens in the model, add an extra place with a token 
		if(model.marking().size() + extraTokens == 0){
			LocalTimedPlace extraPlace = new LocalTimedPlace("EXTRA234526_3452365"); 
			model.add(extraPlace);
			model.addToken(new TimedToken(extraPlace));
		}
		
		clearLocationMappings();
		numberOfInitChannels = 0;
		
		TimedArcPetriNet degree2Model = null;
		try {
			Degree2Converter converter = new Degree2Converter();
			degree2Model = converter.transformModel(model);
			retainedTransitions = converter.getRetainedTransitions();
		} catch (Exception e) {
			return null;
		}
		
		NTA nta = new NTA();
		if (useSymmetry || degree2Model.marking().size() + extraTokens == 0) {
			TimedAutomaton ta = createTokenAutomaton(degree2Model);
			createInitializationTransitionsForTokenAutomata(degree2Model, ta);
			ta.setName(TOKEN_TEMPLATE_NAME);
			ta.setInitLocation(getLocationByName(PCAPACITY));
			if (useSymmetry)
				ta.setParameters("const " + ID_TYPE + " " + ID_TYPE_NAME);
			nta.addTimedAutomaton(ta);
		} else {
			int j = 0;
			for(TimedPlace p : degree2Model.places()) {
				for (TimedToken token : degree2Model.marking().getTokensFor(p)) {
					if (!token.place().name().equals(PLOCK)) {
						clearLocationMappings();
						TimedAutomaton ta = createTokenAutomaton(degree2Model);
						ta.setName(TOKEN_TEMPLATE_NAME + j);
						ta.setInitLocation(getLocationByName(token.place().name()));
						nta.addTimedAutomaton(ta);
						j++;
					}
				}
			}
			
			for (int i = 0; i < extraTokens; i++) {
				clearLocationMappings();
				TimedAutomaton tokenTemplate = createTokenAutomaton(degree2Model);
				tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME + String.valueOf(degree2Model.marking().size() - 1 + i));
			}
		}
		
		TimedAutomaton lockTA = createTokenAutomaton(degree2Model);
		lockTA.setName(LOCK_TEMPLATE_NAME);
		lockTA.setInitLocation(getLocationByName(PLOCK));
		nta.addTimedAutomaton(lockTA);
		
		if(useSymmetry)
			nta.addTimedAutomaton(createInitializationAutomata(degree2Model));
		
		nta.setSystemDeclarations(createSystemDeclaration(degree2Model.marking().size()));
		nta.setGlobalDeclarations(createGlobalDeclarations(degree2Model));
		
		return nta;
	}

	private String createSystemDeclaration(int tokensInModel) {
		if (useSymmetry || tokensInModel + extraTokens == 1) {
			return "system " + CONTROL_TEMPLATE_NAME + ", " + LOCK_TEMPLATE_NAME + ", "	+ TOKEN_TEMPLATE_NAME + ";";
		} else {
			StringBuilder builder = new StringBuilder("system ");
			builder.append(LOCK_TEMPLATE_NAME);

			for (int i = 0; i < extraTokens + tokensInModel - 1; i++) {
				builder.append(", ");
				builder.append(TOKEN_TEMPLATE_NAME);
				builder.append(i);
			}
			builder.append(';');

			return builder.toString();
		}
	}
	
	
	private String createGlobalDeclarations(TimedArcPetriNet degree2Model) {
		StringBuilder builder = new StringBuilder();

		if (useSymmetry) {
			builder.append("const int N = ");
			builder.append(degree2Model.marking().size() + extraTokens - 1);
			builder.append(";\n");
			builder.append("typedef ");
			builder.append("scalar[N] ");
			builder.append(ID_TYPE);
			builder.append(";\n");

			for (int i = 0; i < numberOfInitChannels; i++) {
				builder.append("chan ");
				builder.append(String.format(INITIALIZE_CHANNEL, i, ""));
				builder.append(";\n");
			}
		}

		for (TimedTransition t : degree2Model.transitions()) {
			if(t.presetSize() == 0) {
				continue;
			}
			else if (isTransitionDegree1(t)) {
				builder.append("broadcast chan ");
				builder.append(t.name() + DEG1_SUFFIX);
				builder.append(";\n");
			}
			else if (retainedTransitions.contains(t)) {
				builder.append("chan ");
				builder.append(t.name() + DEG2_SUFFIX);
				builder.append(";\n");
			} else {
				builder.append("chan ");
				builder.append(t.name());
				builder.append(";\n");
			}
		}
		
		builder.append("bool ");
		builder.append(LOCK_BOOL);
		builder.append("= false;\n");

		return builder.toString();
	}

	

	private TimedAutomaton createInitializationAutomata(TimedArcPetriNet degree2Model) {
		TimedAutomaton control = new TimedAutomaton();
		control.setName(CONTROL_TEMPLATE_NAME);
		Location initial = createInitializationTransitionsForControlAutomaton(degree2Model, control);
		control.setInitLocation(initial);

		return control;
	}

	private Location createInitializationTransitionsForControlAutomaton(TimedArcPetriNet degree2Model, TimedAutomaton control) {
		if (degree2Model.marking().size() == 1){
			Location finish = new Location("finish", "");
			control.addLocation(finish);
			return finish;
		}
		
		Location first = new Location("", "");
		first.setCommitted(true);
		control.addLocation(first);
		Location prev = first;

		for (int i = 0; i < degree2Model.marking().size() - 2; i++) {
			Location l = new Location("", "");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(prev, l, "", String.format(INITIALIZE_CHANNEL, i, "!"),	"");
			control.addTransition(e);
			prev = l;
		}
		
		Location finish = new Location("finish", "");
		control.addLocation(finish);
		Edge e = new Edge(prev, finish, "", String.format(INITIALIZE_CHANNEL, degree2Model.marking().size() - 2, "!"), "");
		control.addTransition(e);
		return first;
	}

	private void createInitializationTransitionsForTokenAutomata(TimedArcPetriNet degree2Model, TimedAutomaton ta) {
		int j = 0;
		for(TimedPlace p : degree2Model.places()) {
			for(int i = 0; i < p.numberOfTokens(); i++) {
				if (!p.name().equals(PLOCK) && !p.name().equals(PCAPACITY)) {
					Edge e = new Edge(getLocationByName(PCAPACITY), getLocationByName(p.name()), "", String.format(INITIALIZE_CHANNEL, j, "?"), "");
					ta.addTransition(e);
					numberOfInitChannels++;
					j++;
				}
			}
		}
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

	
	private TimedAutomaton createTokenAutomaton(TimedArcPetriNet degree2Model) {
		TimedAutomaton tokenTA = new TimedAutomaton();
		createLocationsForTokenAutomata(degree2Model, tokenTA);
		createEdgesForTokenAutomata(degree2Model, tokenTA);
		tokenTA.setDeclarations(createLocalDeclarations());

		return tokenTA;
	}

	private void createLocationsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for (TimedPlace place : degree2Net.places()) {
			Location l = new Location(place.name(), convertInvariant(place));
			ta.addLocation(l);
			addLocationMapping(place.name(), l);
		}
	}
	
	private void createEdgesForTokenAutomata(TimedArcPetriNet degree2Model, TimedAutomaton tokenTA) {
		for(TimedTransition transition : degree2Model.transitions()) {
			if(transition.hasInhibitorArcs())
				throw new RuntimeException("Standard translation does not support inhibitor arcs!");
			
			if(transition.presetSize() == 0)
				continue;
			
			Degree2Pairing pairing = new Degree2Pairing(transition);
			
			if(retainedTransitions.contains(transition)) {
				boolean first = true;
				String suffix = isTransitionDegree1(transition) ? DEG1_SUFFIX : DEG2_SUFFIX;
				
				for(TimedInputArc inputArc : transition.getInputArcs()) {
					TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
					String guard = createTransitionGuardWithLock(inputArc.interval());
					Edge e = new Edge(getLocationByName(inputArc.source().name()), 
							getLocationByName(outputArc.destination().name()), guard, 
							transition.name() + suffix + (first ? "!" : "?"),
							createResetExpressionForNormalArc());
					tokenTA.addTransition(e);
					first = false;
				}

				for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
					String guard = createTransitionGuardWithLock(transArc.interval());
					Edge e = new Edge(getLocationByName(transArc.source().name()),
							getLocationByName(transArc.destination().name()),
							guard, transition.name() + suffix + (first ? "!" : "?"), "");
					
					tokenTA.addTransition(e);
					first = false;
				}
			} else {
				for(TimedInputArc inputArc : transition.getInputArcs()) {
					String sync = transition.name() + (isPartOfLockTemplate(inputArc.source().name()) ? "!" : "?");
					String guard = convertGuard(inputArc.interval());
					Edge e = new Edge(getLocationByName(inputArc.source().name()), 
							getLocationByName(pairing.getOutputArcFor(inputArc).destination().name()), 
							guard, sync, createResetExpressionForNormalArc());
					
					tokenTA.addTransition(e);
				}
				
				for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
					String guard = convertGuard(transArc.interval());
					Edge e = new Edge(getLocationByName(transArc.source().name()), 
							getLocationByName(transArc.destination().name()),
							guard, transition.name() + "?", "");
					tokenTA.addTransition(e);
				}
			}
		}
	}

	private boolean isTransitionDegree1(TimedTransition transition) {
		return transition.presetSize() == 1 && transition.postsetSize() == 1;
	}

	private String createTransitionGuardWithLock(TimeInterval interval) {
		String guard = convertGuard(interval);
		
		if (guard == null || guard.isEmpty()) {
			guard = LOCK_BOOL + " == 0";
		} else {
			guard += " && " + LOCK_BOOL + " == 0";
		}
		
		return guard;
	}
	
	private String convertGuard(TimeInterval interval) {
		if(interval.equals(TimeInterval.ZERO_INF))
			return "";
		
		StringBuilder builder = new StringBuilder();
		boolean lowerBoundAdded = false;
		if(!(interval.lowerBound().value() == 0 && interval.IsLowerBoundNonStrict())) {
			builder.append(TOKEN_CLOCK_NAME);
			if(interval.IsLowerBoundNonStrict())
				builder.append(" >= ");
			else
				builder.append(" > ");
		
			builder.append(interval.lowerBound().value());
			lowerBoundAdded = true;
		}
		
		if(!interval.upperBound().equals(Bound.Infinity)) {
			if(lowerBoundAdded) builder.append(" && ");
			builder.append(TOKEN_CLOCK_NAME);
			
			if(interval.IsUpperBoundNonStrict())
				builder.append(" <= ");
			else
				builder.append(" < ");
			
			builder.append(interval.upperBound().value());
		}
		
		return builder.toString();
	}

	private String createResetExpressionForNormalArc() {
		return String.format("%1s := 0", TOKEN_CLOCK_NAME);
	}
	
	protected String convertInvariant(TimedPlace place) {
		String inv = "";
		TimeInvariant invariant = place.invariant();
		if (!invariant.equals(TimeInvariant.LESS_THAN_INFINITY)) {
			inv = TOKEN_CLOCK_NAME + " " + invariant.toString(false);
		}

		return inv;
	}
	
	protected String createLocalDeclarations() {
		return "clock " + TOKEN_CLOCK_NAME + ";";
	}
	
	private boolean isPartOfLockTemplate(String name) {
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}
	
	private UPPAALQuery transformQuery(TimedArcPetriNet model, TAPNQuery query) throws Exception {
		OptimizedStandardTranslationQueryVisitor visitor = new OptimizedStandardTranslationQueryVisitor(model.marking().size() + query.getExtraTokens(), useSymmetry);
		return  new StandardUPPAALQuery(visitor.getUppaalQueryFor(query));
	}

	public TranslationNamingScheme namingScheme() {
		return new OptimizedStandardNamingScheme();
	}
	
	protected class OptimizedStandardNamingScheme implements TranslationNamingScheme {
		private static final int NOT_FOUND = -1;
		private final String START_OF_SEQUENCE_PATTERN = "^(\\w+?)_(?:1_in|single|deg2)$";
		private Pattern startPattern = Pattern.compile(START_OF_SEQUENCE_PATTERN);
		private Pattern ignoredPlacePattern = Pattern.compile("^P_lock|_BOTTOM_|\\w+_\\d+|\\w+_\\d+_(?:in|out)|P_hp_\\w+_\\d+$");;
		private final SequenceInfo seqInfo = SequenceInfo.WHOLE;

		public TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence) {
			List<TransitionTranslation> transitionTranslations = new ArrayList<TransitionTranslation>();

			int startIndex = NOT_FOUND;
			String originalTransitionName = null;
			for (int i = 0; i < firingSequence.size(); i++) {
				String transitionName = firingSequence.get(i);
				Matcher startMatcher = startPattern.matcher(transitionName);

				boolean isStartTransition = startMatcher.matches();

				if (isStartTransition) {
					if (startIndex != NOT_FOUND) {
						transitionTranslations
								.add(new TransitionTranslation(startIndex, i - 1,
										originalTransitionName, seqInfo));
					}
					startIndex = i;
					originalTransitionName = startMatcher.group(1);
				}
			}

			if (startIndex != NOT_FOUND) {
				transitionTranslations.add(new TransitionTranslation(startIndex, firingSequence.size() - 1, originalTransitionName, seqInfo));
			}
			TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
			transitionTranslations.toArray(array);
			return array;
		}

		public String tokenClockName() {
			return "x";
		}

		public boolean isIgnoredPlace(String location) {
			Matcher matcher = ignoredPlacePattern.matcher(location);
			return matcher.matches();
		}

		public boolean isIgnoredAutomata(String automata) {
			return false;
		}

	}

	
	public boolean supportsModel(TimedArcPetriNet model) {
		return !(model.hasInhibitorArcs()) && !(model.hasWeights());
	}

	
	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query) {
		if(query.getProperty() instanceof TCTLEGNode || query.getProperty() instanceof TCTLAFNode) {
			if(!isModelDegree2(model))
				return false;
		}
		
		return true;
	}

	private boolean isModelDegree2(TimedArcPetriNet model) {
		if(model.hasInhibitorArcs())
			return false;
		
		for(TimedTransition t : model.transitions()) {
			if(t.presetSize() > 2 || t.postsetSize() > 2)
				return false;
		}
		
		return true;
	}
}
