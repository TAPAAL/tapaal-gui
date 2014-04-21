package dk.aau.cs.verification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

public class TAPNComposerExtended implements ITAPNComposer {
	private static final String PLACE_FORMAT = "P%1$d";
	private static final String TRANSITION_FORMAT = "T%1$d";

	private Messenger messenger;
	private boolean hasShownMessage = false;
	
	private int nextPlaceIndex;
	private int nextTransitionIndex;

	private HashSet<String> processedSharedObjects;
	private HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private DataLayer composedGuiModel;

	public TAPNComposerExtended(Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels){
		this.messenger = messenger;
		
		HashMap<TimedArcPetriNet, DataLayer> newGuiModels = new HashMap<TimedArcPetriNet, DataLayer>();
		for(Entry<TimedArcPetriNet, DataLayer> entry : guiModels.entrySet()) {
			newGuiModels.put(entry.getKey(), entry.getValue().copy(entry.getKey()));
		}
		
		this.guiModels = newGuiModels;
	}
	
	public Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model) {
		nextPlaceIndex = -1;
		nextTransitionIndex = -1;
		processedSharedObjects = new HashSet<String>();
		TimedArcPetriNet tapn = new TimedArcPetriNet("ComposedModel");
		DataLayer guiModel = new DataLayer();
		NameMapping mapping = new NameMapping();
		hasShownMessage = false;

		
		double greatestWidth = 0,
			   greatestHeight = 0;
		for (TimedArcPetriNet tapn1 : model.activeTemplates()) {
			greatestWidth = greatestWidth >= getRightmostObject(guiModels.get(tapn1)).getPositionX() ? greatestWidth : getRightmostObject(guiModels.get(tapn1)).getPositionX();
			greatestHeight = greatestHeight >= getLowestObject(guiModels.get(tapn1)).getPositionY() ? greatestHeight : getLowestObject(guiModels.get(tapn1)).getPositionY();
		}
		
		createSharedPlaces(model, tapn, mapping, guiModel);
		createPlaces(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		createTransitions(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		createInputArcs(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		createOutputArcs(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		createTransportArcs(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		createInhibitorArcs(model, tapn, mapping, guiModel, greatestWidth, greatestHeight);
		
		// Set composed guiModel in the instance variable
		this.composedGuiModel = guiModel;

		//dumpToConsole(tapn, mapping);

		return new Tuple<TimedArcPetriNet, NameMapping>(tapn, mapping);
	}
	
	public DataLayer getGuiModel() {
		return this.composedGuiModel;
	}
	
	private Tuple<Integer, Integer> calculateComponentPosition(int netNumber) {
		Integer x = netNumber % 2;
		Integer y = (int) Math.floor(netNumber / 2d);
		
		return new Tuple<Integer, Integer>(x, y);		
	}
	
	private PlaceTransitionObject getRightmostObject(DataLayer guiModel) {
		PlaceTransitionObject returnObject = null;
		
		for (PlaceTransitionObject currentObject : guiModel.getPlaces()) {
			if (returnObject == null || returnObject.getPositionX() < currentObject.getPositionX()) {
				returnObject = currentObject;
			}
		}
		
		for (PlaceTransitionObject currentObject : guiModel.getTransitions()) {
			if (returnObject == null || returnObject.getPositionX() < currentObject.getPositionX()) {
				returnObject = currentObject;
			}
		}
		
		return returnObject;
	}

	private PlaceTransitionObject getLowestObject(DataLayer guiModel) {
		PlaceTransitionObject returnObject = null;
		
		for (PlaceTransitionObject currentObject : guiModel.getPlaces()) {
			if (returnObject == null || returnObject.getPositionY() < currentObject.getPositionY()) {
				returnObject = currentObject;
			}
		}
		
		for (PlaceTransitionObject currentObject : guiModel.getTransitions()) {
			if (returnObject == null || returnObject.getPositionY() < currentObject.getPositionY()) {
				returnObject = currentObject;
			}
		}
		
		return returnObject;
	}
	
	@SuppressWarnings("unused")
	private void dumpToConsole(TimedArcPetriNet tapn, NameMapping mapping) {
		System.out.println("Composed Model:");
		System.out.println("PLACES:");
		for(TimedPlace place : tapn.places()){
			System.out.print('\t');
			System.out.print(place.name());
			System.out.print(", invariant: ");
			System.out.print(place.invariant().toString());
			System.out.print(" (Original: ");
			System.out.print(mapping.map(place.name()));
			System.out.println(')');
		}

		System.out.println();
		System.out.println("TRANSITIONS:");
		for(TimedTransition transition : tapn.transitions()){
			System.out.print('\t');
			System.out.print(transition.name());
			System.out.print(" (Original: ");
			System.out.print(mapping.map(transition.name()).toString());
			System.out.println(')');
		}

		System.out.println();
		System.out.println("INPUT ARCS:");
		for(TimedInputArc arc : tapn.inputArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("OUTPUT ARCS:");
		for(TimedOutputArc arc : tapn.outputArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.println(arc.destination().name());
		}

		System.out.println();
		System.out.println("TRANSPORT ARCS:");
		for(TransportArc arc : tapn.transportArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Transition: ");
			System.out.print(arc.transition().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("INHIBITOR ARCS:");
		for(TimedInhibitorArc arc : tapn.inhibitorArcs()){
			System.out.print("\tSource: ");
			System.out.print(arc.source().name());
			System.out.print(", Target: ");
			System.out.print(arc.destination().name());
			System.out.print(", Interval: ");
			System.out.println(arc.interval().toString());
		}

		System.out.println();
		System.out.println("MARKING:");
		for(TimedPlace place : tapn.places()){
			for(TimedToken token : place.tokens()){
				System.out.print(token.toString());
				System.out.print(", ");
			}
		}
		System.out.println();
		System.out.println();
	}
	
	private Place getSharedPlace(String name) {
		for(Entry<TimedArcPetriNet, DataLayer> hashmapEntry : guiModels.entrySet()) {
			Place findPlace = hashmapEntry.getValue().getPlaceByName(name);
			if (findPlace != null)
				return findPlace;
		}
		return null;
	}
	
	private void createSharedPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel) {
		for(SharedPlace place : model.sharedPlaces()){
			String uniquePlaceName = getUniquePlaceName();
			
			LocalTimedPlace constructedPlace = null;
			if (place.invariant().upperBound() instanceof Bound.InfBound) {					
				constructedPlace = new LocalTimedPlace(uniquePlaceName, place.invariant());
			} else {
				constructedPlace = new LocalTimedPlace(uniquePlaceName, new TimeInvariant(place.invariant().isUpperNonstrict(), new IntBound(place.invariant().upperBound().value())));
			}
			constructedModel.add(constructedPlace);
			mapping.addMappingForShared(place.name(), uniquePlaceName);

			if(model.isSharedPlaceUsedInTemplates(place)){
				for (TimedToken token : place.tokens()) {
					constructedPlace.addToken(new TimedToken(constructedPlace, token.age()));
				}
			}
			
			Place oldPlace = getSharedPlace(place.name());
			TimedPlaceComponent newPlace = new TimedPlaceComponent(
					oldPlace.getPositionX(),
					oldPlace.getPositionY(),
					oldPlace.getId(),
					uniquePlaceName,
					oldPlace.getNameOffsetX(),
					oldPlace.getNameOffsetY(),
					0,
					oldPlace.getMarkingOffsetXObject().doubleValue(),
					oldPlace.getMarkingOffsetYObject().doubleValue(),
					0
					);
			newPlace.setGuiModel(guiModel);
			newPlace.setUnderlyingPlace(constructedPlace);
			newPlace.setName(uniquePlaceName);
			guiModel.addPlace(newPlace);
		}
	}

	private void createPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			
			for (TimedPlace timedPlace : tapn.places()) {			
				if(!timedPlace.isShared()){
					String uniquePlaceName = (timedPlace instanceof LocalTimedPlace) ? ((LocalTimedPlace)timedPlace).model().name() + "_" + timedPlace.name() : "Shared_" + timedPlace.name();

					LocalTimedPlace place = null;
					if (timedPlace.invariant().upperBound() instanceof Bound.InfBound) {					
						place = new LocalTimedPlace(uniquePlaceName, timedPlace.invariant());
					} else {
						place = new LocalTimedPlace(uniquePlaceName, new TimeInvariant(timedPlace.invariant().isUpperNonstrict(), new IntBound(timedPlace.invariant().upperBound().value())));
					}
					constructedModel.add(place);
					mapping.addMapping(tapn.name(), timedPlace.name(), uniquePlaceName);

					for (TimedToken token : timedPlace.tokens()) {
						place.addToken(new TimedToken(place, token.age()));
					}
					
					Place oldPlace = currentGuiModel.getPlaceByName(timedPlace.name());
					TimedPlaceComponent newPlace = new TimedPlaceComponent(
							oldPlace.getPositionX() + offset.value1() * greatestWidth,
							oldPlace.getPositionY() + offset.value2() * greatestHeight,
							oldPlace.getId(),
							uniquePlaceName,
							oldPlace.getNameOffsetX(),
							oldPlace.getNameOffsetY(),
							0,
							oldPlace.getMarkingOffsetXObject().doubleValue(),
							oldPlace.getMarkingOffsetYObject().doubleValue(),
							0
							);
					newPlace.setGuiModel(guiModel);
					newPlace.setUnderlyingPlace(place);
					newPlace.setName(uniquePlaceName);
					guiModel.addPlace(newPlace);
				}
			}
			i++;
		}
	}

	private String getUniquePlaceName() {
		nextPlaceIndex++;
		return String.format(PLACE_FORMAT, nextPlaceIndex);
	}

	private void createTransitions(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			
			for (TimedTransition timedTransition : tapn.transitions()) {
				if(!processedSharedObjects.contains(timedTransition.name())){
					
					// CAUTION: This if statement removes orphan transitions.
					//   This changes answers for e.g. DEADLOCK queries if
					//   support for such queries are added later.
					// ONLY THE IF SENTENCE SHOULD BE REMOVED. REST OF CODE SHOULD BE LEFT INTACT
					if(!timedTransition.isOrphan()){
						String uniqueTransitionName = ( ! timedTransition.isShared()) ? timedTransition.model().name() + "_" + timedTransition.name() : "Shared_" + timedTransition.name();
						
						TimedTransition transition = new TimedTransition(uniqueTransitionName, timedTransition.isUrgent());
						constructedModel.add(transition);
						Transition oldTransition = currentGuiModel.getTransitionByName(timedTransition.name());
						TimedTransitionComponent newTransition = new TimedTransitionComponent(
								oldTransition.getPositionX() + offset.value1() * greatestWidth,
								oldTransition.getPositionY() + offset.value2() * greatestHeight,
								oldTransition.getId(),
								uniqueTransitionName,
								oldTransition.getNameOffsetX(),
								oldTransition.getNameOffsetY(),
								true,
								false,
								oldTransition.getAngle(),
								0);
						newTransition.setGuiModel(guiModel);
						newTransition.setUnderlyingTransition(transition);
						newTransition.setName(uniqueTransitionName);
						guiModel.addTransition(newTransition);
						
						if(timedTransition.isShared()){
							String name = timedTransition.sharedTransition().name();
							processedSharedObjects.add(name);
							mapping.addMappingForShared(name, uniqueTransitionName);
						}else{
							mapping.addMapping(tapn.name(), timedTransition.name(), uniqueTransitionName);
						}
					}else{
						if(!hasShownMessage){
							messenger.displayInfoMessage("There are orphan transitions (no incoming and no outgoing arcs) in the model."
									+ System.getProperty("line.separator") + "They will be removed before the verification.");
							hasShownMessage = true;
						}
					}
				}
			}
			i++;
		}
	}

	private String getUniqueTransitionName() {
		nextTransitionIndex++;
		return String.format(TRANSITION_FORMAT, nextTransitionIndex);
	}
	
	private ArcPath createArcPath(DataLayer currentGuiModel, PlaceTransitionObject source, PlaceTransitionObject target, Arc arc, double offsetX, double offsetY) {
		Arc guiArc = currentGuiModel.getArcByEndpoints(source, target);
		ArcPath arcPath = guiArc.getArcPath();
		int arcPathPointsNum = arcPath.getNumPoints();
		
		// Build ArcPath
		ArcPath newArcPath = new ArcPath(arc);
		for(int k = 0; k < arcPathPointsNum; k++) {
			ArcPathPoint point = arcPath.getArcPathPoint(k);
			newArcPath.addPoint(
					point.getPoint().x + offsetX,
					point.getPoint().y + offsetY,
					point.getPointType()
					);
		}
		
		return newArcPath;
	}

	private void createInputArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedInputArc arc : tapn.inputArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				
				String targetTemplate = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(targetTemplate, arc.destination().name()));

				TimeInterval newInterval = new TimeInterval(arc.interval());
				newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
				if (newInterval.upperBound() instanceof Bound.InfBound) {
					newInterval.setUpperBound(newInterval.upperBound());
				} else {
					newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
				}
				TimedInputArc addedArc = new TimedInputArc(source, target, newInterval, arc.getWeight());
				constructedModel.add(addedArc);
				
				Place guiSource = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				Transition guiTarget = guiModel.getTransitionByName(mapping.map(targetTemplate, arc.destination().name()));
				
				Arc newArc = new TimedInputArcComponent(new TimedOutputArcComponent(
						0d,
						0d,
						0d,
						0d,
						guiSource,
						guiTarget,
						arc.getWeight().value(),
						mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(targetTemplate, arc.destination().name()),
						false
						));
				
				// Build ArcPath
				Place oldGuiSource = currentGuiModel.getPlaceByName(arc.source().name());
				Transition oldGuiTarget = currentGuiModel.getTransitionByName(arc.destination().name());
				ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
				
				// Set arcPath, guiModel and connectors
				((TimedInputArcComponent) newArc).setUnderlyingArc(addedArc);
				newArc.setArcPath(newArcPath);
				newArc.setGuiModel(guiModel);
				newArc.updateArcPosition();
				guiModel.addPetriNetObject(newArc);
				guiSource.addConnectFrom(newArc);
				guiTarget.addConnectTo(newArc);
			}
			i++;
		}
	}

	private void createOutputArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedOutputArc arc : tapn.outputArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				TimedTransition source = constructedModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name()));

				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));

				TimedOutputArc addedArc = new TimedOutputArc(source, target, arc.getWeight());
				constructedModel.add(addedArc);
				
				Transition guiSource = guiModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name()));
				Place guiTarget = guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
				
				TimedOutputArcComponent newArc = new TimedOutputArcComponent(
						0d,
						0d,
						0d,
						0d,
						guiModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name())),
						guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name())),
						arc.getWeight().value(),
						mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name()),
						false
						);
				
				// Build ArcPath
				Transition oldGuiSource = currentGuiModel.getTransitionByName(arc.source().name());
				Place oldGuiTarget = currentGuiModel.getPlaceByName(arc.destination().name());
				ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
				
				// Set arcPath, guiModel and connectors
				newArc.setUnderlyingArc(addedArc);
				newArc.setArcPath(newArcPath);
				newArc.setGuiModel(guiModel);
				newArc.updateArcPosition();
				guiModel.addArc(newArc);
				guiSource.addConnectTo(newArc);
				guiTarget.addConnectFrom(newArc);
			}
			i++;
		}
	}

	private void createTransportArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		int nextGroupNr = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TransportArc arc : tapn.transportArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				String transitionTemplate = arc.transition().isShared() ? "" : tapn.name();
				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				TimedTransition transition = constructedModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
				TimedPlace destination = constructedModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
				
				TimeInterval newInterval = new TimeInterval(arc.interval());
				newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
				if (newInterval.upperBound() instanceof Bound.InfBound) {
					newInterval.setUpperBound(newInterval.upperBound());
				} else {
					newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
				}
				TransportArc addedArc = new TransportArc(source, transition, destination, newInterval, arc.getWeight());
				constructedModel.add(addedArc);
				
				//Create input transport arc
				Place guiSourceIn = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				Transition guiTargetIn = guiModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
								
				TimedTransportArcComponent newInArc = new TimedTransportArcComponent(
						new TimedInputArcComponent(new TimedOutputArcComponent(
							0d,
							0d,
							0d,
							0d,
							guiSourceIn,
							guiTargetIn,
							arc.getWeight().value(),
							mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(transitionTemplate, arc.transition().name()),
							false
							)), 
						nextGroupNr, 
						true
						);
				
				// Build ArcPath
				Place oldGuiSourceIn = currentGuiModel.getPlaceByName(arc.source().name());
				Transition oldGuiTargetIn = currentGuiModel.getTransitionByName(arc.transition().name());
				ArcPath newArcPathIn = createArcPath(currentGuiModel, oldGuiSourceIn, oldGuiTargetIn, newInArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
							
				newInArc.setUnderlyingArc(addedArc);
				newInArc.setArcPath(newArcPathIn);
				newInArc.setGuiModel(guiModel);
				newInArc.updateArcPosition();
				guiModel.addArc(newInArc);
				
				guiSourceIn.addConnectTo(newInArc);
				guiTargetIn.addConnectFrom(newInArc);
				
				// Calculate the next group number for this transport arc
				// By looking at the target of the newInArc -> a transition
				// Then finding the largest existing group number of outgoing transport arcs from this transition
				for (Object pt : newInArc.getTarget().getPostset()) {
					if (pt instanceof TimedTransportArcComponent) {
						if (((TimedTransportArcComponent) pt).getGroupNr() > nextGroupNr) {
							nextGroupNr = ((TimedTransportArcComponent) pt).getGroupNr();
						}
					}
				}

				((TimedTransportArcComponent) newInArc).setGroupNr(nextGroupNr + 1);
				
				//Create output transport arc
				Transition guiSourceOut = guiModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
				Place guiTargetOut = guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
				
				TimedTransportArcComponent newOutArc = new TimedTransportArcComponent(
						new TimedInputArcComponent(new TimedOutputArcComponent(
							0d,
							0d,
							0d,
							0d,
							guiSourceOut,
							guiTargetOut,
							1,
							mapping.map(transitionTemplate, arc.transition().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name()),
							false
							)), 
						nextGroupNr + 1, 
						false
						);
				
				// Build ArcPath
				Transition oldGuiSourceOut = currentGuiModel.getTransitionByName(arc.transition().name());
				Place oldGuiTargetOut = currentGuiModel.getPlaceByName(arc.destination().name());
				ArcPath newArcPathOut = createArcPath(currentGuiModel, oldGuiSourceOut, oldGuiTargetOut, newOutArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
			
				newOutArc.setUnderlyingArc(addedArc);
				newOutArc.setArcPath(newArcPathOut);
				newOutArc.setGuiModel(guiModel);
				newOutArc.updateArcPosition();
				guiModel.addArc(newOutArc);
				
				// Add connection references to the two transport arcs
				newInArc.setConnectedTo(newOutArc);
				newOutArc.setConnectedTo(newInArc);
				
				guiSourceOut.addConnectTo(newOutArc);
				guiTargetOut.addConnectFrom(newOutArc);
			}
			i++;
		}
	}

	
	
	private void createInhibitorArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, double greatestWidth, double greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = this.guiModels.get(tapn);
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedInhibitorArc arc : tapn.inhibitorArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(destinationTemplate, arc.destination().name()));

				TimeInterval newInterval = new TimeInterval(arc.interval());
				newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
				if (newInterval.upperBound() instanceof Bound.InfBound) {
					newInterval.setUpperBound(newInterval.upperBound());
				} else {
					newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
				}
				TimedInhibitorArc addedArc = new TimedInhibitorArc(source, target, newInterval, arc.getWeight());
				constructedModel.add(addedArc);
				
				Place guiSource = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				Transition guiTarget = guiModel.getTransitionByName(mapping.map(destinationTemplate, arc.destination().name()));
				Arc newArc = new TimedInhibitorArcComponent(new TimedOutputArcComponent(
						0d,
						0d,
						0d,
						0d,
						guiSource,
						guiTarget,
						arc.getWeight().value(),
						mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name()),
						false
						), "");
				
				// Build ArcPath
				Place oldGuiSource = currentGuiModel.getPlaceByName(arc.source().name());
				Transition oldGuiTarget = currentGuiModel.getTransitionByName(arc.destination().name());
				ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
				
				((TimedInhibitorArcComponent) newArc).setUnderlyingArc(addedArc);
				newArc.setArcPath(newArcPath);
				newArc.setGuiModel(guiModel);
				newArc.updateArcPosition();
				guiModel.addPetriNetObject(newArc);
				guiSource.addConnectTo(newArc);
				guiTarget.addConnectFrom(newArc);
			}
			i++;
		}
	}
}
