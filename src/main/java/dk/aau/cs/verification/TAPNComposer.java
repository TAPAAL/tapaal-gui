package dk.aau.cs.verification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import net.tapaal.gui.petrinet.TAPNLens;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.dialog.ExportBatchDialog;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
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

public class TAPNComposer implements ITAPNComposer {
	private final Messenger messenger;
	private boolean hasShownMessage = false;
	private boolean singleComponentNoPrefix = false; // if set to true then nets with only a single component have no prefix before places/transitions 
    private boolean inlineConstants = true;

	private HashSet<String> processedSharedObjects;
	private HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private DataLayer composedGuiModel;
    private final TAPNLens lens;

    public TAPNComposer(Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels, TAPNLens lens, boolean singleComponentNoPrefix, boolean inlineConstants){
		this.messenger = messenger;

		if(guiModels != null){
            HashMap<TimedArcPetriNet, DataLayer> newGuiModels = new HashMap<TimedArcPetriNet, DataLayer>();
            for(Entry<TimedArcPetriNet, DataLayer> entry : guiModels.entrySet()) {
                newGuiModels.put(entry.getKey(), entry.getValue().copy(entry.getKey()));
            }

            this.guiModels = newGuiModels;
        }
		this.singleComponentNoPrefix = singleComponentNoPrefix;
		this.inlineConstants = inlineConstants;
		this.lens = lens;
	}
	
	public TAPNComposer(Messenger messenger, boolean singleComponentNoPrefix) {
		this.messenger = messenger;
		this.singleComponentNoPrefix = singleComponentNoPrefix;
		this.lens = TAPNLens.Default;
	}
	
	public Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model) {
		processedSharedObjects = new HashSet<String>();
		TimedArcPetriNet tapn = new TimedArcPetriNet("ComposedModel");
		DataLayer guiModel = new DataLayer();
		NameMapping mapping = new NameMapping();
		hasShownMessage = false;
        tapn.setParentNetwork(model);
		
		int greatestWidth = 0, greatestHeight = 0;
		if (this.guiModels != null) {
			for (TimedArcPetriNet tapn1 : model.activeTemplates()) {
			    DataLayer dl = this.guiModels.get(tapn1);
                if (dl == null || isComponentEmpty(dl)) {
                    continue;
                }
				greatestWidth = Math.max(greatestWidth, getRightmostObject(guiModels.get(tapn1)).getPositionX());
				greatestHeight = Math.max(greatestHeight, getLowestObject(guiModels.get(tapn1)).getPositionY());
			}
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

	private boolean isComponentEmpty(DataLayer guiModel) {
		return guiModel == null || guiModel.getPlaces().length == 0 && guiModel.getTransitions().length == 0;
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
			if(!model.isSharedPlaceUsedInTemplates(place))	continue;
			
			String uniquePlaceName = composedPlaceName(place);
			
			LocalTimedPlace constructedPlace = null;
			if (place.invariant().upperBound() instanceof Bound.InfBound) {
				constructedPlace = new LocalTimedPlace(uniquePlaceName, place.invariant(), place.getColorType());
			} else {
				constructedPlace = new LocalTimedPlace(uniquePlaceName, new TimeInvariant(place.invariant().isUpperNonstrict(), new IntBound(place.invariant().upperBound().value())), place.getColorType());
			}
			constructedPlace.setCtiList(place.getCtiList());
			constructedPlace.setTokenExpression(place.getTokensAsExpression());
			
			constructedModel.add(constructedPlace);
			mapping.addMappingForShared(place.name(), uniquePlaceName);
			
			for (TimedToken token : place.tokens()) {
				constructedPlace.addToken(new TimedToken(constructedPlace, token.age(), token.color()));
			}
			
			// Gui work
			if (this.guiModels != null) {
				Place oldPlace = getSharedPlace(place.name());
				if (oldPlace != null) {
					TimedPlaceComponent newPlace = new TimedPlaceComponent(
						oldPlace.getPositionX(),
						oldPlace.getPositionY(),
						oldPlace.getId(),
						oldPlace.getNameOffsetX(),
						oldPlace.getNameOffsetY(),
                        lens
					);
					newPlace.setUnderlyingPlace(constructedPlace);
					newPlace.setName(uniquePlaceName);
					guiModel.addPetriNetObject(newPlace);
				}
			}
		}
	}

	private void createPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);
                    if (isComponentEmpty(currentGuiModel)) {
                        continue;
                    }
            }
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedPlace timedPlace : tapn.places()) {			
				if (!timedPlace.isShared()) {
					String uniquePlaceName = (!singleComponentNoPrefix || model.activeTemplates().size() > 1) ? composedPlaceName(timedPlace) : timedPlace.name();

					LocalTimedPlace place = null;
					if (timedPlace.invariant().upperBound() instanceof Bound.InfBound) {					
						place = new LocalTimedPlace(uniquePlaceName, timedPlace.invariant(),timedPlace.getColorType());
					} else {
						place = new LocalTimedPlace(uniquePlaceName, new TimeInvariant(timedPlace.invariant().isUpperNonstrict(), new IntBound(timedPlace.invariant().upperBound().value())),timedPlace.getColorType());
					}
					place.setCtiList(timedPlace.getCtiList());
					constructedModel.add(place);
					mapping.addMapping(tapn.name(), timedPlace.name(), uniquePlaceName);
                    
					for (TimedToken token : timedPlace.tokens()) {
						place.addToken(new TimedToken(place, token.age(), token.color()));
					}
					place.setTokenExpression(timedPlace.getTokensAsExpression());
					
					// Gui work
					if (this.guiModels != null) {
						Place oldPlace = currentGuiModel.getPlaceByName(timedPlace.name());
                        TimedPlaceComponent newPlace = new TimedPlaceComponent(
                            oldPlace.getPositionX() + offset.value1() * greatestWidth,
                            oldPlace.getPositionY() + offset.value2() * greatestHeight,
                            oldPlace.getId(),
                            oldPlace.getNameOffsetX(),
                            oldPlace.getNameOffsetY(),
                            lens
                        );
						newPlace.setGuiModel(guiModel);

						newPlace.setUnderlyingPlace(place);
						newPlace.setName(uniquePlaceName);
						guiModel.addPetriNetObject(newPlace);
					}
				}
			}
			i++;
		}
	}

	private void createTransitions(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;

		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);
				if (isComponentEmpty(currentGuiModel)) {
					continue;
				}
			}
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedTransition timedTransition : tapn.transitions()) {
				if (!processedSharedObjects.contains(timedTransition.name())) {
					
					// CAUTION: This if statement removes orphan transitions.
					//   This changes answers for e.g. DEADLOCK queries if
					//   support for such queries are added later.
					// ONLY THE IF SENTENCE SHOULD BE REMOVED. REST OF CODE SHOULD BE LEFT INTACT
					if (!timedTransition.isOrphan()) {
                        String uniqueTransitionName = "";
                        if (!singleComponentNoPrefix || model.activeTemplates().size() > 1) {
                            uniqueTransitionName = timedTransition.isShared()
                                ? "Shared_" + timedTransition.name()
                                : timedTransition.model().name() + "_" + timedTransition.name();
                        } else {
                            uniqueTransitionName = timedTransition.isShared()
                                ? "Shared_" + timedTransition.name()
                                : timedTransition.name();
                        }

						TimedTransition transition = new TimedTransition(uniqueTransitionName, timedTransition.isUrgent(), timedTransition.getGuard(), timedTransition.getDistribution(), timedTransition.getWeight());
						if (timedTransition.isUncontrollable()) {
						    transition.setUncontrollable(true);
                        }

						constructedModel.add(transition);
						
						// Gui work
						if (this.guiModels != null) {
							Transition oldTransition = currentGuiModel.getTransitionByName(timedTransition.name());
							TimedTransitionComponent newTransition = new TimedTransitionComponent(
                                oldTransition.getPositionX() + offset.value1() * greatestWidth,
                                oldTransition.getPositionY() + offset.value2() * greatestHeight,
                                oldTransition.getId(),
                                oldTransition.getNameOffsetX(),
                                oldTransition.getNameOffsetY(),
                                oldTransition.getAngle(),
                                lens
                            );
							newTransition.setUnderlyingTransition(transition);
							newTransition.setName(uniqueTransitionName);
							guiModel.addPetriNetObject(newTransition);
						}
						
						if (timedTransition.isShared()) {
							String name = timedTransition.sharedTransition().name();
							processedSharedObjects.add(name);
							mapping.addMappingForShared(name, uniqueTransitionName);
						} else {
							mapping.addMapping(tapn.name(), timedTransition.name(), uniqueTransitionName);
						}
					} else {
						if (!hasShownMessage && !(ExportBatchDialog.isDialogVisible())) {
							messenger.displayInfoMessage("There are orphan transitions (no incoming and no outgoing arcs) in the model.");
							hasShownMessage = true;
						}
						else if (ExportBatchDialog.isDialogVisible()) {
							ExportBatchDialog.setNoOrphanTransitions(true);
						}
					}
				}
			}
			i++;
		}
	}
	
	private ArcPath createArcPath(DataLayer currentGuiModel, PlaceTransitionObject source, PlaceTransitionObject target, Arc arc, int offsetX, int offsetY) {
		Arc guiArc = currentGuiModel.getArcByEndpoints(source, target);
		ArcPath arcPath = guiArc.getArcPath();
		int arcPathPointsNum = arcPath.getNumPoints();
		
		// Build ArcPath
		ArcPath newArcPath = new ArcPath(arc);
		newArcPath.purgePathPoints();
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

	private void createInputArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
                        
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);

				if (isComponentEmpty(currentGuiModel)) {
					continue;
				}
			}
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedInputArc arc : tapn.inputArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				
				String targetTemplate = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(targetTemplate, arc.destination().name()));

				TimeInterval newInterval = new TimeInterval(arc.interval());

				if (inlineConstants) {
                    newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
                    if (newInterval.upperBound() instanceof Bound.InfBound) {
                        newInterval.setUpperBound(newInterval.upperBound());
                    } else {
                        newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
                    }
                }

				TimedInputArc addedArc = new TimedInputArc(source, target, newInterval, arc.getWeightValue(), arc.getArcExpression());
				addedArc.setColorTimeIntervals(arc.getColorTimeIntervals());
				constructedModel.add(addedArc);
				
				// Gui work
				if (this.guiModels != null) {
					Place guiSource = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
					Transition guiTarget = guiModel.getTransitionByName(mapping.map(targetTemplate, arc.destination().name()));
					
					TimedInputArcComponent newArc = new TimedInputArcComponent(
                        new TimedOutputArcComponent(
                            guiSource,
                            guiTarget,
                            arc.getWeight().value(),
                            mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(targetTemplate, arc.destination().name())
                        ),
                        lens
					);
					
					// Build ArcPath
					Place oldGuiSource = currentGuiModel.getPlaceByName(arc.source().name());
					Transition oldGuiTarget = currentGuiModel.getTransitionByName(arc.destination().name());
					ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
					
					// Set arcPath, guiModel and connectors
					newArc.setUnderlyingArc(addedArc);
					newArc.setArcPath(newArcPath);
					newArc.updateArcPosition();
					guiModel.addPetriNetObject(newArc);
				}
			}
			i++;
		}
	}

	private void createOutputArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);
				if (isComponentEmpty(currentGuiModel)) {
					continue;
				}
			}
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedOutputArc arc : tapn.outputArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				TimedTransition source = constructedModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name()));

				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));

				TimedOutputArc addedArc = new TimedOutputArc(source, target, arc.getWeightValue(), arc.getExpression());
				constructedModel.add(addedArc);
				
				// Gui work
				if (this.guiModels != null) {
					Transition guiSource = guiModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name()));
					Place guiTarget = guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
					
					TimedOutputArcComponent newArc = new TimedOutputArcComponent(
                        guiModel.getTransitionByName(mapping.map(sourceTemplate, arc.source().name())),
							guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name())),
							arc.getWeight().value(),
							mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name())
                    );
					
					// Build ArcPath
					Transition oldGuiSource = currentGuiModel.getTransitionByName(arc.source().name());
					Place oldGuiTarget = currentGuiModel.getPlaceByName(arc.destination().name());
					ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
					
					// Set arcPath, guiModel and connectors
					newArc.setUnderlyingArc(addedArc);
					newArc.setArcPath(newArcPath);
					newArc.updateArcPosition();
					guiModel.addPetriNetObject(newArc);
				}
			}
			i++;
		}
	}

	private void createTransportArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;
		int nextGroupNr = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
                        
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);
				if (isComponentEmpty(currentGuiModel)) {
					continue;
				}
			}
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TransportArc arc : tapn.transportArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				String transitionTemplate = arc.transition().isShared() ? "" : tapn.name();
				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				TimedTransition transition = constructedModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
				TimedPlace destination = constructedModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
				
				TimeInterval newInterval = new TimeInterval(arc.interval());
				if(inlineConstants){
                    newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
                    if (newInterval.upperBound() instanceof Bound.InfBound) {
                        newInterval.setUpperBound(newInterval.upperBound());
                    } else {
                        newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
                    }
                }
				TransportArc addedArc = new TransportArc(source, transition, destination, newInterval, arc.getWeightValue(),
                    arc.getInputExpression().deepCopy(), arc.getOutputExpression().deepCopy());
				addedArc.setColorTimeIntervals(arc.getColorTimeIntervals());
				constructedModel.add(addedArc);
				
				//Create input transport arc
				// Gui work
				if (this.guiModels != null) {
					Place guiSourceIn = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
					Transition guiTargetIn = guiModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
									
					TimedTransportArcComponent newInArc = new TimedTransportArcComponent(
							new TimedInputArcComponent(new TimedOutputArcComponent(
                                guiSourceIn,
								guiTargetIn,
								arc.getWeight().value(),
								mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(transitionTemplate, arc.transition().name())
                            )
							),
							nextGroupNr, 
							true
					);
					
					// Build ArcPath
					Place oldGuiSourceIn = currentGuiModel.getPlaceByName(arc.source().name());
					Transition oldGuiTargetIn = currentGuiModel.getTransitionByName(arc.transition().name());
					ArcPath newArcPathIn = createArcPath(currentGuiModel, oldGuiSourceIn, oldGuiTargetIn, newInArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
								
					newInArc.setUnderlyingArc(addedArc);
					newInArc.setArcPath(newArcPathIn);
					newInArc.updateArcPosition();
					guiModel.addPetriNetObject(newInArc);
					
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
	
					newInArc.setGroupNr(nextGroupNr + 1);
					
					//Create output transport arc
					Transition guiSourceOut = guiModel.getTransitionByName(mapping.map(transitionTemplate, arc.transition().name()));
					Place guiTargetOut = guiModel.getPlaceByName(mapping.map(destinationTemplate, arc.destination().name()));
					
					TimedTransportArcComponent newOutArc = new TimedTransportArcComponent(
							new TimedInputArcComponent(new TimedOutputArcComponent(
                                guiSourceOut,
								guiTargetOut,
								1,
								mapping.map(transitionTemplate, arc.transition().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name())
                            )
							),
							nextGroupNr + 1, 
							false
					);
					
					// Build ArcPath
					Transition oldGuiSourceOut = currentGuiModel.getTransitionByName(arc.transition().name());
					Place oldGuiTargetOut = currentGuiModel.getPlaceByName(arc.destination().name());
					ArcPath newArcPathOut = createArcPath(currentGuiModel, oldGuiSourceOut, oldGuiTargetOut, newOutArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
				
					newOutArc.setUnderlyingArc(addedArc);
					newOutArc.setArcPath(newArcPathOut);
					newOutArc.updateArcPosition();
					guiModel.addPetriNetObject(newOutArc);

				}
			}
			i++;
		}
	}

	
	
	private void createInhibitorArcs(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping, DataLayer guiModel, int greatestWidth, int greatestHeight) {
		int i = 0;
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
                        
			DataLayer currentGuiModel = null;
			if (this.guiModels != null) {
				currentGuiModel = this.guiModels.get(tapn);
				if (isComponentEmpty(currentGuiModel)) {
					continue;
				}
			}
			Tuple<Integer, Integer> offset = this.calculateComponentPosition(i);
			
			for (TimedInhibitorArc arc : tapn.inhibitorArcs()) {
				String sourceTemplate = arc.source().isShared() ? "" : tapn.name();
				String destinationTemplate = arc.destination().isShared() ? "" : tapn.name();
				
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(destinationTemplate, arc.destination().name()));

				TimeInterval newInterval = new TimeInterval(arc.interval());
				if(inlineConstants){
                    newInterval.setLowerBound(new IntBound(newInterval.lowerBound().value()));
                    if (newInterval.upperBound() instanceof Bound.InfBound) {
                        newInterval.setUpperBound(newInterval.upperBound());
                    } else {
                        newInterval.setUpperBound(new IntBound(newInterval.upperBound().value()));
                    }
                }
				TimedInhibitorArc addedArc = new TimedInhibitorArc(source, target, newInterval, arc.getWeightValue(), arc.getArcExpression());
				constructedModel.add(addedArc);
				
				// Gui work
				if (this.guiModels != null) {
					Place guiSource = guiModel.getPlaceByName(mapping.map(sourceTemplate, arc.source().name()));
					Transition guiTarget = guiModel.getTransitionByName(mapping.map(destinationTemplate, arc.destination().name()));
					TimedInhibitorArcComponent newArc = new TimedInhibitorArcComponent(new TimedOutputArcComponent(
                        guiSource,
							guiTarget,
							arc.getWeight().value(),
							mapping.map(sourceTemplate, arc.source().name()) + "_to_" + mapping.map(destinationTemplate, arc.destination().name())
                    ), "");
					
					// Build ArcPath
					Place oldGuiSource = currentGuiModel.getPlaceByName(arc.source().name());
					Transition oldGuiTarget = currentGuiModel.getTransitionByName(arc.destination().name());
					ArcPath newArcPath = createArcPath(currentGuiModel, oldGuiSource, oldGuiTarget, newArc, offset.value1() * greatestWidth, offset.value2() * greatestHeight);
					
					newArc.setUnderlyingArc(addedArc);
					newArc.setArcPath(newArcPath);
					newArc.updateArcPosition();
					guiModel.addPetriNetObject(newArc);
				}
			}
			i++;
		}
	}

   public String composedTransitionName(TimedTransition transition) {
        if (transition.isShared()) {
            return "Shared_" + transition.name();
        } else if (singleComponentNoPrefix) {
            return transition.name();
        } else {
            return transition.model().name() + "_" + transition.name();
        }
   }
   
   public String composedPlaceName(TimedPlace place) {
        return  place.isShared() ? "Shared_" + place.name() : ((LocalTimedPlace)place).model().name() + "_" + place.name();
   }
   

}
