package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import dk.aau.cs.TCTL.visitors.CombiTranslationQueryVisitor;
import dk.aau.cs.model.NTA.Edge;
import dk.aau.cs.model.NTA.Location;
import dk.aau.cs.model.NTA.NTA;
import dk.aau.cs.model.NTA.StandardUPPAALQuery;
import dk.aau.cs.model.NTA.TimedAutomaton;
import dk.aau.cs.model.NTA.UPPAALQuery;
import dk.aau.cs.model.tapn.Bound;
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
import dk.aau.cs.translations.PairingCombi;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import javax.swing.JRootPane;

public class CombiTranslation implements ModelTranslator<TimedArcPetriNet, TAPNQuery, NTA, UPPAALQuery> {

	private JRootPane myRootPane;

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
	protected static final String PCAPACITY = "_BOTTOMIN_";
	protected static final String PCAPACITY2 = "_BOTTOMOUT_";
	protected static final String INITIALIZE_CHANNEL = "c%1$d%2$s";

	protected static final String CONTROL_TEMPLATE_NAME = "Control";
	protected static final String TOKEN_TEMPLATE_NAME = "Token";
	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	protected static final String LOCK_BOOL = "lock";
	protected static final String CAPIN_TOKENS = "CapacityInTokens";
	protected static final String CAPOUT_TOKENS = "CapacityOutTokens";

	protected static int maxDegDif = 0;
	protected static int maxTimeIn = 0;
	protected static int capInTokens;

	private final Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	protected Hashtable<TimedInputArc, String> inputArcsToCounters = new Hashtable<TimedInputArc, String>();
	protected Hashtable<TimedInhibitorArc, String> inhibitorArcsToCounters = new Hashtable<TimedInhibitorArc, String>();
	protected Hashtable<TransportArc, String> transportArcsToCounters = new Hashtable<TransportArc, String>();
	protected Hashtable<String, Boolean> placeNameToTimed = new Hashtable<String, Boolean>();
	protected Hashtable<String, Boolean> placeNameToUrgent = new Hashtable<String, Boolean>();

	public CombiTranslation(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}

	public Tuple<NTA, UPPAALQuery> translate(TimedArcPetriNet model, TAPNQuery query) throws Exception {
		if(!supportsModel(model))
			throw new UnsupportedModelException("Combi Translation does not support the given model.");

		if(!supportsQuery(model, query)) // has no effect atm since broadcast translation supports all queries
			throw new UnsupportedQueryException("Combi Translation does not support the given query.");

		initPlaceToTimed(model);
		initPlaceToUrgent(model);


		
		extraTokens = query.getExtraTokens();
		NTA nta = transformModel(model);
		UPPAALQuery uppaalQuery = transformQuery(query, model);
		return new Tuple<NTA, UPPAALQuery>(nta, uppaalQuery);
	}

	private NTA transformModel(TimedArcPetriNet model) {
		clearLocationMappings();
		clearArcMappings();
		largestPresetSize = 0;
		initTransitions = 0;
		for(TimedPlace p : model.places()){
			if(placeNameToTimed.get(p.name()) && p.numberOfTokens()>0){
				initTransitions= initTransitions + p.numberOfTokens();
			}
		}
		TimedArcPetriNet conservativeModel = null;
		try {
			TAPNToTimedConservativeTAPNConverter converter = new TAPNToTimedConservativeTAPNConverter();
			conservativeModel = converter.makeConservative(model, placeNameToTimed, largestPresetSize);
		} catch (Exception e) {
			return null;
		}

		NTA nta = new NTA();
		
		maxDegDif=largestTimedDegreeDifference(conservativeModel);
		maxTimeIn=largestTimedDegreeIn(conservativeModel);

		if(!(maxDegDif==0 && initTransitions==0 && maxTimeIn==0)){
			if (useSymmetry || conservativeModel.marking().size() + extraTokens == 0) {
				capInTokens=model.getNumberOfTokensInNet()+extraTokens;
				TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel);
				addInitializationStructure(tokenTemplate, conservativeModel);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME);
				if (useSymmetry)
					tokenTemplate.setParameters("const " + ID_TYPE + " " + ID_TYPE_NAME);
				tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
			} else {
				capInTokens=model.getNumberOfTokensInNet()+extraTokens;
				int j = 0;
				for(TimedPlace p : conservativeModel.places()) {
					for (TimedToken token : conservativeModel.marking().getTokensFor(p)) {
						if(placeNameToTimed.get(p.name())){
							capInTokens = capInTokens - 1;
							clearLocationMappings();
							clearArcMappings();
							TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel);
							tokenTemplate.setInitLocation(getLocationByName(token.place().name()));
							nta.addTimedAutomaton(tokenTemplate);
							tokenTemplate.setName(TOKEN_TEMPLATE_NAME + j);
							j++;
						}
					}
				}

				for (int i = j; i < conservativeModel.marking().size()+extraTokens; i++) {
					clearLocationMappings();
					clearArcMappings();
					TimedAutomaton tokenTemplate = createTokenTemplate(conservativeModel);
					tokenTemplate.setInitLocation(getLocationByName(PCAPACITY));
					nta.addTimedAutomaton(tokenTemplate);
					tokenTemplate.setName(TOKEN_TEMPLATE_NAME + i);
				}
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
		if(maxDegDif==0 && initTransitions==0 && maxTimeIn == 0){
			return "system " + CONTROL_TEMPLATE_NAME + ";";
		}
		else if (useSymmetry || tokensInModel + extraTokens == 0) {
			return "system " + CONTROL_TEMPLATE_NAME + "," + TOKEN_TEMPLATE_NAME + ";";
		} 
		else {
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
		if(maxDegDif==0 && initTransitions==0 && maxTimeIn ==0){
			builder.append("0");
		}else if(maxDegDif==0 && initTransitions==0){
			builder.append(maxTimeIn);
		}else if(maxDegDif==0){
			builder.append(initTransitions);
		}else if(model.marking().size() + extraTokens == 0){
			builder.append("1");
		}else{
			builder.append(model.marking().size() + extraTokens);
		}
		builder.append(";\n");

		if (useSymmetry && !(maxDegDif==0 && initTransitions==0 && maxTimeIn==0)) {
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

		builder.append("urgent broadcast chan __fill_remove_from_trace__;\n");

		for (TimedTransition t : model.transitions()) {

			if(timedDegreeIn(t)==0 && timedDegreeOut(t)==0 && !hasTimedInhibitor(t)){
				if(t.isUrgent()){
					builder.append("urgent ");
				}
					builder.append("broadcast chan ");
					builder.append(t.name());
					builder.append(";\n");
				


			}else if((timedDegreeIn(t)==1 || timedDegreeOut(t)==1) && timedDegreeIn(t)<=1 && timedDegreeOut(t)<=1 && !hasTimedInhibitor(t)){
				if(t.isUrgent()){
					builder.append("urgent ");
					}
					builder.append("broadcast chan ");
					builder.append(t.name());
					builder.append(";\n");
				


			}

			else if((timedDegreeIn(t)==2 || timedDegreeOut(t)==2) && timedDegreeIn(t)<=2 && timedDegreeOut(t)<=2 && !hasTimedInhibitor(t)){
				if(t.isUrgent()){
					builder.append("urgent ");
				}

				builder.append("chan ");
				builder.append(t.name());
				builder.append(";\n");

			}else{

				if(t.isUrgent()){
					builder.append("urgent ");
				}
				builder.append("broadcast chan ");
				builder.append(String.format(TEST_CHANNEL_NAME, t.name(), ""));
				builder.append(',');
				builder.append(String.format(FIRE_CHANNEL_NAME, t.name(), ""));
				builder.append(";\n");
			}


		}
		
		int max = largestPresetSize;
		if(max<maxTimeIn){
			max = maxTimeIn;
		}

		for (int i = 0; i < max ; i++) {
			builder.append("int[0,N] ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}

		for(TimedPlace p : model.places()){
			if(!placeNameToTimed.get(p.name())){
				builder.append("int X_"+p.name());
				builder.append(" = "+p.numberOfTokens());
				builder.append("; \n");
			}
		}

		for(TimedPlace p : model.places()){
			if(placeNameToUrgent.get(p.name())){
				builder.append("int Y_"+p.name());
				builder.append(" = "+p.numberOfTokens());
				builder.append("; \n");
			}
		}



		builder.append("int Max = ");
		builder.append(model.getNumberOfTokensInNet()+extraTokens);
		builder.append("; \n");

		builder.append("int Active = ");
		builder.append(model.getNumberOfTokensInNet());
		builder.append("; \n");

		builder.append("bool ");
		builder.append(LOCK_BOOL);
		builder.append(" = false;\n");

		builder.append("int ");
		builder.append(CAPOUT_TOKENS);
		builder.append(" = 0;\n");

		builder.append("int ");
		builder.append(CAPIN_TOKENS);
		builder.append(" = ");
		builder.append(capInTokens);
		builder.append("; \n");

		return builder.toString();
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

			if((timedDegreeIn(transition)==2 || timedDegreeOut(transition)==2) && timedDegreeIn(transition)<=2 && timedDegreeOut(transition)<=2 && !hasTimedInhibitor(transition)){
				continue;
			}else if((timedDegreeIn(transition)==1 || timedDegreeOut(transition)==1) && timedDegreeIn(transition)<=1 && timedDegreeOut(transition)<=1 && !hasTimedInhibitor(transition)){
				continue;
			}else if(timedDegreeIn(transition)==0 && timedDegreeOut(transition)==0 && !hasTimedInhibitor(transition)){
				Edge fireEdge = new Edge(lock, 
						lock, 
						createControlGuard(transition), 
						transition.name()+"!", 
						createResetExpressionForControl(transition));
				control.addTransition(fireEdge);
				continue;
			}

			String invariant = createInvariantForControl(transition);

			Location tempLoc = new Location("", invariant);
			tempLoc.setCommitted(true);
			control.addLocation(tempLoc);

			Edge testEdge = new Edge(lock, 
					tempLoc, 
					createControlGuard(transition), 
					String.format(TEST_CHANNEL_NAME, transition.name(), "!"), 
					lockUpdateStatement(true));
			control.addTransition(testEdge);



			Edge fireEdge = new Edge(tempLoc, 
					lock,	
					createGuardForControl(transition), 
					String.format(FIRE_CHANNEL_NAME, transition.name(), "!"),
					createResetExpressionForControl(transition));
			control.addTransition(fireEdge);	
		}
	}

	private String createControlGuard(TimedTransition transition) {

		StringBuilder builder = new StringBuilder();

		builder.append("(" + CAPOUT_TOKENS + " == " + maxDegDif + " || " + CAPIN_TOKENS + " == 0)");

		Hashtable<String, Integer> weightTable = new Hashtable<String, Integer>();

		for(TimedInputArc ia : transition.getInputArcs()){
			if(!placeNameToTimed.get(ia.source().name())){
				builder.append(" && ");
				builder.append("X_"+ia.source().name());
				builder.append(" >= ");
				builder.append(ia.getWeight().value());
			}			
			if(placeNameToUrgent.get(ia.source().name())){
				String name = ia.source().name();
				if(weightTable.containsKey(name)){
					int i = weightTable.get(name) + ia.getWeight().value();
					weightTable.remove(name);
					weightTable.put(name, i);
				}
				else{
					weightTable.put(name, ia.getWeight().value());
				}
			}
		}	

		for(String place : weightTable.keySet()){
			builder.append(" && ");
			builder.append("Y_"+ place);
			builder.append(" >= ");
			builder.append(weightTable.get(place));
		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {
			if(!placeNameToTimed.get(inhib.source().name())){
				builder.append(" && ");
				builder.append("X_"+inhib.source().name());
				builder.append(" < ");
				builder.append(inhib.getWeight().value());
			}
			if(placeNameToUrgent.get(inhib.source().name())){
				builder.append(" && ");
				builder.append("Y_"+inhib.source().name());
				builder.append(" < ");
				builder.append(inhib.getWeight().value());
			}
		}

		for(TransportArc trans : transition.getTransportArcsGoingThrough()){
			if(placeNameToUrgent.get(trans.source().name())){
				builder.append(" && ");
				builder.append("Y_"+trans.source().name());
				builder.append(" >= ");
				builder.append(trans.getWeight().value());
			}
		}

		int dif = degreeDifference(transition);
		if(dif>0){
			builder.append(" && ");
			builder.append("Active + "+dif+" <= Max");	
		}

		return builder.toString();
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

			if(placeNameToTimed.get(presetArc.source().name())){
				String counter = inputArcsToCounters.get(presetArc);
				builder.append(counter);
				builder.append(":=0");
				first = false;
			}else{
				builder.append("X_"+presetArc.source().name());
				builder.append(" = X_"+presetArc.source().name());
				builder.append(" - "+presetArc.getWeight().value());
				first = false;
			}
		}
		for(TimedOutputArc oa : transition.getOutputArcs()){
			if(!placeNameToTimed.get(oa.destination().name())){
				if (!first) {
					builder.append(", ");
				}

				builder.append("X_"+oa.destination().name());
				builder.append(" = X_"+oa.destination().name());
				builder.append(" + "+oa.getWeight().value());
				first = false;

			}
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
			if(placeNameToTimed.get(inhib.source().name())){
				if (!first) {
					builder.append(", ");
				}

				String counter = inhibitorArcsToCounters.get(inhib);
				builder.append(counter);
				builder.append(":=0");
				first = false;
			}
		}

		if (!first) {
			builder.append(", ");
		}
		builder.append(lockUpdateStatement(false));

		int dif = degreeDifference(transition);

		if (!first) {
			builder.append(" , ");
		}
		builder.append("Active = Active + "+dif);


		return builder.toString();
	}

	private String createGuardForControl(TimedTransition transition) {

		return createBooleanExpressionForControl(transition, "==", "<", ">=");
	}

	protected String createInvariantForControl(TimedTransition transition) {
		return createBooleanExpressionForControl(transition, ">=", "<", ">=");
	}

	protected String createBooleanExpressionForControl(TimedTransition transition, String comparison, String inhibComparison, String untimedComparison) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for (TimedInputArc presetArc : transition.getInputArcs()) {


			if(placeNameToTimed.get(presetArc.source().name())){
				if (!first) {
					builder.append(" && ");
				}
				String counter = inputArcsToCounters.get(presetArc);
				builder.append(counter);
				builder.append(comparison);
				builder.append(presetArc.getWeight().value());
				first = false;

			}
		}
		for (TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (!first) {
				builder.append(" && ");
			}

			String counter = transportArcsToCounters.get(transArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append(transArc.getWeight().value());
			first = false;

		}

		for (TimedInhibitorArc inhib : transition.getInhibitorArcs()) {


			if(placeNameToTimed.get(inhib.source().name())){
				if (!first) {
					builder.append(" && ");
				}
				String counter = inhibitorArcsToCounters.get(inhib);
				builder.append(counter);
				builder.append(inhibComparison);
				builder.append(inhib.getWeight().value());
				first = false;

			}
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

	private TimedAutomaton createTokenTemplate(TimedArcPetriNet model) {
		TimedAutomaton ta = new TimedAutomaton();

		String declarations = createLocalDeclarations();
		ta.setDeclarations(declarations);
		createTemplateStructure(ta, model);
		if(maxDegDif!=0){
			Edge move = new Edge(getLocationByName(PCAPACITY), getLocationByName(PCAPACITY2),CAPOUT_TOKENS + " < " + maxDegDif, "__fill_remove_from_trace__!", CAPOUT_TOKENS + "++, " + CAPIN_TOKENS + "--");
			ta.addTransition(move);
		}
		return ta;
	}

	protected String createLocalDeclarations() {
		return "clock " + TOKEN_CLOCK_NAME + ";";
	}

	protected void addInitializationStructure(TimedAutomaton ta, TimedArcPetriNet model) {
		int j = 0;
		for(TimedPlace p : model.places()) {
			if(!p.name().equals(PCAPACITY) && placeNameToTimed.get(p.name())){
				for (int i = 0; i < p.numberOfTokens(); i++) {
					Edge initEdge = new Edge(getLocationByName(PCAPACITY), getLocationByName(p.name()), "",	String.format(INITIALIZE_CHANNEL, j, "?"), CAPIN_TOKENS +"--");
					ta.addTransition(initEdge);
					j++;
				}
			}
		}
	}

	private void createTemplateStructure(TimedAutomaton ta,	TimedArcPetriNet model) { 
		ta.setLocations(CreateLocationsFromModel(model));

		if(maxDegDif!=0){
			Location bot = new Location(PCAPACITY2, CAPOUT_TOKENS + " <= " + maxDegDif);
			ta.addLocation(bot);
			addLocationMapping(PCAPACITY2, bot);
		}

		for (TimedTransition t : model.transitions()) {
			int presetSize = t.getInhibitorArcs().size()+t.getNumberOfTransportArcsGoingThrough();

			for(TimedInputArc ia : t.getInputArcs()){
				presetSize = presetSize + ia.getWeight().value();
			}


			if(presetSize == 0)
				continue;
			
			if (presetSize > largestPresetSize) {
				largestPresetSize = presetSize;
			}

			PairingCombi pairing = new PairingCombi(t, placeNameToTimed);

			if(timedDegreeIn(t)==0 && timedDegreeOut(t)==0 && !hasTimedInhibitor(t)){
				continue;
			}else if((timedDegreeIn(t)==1 || timedDegreeOut(t)==1) && timedDegreeIn(t)<=1 && timedDegreeOut(t)<=1 && !hasTimedInhibitor(t)){
				createDeg1Structure(ta, t, pairing);
			}else if((timedDegreeIn(t)==2 || timedDegreeOut(t)==2) && timedDegreeIn(t)<=2 && timedDegreeOut(t)<=2 && !hasTimedInhibitor(t)){
				createDeg2Structure(ta, t, pairing);
			}else{
				createStructureForPairing(ta, t, pairing);
			}


		}
	}

	private void createDeg1Structure(TimedAutomaton ta, TimedTransition t, PairingCombi pairing) {
		if(t.getNumberOfTransportArcsGoingThrough()>0){
			for(TransportArc transArc : t.getTransportArcsGoingThrough()) {
				String chan = t.name()+"!";
				Edge e = new Edge(getLocationByName(transArc.source().name()),
						getLocationByName(transArc.destination().name()),
						createTransitionGuardWithLock(t, transArc, transArc.destination(), true), 
						chan,
						createUpdateDeg2(t, true, true));
				ta.addTransition(e);
			}
		}else{
			for(TimedInputArc inputArc : t.getInputArcs()) {
				if(placeNameToTimed.get(inputArc.source().name())){
					Location source = getLocationByName(inputArc.source().name());
					String chan = t.name()+"!";
					if(inputArc.source().name().equals(PCAPACITY)){
						source = getLocationByName(PCAPACITY2);
					}

					TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
					Edge e = new Edge(source,
							getLocationByName(outputArc.destination().name()),
							createTransitionGuardWithLock(t, inputArc, outputArc, outputArc.destination(), true), 
							chan,
							createUpdateDeg2(t, true, false));
					ta.addTransition(e);
				}
			}
		}		
	}

	private void createDeg2Structure(TimedAutomaton ta, TimedTransition t, PairingCombi pairing) {
		boolean first = true;


		for(TimedInputArc inputArc : t.getInputArcs()) {
			if(placeNameToTimed.get(inputArc.source().name())){
				for(int i = 0 ;i<inputArc.getWeight().value();i++){
					Location source = getLocationByName(inputArc.source().name());

					if(inputArc.source().name().equals(PCAPACITY)){
						source = getLocationByName(PCAPACITY2);
					}

					TimedOutputArc outputArc = pairing.getOutputArcFor(inputArc);
					Edge e = new Edge(source,
							getLocationByName(outputArc.destination().name()),
							createTransitionGuardWithLock(t, inputArc, outputArc, outputArc.destination(), first), 
							t.name() + (first ? "!" : "?"),
							createUpdateDeg2(t, first, false));
					ta.addTransition(e);
					first = false;
				}
			}
		}

		for(TransportArc transArc : t.getTransportArcsGoingThrough()) {
			for(int i = 0 ;i<transArc.getWeight().value();i++){
				Edge e = new Edge(getLocationByName(transArc.source().name()),
						getLocationByName(transArc.destination().name()),
						createTransitionGuardWithLock(t, transArc, transArc.destination(), first), 
						t.name() + (first ? "!" : "?"),
						createUpdateDeg2(t, first, true));

				ta.addTransition(e);
				first = false;
			}
		}




	}

	private String createUpdateDeg2(TimedTransition t, boolean first, boolean transport) {
		StringBuilder builder = new StringBuilder();

		boolean firstUp = true;
		if(!transport){
			builder.append(createResetExpressionForNormalArc());
			firstUp = false;
		}


		if(first){
			for (TimedInputArc presetArc : t.getInputArcs()) {
				if(presetArc.source().name().equals(PCAPACITY)){
					if (!firstUp) {
						builder.append(", ");
					}					
					builder.append(CAPOUT_TOKENS);
					builder.append(" = "+CAPOUT_TOKENS);
					builder.append(" - "+presetArc.getWeight().value());
					firstUp=false;
				}
				if(!placeNameToTimed.get(presetArc.source().name())){
					if (!firstUp) {
						builder.append(", ");
					}
					builder.append("X_"+presetArc.source().name());
					builder.append(" = X_"+presetArc.source().name());
					builder.append(" - "+presetArc.getWeight().value());
					firstUp = false;
				}else if(placeNameToUrgent.get(presetArc.source().name())){
					if(!presetArc.source().name().equals(PCAPACITY2)){
						if (!firstUp) {
							builder.append(", ");
						}
						builder.append("Y_"+presetArc.source().name());
						builder.append(" = Y_"+presetArc.source().name());
						builder.append(" - "+presetArc.getWeight().value());
						firstUp = false;
					}

				}
			}
			for(TimedOutputArc oa : t.getOutputArcs()){
				if(oa.destination().name().equals(PCAPACITY)){
					if (!firstUp) {
						builder.append(", ");
					}

					builder.append(CAPIN_TOKENS);
					builder.append(" = "+CAPIN_TOKENS);
					builder.append(" + "+oa.getWeight().value());
					firstUp=false;
				}

				if(!placeNameToTimed.get(oa.destination().name())){
					if (!firstUp) {
						builder.append(" , ");
					}
					builder.append("X_"+oa.destination().name());
					builder.append(" = X_"+oa.destination().name());
					builder.append(" + "+oa.getWeight().value());
					firstUp = false;
				}else if(placeNameToUrgent.get(oa.destination().name())){
					if(!oa.destination().name().equals(PCAPACITY)){
						if (!firstUp) {
							builder.append(" , ");
						}
						builder.append("Y_"+oa.destination().name());
						builder.append(" = Y_"+oa.destination().name());
						builder.append(" + "+oa.getWeight().value());
						firstUp = false;
					}
				}

			}
			for(TransportArc tarc : t.getTransportArcsGoingThrough()){
				if(placeNameToUrgent.get(tarc.source().name())){
					if (!firstUp) {
						builder.append(", ");
					}
					builder.append("Y_"+tarc.source().name());
					builder.append(" = Y_"+tarc.source().name());
					builder.append(" - "+tarc.getWeight().value());
					firstUp = false;
				}if(placeNameToUrgent.get(tarc.destination().name())){
					if (!firstUp) {
						builder.append(", ");
					}
					builder.append("Y_"+tarc.destination().name());
					builder.append(" = Y_"+tarc.destination().name());
					builder.append(" + "+tarc.getWeight().value());
					firstUp = false;
				}
			}

			int dif = degreeDifference(t);

			if (!firstUp) {
				builder.append(" , ");
			}

			builder.append("Active = Active + "+dif);
		}
		return builder.toString();
	}

	private String createTransitionGuardWithLock(TimedTransition t, TransportArc transArc, TimedPlace destination, boolean first) {
		StringBuilder builder = new StringBuilder();

		if(first){
			builder.append("(" + CAPOUT_TOKENS + " == " + maxDegDif + " || " + CAPIN_TOKENS + " == 0)");
			builder.append(" && "+ LOCK_BOOL + " == 0");
		}

		String interval;
		try {
			TimeInterval newInterval = transArc.interval().intersect(destination.invariant());
			interval = convertGuard(newInterval);
		} catch(Exception e) {
			interval = "false";
		}

		if(!interval.equals("")){
			if(first){
				builder.append(" && ");
			}
			builder.append(interval);			
		}

		if(first){
			for(TimedInputArc IA : t.getInputArcs()){
				if(!placeNameToTimed.get(IA.source().name())){
					builder.append(" && ");
					builder.append("X_"+IA.source().name());
					builder.append(">=");
					builder.append(IA.getWeight().value());
				}
			}
			for(TimedInhibitorArc inhib : t.getInhibitorArcs()){
				if(!placeNameToTimed.get(inhib.source().name())){
					builder.append(" && ");
					builder.append("X_"+inhib.source().name());
					builder.append("<");
					builder.append(inhib.getWeight().value());
				}
			}
			if(t.isUrgent()){

			}
		}

		if(first){
			int dif = degreeDifference(t);
			if(dif>0){
				builder.append(" && ");
				builder.append("Active + "+dif+" <= Max");	
			}
		}
		return builder.toString();
	}

	private String createTransitionGuardWithLock(TimedTransition t, TimedInputArc inputArc, TimedOutputArc outputArc, TimedPlace destination, boolean first) {
		StringBuilder builder = new StringBuilder();



		if(first){
			builder.append("(" + CAPOUT_TOKENS + " == " + maxDegDif + " || " + CAPIN_TOKENS + " == 0)");
			builder.append(" && "+ LOCK_BOOL + " == 0");
		}

		String interval = convertGuard(inputArc.interval());
		if(!interval.equals("")){
			if(first){
				builder.append(" && ");
			}
			builder.append(interval);			
		}

		if(first){
			for(TimedInputArc IA : t.getInputArcs()){
				if(!placeNameToTimed.get(IA.source().name())){
					builder.append(" && ");
					builder.append("X_"+IA.source().name());
					builder.append(">=");
					builder.append(IA.getWeight().value());
				}
			}
			for(TimedInhibitorArc inhib : t.getInhibitorArcs()){
				if(!placeNameToTimed.get(inhib.source().name())){
					builder.append(" && ");
					builder.append("X_"+inhib.source().name());
					builder.append("<");
					builder.append(inhib.getWeight().value());
				}
			}
		}

		if(first){
			int dif = degreeDifference(t);
			if(dif>0){
				builder.append(" && ");
				builder.append("Active + "+dif+" <= Max");	
			}
		}
		return builder.toString();
	}

	protected void createStructureForPairing(TimedAutomaton ta,	TimedTransition t, PairingCombi pairing) { 
		int i = 0;

		for(TimedInputArc inputArc : t.getInputArcs()) {
			//	for(int j = 0 ; j <inputArc.getWeight().value();j++){
			if(placeNameToTimed.get(inputArc.source().name())){
				String inputPlaceName = inputArc.source().name();
				String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.name(), i);

				if(inputPlaceName.equals(PCAPACITY)){
					locationName = String.format(TOKEN_INTERMEDIATE_PLACE, PCAPACITY2, t.name(), i);
				}

				Location intermediate = new Location(locationName, "");
				intermediate.setCommitted(true);
				ta.addLocation(intermediate);
				addLocationMapping(locationName, intermediate);

				String counter = String.format(COUNTER_NAME, i);
				inputArcsToCounters.put(inputArc, counter);

				createTestFireStructure(ta, t, pairing, inputArc.source(), 
						convertGuard(inputArc.interval()), 
						pairing.getOutputArcFor(inputArc).destination(), 
						intermediate, counter,false, inputArc.getWeight().value());

				i++;
			}
			//}
		}

		for(TransportArc transArc : t.getTransportArcsGoingThrough()) {

			String inputPlaceName = transArc.source().name();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.name(), i);

			if(inputPlaceName.equals(PCAPACITY)){
				locationName = String.format(TOKEN_INTERMEDIATE_PLACE, PCAPACITY2, t.name(), i);
			}

			Location intermediate = new Location(locationName, "");
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			String counter = String.format(COUNTER_NAME, i);
			transportArcsToCounters.put(transArc, counter);

			String guard = "";
			if(t.isUrgent() && !transArc.destination().invariant().asIterval().toString().equals("[0,inf)")){
				JOptionPane.showMessageDialog(myRootPane, "There is an invariant on a destination for a transport arc going through an urgent transition. The translation will not consider this. To get the true result remove the invariant or the urgency.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				try {
					TimeInterval newInterval = transArc.interval().intersect(transArc.destination().invariant());
					guard = convertGuard(newInterval);
				} catch(Exception e) {
					guard = "false";
				}
			}
			int weight = transArc.getWeight().value();
			createTestFireStructure(ta, t, pairing, transArc.source(), 
					guard, 
					transArc.destination(), 
					intermediate, counter, true, weight);

			i++;

		}

		createStructureForInhibitorArcs(ta, t, i);
	}

	private void createTestFireStructure(TimedAutomaton ta, TimedTransition t, PairingCombi pairing, TimedPlace inputPlace, String testTransitionGuard, TimedPlace OutputPlace,  Location intermediate, String counter, boolean isTransportArc, int weight) {
		String update = String.format(COUNTER_UPDATE, counter, "++");
		Location source = getLocationByName(inputPlace.name());

		if(inputPlace.name().equals(PCAPACITY)){
			update = String.format(COUNTER_UPDATE, counter, "++") + ", " + CAPOUT_TOKENS + "--";
			source = getLocationByName(PCAPACITY2);
		}
		if(placeNameToUrgent.get(inputPlace.name())){
			update = update + ", Y_" + inputPlace.name() + "--";
		}
		Edge testEdge = new Edge(source,
				intermediate, testTransitionGuard, 
				String.format(TEST_CHANNEL_NAME, t.name(), "?"),
				update);
		ta.addTransition(testEdge);

		update = isTransportArc ? "" : createResetExpressionForNormalArc();

		if(OutputPlace.name().equals(PCAPACITY)){
			update = isTransportArc ? "" : createResetExpressionForNormalArc() + ", " + CAPIN_TOKENS + "++";
		}	
		if(placeNameToUrgent.get(OutputPlace.name())){
			if(isTransportArc){
				update = update + "Y_" + OutputPlace.name() + "++";
			}
			else{
				update = update + ", Y_" + OutputPlace.name() + "++";
			}
		}
		Edge fireEdge = new Edge(intermediate, getLocationByName(OutputPlace.name()),
				"",
				String.format(FIRE_CHANNEL_NAME, t.name(), "?"),
				update);
		ta.addTransition(fireEdge);


		String guard = String.format(COUNTER_UPDATE, counter, ">"+weight);
		update = String.format(COUNTER_UPDATE, counter, "--");
		source = getLocationByName(inputPlace.name());

		if(inputPlace.name().equals(PCAPACITY)){
			update = String.format(COUNTER_UPDATE, counter, "--") + ", " + CAPOUT_TOKENS + "++";
			source = getLocationByName(PCAPACITY2);
		}		
		if(placeNameToUrgent.get(inputPlace.name())){
			update = update + ", Y_" + inputPlace.name() + "++";
		}
		Edge backEdge = new Edge(intermediate, 
				source, 
				guard, 
				"", 
				update);
		ta.addTransition(backEdge);


	}

	protected void createStructureForInhibitorArcs(TimedAutomaton ta, TimedTransition t, int i) {
		for (TimedInhibitorArc inhibArc : t.getInhibitorArcs()) {
			if(placeNameToTimed.get(inhibArc.source().name())){
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
	}

	private String createResetExpressionForNormalArc() {
		return String.format("%1s := 0", TOKEN_CLOCK_NAME);
	}

	private ArrayList<Location> CreateLocationsFromModel(TimedArcPetriNet model) {
		clearLocationMappings();

		ArrayList<Location> locations = new ArrayList<Location>();
		for (TimedPlace p : model.places()) {
			if(placeNameToTimed.get(p.name())){
				Location l = new Location(p.name(), convertInvariant(p));

				locations.add(l);
				addLocationMapping(p.name(), l);
			}
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
		CombiTranslationQueryVisitor visitor = new CombiTranslationQueryVisitor(useSymmetry, model.marking().size() + tapnQuery.getExtraTokens(), model,  placeNameToTimed, maxDegDif, initTransitions, maxTimeIn);

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

	private void initPlaceToTimed(TimedArcPetriNet model){
		for(TimedPlace p : model.places()){
			placeNameToTimed.put(p.name(), false);
			if(!p.invariant().equals(TimeInvariant.LESS_THAN_INFINITY)){
				placeNameToTimed.put(p.name(), true);
			}
		}
		for(TimedTransition t : model.transitions()){
			for(TransportArc tt :t.getTransportArcsGoingThrough()){
				placeNameToTimed.put(tt.destination().name(), true);
				placeNameToTimed.put(tt.source().name(), true);
			}
			for(TimedInputArc ia : t.getInputArcs()){
				if(!ia.interval().equals(TimeInterval.ZERO_INF)){
					placeNameToTimed.put(ia.source().name(), true);
				}
			}
		}
		placeNameToTimed.put(PCAPACITY, true);
	}
	private void initPlaceToUrgent(TimedArcPetriNet model){
		for(TimedPlace p : model.places()){
			placeNameToUrgent.put(p.name(), false);
		}
		placeNameToUrgent.put(PCAPACITY, false);
		for(TimedTransition t : model.transitions()){
			if(t.isUrgent()){
				for (TimedInputArc ia : t.getInputArcs()){
					if(placeNameToTimed.get(ia.source().name())){
						placeNameToUrgent.put(ia.source().name(),true);
					}
				}
				for (TransportArc trans : t.getTransportArcsGoingThrough()){
					if(placeNameToTimed.get(trans.source().name())){
						placeNameToUrgent.put(trans.source().name(),true);
					}
				}
				for (TimedInhibitorArc inhib : t.getInhibitorArcs()){
					if(placeNameToTimed.get(inhib.source().name())){
						placeNameToUrgent.put(inhib.source().name(),true);
					}
				}
			}
		}
	}


	public boolean supportsModel(TimedArcPetriNet model) {
		return true;
	}


	public boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query) {
		return true;
	}

	public int degreeDifference(TimedTransition t){
		int inDeg = 0;
		int outDeg = 0;
		int dif;
		for (TimedInputArc presetArc : t.getInputArcs()) {
			if(!presetArc.source().name().equals(PCAPACITY)){
				inDeg = inDeg + presetArc.getWeight().value();
			}
		}
		for (TimedOutputArc postsetArc : t.getOutputArcs()) {
			if(!postsetArc.destination().name().equals(PCAPACITY)){
				outDeg = outDeg + postsetArc.getWeight().value();
			}
		}
		dif = outDeg-inDeg;
		return dif;
	}

	public int timedDegreeDifference(TimedTransition t){
		int inDeg = 0;
		int outDeg = 0;
		int dif;
		for (TimedInputArc presetArc : t.getInputArcs()) {
			if(placeNameToTimed.get(presetArc.source().name())){
				if(!presetArc.source().name().equals(PCAPACITY)){
					inDeg = inDeg + presetArc.getWeight().value();
				}
			}
		}
		for (TimedOutputArc postsetArc : t.getOutputArcs()) {
			if(placeNameToTimed.get(postsetArc.destination().name())){
				if(!postsetArc.destination().name().equals(PCAPACITY)){
					outDeg = outDeg + postsetArc.getWeight().value();
				}
			}
		}
		dif = outDeg-inDeg;
		return dif;
	}

	public int timedDegreeIn(TimedTransition t){
		int inDeg = 0;
		for (TimedInputArc presetArc : t.getInputArcs()) {
			if(placeNameToTimed.get(presetArc.source().name())){
				if(!presetArc.source().name().equals(PCAPACITY)){
					inDeg = inDeg + presetArc.getWeight().value();
				}
			}
		}		
		for(TransportArc tarc : t.getTransportArcsGoingThrough()){
			inDeg = inDeg + tarc.getWeight().value();
		}
		return inDeg;
	}

	public int timedDegreeOut(TimedTransition t){
		int outDeg = 0;		
		for (TimedOutputArc postsetArc : t.getOutputArcs()) {
			if(placeNameToTimed.get(postsetArc.destination().name())){
				if(!postsetArc.destination().name().equals(PCAPACITY)){
					outDeg = outDeg + postsetArc.getWeight().value();
				}
			}
		}
		for(TransportArc tarc : t.getTransportArcsGoingThrough()){
			outDeg = outDeg + tarc.getWeight().value();
		}
		return outDeg;
	}

	public int largestTimedDegreeDifference(TimedArcPetriNet model){
		int maxDeg = 0;
		for (TimedTransition transition : model.transitions()) {
			int deg=timedDegreeDifference(transition);
			if (deg>maxDeg){
				maxDeg=deg;
			}
		}
		return maxDeg;
	}
	
	public int largestTimedDegreeIn(TimedArcPetriNet model){
		int maxDeg = 0;
		for (TimedTransition transition : model.transitions()) {
			int deg= timedDegreeIn(transition);
			if (deg>maxDeg){
				maxDeg=deg;
			}
		}
		
		return maxDeg;
	}

	public boolean hasTimedInhibitor(TimedTransition t){
		if(t.hasInhibitorArcs()){
			for(TimedInhibitorArc IA: t.getInhibitorArcs()){
				if(placeNameToTimed.get(IA.source().name())){
					return true;
				}
			}
		}
		return false;
	}
}