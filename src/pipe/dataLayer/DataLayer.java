package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;

import dk.aau.cs.util.Require;


/**
 * <b>DataLayer</b> - Encapsulates entire Petri-Net, also contains functions to
 * perform calculations
 * 
 * @see <p>
 *      <a href="..\PNMLSchema\index.html">PNML - Petri-Net XMLSchema
 *      (stNet.xsd)</a>
 * @see </p>
 *      <p>
 *      <a href="uml\DataLayer.png">DataLayer UML</a>
 *      </p>
 * @version 1.0
 * @author James D Bloom
 * 
 * @author David Patterson Jan 2, 2006: Changed the fireRandomTransition method
 *         to give precedence to immediate transitions.
 * 
 * @author Edwin Chung added a boolean attribute to each matrix generated to
 *         prevent them from being created again when they have not been changed
 *         (6th Feb 2007)
 * 
 * @author Ben Kirby Feb 10, 2007: Removed savePNML method and the
 *         createPlaceElement, createAnnotationElement, createArcElement,
 *         createArcPoint, createTransitionElement methods it uses to a separate
 *         DataLayerWriter class, as part of refactoring to remove XML related
 *         actions from the DataLayer class.
 * 
 * @author Ben Kirby Feb 10, 2007: Split loadPNML into two bits. All XML work
 *         (Files, transformers, documents) is done in new PNMLTransformer
 *         class. The calls to actually populate a DataLayer object with the
 *         info contained in the PNML document have been moved to a
 *         createFromPNML method. The DataLayer constructor which previously
 *         used loadPNML has been changed to reflect these modifications. Also
 *         moved getDOM methods to PNMLTranformer class, as getDom is XML
 *         related. Removed getDom() (no arguments) completely as this is not
 *         called anywhere in the application.
 * 
 * @author Will Master Feb 13 2007: Added methods getPlacesCount and
 *         getTransitionsCount to avoid needlessly copying place and transition
 *         arrayLists.
 * 
 * @author Edwin Chung 15th Mar 2007: modified the createFromPNML function so
 *         that DataLayer objects can be created outside GUI
 * 
 * @author Dave Patterson 24 April 2007: Modified the fireRandomTransition
 *         method so it is quicker when there is only one transition to fire
 *         (just fire it, don't get a random variable first). Also, throw a
 *         RuntimeException if a rate less than 1 is detected. The current code
 *         uses the rate as a weight, and a rate such as 0.5 leads to a
 *         condition like that of bug 1699546 where no transition is available
 *         to fire.
 * 
 * @author Dave Patterson 10 May 2007: Modified the fireRandomTransitino method
 *         so it now properly handles fractional weights. There is no
 *         RuntimeException thrown now. The code for timed transitions uses the
 *         same logic, but will soon be changed to use exponentially distributed
 *         times where fractional rates are valid.
 * 
 * @author Barry Kearns August 2007: Added clone functionality and storage of
 *         state groups.
 * 
 **/
public class DataLayer extends Observable implements Cloneable {

	/** PNML File Name */
	public String pnmlName = null;
	/** List containing all the Place objects in the Petri-Net */
	private ArrayList<Place> placesArray = null;
	/** ArrayList containing all the Transition objects in the Petri-Net */
	private ArrayList<Transition> transitionsArray = null;
	/** ArrayList containing all the Arc objects in the Petri-Net */
	private ArrayList<Arc> arcsArray = null;

	/** ArrayList containing all the Arc objects in the Petri-Net */
	private ArrayList<InhibitorArc> inhibitorsArray = null;

	/**
	 * ArrayList for net-level label objects (as opposed to element-level
	 * labels).
	 */
	private ArrayList<AnnotationNote> labelsArray = null;

	/**
	 * An ArrayList used to point to either the Arc, Place or Transition
	 * ArrayLists when these ArrayLists are being update
	 */
	private ArrayList changeArrayList = null;

	/** Used to determine whether the matrixes have been modified */
	static boolean initialMarkingVectorChanged = true;

	static boolean currentMarkingVectorChanged = true;

	/**
	 * Hashtable which maps PlaceTransitionObjects to their list of connected
	 * arcs
	 */
	private Hashtable<PlaceTransitionObject, ArrayList<TimedOutputArcComponent>> arcsMap = null;

	/**
	 * Hashtable which maps PlaceTransitionObjects to their list of connected
	 * arcs
	 */
	private Hashtable<PlaceTransitionObject, ArrayList<InhibitorArc>> inhibitorsMap = null;
	private Hashtable<PlaceTransitionObject, ArrayList<TimedInhibitorArcComponent>> tapnInhibitorsMap = null;

	private NetType type = NetType.TAPN;

	/**
	 * Create empty Petri-Net object
	 */
	public DataLayer() {
		initializeMatrices();
	}

	/**
	 * Initialize Arrays
	 */
	private void initializeMatrices() {

		placesArray = new ArrayList<Place>();
		transitionsArray = new ArrayList<Transition>();
		arcsArray = new ArrayList<Arc>();
		inhibitorsArray = new ArrayList<InhibitorArc>();
		// tapnInhibitorsArray = new ArrayList();
		labelsArray = new ArrayList<AnnotationNote>();

		// may as well do the hashtable here as well
		arcsMap = new Hashtable<PlaceTransitionObject, ArrayList<TimedOutputArcComponent>>();
		inhibitorsMap = new Hashtable<PlaceTransitionObject, ArrayList<InhibitorArc>>();
		tapnInhibitorsMap = new Hashtable<PlaceTransitionObject, ArrayList<TimedInhibitorArcComponent>>();
	}

	/**
	 * Add placeInput to the back of the Place ArrayList All observers are
	 * notified of this change (Model-View Architecture)
	 * 
	 * @param placeInput
	 *            Place Object to add
	 */
	public void addPlace(Place placeInput) {
		Require.that(placeInput != null, "input place was null");

		placesArray.add(placeInput);
		setChanged();
		notifyObservers(placeInput);
	}

	/**
	 * Add labelInput to the back of the AnnotationNote ArrayList All observers
	 * are notified of this change (Model-View Architecture)
	 * 
	 * @param labelInput
	 *            AnnotationNote Object to add
	 */
	public void addAnnotation(AnnotationNote labelInput) {
		labelsArray.add(labelInput);
		setChanged();
		notifyObservers(labelInput);
	}

	/**
	 * Add transitionInput to back of the Transition ArrayList All observers are
	 * notified of this change (Model-View Architecture)
	 * 
	 * @param transitionInput
	 *            Transition Object to add
	 */
	public void addTransition(Transition transitionInput) {
		Require.that(transitionInput != null, "input transition was null");
		
		transitionsArray.add(transitionInput);
		setChanged();
		notifyObservers(transitionInput);
	}

	/**
	 * Add arcInput to back of the Arc ArrayList All observers are notified of
	 * this change (Model-View Architecture)
	 * 
	 * @param arcInput
	 *            Arc Object to add
	 */
	public void addArc(TimedOutputArcComponent arcInput) {
		boolean unique = true;

		// Check if the arcs have a valid source and target
		if (arcInput.getSource() == null || arcInput.getTarget() == null) {
			System.err.println(("Cant add arc" + arcInput + " where source: "
					+ arcInput.getSource() + " or target: "
					+ arcInput.getTarget() + " is null"));
			return;
		}

		if (arcInput != null) {
			if (arcInput.getId() != null && arcInput.getId().length() > 0) {
				for (int i = 0; i < arcsArray.size(); i++) {
					if (arcInput.getId().equals((arcsArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (arcsArray != null && arcsArray.size() > 0) {
					int no = arcsArray.size();
					do {
						for (int i = 0; i < arcsArray.size(); i++) {
							id = "A" + no;
							if (arcsArray.get(i) != null) {
								if (id.equals((arcsArray.get(i)).getId())) {
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
				if (id != null) {
					arcInput.setId(id);
				} else {
					arcInput.setId("error");
				}
			}

			// Transportarc fix boddy
			if (arcInput instanceof TransportArcComponent) {
				TransportArcComponent tmp = (TransportArcComponent) arcInput;
				PlaceTransitionObject first = tmp.getSource();

				if (tmp.getConnectedTo() == null) {
					if (first instanceof TimedPlaceComponent) {

						for (Object o : tmp.getTarget().getPostset()) {

							if (o instanceof TransportArcComponent) {
								if (tmp.getGroupNr() == ((TransportArcComponent) o)
										.getGroupNr()) {
									// Found partner

									tmp
											.setConnectedTo(((TransportArcComponent) o));
									((TransportArcComponent) o)
											.setConnectedTo(tmp);

									break;
								}
							}

						}

					} else {
						// First is TimedTransition
						tmp = (TransportArcComponent) arcInput;

						for (Object o : tmp.getSource().getPreset()) {

							if (o instanceof TransportArcComponent) {
								if (tmp.getGroupNr() == ((TransportArcComponent) o)
										.getGroupNr()) {
									// Found partner

									tmp
											.setConnectedTo(((TransportArcComponent) o));
									((TransportArcComponent) o)
											.setConnectedTo(tmp);

									break;
								}
							}

						}

					}
				}

			}

			arcsArray.add(arcInput);
			addArcToArcsMap(arcInput);

			setChanged();

			// notifyObservers(arcInput.getBounds());
			notifyObservers(arcInput);
		}
	}

	public void addTransportArc(TransportArcComponent transportArc) {
		arcsArray.add(transportArc);
		addArcToArcsMap(transportArc);
		setChanged();
		notifyObservers();
	}

	public void addArc(TimedInhibitorArcComponent inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null
					&& inhibitorArcInput.getId().length() > 0) {
				for (int i = 0; i < arcsArray.size(); i++) {
					if (inhibitorArcInput.getId().equals(
							(arcsArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (arcsArray != null && arcsArray.size() > 0) {
					int no = arcsArray.size();
					do {
						for (int i = 0; i < arcsArray.size(); i++) {
							id = "I" + no;
							if (arcsArray.get(i) != null) {
								if (id.equals((arcsArray.get(i)).getId())) {
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
				if (id != null) {
					inhibitorArcInput.setId(id);
				} else {
					inhibitorArcInput.setId("error");
				}
			}

			arcsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

			setChanged();

			// notifyObservers(arcInput.getBounds());
			notifyObservers(inhibitorArcInput);
		}
	}

	/**
	 * Add inhibitorArcInput to back of the InhibitorArc ArrayList All observers
	 * are notified of this change (Model-View Architecture)
	 * 
	 * @param arcInput
	 *            Arc Object to add
	 */
	public void addArc(InhibitorArc inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null
					&& inhibitorArcInput.getId().length() > 0) {
				for (int i = 0; i < inhibitorsArray.size(); i++) {
					if (inhibitorArcInput.getId().equals(
							((Arc) inhibitorsArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (inhibitorsArray != null && inhibitorsArray.size() > 0) {
					int no = inhibitorsArray.size();
					do {
						for (int i = 0; i < inhibitorsArray.size(); i++) {
							id = "I" + no;
							if (inhibitorsArray.get(i) != null) {
								if (id.equals(((Arc) inhibitorsArray.get(i))
										.getId())) {
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
				if (id != null) {
					inhibitorArcInput.setId(id);
				} else {
					inhibitorArcInput.setId("error");
				}
			}
			inhibitorsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

			setChanged();

			// notifyObservers(arcInput.getBounds());
			notifyObservers(inhibitorArcInput);
		}
	}

	/**
	 * Update the arcsMap hashtable to reflect the new arc
	 * 
	 * @param arcInput
	 *            New Arc
	 * */
	private void addArcToArcsMap(TimedOutputArcComponent arcInput) {
		// now we want to add the arc to the list of arcs for it's source and
		// target
		PlaceTransitionObject source = arcInput.getSource();
		PlaceTransitionObject target = arcInput.getTarget();
		ArrayList<TimedOutputArcComponent> newList = null;

		if (source != null) {
			// Pete: Place/Transitions now always moveable
			// source.setMovable(false);
			if (arcsMap.get(source) != null) {
				// System.out.println("adding arc to existing list");
				arcsMap.get(source).add(arcInput);
			} else {
				// System.out.println("creating new arc list");
				newList = new ArrayList<TimedOutputArcComponent>();
				newList.add(arcInput);
				arcsMap.put(source, newList);
			}
		}

		if (target != null) {
			// Pete: Place/Transitions now always moveable
			// target.setMovable(false);
			if (arcsMap.get(target) != null) {
				// System.out.println("adding arc to existing list2");
				arcsMap.get(target).add(arcInput);
			} else {
				// System.out.println("creating new arc list2");
				newList = new ArrayList<TimedOutputArcComponent>();
				newList.add(arcInput);
				arcsMap.put(target, newList);
			}
		}
		// System.out.println("arcsMap size: " + arcsMap.size());
	}

	/**
	 * Update the inhibitorsMap hashtable to reflect the new inhibitor arc
	 * 
	 * @param arcInput
	 *            New Arc
	 */
	private void addInhibitorArcToInhibitorsMap(
			TimedInhibitorArcComponent inhibitorArcInput) {
		// now we want to add the inhibitor arc to the list of inhibitor arcs
		// for
		// it's source and target
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
		// System.out.println("inhibitorsMap size: " + inhibitorsMap.size());
	}

	/**
	 * Update the inhibitorsMap hashtable to reflect the new inhibitor arc
	 * 
	 * @param arcInput
	 *            New Arc
	 */
	private void addInhibitorArcToInhibitorsMap(InhibitorArc inhibitorArcInput) {
		// now we want to add the inhibitor arc to the list of inhibitor arcs
		// for
		// it's source and target
		PlaceTransitionObject source = inhibitorArcInput.getSource();
		PlaceTransitionObject target = inhibitorArcInput.getTarget();
		ArrayList<InhibitorArc> newList = null;

		if (source != null) {
			if (inhibitorsMap.get(source) != null) {
				inhibitorsMap.get(source).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<InhibitorArc>();
				newList.add(inhibitorArcInput);
				inhibitorsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (inhibitorsMap.get(target) != null) {
				inhibitorsMap.get(target).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<InhibitorArc>();
				newList.add(inhibitorArcInput);
				inhibitorsMap.put(target, newList);
			}
		}
		// System.out.println("inhibitorsMap size: " + inhibitorsMap.size());
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
		if (setPetriNetObjectArrayList(pnObject)) {
			if (pnObject instanceof TimedOutputArcComponent) {
				addArc((TimedOutputArcComponent) pnObject);
			} else if (pnObject instanceof InhibitorArc) {
				addArc((InhibitorArc) pnObject);
			} else if (pnObject instanceof TimedInhibitorArcComponent) {
				addArc((TimedInhibitorArcComponent) pnObject);
			} else if (pnObject instanceof Place) {
				addPlace((Place) pnObject);
			} else if (pnObject instanceof Transition) {
				addTransition((Transition) pnObject);
			} else if (pnObject instanceof AnnotationNote) {
				addAnnotation((AnnotationNote)pnObject);				
			} else { // arrows, other labels.
				changeArrayList.add(pnObject);
				setChanged();

				notifyObservers(pnObject);
			}
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
		boolean didSomething = false;
		ArrayList<?> attachedArcs = null;

		try {
			if (setPetriNetObjectArrayList(pnObject)) {
				didSomething = changeArrayList.remove(pnObject);
				// we want to remove all attached arcs also
				if (pnObject instanceof PlaceTransitionObject) {
					if (arcsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are
						// removing
						attachedArcs = arcsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						// for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--) {
							try {
								((Arc) attachedArcs.get(i)).delete();
							} catch (IndexOutOfBoundsException e) {
								// XXX - this is a hack
								// This is OK, it just means that the transport
								// arc
								// already has been removed

							}

						}
						arcsMap.remove(pnObject);
					}

					if (inhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are
						// removing
						attachedArcs = inhibitorsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						// for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--) {
							((Arc) attachedArcs.get(i)).delete();
						}
						inhibitorsMap.remove(pnObject);
					}
					if (tapnInhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are
						// removing
						attachedArcs = tapnInhibitorsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						// for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--) {
							((Arc) attachedArcs.get(i)).delete();
						}
						tapnInhibitorsMap.remove(pnObject);
					}
				} else if (pnObject instanceof TimedOutputArcComponent) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc) pnObject)
							.getSource();

					if (attached != null) {
						ArrayList<TimedOutputArcComponent> a = arcsMap
								.get(attached);
						if (a != null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
							attached.updateConnected();
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}

					attached = ((Arc) pnObject).getTarget();
					if (attached != null) {
						if (arcsMap.get(attached) != null) { // causing null
																// pointer
																// exceptions
																// (!)
							arcsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
							attached.updateConnected();
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}
				} else if (pnObject instanceof InhibitorArc) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc) pnObject)
							.getSource();

					if (attached != null) {
						ArrayList<InhibitorArc> a = inhibitorsMap.get(attached);
						if (a != null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}

					attached = ((Arc) pnObject).getTarget();

					if (attached != null) {
						if (inhibitorsMap.get(attached) != null) { // causing
																	// null
																	// pointer
																	// exceptions
																	// (!)
							inhibitorsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}
				} else if (pnObject instanceof TimedInhibitorArcComponent) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc) pnObject)
							.getSource();

					if (attached != null) {
						ArrayList<TimedInhibitorArcComponent> a = tapnInhibitorsMap
								.get(attached);
						if (a != null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}

					attached = ((Arc) pnObject).getTarget();

					if (attached != null) {
						if (tapnInhibitorsMap.get(attached) != null) { // causing
																		// null
																		// pointer
																		// exceptions
																		// (!)
							tapnInhibitorsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc) pnObject);
						if (attached instanceof Transition) {
							((Transition) attached)
									.removeArcCompareObject((Arc) pnObject);
						}
						// attached.updateConnected(); //causing null pointer
						// exceptions (?)
					}
				}

				if (didSomething) {
					setChanged();

					// notifyObservers(pnObject.getBounds());
					notifyObservers(pnObject);
				}
			}
		} catch (NullPointerException npe) {
			System.out.println("NullPointerException [debug]\n"
					+ npe.getMessage());
			throw npe;
		}
		// we reset to null so that the wrong ArrayList can't get added to
		changeArrayList = null;
	}

	/**
	 * Returns an iterator for the transitions array. Used by Animator.class to
	 * set all enabled transitions to highlighted
	 */
	public Iterator<Transition> returnTransitions() {
		return transitionsArray.iterator();
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
		} else if (pnObject instanceof InhibitorArc) {
			changeArrayList = inhibitorsArray;
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
		ArrayList<PetriNetObject> all = new ArrayList<PetriNetObject>(
				placesArray);
		all.addAll(transitionsArray);
		all.addAll(arcsArray);
		all.addAll(labelsArray);

		return all;
	}

	/**
	 * Fire a random transition, takes rate (probability) of Transitions into
	 * account
	 */
	// public Transition fireRandomTransition() {
	//
	// setEnabledTransitions();
	// // All the enabled transitions are of the same type:
	// // a) all are immediate transitions; or
	// // b) all are timed transitions.
	//
	// ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
	//
	// for (int i = 0; i < transitionsArray.size(); i++) {
	// Transition transition = (Transition)transitionsArray.get(i);
	// if (transition.isEnabled()) {
	// enabledTransitions.add(transition);
	// }
	// }
	//
	// // if there is only one enabled transition, return this transition
	// if (enabledTransitions.size() == 1) {
	// return enabledTransitions.get(0);
	// }
	//
	// int random = randomNumber.nextInt(enabledTransitions.size());
	//
	// // no enabled transition found, so no transition can be fired
	// return enabledTransitions.get(random);
	// }

	// public void setEnabledTransitions(){
	// for ( Transition t : getTransitions() ){
	//			
	// // boolean isEnabled = false;
	// //
	// // if(!isUsingColors()){
	// // isEnabled = isTransitionEnabledNonColored(t);
	// // }else{
	// // isEnabled = isTransitionEnabledColored(t);
	// // }
	// // t.setEnabled(isEnabled);
	// setChanged();
	// notifyObservers(t);
	// }
	// }

	// private boolean isTransitionEnabledColored(Transition t) {
	// boolean enabled = true;
	// for(Arc arc : t.getPreset()){
	// boolean arcEnabled = false;
	// ColoredTimedPlace inputPlace = (ColoredTimedPlace)arc.getSource();
	//
	// for(ColoredToken token : inputPlace.getColoredTokens()){
	// if(arc instanceof ColoredInputArc){
	// arcEnabled = arcEnabled || ((ColoredInputArc)arc).satisfiesGuard(token);
	// }else if(arc instanceof ColoredTransportArc){
	// boolean guardSatisfied =
	// ((ColoredTransportArc)arc).satisfiesGuard(token);
	// boolean targetInvariantSatisfied =
	// ((ColoredTransportArc)arc).satisfiesTargetInvariant(token);
	//
	// arcEnabled = arcEnabled || (guardSatisfied && targetInvariantSatisfied);
	// }else if(arc instanceof ColoredInhibitorArc){
	// arcEnabled = arcEnabled ||
	// ((ColoredInhibitorArc)arc).satisfiesGuard(token);
	// }
	//
	// if(arcEnabled){
	// break;
	// }
	// }
	//
	// enabled = enabled && arcEnabled;
	// }
	//
	// for(Arc arc : t.getPostset()){
	// if(arc instanceof ColoredOutputArc){
	// int value = ((ColoredOutputArc)arc).getOutputValue().getValue();
	// ColorSet colorInvariant =
	// ((ColoredTimedPlace)arc.getTarget()).getColorInvariant();
	// enabled = enabled && colorInvariant.contains(value);
	//
	// }
	// }
	//
	// return enabled;
	// }
	//
	//
	// private boolean isTransitionEnabledNonColored(Transition t) {
	// boolean isEnabled = true;
	//
	// Collection<Arc> presetArcs = t.getPreset();
	// for ( Arc a : presetArcs ){
	//
	// Place p = (Place)a.getSource();
	// if (p instanceof TimedPlaceComponent){
	//
	// boolean ageIsSatisfied;
	//
	// if(a instanceof TimedInhibitorArcComponent)
	// ageIsSatisfied = true;
	// else
	// ageIsSatisfied = false;
	//
	//
	// if (p.currentMarking > 0){
	//
	// for ( BigDecimal token : ((TimedPlaceComponent)p).getTokens() ){
	// if(a instanceof TimedInhibitorArcComponent)
	// {
	// if(!((TimedInputArcComponent)a).satisfiesGuard(token))
	// {
	// ageIsSatisfied = false;
	// break;
	// }
	//
	//
	// }
	// else
	// {
	// if ( ((TimedInputArcComponent)a).satisfiesGuard(token) ){
	//
	// //make sure no invariants are violated
	// if (a instanceof TransportArcComponent){
	// for ( Arc postsetArc : (LinkedList<Arc>)t.getPostset() ){
	// if (postsetArc instanceof TransportArcComponent){
	// if ( ((TransportArcComponent) postsetArc).getGroupNr() ==
	// ((TransportArcComponent)a).getGroupNr()){
	// if (
	// ((TimedPlaceComponent)postsetArc.getTarget()).satisfiesInvariant(token)
	// ){
	// ageIsSatisfied = true;
	// break;
	// }
	// }
	// }
	// }
	// //invariants are not violated, if it is not a transport arc
	// }else {
	// ageIsSatisfied = true;
	// break;
	// }
	// }
	// }
	// }
	// }
	//
	// isEnabled = ageIsSatisfied;
	//
	// if (!isEnabled){
	// break;
	// }
	//
	// }else {
	// //p should always be a TimedPlace unless we have introduced hybrid nets
	// }
	// }
	// return isEnabled;
	// }

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

	/**
	 * Get an List of all the InhibitorArc objects in the Petri-Net
	 * 
	 * @return An List of all the InhibitorArc objects
	 */
	public InhibitorArc[] getInhibitors() {
		InhibitorArc[] returnArray = new InhibitorArc[inhibitorsArray.size()];

		for (int i = 0; i < inhibitorsArray.size(); i++) {
			returnArray[i] = inhibitorsArray.get(i);
		}
		return returnArray;
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

		if (transitionsArray != null) {
			if (transitionID != null) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if (transitionID.equalsIgnoreCase((transitionsArray.get(i))
							.getId())) {
						returnTransition = transitionsArray.get(i);
					}
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

		if (transitionsArray != null) {
			if (transitionName != null) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if (transitionName.equalsIgnoreCase((transitionsArray
							.get(i)).getName())) {
						returnTransition = transitionsArray.get(i);
					}
				}
			}
		}
		return returnTransition;
	}

	public Transition getTransitionByNameIgnoreGiven(Transition ignore,
			String transitionName) {
		Transition returnTransition = null;

		if (transitionsArray != null) {
			if (transitionName != null) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if (!transitionsArray.get(i).equals(ignore)) {
						if (transitionName.equalsIgnoreCase((transitionsArray
								.get(i)).getName())) {
							returnTransition = transitionsArray.get(i);
						}
					}
				}
			}
		}
		return returnTransition;
	}

	/**
	 * Return the Place called placeName from the Petri-Net
	 * 
	 * @param placeId
	 *            ID of Place object to return
	 * @return The first Place object found with id equal to placeId
	 */
	public Place getPlaceById(String placeID) {
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeID != null) {
				for (int i = 0; i < placesArray.size(); i++) {
					if (placeID.equalsIgnoreCase((placesArray.get(i)).getId())) {
						returnPlace = placesArray.get(i);
					}
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

		if (placesArray != null) {
			if (placeName != null) {
				for (int i = 0; i < placesArray.size(); i++) {
					if (placeName.equalsIgnoreCase((placesArray.get(i))
							.getName())) {
						returnPlace = placesArray.get(i);
					}
				}
			}
		}
		return returnPlace;
	}

	public Place getPlaceByNameIgnoreGiven(Place ignore, String placeName) {
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeName != null) {
				for (int i = 0; i < placesArray.size(); i++) {
					if (!placesArray.get(i).equals(ignore)) {
						if (placeName.equalsIgnoreCase((placesArray.get(i))
								.getName())) {
							returnPlace = placesArray.get(i);
						}
					}
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

		return null;
	}

	public boolean hasTAPNInhibitorArcs() { // TODO: Fix this to make it faster
		for (Arc arc : arcsArray) {
			if (arc instanceof TimedInhibitorArcComponent) {
				return true;
			}
		}
		return false;
	}

	public NetType netType() {
		return type;
	}

	public void setNetType(NetType type) {
		this.type = type;
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
			t.repaint();
		}

		for (Arc arc : arcsArray) {
			arc.updateWeightLabel(displayConstantNames);
			arc.repaint();
		}

		for (InhibitorArc arc : inhibitorsArray) {
			arc.updateWeightLabel(displayConstantNames);
			arc.repaint();
		}

	}
}
