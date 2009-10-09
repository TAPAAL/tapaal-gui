package pipe.dataLayer;

import java.awt.Component;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.aau.cs.petrinet.TAPN;

import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.ReductionOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.exception.InvariantViolatedAnimationException;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.widgets.ProgressBar;


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
	// kyrke -  this is java >1.4 use generics!!!
	private ArrayList<Place> placesArray = null;
	/** ArrayList containing all the Transition objects in the Petri-Net */
	// kyrke -  this is java >1.4 use generics!!!
	private ArrayList<Transition> transitionsArray = null;
	/** ArrayList containing all the Arc objects in the Petri-Net */
	private ArrayList arcsArray = null;

	/** ArrayList containing all the Arc objects in the Petri-Net */
	private ArrayList inhibitorsArray = null; 
	private ArrayList tapnInhibitorsArray = null; 

	/** ArrayList for net-level label objects (as opposed to element-level labels).*/
	private ArrayList labelsArray = null;

	/** ArrayList for marking Parmameters objects.*/
	private ArrayList markingParametersArray = null;   

	/** ArrayList for rate Parmameters objects.*/
	private ArrayList rateParametersArray = null;      

	/** An ArrayList used to point to either the Arc, Place or Transition 
	 * ArrayLists when these ArrayLists are being update */
	private ArrayList changeArrayList = null;

	/** Initial Marking Vector */
	private int[] initialMarkingVector = null;
	/** Current Marking Vector */
	private int[] currentMarkingVector = null;
	/** Capacity Matrix */
	private int[] capacityVector = null;
	/** Priority Matrix */
	private int[] priorityVector = null;
	/** Timed Matrix */
	private boolean[] timedVector = null;   
	/** Marking Vector Storage used during animation */
	private int[] markingVectorAnimationStorage = null;

	/** Forward Incidence Matrix */
	private PNMatrix forwardsIncidenceMatrix = null;
	/** Backward Incidence Matrix */
	private PNMatrix backwardsIncidenceMatrix = null;
	/** Incidence Matrix */
	private PNMatrix incidenceMatrix = null;

	/** Inhibition Matrix */
	private PNMatrix inhibitionMatrix = null;
	private PNMatrix tapnInhibitionMatrix = null;

	/** Used to determine whether the matrixes have been modified */
	static boolean initialMarkingVectorChanged = true;

	static boolean currentMarkingVectorChanged = true;


	/** X-Axis Scale Value */
	private final int DISPLAY_SCALE_FACTORX = 7; // Scale factors for loading other Petri-Nets (not yet implemented)
	/** Y-Axis Scale Value */
	private final int DISPLAY_SCALE_FACTORY = 7; // Scale factors for loading other Petri-Nets (not yet implemented)
	/** X-Axis Shift Value */
	private final int DISPLAY_SHIFT_FACTORX = 270; // Scale factors for loading other Petri-Nets (not yet implemented)
	/** Y-Axis Shift Value */
	private final int DISPLAY_SHIFT_FACTORY = 120; // Scale factors for loading other Petri-Nets (not yet implemented)

	/** Hashtable which maps PlaceTransitionObjects to their list of connected arcs */
	private Hashtable arcsMap = null;

	/** Hashtable which maps PlaceTransitionObjects to their list of connected arcs */
	private Hashtable inhibitorsMap = null;
	private Hashtable tapnInhibitorsMap = null;

	/** An ArrayList used store the source / destination state groups associated 
	 * with this Petri-Net */
	private ArrayList stateGroups = null;   

	private HashSet markingParameterHashSet = new HashSet();

	private HashSet rateParameterHashSet = new HashSet();

	private HashMap<Transition, HashMap<TransportArc, TransportArc> > transportArcMap;

	private HashMap<TimedPlace, ArrayList<BigDecimal>> placeMarkingStorageMap = null;

	private ArrayList<TAPNQuery> queries = null;
	private HashMap<String, Constant> constants = new HashMap<String, Constant>();

	/**
	 * Create Petri-Net object from PNML file with URI pnmlFileName
	 * @param pnmlFileName Name of PNML File
	 */
	public DataLayer(String pnmlFileName) {

		initializeMatrices();
		PNMLTransformer transform = new PNMLTransformer();
		File temp = new File(pnmlFileName);
		pnmlName = temp.getName();
		createFromPNML(transform.transformPNML(pnmlFileName));
		transportArcMap = new HashMap<Transition, HashMap<TransportArc,TransportArc> >();
		queries = new ArrayList<TAPNQuery>();
	}  


	/**
	 * Create Petri-Net object from pnmlFile
	 * @param pnmlFile PNML File
	 */
	public DataLayer(File pnmlFile) {
		this(pnmlFile.getAbsolutePath());
		queries = new ArrayList<TAPNQuery>();
	}


	/**
	 * Create empty Petri-Net object
	 */
	public DataLayer() {
		initializeMatrices();
		queries = new ArrayList<TAPNQuery>();
	}


	/**
	 * Method to clone a DataLayer obejct
	 */
	public DataLayer clone() {
		DataLayer newClone = null;
		try { 
			newClone = (DataLayer)super.clone();

			newClone.placesArray = deepCopy(placesArray);
			newClone.transitionsArray = deepCopy(transitionsArray);
			newClone.arcsArray = deepCopy(arcsArray);
			newClone.inhibitorsArray = deepCopy(inhibitorsArray);
			newClone.tapnInhibitorsArray = deepCopy(tapnInhibitorsArray);
			//newClone.tokensArray = deepCopy(tokensArray);
			newClone.labelsArray = deepCopy(labelsArray);
		} catch(CloneNotSupportedException e) {
			throw new Error(e);
		}
		return newClone;
	}


	/**
	 * @param original arraylist to be deep copied
	 * @return a clone of the arraylist
	 */
	private static ArrayList deepCopy(ArrayList original) {
		ArrayList result = (ArrayList) original.clone();
		ListIterator listIter = result.listIterator();

		while(listIter.hasNext()) {
			PetriNetObject pnObj = (PetriNetObject) listIter.next();
			listIter.set(pnObj.clone());
		}
		return result;
	}   


	/**
	 * Initialize Arrays
	 */
	private void initializeMatrices() {
		placesArray = new ArrayList();
		transitionsArray = new ArrayList();
		arcsArray = new ArrayList();
		inhibitorsArray = new ArrayList();
		tapnInhibitorsArray = new ArrayList();
		labelsArray = new ArrayList();
		stateGroups = new ArrayList();
		markingParametersArray = new ArrayList();
		rateParametersArray = new ArrayList();
		initialMarkingVector = null;
		forwardsIncidenceMatrix = null;
		backwardsIncidenceMatrix = null;
		incidenceMatrix = null;
		inhibitionMatrix = null;
		tapnInhibitionMatrix = null;

		// may as well do the hashtable here as well
		arcsMap = new Hashtable();
		inhibitorsMap = new Hashtable();
		tapnInhibitorsMap = new Hashtable();
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
			setMatrixChanged();
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
		boolean unique = true;
		labelsArray.add(labelInput);
		setChanged();
		notifyObservers(labelInput);
	}


	/**
	 * Add markingParameterInput to the back of the Marking Parameter ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param placeInput Place Object to add
	 */
	private void addAnnotation(MarkingParameter markingParameterInput) {
		boolean unique = true;
		markingParametersArray.add(markingParameterInput);
		setChanged();
		notifyObservers(markingParameterInput);
	}


	/**
	 * Add rateParameterInput to the back of the Rate Parameter ArrayList
	 * All observers are notified of this change (Model-View Architecture)
	 * @param placeInput Place Object to add
	 */
	private void addAnnotation(RateParameter rateParameterInput) {
		boolean unique = true;
		rateParametersArray.add(rateParameterInput);
		setChanged();
		notifyObservers(rateParameterInput);
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
			setMatrixChanged();
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

				if (tmp.connectedTo == null){
					if (first instanceof TimedPlace){

						for (Object o : tmp.getTarget().getPostset()){

							if (o instanceof TransportArc){
								if (tmp.getGroupNr() == ((TransportArc)o).getGroupNr()){
									//Found partner

									tmp.connectedTo = ((TransportArc)o);
									((TransportArc)o).connectedTo = tmp; 
									
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

									tmp.connectedTo = ((TransportArc)o);
									((TransportArc)o).connectedTo = tmp; 
									
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
			setMatrixChanged();
			//notifyObservers(arcInput.getBounds());
			notifyObservers(arcInput);
		}
	}

	public void addArc(TAPNInhibitorArc inhibitorArcInput) {
		boolean unique = true;

		if (inhibitorArcInput != null) {
			if (inhibitorArcInput.getId() != null &&
					inhibitorArcInput.getId().length() > 0) {
				for (int i = 0 ; i < tapnInhibitorsArray.size() ; i++) {
					if (inhibitorArcInput.getId().equals(
							((Arc)tapnInhibitorsArray.get(i)).getId())) {
						unique = false;
					}
				}
			} else {
				String id = null;
				if (tapnInhibitorsArray != null && tapnInhibitorsArray.size() > 0) {
					int no = tapnInhibitorsArray.size();
					do {
						for (int i = 0; i < tapnInhibitorsArray.size(); i++) {
							id = "I" + no;
							if (tapnInhibitorsArray.get(i) != null) {
								if (id.equals(((Arc)tapnInhibitorsArray.get(i)).getId())) {
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
			tapnInhibitorsArray.add(inhibitorArcInput);
			addInhibitorArcToInhibitorsMap(inhibitorArcInput);

			setChanged();
			setMatrixChanged();
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
			setMatrixChanged();
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
		ArrayList newList = null;

		if (source != null) {
//			Pete: Place/Transitions now always moveable
//			source.setMovable(false);
			if (arcsMap.get(source)!=null) {
//				System.out.println("adding arc to existing list");
				((ArrayList)arcsMap.get(source)).add(arcInput);
			} else {
//				System.out.println("creating new arc list");
				newList = new ArrayList();
				newList.add(arcInput);
				arcsMap.put(source,newList);
			}
		}

		if (target != null) {
//			Pete: Place/Transitions now always moveable
//			target.setMovable(false);
			if (arcsMap.get(target)!=null) {
//				System.out.println("adding arc to existing list2");
				((ArrayList)arcsMap.get(target)).add(arcInput);
			} else {
//				System.out.println("creating new arc list2");
				newList = new ArrayList();
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
		ArrayList newList = null;

		if (source != null) {
			if (tapnInhibitorsMap.get(source) != null) {
				((ArrayList)tapnInhibitorsMap.get(source)).add(inhibitorArcInput);
			} else {
				newList = new ArrayList();
				newList.add(inhibitorArcInput);
				tapnInhibitorsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (tapnInhibitorsMap.get(target) != null) {
				((ArrayList)tapnInhibitorsMap.get(target)).add(inhibitorArcInput);
			} else {
				newList = new ArrayList();
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
		ArrayList newList = null;

		if (source != null) {
			if (inhibitorsMap.get(source) != null) {
				((ArrayList)inhibitorsMap.get(source)).add(inhibitorArcInput);
			} else {
				newList = new ArrayList();
				newList.add(inhibitorArcInput);
				inhibitorsMap.put(source, newList);
			}
		}

		if (target != null) {
			if (inhibitorsMap.get(target) != null) {
				((ArrayList)inhibitorsMap.get(target)).add(inhibitorArcInput);
			} else {
				newList = new ArrayList();
				newList.add(inhibitorArcInput);
				inhibitorsMap.put(target, newList);
			} 
		}
//		System.out.println("inhibitorsMap size: " + inhibitorsMap.size());
	}


	public void addStateGroup(StateGroup stateGroupInput) {
		boolean unique = true;
		String id = null;
		int no = stateGroups.size();

		// Check if ID is set from PNML file
		if (stateGroupInput.getId() != null &&
				stateGroupInput.getId().length() > 0) {
			id = stateGroupInput.getId();

			// Check if ID is unique
			for (int i = 0; i < stateGroups.size(); i++) {
				if(id.equals(((StateGroup)stateGroups.get(i)).getId())) {
					unique = false;
				}
			}
		} else {
			unique = false;
		}

		// Find a unique ID for the new state group
		if (!unique) {
			id = "SG" + no;
			for (int i = 0; i < stateGroups.size(); i++) {
				// If a matching ID is found, increment id and reset loop
				if (id.equals(((StateGroup)stateGroups.get(i)).getId())) {
					id = "SG" + ++no;
					i = 0;
				}
			}
			stateGroupInput.setId(id);
		}
		stateGroups.add(stateGroupInput);
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
				labelsArray.add(pnObject);
			} else if (pnObject instanceof RateParameter) {
				rateParametersArray.add(pnObject);
				rateParameterHashSet.add(pnObject.getName());
			} else if (pnObject instanceof MarkingParameter) {
				markingParametersArray.add(pnObject);            
				markingParameterHashSet.add(pnObject.getName());
			} else { // arrows, other labels.
				changeArrayList.add(pnObject);
			setChanged();
			setMatrixChanged();
			notifyObservers(pnObject);
			}
		}
		// we reset to null so that the wrong ArrayList can't get added to
		changeArrayList = null;
	}

//	private void addTAPNTransition(TAPNTransition transitionInput) {
//		boolean unique = true;
//
//		if (transitionInput != null) {
//			if (transitionInput.getId() != null
//					&& transitionInput.getId().length() > 0) {
//				for (int i = 0; i < transitionsArray.size(); i++) {
//					if (transitionInput.getId().equals(
//							((Transition)transitionsArray.get(i)).getId())) {
//						unique = false;
//					}
//				}
//			} else {
//				String id = null;
//				if (transitionsArray != null && transitionsArray.size() > 0) {
//					int no = transitionsArray.size();
//					do {
//						// System.out.println("transition while loop");
//						for (int i = 0; i < transitionsArray.size(); i++) {
//							id = "T" + no;
//							if (transitionsArray.get(i) != null) {
//								if (id.equals(
//										((TAPNTransition)transitionsArray.get(i)).getId())) {
//									unique = false;
//									no++;
//								} else {
//									unique = true;
//								}
//							}
//						}
//					} while(!unique);
//				} else {
//					id = "T0";
//				}
//
//				if(id != null) {
//					transitionInput.setId(id);
//				} else {
//					transitionInput.setId("error");
//				}
//			}
//			transitionsArray.add(transitionInput);
//			setChanged();
//			setMatrixChanged();
//			//    notifyObservers(transitionInput.getBounds());
//			notifyObservers(transitionInput);
//		}
//	}


	// XXX - kyrke 
	public void changed(){

		setChanged();
		setMatrixChanged();



		for(Transition t : getTransitions()){
			System.out.println("lalal" + t.id);
			notifyObservers(t);   

		}



	}

	/**
	 * Removes the specified object from the appropriate ArrayList of objects.
	 * All observers are notified of this change.
	 * @param pnObject The PetriNetObject to be removed.
	 */
	public void removePetriNetObject(PetriNetObject pnObject){
		boolean didSomething = false;
		ArrayList attachedArcs = null;

		try{
			if (setPetriNetObjectArrayList(pnObject)) {
				didSomething = changeArrayList.remove(pnObject);
				// we want to remove all attached arcs also
				if (pnObject instanceof PlaceTransitionObject) {

					if ( (ArrayList)arcsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = ((ArrayList)arcsMap.get(pnObject));

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

					if ( (ArrayList)inhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = ((ArrayList)inhibitorsMap.get(pnObject));

						// iterate over all the attached arcs, removing them all
						// Pere: in inverse order!
						//for (int i=0; i < attachedArcs.size(); i++){
						for (int i = attachedArcs.size() - 1; i >= 0; i--){
							((Arc)attachedArcs.get(i)).delete();
						}
						inhibitorsMap.remove(pnObject);
					}
					if ( (ArrayList)tapnInhibitorsMap.get(pnObject) != null) {

						// get the list of attached arcs for the object we are removing
						attachedArcs = ((ArrayList)tapnInhibitorsMap.get(pnObject));

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
						ArrayList a = (ArrayList)arcsMap.get(attached);
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
							((ArrayList)arcsMap.get(attached)).remove(pnObject);
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
						ArrayList a=(ArrayList)inhibitorsMap.get(attached);
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
							((ArrayList)inhibitorsMap.get(attached)).remove(pnObject);
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
						ArrayList a=(ArrayList)tapnInhibitorsMap.get(attached);
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
							((ArrayList)tapnInhibitorsMap.get(attached)).remove(pnObject);
						}

						attached.removeToArc((Arc)pnObject);
						if (attached instanceof Transition) {
							((Transition)attached).removeArcCompareObject((Arc)pnObject);
						}
//						attached.updateConnected(); //causing null pointer exceptions (?)
					}
				} else if (pnObject instanceof MarkingParameter) {
					markingParameterHashSet.remove(pnObject.getName());
				} else if (pnObject instanceof RateParameter) {
					rateParameterHashSet.remove(pnObject.getName());
				}

				if (didSomething) {
					setChanged();
					setMatrixChanged();
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
	 *  This method removes a state group from the arrayList
	 * @param SGObject The State Group objet to be removed
	 */
	public void removeStateGroup(StateGroup SGObject) {
		stateGroups.remove(SGObject);
	}

	/**
	 * Checks whether a state group with the same name exists already as the
	 * argument
	 * * @param stateName
	 * @return
	 */
	public boolean stateGroupExistsAlready(String stateName) {
		Iterator<StateGroup> i = stateGroups.iterator();
		while(i.hasNext()) {
			StateGroup stateGroup = i.next();
			String stateGroupName = stateGroup.getName();
			if (stateName.equals(stateGroupName)) {
				return true;
			}
		}
		return false;
	}   


	/**
	 * Returns an iterator for the transitions array.
	 * Used by Animator.class to set all enabled transitions to highlighted 
	 */
	public Iterator returnTransitions(){
		return transitionsArray.iterator();
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
			changeArrayList = tapnInhibitorsArray;
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
		} else if(pnObject instanceof MarkingParameter) {
			changeArrayList = markingParametersArray;
			return true;
		} else if(pnObject instanceof RateParameter) {
			changeArrayList = rateParametersArray;
			return true;
		}      
		return false;
	}


	/**
	 * Returns an iterator of all PetriNetObjects - the order of these cannot be guaranteed.
	 * @return An iterator of all PetriNetObjects
	 */
	public Iterator getPetriNetObjects(){
		ArrayList all = new ArrayList(placesArray);
		all.addAll(transitionsArray);
		all.addAll(arcsArray);
		all.addAll(labelsArray);
		//tokensArray removed
		all.addAll(markingParametersArray);
		all.addAll(rateParametersArray);

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
		String nameTemp = inputLabelElement.getAttribute("name");

		if (positionXTempStorage.length() > 0) {
			positionXInput = Integer.valueOf(positionXTempStorage).intValue() *
			(false ? DISPLAY_SCALE_FACTORX : 1) +
			(false ? DISPLAY_SHIFT_FACTORX : 1);
		}

		if (positionYTempStorage.length() > 0){
			positionYInput = Integer.valueOf(positionYTempStorage).intValue() *
			(false ? DISPLAY_SCALE_FACTORX : 1) +
			(false ? DISPLAY_SHIFT_FACTORX : 1);
		}

		if (widthTemp.length() > 0) {
			widthInput = Integer.valueOf(widthTemp).intValue() *
			(false ? DISPLAY_SCALE_FACTORY : 1) +
			(false ? DISPLAY_SHIFT_FACTORY : 1);
		}

		if (heightTemp.length() > 0) {
			heightInput = Integer.valueOf(heightTemp).intValue() *
			(false ? DISPLAY_SCALE_FACTORY : 1) +
			(false ? DISPLAY_SHIFT_FACTORY : 1);
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
	 * Creates a Parameter object from a Definition DOM Element
	 * @param inputDefinitionElement Input Label DOM Element
	 * @return Parameter Object
	 */
	private Parameter createParameter (Element inputDefinitionElement) {
		int positionXInput = 0;
		int positionYInput = 0;
		String type = null;
		boolean borderInput = true;

		String positionXTempStorage = inputDefinitionElement.getAttribute("positionX");
		String positionYTempStorage = inputDefinitionElement.getAttribute("positionY");
		String typeTemp = inputDefinitionElement.getAttribute("type");
		String borderTemp = "true";
		String nameTemp = inputDefinitionElement.getAttribute("name");
		String expressionTemp = inputDefinitionElement.getAttribute("expression");

		if (positionXTempStorage.length() > 0) {
			positionXInput = Integer.valueOf(positionXTempStorage).intValue()/* *
                  (false ? DISPLAY_SCALE_FACTORX : 1) +
                  (false ? DISPLAY_SHIFT_FACTORX : 1)*/;
		}

		if (positionYTempStorage.length() > 0){
			positionYInput = Integer.valueOf(positionYTempStorage).intValue()/* *
                  (false ? DISPLAY_SCALE_FACTORX : 1) +
                  (false ? DISPLAY_SHIFT_FACTORX : 1)*/;
		}

		if (typeTemp.length() > 0) {
			type = typeTemp;
		} else {
			type = "real";
		}

		if (type.equals("real")) {
			rateParameterHashSet.add(nameTemp);
			return new RateParameter(nameTemp, Double.parseDouble(expressionTemp),  
					positionXInput, positionYInput);
		} else {
			markingParameterHashSet.add(nameTemp);
			return new MarkingParameter(nameTemp, Integer.parseInt(expressionTemp),  
					positionXInput, positionYInput);               
		}
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
		double rate = 1.0;
		boolean timedTransition;
		boolean infiniteServer;
		int angle = 0;
		int priority = 1;
		double weight = 1.0;

		String positionXTempStorage = element.getAttribute("positionX");
		String positionYTempStorage = element.getAttribute("positionY");
		String idTempStorage = element.getAttribute("id");
		String nameTempStorage = element.getAttribute("name");
		String nameOffsetXTempStorage = element.getAttribute("nameOffsetX");
		String nameOffsetYTempStorage = element.getAttribute("nameOffsetY");
		String nameRate = element.getAttribute("rate");
		String nameTimed = element.getAttribute("timed");
		String nameInfiniteServer = element.getAttribute("infiniteServer");
		String nameAngle = element.getAttribute("angle");
		String namePriority = element.getAttribute("priority");
		//String nameWeight = element.getAttribute("weight");
		String parameterTempStorage = element.getAttribute("parameter");

		/* wjk - a useful little routine to display all attributes of a transition
       for (int i=0; ; i++) {
          Object obj = inputTransitionElement.getAttributes().item(i);
          if (obj == null) {
             break;
          }
          System.out.println("Attribute " + i + " = " + obj.toString());
       }
		 */      

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
			(false ? Pipe.DISPLAY_SCALE_FACTORX : 1) +
			(false ? Pipe.DISPLAY_SHIFT_FACTORX : 1);
		}
		if (positionYTempStorage.length() > 0) {
			positionYInput = Double.valueOf(positionYTempStorage).doubleValue() *
			(false ? Pipe.DISPLAY_SCALE_FACTORY : 1) +
			(false ? Pipe.DISPLAY_SHIFT_FACTORY : 1);
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

		if (nameRate.length()== 0) {
			nameRate = "1.0";
		}
		if (nameRate != "1.0") {
			rate = Double.valueOf(nameRate).doubleValue();
		} else {
			rate = 1.0;
		}

		if (nameAngle.length() > 0) {
			angle = Integer.valueOf(nameAngle).intValue();
		}

		if (namePriority.length() > 0) {
			priority = Integer.valueOf(namePriority).intValue();
		}

		Transition transition = null;
		// XXX - we need to se here if it is a TAPN or normal thing we are loading
		// XXX - this is a quick hack
		if (true) {
			transition =  
				new TAPNTransition(positionXInput, positionYInput,     
						idInput, 
						nameInput, 
						nameOffsetXInput, nameOffsetYInput, 
						rate, 
						timedTransition, 
						infiniteServer,
						angle,
						priority);
		} else {

			transition =  
				new Transition(positionXInput, positionYInput,     
						idInput, 
						nameInput, 
						nameOffsetXInput, nameOffsetYInput, 
						rate, 
						timedTransition, 
						infiniteServer,
						angle,
						priority);
		}

		if (parameterTempStorage.length() > 0) {
			if (existsRateParameter(parameterTempStorage)) { 
				for (int i = 0; i < rateParametersArray.size(); i++) {
					if (parameterTempStorage.equals(
							((RateParameter)rateParametersArray.get(i)).getName())) {
						transition.setRateParameter(
								(RateParameter)rateParametersArray.get(i));
					}
				}
			}
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
		String parameterTempStorage = element.getAttribute("parameter");
		String invariantTempStorage = element.getAttribute("invariant");


		if (positionXTempStorage.length() > 0) {
			positionXInput = Double.valueOf(positionXTempStorage).doubleValue() *
			(false ? Pipe.DISPLAY_SCALE_FACTORX : 1) +
			(false ? Pipe.DISPLAY_SHIFT_FACTORX : 1);
		}
		if (positionYTempStorage.length() > 0) {
			positionYInput = Double.valueOf(positionYTempStorage).doubleValue() *
			(false ? Pipe.DISPLAY_SCALE_FACTORY : 1) +
			(false ? Pipe.DISPLAY_SHIFT_FACTORY : 1);
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


		if (parameterTempStorage.length() > 0) {
			if (existsMarkingParameter(parameterTempStorage)) { 
				for (int i = 0; i < markingParametersArray.size(); i++) {
					if (parameterTempStorage.equals(
							((MarkingParameter)markingParametersArray.get(i)).getName())) {
						place.setMarkingParameter(
								(MarkingParameter)markingParametersArray.get(i));
					}
				}
			}
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
		int weightInput = 1;
		double inscriptionOffsetXInput = 0;
		double inscriptionOffsetYInput = 0;
		double startX = 0;
		double startY = 0;
		double endX = 0;
		double endY = 0;
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
//				System.out.println("PNMLDATA: targetInput is not null");
				endX = getPlaceTransitionObject(targetInput).getPositionX();
				endY = getPlaceTransitionObject(targetInput).getPositionY();
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
						Integer.valueOf(inscriptionTempStorage),
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
	 * Creates all Petri-Net Matrixes from current Petri-Net
	 */
	private void createMatrixes(){
		createIncidenceMatrix();
		createInitialMarkingVector();
		createCurrentMarkingVector();
		createInhibitionMatrix();
	}


	/**
	 * Creates Forward Incidence Matrix from current Petri-Net
	 */
	private void createForwardIncidenceMatrix(){
		int placeSize = placesArray.size();
		int transitionSize = transitionsArray.size();

		forwardsIncidenceMatrix = new PNMatrix(placeSize, transitionSize);

		for (int i = 0; i < arcsArray.size(); i++) {
			Arc arc = (Arc)arcsArray.get(i);
			if (arc != null ) {
				PetriNetObject pnObject = arc.getTarget();
				if (pnObject != null) {
					if (pnObject instanceof Place) {
						Place place = (Place)pnObject;
						pnObject = arc.getSource();
						if (pnObject != null) {
							if (pnObject instanceof Transition) {
								Transition transition = (Transition)pnObject;
								int transitionNo = getListPosition(transition);
								int placeNo = getListPosition(place);
								try {
									forwardsIncidenceMatrix.set(
											placeNo, transitionNo, arc.getWeight());
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, 
											"Problem in forwardsIncidenceMatrix");
									System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + arc.getWeight());
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Creates Backwards Incidence Matrix from current Petri-Net
	 */
	private void createBackwardsIncidenceMatrix(){//Matthew
		int placeSize = placesArray.size();
		int transitionSize = transitionsArray.size();

		backwardsIncidenceMatrix = new PNMatrix(placeSize, transitionSize);

		for (int i = 0 ; i < arcsArray.size() ; i++) {
			Arc arc = (Arc)arcsArray.get(i);
			if (arc != null) {
				PetriNetObject pnObject = arc.getSource();
				if (pnObject != null){
					if (pnObject instanceof Place) {
						Place place = (Place)pnObject;
						pnObject = arc.getTarget();
						if (pnObject != null){
							if (pnObject instanceof Transition){
								Transition transition = (Transition)pnObject;
								int transitionNo = getListPosition(transition);
								int placeNo = getListPosition(place);
								try {
									backwardsIncidenceMatrix.set(
											placeNo, transitionNo, arc.getWeight());
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, 
											"Problem in backwardsIncidenceMatrix");                            
									System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + arc.getWeight());
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Creates Incidence Matrix from current Petri-Net
	 */
	private void createIncidenceMatrix(){
		createForwardIncidenceMatrix();
		createBackwardsIncidenceMatrix();
		incidenceMatrix = new PNMatrix(forwardsIncidenceMatrix);
		incidenceMatrix = incidenceMatrix.minus(backwardsIncidenceMatrix);
		incidenceMatrix.matrixChanged  = false;      
	}


	/**
	 * Creates Initial Marking Vector from current Petri-Net
	 */
	private void createInitialMarkingVector(){
		int placeSize = placesArray.size();

		initialMarkingVector = new int[placeSize];
		for (int placeNo = 0; placeNo < placeSize; placeNo++) {
			initialMarkingVector[placeNo] = 
				((Place)placesArray.get(placeNo)).getInitialMarking();
		}
	}


	/**
	 * Creates Current Marking Vector from current Petri-Net
	 */
	private void createCurrentMarkingVector(){
		int placeSize = placesArray.size();

		currentMarkingVector = new int[placeSize];
		for (int placeNo = 0; placeNo < placeSize; placeNo++) {
			currentMarkingVector[placeNo] = 
				((Place)placesArray.get(placeNo)).getCurrentMarking();
		}
	}


	/**
	 * Creates Capacity Vector from current Petri-Net
	 */
	private void createCapacityVector(){
		int placeSize = placesArray.size();

		capacityVector = new int[placeSize];
		for (int placeNo = 0; placeNo < placeSize; placeNo++) {
			capacityVector[placeNo] = ((Place)placesArray.get(placeNo)).getCapacity();
		}
	}   


	/**
	 * Creates Timed Vector from current Petri-Net
	 */
	private void createTimedVector() {
		int transitionSize = transitionsArray.size();

		timedVector = new boolean[transitionSize];
		for (int transitionNo = 0; transitionNo < transitionSize; transitionNo++) {
			timedVector[transitionNo] =
				((Transition)transitionsArray.get(transitionNo)).isTimed();
		}
	}  


	/**
	 * Creates Priority Vector from current Petri-Net
	 */
	private void createPriorityVector() {
		int transitionSize = transitionsArray.size();

		priorityVector = new int[transitionSize];
		for (int transitionNo = 0; transitionNo < transitionSize; transitionNo++) {
			priorityVector[transitionNo] =
				((Transition)transitionsArray.get(transitionNo)).getPriority();
		}
	}     


	/**
	 * Creates Inhibition Matrix from current Petri-Net
	 */
	private void createInhibitionMatrix(){
		int placeSize = placesArray.size();
		int transitionSize = transitionsArray.size();
		inhibitionMatrix = new PNMatrix(placeSize, transitionSize);

		for (int i = 0; i < inhibitorsArray.size(); i++) {
			InhibitorArc inhibitorArc = (InhibitorArc)inhibitorsArray.get(i);
			if (inhibitorArc != null) {
				PetriNetObject pnObject = inhibitorArc.getSource();
				if (pnObject != null) {
					if (pnObject instanceof Place) {
						Place place = (Place)pnObject;
						pnObject = inhibitorArc.getTarget();
						if (pnObject != null) {
							if (pnObject instanceof Transition) {
								Transition transition = (Transition)pnObject;
								int transitionNo = getListPosition(transition);
								int placeNo = getListPosition(place);
								try {
									inhibitionMatrix.set(
											placeNo, transitionNo, inhibitorArc.getWeight());
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, 
											"Problema a inhibitionMatrix");                          
									System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + inhibitorArc.getWeight());
								}                        
							}
						}
					}
				}
			}
		}
	}
	private void createTAPNInhibitionMatrix(){
		int placeSize = placesArray.size();
		int transitionSize = transitionsArray.size();
		tapnInhibitionMatrix = new PNMatrix(placeSize, transitionSize);

		for (int i = 0; i < tapnInhibitorsArray.size(); i++) {
			TAPNInhibitorArc inhibitorArc = (TAPNInhibitorArc)tapnInhibitorsArray.get(i);
			if (inhibitorArc != null) {
				PetriNetObject pnObject = inhibitorArc.getSource();
				if (pnObject != null) {
					if (pnObject instanceof Place) {
						Place place = (Place)pnObject;
						pnObject = inhibitorArc.getTarget();
						if (pnObject != null) {
							if (pnObject instanceof Transition) {
								Transition transition = (Transition)pnObject;
								int transitionNo = getListPosition(transition);
								int placeNo = getListPosition(place);
								try {
									tapnInhibitionMatrix.set(
											placeNo, transitionNo, inhibitorArc.getWeight());
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, 
											"Problema a inhibitionMatrix");                          
									System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + inhibitorArc.getWeight());
								}                        
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Stores Current Marking
	 */
	public void storeState(){
		boolean isTAPN = true;
		if (isTAPN){
			placeMarkingStorageMap = new HashMap<TimedPlace, ArrayList<BigDecimal>>();
			for (Place p : getPlaces()){
				placeMarkingStorageMap.put( (TimedPlace)p, (ArrayList<BigDecimal>)((TimedPlace)p).getTokens().clone() );
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
			if (placeMarkingStorageMap!=null){
				for (Place p : getPlaces()){
					ArrayList<BigDecimal> markingOfP = placeMarkingStorageMap.get((TimedPlace)p);
					p.setCurrentMarking( markingOfP.size() );
					((TimedPlace)p).setAgeOfTokens(markingOfP);
					setChanged();
					notifyObservers(p);
					setMatrixChanged();
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
						setMatrixChanged();
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
	public DiscreetFiringAction fireTransition(Transition transition) {
		DiscreetFiringAction toReturn = null;
		
		// If it is a TAPN
		if (Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET){
			toReturn = new DiscreetFiringAction(transition);
			
			if (transition != null){
				setEnabledTransitions();

				// Index:
				// Check transition is enables
				//Find the tokens valid to fire (check guard)
				// - If transportarc check if invariant is ok
				// Select the tokens to consume, based on firing stradegy
				// Consume the tokens and create the tokens

				if (transition.isEnabled()){
					boolean hadTransportArc = false;
					HashMap<Integer, BigDecimal> tokensConsumedByTransportArcs = new HashMap<Integer, BigDecimal>();

					for (Arc a : (LinkedList<Arc>)transition.getPreset() ){



						hadTransportArc = true;
						if (a instanceof TransportArc){
							ArrayList<BigDecimal> eligableToken = new ArrayList<BigDecimal>();
							

						   TimedPlace p = (TimedPlace)a.getSource();
						   
						   ArrayList<BigDecimal> tokensOfPlace = p.getTokens();					
						   
						   TimedPlace targetPlace = (TimedPlace)((TransportArc)a).connectedTo.getTarget();
						   
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
		}else{
			if (transition != null) {

				setEnabledTransitions();
				if (transition.isEnabled() && placesArray != null){
					int transitionNo = transitionsArray.indexOf(transition);
					for (int placeNo = 0; placeNo < placesArray.size(); placeNo++) {
						((Place)placesArray.get(placeNo)).setCurrentMarking(
								(currentMarkingVector[placeNo] + 
										incidenceMatrix.get(placeNo, transitionNo)));
					}
				}
			}
		}
		setMatrixChanged();
		
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
					boolean hadTransportArc = false;
					HashMap<Integer, BigDecimal> tokensConsumedByTransportArcs = new HashMap<Integer, BigDecimal>();

					for ( Arc a : (LinkedList<Arc>)transition.getPreset() ){

						if (a instanceof TransportArc){
							hadTransportArc = true;
							ArrayList<BigDecimal> eligableToken = new ArrayList<BigDecimal>();

							TimedPlace p = (TimedPlace)a.getSource();

							ArrayList<BigDecimal> tokensOfPlace = p.getTokens();					

							TimedPlace targetPlace = (TimedPlace)((TransportArc)a).connectedTo.getTarget();

							for (int i=0; i< tokensOfPlace.size(); i++){
								if ( ((TimedArc)a).satisfiesGuard(tokensOfPlace.get(i)) && targetPlace.satisfiesInvariant(tokensOfPlace.get(i))) {
									eligableToken.add(tokensOfPlace.get(i));
								}
							}	
							BigDecimal tokenToRemove = consumedTokens.get(p).get(0);

							//XXX  - This will break if two tokens from the same place is consumed

							tokensConsumedByTransportArcs.put(((TransportArc) a).getGroupNr(), tokenToRemove);

							p.removeTokenofAge(tokenToRemove);

						}
						// if arc is an inhibitor arc then do nothing
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
							BigDecimal tokenToRemove = consumedTokens.get(p).get(0);

							//XXX  - This will break if two tokens from the same place is consumed

							p.removeTokenofAge(tokenToRemove);

						}else {
							System.out.println("not a timed or transport arc");
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
		setMatrixChanged();
	}
	


	/**
	 * Fire a random transition, takes rate (probability) of Transitions into account
	 */
	public Transition fireRandomTransition() {

		setEnabledTransitions();
		// All the enabled transitions are of the same type:
		// a) all are immediate transitions; or 
		// b) all are timed transitions.

		ArrayList enabledTransitions = new ArrayList();
		double rate = 0;
		for (int i = 0; i < transitionsArray.size(); i++) {
			Transition transition = (Transition)transitionsArray.get(i);
			if (transition.isEnabled()) {
				enabledTransitions.add(transition);
				rate += transition.getRate();
			}
		}

		// if there is only one enabled transition, return this transition
		if (enabledTransitions.size() == 1) {
			return (Transition)enabledTransitions.get(0);
		}      

		double random = randomNumber.nextDouble();
		double x = 0;
		for (int i = 0; i < enabledTransitions.size(); i++) {
			Transition t = (Transition)enabledTransitions.get(i);

			x += t.getRate() / rate;

			if (random < x) {
				return t;
			}         
		}

		// no enabled transition found, so no transition can be fired
		return null;
	}

	/**
	 * This method will fire a random transition, and gives precedence 
	 * to immediate transitions before considering "timed" transitions. 
	 * The "rate" property of the transition is used as a weighting 
	 * factor so the probability of selecting a transition is the 
	 * rate of that transition divided by the sum of the weights of the
	 * other enabled transitions of its class. The "rate" property can 
	 * now be used to give priority among several enabled, immediate
	 * transitions, or when there are no enabled, immediate transitions
	 * to give priority among several enabled, "timed" transitions. 
	 * 
	 * Note: in spite of the name "timed" there is no probabilistic rate
	 * calculated -- just a weighting factor among similar transitions. 
	 * 
	 * Changed by David Patterson Jan 2, 2006
	 *
	 * Changed by David Patterson Apr 24, 2007 to clean up problems 
	 * caused by fractional rates, and to speed up processing when only
	 * one transition of a kind is enabled.
	 * 
	 * Changed by David Patterson May 10, 2007 to properly handle fractional
	 * weights for immeditate transitions.
	 * 
	 * THe same logic is also used for timed transitions until the exponential
	 * distribution is added. When that happens, the code will only be used for
	 * immediate transitions.
	 * /
  public Transition fireRandomTransition() {
     Transition result = null;
     Transition t;
     setEnabledTransitions();
     // int transitionsSize = transitionsArray.size()*transitionsArray.size()*transitionsArray.size();
     int transitionNo = 0;

     double rate = 0.0d;
     double sumOfImmedWeights = 0.0d;
     double sumOfTimedWeights = 0.0d;
     ArrayList timedTransitions = new ArrayList();	// ArrayList<Transition>
     ArrayList immedTransitions = new ArrayList();	// ArrayList<Transition>

     for(transitionNo = 0 ; transitionNo < transitionsArray.size() ; transitionNo++){
        t = (Transition) transitionsArray.get(  transitionNo  );
        rate = t.getRate();
        if ( t.isEnabled()) {
           if ( t.isTimed() ) {                     // is it a timed transition
              timedTransitions.add( t );
              sumOfTimedWeights += rate;
           } else {                                  // immediate transition
              immedTransitions.add( t  );
              sumOfImmedWeights += rate;
           }
        }		// end of if isEnabled
     }		// end of for transitionNo

     // Now, if there are immediate transitions, pick one
     // next block changed by David Patterson to fix bug
     int count = immedTransitions.size();
     switch ( count ) {
        case 0:		// no immediate transitions
           break;	// skip out
        case 1: 	// only one immediate transition
           result = (Transition) immedTransitions.get( 0 );
           break;	// skip out
        default:	// several immediate transitions
           double rval = sumOfImmedWeights * randomNumber.nextDouble();
           for ( int index = 0; index < count; index++ ) {
              t = (Transition) immedTransitions.get( index );
              rval -= t.getRate();
              if ( rval <= 0.0d ) {
                 result = t;
                 break;
              }	
           }
     }
     if ( result == null ) {             // no immediate transition found
        count = timedTransitions.size(); // count of timed, enabled transitions
        switch( count ) {
           case 0:		// trouble! No enabled transition found
              break;
           case 1: 	// only one timed transition
              result = ( Transition ) timedTransitions.get( 0 );
              break;
           default:		// several timed transitions -- for now, pick one
              double rval = sumOfTimedWeights * randomNumber.nextDouble();
              for ( int index = 0; index < count; index++ ) {
                 t = (Transition) timedTransitions.get( index );
                 rval -= t.getRate();
                 if ( rval <= 0.0d ) {
                    result = t;
                    break;
                 }
              }
        }
     }

     if ( result == null ) {
        System.out.println( "no random transition to fire" );
     } else {
        fireTransition(result);
        createCurrentMarkingVector();
     }
     resetEnabledTransitions();
     return result;
  }     // end of method fireRandomTransition */


	public void fireTimedTransitionBackwards(HashMap<TimedPlace, ArrayList<BigDecimal>> presetMarking, 
			HashMap<TimedPlace, ArrayList<BigDecimal>> postsetMarking, 
			TAPNTransition transition){
		for (Arc a : (LinkedList<TimedArc>)transition.getPreset()){
			if (! presetMarking.containsKey(a.getSource()) )
				throw new IllegalArgumentException("Incorrect Preset for transition argument!");
		}
		for (Arc a : (LinkedList<Arc>)transition.getPostset()){
			if (! postsetMarking.containsKey(a.getTarget()) )
				throw new IllegalArgumentException("Incorrect Postset for transition argument!");
		}

		fireTransitionBackwards(transition);

		for (Arc a : (LinkedList<TimedArc>)transition.getPreset()){
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
		setMatrixChanged(); 
	}
	
	
	
	public void fireTransitionBackwards(Transition transition) {
		if (transition != null) {
			setEnabledTransitionsBackwards();
			if (transition.isEnabled() && placesArray != null){
				int transitionNo = transitionsArray.indexOf(transition);
				for (int placeNo = 0; placeNo < placesArray.size(); placeNo++) {
					((Place)placesArray.get(placeNo)).setCurrentMarking(
							(currentMarkingVector[placeNo] - 
									incidenceMatrix.get(placeNo, transitionNo)));
				}
			}
		}
		setMatrixChanged();
	}


	/* Method not used * /
   public void fireRandomTransitionBackwards() {
      setEnabledTransitionsBackwards();
      int transitionsSize = transitionsArray.size() * transitionsArray.size() *
              transitionsArray.size();
      int randomTransitionNumber = 0;
      Transition randomTransition = null;
      do {
         randomTransitionNumber = randomNumber.nextInt(transitionsArray.size());
         randomTransition = 
                 (Transition)transitionsArray.get(randomTransitionNumber);
         transitionsSize--;
         if(transitionsSize <= 0){
            break;
         }
      } while(! randomTransition.isEnabled());
      fireTransitionBackwards(randomTransition);
//    System.out.println("Random Fired Transition Backwards" + ((Transition)transitionsArray.get(randonTransition)).getId());
   }*/


	public void resetEnabledTransitions()  {
		for (int i = 0; i < transitionsArray.size(); i++) {
			Transition transition = (Transition)transitionsArray.get(i);
			transition.setEnabled(false);
			setChanged();
			notifyObservers(transition);
		}
	}


	/**Calculate whether a transition is enabled given a specific marking
	 * @param DataLayer - the net
	 * @param int[]     - the marking
	 * @param int       - the specific transition to test for enabled status
	 * @return boolean  - an array of booleans specifying which transitions are 
	 *                    enabled in the specified marking
	 */
	public boolean getTransitionEnabledStatus(int[] marking, int transition) {
		int transCount = this.getTransitionsCount();
		int placeCount = this.getPlacesCount();
		boolean[] result = new boolean[transCount];
		int[][] CMinus = this.getBackwardsIncidenceMatrix();

		//initialise matrix to true
		for (int k = 0; k < transCount; k++) { 
			result[k] = true;
		}
		for (int i = 0; i < transCount; i++) {
			for (int j = 0; j < placeCount; j++) {
				if (marking[j] < CMinus[j][i]) {
					result[i] = false;
				}
			}
		}
		return result[transition];
	}   


	/**
	 * getTransitionEnabledStatusArray()
	 * Calculate which transitions are enabled given a specific marking.
	 * @author Matthew Cook (original code), Nadeem Akharware (optimisation)
	 * @author Pere Bonet added inhibitor arcs, place capacities and transition 
	 * priorities
	 * @param int[]       the marking
	 * @return boolean[]  an array of booleans specifying which transitions are 
	 *                    enabled in the specified marking
	 */
	public boolean[] getTransitionEnabledStatusArray(int[] marking) {
		return getTransitionEnabledStatusArray(
				this.getTransitions(),
				marking,
				this.getBackwardsIncidenceMatrix(),
				this.getForwardsIncidenceMatrix(),              
				this.getInhibitionMatrix(),
				this.getCapacityVector(),
				this.getPlacesCount(),
				this.getTransitionsCount());
	}


	/**
	 * Determines whether all transitions are enabled and sets
	 * the correct value of the enabled boolean
	 */
	public void setEnabledTransitionsBackwards() {

		if (currentMarkingVectorChanged) {
			createMatrixes();
		}

		boolean[] enabledTransitions = getTransitionEnabledStatusArray(
				this.getTransitions(),
				this.getCurrentMarkingVector(),
				this.getForwardsIncidenceMatrix(),
				this.getBackwardsIncidenceMatrix(),
				this.getInhibitionMatrix(),
				this.getCapacityVector(),
				this.getPlacesCount(),
				this.getTransitionsCount());

		for (int i = 0; i < enabledTransitions.length; i++) {
			Transition transition = (Transition)transitionsArray.get(i);
			if (enabledTransitions[i] != transition.isEnabled()) {
				transition.setEnabled(enabledTransitions[i]);
				setChanged();
				notifyObservers(transition);
			}
		}
	}


	/**
	 * Determines whether all transitions are enabled and sets
	 * the correct value of the enabled boolean
	 */
//	Joakim Byg - Used other less obscure method implemented right below   
	/*   public void setEnabledTransitions() {

      if (currentMarkingVectorChanged) {
         createMatrixes();
      }      

      boolean[] enabledTransitions = getTransitionEnabledStatusArray(
              this.getTransitions(),
              this.getCurrentMarkingVector(),
              this.getBackwardsIncidenceMatrix(),
              this.getForwardsIncidenceMatrix(),              
              this.getInhibitionMatrix(),
              this.getCapacityVector(),
              this.getPlacesCount(),
              this.getTransitionsCount());

      for (int i = 0; i < enabledTransitions.length; i++) {
         Transition transition = (Transition)transitionsArray.get(i);
         if (enabledTransitions[i] != transition.isEnabled()) {
            transition.setEnabled(enabledTransitions[i]);
            setChanged();
            notifyObservers(transition);
         }
      }
   }
	 */  
//	Joakim Byg - new less obscure global update for enabled transitions
	public void setEnabledTransitions(){
		createMatrixes();

		for ( Transition t : getTransitions() ){
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
			t.setEnabled(isEnabled);
			setChanged();
			notifyObservers(t);
		}
	}


	/**
	 * getTransitionEnabledStatusArray()
	 * Calculate which transitions are enabled given a specific marking.
	 * @author Matthew Cook (original code), Nadeem Akharware (optimisation)
	 * @author Pere Bonet added inhibitor arcs, place capacities and transition 
	 * priorities
	 * @param int[]       the marking
	 * @return boolean[]  an array of booleans specifying which transitions are 
	 *                    enabled in the specified marking
	 */
	private boolean[] getTransitionEnabledStatusArray(
			final Transition[] transArray, final int[] marking, 
			final int[][] CMinus, final int[][]CPlus, final int [][]inhibition, 
			final int capacities[], final int placeCount, 
			final int transitionCount) {

		boolean[] result = new boolean[transitionCount];
		boolean hasTimed = false;
		boolean hasImmediate = false;

		int maxPriority = 0;

		for (int i = 0; i < transitionCount ;i++) {
			result[i] = true; //inicialitzam a enabled
			for (int j = 0; j < placeCount; j++) {
				if ((marking[j] < CMinus[j][i]) && (marking[j]!=-1)) {
					result[i] = false;
					break;
				}

				// capacities
				if ((capacities[j] > 0) && 
						(marking[j] + CPlus[j][i] - CMinus[j][i]> capacities[j])) {
					// firing this transition would break a capacity restriction so 
					// the transition is not enabled
					result[i] = false;
					break;
				}

				// inhibitor arcs
				if (inhibition[j][i] > 0 && marking[j] >= inhibition[j][i]) {
					// an inhibitor arc prevents the firing of this transition so 
					// the transition is not enabled               
					result[i] = false;
					break;
				}
			}


			// we look for the highest priority of the enabled transitions
			if (result[i] == true) {
				if (transArray[i].isTimed() == true) {
					hasTimed = true;
				} else {
					hasImmediate = true;
					if (transArray[i].getPriority() > maxPriority) {
						maxPriority = transArray[i].getPriority();
					}               
				}
			}

		}

		// Now make sure that if any of the enabled transitions are immediate 
		// transitions, only they can fire as this must then be a vanishing state.
		// - disable the immediate transitions with lower priority.
		// - disable all timed transitions if there is an immediate transition enabled.
		for (int i = 0; i < transitionCount ;i++) {
			if (!transArray[i].isTimed() && 
					transArray[i].getPriority() < maxPriority) {
				result[i] = false;            
			}
			if (hasTimed && hasImmediate) {
				if (transArray[i].isTimed() == true) {
					result[i] = false;
				}
			}
		}

		//print("getTransitionEnabledStatusArray: ",result);//debug
		return result;
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
		markingParametersArray = null;
		rateParametersArray = null;
		changeArrayList = null;
		initialMarkingVector = null;
		forwardsIncidenceMatrix = null;
		backwardsIncidenceMatrix = null;
		incidenceMatrix = null;
		inhibitionMatrix = null;
		tapnInhibitionMatrix = null;
		arcsMap = null;
		initializeMatrices();
	}


	/**
	 * Get position of Petri-Net Object in ArrayList of given Petri-Net Object's type
	 * @param pnObject PlaceTransitionObject to get the position of
	 * @return Position (-1 if not present) of Petri-Net Object in ArrayList of 
	 * given Petri-Net Object's type
	 */
	public int getListPosition(PetriNetObject pnObject){

		if (setPetriNetObjectArrayList(pnObject)){
			return changeArrayList.indexOf(pnObject);
		} else {
			return -1;
		}
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
	 * Get a List of all the marking Parameter objects in the Petri-Net
	 * @return A List of all the marking Parameter objects 
	 */
	public MarkingParameter[] getMarkingParameters() {
		MarkingParameter[] returnArray = 
			new MarkingParameter[markingParametersArray.size()];

		for (int i = 0; i < markingParametersArray.size(); i++){
			returnArray[i] = (MarkingParameter)markingParametersArray.get(i);
		}
		return returnArray;
	}   


	/**
	 * Get a List of all the marking Parameter objects in the Petri-Net
	 * @return A List of all the marking Parameter objects 
	 */
	public RateParameter[] getRateParameters() {
		RateParameter[] returnArray =  new RateParameter[rateParametersArray.size()];

		for (int i = 0; i < rateParametersArray.size(); i++){
			returnArray[i] = (RateParameter)rateParametersArray.get(i);
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
		TAPNInhibitorArc[] returnArray = new TAPNInhibitorArc[inhibitorsArray.size()];

		for (int i = 0; i < tapnInhibitorsArray.size(); i++){
			returnArray[i] = (TAPNInhibitorArc)tapnInhibitorsArray.get(i);
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
	 * Return the Forward Incidence Matrix for the Petri-Net
	 * @return The Forward Incidence Matrix for the Petri-Net
	 */
	public int[][] getForwardsIncidenceMatrix() {
		if (forwardsIncidenceMatrix == null 
				|| forwardsIncidenceMatrix.matrixChanged) {
			createForwardIncidenceMatrix();
		}
		return (forwardsIncidenceMatrix != null
				? forwardsIncidenceMatrix.getArrayCopy() 
						: null);
	}


	/**
	 * Return the Backward Incidence Matrix for the Petri-Net
	 * @return The Backward Incidence Matrix for the Petri-Net
	 */
	public int[][] getBackwardsIncidenceMatrix() {
		if (backwardsIncidenceMatrix == null 
				|| backwardsIncidenceMatrix.matrixChanged) {
			createBackwardsIncidenceMatrix();
		}
		return (backwardsIncidenceMatrix != null 
				? backwardsIncidenceMatrix.getArrayCopy()
						: null);
	}


	/**
	 * Return the Incidence Matrix for the Petri-Net
	 * @return The Incidence Matrix for the Petri-Net
	 */
	public int[][] getIncidenceMatrix() {
		if (incidenceMatrix == null || incidenceMatrix.matrixChanged) {
			createIncidenceMatrix();
		}
		return (incidenceMatrix != null ? incidenceMatrix.getArrayCopy() : null);
	}


	/**
	 * Return the Incidence Matrix for the Petri-Net
	 * @return The Incidence Matrix for the Petri-Net
	 */
	public int[][] getInhibitionMatrix() {
		if (inhibitionMatrix == null || inhibitionMatrix.matrixChanged) {
			createInhibitionMatrix();
		}
		return (inhibitionMatrix != null ? inhibitionMatrix.getArrayCopy() : null);
	}
	public int[][] getTAPNInhibitionMatrix() {
		if (tapnInhibitionMatrix == null || tapnInhibitionMatrix.matrixChanged) {
			createInhibitionMatrix();
		}
		return (tapnInhibitionMatrix != null ? tapnInhibitionMatrix.getArrayCopy() : null);
	}   

	/**
	 * Return the Initial Marking Vector for the Petri-Net
	 * @return The Initial Marking Vector for the Petri-Net
	 */
	public int[] getInitialMarkingVector() {
		if (initialMarkingVectorChanged) {
			createInitialMarkingVector();
		}
		return initialMarkingVector;
	}


	/**
	 * Return the Initial Marking Vector for the Petri-Net
	 * @return The Initial Marking Vector for the Petri-Net
	 */
	public int[] getCurrentMarkingVector() {
		if (currentMarkingVectorChanged)  {
			createCurrentMarkingVector();
		}
		return currentMarkingVector;
	}


	/**
	 * Return the capacity Matrix for the Petri-Net
	 * @return The capacity Matrix for the Petri-Net
	 */
	public int[] getCapacityVector() {
		createCapacityVector();
		return capacityVector;
	}


	/**
	 * Return the capacity Matrix for the Petri-Net
	 * @return The capacity Matrix for the Petri-Net
	 */
	public int[] getPriorityVector() {
		createPriorityVector();
		return priorityVector;
	}


	/**
	 * Return the capacity Matrix for the Petri-Net
	 * @return The capacity Matrix for the Petri-Net
	 */
	public boolean[] getTimedVector() {
		createTimedVector();
		return timedVector;
	}


	private void setMatrixChanged () {
		if (forwardsIncidenceMatrix != null) {
			forwardsIncidenceMatrix.matrixChanged = true;
		}
		if (backwardsIncidenceMatrix != null) {
			backwardsIncidenceMatrix.matrixChanged  = true;
		}
		if (incidenceMatrix != null) {
			incidenceMatrix.matrixChanged = true;
		}
		if (inhibitionMatrix != null) {
			inhibitionMatrix.matrixChanged = true;
		}
		if (tapnInhibitionMatrix != null) {
			tapnInhibitionMatrix.matrixChanged = true;
		}   
		initialMarkingVectorChanged = true;
		currentMarkingVectorChanged = true;
	}

	/**
	 * Create model from TAPN model
	 * @author Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
	 * @param TAPN model
	 */
	public void createFromTAPN(TAPN model) {

		double xcord = 10;
		double ycord = 10;

		List<dk.aau.cs.petrinet.Place> places = model.getPlaces(); 
		List<dk.aau.cs.petrinet.Transition> transitions = model.getTransitions();
		List<dk.aau.cs.petrinet.Arc> arcs = model.getArcs();

		// Add places
		for (dk.aau.cs.petrinet.Place p : places){
			Place tmp = new Place(xcord, ycord, ""+p.getID(), p.getName(), 10.0, 10.0, 0, 0, 0, 0);

			addPlace(tmp);

			xcord = xcord + 10;
			ycord = ycord + 10;
		}

		// Add transitions
		for (dk.aau.cs.petrinet.Transition t : transitions){
			addTransition(new Transition(xcord, ycord, ""+t.getID(), t.getName(), 10.0, 10.0, 10.0, false, false, 1, 1));
			xcord = xcord + 10;
			ycord = ycord + 10;
		}

		// Add arcs
		for (dk.aau.cs.petrinet.Arc a : arcs){



		}

		if (CreateGui.getApp()!=null) {
			CreateGui.getApp().restoreMode();
		}

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
	public void createFromPNML(Document PNMLDoc)	{
		// XXX - kyrke debug
		emptyPNML();
		Element element = null;
		Node node = null;
		NodeList nodeList = null;

		try {
			nodeList = PNMLDoc.getDocumentElement().getChildNodes();
			if (CreateGui.getApp()!=null) {
				// Notifies used to indicate new instances.
				CreateGui.getApp().setMode(Pipe.CREATING); 
			}
			for(int i = 0 ; i < nodeList.getLength() ; i++) {
				node = nodeList.item(i);

				if(node instanceof Element) {
					element = (Element)node;
					if ("labels".equals(element.getNodeName())){
						addAnnotation(createAnnotation(element));
					} else if ("definition".equals(element.getNodeName())){
						Note note = createParameter(element);
						if (note instanceof MarkingParameter) {
							addAnnotation((MarkingParameter)note);
						} else if (note instanceof RateParameter) {
							addAnnotation((RateParameter)note);
						}
					} else if("place".equals(element.getNodeName())){
						addPlace(createPlace(element));
					} else if ("transition".equals(element.getNodeName())){
						addTransition(createTransition(element));
					} else if ("arc".equals(element.getNodeName())) {

						Arc newArc = createArc(element);
						if (newArc instanceof InhibitorArc) {
							addArc((InhibitorArc) newArc);
						} else if (newArc instanceof TAPNInhibitorArc) {
							addArc((TAPNInhibitorArc) newArc);
						}
						else {
							addArc((NormalArc) newArc);
							checkForInverseArc((NormalArc) newArc); 
						}
					} else if ("stategroup".equals(element.getNodeName())) {
						addStateGroup(createStateGroup(element));
					} else if ("queries".equals(element.getNodeName())){
						queries.add(createQuery(element));
					} else {
						System.out.println("!" + element.getNodeName());
					}
				}
			}



			if (CreateGui.getApp()!=null) {
				CreateGui.getApp().restoreMode();
			}
		} catch (Exception e) {
			System.out.println("runtime except");
			throw new RuntimeException(e);
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

			for (Object a : p.getPostset()){

			}
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

	/**
	 * Creates a StateGroup object from a DOM element
	 *
	 * @param inputStateGroupElement input state group DOM Element
	 * @return StateGroup Object
	 */
	private StateGroup createStateGroup(Element inputStateGroupElement) {
		// Create the state group with name and id
		String id = inputStateGroupElement.getAttribute("id");
		String name = inputStateGroupElement.getAttribute("name");
		StateGroup newGroup = new StateGroup(id, name);

		Node node = null;
		NodeList nodelist = null;
		StringTokenizer tokeniser;
		nodelist = inputStateGroupElement.getChildNodes();

		// If this state group contains states then add them
		if (nodelist.getLength() > 0) {
			for (int i = 1; i < nodelist.getLength()-1; i++) {
				node = nodelist.item(i);
				if(node instanceof Element) {
					Element element = (Element)node;
					if ("statecondition".equals(element.getNodeName() )) {
						// Loads the condition in the form "P0 > 4"
						String condition = element.getAttribute("value");
						// Now we tokenise the elements of the condition
						// (i.e. "P0" ">" "4") to create a state
						tokeniser = new StringTokenizer(condition);
						newGroup.addState(tokeniser.nextToken(), tokeniser.nextToken(), tokeniser.nextToken());
					}
				}
			}
		}
		return newGroup;
	}   


	public StateGroup[] getStateGroups() {
		StateGroup[] returnArray = new StateGroup[stateGroups.size()];
		for(int i = 0; i < stateGroups.size(); i++) {
			returnArray[i] = (StateGroup)stateGroups.get(i);
		}
		return returnArray;
	}   


	/**
	 * Create model from transformed PNML file
	 */
	public void createFromPNML(Document PNMLDoc, ProgressBar progressBar) {
		emptyPNML();
		Element element = null;
		Node node = null;
		NodeList nodeList = null;
		ArrayList<TAPNQuery> queriesArrayList = new ArrayList<TAPNQuery>();
		
		try {
			nodeList = PNMLDoc.getDocumentElement().getChildNodes();
			if (CreateGui.getApp()!=null) {
				// Notifies used to indicate new instances.
				CreateGui.getApp().setMode(Pipe.CREATING); 
			}
			progressBar.setProgressBar(nodeList.getLength());
			for(int i = 0 ; i < nodeList.getLength() ; i++) {
				node = nodeList.item(i);

				if(node instanceof Element) {
					element = (Element)node;
					if ("labels".equals(element.getNodeName())){
						addAnnotation(createAnnotation(element));
					} else if ("definition".equals(element.getNodeName())){
						Note note = createParameter(element);
						if (note instanceof MarkingParameter) {
							addAnnotation((MarkingParameter)note);
						} else if (note instanceof RateParameter) {
							addAnnotation((RateParameter)note);
						}
					} else if("place".equals(element.getNodeName())){
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
						queries.add(query);
					} else {
						System.out.println("!" + element.getNodeName());
					}
				}
				progressBar.step();
			}

			if (CreateGui.getApp()!=null) {
				CreateGui.getApp().restoreMode();
			}
			CreateGui.getModel().setQueries(queries);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			reductionOption = ReductionOption.NAIVE;
		}
		
		String capacity;
		try{
			capacity =  queryElement.getAttribute("capacity");		
		}catch (Exception e) {
			capacity = "3";
		}
		String query;
		try{
			query = queryElement.getAttribute("query");
			return new TAPNQuery(comment, Integer.parseInt(capacity), query, traceOption, searchOption, reductionOption, hashTableSize, extrapolationOption);
		}catch (Exception e) {
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


	public boolean existsMarkingParameter (String name){
		return markingParameterHashSet.contains(name);
	}


	public boolean existsRateParameter (String name){
		return rateParameterHashSet.contains(name);
	}


	public boolean changeRateParameter(String oldName, String newName) {
		if (rateParameterHashSet.contains(newName)){
			return false;
		}
		rateParameterHashSet.remove(oldName);
		rateParameterHashSet.add(newName);
		return true;
	}


	public boolean changeMarkingParameter(String oldName, String newName) {
		if (markingParameterHashSet.contains(newName)){
			return false;
		}
		markingParameterHashSet.remove(oldName);
		markingParameterHashSet.add(newName);
		return true;
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


	/**Work out if a specified marking describes a tangible state.
	 * A state is either tangible (all enabled transitions are timed)
	 * or vanishing (there exists at least one enabled state that is transient, 
	 * i.e. untimed).
	 * If an immediate transition exists, it will automatically fire before a 
	 * timed transition.
	 * @param DataLayer - the net to be tested
	 * @param int[]     - the marking of the net to be tested
	 * @return boolean  - is it tangible or not
	 */
	public boolean isTangibleState(int[] marking) {   
		Transition[] trans = this.getTransitions();
		int numTrans = trans.length;
		boolean hasTimed = false;
		boolean hasImmediate = false;

		for (int i = 0; i < numTrans; i++ ){
			if (this.getTransitionEnabledStatus(marking, i) == true){
				if (trans[i].isTimed()== true) {
					//If any immediate transtions exist, the state is vanishing
					//as they will fire immediately
					hasTimed = true;
				} else if (trans[i].isTimed() != true) {
					hasImmediate = true;
				}
			}
		}
		return (hasTimed == true && hasImmediate == false);   
	}


	private void checkForInverseArc(NormalArc newArc) {
		Iterator iterator = newArc.getSource().getConnectToIterator();

		Arc anArc;
		while (iterator.hasNext()){
			anArc = (Arc)iterator.next();
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


	public HashMap getTransportArcMap() {
		return transportArcMap;
	}

	public void letTimePass(BigDecimal timeToPass) throws InvariantViolatedAnimationException{

		//	 Can we do time delay
		for (Place p : getPlaces()){
			if ( ! (p instanceof TimedPlace) ){
				//Do not do stuff with tokens
			}else{
				int sizeOfArray = ((TimedPlace)p).getTokens().size();

				for (int i=0; i< sizeOfArray; i++ ){
					BigDecimal tokensAge = ((TimedPlace)p).getTokens().get(i);

					if (!((TimedPlace)p).satisfiesInvariant(tokensAge.add(timeToPass))){   
						throw new InvariantViolatedAnimationException(p, tokensAge.add(timeToPass));
					}

				}

			}
		}

		//Do the update
		for (Place p : getPlaces()){
			if ( ! (p instanceof TimedPlace) ){
				//Do not do stuff with tokens
			}else{
				int sizeOfArray = ((TimedPlace)p).getTokens().size();


				for (int i=0; i< sizeOfArray; i++ ){
					BigDecimal tokensAge = ((TimedPlace)p).getTokens().get(i);
					((TimedPlace)p).getTokens().set(i, tokensAge.add(timeToPass));	   
				}

				setChanged();
				notifyObservers(p);
			}
		}
		
		
		setEnabledTransitions();
		setMatrixChanged();
	}


	public boolean canTimePass(BigDecimal timeToPass) throws InvariantViolatedAnimationException{

		//	 Can we do time delay
		for (Place p : getPlaces()){
			if ( ! (p instanceof TimedPlace) ){
				//Do not do stuff with tokens
			}else{
				int sizeOfArray = ((TimedPlace)p).getTokens().size();

				for (int i=0; i< sizeOfArray; i++ ){
					BigDecimal tokensAge = ((TimedPlace)p).getTokens().get(i);

					if (!((TimedPlace)p).satisfiesInvariant(tokensAge.add(timeToPass))){   
						return false;
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
		return constants.values();
	}
	
	public void updateConstant(String oldName, Constant constant)
	{
		if(constants.containsKey(oldName))
		{
			constants.put(oldName, constant);
		}
	}

	public void addConstant(String name, int value) {	
		if(!constants.containsKey(name))
			constants.put(name, new Constant(name, value));
		else
		{
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"A constant with the specified name already exists.",
					"Constant exists",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void removeConstant(String name){
		if(constants.containsKey(name))
			constants.remove(name);
	}
}
