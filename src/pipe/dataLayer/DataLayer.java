package pipe.dataLayer;

import java.util.*;

import dk.aau.cs.util.RequireException;
import pipe.gui.canvas.Canvas;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.util.Require;


public class DataLayer {


	//XXX: Temp solution while refactoring, should be changed to interface to now allow to many actions
	//Long term should use callback to not have tight coupling
	private Canvas view;
	public void addedToView(Canvas view){this.view = view;}
	public void removedFromView() {this.view = null;}

	//XXX temp solution while refactorting, component removes children them self
	//migth not be best solution long term.
	private void removeFromViewIfConnected(PetriNetObject pno) {
		if (view != null) {
			view.removePetriNetObject(pno.getGraphicalElement());
		}
	}

	private void addToViewIfConnected(PetriNetObject pno) {
		if (view != null) {
			view.addNewPetriNetObject(pno.getGraphicalElement());
		}
	}

	/** PNML File Name */
	public String pnmlName = null;
	/** List containing all the Place objects in the Petri-Net */
	private final ArrayList<Place> placesArray = new ArrayList<Place>();
	/** ArrayList containing all the Transition objects in the Petri-Net */
	private final ArrayList<Transition> transitionsArray = new ArrayList<Transition>();
	/** ArrayList containing all the Arc objects in the Petri-Net */
	private final ArrayList<Arc> arcsArray = new ArrayList<Arc>();

	/** Set holding all ArcPathPoints
	 * Uses the reference as lookup key (not hash code)
	 * Collections.newSetFromMap(new IdentityHashMap<E, Boolean>());
	 * */
	private final Set<ArcPathPoint> arcPathSet = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * ArrayList for net-level label objects (as opposed to element-level
	 * labels).
	 */
	private final ArrayList<AnnotationNote> labelsArray = new ArrayList<AnnotationNote>();

	/**
	 * An ArrayList used to point to either the Arc, Place or Transition
	 * ArrayLists when these ArrayLists are being update
	 */
	private ArrayList<? extends PetriNetObject> changeArrayList = null;

    // may as well do the hashtable here as well
    /**
	 * Hashtable which maps PlaceTransitionObjects to their list of connected
	 * arcs
	 */
	private final Hashtable<PlaceTransitionObject, ArrayList<TimedOutputArcComponent>> arcsMap = new Hashtable<PlaceTransitionObject, ArrayList<TimedOutputArcComponent>>();

	/**
	 * Hashtable which maps PlaceTransitionObjects to their list of connected
	 * arcs
	 */
	private final Hashtable<PlaceTransitionObject, ArrayList<TimedInhibitorArcComponent>> tapnInhibitorsMap = new Hashtable<PlaceTransitionObject, ArrayList<TimedInhibitorArcComponent>>();


	//XXX: Added from drawingsurface to have a way to acces all elements added,
    //Should be refactored later to combine the existing list, however this is the quick fix during refactoring
    private final ArrayList<PetriNetObject> petriNetObjects = new ArrayList<PetriNetObject>();

    public ArrayList<PetriNetObject> getPNObjects() {
        return petriNetObjects;
    }

    public ArrayList<PetriNetObject> getPlaceTransitionObjects(){
        ArrayList<PetriNetObject> result = new ArrayList<PetriNetObject>();
        for (PetriNetObject pnObject : petriNetObjects) {
            if((pnObject instanceof PlaceTransitionObject)){
                result.add(pnObject);
            }
        }
        return result;
    }

	/**
	 * Create empty Petri-Net object
	 */
	public DataLayer() {
        super();
    }


	/**
	 * Add placeInput to the back of the Place ArrayList All observers are
	 * notified of this change (Model-View Architecture)
	 * 
	 * @param placeInput
	 *            Place Object to add
	 */
	private void addPlace(Place placeInput) {
		Require.that(placeInput != null, "input place was null");

		placesArray.add(placeInput);
	}

	/**
	 * Add labelInput to the back of the AnnotationNote ArrayList All observers
	 * are notified of this change (Model-View Architecture)
	 * 
	 * @param labelInput
	 *            AnnotationNote Object to add
	 */
	private void addAnnotation(AnnotationNote labelInput) {
		labelsArray.add(labelInput);
	}

	/**
	 * Add transitionInput to back of the Transition ArrayList All observers are
	 * notified of this change (Model-View Architecture)
	 * 
	 * @param transitionInput
	 *            Transition Object to add
	 */
	private void addTransition(Transition transitionInput) {
		Require.that(transitionInput != null, "input transition was null");
		
		transitionsArray.add(transitionInput);
	}

	/**
	 * Add arcInput to back of the Arc ArrayList All observers are notified of
	 * this change (Model-View Architecture)
	 * 
	 * @param arcInput
	 *            Arc Object to add
	 */
	private void addArc(TimedOutputArcComponent arcInput) {
		boolean unique = true;

		// Check if the arcs have a valid source and target
		if (arcInput.getSource() == null || arcInput.getTarget() == null) {
			System.err.println(
			    "Cant add arc" + arcInput + " where source: "
					+ arcInput.getSource() + " or target: "
					+ arcInput.getTarget() + " is null"
            );
			return;
		}

        if (arcInput.getId() != null && arcInput.getId().length() > 0) {
            for (Arc arc : arcsArray) {
                if (arcInput.getId().equals(arc.getId())) {
                    unique = false;
                }
            }
        } else {
            String id = null;
            if (arcsArray.size() > 0) {
                int no = arcsArray.size();
                do {
                    for (Arc arc : arcsArray) {
                        id = "A" + no;
                        if (arc != null) {
                            if (id.equals(arc.getId())) {
                                unique = false;
                                no++;
                            } else {
                                unique = true;
                            }
                        }
                    }
                } while (!unique);
            } else {
                id = "A0";
            }
            arcInput.setId(id);
        }

        //XXX: this is still nedede as nets loaded from file (XML/tapn) does not set connectedTo correctly //2019-09-18
        // Transportarc fix boddy
        if (arcInput instanceof TimedTransportArcComponent) {
            TimedTransportArcComponent tmp = (TimedTransportArcComponent) arcInput;
            PlaceTransitionObject first = tmp.getSource();

            if (tmp.getConnectedTo() == null) {
                if (first instanceof TimedPlaceComponent) {

                    for (Object o : tmp.getTarget().getPostset()) {

                        if (o instanceof TimedTransportArcComponent) {
                            if (tmp.getGroupNr() == ((TimedTransportArcComponent) o).getGroupNr()) {
                                // Found partner

                                tmp.setConnectedTo(((TimedTransportArcComponent) o));
                                ((TimedTransportArcComponent) o).setConnectedTo(tmp);

                                break;
                            }
                        }

                    }

                } else {
                    // First is TimedTransition

                    for (Object o : tmp.getSource().getPreset()) {

                        if (o instanceof TimedTransportArcComponent) {
                            if (tmp.getGroupNr() == ((TimedTransportArcComponent) o).getGroupNr()) {
                                // Found partner

                                tmp.setConnectedTo(((TimedTransportArcComponent) o));
                                ((TimedTransportArcComponent) o).setConnectedTo(tmp);

                                break;
                            }
                        }

                    }

                }
            }

        }

        arcsArray.add(arcInput);
        addArcToArcsMap(arcInput);

    }

	private void addTransportArc(TimedTransportArcComponent transportArc) {
		arcsArray.add(transportArc);
		addArcToArcsMap(transportArc);
	}

	private void addArc(TimedInhibitorArcComponent inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null && inhibitorArcInput.getId().length() > 0) {
                for (Arc arc : arcsArray) {
                    if (inhibitorArcInput.getId().equals(arc.getId())) {
                        unique = false;
                    }
                }
			} else {
				String id = null;
				if (arcsArray.size() > 0) {
					int no = arcsArray.size();
					do {
                        for (Arc arc : arcsArray) {
                            id = "I" + no;
                            if (arc != null) {
                                if (id.equals(arc.getId())) {
                                    unique = false;
                                    no++;
                                } else {
                                    unique = true;
                                }
                            }
                        }
					} while (!unique);
				} else {
					id = "I0";
				}
                inhibitorArcInput.setId(id);
            }

			arcsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

		}
	}


	/**
	 * Update the arcsMap hashtable to reflect the new arc
	 * 
	 * @param arcInput
	 *            New Arc
	 * */
	private void addArcToArcsMap(TimedOutputArcComponent arcInput) {
		// now we want to add the arc to the list of arcs for it's source and target
		PlaceTransitionObject source = arcInput.getSource();
		PlaceTransitionObject target = arcInput.getTarget();
		ArrayList<TimedOutputArcComponent> newList = null;

		if (source != null) {
			if (arcsMap.get(source) != null) {
				arcsMap.get(source).add(arcInput);
			} else {
				newList = new ArrayList<TimedOutputArcComponent>();
				newList.add(arcInput);
				arcsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (arcsMap.get(target) != null) {
				arcsMap.get(target).add(arcInput);
			} else {
				newList = new ArrayList<TimedOutputArcComponent>();
				newList.add(arcInput);
				arcsMap.put(target, newList);
			}
		}
	}

	/**
	 * Update the inhibitorsMap hashtable to reflect the new inhibitor arc
	 * 
	 * @param inhibitorArcInput
	 *            New Arc
	 */
	private void addInhibitorArcToInhibitorsMap(TimedInhibitorArcComponent inhibitorArcInput) {
		// now we want to add the inhibitor arc to the list of inhibitor arcs for it's source and target
		PlaceTransitionObject source = inhibitorArcInput.getSource();
		PlaceTransitionObject target = inhibitorArcInput.getTarget();
		ArrayList<TimedInhibitorArcComponent> newList = null;

		if (source != null) {
			if (tapnInhibitorsMap.get(source) != null) {
				tapnInhibitorsMap.get(source).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<TimedInhibitorArcComponent>();
				newList.add(inhibitorArcInput);
				tapnInhibitorsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (tapnInhibitorsMap.get(target) != null) {
				tapnInhibitorsMap.get(target).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<TimedInhibitorArcComponent>();
				newList.add(inhibitorArcInput);
				tapnInhibitorsMap.put(target, newList);
			}
		}
	}


	/**
	 * Add any PetriNetObject - the object will be added to the appropriate
	 * list. If the object passed in isn't a Transition, Place or Arc nothing
	 * will happen. All observers are notified of this change.
	 * 
	 * @param pnObject
	 *            The PetriNetObject to be added.
	 */
	public void addPetriNetObject(PetriNetObject pnObject) {

		pnObject.setGuiModel(this);
		addToViewIfConnected(pnObject); // Must be called after model is set

        //XXX: temp solution to have access to all elements types at once
        petriNetObjects.add(pnObject);

        pnObject.setDeleted(false);

        if (pnObject instanceof Arc ) {

        	Arc arc = (Arc)pnObject;

        	arc.getSource().addConnectFrom(arc);
        	arc.getTarget().addConnectTo(arc);

		}

		if (setPetriNetObjectArrayList(pnObject)) {
			if (pnObject instanceof TimedInhibitorArcComponent) {
				addArc((TimedInhibitorArcComponent) pnObject);
			} else if (pnObject instanceof TimedOutputArcComponent) {
				addArc((TimedOutputArcComponent) pnObject);
			} else if (pnObject instanceof Place) {
				addPlace((Place) pnObject);
			} else if (pnObject instanceof Transition) {
				addTransition((Transition) pnObject);
			} else if (pnObject instanceof AnnotationNote) {
				addAnnotation((AnnotationNote)pnObject);
			}
		} else if (pnObject instanceof ArcPathPoint){
			arcPathSet.add((ArcPathPoint) pnObject);
		} else {
			throw new RuntimeException("Unknow element type added");
		}
		// we reset to null so that the wrong ArrayList can't get added to
		changeArrayList = null;
	}

	/**
	 * Removes the specified object from the appropriate ArrayList of objects.
	 * All observers are notified of this change.
	 * 
	 * @param pnObject
	 *            The PetriNetObject to be removed.
	 */
	public void removePetriNetObject(PetriNetObject pnObject) {

        //XXX: Should remove guiModel for object, but is used for undelete action, KYRKE 2018-10-18
        //pnObject.setGuiModel(null);

		removeFromViewIfConnected(pnObject);

        //XXX: temp solution to have access to all elements types at once
        petriNetObjects.remove(pnObject);

		pnObject.setDeleted(true);

		//XXX: is also called down below
		if (pnObject instanceof Arc ) {

			Arc arc = (Arc)pnObject;

			arc.getSource().removeFromArc(arc);
			arc.getTarget().removeToArc(arc);

		}else if (pnObject instanceof ArcPathPoint) {
			arcPathSet.remove(pnObject);
		}

		boolean didSomething = false;
		ArrayList<?> attachedArcs = null;

		try {
			if (setPetriNetObjectArrayList(pnObject)) {
				didSomething = changeArrayList.remove(pnObject);
				// we want to remove all attached arcs also
				if (pnObject instanceof PlaceTransitionObject) {
					if (arcsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = arcsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all in inverse order!
						//for (int i = attachedArcs.size() - 1; i >= 0; i--) {
						//	try {
						//		((Arc) attachedArcs.get(i)).delete();
						//	} catch (IndexOutOfBoundsException e) {
						//		// XXX - this is a hack
						//		// This is OK, it just means that the transport
						//		// arc already has been removed
						//	}

						//}

						if (attachedArcs.size() > 0) {
							//XXX Model is no longer valid as the pno is removed from petriNetObjects list
							throw new RequireException("Arc to/from the object must be delete first");
						}

						arcsMap.remove(pnObject);
					}

					if (tapnInhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = tapnInhibitorsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all in inverse order!
						//for (int i = attachedArcs.size() - 1; i >= 0; i--) {
						//	((Arc) attachedArcs.get(i)).delete();
						//}
						if (attachedArcs.size() > 0) {
							//XXX Model is no longer valid as the pno is removed from petriNetObjects list
							throw new RequireException("Arc to/from the object must be delete first");
						}
						tapnInhibitorsMap.remove(pnObject);
					}
				} else if (pnObject instanceof TimedInhibitorArcComponent) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc) pnObject).getSource();

					if (attached != null) {
						ArrayList<TimedInhibitorArcComponent> a = tapnInhibitorsMap.get(attached);
						if (a != null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached).removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}

					attached = ((Arc) pnObject).getTarget();

					if (attached != null) {
						if (tapnInhibitorsMap.get(attached) != null) { // causing null pointer exceptions (!)
							tapnInhibitorsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached).removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}
				} else if (pnObject instanceof TimedOutputArcComponent) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc) pnObject).getSource();

					if (attached != null) {
						ArrayList<TimedOutputArcComponent> a = arcsMap.get(attached);
						if (a != null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached).removeArcCompareObject((Arc) pnObject);
							attached.updateConnected();
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}

					attached = ((Arc) pnObject).getTarget();
					if (attached != null) {
						if (arcsMap.get(attached) != null) { // causing null pointer exceptions (!)
							arcsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached).removeArcCompareObject((Arc) pnObject);
							attached.updateConnected();
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}
				} 

			}
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException [debug]\n" + npe.getMessage());
			throw npe;
		}
		// we reset to null so that the wrong ArrayList can't get added to
		changeArrayList = null;
	}

	/**
	 * Returns an iteratbale for the transitions array. Used by Animator.class to
	 * set all enabled transitions to highlighted
	 */
	public Iterable<Transition> transitions() {

		return transitionsArray;
	}


	/**
	 * Sets an internal ArrayList according to the class of the object passed
	 * in.
	 * 
	 * @param pnObject
	 *            The pnObject in question.
	 * @return Returns True if the pnObject is of type Place, Transition or Arc
	 */
	private boolean setPetriNetObjectArrayList(PetriNetObject pnObject) {

		// determine appropriate ArrayList
		if (pnObject instanceof Transition) {
			changeArrayList = transitionsArray;
			return true;
		} else if (pnObject instanceof Place) {
			changeArrayList = placesArray;
			return true;
		} else if (pnObject instanceof TimedInhibitorArcComponent) {
			changeArrayList = arcsArray;
			return true;
		} else if (pnObject instanceof TimedOutputArcComponent) {
			changeArrayList = arcsArray;
			return true;
		} else if (pnObject instanceof AnnotationNote) {
			changeArrayList = labelsArray;
			return true;
		}
		return false;
	}

	/**
	 * Returns an iterator of all PetriNetObjects - the order of these cannot be
	 * guaranteed.
	 * 
	 * @return An iterator of all PetriNetObjects
	 */
	public Iterable<PetriNetObject> getPetriNetObjects() {
		ArrayList<PetriNetObject> all = new ArrayList<PetriNetObject>(placesArray);
		all.addAll(transitionsArray);
		all.addAll(arcsArray);
		all.addAll(labelsArray);

		return all;
	}

	public Iterable<PetriNetObject> getPetriNetObjectsWithArcPathPoint() {
		ArrayList<PetriNetObject> all = new ArrayList<PetriNetObject>(placesArray);
		all.addAll(transitionsArray);
		all.addAll(arcsArray);
		all.addAll(labelsArray);
		all.addAll(arcPathSet);

		return all;
	}

	/**
	 * Get a List of all the Place objects in the Petri-Net
	 * 
	 * @return A List of all the Place objects
	 */
	public Place[] getPlaces() {
		Place[] returnArray = new Place[placesArray.size()];

		for (int i = 0; i < placesArray.size(); i++) {
			returnArray[i] = placesArray.get(i);
		}
		return returnArray;
	}

	/**
	 * Get a List of all the net-level NameLabel objects in the Petri-Net
	 * 
	 * @return A List of all the net-level (as opposed to element-specific)
	 *         label objects
	 */
	public AnnotationNote[] getLabels() {
		AnnotationNote[] returnArray = new AnnotationNote[labelsArray.size()];

		for (int i = 0; i < labelsArray.size(); i++) {
			returnArray[i] = labelsArray.get(i);
		}
		return returnArray;
	}

	/**
	 * Get an List of all the Transition objects in the Petri-Net
	 * 
	 * @return An List of all the Transition objects
	 */
	public Transition[] getTransitions() {
		Transition[] returnArray = new Transition[transitionsArray.size()];

		for (int i = 0; i < transitionsArray.size(); i++) {
			returnArray[i] = transitionsArray.get(i);
		}
		return returnArray;
	}

	/**
	 * Get an List of all the Arcs objects in the Petri-Net
	 * 
	 * @return An List of all the Arc objects
	 */
	public Arc[] getArcs() {
		Arc[] returnArray = new Arc[arcsArray.size()];

		for (int i = 0; i < arcsArray.size(); i++) {
			returnArray[i] = arcsArray.get(i);
		}
		return returnArray;
	}

	public Arc getArcByEndpoints(PlaceTransitionObject source, PlaceTransitionObject target) {
		Arc returnArc = null;
		for (Arc arc : arcsArray) {
			if (arc.getSource() == source && arc.getTarget() == target) {
				returnArc = arc;
			}
		}
		return returnArc;
	}

	/**
	 * Return the Transition called transitionName from the Petri-Net
	 * 
	 * @param transitionID
	 *            ID of Transition object to return
	 * @return The first Transition object found with a name equal to
	 *         transitionName
	 */
	public Transition getTransitionById(String transitionID) {
		Transition returnTransition = null;

        if (transitionID != null) {
            for (Transition transition : transitionsArray) {
                if (transitionID.equalsIgnoreCase(transition.getId())) {
                    returnTransition = transition;
                }
            }
        }
        return returnTransition;
	}

	/**
	 * Return the Transition called transitionName from the Petri-Net
	 * 
	 * @param transitionName
	 *            Name of Transition object to return
	 * @return The first Transition object found with a name equal to
	 *         transitionName
	 */
	public Transition getTransitionByName(String transitionName) {
		Transition returnTransition = null;

        if (transitionName != null) {
            for (Transition transition : transitionsArray) {
                if (transitionName.equalsIgnoreCase(transition.getName())) {
                    returnTransition = transition;
                }
            }
        }
        return returnTransition;
	}

	/**
	 * Return the Place called placeName from the Petri-Net
	 *
	 * @param placeID
	 *            ID of Place object to return
	 * @return The first Place object found with id equal to placeId
	 */
	public Place getPlaceById(String placeID) {
		Place returnPlace = null;

        if (placeID != null) {
            for (Place place : placesArray) {
                if (placeID.equalsIgnoreCase(place.getId())) {
                    returnPlace = place;
                }
            }
        }
        return returnPlace;
	}

	/**
	 * Return the Place called placeName from the Petri-Net
	 * 
	 * @param placeName
	 *            Name of Place object to return
	 * @return The first Place object found with a name equal to placeName
	 */
	public Place getPlaceByName(String placeName) {
		Place returnPlace = null;

        if (placeName != null) {
            for (Place place : placesArray) {
                if (placeName.equalsIgnoreCase(place.getName())) {
                    returnPlace = place;
                }
            }
        }
        return returnPlace;
	}

	/**
	 * Return the PlaceTransitionObject called ptoName from the Petri-Net
	 * 
	 * @param ptoId
	 *            Id of PlaceTransitionObject object to return
	 * @return The first Arc PlaceTransitionObject found with a name equal to
	 *         ptoName
	 */
	public PlaceTransitionObject getPlaceTransitionObject(String ptoId) {
		if (ptoId != null) {
			if (getPlaceById(ptoId) != null) {
				return getPlaceById(ptoId);
			} else if (getTransitionById(ptoId) != null) {
				return getTransitionById(ptoId);
			}
		}

		throw new NullPointerException("One or more arcs are connected to a place/transition, which can not be found in the net");
    }

    public void redrawVisibleTokenLists() {
		for (Place place : placesArray) {
			if (place instanceof TimedPlaceComponent) {
				TimedPlaceComponent tp = (TimedPlaceComponent) place;
				if (tp.isAgeOfTokensShown()) {
					tp.showAgeOfTokens(true);
				}
			}
		}
	}

	public void repaintPlaces() {
		for (Place p : placesArray) {
			p.repaint();
		}
	}

	public void repaintPlaces(boolean displayConstantNames) {
		for (Place p : placesArray) {
			p.update(displayConstantNames);
			p.repaint();
		}
	}

	public void repaintAll(boolean displayConstantNames) {
		repaintPlaces(displayConstantNames);

		for (Transition t : transitionsArray) {
			t.updateOnMoveOrZoom();
			t.repaint();
		}

		for (Arc arc : arcsArray) {
			arc.updateLabel(displayConstantNames);
			arc.repaint();
		}

	}
	
	public DataLayer copy(TimedArcPetriNet tapn) {
		DataLayer guiModel = new DataLayer();
		Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping = new Hashtable<PlaceTransitionObject, PlaceTransitionObject>();
		
		for(Place p : placesArray) {
			if(p instanceof TimedPlaceComponent) {
				TimedPlaceComponent place = ((TimedPlaceComponent)p).copy(tapn);
				oldToNewMapping.put(p, place);
				guiModel.addPetriNetObject(place);
			}
		}
		
		for(Transition t : transitionsArray) {
			if(t instanceof TimedTransitionComponent) {
				TimedTransitionComponent trans = ((TimedTransitionComponent)t).copy(tapn);
				oldToNewMapping.put(t, trans);
				guiModel.addPetriNetObject(trans);
			}
		}
		
		for(Arc arc : arcsArray) {
			if(arc instanceof TimedTransportArcComponent) {
				Arc transArc = ((TimedTransportArcComponent)arc).copy(tapn, oldToNewMapping);
				guiModel.addPetriNetObject(transArc);
			}
			else if(arc instanceof TimedInhibitorArcComponent) {
				TimedInhibitorArcComponent inhibArc = ((TimedInhibitorArcComponent)arc).copy(tapn, oldToNewMapping);
				guiModel.addPetriNetObject(inhibArc);
			}
			else if(arc instanceof TimedInputArcComponent) {
				TimedInputArcComponent inputArc = ((TimedInputArcComponent)arc).copy(tapn, oldToNewMapping);
				guiModel.addPetriNetObject(inputArc);
			}
			else if(arc instanceof TimedOutputArcComponent) {
				TimedOutputArcComponent outputArc = ((TimedOutputArcComponent)arc).copy(tapn, oldToNewMapping);
				guiModel.addPetriNetObject(outputArc);
			}
			else {
				throw new RuntimeException("Arc was unknown type");
			}
		}
		
		for(AnnotationNote note : labelsArray) {
			AnnotationNote annotation = note.copy();
			guiModel.addPetriNetObject(annotation);
		}
			
		
		return guiModel;
	}
	public PlaceTransitionObject getPlaceTransitionObjectByName(String name) {
		for(PetriNetObject object : getPlaceTransitionObjects()) {
			if(object.getName().equals(name)) {
				return ((PlaceTransitionObject)object);
			}
		}
		return null;
	}
}
