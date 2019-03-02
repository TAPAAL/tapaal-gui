package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import pipe.gui.MessengerImpl;
import pipe.gui.widgets.PlaceEditorPanel;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.event.ConstantChangedEvent;
import dk.aau.cs.model.tapn.event.ConstantEvent;
import dk.aau.cs.model.tapn.event.ConstantsListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.StringComparator;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;

public class TimedArcPetriNetNetwork {
	private List<TimedArcPetriNet> tapns = new ArrayList<TimedArcPetriNet>();
	private List<SharedPlace> sharedPlaces = new ArrayList<SharedPlace>();
	private List<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
	
	private NetworkMarking currentMarking;
	private ConstantStore constants;
	
	private int defaultBound = 3;
	
	private List<ConstantsListener> constantsListeners = new ArrayList<ConstantsListener>();
	
	private boolean paintNet = true;
	
	public TimedArcPetriNetNetwork() {
		this(new ConstantStore());
	}
	
	public TimedArcPetriNetNetwork(ConstantStore constants){
		this.constants = constants;
		currentMarking = new NetworkMarking();
		buildConstraints();
	}
	
	public void addConstantsListener(ConstantsListener listener){
		constantsListeners.add(listener);
	}
	
	public void removeConstantsListener(ConstantsListener listener){
		constantsListeners.remove(listener);
	}

	public void add(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");

		tapn.setParentNetwork(this);
		tapns.add(tapn);
		LocalTimedMarking marking = tapn.marking() instanceof LocalTimedMarking ? (LocalTimedMarking)tapn.marking() : new LocalTimedMarking();
		currentMarking.addMarking(tapn, marking);
		tapn.setMarking(currentMarking);
	}
	public void add(SharedTransition sharedTransition){
		add(sharedTransition, false);
	}

	
	public void add(SharedTransition sharedTransition, boolean multiAdd){
		Require.that(sharedTransition != null, "sharedTransition must not be null");
		if(!multiAdd) {
			Require.that(!isNameUsed(sharedTransition.name()), "There is already a transition or place with that name");
		}
		
		sharedTransition.setNetwork(this);
		if(!(sharedTransitions.contains(sharedTransition)))
			sharedTransitions.add(sharedTransition);
	}
	
	public void add(SharedPlace sharedPlace) {
		add(sharedPlace, false);
	}
	
	public void add(SharedPlace sharedPlace, boolean multiremove) {
		Require.that(sharedPlace != null, "sharedPlace must not be null");
		if(multiremove == false) {
			Require.that(!isNameUsed(sharedPlace.name()), "There is already a transition or place with that name");
		}
		sharedPlace.setNetwork(this);
		sharedPlace.setCurrentMarking(currentMarking);
		if(!(sharedPlaces.contains(sharedPlace)))
			sharedPlaces.add(sharedPlace);
	}

	public boolean isNameUsedForShared(String name){
		for(SharedTransition transition : sharedTransitions){
			if(transition.name().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isNameUsedInTemplates(String name){
		for(TimedArcPetriNet net : tapns){
			if(net.isNameUsed(name)) return true;
		}
		return false;
	}
	
	public boolean isNameUsedForPlacesOnly(String name) {
		for(TimedArcPetriNet net : tapns){
			for(TimedTransition transition : net.transitions()) {
				if(name.equalsIgnoreCase(transition.name()))
					return false;
			}
		}
		return true;
	}
	public boolean isNameUsedForTransitionsOnly(String name) {
		for(TimedArcPetriNet net : tapns){
			for(TimedPlace place : net.places()) {
				if(name.equalsIgnoreCase(place.name()))
					return false;
			}
		}
		return true;
	}
		
	public boolean isNameUsed(String name) {
		return isNameUsedForShared(name) || isNameUsedInTemplates(name);
	}

	public void remove(TimedArcPetriNet tapn) {
		if (tapn != null) {
			tapn.setParentNetwork(null);
			tapns.remove(tapn);
			currentMarking.removeMarkingFor(tapn);
		}
	}
	
	public void remove(SharedPlace sharedPlace) {
		if (sharedPlace != null) {
			sharedPlace.setNetwork(null);
			sharedPlaces.remove(sharedPlace);
		}
	}
	
	public void remove(SharedTransition sharedTransition) {
		if (sharedTransition != null) {
			sharedTransition.setNetwork(null);
			sharedTransitions.remove(sharedTransition);
			sharedTransition.delete();
		}
	}
	
	public List<TimedArcPetriNet> activeTemplates() {
		List<TimedArcPetriNet> activeTemplates = new ArrayList<TimedArcPetriNet>();
		for(TimedArcPetriNet t : tapns) {
			if(t.isActive())
				activeTemplates.add(t);
		}
		
		return activeTemplates;
	}

	public List<TimedArcPetriNet> allTemplates() {
		return tapns;
	}

	public boolean hasTAPNCalled(String newName) {
		for (TimedArcPetriNet tapn : tapns)
			if (tapn.name().equalsIgnoreCase(newName))
				return true;
		return false;
	}

	public NetworkMarking marking() {
		return currentMarking;
	}

	public void setMarking(NetworkMarking marking) {
		currentMarking = marking;
		for (TimedArcPetriNet tapn : tapns) {
			tapn.setMarking(currentMarking);
		}
	}

	public boolean isConstantNameUsed(String newName) {
		return constants.containsConstantByName(newName);
	}

	public void buildConstraints() {
		constants.buildConstraints(this);
	}

	// TODO: Command is a GUI concern. This should not know anything about it
	public Command addConstant(String name, int val) {
		Command cmd = constants.addConstant(name, val); 
		Constant constant = constants.getConstantByName(name);
		fireConstantAdded(constant);
		return cmd;
	}

	private void fireConstantAdded(Constant constant) {
		for(ConstantsListener listener : constantsListeners){
			listener.constantAdded(new ConstantEvent(constant, constants.getIndexOf(constant)));
		}
	}


	public Command removeConstant(String name) {
		Constant constant = constants.getConstantByName(name);
		int index = constants.getIndexOf(constant);
		Command cmd = constants.removeConstant(name);
		for(ConstantsListener listener : constantsListeners){
			listener.constantRemoved(new ConstantEvent(constant, index));
		}
		return cmd;
	}

	public Command updateConstant(String oldName, Constant constant) {
		Constant old = constants.getConstantByName(oldName);
		int index = constants.getIndexOf(old);
		Command edit = constants.updateConstant(oldName, constant, this);

		if (edit != null) {
			updateGuardsAndWeightsWithNewConstant(oldName, constant);
			for(ConstantsListener listener : constantsListeners){
				listener.constantChanged(new ConstantChangedEvent(old, constant, index));
			}
		}

		return edit;
	}

	public void updateGuardsAndWeightsWithNewConstant(String oldName, Constant newConstant) {
		for (TimedArcPetriNet tapn : allTemplates()) {
			for (TimedPlace place : tapn.places()) {
				updatePlaceInvariant(oldName, newConstant, place);
			}

			for (TimedInputArc inputArc : tapn.inputArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, inputArc.interval(), inputArc.getWeight());
			}

			for (TransportArc transArc : tapn.transportArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, transArc.interval(), transArc.getWeight());
			}

			for (TimedInhibitorArc inhibArc : tapn.inhibitorArcs()) {
				updateTimeIntervalAndWeight(oldName, newConstant, inhibArc.interval(), inhibArc.getWeight());
			}
			
			for (TimedOutputArc outputArc : tapn.outputArcs()) {
				updateWeight(oldName, newConstant, outputArc.getWeight());
			}
		}

	}

	private void updatePlaceInvariant(String oldName, Constant newConstant, TimedPlace place) {
		updateBound(oldName, newConstant, place.invariant().upperBound());
	}

	private void updateTimeIntervalAndWeight(String oldName, Constant newConstant, TimeInterval interval, Weight weight) {
		updateBound(oldName, newConstant, interval.lowerBound());
		updateBound(oldName, newConstant, interval.upperBound());
		updateWeight(oldName, newConstant, weight);
	}

	private void updateBound(String oldName, Constant newConstant, Bound bound) {
		if (bound instanceof ConstantBound) {
			ConstantBound cb = (ConstantBound) bound;

			if (cb.name().equals(oldName)) {
				cb.setConstant(newConstant);
			}
		}
	}
	
	private void updateWeight(String oldName, Constant newConstant, Weight weight) {
		if(weight instanceof ConstantWeight){
			ConstantWeight cw = (ConstantWeight) weight;
			
			if(cw.constant().name().equals(oldName)){
				cw.setConstant(newConstant);
			}
		}
		
	}

	public Collection<Constant> constants() {
		return constants.getConstants();
	}

	public Set<String> getConstantNames() {
		return constants.getConstantNames();
	}

	public int getConstantValue(String name) {
		return constants.getConstantByName(name).value();
	}

	public int getLargestConstantValue() {
		return constants.getLargestConstantValue();
	}

	public void setConstants(Iterable<Constant> constants) {
		for (Constant c : constants) {
			this.constants.add(c);
			fireConstantAdded(c);			
		}
	}

	public Constant getConstant(String constantName) {
		return constants.getConstantByName(constantName);
	}
	
	public Constant getConstant(int index){
		return constants.getConstantByIndex(index);
	}

	public TimedArcPetriNet getTAPNByName(String name) {
		for (TimedArcPetriNet tapn : tapns) {
			if (tapn.name().equals(name))
				return tapn;
		}
		return null;
	}

	
	
	public int numberOfSharedPlaces() {
		return sharedPlaces.size();
	}
	
	public int numberOfSharedTransitions() {
		return sharedTransitions.size();
	}

	public SharedPlace getSharedPlaceByIndex(int index) {
		return sharedPlaces.get(index);
	}

	public Object getSharedTransitionByIndex(int index) {
		return sharedTransitions.get(index);
	}

	public Collection<SharedTransition> sharedTransitions() {
		return sharedTransitions;
	}

	public Collection<SharedPlace> sharedPlaces() {
		return sharedPlaces;
	}

	public SharedTransition getSharedTransitionByName(String name) {
		for(SharedTransition t : sharedTransitions){
			if(t.name().equalsIgnoreCase(name)) return t;
		}
		return null;
	}

	public TimedPlace getSharedPlaceByName(String name) {
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) return place;
		}
		return null;
	}

	public boolean hasInhibitorArcs() {
		for(TimedArcPetriNet tapn : tapns) {
			if(tapn.isActive() && tapn.hasInhibitorArcs())
				return true;
		}
		return false;
	}

	public void swapTemplates(int currentIndex, int newIndex) {
		TimedArcPetriNet temp = tapns.get(currentIndex);
		tapns.set(currentIndex, tapns.get(newIndex));
		tapns.set(newIndex, temp);
	}
	
	public TimedArcPetriNet[] sortTemplates() {
		TimedArcPetriNet[] oldOrder = tapns.toArray(new TimedArcPetriNet[0]);
		Collections.sort(tapns, new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(TimedArcPetriNet[] tapns) {
		this.tapns.clear();
		for(TimedArcPetriNet t: tapns){
			this.tapns.add(t);
		}
	}

	public void swapConstants(int currentIndex, int newIndex) {
		constants.swapConstants(currentIndex, newIndex);
	}
	
	public Constant[] sortConstants() {
		return constants.sortConstants();
	}
	
	public void undoSort(Constant[] oldOrder) {
		constants.undoSort(oldOrder);
	}

	public void swapSharedPlaces(int currentIndex, int newIndex) {
		SharedPlace temp = sharedPlaces.get(currentIndex);
		sharedPlaces.set(currentIndex, sharedPlaces.get(newIndex));
		sharedPlaces.set(newIndex, temp);
	}
	
	public SharedPlace[] sortSharedPlaces() {
		SharedPlace[] oldOrder = sharedPlaces.toArray(new SharedPlace[0]);
		Collections.sort(sharedPlaces, new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedPlace[] oldOrder) {
		sharedPlaces.clear();
		for(SharedPlace p : oldOrder){
			sharedPlaces.add(p);
		}
	}

	public void swapSharedTransitions(int currentIndex, int newIndex) {
		SharedTransition temp = sharedTransitions.get(currentIndex);
		sharedTransitions.set(currentIndex, sharedTransitions.get(newIndex));
		sharedTransitions.set(newIndex, temp);
	}
	
	public SharedTransition[] sortSharedTransitions() {
		SharedTransition[] oldOrder = sharedTransitions.toArray(new SharedTransition[0]); 
		Collections.sort(sharedTransitions, new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedTransition[] oldOrder) {
		sharedTransitions.clear();
		for(SharedTransition p : oldOrder){
			sharedTransitions.add(p);
		}
	}	
	
	public boolean isUntimed(){
		for(TimedArcPetriNet t : tapns){
			if(!t.isUntimed()){
				return false;
			}
		}
		return true;
	}

	public boolean hasWeights() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && t.hasWeights()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasUrgentTransitions() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && t.hasUrgentTransitions()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasInvariants() {
		for(TimedArcPetriNet t : tapns){
			if(t.isActive()){
				for(TimedPlace p : t.places()){
					if(!p.invariant().upperBound().equals(Bound.Infinity)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isNonStrict(){
		for(TimedArcPetriNet t : tapns){
			if(t.isActive() && !t.isNonStrict()){
				return false;
			}
		}
		return true;
	}
	
	public boolean isDegree2(){
		ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
		Tuple<TimedArcPetriNet,NameMapping> composedModel = composer.transformModel(this);

		return composedModel.value1().isDegree2();
	}

	public boolean isSharedPlaceUsedInTemplates(SharedPlace place) {
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			for(TimedPlace timedPlace : tapn.places()){
				if(timedPlace.equals(place)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Finds the biggest constant in the active part of the network
	 * @return The biggest constant in the active part of the network or -1 if there are no constants in the net
	 */
	public int biggestConstantInActiveNet(){
		int biggestConstant = -1;
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			int tmp = tapn.getBiggestConstant();
			if(tmp > biggestConstant){
				biggestConstant = tmp;
			}
		}
		return biggestConstant;
	}
	
	/**
	 * Finds the biggest constant which is associated with an enabled transition in the active part of the network
	 * @return The biggest constant which is associated with an enabled transition in the active part of the net or -1 if there are no such constants 
	 */
	public int biggestContantInActiveNetEnabledTransitions(){
		int biggestConstant = -1;
		for(TimedArcPetriNet tapn : this.activeTemplates()){
			int tmp = tapn.getBiggestConstantEnabledTransitions();
			if(tmp > biggestConstant){
				biggestConstant = tmp;
			}
		}
		return biggestConstant;
	}
	
	public TimedArcPetriNetNetwork copy(){
		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
		
		for(SharedPlace p : sharedPlaces){
			network.add(new SharedPlace(p.name(), p.invariant().copy()));
            
			/* Copy markings for shared places */
			for(TimedToken token : currentMarking.getTokensFor(p)){
				network.currentMarking.add(token.clone());
			}
		}
		
		for(SharedTransition t : sharedTransitions){
			network.add(new SharedTransition(t.name()));	// TODO This is okay for now
		}
		
		for(Constant c : constants()){
			network.addConstant(c.name(), c.value());
		}
		
		for(TimedArcPetriNet t : tapns){
			TimedArcPetriNet new_t = t.copy();
			network.add(new_t);
			for(TimedTransition trans : new_t.transitions()){
				if(trans.isShared()){
					network.getSharedTransitionByName(trans.name()).makeShared(trans);
				}
			}
		}
		
		network.setDefaultBound(getDefaultBound());
		
		return network;
	}

	public int getDefaultBound() {
		return defaultBound;
	}
	
	public void setDefaultBound(int defaultBound) {
		this.defaultBound = defaultBound;
	}

	public boolean paintNet() {
		return paintNet;
	}
	
	public void setPaintNet(boolean paintNet){
		this.paintNet = paintNet;
	}

}
