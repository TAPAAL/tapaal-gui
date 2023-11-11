package dk.aau.cs.model.tapn;

import java.util.*;

import net.tapaal.gui.petrinet.undo.Colored.*;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.event.ConstantChangedEvent;
import dk.aau.cs.model.tapn.event.ConstantEvent;
import dk.aau.cs.model.tapn.event.ConstantsListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.StringComparator;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import pipe.gui.petrinet.undo.UndoManager;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

public class TimedArcPetriNetNetwork {
	private final List<TimedArcPetriNet> tapns = new ArrayList<TimedArcPetriNet>();
	private final List<SharedPlace> sharedPlaces = new ArrayList<SharedPlace>();
	private final List<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
	
	private NetworkMarking currentMarking = new NetworkMarking();
	private final ConstantStore constants;

    private List<ColorType> colorTypes = new ArrayList<ColorType>();
    private List<Variable> variables = new ArrayList<Variable>();
	private int defaultBound = 3;
	
	private final List<ConstantsListener> constantsListeners = new ArrayList<ConstantsListener>();
	
	private boolean paintNet = true;
	
	public TimedArcPetriNetNetwork() {
		this(new ConstantStore(), List.of(ColorType.COLORTYPE_DOT));
	}
	
	public TimedArcPetriNetNetwork(ConstantStore constants, List<ColorType> colorTypes){
		this.constants = constants;
		this.colorTypes.addAll(colorTypes);
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
		if(!multiremove) {
			Require.that(!isNameUsed(sharedPlace.name()), "There is already a transition or place with that name");
		}
		sharedPlace.setNetwork(this);
		sharedPlace.setCurrentMarking(currentMarking);
		if(!(sharedPlaces.contains(sharedPlace)))
			sharedPlaces.add(sharedPlace);
	}

	public boolean isNameUsedForShared(String name){
		for(SharedTransition transition : sharedTransitions){
			if(transition.name().equals(name)) {
				return true;
			}
		}
		
		for(SharedPlace place : sharedPlaces){
			if(place.name().equals(name)) {
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
				if(name.equals(transition.name()))
					return false;
			}
		}
		return true;
	}
	public boolean isNameUsedForTransitionsOnly(String name) {
		for(TimedArcPetriNet net : tapns){
			for(TimedPlace place : net.places()) {
				if(name.equals(place.name()))
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

    public ConstantStore getConstantStore() {
        return constants;
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
			if(t.name().equals(name)) return t;
		}
		return null;
	}

	public TimedPlace getSharedPlaceByName(String name) {
		for(SharedPlace place : sharedPlaces){
			if(place.name().equals(name)) return place;
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
		tapns.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(TimedArcPetriNet[] tapns) {
		this.tapns.clear();
		this.tapns.addAll(Arrays.asList(tapns));
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
		sharedPlaces.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedPlace[] oldOrder) {
		sharedPlaces.clear();
		sharedPlaces.addAll(Arrays.asList(oldOrder));
	}

	public void swapSharedTransitions(int currentIndex, int newIndex) {
		SharedTransition temp = sharedTransitions.get(currentIndex);
		sharedTransitions.set(currentIndex, sharedTransitions.get(newIndex));
		sharedTransitions.set(newIndex, temp);
	}
	
	public SharedTransition[] sortSharedTransitions() {
		SharedTransition[] oldOrder = sharedTransitions.toArray(new SharedTransition[0]); 
		sharedTransitions.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(SharedTransition[] oldOrder) {
		sharedTransitions.clear();
		sharedTransitions.addAll(Arrays.asList(oldOrder));
	}

	public boolean isColored() {
	    for (TimedArcPetriNet tapn : tapns) {
	        if (tapn.isColored()) {
	            return true;
            }
        }
	    return colorTypes.size() > 1 || variables.size() > 0;
    }

	public boolean isUntimed(){
		for(TimedArcPetriNet t : tapns){
			if(!t.isUntimed()){
				return false;
			}
		}
		return true;
	}

	public boolean isTimed(){
		return !isUntimed();
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

    public boolean hasUncontrollableTransitions() {
        for(TimedArcPetriNet t : tapns){
            if(t.isActive() && t.hasUncontrollableTransitions()){
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

    public int getHighestNetDegree(){
        ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), false);
        Tuple<TimedArcPetriNet,NameMapping> composedModel = composer.transformModel(this);

        return composedModel.value1().getHighestNetDegree();
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
			network.add(new SharedPlace(p.name(), p.invariant().copy(),p.getColorType()));
            
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

	//For colors

    public List<ColorType> colorTypes() { return colorTypes;}
    public void setColorTypes(List<ColorType> cts) { colorTypes = cts;}


    public List<Variable> variables() {return variables;}
    public void setVariables(List<Variable> newVariables) { variables = newVariables;}

    public void renameColorType(ColorType oldColorType, ColorType colorType, ConstantsPane.ColorTypesListModel colorTypesListModel, UndoManager undoManager){
        Integer index = getColorTypeIndex(oldColorType.getName());

        Command command = new UpdateColorTypeCommand(this, oldColorType, colorType, index, colorTypesListModel);
        command.redo();
        undoManager.addEdit(command);
        updateProductTypes(oldColorType, colorType, undoManager);
    }

    public void updateColorType(ColorType oldColorType, ColorType colorType, ConstantsPane.ColorTypesListModel colorTypesListModel, UndoManager undoManager) {
        undoManager.newEdit();
        renameColorType(oldColorType, colorType, colorTypesListModel, undoManager);
    }


    private void updateProductTypes(ColorType oldColorType, ColorType colorType, UndoManager undoManager){
        for (ColorType ct : colorTypes) {
            if (ct instanceof ProductType) {
                Command command = new UpdatePTColorTypeCommand(oldColorType, colorType, (ProductType)ct);
                command.redo();
                undoManager.addEdit(command);
            }
        }
    }

    public Integer getColorTypeIndex(String name) {
        for (int i = 0; i < colorTypes.size(); i++) {
            if (colorTypes.get(i).getName().equals(name)) {
                return i;
            }
        }
        return null;
    }
    public ColorType getColorTypeByName(String name) {
        for (ColorType element : colorTypes) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        return null;
    }

    public Integer getVariableIndex(String name) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getName().equals(name)) {
                return i;
            }
        }
        return null;
    }
	
	public boolean isIndeticalToExisting(ColorType newColorType) {
		for (ColorType ct : colorTypes) {
			if (ct.isIdentical(newColorType)) {
				return true;
			}
		}

		return false;
	}

    public boolean isNameUsedForColorType(String name) {
        for (ColorType element : colorTypes) {
            if (element.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public boolean isNameUsedForVariable(String name) {
        for (Variable element : variables) {
            if (element.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public boolean isNameUsedForColor(String name, ColorType ignored) {
        for (ColorType e : colorTypes) {
            if (e != ignored && !e.isIntegerRange() && !e.isProductColorType()){
                for (Color c : e.getColors()) {
                    if (c.getName().equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isNameUsedForConstant(String newName) {
        return constants.containsConstantByName(newName);
    }

    public Variable getVariableByName(String name){
        for (Variable variable : variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }
    public Color getColorByName(String name){
        for (ColorType element : colorTypes) {
            if(element.getColorByName(name) != null){
                return element.getColorByName(name);
            }
        }
        return null;
    }
    public Variable getVariableByIndex(int index) {
        return variables.get(index);
    }
    public int numberOfColorTypes() {
        return colorTypes.size();
    }

    public ColorType getColorTypeByIndex(int index) {
        return colorTypes.get(index);
    }

    public int numberOfVariables() {
        return variables.size();
    }
    public void add(ColorType colorType) {
        if (colorType.equals(ColorType.COLORTYPE_DOT) && isNameUsedForColorType(ColorType.COLORTYPE_DOT.getName()))
            return;

        Require.that(colorType != null, "colorType must not be null");
        Require.that(!isNameUsedForColorType(colorType.getName()), "There is already a color type with that name"); //TODO:: When using load, a nullpointer exception is thrown here
        colorTypes.add(colorType);
    }

    public void add(Variable variable) {
        Require.that(variable != null, "variable must not be null");
        Require.that(!isNameUsedForVariable(variable.getName()), "There is already a variable with that name");

        variables.add(variable);
    }

    public boolean remove(ColorType colorType, ConstantsPane.ColorTypesListModel colorTypesListModel, UndoManager undoManager, ArrayList<String> messages) {
        Integer index = getColorTypeIndex(colorType.getName());

        if(canColorTypeBeRemoved(colorType, messages)){
            Command command = new RemoveColorTypeFromNetworkCommand(colorType, this, colorTypesListModel, index);
            command.redo();
            undoManager.addEdit(command);
            //Success
            return true;
        }

        return false;
    }

    public boolean canColorTypeBeRemoved(ColorType colorType, ArrayList<String> messages){
	    isColorTypeUsedInProduct(colorType, messages);
        isColorTypeUsedInVaraible(colorType, messages);
	    for(TimedArcPetriNet tapn : allTemplates()){
            for(TimedPlace p : tapn.places()){
                if(p.getColorType().equals(colorType)){
                    messages.add("Color type " + p.getColorType().getName() + " is used in place " + p.name() + " \n");
                }
            }
            for(TimedTransition t : tapn.transitions()){
                for(Color c : colorType.getColors()){
                    if(t.getGuard() != null && t.getGuard().containsColor(c)){
                        messages.add("Colors of color type " + colorType.getName() + " are used in transition " + t.name() + "\n");
                    }
                }
            }
        }
        return messages.isEmpty();
    }

    private void isColorTypeUsedInVaraible(ColorType colorType, ArrayList<String> messages) {
        for (Variable variable : variables) {
            if (variable.getColorType().equals(colorType)) {
                messages.add("Color type " + variable.getColorType().getName() + " is used in variable " + variable.getName() + "\n");
            }
        }
    }

    public boolean canColorBeRemoved(Color color, ArrayList<String> messages) {
        isColorTypeUsedInProduct(color.getColorType(), messages);
        for (TimedArcPetriNet tapn : allTemplates()) {
            for (TimedPlace p : tapn.places()) {
                if (p.getTokensAsExpression() != null && p.getTokensAsExpression().containsColor(color)) {
                    messages.add(color.getName() + " is used in a token in place " + p.name() + " \n");
                }
                for (ColoredTimeInvariant invariant : p.getCtiList()) {
                    if (invariant.getColor().equals(color)) {
                        messages.add(color.getName() + " is used in an invariant in place " + p.name() + " \n");
                    }
                }
            }
            for (TimedTransition t : tapn.transitions()) {
                if (t.getGuard() != null && t.getGuard().containsColor(color)) {
                    messages.add(color.getName() + " of color type is used in transition " + t.name() + "\n");
                }
            }
            for (TransportArc arc : tapn.transportArcs()) {
                if (arc.getInputExpression().containsColor(color)) {
                    messages.add(color.getName() + " is used on transport arc from " + arc.source().name() + " to " + arc.transition() + "\n");
                }
                if (arc.getOutputExpression().containsColor(color)) {
                    messages.add(color.getName() + " is used on transport arc from " + arc.transition()+ " to " + arc.destination().name() + "\n");
                }
            }
            for (TimedInputArc arc : tapn.inputArcs()) {
                if (arc.getArcExpression().containsColor(color)) {
                    messages.add(color.getName() + " is used on arc from " + arc.source().name() + " to " + arc.destination().name() + "\n");
                }
            }
            for (TimedOutputArc arc : tapn.outputArcs()) {
                if (arc.getExpression().containsColor(color)) {
                    messages.add(color.getName() + " is used on arc from " + arc.source().name() + " to " + arc.destination().name() + "\n");
                }
            }
        }
        return messages.isEmpty();
    }

    private void isColorTypeUsedInProduct(ColorType colorType, ArrayList<String> messages) {
        for (ColorType ct : colorTypes) {
            if (ct instanceof ProductType && ((ProductType) ct).contains(colorType)) {
                messages.add("Color type " + colorType.getName() + " is used in product type " + ct.getName() + " \n");
            }
        }
    }

    public void remove(Variable variable , ConstantsPane.VariablesListModel variablesListModel, UndoManager undoManager, List<String> messages) {
	    if (canVariableBeRemoved(variable,messages)) {
            Integer index = getVariableIndex(variable.getName());
            Command command = new RemoveVariableFromNetworkCommand(variable, this, variablesListModel, index);
            command.redo();
            undoManager.addEdit(command);
        }
    }

    public void remove(Variable variable) {
	    if (variable != null) {
	        variables.remove(variable);
        }
    }

    public boolean canVariableBeRemoved(Variable variable, List<String> messages) {
        for (TimedArcPetriNet tapn : allTemplates()) {
            for (TimedInputArc arc : tapn.inputArcs()) {
                Set<Variable> variables = new HashSet<>();
                arc.getArcExpression().getVariables(variables);
                if (variables.contains(variable)) {
                    messages.add("Variable contained on input arc " + arc.fromTo());
                }
            }
            for (TimedOutputArc arc : tapn.outputArcs()) {
                Set<Variable> variables = new HashSet<>();
                arc.getExpression().getVariables(variables);
                if (variables.contains(variable)) {
                    messages.add("Variable contained on output arc " + arc);
                }
            }
            for (TransportArc arc : tapn.transportArcs()) {
                Set<Variable> variables = new HashSet<>();
                arc.getInputExpression().getVariables(variables);
                arc.getOutputExpression().getVariables(variables);

                if (variables.contains(variable)) {
                    messages.add("Variable contained on transport arc " + arc.fromTo());
                }
            }
        }
        return messages.isEmpty();
    }

    public ColorType[] sortColorTypes() {
        ColorType[] oldorder = colorTypes.toArray(new ColorType[0]);
        colorTypes.sort(new StringComparator());
        return oldorder;
    }
    public void undoSort(ColorType[] oldorder) {
        colorTypes.clear();
        colorTypes.addAll(Arrays.asList(oldorder));
    }
    public void swapColorTypes(int currentIndex, int newIndex) {
        ColorType temp = colorTypes.get(currentIndex);
        colorTypes.set(currentIndex, colorTypes.get(newIndex));
        colorTypes.set(newIndex, temp);
    }

    public void swapVariables(int currentIndex, int newIndex) {
        Variable temp = variables.get(currentIndex);
        variables.set(currentIndex, variables.get(newIndex));
        variables.set(newIndex, temp);
    }

    public Variable[] sortVariables() {
        Variable[] oldOrder = variables.toArray(new Variable[0]);
        variables.sort(new StringComparator());
        return oldOrder;
    }

    public void undoSort(Variable[] oldOrder) {
        variables.clear();
        variables.addAll(Arrays.asList(oldOrder));
    }

    public ExpressionContext getContext(){
	    HashMap<String, ColorType> hashMap = new HashMap<>();
	    for(ColorType colorType : colorTypes){
	        hashMap.put(colorType.getName(), colorType);
        }
	    return new ExpressionContext(new HashMap<String, Color>(), hashMap);
    }


}
