package pipe.dataLayer;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredInterval;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.ColoredTimeInvariant;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.dataLayer.colors.Preserve;
import pipe.exception.InvariantViolatedAnimationException;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.undo.UndoableEdit;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.translations.ReductionOption;


/**
 * <b>DataLayer</b> - Encapsulates entire Petri-Net, also contains functions to 
 * perform calculations
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema 
 * (stNet.xsd)</a>
 * @see </p><p><a href="uml\DataLayer.png">DataLayer UML</a></p>
 * @version 1.0
 * @author James D Bloom
 *
 * @author David Patterson Jan 2, 2006: Changed the fireRandomTransition 
 * method to give precedence to immediate transitions.
 * 
 * @author Edwin Chung added a boolean attribute to each matrix generated to
 * prevent them from being created again when they have not been changed 
 * (6th Feb 2007)
 * 
 * @author Ben Kirby Feb 10, 2007: Removed savePNML method and the 
 * createPlaceElement, createAnnotationElement, createArcElement, createArcPoint,
 * createTransitionElement methods it uses to a separate DataLayerWriter class, 
 * as part of refactoring to remove XML related actions from the DataLayer class.
 * 
 * @author Ben Kirby Feb 10, 2007: Split loadPNML into two bits. All XML work 
 * (Files, transformers, documents) is done in new PNMLTransformer class. The 
 * calls to actually populate a DataLayer object with the info contained in the 
 * PNML document have been moved to a createFromPNML method. The DataLayer 
 * constructor which previously used loadPNML has been changed to reflect 
 * these modifications. Also moved getDOM methods to PNMLTranformer class, as 
 * getDom is XML related. Removed getDom() (no arguments) completely as this 
 * is not called anywhere in the application. 
 *
 * @author Will Master Feb 13 2007: Added methods getPlacesCount and 
 * getTransitionsCount to avoid needlessly copying place and transition 
 * arrayLists.
 * 
 * @author Edwin Chung 15th Mar 2007: modified the createFromPNML function so 
 * that DataLayer objects can be created outside GUI
 *
 * @author Dave Patterson 24 April 2007: Modified the fireRandomTransition 
 * method so it is quicker when there is only one transition to fire (just fire 
 * it, don't get a random variable first). Also, throw a RuntimeException if a 
 * rate less than 1 is detected. The current code uses the rate as a weight, 
 * and a rate such as 0.5 leads to a condition like that of bug 1699546 where no
 * transition is available to fire.
 * 
 * @author Dave Patterson 10 May 2007: Modified the fireRandomTransitino method
 * so it now properly handles fractional weights. There is no RuntimeException
 * thrown now. The code for timed transitions uses the same logic, but will soon
 * be changed to use exponentially distributed times where fractional rates 
 * are valid.
 *
 * @author Barry Kearns August 2007: Added clone functionality and storage of
 * state groups.
 * 
 **/
public class DataLayer 
extends Observable 
implements Cloneable {

	private static Random randomNumber = new Random(); // Random number generator

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

	/** ArrayList for net-level label objects (as opposed to element-level labels).*/
	private ArrayList<AnnotationNote> labelsArray = null;

	/** An ArrayList used to point to either the Arc, Place or Transition 
	 * ArrayLists when these ArrayLists are being update */
	private ArrayList changeArrayList = null;

	/** Marking Vector Storage used during animation */
	private int[] markingVectorAnimationStorage = null;

	/** Used to determine whether the matrixes have been modified */
	static boolean initialMarkingVectorChanged = true;

	static boolean currentMarkingVectorChanged = true;

	/** Hashtable which maps PlaceTransitionObjects to their list of connected arcs */
	private Hashtable<PlaceTransitionObject, ArrayList<NormalArc>> arcsMap = null;

	/** Hashtable which maps PlaceTransitionObjects to their list of connected arcs */
	private Hashtable<PlaceTransitionObject, ArrayList<InhibitorArc>> inhibitorsMap = null;
	private Hashtable<PlaceTransitionObject, ArrayList<TAPNInhibitorArc>> tapnInhibitorsMap = null;

	private HashMap<Transition, HashMap<TransportArc, TransportArc> > transportArcMap;

	private HashMap<TimedPlace, ArrayList<BigDecimal>> placeMarkingStorageMap = null;

	private ArrayList<TAPNQuery> queries = null;
	private ConstantStore constants = new ConstantStore();
	private boolean useColors = true;

	private HashMap<ColoredTimedPlace, List<ColoredToken>> coloredPlaceMarkingStorageMap;

	/**
	 * Create Petri-Net object from PNML file with URI pnmlFileName
	 * @param pnmlFileName Name of PNML File
	 */
	public DataLayer(String pnmlFileName) {

		queries = new ArrayList<TAPNQuery>();
		initializeMatrices();
		PNMLTransformer transform = new PNMLTransformer();
		File temp = new File(pnmlFileName);
		pnmlName = temp.getName();
		createFromPNML(transform.transformPNML(pnmlFileName));
		transportArcMap = new HashMap<Transition, HashMap<TransportArc,TransportArc> >();

	}  


	/**
	 * Create Petri-Net object from pnmlFile
	 * @param pnmlFile PNML File
	 */
	public DataLayer(File pnmlFile) {
		this(pnmlFile.getAbsolutePath());
	}


	/**
	 * Create empty Petri-Net object
	 */
	public DataLayer() {
		initializeMatrices();
		queries = new ArrayList<TAPNQuery>();
	}


	/**
	 * Initialize Arrays
	 */
	private void initializeMatrices() {

		placesArray = new ArrayList<Place>();
		transitionsArray = new ArrayList<Transition>();
		arcsArray = new ArrayList<Arc>();
		inhibitorsArray = new ArrayList<InhibitorArc>();
		//tapnInhibitorsArray = new ArrayList();
		labelsArray = new ArrayList<AnnotationNote>();


		// may as well do the hashtable here as well
		arcsMap = new Hashtable<PlaceTransitionObject, ArrayList<NormalArc>>();
		inhibitorsMap = new Hashtable<PlaceTransitionObject, ArrayList<InhibitorArc>>();
		tapnInhibitorsMap = new Hashtable<PlaceTransitionObject, ArrayList<TAPNInhibitorArc>>();
	}


	/**
	 * Add placeInput to the back of the Place ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param placeInput Place Object to add
	 */
	private void addPlace(Place placeInput) {
		boolean unique = true;

		if (placeInput != null) {
			if (placeInput.getId() != null && placeInput.getId().length() > 0) {
				for (int i = 0; i < placesArray.size(); i++) {
					if (placeInput.getId().equals(((Place)placesArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (placesArray != null && placesArray.size() > 0) {
					int no = placesArray.size();
					//          id = "P" + no;
					do {
						//						System.out.println("in while loop"); //DBG
						for (int i = 0; i < placesArray.size(); i++) {
							id = "P" + no;
							if (placesArray.get(i) != null) {
								if (id.equals(((Place)placesArray.get(i)).getId())) {
									//									System.out.println("testing id: " + id); //DBG
									unique = false;
									no++;
								} else {
									unique = true;
								}
							}
						}
					} while(!unique);
				} else {
					id = "P0";
				}

				if (id != null) {
					placeInput.setId(id);
					if (getPlaceByName(placeInput.getName()) != null){
						boolean hasUniqueName = false;
						int nameID = 0;
						while (!hasUniqueName){
							if (getPlaceByName("P"+nameID) == null){
								placeInput.setName("P"+nameID);
								hasUniqueName = true;
							}else{
								nameID++;
							}
						}
					}
				} else {
					placeInput.setId("error");
				}
			}
			placesArray.add(placeInput);
			setChanged();
			// notifyObservers(placeInput.getBounds());
			notifyObservers(placeInput);
		}
	}


	/**
	 * Add labelInput to the back of the AnnotationNote ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param labelInput AnnotationNote Object to add
	 */
	private void addAnnotation(AnnotationNote labelInput) {
		labelsArray.add(labelInput);
		setChanged();
		notifyObservers(labelInput);
	}

	/**
	 * Add transitionInput to back of the Transition ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param transitionInput Transition Object to add
	 */
	private void addTransition(Transition transitionInput) {
		boolean unique = true;

		if (transitionInput != null) {
			if (transitionInput.getId() != null
					&& transitionInput.getId().length() > 0) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if (transitionInput.getId().equals(
							((Transition)transitionsArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (transitionsArray != null && transitionsArray.size() > 0) {
					int no = transitionsArray.size();
					do {
						// System.out.println("transition while loop");
						for (int i = 0; i < transitionsArray.size(); i++) {
							id = "T" + no;
							if (transitionsArray.get(i) != null) {
								if (id.equals(
										((Transition)transitionsArray.get(i)).getId())) {
									unique = false;
									no++;
								} else {
									unique = true;
								}
							}
						}
					} while(!unique);
				} else {
					id = "T0";
				}

				if(id != null) {
					transitionInput.setId(id);
					if (getTransitionByName(transitionInput.getName()) != null){
						boolean hasUniqueName = false;
						int nameID = 0;
						while (!hasUniqueName){
							if (getTransitionByName("T"+nameID) == null){
								transitionInput.setName("T"+nameID);
								hasUniqueName = true;
							}else{
								nameID++;
							}
						}
					}
				} else {
					transitionInput.setId("error");
				}
			}
			transitionsArray.add(transitionInput);
			setChanged();

			//    notifyObservers(transitionInput.getBounds());
			notifyObservers(transitionInput);
		}
	}


	/**
	 * Add arcInput to back of the Arc ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param arcInput Arc Object to add
	 */
	public void addArc(NormalArc arcInput) {
		boolean unique = true;

		//Check if the arcs have a valid source and target
		if (arcInput.getSource() == null || arcInput.getTarget() == null){
			System.err.println(("Cant add arc" + arcInput + " where source: " + 
					arcInput.getSource() + " or target: " + arcInput.getTarget() + " is null"));
			return;
		}

		if (arcInput != null) {
			if (arcInput.getId() != null && arcInput.getId().length() > 0) {
				for (int i = 0 ; i < arcsArray.size() ; i++) {
					if (arcInput.getId().equals(((Arc)arcsArray.get(i)).getId())) {
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
								if (id.equals(((Arc)arcsArray.get(i)).getId())) {
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
				if(id != null) {
					arcInput.setId(id);
				} else {
					arcInput.setId("error");
				}
			}

			//Transportarc fix boddy
			if (arcInput instanceof TransportArc){
				TransportArc tmp = (TransportArc)arcInput;
				PlaceTransitionObject first = tmp.getSource();

				if (tmp.getConnectedTo() == null){
					if (first instanceof TimedPlace){

						for (Object o : tmp.getTarget().getPostset()){

							if (o instanceof TransportArc){
								if (tmp.getGroupNr() == ((TransportArc)o).getGroupNr()){
									//Found partner

									tmp.setConnectedTo(((TransportArc)o));
									((TransportArc)o).setConnectedTo(tmp); 

									break;
								}
							}

						}

					} else{
						// First is TimedTransition
						tmp = (TransportArc)arcInput;

						for (Object o : tmp.getSource().getPreset()){

							if (o instanceof TransportArc){
								if (tmp.getGroupNr() == ((TransportArc)o).getGroupNr()){
									//Found partner

									tmp.setConnectedTo(((TransportArc)o));
									((TransportArc)o).setConnectedTo(tmp); 

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

			//notifyObservers(arcInput.getBounds());
			notifyObservers(arcInput);
		}
	}

	public void addArc(TAPNInhibitorArc inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null &&
					inhibitorArcInput.getId().length() > 0) {
				for (int i = 0 ; i < arcsArray.size() ; i++) {
					if (inhibitorArcInput.getId().equals(
							((Arc)arcsArray.get(i)).getId())) {
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
								if (id.equals(((Arc)arcsArray.get(i)).getId())) {
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
				if(id != null) {
					inhibitorArcInput.setId(id);
				} else {
					inhibitorArcInput.setId("error");
				}
			}

			arcsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

			setChanged();

			//notifyObservers(arcInput.getBounds());
			notifyObservers(inhibitorArcInput);
		}
	}
	/**
	 * Add inhibitorArcInput to back of the InhibitorArc ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param arcInput Arc Object to add
	 */
	public void addArc(InhibitorArc inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null &&
					inhibitorArcInput.getId().length() > 0) {
				for (int i = 0 ; i < inhibitorsArray.size() ; i++) {
					if (inhibitorArcInput.getId().equals(
							((Arc)inhibitorsArray.get(i)).getId())) {
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
								if (id.equals(((Arc)inhibitorsArray.get(i)).getId())) {
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
				if(id != null) {
					inhibitorArcInput.setId(id);
				} else {
					inhibitorArcInput.setId("error");
				}
			}
			inhibitorsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

			setChanged();

			//notifyObservers(arcInput.getBounds());
			notifyObservers(inhibitorArcInput);
		}
	}   

	/** Update the arcsMap hashtable to reflect the new arc
	 * @param arcInput New Arc
	 * */
	private void addArcToArcsMap(NormalArc arcInput) {
		// now we want to add the arc to the list of arcs for it's source and target
		PlaceTransitionObject source = arcInput.getSource();
		PlaceTransitionObject target = arcInput.getTarget();
		ArrayList<NormalArc> newList = null;

		if (source != null) {
			//			Pete: Place/Transitions now always moveable
			//			source.setMovable(false);
			if (arcsMap.get(source)!=null) {
				//				System.out.println("adding arc to existing list");
				arcsMap.get(source).add(arcInput);
			} else {
				//				System.out.println("creating new arc list");
				newList = new ArrayList<NormalArc>();
				newList.add(arcInput);
				arcsMap.put(source,newList);
			}
		}

		if (target != null) {
			//			Pete: Place/Transitions now always moveable
			//			target.setMovable(false);
			if (arcsMap.get(target)!=null) {
				//				System.out.println("adding arc to existing list2");
				arcsMap.get(target).add(arcInput);
			} else {
				//				System.out.println("creating new arc list2");
				newList = new ArrayList<NormalArc>();
				newList.add(arcInput);
				arcsMap.put(target,newList);
			}
		}
		//		System.out.println("arcsMap size: " + arcsMap.size());
	}

	/** 
	 * Update the inhibitorsMap hashtable to reflect the new inhibitor arc
	 * @param arcInput New Arc
	 */
	private void addInhibitorArcToInhibitorsMap(TAPNInhibitorArc inhibitorArcInput) {
		// now we want to add the inhibitor arc to the list of inhibitor arcs for
		// it's source and target
		PlaceTransitionObject source = inhibitorArcInput.getSource();
		PlaceTransitionObject target = inhibitorArcInput.getTarget();
		ArrayList<TAPNInhibitorArc> newList = null;

		if (source != null) {
			if (tapnInhibitorsMap.get(source) != null) {
				tapnInhibitorsMap.get(source).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<TAPNInhibitorArc>();
				newList.add(inhibitorArcInput);
				tapnInhibitorsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (tapnInhibitorsMap.get(target) != null) {
				tapnInhibitorsMap.get(target).add(inhibitorArcInput);
			} else {
				newList = new ArrayList<TAPNInhibitorArc>();
				newList.add(inhibitorArcInput);
				tapnInhibitorsMap.put(target, newList);
			} 
		}
		//		System.out.println("inhibitorsMap size: " + inhibitorsMap.size());
	}

	/** 
	 * Update the inhibitorsMap hashtable to reflect the new inhibitor arc
	 * @param arcInput New Arc
	 */
	private void addInhibitorArcToInhibitorsMap(InhibitorArc inhibitorArcInput) {
		// now we want to add the inhibitor arc to the list of inhibitor arcs for
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
		//		System.out.println("inhibitorsMap size: " + inhibitorsMap.size());
	}


	/**
	 * Add any PetriNetObject - the object will be added to the appropriate list.
	 * If the object passed in isn't a Transition, Place or Arc nothing will happen.
	 * All observers are notified of this change.
	 * @param pnObject The PetriNetObject to be added.
	 */
	public void addPetriNetObject(PetriNetObject pnObject) {
		if (setPetriNetObjectArrayList(pnObject)) {
			if (pnObject instanceof NormalArc) {
				addArcToArcsMap((NormalArc)pnObject);
				addArc((NormalArc)pnObject);
			} else if (pnObject instanceof InhibitorArc) {
				addInhibitorArcToInhibitorsMap((InhibitorArc)pnObject);
				addArc((InhibitorArc)pnObject);
			} else if (pnObject instanceof TAPNInhibitorArc) {
				addInhibitorArcToInhibitorsMap((TAPNInhibitorArc)pnObject);
				addArc((TAPNInhibitorArc)pnObject);
			} else if (pnObject instanceof Place) {
				addPlace((Place)pnObject);
				//			} else if (pnObject instanceof TAPNTransition){
				//				addTAPNTransition((TAPNTransition)pnObject);
			}else if (pnObject instanceof Transition) {
				addTransition((Transition)pnObject);
			} else if (pnObject instanceof AnnotationNote) {
				labelsArray.add((AnnotationNote)pnObject);
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
	 * @param pnObject The PetriNetObject to be removed.
	 */
	public void removePetriNetObject(PetriNetObject pnObject){
		boolean didSomething = false;
		ArrayList<?> attachedArcs = null;

		try{
			if (setPetriNetObjectArrayList(pnObject)) {
				didSomething = changeArrayList.remove(pnObject);
				// we want to remove all attached arcs also
				if (pnObject instanceof PlaceTransitionObject) {
					if(pnObject instanceof ColoredTimedPlace){
						((ColoredTimedPlace)pnObject).showAgeOfTokens(false);
					}

					if ( arcsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = arcsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						//for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--){
							try {
								((Arc)attachedArcs.get(i)).delete();
							} catch (IndexOutOfBoundsException e) {
								// XXX - this is a hack
								// This is OK, it just means that the transport arc
								// already has been removed

							}


						}
						arcsMap.remove(pnObject);
					}

					if ( inhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = inhibitorsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						//for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--){
							((Arc)attachedArcs.get(i)).delete();
						}
						inhibitorsMap.remove(pnObject);
					}
					if ( tapnInhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = tapnInhibitorsMap.get(pnObject);

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						//for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--){
							((Arc)attachedArcs.get(i)).delete();
						}
						tapnInhibitorsMap.remove(pnObject);
					}
				} else if (pnObject instanceof NormalArc) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc)pnObject).getSource();

					if (attached != null) {
						ArrayList<NormalArc> a = arcsMap.get(attached);
						if (a!=null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc)pnObject);
						if (attached instanceof Transition){
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
							attached.updateConnected();
						}
						//attached.updateConnected(); //causing null pointer exceptions (?)
					}

					attached = ((Arc)pnObject).getTarget();           
					if (attached != null) {
						if (arcsMap.get(attached) != null) { //causing null pointer exceptions (!)
							arcsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc)pnObject);
						if (attached instanceof Transition) {
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
							attached.updateConnected();
						}
						//						attached.updateConnected(); //causing null pointer exceptions (?)
					}
				} else if (pnObject instanceof InhibitorArc) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc)pnObject).getSource();

					if (attached != null) {
						ArrayList<InhibitorArc> a=inhibitorsMap.get(attached);
						if (a!=null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc)pnObject);
						if (attached instanceof Transition){
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
						}
						//attached.updateConnected(); //causing null pointer exceptions (?)
					}


					attached = ((Arc)pnObject).getTarget();

					if (attached != null) {
						if (inhibitorsMap.get(attached) != null) { //causing null pointer exceptions (!)
							inhibitorsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc)pnObject);
						if (attached instanceof Transition) {
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
						}
						//						attached.updateConnected(); //causing null pointer exceptions (?)
					}
				}else if (pnObject instanceof TAPNInhibitorArc) {

					// get source and target of the arc
					PlaceTransitionObject attached = ((Arc)pnObject).getSource();

					if (attached != null) {
						ArrayList<TAPNInhibitorArc> a=tapnInhibitorsMap.get(attached);
						if (a!=null) {
							a.remove(pnObject);
						}

						attached.removeFromArc((Arc)pnObject);
						if (attached instanceof Transition){
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
						}
						//attached.updateConnected(); //causing null pointer exceptions (?)
					}


					attached = ((Arc)pnObject).getTarget();

					if (attached != null) {
						if (tapnInhibitorsMap.get(attached) != null) { //causing null pointer exceptions (!)
							tapnInhibitorsMap.get(attached).remove(pnObject);
						}

						attached.removeToArc((Arc)pnObject);
						if (attached instanceof Transition) {
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
						}
						//						attached.updateConnected(); //causing null pointer exceptions (?)
					}
				}

				if (didSomething) {
					setChanged();

					// notifyObservers(pnObject.getBounds());
					notifyObservers(pnObject);
				}
			}
		} catch(NullPointerException npe){
			System.out.println("NullPointerException [debug]\n" + npe.getMessage());
			throw npe;
		}
		// we reset to null so that the wrong ArrayList can't get added to
		changeArrayList = null;
	}


	/**
	 * Returns an iterator for the transitions array.
	 * Used by Animator.class to set all enabled transitions to highlighted 
	 */
	public Iterator<Transition> returnTransitions(){
		return transitionsArray.iterator();
	}


	public boolean isConstantNameUsed(String name){
		return constants.isConstantNameUsed(name);
	}
	/**
	 * Sets an internal ArrayList according to the class of the object passed in.
	 * @param pnObject The pnObject in question.
	 * @return Returns True if the pnObject is of type Place, Transition or Arc
	 */
	private boolean setPetriNetObjectArrayList(PetriNetObject pnObject) {

		// determine appropriate ArrayList
		if (pnObject instanceof Transition) {
			changeArrayList = transitionsArray;
			return true;
		} else if (pnObject instanceof Place){
			changeArrayList = placesArray;
			return true;
		}
		else if (pnObject instanceof TAPNInhibitorArc) {
			changeArrayList = arcsArray;
			return true;
		}	
		else if (pnObject instanceof NormalArc) {
			changeArrayList = arcsArray;
			return true;
		} else if (pnObject instanceof InhibitorArc) {
			changeArrayList = inhibitorsArray;
			return true;
		}
		else if(pnObject instanceof AnnotationNote) {
			changeArrayList = labelsArray;
			return true;
		}    
		return false;
	}


	/**
	 * Returns an iterator of all PetriNetObjects - the order of these cannot be guaranteed.
	 * @return An iterator of all PetriNetObjects
	 */
	public Iterator<PetriNetObject> getPetriNetObjects(){
		ArrayList<PetriNetObject> all = new ArrayList<PetriNetObject>(placesArray);
		all.addAll(transitionsArray);
		all.addAll(arcsArray);
		all.addAll(labelsArray);

		return all.iterator();
	}


	public boolean hasPlaceTransitionObjects(){
		return (placesArray.size() + transitionsArray.size()) > 0;
	}


	/**
	 * Creates a Label object from a Label DOM Element
	 * @param inputLabelElement Input Label DOM Element
	 * @return Label Object
	 */
	private AnnotationNote createAnnotation (Element inputLabelElement) {
		int positionXInput = 0;
		int positionYInput = 0;
		int widthInput = 0;
		int heightInput = 0;
		String text = null;
		boolean borderInput = true;

		String positionXTempStorage = inputLabelElement.getAttribute("xPosition");
		String positionYTempStorage = inputLabelElement.getAttribute("yPosition");
		String widthTemp = inputLabelElement.getAttribute("w");
		String heightTemp = inputLabelElement.getAttribute("h");
		String textTempStorage = inputLabelElement.getAttribute("txt");
		String borderTemp = inputLabelElement.getAttribute("border");

		if (positionXTempStorage.length() > 0) {
			positionXInput = Integer.valueOf(positionXTempStorage).intValue() + 1;
		}

		if (positionYTempStorage.length() > 0){
			positionYInput = Integer.valueOf(positionYTempStorage).intValue() + 1;
		}

		if (widthTemp.length() > 0) {
			widthInput = Integer.valueOf(widthTemp).intValue() + 1;
		}

		if (heightTemp.length() > 0) {
			heightInput = Integer.valueOf(heightTemp).intValue() + 1;
		}

		if (borderTemp.length()>0) {
			borderInput = Boolean.valueOf(borderTemp).booleanValue();
		} else {
			borderInput = true;
		}

		if (textTempStorage.length() > 0) {
			text = textTempStorage;
		} else {
			text = "";
		}

		return new AnnotationNote(text, positionXInput, positionYInput, 
				widthInput, heightInput, borderInput);
	}   



	/**
	 * Creates a Transition object from a Transition DOM Element
	 * @param inputTransitionElement Input Transition DOM Element
	 * @return Transition Object
	 */
	private Transition createTransition(Element element){
		double positionXInput = 0;
		double positionYInput = 0;
		String idInput = null;
		String nameInput = null;
		double nameOffsetYInput = 0;
		double nameOffsetXInput = 0;
		boolean timedTransition;
		boolean infiniteServer;
		int angle = 0;
		int priority = 1;
		String positionXTempStorage = element.getAttribute("positionX");
		String positionYTempStorage = element.getAttribute("positionY");
		String idTempStorage = element.getAttribute("id");
		String nameTempStorage = element.getAttribute("name");
		String nameOffsetXTempStorage = element.getAttribute("nameOffsetX");
		String nameOffsetYTempStorage = element.getAttribute("nameOffsetY");
		String nameTimed = element.getAttribute("timed");
		String nameInfiniteServer = element.getAttribute("infiniteServer");
		String nameAngle = element.getAttribute("angle");
		String namePriority = element.getAttribute("priority");

		if (nameTimed.length() == 0) {
			timedTransition = false;
		} else if (nameTimed.length()== 5) {
			timedTransition = false;
		} else {
			timedTransition = true;
		}

		infiniteServer = !(nameInfiniteServer.length() == 0 ||
				nameInfiniteServer.length() == 5);

		if (positionXTempStorage.length() > 0) {
			positionXInput = Double.valueOf(positionXTempStorage).doubleValue() *
			(1) +
			(1);
		}
		if (positionYTempStorage.length() > 0) {
			positionYInput = Double.valueOf(positionYTempStorage).doubleValue() *
			(1) +
			(1);
		}

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idTempStorage.length() > 0) {
			idInput = idTempStorage;
		} else if (nameTempStorage.length() > 0) {
			idInput = nameTempStorage;
		}

		if (nameTempStorage.length() > 0) {
			nameInput = nameTempStorage;
		} else if (idTempStorage.length() > 0) {
			nameInput = idTempStorage;
		}

		if (nameOffsetXTempStorage.length() > 0) {
			nameOffsetXInput = Double.valueOf(nameOffsetXTempStorage).doubleValue();
		}

		if (nameOffsetYTempStorage.length() > 0) {
			nameOffsetYInput = Double.valueOf(nameOffsetYTempStorage).doubleValue();
		}

		if (nameAngle.length() > 0) {
			angle = Integer.valueOf(nameAngle).intValue();
		}

		if (namePriority.length() > 0) {
			priority = Integer.valueOf(namePriority).intValue();
		}

		Transition transition = null;
		{
			transition =  
				new TAPNTransition(positionXInput, positionYInput,     
						idInput, 
						nameInput, 
						nameOffsetXInput, nameOffsetYInput, 
						timedTransition, 
						infiniteServer,
						angle,
						priority);
		}

		return transition;
	}


	private Place createPlace(Element element){
		double positionXInput = 0;
		double positionYInput = 0;
		String idInput = null;
		String nameInput = null;
		double nameOffsetYInput = 0;
		double nameOffsetXInput = 0;
		int initialMarkingInput = 0;
		double markingOffsetXInput = 0;
		double markingOffsetYInput = 0;
		int capacityInput = 0;

		String positionXTempStorage = element.getAttribute("positionX");
		String positionYTempStorage = element.getAttribute("positionY");
		String idTempStorage = element.getAttribute("id");
		String nameTempStorage = element.getAttribute("name");
		String nameOffsetXTempStorage = element.getAttribute("nameOffsetX");
		String nameOffsetYTempStorage = element.getAttribute("nameOffsetY");
		String initialMarkingTempStorage = element.getAttribute("initialMarking");
		String markingOffsetXTempStorage = element.getAttribute("markingOffsetX");
		String markingOffsetYTempStorage = element.getAttribute("markingOffsetY");
		String capacityTempStorage = element.getAttribute("capacity");
		String invariantTempStorage = element.getAttribute("invariant");


		if (positionXTempStorage.length() > 0) {
			positionXInput = Double.valueOf(positionXTempStorage).doubleValue() + 1;
		}
		if (positionYTempStorage.length() > 0) {
			positionYInput = Double.valueOf(positionYTempStorage).doubleValue() + 1;
		}
		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idTempStorage.length() > 0) {
			idInput = idTempStorage;
		} else if (nameTempStorage.length() > 0) {
			idInput = nameTempStorage;
		}

		if (nameTempStorage.length() > 0) {
			nameInput = nameTempStorage;
		} else if (idTempStorage.length() > 0) {
			nameInput = idTempStorage;
		}

		if (nameOffsetYTempStorage.length() > 0) {
			nameOffsetXInput = Double.valueOf(nameOffsetXTempStorage).doubleValue();
		}
		if (nameOffsetXTempStorage.length() > 0) {
			nameOffsetYInput = Double.valueOf(nameOffsetYTempStorage).doubleValue();
		}

		if (initialMarkingTempStorage.length() > 0) {
			initialMarkingInput = Integer.valueOf(initialMarkingTempStorage).intValue();
		}
		if (markingOffsetXTempStorage.length() > 0) {
			markingOffsetXInput = Double.valueOf(markingOffsetXTempStorage).doubleValue();
		}
		if (markingOffsetYTempStorage.length() > 0) {
			markingOffsetYInput = Double.valueOf(markingOffsetYTempStorage).doubleValue();
		}

		if (capacityTempStorage.length() > 0) {
			capacityInput = Integer.valueOf(capacityTempStorage).intValue();
		}

		Place  place = null;

		if (invariantTempStorage == null || invariantTempStorage == ""){
			place = new Place(positionXInput, positionYInput,
					idInput,  
					nameInput, 
					nameOffsetXInput, nameOffsetYInput,
					initialMarkingInput,
					markingOffsetXInput, markingOffsetYInput,  
					capacityInput);

		} else {

			place = new TimedPlace(positionXInput, positionYInput,
					idInput,  
					nameInput, 
					nameOffsetXInput, nameOffsetYInput,
					initialMarkingInput,
					markingOffsetXInput, markingOffsetYInput,  
					capacityInput, invariantTempStorage);
		}

		return place;
	}


	/**
	 * Creates a Arc object from a Arc DOM Element
	 * @param inputArcElement Input Arc DOM Element
	 * @return Arc Object
	 */
	private Arc createArc(Element inputArcElement){
		String idInput = null;
		String sourceInput = null;
		String targetInput = null;
		double startX = 0;
		double startY = 0;
		boolean taggedArc;

		sourceInput = inputArcElement.getAttribute("source");
		targetInput = inputArcElement.getAttribute("target");
		String idTempStorage = inputArcElement.getAttribute("id");
		String sourceTempStorage = inputArcElement.getAttribute("source");
		String targetTempStorage = inputArcElement.getAttribute("target");
		String inscriptionTempStorage = inputArcElement.getAttribute("inscription");

		String taggedTempStorage = inputArcElement.getAttribute("tagged");
		//		String inscriptionOffsetXTempStorage = inputArcElement.getAttribute("inscriptionOffsetX");
		//		String inscriptionOffsetYTempStorage = inputArcElement.getAttribute("inscriptionOffsetY");

		taggedArc = !(taggedTempStorage.length() == 0 ||
				taggedTempStorage.length() == 5);

		if (idTempStorage.length() > 0) {
			idInput = idTempStorage;
		}
		if (sourceTempStorage.length() > 0) {
			sourceInput = sourceTempStorage;
		}
		if (targetTempStorage.length() > 0) {
			targetInput = targetTempStorage;
		}


		if (sourceInput.length() > 0) {
			if (getPlaceTransitionObject(sourceInput) != null) {
				//				System.out.println("PNMLDATA: sourceInput is not null");
				startX = getPlaceTransitionObject(sourceInput).getPositionX();
				startX += getPlaceTransitionObject(sourceInput).centreOffsetLeft();
				startY = getPlaceTransitionObject(sourceInput).getPositionY();
				startY += getPlaceTransitionObject(sourceInput).centreOffsetTop();
			}
		}
		if (targetInput.length() > 0) {
			if (getPlaceTransitionObject(targetInput) != null) {
				getPlaceTransitionObject(targetInput).getPositionX();
				getPlaceTransitionObject(targetInput).getPositionY();
			}
		}

		PlaceTransitionObject sourceIn = getPlaceTransitionObject(sourceInput);
		PlaceTransitionObject targetIn = getPlaceTransitionObject(targetInput);

		// add the insets and offset
		int aStartx = sourceIn.getX() + sourceIn.centreOffsetLeft();
		int aStarty = sourceIn.getY() + sourceIn.centreOffsetTop();

		int aEndx = targetIn.getX() + targetIn.centreOffsetLeft();
		int aEndy = targetIn.getY() + targetIn.centreOffsetTop();


		double _startx = aStartx;
		double _starty = aStarty;
		double _endx = aEndx;
		double _endy = aEndy;
		//TODO

		Arc tempArc;



		String type = "normal";  // default value
		NodeList nl = inputArcElement.getElementsByTagName("type");
		if (nl.getLength()>0) {
			type = ((Element)(nl.item(0))).getAttribute("type");
		}


		if (type.equals("inhibitor")) {
			tempArc = new InhibitorArc(_startx, _starty, 
					_endx, _endy, 
					sourceIn, 
					targetIn,       
					/*CB Joakim Byg - inscribtion is inserted as the arcs weight*/                                    
					Integer.valueOf(inscriptionTempStorage),
					/*EOC*/                                    
					idInput);
		} else if (type.equals("tapnInhibitor")){
			tempArc = new TAPNInhibitorArc(new TimedArc(new NormalArc(_startx, _starty, _endx, _endy, sourceIn, targetIn, 1, idInput, taggedArc)), (inscriptionTempStorage!=null ? inscriptionTempStorage : ""));
		} else {



			//XXX - cant check for if arc is timed, check pn-type instead
			if (type.equals("timed")){
				tempArc = new TimedArc(new NormalArc (_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						1,
						idInput,
						taggedArc), (inscriptionTempStorage!=null ? inscriptionTempStorage : ""));
			}else if (type.equals("transport")){
				String[] inscriptionSplit = {};
				if (inscriptionTempStorage.contains(":")){
					inscriptionSplit = inscriptionTempStorage.split(":");
				}
				boolean isInPreSet = false;
				if ( sourceIn instanceof Place ) {
					isInPreSet = true;
				}
				tempArc = new TransportArc( new TimedArc( new NormalArc(_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						1,
						idInput,
						taggedArc), inscriptionSplit[0]), Integer.parseInt(inscriptionSplit[1]), isInPreSet );
			}else {
				tempArc = new NormalArc(	_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						//inscribtion is inserted as the arcs weight    				  					
						Integer.valueOf(inscriptionTempStorage),
						idInput,
						taggedArc);
			}

		}

		getPlaceTransitionObject(sourceInput).addConnectFrom(tempArc);
		getPlaceTransitionObject(targetInput).addConnectTo(tempArc);

		//		**********************************************************************************
		//		The following section attempts to load and display arcpath details****************

		//NodeList nodelist = inputArcElement.getChildNodes();
		NodeList nodelist = inputArcElement.getElementsByTagName("arcpath");
		if (nodelist.getLength()>0) {
			tempArc.getArcPath().purgePathPoints();
			for (int i = 0; i < nodelist.getLength(); i++) {         
				Node node = nodelist.item(i);
				if(node instanceof Element) {
					Element element = (Element)node;
					if ("arcpath".equals(element.getNodeName())){
						String arcTempX = element.getAttribute("x");
						String arcTempY = element.getAttribute("y");
						String arcTempType = element.getAttribute("arcPointType");
						float arcPointX = Float.valueOf(arcTempX).floatValue();
						float arcPointY = Float.valueOf(arcTempY).floatValue();
						arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						boolean arcPointType = 
							Boolean.valueOf(arcTempType).booleanValue();
						tempArc.getArcPath().addPoint(arcPointX,arcPointY,arcPointType);
					}
				}
			}
		}

		//		Arc path creation ends here***************************************************************
		//		******************************************************************************************
		return tempArc;
	}


	/**
	 * Stores Current Marking
	 */
	public void storeState(){
		boolean isTAPN = true;
		if (isTAPN){
			if(!isUsingColors()){
				placeMarkingStorageMap = new HashMap<TimedPlace, ArrayList<BigDecimal>>();
				for (Place p : getPlaces()){
					placeMarkingStorageMap.put( (TimedPlace)p, (ArrayList<BigDecimal>)((TimedPlace)p).getTokens().clone() );
				}
			}else{
				coloredPlaceMarkingStorageMap = new HashMap<ColoredTimedPlace, List<ColoredToken>>();
				for(Place p : getPlaces()){
					ColoredTimedPlace ctp = (ColoredTimedPlace)p;
					coloredPlaceMarkingStorageMap.put(ctp, copyListOfColoredTokens(ctp.getColoredTokens()));
				}
			}
		}else {
			int placeSize = placesArray.size();
			markingVectorAnimationStorage = new int[placeSize];
			for (int placeNo = 0; placeNo < placeSize; placeNo++) {
				markingVectorAnimationStorage[placeNo] = 
					((Place)placesArray.get(placeNo)).getCurrentMarking();
			}
		}
	}

	private List<ColoredToken> copyListOfColoredTokens(
			List<ColoredToken> coloredTokens) {
		ArrayList<ColoredToken> list = new ArrayList<ColoredToken>(coloredTokens.size());
		for(ColoredToken token : coloredTokens){
			list.add(new ColoredToken(token));
		}
		return list;
	}


	public HashMap<TimedPlace, ArrayList<BigDecimal>> getCurrentMarking(){
		boolean isTAPN = true;

		HashMap<TimedPlace, ArrayList<BigDecimal>> toReturn = new HashMap<TimedPlace, ArrayList<BigDecimal>>();
		if (isTAPN){
			for (Place p : getPlaces()){
				toReturn.put( (TimedPlace)p, (ArrayList<BigDecimal>)((TimedPlace)p).getTokens().clone() );
			}
		}
		return toReturn;
	}


	/**
	 * Restores To previous Stored Marking
	 */
	public void restoreState(){
		boolean isTAPN = true;
		if(isTAPN){
			if(!isUsingColors()){
				if (placeMarkingStorageMap!=null){
					for (Place p : getPlaces()){
						ArrayList<BigDecimal> markingOfP = placeMarkingStorageMap.get((TimedPlace)p);
						p.setCurrentMarking( markingOfP.size() );
						((TimedPlace)p).setAgeOfTokens(markingOfP);
						setChanged();
						notifyObservers(p);

					}
				}
			}else{
				if(coloredPlaceMarkingStorageMap != null){
					for(Place p : getPlaces()){
						ColoredTimedPlace ctp = (ColoredTimedPlace)p;
						ctp.setColoredTokens(coloredPlaceMarkingStorageMap.get(ctp));
						setChanged();
						notifyObservers(p);

					}
				}
			}
		}else{
			if(markingVectorAnimationStorage != null) {
				int placeSize = placesArray.size();
				for (int placeNo = 0; placeNo < placeSize; placeNo++) {
					Place place = ((Place)placesArray.get(placeNo));
					if (place != null) {
						place.setCurrentMarking(markingVectorAnimationStorage[placeNo]);
						setChanged();
						notifyObservers(place);

					}
				}
			}
		}
	}


	/**
	 * Fire a specified transition, no affect if transtions not enabled
	 * @param transition Reference of specifiec Transition
	 *
	 * @author Kenneth Yrke Joergensen <kenneth@yrke.dk> Changed to handeling
	 * firing modes when working with TAPN.
	 */
	public FiringAction fireTransition(Transition transition) {
		FiringAction toReturn = null;

		// If it is a TAPN
		if (Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET){
			if(!isUsingColors()){
				toReturn = fireTransitionInNonColoredTAPN(transition);	
			}else{
				toReturn = fireTransitionInColoredTAPN(transition);
			}
		}

		return toReturn;
	}


	private FiringAction fireTransitionInColoredTAPN(
			Transition transition) {
		ColoredDiscreteFiringAction firingAction = new ColoredDiscreteFiringAction((TAPNTransition)transition);

		setEnabledTransitions();
		if(transition.isEnabled()){
			HashMap<Integer, ColoredToken> tokensConsumedByTransportArcs = new HashMap<Integer, ColoredToken>();

			for(Arc arc : transition.getPreset()){				
				if(arc instanceof ColoredTransportArc){
					ColoredTransportArc tarc = (ColoredTransportArc)arc;
					ArrayList<ColoredToken> possibleTokens = new ArrayList<ColoredToken>();
					ColoredTimedPlace source = (ColoredTimedPlace)arc.getSource();
					ColoredTimedPlace target = (ColoredTimedPlace)tarc.getConnectedTo().getTarget();

					for(ColoredToken token : source.getColoredTokens()){
						if(tarc.satisfiesGuard(token) && target.satisfiesInvariant(token)){
							possibleTokens.add(token);
						}
					}

					ColoredToken usedToken = CreateGui.getAnimator().firingmode.fire(possibleTokens);
					firingAction.addConsumedToken(source, usedToken);
					tokensConsumedByTransportArcs.put(tarc.getGroupNr(), usedToken);
					source.removeColoredToken(usedToken);
				}else if(arc instanceof ColoredInputArc){
					ColoredInputArc inputArc = (ColoredInputArc)arc;
					ArrayList<ColoredToken> possibleTokens = new ArrayList<ColoredToken>();
					ColoredTimedPlace source = (ColoredTimedPlace)arc.getSource();

					for(ColoredToken token : source.getColoredTokens()){
						if(inputArc.satisfiesGuard(token)){
							possibleTokens.add(token);
						}
					}

					ColoredToken usedToken = CreateGui.getAnimator().firingmode.fire(possibleTokens);
					firingAction.addConsumedToken(source, usedToken);
					source.removeColoredToken(usedToken);
				}
			}

			for(Arc arc : transition.getPostset()){
				if(arc instanceof ColoredTransportArc){
					ColoredTransportArc tarc = (ColoredTransportArc)arc;
					ColoredToken consumed = tokensConsumedByTransportArcs.get(tarc.getGroupNr());
					ColoredToken newToken = tarc.generateOutputToken(consumed);

					ColoredTimedPlace target = (ColoredTimedPlace)tarc.getTarget();
					firingAction.addProducedToken(target, newToken);
					target.addColoredToken(newToken);
				}else if(arc instanceof ColoredOutputArc){
					ColoredOutputArc outputArc = (ColoredOutputArc)arc;
					ColoredToken token = outputArc.generateOutputToken();
					ColoredTimedPlace target = (ColoredTimedPlace)outputArc.getTarget();

					firingAction.addProducedToken(target, token);
					target.addColoredToken(token);
				}
			}
		}		

		return firingAction;
	}


	private FiringAction fireTransitionInNonColoredTAPN(Transition transition) {
		DiscreetFiringAction toReturn = new DiscreetFiringAction(transition);
		if (transition != null){
			setEnabledTransitions();

			// Index:
			// Check transition is enables
			//Find the tokens valid to fire (check guard)
			// - If transportarc check if invariant is ok
			// Select the tokens to consume, based on firing stradegy
			// Consume the tokens and create the tokens

			if (transition.isEnabled()){
				HashMap<Integer, BigDecimal> tokensConsumedByTransportArcs = new HashMap<Integer, BigDecimal>();

				for (Arc a : (LinkedList<Arc>)transition.getPreset() ){



					if (a instanceof TransportArc){
						ArrayList<BigDecimal> eligableToken = new ArrayList<BigDecimal>();


						TimedPlace p = (TimedPlace)a.getSource();

						ArrayList<BigDecimal> tokensOfPlace = p.getTokens();					

						TimedPlace targetPlace = (TimedPlace)((TransportArc)a).getConnectedTo().getTarget();

						for (int i=0; i< tokensOfPlace.size(); i++){
							if ( ((TimedArc)a).satisfiesGuard(tokensOfPlace.get(i)) && targetPlace.satisfiesInvariant(tokensOfPlace.get(i))) {
								eligableToken.add(tokensOfPlace.get(i));
							}
						}	
						BigDecimal tokenToRemove = CreateGui.getAnimator().firingmode.fire(eligableToken);

						//							XXX  - This will break if two tokens from the same place is consumed
						toReturn.addConsumedToken(p, tokenToRemove);

						tokensConsumedByTransportArcs.put(((TransportArc) a).getGroupNr(), tokenToRemove);



						p.removeTokenofAge(tokenToRemove);
					}
					// if arc is an inhibitor arc then do nothing.
					else if(a instanceof TAPNInhibitorArc)
					{

					}
					else if (a instanceof TimedArc){
						ArrayList<BigDecimal> eligableToken = new ArrayList<BigDecimal>();
						//int indexOfOldestEligebleToken = 0;

						TimedPlace p = (TimedPlace)a.getSource();

						ArrayList<BigDecimal> tokensOfPlace = p.getTokens();						   
						for (int i=0; i< tokensOfPlace.size(); i++){
							if ( ((TimedArc)a).satisfiesGuard(tokensOfPlace.get(i))){
								eligableToken.add(tokensOfPlace.get(i));
							}
						}						   

						//Select torken to remove based on firing mode
						BigDecimal tokenToRemove = CreateGui.getAnimator().firingmode.fire(eligableToken);

						//							XXX  - This will break if two tokens from the same place is consumed
						toReturn.addConsumedToken(p, tokenToRemove);

						p.removeTokenofAge(tokenToRemove);



					} 
					else {
						//Should not be possible
					}
				}



				for (Arc a : (LinkedList<Arc>)transition.getPostset() ){
					if (a instanceof TransportArc){
						TimedPlace p = (TimedPlace)a.getTarget();
						int newNumberOfTokens = p.getTokens().size()+1;
						p.setCurrentMarking(newNumberOfTokens);
						ArrayList<BigDecimal> markingToBeSet = p.getTokens();
						BigDecimal ageOfTokenToSet = tokensConsumedByTransportArcs.get( ((TransportArc) a).getGroupNr() );
						markingToBeSet.set(markingToBeSet.size()-1,ageOfTokenToSet);

						p.setAgeOfTokens(markingToBeSet);
					}
					else{
						TimedPlace p = (TimedPlace)a.getTarget();
						int newNumberOfTokens = p.getTokens().size()+1;
						p.setCurrentMarking(newNumberOfTokens);

					}
				}

			}
		}

		return toReturn;
	}


	/**
	 * Fire a specified transition and specific tokens, no affect if transtions not enabled
	 * @param transition Reference of specifiec Transition
	 *
	 * @author Kenneth Yrke Joergensen <kenneth@yrke.dk> Changed to handeling
	 * firing modes when working with TAPN.
	 */
	public void fireTransition(Transition transition, HashMap<Place, ArrayList<BigDecimal>> consumedTokens) {


		// If it is a TAPN
		if (Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET){

			if (transition != null){
				setEnabledTransitions();

				// Index:
				// Check transition is enables
				//Find the tokens valid to fire (check guard)
				// - If transportarc check if invariant is ok
				// Select the tokens to consume, based on firing stradegy
				// Consume the tokens and create the tokens

				if (transition.isEnabled()){
					HashMap<Integer, BigDecimal> tokensConsumedByTransportArcs = new HashMap<Integer, BigDecimal>();

					for ( Arc a : (LinkedList<Arc>)transition.getPreset() ){
						TimedPlace p = (TimedPlace)a.getSource();

						if (a instanceof TransportArc){
							BigDecimal tokenToRemove = consumedTokens.get(p).get(0);

							//XXX  - This will break if two tokens from the same place is consumed

							tokensConsumedByTransportArcs.put(((TransportArc) a).getGroupNr(), tokenToRemove);

							p.removeTokenofAge(tokenToRemove);

						}
						else if (a instanceof TimedArc){
							//Select torken to remove based on firing mode
							BigDecimal tokenToRemove = consumedTokens.get(p).get(0);

							//XXX  - This will break if two tokens from the same place is consumed

							p.removeTokenofAge(tokenToRemove);

						}
					}



					for (Arc a : (LinkedList<Arc>)transition.getPostset() ){
						if (a instanceof TransportArc){
							TimedPlace p = (TimedPlace)a.getTarget();
							int newNumberOfTokens = p.getTokens().size()+1;
							p.setCurrentMarking(newNumberOfTokens);
							ArrayList<BigDecimal> markingToBeSet = p.getTokens();
							BigDecimal ageOfTokenToSet = tokensConsumedByTransportArcs.get( ((TransportArc) a).getGroupNr() );
							markingToBeSet.set(markingToBeSet.size()-1,ageOfTokenToSet);

							p.setAgeOfTokens(markingToBeSet);
						}
						else{
							TimedPlace p = (TimedPlace)a.getTarget();
							int newNumberOfTokens = p.getTokens().size()+1;
							p.setCurrentMarking(newNumberOfTokens);

						}
					}

				}
			}		   

		}
	}



	/**
	 * Fire a random transition, takes rate (probability) of Transitions into account
	 */
	public Transition fireRandomTransition() {

		setEnabledTransitions();
		// All the enabled transitions are of the same type:
		// a) all are immediate transitions; or 
		// b) all are timed transitions.

		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

		for (int i = 0; i < transitionsArray.size(); i++) {
			Transition transition = (Transition)transitionsArray.get(i);
			if (transition.isEnabled()) {
				enabledTransitions.add(transition);
			}
		}

		// if there is only one enabled transition, return this transition
		if (enabledTransitions.size() == 1) {
			return enabledTransitions.get(0);
		}      

		int random = randomNumber.nextInt(enabledTransitions.size());

		// no enabled transition found, so no transition can be fired
		return enabledTransitions.get(random);
	}


	public void fireTimedTransitionBackwards(HashMap<TimedPlace, ArrayList<BigDecimal>> presetMarking, 
			HashMap<TimedPlace, ArrayList<BigDecimal>> postsetMarking, 
			TAPNTransition transition){
		for (Arc a : (LinkedList<Arc>)transition.getPreset()){
			if (! presetMarking.containsKey(a.getSource()) )
				throw new IllegalArgumentException("Incorrect Preset for transition argument!");
		}
		for (Arc a : (LinkedList<Arc>)transition.getPostset()){
			if (! postsetMarking.containsKey(a.getTarget()) )
				throw new IllegalArgumentException("Incorrect Postset for transition argument!");
		}


		for (Arc a : (LinkedList<Arc>)transition.getPreset()){
			TimedPlace place = (TimedPlace)a.getSource();
			//place.setCurrentMarking( (presetMarking.get(place)).size() );
			//place.setAgeOfTokens(presetMarking.get(place));

			// XXX - kyrke Hav now idea on why the two above functions do net do the work, 
			// But the dont... So we will just use this function for now instead
			place.setTokensAndAgeOfTokens(presetMarking.get(place));
		}
		for (Arc a : (LinkedList<Arc>)transition.getPostset()){
			TimedPlace place = (TimedPlace)a.getTarget();
			//place.setCurrentMarking( ((ArrayList<Float>)postsetMarking.get(place)).size() );
			//place.setAgeOfTokens(postsetMarking.get(place));

			// XXX - kyrke Hav now idea on why the two above functions do net do the work, 
			// But the dont... So we will just use this function for now instead
			place.setTokensAndAgeOfTokens(postsetMarking.get(place));
		}
	}




	public void resetEnabledTransitions()  {
		for (int i = 0; i < transitionsArray.size(); i++) {
			Transition transition = (Transition)transitionsArray.get(i);
			transition.setEnabled(false);
			setChanged();
			notifyObservers(transition);
		}
	}





	public void setEnabledTransitions(){
		for ( Transition t : getTransitions() ){
			boolean isEnabled = false;

			if(!isUsingColors()){
				isEnabled = isTransitionEnabledNonColored(t);
			}else{
				isEnabled = isTransitionEnabledColored(t);
			}
			t.setEnabled(isEnabled);
			setChanged();
			notifyObservers(t);
		}
	}


	private boolean isTransitionEnabledColored(Transition t) {
		boolean enabled = true;
		for(Arc arc : t.getPreset()){
			boolean arcEnabled = false;
			ColoredTimedPlace inputPlace = (ColoredTimedPlace)arc.getSource();

			for(ColoredToken token : inputPlace.getColoredTokens()){
				if(arc instanceof ColoredInputArc){
					arcEnabled = arcEnabled || ((ColoredInputArc)arc).satisfiesGuard(token);
				}else if(arc instanceof ColoredTransportArc){
					boolean guardSatisfied = ((ColoredTransportArc)arc).satisfiesGuard(token);
					boolean targetInvariantSatisfied = ((ColoredTransportArc)arc).satisfiesTargetInvariant(token);

					arcEnabled = arcEnabled || (guardSatisfied && targetInvariantSatisfied);
				}else if(arc instanceof ColoredInhibitorArc){
					arcEnabled = arcEnabled || ((ColoredInhibitorArc)arc).satisfiesGuard(token);
				}

				if(arcEnabled){
					break;
				}
			}

			enabled = enabled && arcEnabled;
		}

		for(Arc arc : t.getPostset()){
			if(arc instanceof ColoredOutputArc){
				int value = ((ColoredOutputArc)arc).getOutputValue().getValue();
				ColorSet colorInvariant = ((ColoredTimedPlace)arc.getTarget()).getColorInvariant();
				enabled = enabled && colorInvariant.contains(value);

			}
		}

		return enabled;
	}


	private boolean isTransitionEnabledNonColored(Transition t) {
		boolean isEnabled = true;

		Collection<Arc> presetArcs = t.getPreset();
		for ( Arc a : presetArcs ){

			Place p = (Place)a.getSource();
			if (p instanceof TimedPlace){

				boolean ageIsSatisfied;

				if(a instanceof TAPNInhibitorArc)
					ageIsSatisfied = true;
				else
					ageIsSatisfied = false;


				if (p.currentMarking > 0){

					for ( BigDecimal token : ((TimedPlace)p).getTokens() ){
						if(a instanceof TAPNInhibitorArc)
						{
							if(!((TimedArc)a).satisfiesGuard(token))
							{
								ageIsSatisfied = false;
								break;
							}


						}
						else
						{
							if ( ((TimedArc)a).satisfiesGuard(token) ){

								//make sure no invariants are violated
								if (a instanceof TransportArc){
									for ( Arc postsetArc : (LinkedList<Arc>)t.getPostset() ){
										if (postsetArc instanceof TransportArc){
											if ( ((TransportArc) postsetArc).getGroupNr() == ((TransportArc)a).getGroupNr()){
												if ( ((TimedPlace)postsetArc.getTarget()).satisfiesInvariant(token) ){
													ageIsSatisfied = true;
													break;
												}
											}
										}
									}
									//invariants are not violated, if it is not a transport arc
								}else {
									ageIsSatisfied = true;
									break;
								}
							}
						}
					}
				}

				isEnabled = ageIsSatisfied;

				if (!isEnabled){
					break;
				}

			}else {
				//p should always be a TimedPlace unless we have introduced hybrid nets
			}
		}
		return isEnabled;
	}



	/**
	 * Empty all attributes, turn into empty Petri-Net
	 */
	private void emptyPNML() {
		pnmlName = null;
		placesArray = null;
		transitionsArray = null;
		arcsArray = null;
		labelsArray = null;
		changeArrayList = null;
		arcsMap = null;
		initializeMatrices();
	}

	/**
	 * Get a List of all the Place objects in the Petri-Net
	 * @return A List of all the Place objects
	 */
	public Place[] getPlaces() {
		Place[] returnArray = new Place[placesArray.size()];

		for (int i = 0; i < placesArray.size(); i++){
			returnArray[i] = (Place)placesArray.get(i);
		}
		return returnArray;
	}


	public int getPlacesCount(){
		if (placesArray == null) {
			return 0;
		} else {
			return placesArray.size();
		}	  
	}   


	/* wjk added 03/10/2007 */
	/**
	 * Get the current marking of the Petri net
	 *
	 * @return The current marking of the Petri net
	 */
	public int[] getMarking() {
		int [] result = new int[placesArray.size()];

		for (int i=0; i < placesArray.size(); i++) {
			result[i] = ((Place) placesArray.get(i)).getCurrentMarking();
		}
		return result;
	}   


	/**
	 * Get a List of all the net-level NameLabel objects in the Petri-Net
	 * @return A List of all the net-level (as opposed to element-specific) label objects
	 */
	public AnnotationNote[] getLabels() {
		AnnotationNote[] returnArray = new AnnotationNote[labelsArray.size()];

		for (int i = 0; i < labelsArray.size(); i++){
			returnArray[i] = (AnnotationNote)labelsArray.get(i);
		}
		return returnArray;
	}

	/**
	 * Get an List of all the Transition objects in the Petri-Net
	 * @return An List of all the Transition objects
	 */
	public Transition[] getTransitions() {
		Transition[] returnArray = new Transition[transitionsArray.size()];

		for (int i = 0; i < transitionsArray.size(); i++){
			returnArray[i] = (Transition)transitionsArray.get(i);
		}
		return returnArray;
	}


	public int getTransitionsCount() {
		if (transitionsArray == null) {
			return 0;
		} else {
			return transitionsArray.size();
		}	  
	}   

	/**
	 * Get an List of all the Arcs objects in the Petri-Net
	 * @return An List of all the Arc objects
	 */
	public Arc[] getArcs() {
		Arc[] returnArray = new Arc[arcsArray.size()];

		for (int i = 0; i < arcsArray.size(); i++){
			returnArray[i] = (Arc)arcsArray.get(i);
		}
		return returnArray;
	}


	/**
	 * Get an List of all the InhibitorArc objects in the Petri-Net
	 * @return An List of all the InhibitorArc objects
	 */
	public InhibitorArc[] getInhibitors() {
		InhibitorArc[] returnArray = new InhibitorArc[inhibitorsArray.size()];

		for (int i = 0; i < inhibitorsArray.size(); i++){
			returnArray[i] = (InhibitorArc)inhibitorsArray.get(i);
		}
		return returnArray;
	}

	public TAPNInhibitorArc[] getTAPNInhibitors() {
		int numInhibArcs = 0;
		for (int i = 0; i < arcsArray.size(); i++){
			if(arcsArray.get(i) instanceof TAPNInhibitorArc)
				numInhibArcs++;
		}

		TAPNInhibitorArc[] returnArray = new TAPNInhibitorArc[numInhibArcs];

		for(int i = 0; i < numInhibArcs; i++)
		{
			for (int j = 0; j < arcsArray.size(); j++)
			{
				if(arcsArray.get(j) instanceof TAPNInhibitorArc)
					returnArray[i] = (TAPNInhibitorArc)arcsArray.get(j);
			}
		}

		return returnArray;
	}   


	/**
	 * Return the Transition called transitionName from the Petri-Net
	 * @param transitionID ID of Transition object to return
	 * @return The first Transition object found with a name equal to transitionName
	 */
	public Transition getTransitionById(String transitionID) {
		Transition returnTransition = null;

		if (transitionsArray != null) {
			if (transitionID != null) {
				for (int i = 0 ; i < transitionsArray.size(); i++) {
					if (transitionID.equalsIgnoreCase(
							((Transition)transitionsArray.get(i)).getId())) {
						returnTransition = (Transition)transitionsArray.get(i);
					}
				}
			}
		}
		return returnTransition;
	}   


	/**
	 * Return the Transition called transitionName from the Petri-Net
	 * @param transitionName Name of Transition object to return
	 * @return The first Transition object found with a name equal to transitionName
	 */
	public Transition getTransitionByName(String transitionName) {
		Transition returnTransition = null;

		if (transitionsArray != null) {
			if (transitionName != null) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if (transitionName.equalsIgnoreCase(
							((Transition)transitionsArray.get(i)).getName())) {
						returnTransition = (Transition)transitionsArray.get(i);
					}
				}
			}
		}
		return returnTransition;
	}

	public Transition getTransitionByNameIgnoreGiven(Transition ignore, String transitionName) {
		Transition returnTransition = null;

		if (transitionsArray != null) {
			if (transitionName != null) {
				for (int i = 0; i < transitionsArray.size(); i++) {
					if(!transitionsArray.get(i).equals(ignore)) {
						if (transitionName.equalsIgnoreCase(
								((Transition)transitionsArray.get(i)).getName())) {
							returnTransition = (Transition)transitionsArray.get(i);
						}
					}
				}
			}
		}
		return returnTransition;
	}


	/**
	 * Return the Transition called transitionName from the Petri-Net
	 * @param transitionNo No of Transition object to return
	 * @return The Transition object
	 */
	public Transition getTransition(int transitionNo) {
		Transition returnTransition = null;

		if (transitionsArray != null) {
			if (transitionNo < transitionsArray.size()) {
				returnTransition = (Transition)transitionsArray.get(transitionNo);
			}
		}
		return returnTransition;
	}


	/** Return the Place called placeName from the Petri-Net
	 * @param placeId ID of Place object to return
	 * @return The first Place object found with id equal to placeId
	 */
	public Place getPlaceById(String placeID) {
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeID != null) {
				for (int i = 0 ; i < placesArray.size(); i++) {
					if (placeID.equalsIgnoreCase(
							((Place)placesArray.get(i)).getId())) {
						returnPlace = (Place)placesArray.get(i);
					}
				}
			}
		}
		return returnPlace;
	}   


	/**
	 * Return the Place called placeName from the Petri-Net
	 * @param placeName Name of Place object to return
	 * @return The first Place object found with a name equal to placeName
	 */
	public Place getPlaceByName(String placeName) {
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeName != null) {
				for (int i = 0; i < placesArray.size(); i++) {
					if (placeName.equalsIgnoreCase(
							((Place)placesArray.get(i)).getName())) {
						returnPlace = (Place)placesArray.get(i);
					}
				}
			}
		}
		return returnPlace;
	}

	public Place getPlaceByNameIgnoreGiven(Place ignore, String placeName)
	{
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeName != null) {
				for (int i = 0; i < placesArray.size(); i++) {
					if(!placesArray.get(i).equals(ignore)) {
						if (placeName.equalsIgnoreCase(
								((Place)placesArray.get(i)).getName())) {
							returnPlace = (Place)placesArray.get(i);
						}
					}
				}
			}
		}
		return returnPlace;
	}


	/**
	 * Return the Place called placeName from the Petri-Net
	 * @param placeNo No of Place object to return
	 * @return The Place object
	 */
	public Place getPlace(int placeNo) {
		Place returnPlace = null;

		if (placesArray != null) {
			if (placeNo < placesArray.size()) {
				returnPlace = (Place)placesArray.get(placeNo);
			}
		}
		return returnPlace;
	}


	/**
	 * Return the PlaceTransitionObject called ptoName from the Petri-Net
	 * @param ptoId Id of PlaceTransitionObject object to return
	 * @return The first Arc PlaceTransitionObject found with a name equal to ptoName
	 */
	public PlaceTransitionObject getPlaceTransitionObject(String ptoId) {
		if (ptoId != null) {
			if (getPlaceById(ptoId) != null){
				return getPlaceById(ptoId);
			} else if (getTransitionById(ptoId) != null) {
				return getTransitionById(ptoId);
			}
		}

		return null;
	}



	/**
	 * Create model from TAPN model
	 * @author Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
	 * @param TAPN model
	 */
	public void createFromTAPN(TAPN model) {

		//		double xcord = 10;
		//		double ycord = 10;
		//
		//		List<dk.aau.cs.petrinet.TAPNPlace> places = model.getPlaces(); 
		//		List<dk.aau.cs.petrinet.TAPNTransition> transitions = model.getTransitions();
		//		List<dk.aau.cs.petrinet.Arc> arcs = model.getArcs();
		//
		//		// Add places
		//		for (dk.aau.cs.petrinet.TAPNPlace p : places){
		//			Place tmp = new Place(xcord, ycord, ""+p.getID(), p.getName(), 10.0, 10.0, 0, 0, 0, 0);
		//			TimedPlace tp = new TimedPlace(tmp, p.getInvariant());
		//			
		//			addPlace(tp);
		//
		//			xcord = xcord + 10;
		//			ycord = ycord + 10;
		//		}
		//
		//		// Add transitions
		//		for (dk.aau.cs.petrinet.TAPNTransition t : transitions){
		//			Transition trans = new Transition(xcord, ycord, ""+t.getID(), t.getName(), 10.0, 10.0, 10.0, false, false, 1, 1);
		//			addTransition(new TAPNTransition(trans));
		//			xcord = xcord + 10;
		//			ycord = ycord + 10;
		//		}
		//
		//		// Add arcs
		//		for (dk.aau.cs.petrinet.Arc a : arcs){
		//			
		//		}
		//
		//		if (CreateGui.getApp()!=null) {
		//			CreateGui.getApp().restoreMode();
		//		}

	}


	/**
	 * Create model from transformed PNML file
	 * @author Ben Kirby, 10 Feb 2007
	 * @param filename URI location of PNML
	 * 
	 * @author Edwin Chung 
	 * This code is modified so that dataLayer objects can be created
	 * outside the GUI
	 */
	public void createFromPNML(Document PNMLDoc){
		createFromPNML(PNMLDoc, false);		
	}

	public void createFromPNML(Document PNMLDoc, boolean colors)	{
		// XXX - kyrke debug
		emptyPNML();
		this.useColors = colors;
		Node node = null;
		NodeList nodeList = null;

		try {
			if (CreateGui.getApp()!=null) {
				// Notifies used to indicate new instances.
				CreateGui.getApp().setMode(Pipe.CREATING); 
			}

			if(colors){
				NodeList places = PNMLDoc.getElementsByTagName("net").item(0).getChildNodes();
				for(int i = 0; i < places.getLength(); i++){
					this.parseColoredElement(places.item(i));
				}
			}else{
				nodeList = PNMLDoc.getDocumentElement().getChildNodes();
				for(int i = 0 ; i < nodeList.getLength() ; i++) {
					node = nodeList.item(i);
						parseElement(node);
				}
			}

			if (CreateGui.getApp()!=null) {
				CreateGui.getApp().restoreMode();
			}
			buildConstraints();
			

		} catch (Exception e) {
			System.out.println("runtime except");
			throw new RuntimeException(e);
		}
	}

	private void parseColoredElement(Node node) {
		Element element;
		if(node instanceof Element) {
			element = (Element)node;
			if ("labels".equals(element.getNodeName())){
				addAnnotation(createAnnotation(element));
			} else if("place".equals(element.getNodeName())){
				addPlace(createColoredPlace(element));
			} else if ("transition".equals(element.getNodeName())){
				addTransition(createTransition(element));
			} else if ("arc".equals(element.getNodeName())) {
				Arc newArc = createColoredArc(element);
				if (newArc instanceof InhibitorArc) {
					addArc((InhibitorArc) newArc);
				} else if (newArc instanceof TAPNInhibitorArc) {
					addArc((TAPNInhibitorArc) newArc);
				} else {
					addArc((NormalArc) newArc);
					checkForInverseArc((NormalArc) newArc);
				}                  
			} else if( "queries".equals(element.getNodeName()) ){
				TAPNQuery query = createQuery(element);
				if(query != null)
					queries.add(query);
			} else if ("constant".equals(element.getNodeName())){
				String name = element.getAttribute("name");
				int value = Integer.parseInt(element.getAttribute("value"));
				if(!name.isEmpty() && value >= 0)
					addConstant(name, value);
			} else {
				System.out.println("!" + element.getNodeName());
			}
		}

	}


	private Arc createColoredArc(Element inputArcElement) {
		String idInput = null;
		String sourceInput = null;
		String targetInput = null;
		double startX = 0;
		double startY = 0;
		boolean taggedArc;

		sourceInput = inputArcElement.getAttribute("source");
		targetInput = inputArcElement.getAttribute("target");
		idInput = inputArcElement.getAttribute("id");

		String taggedTempStorage = inputArcElement.getAttribute("tagged");
		//		String inscriptionOffsetXTempStorage = inputArcElement.getAttribute("inscriptionOffsetX");
		//		String inscriptionOffsetYTempStorage = inputArcElement.getAttribute("inscriptionOffsetY");

		taggedArc = !(taggedTempStorage.length() == 0 ||
				taggedTempStorage.length() == 5);

		if (sourceInput.length() > 0) {
			if (getPlaceTransitionObject(sourceInput) != null) {
				//				System.out.println("PNMLDATA: sourceInput is not null");
				startX = getPlaceTransitionObject(sourceInput).getPositionX();
				startX += getPlaceTransitionObject(sourceInput).centreOffsetLeft();
				startY = getPlaceTransitionObject(sourceInput).getPositionY();
				startY += getPlaceTransitionObject(sourceInput).centreOffsetTop();
			}
		}
		if (targetInput.length() > 0) {
			if (getPlaceTransitionObject(targetInput) != null) {
			}
		}

		PlaceTransitionObject sourceIn = getPlaceTransitionObject(sourceInput);
		PlaceTransitionObject targetIn = getPlaceTransitionObject(targetInput);

		// add the insets and offset
		int aStartx = sourceIn.getX() + sourceIn.centreOffsetLeft();
		int aStarty = sourceIn.getY() + sourceIn.centreOffsetTop();

		int aEndx = targetIn.getX() + targetIn.centreOffsetLeft();
		int aEndy = targetIn.getY() + targetIn.centreOffsetTop();


		double _startx = aStartx;
		double _starty = aStarty;
		double _endx = aEndx;
		double _endy = aEndy;

		String type = inputArcElement.getAttribute("type");

		Arc arc = null;

		if(type.equals("ColoredInputArc")){
			ColoredInputArc cia = new ColoredInputArc(new NormalArc(_startx, _starty, _endx, _endy, sourceIn, targetIn,1,idInput, taggedArc));
			ColoredInterval timeGuard = new ColoredInterval(inputArcElement.getAttribute("timeGuard"));
			cia.setTimeGuard(timeGuard);

			ColorSet colorGuard = new ColorSet(inputArcElement.getAttribute("colorGuard"));
			cia.setColorGuard(colorGuard);

			arc = cia;
		}else if(type.equals("ColoredOutputArc")){
			ColoredOutputArc coa = new ColoredOutputArc(_startx, _starty, _endx, _endy, sourceIn, targetIn,1,idInput, taggedArc);
			coa.setOutputValue(new IntOrConstant(inputArcElement.getAttribute("outputValue")));

			arc = coa;
		}else if(type.equals("ColoredInhibitorArc")){
			ColoredInhibitorArc cia = new ColoredInhibitorArc(new NormalArc(_startx, _starty, _endx, _endy, sourceIn, targetIn,1,idInput, taggedArc));
			ColoredInterval timeGuard = new ColoredInterval(inputArcElement.getAttribute("timeGuard"));
			cia.setTimeGuard(timeGuard);

			ColorSet colorGuard = new ColorSet(inputArcElement.getAttribute("colorGuard"));
			cia.setColorGuard(colorGuard);

			arc = cia;

		}else if(type.equals("ColoredTransportArc")){
			ColoredTransportArc cta = new ColoredTransportArc(
					new TimedArc( new NormalArc(_startx, _starty,
							_endx, _endy,
							sourceIn,
							targetIn,
							1,
							idInput,
							taggedArc),
					""),
					Integer.parseInt(inputArcElement.getAttribute("groupNo")), 
					sourceIn instanceof Place);

			ColoredInterval timeGuard = new ColoredInterval(inputArcElement.getAttribute("timeGuard"));
			cta.setTimeGuard(timeGuard);

			ColorSet colorGuard = new ColorSet(inputArcElement.getAttribute("colorGuard"));
			cta.setColorGuard(colorGuard);

			Preserve preservation = Preserve.valueOf(inputArcElement.getAttribute("preservation"));
			cta.setPreservation(preservation);

			IntOrConstant outputValue = new IntOrConstant(inputArcElement.getAttribute("outputValue"));
			cta.setOutputValue(outputValue);

			arc = cta;
		}

		getPlaceTransitionObject(sourceInput).addConnectFrom(arc);
		getPlaceTransitionObject(targetInput).addConnectTo(arc);

		//		**********************************************************************************
		//		The following section attempts to load and display arcpath details****************

		//NodeList nodelist = inputArcElement.getChildNodes();
		NodeList nodelist = inputArcElement.getElementsByTagName("arcpath");
		if (nodelist.getLength()>0) {
			arc.getArcPath().purgePathPoints();
			for (int i = 0; i < nodelist.getLength(); i++) {         
				Node node = nodelist.item(i);
				if(node instanceof Element) {
					Element element = (Element)node;
					if ("arcpath".equals(element.getNodeName())){
						String arcTempX = element.getAttribute("xCoord");
						String arcTempY = element.getAttribute("yCoord");
						String arcTempType = element.getAttribute("arcPointType");
						float arcPointX = Float.valueOf(arcTempX).floatValue();
						float arcPointY = Float.valueOf(arcTempY).floatValue();
						arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
						boolean arcPointType = 
							Boolean.valueOf(arcTempType).booleanValue();
						arc.getArcPath().addPoint(arcPointX,arcPointY,arcPointType);
					}
				}
			}
		}

		//		Arc path creation ends here***************************************************************
		//		******************************************************************************************
		return arc;
	}


	private Place createColoredPlace(Element element) {
		double positionXInput = 0;
		double positionYInput = 0;
		String idInput = element.getAttribute("id");
		String nameInput = element.getAttribute("name");
		double nameOffsetYInput = 0;
		double nameOffsetXInput = 0;
		double markingOffsetXInput = 0;
		double markingOffsetYInput = 0;
		int capacityInput = 0;

		String positionXTempStorage = element.getAttribute("positionX");
		String positionYTempStorage = element.getAttribute("positionY");
		String nameOffsetXTempStorage = element.getAttribute("nameOffsetX");
		String nameOffsetYTempStorage = element.getAttribute("nameOffsetY");
		String initialMarkingTempStorage = element.getAttribute("initialMarking");
		String markingOffsetXTempStorage = element.getAttribute("markingOffsetX");
		String markingOffsetYTempStorage = element.getAttribute("markingOffsetY");
		String capacityTempStorage = element.getAttribute("capacity");

		if (positionXTempStorage.length() > 0) {
			positionXInput = Double.valueOf(positionXTempStorage).doubleValue() + 1;
		}
		if (positionYTempStorage.length() > 0) {
			positionYInput = Double.valueOf(positionYTempStorage).doubleValue() + 1;
		}

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (nameOffsetYTempStorage.length() > 0) {
			nameOffsetXInput = Double.valueOf(nameOffsetXTempStorage).doubleValue();
		}
		if (nameOffsetXTempStorage.length() > 0) {
			nameOffsetYInput = Double.valueOf(nameOffsetYTempStorage).doubleValue();
		}

		if (initialMarkingTempStorage.length() > 0) {
		}
		if (markingOffsetXTempStorage.length() > 0) {
			markingOffsetXInput = Double.valueOf(markingOffsetXTempStorage).doubleValue();
		}
		if (markingOffsetYTempStorage.length() > 0) {
			markingOffsetYInput = Double.valueOf(markingOffsetYTempStorage).doubleValue();
		}

		if (capacityTempStorage.length() > 0) {
			capacityInput = Integer.valueOf(capacityTempStorage).intValue();
		}

		ColoredTimeInvariant timeInvariant = new ColoredTimeInvariant(element.getAttribute("timeInvariant"));
		ColorSet colorInvariant = new ColorSet(element.getAttribute("colorInvariant"));

		ColoredTimedPlace place = new ColoredTimedPlace(positionXInput,positionYInput, idInput, nameInput,nameOffsetXInput, nameOffsetYInput, 0, markingOffsetXInput, markingOffsetYInput, capacityInput, "");
		place.setTimeInvariant(timeInvariant);
		place.setColorInvariant(colorInvariant);

		NodeList tokenNodes = element.getElementsByTagName("colored-token");
		List<ColoredToken> tokens = parseTokens(tokenNodes);
		place.setColoredTokens(tokens);


		return place;
	}


	private List<ColoredToken> parseTokens(NodeList tokenNodes) {
		ArrayList<ColoredToken> list = new ArrayList<ColoredToken>();

		for(int i = 0; i < tokenNodes.getLength(); i++){
			Element element = (Element)tokenNodes.item(i);

			IntOrConstant value = new IntOrConstant(element.getAttribute("value"));
			list.add(new ColoredToken(value));
		}

		return list;
	}

	private void parseElement(Node node) {
		Element element;
		if(node instanceof Element) {
			element = (Element)node;
			if ("labels".equals(element.getNodeName())){
				addAnnotation(createAnnotation(element));
			}  else if("place".equals(element.getNodeName())){
				addPlace(createPlace(element));
			} else if ("transition".equals(element.getNodeName())){
				addTransition(createTransition(element));
			} else if ("arc".equals(element.getNodeName())) {
				Arc newArc = createArc(element);
				if (newArc instanceof InhibitorArc) {
					addArc((InhibitorArc) newArc);
				} else if (newArc instanceof TAPNInhibitorArc) {
					addArc((TAPNInhibitorArc) newArc);
				} else {
					addArc((NormalArc) newArc);
					checkForInverseArc((NormalArc) newArc);
				}                  
			} else if( "queries".equals(element.getNodeName()) ){
				TAPNQuery query = createQuery(element);
				if(query != null)
					queries.add(query);
			} else if ("constant".equals(element.getNodeName())){
				String name = element.getAttribute("name");
				int value = Integer.parseInt(element.getAttribute("value"));
				if(!name.isEmpty() && value >= 0)
					addConstant(name, value);
			} else {
				System.out.println("!" + element.getNodeName());
			}
		}
	}   


	/**  
	 * This method creates a new datalayer based on a datalayer,
	 * this can be used for drawing a datalayer after having modyfied it
	 * by some external code.
	 * 
	 *  This method is used for drawing a datalayer after it has been
	 *  transformed by dk.aau.cs.TAPN.transformer
	 *  
	 * @author Kenneth Yrke Joergensen (kyrke@cs.aau.dk)  **/ 

	public void createFromDataLayer(DataLayer m){
		if (CreateGui.getApp()!=null) {
			// Notifies used to indicate new instances.
			CreateGui.getApp().setMode(Pipe.CREATING); 
		}

		for (Transition t : m.getTransitions()){
			addTransition(new Transition(t));		   
		}

		for (Place p : m.getPlaces()){
			addPlace(p.clone());
		}


		for (Arc a:m.getArcs()){
			NormalArc tmp = (NormalArc)a.clone();

			PlaceTransitionObject source = getPlaceTransitionObject(tmp.getSource().getName());
			PlaceTransitionObject target =  getPlaceTransitionObject(tmp.getTarget().getName());

			target.addConnectTo(tmp);
			source.addConnectFrom(tmp);


			/* */

			/*	      
		         if (getPlaceTransitionObject(sourceInput) != null) {
//		        System.out.println("PNMLDATA: sourceInput is not null");
		            startX = getPlaceTransitionObject(sourceInput).getPositionX();
		            startX += getPlaceTransitionObject(sourceInput).centreOffsetLeft();
		            startY = getPlaceTransitionObject(sourceInput).getPositionY();
		            startY += getPlaceTransitionObject(sourceInput).centreOffsetTop();
		         }

		         if (getPlaceTransitionObject(targetInput) != null) {
//		        System.out.println("PNMLDATA: targetInput is not null");
		            endX = getPlaceTransitionObject(targetInput).getPositionX();
		            endY = getPlaceTransitionObject(targetInput).getPositionY();
		         }


		      PlaceTransitionObject sourceIn = getPlaceTransitionObject(sourceInput);
		      PlaceTransitionObject targetIn = getPlaceTransitionObject(targetInput);

		      // add the insets and offset
		      int aStartx = sourceIn.getX() + sourceIn.centreOffsetLeft();
		      int aStarty = sourceIn.getY() + sourceIn.centreOffsetTop();

		      int aEndx = targetIn.getX() + targetIn.centreOffsetLeft();
		      int aEndy = targetIn.getY() + targetIn.centreOffsetTop();


		      double _startx = aStartx;
		      double _starty = aStarty;
		      double _endx = aEndx;
		      double _endy = aEndy; */
			/* */

			addArc(tmp);
		}


		if (CreateGui.getApp()!=null) {
			CreateGui.getApp().restoreMode();
		} 
	}



	private TAPNQuery createQuery(Element queryElement) {

		String comment;
		try{
			comment = queryElement.getAttribute("name");
		}catch (Exception e) {
			comment = "No comment specified";
		}

		TraceOption traceOption;
		try{
			traceOption = TraceOption.valueOf(queryElement.getAttribute("traceOption"));
		}catch (Exception e) {
			traceOption = TraceOption.NONE;
		}

		SearchOption searchOption;
		try{
			searchOption = SearchOption.valueOf(queryElement.getAttribute("searchOption"));
		}catch (Exception e) {
			searchOption = SearchOption.BFS;
		}

		HashTableSize hashTableSize;
		try{
			hashTableSize = HashTableSize.valueOf(queryElement.getAttribute("hashTableSize"));		
		}catch (Exception e) {
			hashTableSize = HashTableSize.MB_16;
		}

		ExtrapolationOption extrapolationOption;
		try{
			extrapolationOption = ExtrapolationOption.valueOf(queryElement.getAttribute("extrapolationOption"));		
		}catch (Exception e) {
			extrapolationOption = ExtrapolationOption.AUTOMATIC;
		}

		ReductionOption reductionOption;
		try{
			reductionOption = ReductionOption.valueOf(queryElement.getAttribute("reductionOption"));
		}catch (Exception e) {
			reductionOption = ReductionOption.STANDARD;
		}

		String capacity;
		try{
			capacity =  queryElement.getAttribute("capacity");		
		}catch (Exception e) {
			capacity = "3";
		}
		TAPAALQueryParser queryParser = new TAPAALQueryParser();
		TCTLAbstractProperty query;
		try{
			query = queryParser.parse(queryElement.getAttribute("query"));
			if(query != null)
				return new TAPNQuery(comment, Integer.parseInt(capacity), query, traceOption, searchOption, reductionOption, hashTableSize, extrapolationOption);
			else
				return null;
		}catch (Exception e) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), "TAPAAL encountered an error trying to parse the queries in the model.\n\nThe queries that could not be parsed will not show up in the query list.", "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
			System.err.println("No query was specified: " + e.getStackTrace());
			return null;
		}


	}


	/**
	 * Return a URI for the PNML file for the Petri-Net
	 * @return A DOM for the Petri-Net
	 */
	public String getURI() {
		return pnmlName;
	}


	/** prints out a brief representation of the dataLayer object */
	public void print() {
		System.out.println("No of Places = " + placesArray.size() + "\"");
		System.out.println("No of Transitions = " + transitionsArray.size() + "\"");
		System.out.println("No of Arcs = " + arcsArray.size() + "\"");
		System.out.println("No of Labels = " + labelsArray.size() + 
		"\" (Model View Controller Design Pattern)");
	}



	/**
	 * See if the supplied net has any timed transitions.
	 * @param DataLayer
	 * @return boolean
	 * @author Matthew
	 */
	public boolean hasTimedTransitions(){
		Transition[] transitions = this.getTransitions();
		int transCount = transitions.length;

		for (int i = 0; i< transCount; i++) {
			if (transitions[i].isTimed() == true) {
				return true;
			}
		}
		return false;      
	}


	/**
	 * See if the net has any timed transitions.
	 * @return boolean
	 * @author Matthew
	 */
	public boolean hasImmediateTransitions(){
		Transition[] transitions = this.getTransitions();
		int transCount = transitions.length;

		for (int i = 0; i< transCount; i++) {
			if (transitions[i].isTimed() == false) {
				return true;
			}
		}
		return false;
	}   




	private void checkForInverseArc(NormalArc newArc) {
		Iterator<Arc> iterator = newArc.getSource().getConnectToIterator();

		Arc anArc;
		while (iterator.hasNext()){
			anArc = iterator.next();
			if (anArc.getTarget() == newArc.getSource() && 
					anArc.getSource() == newArc.getTarget()) {
				if (anArc.getClass() == NormalArc.class) {
					if (!newArc.hasInverse()) {
						((NormalArc)anArc).setInverse(newArc, Pipe.JOIN_ARCS);
					}               
				}
			}
		}
	}


	public String getTransitionName(int i) {
		return ((Transition)transitionsArray.get(i)).getName();
	}


	public HashMap<Transition, HashMap<TransportArc, TransportArc> > getTransportArcMap() {
		return transportArcMap;
	}

	public void letTimePass(BigDecimal timeToPass) throws InvariantViolatedAnimationException{
		if(!canTimePass(timeToPass)){
			throw new InvariantViolatedAnimationException();
		}

		//Do the update
		for (Place p : getPlaces()){
			if(isUsingColors()){
				ColoredTimedPlace place = (ColoredTimedPlace)p;
				for(ColoredToken token : place.getColoredTokens()){
					token.doTimeDelay(timeToPass);
				}
			}else{
				if (p instanceof TimedPlace){
					TimedPlace place = (TimedPlace)p;
					int sizeOfArray = place.getTokens().size();

					for (int i = 0; i< sizeOfArray; i++){
						BigDecimal token = place.getTokens().get(i);
						place.getTokens().set(i, token.add(timeToPass));	   
					}					
				}
			}

			setChanged();
			notifyObservers(p);
		}


		setEnabledTransitions();
	}


	public boolean canTimePass(BigDecimal timeToPass) throws InvariantViolatedAnimationException{
		//	 Can we do time delay
		for (Place p : getPlaces()){
			if(isUsingColors()){
				ColoredTimedPlace ctp = (ColoredTimedPlace)p;

				for(ColoredToken token : ctp.getColoredTokens()){
					ColoredToken newToken = new ColoredToken(token.getAge().add(timeToPass), token.getColor());
					if(!ctp.satisfiesInvariant(newToken)){
						return false;
					}
				}
			}else{
				if (p instanceof TimedPlace){
					TimedPlace timedPlace = (TimedPlace)p;

					for (BigDecimal token : timedPlace.getTokens()){
						if (!timedPlace.satisfiesInvariant(token.add(timeToPass))){   
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public void setQueries(ArrayList<TAPNQuery> queries) {
		this.queries = queries;

	}
	public ArrayList<TAPNQuery> getQueries(){
		return queries;
	}

	public Collection<Constant> getConstants()
	{
		return constants.getConstants();
	}

	public UndoableEdit updateConstant(String oldName, Constant constant)
	{
		UndoableEdit edit = constants.updateConstant(oldName, constant, this);
		if(edit != null){
			correctGuards(oldName, constant.getName());
		}

		return edit;
	}

	public void correctGuards(String oldName, String newName) {
		updateArcGuards(oldName, newName);
		for(Place p : placesArray){
			if(!isUsingColors()){
				if(p instanceof TimedPlace){
					TimedPlace tp = (TimedPlace)p;
					String inv = tp.getInvariant();

					String operator = inv.contains("<=") ? "<=" : "<";
					String first = inv.substring(operator.length());

					if(first.equals(oldName)){
						first = newName;
					}

					tp.setInvariant(operator + first);
				}
			}else{
				((ColoredTimedPlace)p).updateConstantName(oldName, newName);
			}
		}

	}


	private void updateArcGuards(String oldName, String newName) {
		for(Arc arc : arcsArray){
			if(!isUsingColors()){
				if(arc instanceof TimedArc || arc instanceof TransportArc){
					TimedArc tarc = (TimedArc)arc;
					String guard = tarc.getGuard();
					String leftDelim = guard.substring(0,1);
					String rightDelim = guard.substring(guard.length()-1, guard.length());
					String first = guard.substring(1, guard.indexOf(","));
					String second = guard.substring(guard.indexOf(",")+1, guard.length()-1);

					if(first.equals(oldName)){
						first = newName;
					}
					if(second.equals(oldName)){
						second = newName;
					}

					tarc.setGuard(leftDelim + first + "," + second + rightDelim);
				}
			}else{
				if(arc instanceof ColoredTransportArc){
					ColoredTransportArc cta = (ColoredTransportArc)arc;
					cta.updateConstantName(oldName, newName);
				}else if(arc instanceof ColoredInhibitorArc){
					((ColoredInhibitorArc)arc).updateConstantName(oldName, newName);
				}else if(arc instanceof ColoredInputArc){
					((ColoredInputArc)arc).updateConstantName(oldName, newName);
				}else if(arc instanceof ColoredOutputArc){
					((ColoredOutputArc)arc).updateConstantName(oldName, newName);
				}
			}
		}
	}


	public UndoableEdit addConstant(String name, int value) {	
		return constants.addConstant(name, value);
	}

	public UndoableEdit removeConstant(String name){
		return constants.removeConstant(name);		
	}

	public void addQuery(TAPNQuery query) {
		queries.add(query);

	}


	public Set<String> getConstantNames() {
		return constants.getConstantNames();
	}



	public int getConstantValue(String constantName) {
		return constants.getConstant(constantName).getValue();
	}


	public int getLargestConstantValue() {
		return constants.getLargestConstantValue();
	}


	public void buildConstraints() {
		constants.buildConstraints(placesArray,arcsArray, isUsingColors());
	}	

	public boolean hasTAPNInhibitorArcs(){ // TODO: Fix this to make it faster
		for(Arc arc : arcsArray){
			if(arc instanceof TAPNInhibitorArc){
				return true;
			}
		}
		return false;
	}


	public void setUseColors(boolean useColors) {
		this.useColors = useColors;
	}


	public boolean isUsingColors() {
		return useColors;
	}


	public void fireColoredTransitionBackwards(
			ColoredDiscreteFiringAction action) {
		for(Entry<ColoredTimedPlace, ColoredToken> entry : action.getConsumedTokens().entrySet()){
			entry.getKey().addColoredToken(entry.getValue());
		}

		for(Entry<ColoredTimedPlace, ColoredToken> entry : action.getProducedTokens().entrySet()){
			entry.getKey().removeColoredToken(entry.getValue());
		}

		//fireTransitionBackwards(action.getTransition());
	}


	public void fireTransition(ColoredDiscreteFiringAction action) {

		for(Entry<ColoredTimedPlace, ColoredToken> entry : action.getConsumedTokens().entrySet()){
			ColoredTimedPlace place = entry.getKey();
			ColoredToken token = entry.getValue();

			place.removeColoredToken(token);
		}

		for(Entry<ColoredTimedPlace, ColoredToken> entry : action.getProducedTokens().entrySet()){
			entry.getKey().addColoredToken(entry.getValue());
		}
	}

	public boolean isDegree2(){
		boolean isDegree2=true;

		for (Transition t : getTransitions()){

			if (t.getPostset().size() > 2 || t.getPreset().size() > 2){
				isDegree2 = false;
				break;
			}

		}

		return isDegree2;

	}


	public void redrawVisibleTokenLists() {
		for (Place place : placesArray) {
			if(place instanceof TimedPlace){
				TimedPlace tp = (TimedPlace)place;
				if(tp.isAgeOfTokensShown()){
					tp.showAgeOfTokens(true);
				}
			}
		}
	}
}
