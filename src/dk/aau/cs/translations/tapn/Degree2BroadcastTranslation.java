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
import dk.aau.cs.translations.Degree2Converter;
import dk.aau.cs.translations.Degree2Pairing;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;

public class Degree2BroadcastTranslation implements
		ModelTranslator<TimedArcPetriNet, TAPNQuery, NTA, UPPAALQuery> {

	private static final String DEG2_SUFFIX = "_deg2";
	private static final String DEG1_SUFFIX = "_single";
	private static final String PLOCK = "P_lock";
	protected static final String P_CAPACITY = "_BOTTOM_";
	private static final String TOKEN_TEMPLATE_NAME = "Token";
	private static final String CONTROL_TEMPLATE_NAME = "Control";
	private static final String ID_TYPE = "id_t";
	private static final String ID_PARAMETER_NAME = "id";
	protected static final String CLOCK_NAME = "x";
	private static final String COUNTER_NAME = "count%1$d";
	private static final String COUNTER_UPDATE = "%1$s%2$s";
	private static final String TEST_CHANNEL = "%1$s_test%2$s";
	private static final String INIT_CHANNEL = "c%1$d%2$s";
	protected static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	protected static final String T_MAX_FORMAT = "%1$s_%2$d";
	protected static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;
	protected static final String LOCK_BOOL = "lock";

	private final Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	private final Hashtable<TimedInputArc, String> inputArcsToCounters = new Hashtable<TimedInputArc, String>();
	private final Hashtable<TimedInhibitorArc, String> inhibitorArcsToCounters = new Hashtable<TimedInhibitorArc, String>();
	private final Hashtable<TransportArc, String> transportArcsToCounters = new Hashtable<TransportArc, String>();
	private final Hashtable<String, Hashtable<String, String>> arcGuards = new Hashtable<String, Hashtable<String, String>>();
	
	private List<TimedTransition> retainedTransitions;
	
	private int numberOfInitChannels = 0;
	protected int extraTokens = 0;
	private int largestPresetSize = 0;
	protected boolean useSymmetry;

	public Degree2BroadcastTranslation(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}
	
	public Tuple<NTA, UPPAALQuery> translate(TimedArcPetriNet model, TAPNQuery query) throws Exception {
		if(!supportsModel(model))
			throw new UnsupportedModelException("Degree 2 Broadcast Translation does not support the given model.");
		
		if(!supportsQuery(model, query)) // has no effect atm since the translation supports all queries
			throw new UnsupportedQueryException("Degree 2 Broadcast Translation does not support the given query.");
		
		extraTokens = query.getExtraTokens();
		NTA nta = transformModel(model);
		UPPAALQuery uppaalQuery = transformQuery(query, model);
		
		return new Tuple<NTA, UPPAALQuery>(nta, uppaalQuery);
	}

	protected NTA transformModel(TimedArcPetriNet model) throws Exception {
		 // if there are no tokens in the model, add an extra place with a token
		if(model.marking().size() + extraTokens == 0){
			LocalTimedPlace extraPlace = new LocalTimedPlace("EXTRA3242342_234765"); 
			model.add(extraPlace);
			model.addToken(new TimedToken(extraPlace));
		}
		
		clearArcMappings();
		arcGuards.clear();
		clearLocationMappings();
		numberOfInitChannels = 0;
		largestPresetSize = 0;

		TimedArcPetriNet conservativeModel = null;
		TimedArcPetriNet degree2Model = null;
		try {
			TAPNToConservativeTAPNConverter conservativeConverter = new TAPNToConservativeTAPNConverter();
			conservativeModel = conservativeConverter.makeConservative(model);
			
			Degree2Converter converter = new Degree2Converter();
			degree2Model = converter.transformModel(conservativeModel);
			retainedTransitions = converter.getRetainedTransitions();
		} catch (Exception e) {
			return null;
		}

		NTA nta = new NTA();
		if (useSymmetry || degree2Model.marking().size() + extraTokens == 0) {
			TimedAutomaton ta = createTokenAutomaton(degree2Model, conservativeModel, null);
			createInitializationTransitionsForTokenAutomata(degree2Model, ta);
			ta.setName(TOKEN_TEMPLATE_NAME);
			ta.setInitLocation(getLocationByName(P_CAPACITY));
			if (useSymmetry)
				ta.setParameters("const " + ID_TYPE + " " + ID_PARAMETER_NAME);
			nta.addTimedAutomaton(ta);
		} else {
			int j = 0;
			for(TimedPlace p : degree2Model.places()) {
				for (TimedToken token : degree2Model.marking().getTokensFor(p)) {
					if (!token.place().name().equals(PLOCK)) {
						clearLocationMappings();
						clearArcMappings();
						TimedAutomaton ta = createTokenAutomaton(degree2Model, conservativeModel, token);
						ta.setName(TOKEN_TEMPLATE_NAME + j);
						ta.setInitLocation(getLocationByName(token.place().name()));
						nta.addTimedAutomaton(ta);
						j++;
					}
				}
			}

			TimedPlace bottom = degree2Model.getPlaceByName(P_CAPACITY);
			for (int i = 0; i < extraTokens; i++) {
				clearLocationMappings();
				clearArcMappings();
				TimedAutomaton tokenTemplate = createTokenAutomaton(degree2Model, conservativeModel, new TimedToken(bottom));
				tokenTemplate.setInitLocation(getLocationByName(P_CAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME + (degree2Model.marking().size() - 1 + i));
			}
		}

		nta.addTimedAutomaton(createControlAutomaton(degree2Model, conservativeModel));
		nta.setSystemDeclarations(createSystemDeclaration(degree2Model.marking().size()));
		nta.setGlobalDeclarations(createGlobalDeclarations(degree2Model, conservativeModel));

		return nta;
	}

	private String createSystemDeclaration(int tokensInModel) {
		if (useSymmetry || tokensInModel + extraTokens == 1) {
			return "system " + CONTROL_TEMPLATE_NAME + ","
					+ TOKEN_TEMPLATE_NAME + ";";
		} else {
			StringBuilder builder = new StringBuilder("system ");
			builder.append(CONTROL_TEMPLATE_NAME);

			for (int i = 0; i < extraTokens + tokensInModel - 1; i++) {
				builder.append(", ");
				builder.append(TOKEN_TEMPLATE_NAME);
				builder.append(i);
			}
			builder.append(';');

			return builder.toString();
		}
	}

	protected String createGlobalDeclarations(TimedArcPetriNet degree2Net, TimedArcPetriNet originalModel) {
		StringBuilder builder = new StringBuilder();

		if (useSymmetry) {
			builder.append("const int N = ");
			builder.append(degree2Net.marking().size() + extraTokens - 1);
			builder.append(";\n");
			builder.append("typedef ");
			builder.append("scalar[N] ");
			builder.append(ID_TYPE);
			builder.append(";\n");

			for (int i = 0; i < numberOfInitChannels; i++) {
				builder.append("chan ");
				builder.append(String.format(INIT_CHANNEL, i, ""));
				builder.append(";\n");
			}
		}

		for (TimedTransition t : degree2Net.transitions()) {
			if(t.presetSizeWithoutInhibitorArcs() == 0 && !t.hasInhibitorArcs()) {
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

		for (TimedTransition t : originalModel.transitions()) {
			if((t.presetSizeWithoutInhibitorArcs() == 0 || isTransitionDegree1(t)) && !t.hasInhibitorArcs()) {
				continue;
			}
			else if (!isTransitionDegree2(t) || t.hasInhibitorArcs()) {
				builder.append("broadcast chan ");
				builder.append(String.format(TEST_CHANNEL, t.name(), ""));
				builder.append(";\n");
			}
		}

		for (int i = 0; i < largestPresetSize; i++) {
			builder.append("bool ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}

		builder.append("bool ");
		builder.append(LOCK_BOOL);
		builder.append("= false;\n");

		return builder.toString();
	}

	private boolean isTransitionDegree1(TimedTransition t) {
		return t.presetSizeWithoutInhibitorArcs() == 1 && t.postsetSize() == 1;
	}
	
	private boolean isTransitionDegree2(TimedTransition t) {
		return t.presetSizeWithoutInhibitorArcs() == 2 && t.postsetSize() == 2;
	}

	protected String convertInvariant(TimedPlace place) {
		String inv = "";
		TimeInvariant invariant = place.invariant();
		if (!invariant.equals(TimeInvariant.LESS_THAN_INFINITY)) {
			inv = CLOCK_NAME + " " + invariant.toString(false);
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

	private TimedAutomaton createControlAutomaton(TimedArcPetriNet degree2Net, TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();
		createInitialLocationsForControlAutomaton(degree2Net, control);
		createEdgesForControlAutomaton(degree2Net, model, control);
		control.setName(CONTROL_TEMPLATE_NAME);
		if (useSymmetry) {
			Location initial = createInitializationTransitionsForControlAutomaton(degree2Net, control);
			control.setInitLocation(initial);
		} else {
			control.setInitLocation(getLocationByName(PLOCK));
		}

		return control;
	}

	private Location createInitializationTransitionsForControlAutomaton(TimedArcPetriNet degree2Net, TimedAutomaton control) {
		if (degree2Net.marking().size() == 1)
			return getLocationByName(PLOCK);

		Location first = new Location("", "");
		first.setCommitted(true);
		control.addLocation(first);
		Location prev = first;

		for (int i = 0; i < degree2Net.marking().size() - 2; i++) {
			Location l = new Location("", "");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(prev, l, "", String.format(INIT_CHANNEL, i, "!"),	"");
			control.addTransition(e);
			prev = l;
		}

		Edge e = new Edge(prev, getLocationByName(PLOCK), "", String.format(INIT_CHANNEL, degree2Net.marking().size() - 2, "!"), "");
		control.addTransition(e);
		return first;
	}

	private void createEdgesForControlAutomaton(TimedArcPetriNet degree2Net, TimedArcPetriNet originalModel, TimedAutomaton control) {
		for (TimedTransition transition : degree2Net.transitions()) {
			if (!retainedTransitions.contains(transition)) {
				Degree2Pairing pairing = new Degree2Pairing(transition);
				
				for(TimedInputArc inputArc : transition.getInputArcs()) {
					if (!inputArc.source().name().equals(PLOCK) && isPartOfLockTemplate(inputArc.source().name())) {
						Edge e = new Edge(getLocationByName(inputArc.source().name()), 
								getLocationByName(pairing.getOutputArcFor(inputArc).destination().name()), 
								"", transition.name() + "!", "");
						control.addTransition(e);
					}
				}
			}
		}

		for (TimedTransition transition : originalModel.transitions()) {
			if (!(isTransitionDegree1(transition) || isTransitionDegree2(transition)) || transition.hasInhibitorArcs()) {
				Location ptest = new Location("", createInvariantForControl(transition));
				ptest.setCommitted(true);
				control.addLocation(ptest);

				Edge first = new Edge(getLocationByName(PLOCK), ptest, "", String.format(TEST_CHANNEL, transition.name(), "!"), "");
				control.addTransition(first);

				if (transition.presetSizeWithoutInhibitorArcs() != 1) {
					Edge second = new Edge(ptest, getLocationByName(String.format(P_T_IN_FORMAT, transition.name(), 1)),
							"", String.format(T_I_IN_FORMAT + "%3$s", transition.name(), 1, "!"),
							createResetExpressionForControl(transition));
					control.addTransition(second);
				} else {
					Edge second = new Edge(ptest, getLocationByName(PLOCK), "",
							String.format(T_MAX_FORMAT + "%3$s", transition.name(), 1, "!"),
							createResetExpressionForControl(transition));
					control.addTransition(second);
				}
			}
		}
	}

	private void createInitialLocationsForControlAutomaton(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for (TimedPlace place : degree2Net.places()) {
			if (isPartOfLockTemplate(place.name())) {
				Location l = new Location(place.name(), "");

				if (!place.name().equals(PLOCK)) {
					l.setCommitted(true);
				}

				ta.addLocation(l);
				addLocationMapping(place.name(), l);
			}
		}
	}

	private TimedAutomaton createTokenAutomaton(TimedArcPetriNet degree2Net, TimedArcPetriNet originalModel, TimedToken token) throws Exception {
		TimedAutomaton tokenTA = new TimedAutomaton();
		createInitialLocationsForTokenAutomata(degree2Net, tokenTA);
		createEdgesForTokenAutomata(degree2Net, tokenTA);
		createTestingEdgesForTokenAutomata(originalModel, tokenTA);
		tokenTA.setDeclarations(createLocalDeclarations());

		return tokenTA;
	}

	protected String createLocalDeclarations() {
		return "clock " + CLOCK_NAME + ";";
	}

	private void createInitializationTransitionsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		int j = 0;
		for(TimedPlace p : degree2Net.places()) {
			for (int i = 0; i < p.numberOfTokens(); i++) {
				if (!p.name().equals(PLOCK) && !p.name().equals(P_CAPACITY)) {
					Edge e = new Edge(getLocationByName(P_CAPACITY), getLocationByName(p.name()), "", String.format(INIT_CHANNEL, j, "?"), "");
					ta.addTransition(e);
					numberOfInitChannels++;
					j++;
				}
			}
		}
	}

	private void createEdgesForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton token) {
		for (TimedTransition transition : degree2Net.transitions()) {
			if(transition.presetSizeWithoutInhibitorArcs() == 0 && !transition.hasInhibitorArcs())
				continue;
			
			Degree2Pairing pairing = new Degree2Pairing(transition);
			
			if (retainedTransitions.contains(transition)) {
					boolean first = true;
					String suffix = isTransitionDegree1(transition) ? DEG1_SUFFIX : DEG2_SUFFIX;
					
					for(TimedInputArc inputArc : transition.getInputArcs()) {
						if(isPartOfLockTemplate(inputArc.source().name()))
							continue;
						
						TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
						String guard = createTransitionGuardWithLock(inputArc, outputArc.destination(), false);
						Edge e = new Edge(getLocationByName(inputArc.source().name()), 
								getLocationByName(outputArc.destination().name()), guard, 
								transition.name() + suffix + (first ? "!" : "?"),
								createResetExpressionForNormalArc());
						token.addTransition(e);
						saveGuard(transition.name(), inputArc.source().name(), guard);
						first = false;
					}

					for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
						String guard = createTransitionGuardWithLock(transArc, transArc.destination(), true);
						Edge e = new Edge(getLocationByName(transArc.source().name()),
								getLocationByName(transArc.destination().name()),
								guard, transition.name() + suffix + (first ? "!" : "?"), "");
						
						token.addTransition(e);
						saveGuard(transition.name(), transArc.source().name(), guard);
						first = false;
					}
			} else {
				for(TimedInputArc inputArc : transition.getInputArcs()) {
					if(isPartOfLockTemplate(inputArc.source().name()))
						continue;
					
					String guard = convertGuard(inputArc.interval());
					Edge e = new Edge(getLocationByName(inputArc.source().name()),
							getLocationByName(pairing.getOutputArcFor(inputArc).destination().name()),
							guard, transition.name() + "?",
							createResetExpressionForNormalArc());
					token.addTransition(e);
					saveGuard(transition.name(), inputArc.source().name(), guard);
				
				}
				
				for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
					
					String guard = "";
					try {
						TimeInterval newGuard = transArc.interval().intersect(transArc.destination().invariant());
						guard = convertGuard(newGuard);
					} catch(Exception e) {
						guard = "false";
					}
					Edge e = new Edge(getLocationByName(transArc.source().name()),
							getLocationByName(transArc.destination().name()),
							guard, transition.name() + "?", "");
					
					token.addTransition(e);
					saveGuard(transition.name(), transArc.source().name(), guard);
				}
			}
		}
	}

	

	private void saveGuard(String transitionName, String inputPlaceName, String guard) {
		String originalTransitionName = getOriginalTransitionName(transitionName);
		if (originalTransitionName != null && !originalTransitionName.isEmpty()) {
			if (!arcGuards.containsKey(originalTransitionName)) {
				arcGuards.put(originalTransitionName,
						new Hashtable<String, String>());
			}

			arcGuards.get(originalTransitionName).put(inputPlaceName, guard);
		}
	}

	private String getOriginalTransitionName(String name) {
		Pattern pattern = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)_([0-9]*(?:_in)?)$");

		Matcher matcher = pattern.matcher(name);
		if (!matcher.find()) {
			return null;
		} else {
			return matcher.group(1);
		}
	}

	private void createTestingEdgesForTokenAutomata(TimedArcPetriNet originalModel, TimedAutomaton ta) throws Exception {

		for (TimedTransition transition : originalModel.transitions()) {
			int size = transition.presetSizeWithoutInhibitorArcs() + transition.getInhibitorArcs().size();
			if (size > largestPresetSize)
				largestPresetSize = size;
			
			if(size == 0)
				continue;

			if (!(isTransitionDegree1(transition) || isTransitionDegree2(transition)) || transition.hasInhibitorArcs()) {
				int i = 0;
				for (TimedInputArc inputArc : transition.getInputArcs()) {
					String source = inputArc.source().name();
					String counter = String.format(COUNTER_NAME, i);
					inputArcsToCounters.put(inputArc, counter);

					String guard = arcGuards.get(transition.name()).get(source);
					if (guard == null && source.equals(P_CAPACITY)) {
						guard = "";
					} else if(guard == null)
						throw new Exception("guard was not precomputed");

					Edge e = new Edge(getLocationByName(source),
							getLocationByName(source), guard, String.format(TEST_CHANNEL, transition.name(), "?"),
							String.format(COUNTER_UPDATE, counter, "= true"));
					ta.addTransition(e);
					i++;
				}
				
				for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
					String source = transArc.source().name();
					String counter = String.format(COUNTER_NAME, i);
					transportArcsToCounters.put(transArc, counter);

					String guard = arcGuards.get(transition.name()).get(source);
					if (guard == null && source.equals(P_CAPACITY)) {
						guard = "";
					} else if(guard == null)
						throw new Exception("guard was not precomputed");

					Edge e = new Edge(getLocationByName(source),
							getLocationByName(source), guard, String.format(TEST_CHANNEL, transition.name(), "?"),
							String.format(COUNTER_UPDATE, counter, "= true"));
					ta.addTransition(e);
					i++;
				}

				for (TimedInhibitorArc inhibArc : transition.getInhibitorArcs()) {
					String source = inhibArc.source().name();
					String counter = String.format(COUNTER_NAME, i);
					inhibitorArcsToCounters.put(inhibArc, counter);

					Edge e = new Edge(getLocationByName(source),
							getLocationByName(source), convertGuard(inhibArc.interval()), 
							String.format(TEST_CHANNEL, inhibArc.destination().name(),"?"), 
							String.format(COUNTER_UPDATE,counter, "=true"));
					ta.addTransition(e);
					i++;
				}
			}
		}
	}

	private void createInitialLocationsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for (TimedPlace place : degree2Net.places()) {
			if (!isPartOfLockTemplate(place.name())) {
				Location l = new Location(place.name(), convertInvariant(place));
				ta.addLocation(l);
				addLocationMapping(place.name(), l);
			}
		}
	}

	private String createInvariantForControl(TimedTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==");
	}

	private String createBooleanExpressionForControl(TimedTransition transition, String comparison, String inhibComparison) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for (TimedInputArc presetArc : transition.getInputArcs()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = inputArcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append("true");
			first = false;
		}
		
		for (TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = transportArcsToCounters.get(transArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append("true");
			first = false;
		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = inhibitorArcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(inhibComparison);
			builder.append("false");
		}

		return builder.toString();
	}

	private String createResetExpressionForControl(TimedTransition transition) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for (TimedInputArc presetArc : transition.getInputArcs()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = inputArcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(":=false");
			first = false;
		}
		
		for (TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = transportArcsToCounters.get(transArc);
			builder.append(counter);
			builder.append(":=false");
			first = false;
		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {
			if (!first) {
				builder.append(", ");
			}

			String counter = inhibitorArcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(":=false");
			first = false;
		}

		return builder.toString();
	}

	protected String createTransitionGuardWithLock(TimedInputArc inputArc, TimedPlace targetPlace, boolean isTransportArc) {
		String guard = convertGuard(inputArc.interval());

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
		if(!(interval.lowerBound().value() == 0 && interval.isLowerBoundNonStrict())) {
			builder.append(CLOCK_NAME);
			if(interval.isLowerBoundNonStrict())
				builder.append(" >= ");
			else
				builder.append(" > ");
		
			builder.append(interval.lowerBound().value());
			lowerBoundAdded = true;
		}
		
		if(!interval.upperBound().equals(Bound.Infinity)) {
			if(lowerBoundAdded) builder.append(" && ");
			builder.append(CLOCK_NAME);
			
			if(interval.isUpperBoundNonStrict())
				builder.append(" <= ");
			else
				builder.append(" < ");
			
			builder.append(interval.upperBound().value());
		}
		
		return builder.toString();
	}
	
	private String createTransitionGuardWithLock(TransportArc transArc, TimedPlace targetPlace, boolean isTransportArc) {
		String guard = "";
		try {
			TimeInterval newGuard = transArc.interval().intersect(targetPlace.invariant());
			guard = convertGuard(newGuard);
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

	private String createResetExpressionForNormalArc() {
		return String.format("%1s := 0", CLOCK_NAME);
	}

	private boolean isPartOfLockTemplate(String name) {
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	protected UPPAALQuery transformQuery(TAPNQuery tapnQuery, TimedArcPetriNet model) throws Exception {
		BroadcastTranslationQueryVisitor visitor = new BroadcastTranslationQueryVisitor(
				useSymmetry, model.marking().size() + tapnQuery.getExtraTokens());

		return new StandardUPPAALQuery(visitor.getUppaalQueryFor(tapnQuery));
	}

	public TranslationNamingScheme namingScheme() {
		return new Degree2BroadcastNamingScheme();
	}

	protected static class Degree2BroadcastNamingScheme implements
			TranslationNamingScheme {
		private static final int NOT_FOUND = -1;
		private final String START_OF_SEQUENCE_PATTERN = "^(\\w+)_(?:test|single|deg2)$";
		private final Pattern startPattern = Pattern.compile(START_OF_SEQUENCE_PATTERN);
		private final Pattern testTransitionPattern = Pattern.compile("^(\\w+)_test$");
		private final Pattern ignoredPlacePattern = Pattern.compile("^" + PLOCK + "|" + P_CAPACITY + "|P_hp_\\w+_\\d+$");
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
						TransitionTranslation transitionTranslation = createTransitionTranslation(
								firingSequence.get(startIndex), startIndex,
								i - 1, originalTransitionName);
						transitionTranslations.add(transitionTranslation);
					}
					startIndex = i;
					originalTransitionName = startMatcher.group(1);
				}
			}

			if (startIndex != NOT_FOUND) {
				TransitionTranslation transitionTranslation = createTransitionTranslation(
						firingSequence.get(startIndex), startIndex,
						firingSequence.size() - 1, originalTransitionName);
				transitionTranslations.add(transitionTranslation);
			}
			TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
			transitionTranslations.toArray(array);
			return array;
		}

		private TransitionTranslation createTransitionTranslation(
				String startTransition, int startIndex, int endIndex,
				String originalTransitionName) {
			if (testTransitionPattern.matcher(startTransition).matches()) {
				startIndex += 1; // ignores _test transition
			}
			return new TransitionTranslation(startIndex, endIndex, originalTransitionName, seqInfo);
		}

		public String tokenClockName() {
			return CLOCK_NAME;
		}

		public boolean isIgnoredPlace(String location) {
			Matcher matcher = ignoredPlacePattern.matcher(location);
			return matcher.matches();
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
