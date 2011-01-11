package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.translations.ReductionOption;

import pipe.dataLayer.AnnotationNote;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Note;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.GuiFrame;
import pipe.gui.Pipe;
import pipe.gui.Zoomable;
import pipe.gui.handler.AnimationHandler;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.handler.ArcHandler;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.handler.TAPNTransitionHandler;
import pipe.gui.handler.TimedArcHandler;
import pipe.gui.handler.TransitionHandler;
import pipe.gui.handler.TransportArcHandler;

public class TimedArcPetriNetFactory {
	
	private HashMap<TimedTransitionComponent, TransportArcComponent> presetArcs;
	private HashMap<TimedTransitionComponent, TransportArcComponent> postsetArcs;
	private HashMap<TransportArcComponent, TimeInterval> transportArcsTimeIntervals;
	private TimedArcPetriNet tapn;
	private DataLayer guiModel;
	private TimedMarking initialMarking;
	private ArrayList<TAPNQuery> queries;
	private DrawingSurfaceImpl drawingSurface;
	
	public TimedArcPetriNetFactory(DrawingSurfaceImpl drawingSurfaceImpl)
	{
		presetArcs = new HashMap<TimedTransitionComponent, TransportArcComponent>();
		postsetArcs = new HashMap<TimedTransitionComponent, TransportArcComponent>();
		transportArcsTimeIntervals = new HashMap<TransportArcComponent, TimeInterval>();
		initialMarking = new TimedMarking();
		queries = new ArrayList<TAPNQuery>();
		this.drawingSurface = drawingSurfaceImpl;
	}
	
	public Template<TimedArcPetriNet> createTimedArcPetriNetFromPNML(Node tapnNode) {
		initialMarking = new TimedMarking();
		if(tapnNode instanceof Element) {
			String name = getTAPNName((Element)tapnNode);
			tapn = new TimedArcPetriNet(name);
		}
		else {
			tapn = new TimedArcPetriNet();
		}
		guiModel = new DataLayer();
		
		Node node = null;
		NodeList nodeList = null;

		try {
			
			nodeList = tapnNode.getChildNodes();
			for(int i = 0 ; i < nodeList.getLength() ; i++) {
				node = nodeList.item(i);
					parseElement(node);
			}
			
			tapn.setMarking(initialMarking);
			guiModel.buildConstraints();
			

		} catch (Exception e) {
			System.out.println("runtime except");
			throw new RuntimeException(e);
		}
		
		return new Template<TimedArcPetriNet>(tapn,guiModel);
	}
	
	public Iterable<TAPNQuery> getQueries(){
		return queries;
	}


	private String getTAPNName(Element tapnNode) {
			String name = tapnNode.getAttribute("name");
			
			if(name == null || name.equals(""))
				name = tapnNode.getAttribute("id");
			
			return name;
	}

	private void parseElement(Node node) {
		Element element;
		if(node instanceof Element) {
			element = (Element)node;
			if ("labels".equals(element.getNodeName())){
				createAndAddAnnotation(element);
			}  else if("place".equals(element.getNodeName())){
				createAndAddPlace(element);
			} else if ("transition".equals(element.getNodeName())){
				createAndAddTransition(element);
			} else if ("arc".equals(element.getNodeName())) {
				createAndAddArc(element);         
			} else if( "queries".equals(element.getNodeName()) ){
				TAPNQuery query = createQuery(element);
				if(query != null)
					queries.add(query);
			} else if ("constant".equals(element.getNodeName())){
				String name = element.getAttribute("name");
				int value = Integer.parseInt(element.getAttribute("value"));
				if(!name.isEmpty() && value >= 0)
					guiModel.addConstant(name, value);
			} else {
				System.out.println("!" + element.getNodeName());
			}
		}
	} 
	
	public void createAndAddAnnotation (Element inputLabelElement) {
		int positionXInput = 0;
		int positionYInput = 0;
		int widthInput = 0;
		int heightInput = 0;
		boolean borderInput = true;

		String positionXTempStorage = inputLabelElement.getAttribute("x");
		String positionYTempStorage = inputLabelElement.getAttribute("y");
		String widthTemp = inputLabelElement.getAttribute("width");
		String heightTemp = inputLabelElement.getAttribute("height");
		String borderTemp = inputLabelElement.getAttribute("border");
		
		String text = getNode(inputLabelElement, "text").getTextContent();

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
		AnnotationNote an = new AnnotationNote(text, positionXInput, positionYInput, 
				widthInput, heightInput, borderInput);
		guiModel.addPetriNetObject(an);
		drawingSurface.addNewPetriNetObject(an);
	} 
	
	private void createAndAddTransition(Element element){
		double positionXInput = getPositionAttribute(element, "x");
		double positionYInput = getPositionAttribute(element, "y");
		String idInput = element.getAttribute("id");
		String nameInput = getValueChildNodeContentAsString(element, "name");
		double nameOffsetXInput= getNameOffsetAttribute(element, "x");
		double nameOffsetYInput = getNameOffsetAttribute(element, "y");
		boolean timedTransition = getValueChildNodeContentAsBoolean(element, "timed");
		boolean infiniteServer = getValueChildNodeContentAsBoolean(element, "infiniteServer");
		int angle = getValueChildNodeContentAsInt(element, "orientation");
		int priority = getValueChildNodeContentAsInt(element, "priority");

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		TimedTransitionComponent transition =  
			new TimedTransitionComponent(positionXInput, positionYInput,     
					idInput, 
					nameInput, 
					nameOffsetXInput, nameOffsetYInput, 
					timedTransition, 
					infiniteServer,
					angle,
					priority);
		TimedTransition t = new TimedTransition(nameInput);
		transition.setUnderlyingTransition(t);
		guiModel.addPetriNetObject(transition);
		addListeners(transition);
		tapn.add(t);
	}

	private void addListeners(PetriNetObject newObject) {
		if (newObject != null) {
			if (newObject.getMouseListeners().length == 0) {
				if (newObject instanceof Place) {
					// XXX - kyrke
					if (newObject instanceof TimedPlaceComponent) {

						LabelHandler labelHandler =
							new LabelHandler(((Place)newObject).getNameLabel(),
									(Place)newObject);
						((Place)newObject).getNameLabel().addMouseListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler =
							new PlaceHandler(drawingSurface, (Place)newObject, guiModel, tapn);
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);
					}else{

						LabelHandler labelHandler =
							new LabelHandler(((Place)newObject).getNameLabel(),
									(Place)newObject);
						((Place)newObject).getNameLabel().addMouseListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseMotionListener(labelHandler);
						((Place)newObject).getNameLabel().addMouseWheelListener(labelHandler);

						PlaceHandler placeHandler =
							new PlaceHandler(drawingSurface, (Place)newObject);
						newObject.addMouseListener(placeHandler);
						newObject.addMouseWheelListener(placeHandler);
						newObject.addMouseMotionListener(placeHandler);

					}
				} else if (newObject instanceof Transition) {
					TransitionHandler transitionHandler;
					if (newObject instanceof TimedTransitionComponent){
						transitionHandler =
							new TAPNTransitionHandler(drawingSurface, (Transition)newObject, guiModel, tapn);
					}else {
						transitionHandler =
							new TransitionHandler(drawingSurface, (Transition)newObject);	
					}

					LabelHandler labelHandler =
						new LabelHandler(((Transition)newObject).getNameLabel(),
								(Transition)newObject);
					((Transition)newObject).getNameLabel().addMouseListener(labelHandler);
					((Transition)newObject).getNameLabel().addMouseMotionListener(labelHandler);
					((Transition)newObject).getNameLabel().addMouseWheelListener(labelHandler);

					newObject.addMouseListener(transitionHandler);
					newObject.addMouseMotionListener(transitionHandler);
					newObject.addMouseWheelListener(transitionHandler);


					newObject.addMouseListener(new AnimationHandler());

				} else if (newObject instanceof Arc) {
					/* CB - Joakim Byg add timed arcs*/
					if (newObject instanceof TimedInputArcComponent){
						if (newObject instanceof TransportArcComponent){ 
							TransportArcHandler transportArcHandler = new TransportArcHandler(drawingSurface, (Arc)newObject);
							newObject.addMouseListener(transportArcHandler);
							newObject.addMouseWheelListener(transportArcHandler);
							newObject.addMouseMotionListener(transportArcHandler);
						}else {
							TimedArcHandler timedArcHandler = new TimedArcHandler(drawingSurface, (Arc)newObject);
							newObject.addMouseListener(timedArcHandler);
							newObject.addMouseWheelListener(timedArcHandler);
							newObject.addMouseMotionListener(timedArcHandler);
						}
					}else {
						/*EOC*/            	
						ArcHandler arcHandler = new ArcHandler(drawingSurface, (Arc)newObject);
						newObject.addMouseListener(arcHandler);
						newObject.addMouseWheelListener(arcHandler);
						newObject.addMouseMotionListener(arcHandler);
					}
				} else if (newObject instanceof AnnotationNote) {
					AnnotationNoteHandler noteHandler =
						new AnnotationNoteHandler(drawingSurface, (AnnotationNote)newObject);
					newObject.addMouseListener(noteHandler);
					newObject.addMouseMotionListener(noteHandler);
					((Note)newObject).getNote().addMouseListener(noteHandler);
					((Note)newObject).getNote().addMouseMotionListener(noteHandler);
				} 
			}
			newObject.setGuiModel(guiModel);
		}
	}
	
	private boolean getValueChildNodeContentAsBoolean(Element element, String childNodeName) {
		Node node = getNode(element,childNodeName);
		
		if(node instanceof Element) {
			Element e = (Element)node;
			
			String value = getValueChildNodeContent(e);
			
			return Boolean.parseBoolean(value);
		}
		
		return false;
	}

	private double getNameOffsetAttribute(Element element, String coordinateName) {
		Node node = getNode(element,"name");
		
		if(node instanceof Element) {
			Element e = (Element)node;
			
			Element graphics = ((Element)getNode(e,"graphics"));
			String offsetCoordinate = ((Element)getNode(graphics,"offset")).getAttribute(coordinateName);
			if (offsetCoordinate.length() > 0) {
				return Double.valueOf(offsetCoordinate).doubleValue();
			}
		}
		
		return 0.0;
	}

	private Node getNode(Element element, String childNodeName) {
		return element.getElementsByTagName(childNodeName).item(0);
	}
	
	private String getValueChildNodeContent(Element element) {
		return ((Element)getNode(element,"value")).getTextContent();
	}

	private String getValueChildNodeContentAsString(Element element, String childNodeName) {
		Node node = getNode(element,childNodeName);
		
		if(node instanceof Element) {
			Element e = (Element)node;
			
			return getValueChildNodeContent(e);
		}
		
		return "";
	}

	private double getPositionAttribute(Element element, String coordinateName) {
		Node node = getNode(element,"graphics");
		
		if(node instanceof Element){
			Element e = (Element)node;
			
			String posCoordinate = ((Element)getNode(e,"position")).getAttribute(coordinateName);
			if (posCoordinate.length() > 0) {
				return Double.valueOf(posCoordinate).doubleValue();
			}
		}
		
		return 0.0;
	}
	
	private int getValueChildNodeContentAsInt(Element element,String childNodeName) {
		Node node = getNode(element,childNodeName);
		
		if(node instanceof Element) {
			Element e = (Element)node;
			
			String value = getValueChildNodeContent(e);
			
			if(value.length() > 0)
				return Integer.parseInt(value);
		}
		
		return 0;
	}

	private double getMarkingOffsetAttribute(Element element, String coordinateName) {
		Node node = getNode(element,"initialMarking");
		
		if(node instanceof Element) {
			Element e = (Element)node;
			
			Element graphics = ((Element)getNode(e,"graphics"));
			String offsetCoordinate = ((Element)getNode(graphics,"offset")).getAttribute(coordinateName);
			if (offsetCoordinate.length() > 0)
				return Double.parseDouble(offsetCoordinate);
		}
		
		return 0.0;
	}

	private void createAndAddPlace(Element element){
		double positionXInput = getPositionAttribute(element, "x");
		double positionYInput = getPositionAttribute(element, "y");
		String idInput = element.getAttribute("id");
		String nameInput = getValueChildNodeContentAsString(element, "name");
		double nameOffsetXInput = getNameOffsetAttribute(element, "x");
		double nameOffsetYInput = getNameOffsetAttribute(element, "y");
		int initialMarkingInput = getValueChildNodeContentAsInt(element, "initialMarking");
		double markingOffsetXInput = getMarkingOffsetAttribute(element,"x");
		double markingOffsetYInput = getMarkingOffsetAttribute(element,"y");
		int capacityInput = getValueChildNodeContentAsInt(element, "capacity");
		String invariant = getValueChildNodeContentAsString(element, "invariant");

		positionXInput = Grid.getModifiedX(positionXInput);
		positionYInput = Grid.getModifiedY(positionYInput);

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		} 

		Place  place = null;

		if (invariant == null || invariant == ""){
			place = new Place(positionXInput, positionYInput,
					idInput,  
					nameInput, 
					nameOffsetXInput, nameOffsetYInput,
					initialMarkingInput,
					markingOffsetXInput, markingOffsetYInput,  
					capacityInput);

		} else {

			place = new TimedPlaceComponent(positionXInput, positionYInput,
					idInput,  
					nameInput, 
					nameOffsetXInput, nameOffsetYInput,
					initialMarkingInput,
					markingOffsetXInput, markingOffsetYInput,  
					capacityInput, invariant);
			TimedPlace p = new TimedPlace(nameInput, TimeInvariant.parse(invariant));
			
			for(int i = 0; i < initialMarkingInput; i++){
				initialMarking.add(p, new TimedToken(p, new BigDecimal(0.0)));
			}
			
			((TimedPlaceComponent)place).setUnderlyingPlace(p);
			guiModel.addPetriNetObject(place);
			//drawingSurface.addNewPetriNetObject(place);
			addListeners(place);
			tapn.add(p);
			
		}
	}
	
	

	private void createAndAddArc(Element inputArcElement){
		String idInput = inputArcElement.getAttribute("id");
		String sourceInput = inputArcElement.getAttribute("source");
		String targetInput = inputArcElement.getAttribute("target");
		boolean taggedArc = getValueChildNodeContentAsBoolean(inputArcElement, "tagged");
		String inscriptionTempStorage = getValueChildNodeContentAsString(inputArcElement, "inscription");
		
		PlaceTransitionObject sourceIn = guiModel.getPlaceTransitionObject(sourceInput);
		PlaceTransitionObject targetIn = guiModel.getPlaceTransitionObject(targetInput);

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


		String type = "normal";
		type = ((Element)getNode(inputArcElement,"type")).getAttribute("value");


		if (type.equals("tapnInhibitor")){
			
			tempArc = new TimedInhibitorArcComponent(new TimedInputArcComponent(new TimedOutputArcComponent(_startx, _starty, _endx, _endy, sourceIn, targetIn, 1, idInput, taggedArc)), (inscriptionTempStorage!=null ? inscriptionTempStorage : ""));
			TimedPlace place = tapn.getPlaceByName(sourceIn.getName());
			TimedTransition transition = tapn.getTransitionByName(targetIn.getName());
			TimeInterval interval = TimeInterval.parse(inscriptionTempStorage);
			TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, interval);
			
			((TimedInhibitorArcComponent)tempArc).setUnderlyingArc(inhibArc);
			guiModel.addPetriNetObject(tempArc);
			addListeners(tempArc);
			tapn.add(inhibArc);
			
			sourceIn.addConnectFrom(tempArc);
			targetIn.addConnectTo(tempArc);

		} else {



			//XXX - cant check for if arc is timed, check pn-type instead
			if (type.equals("timed")){
				tempArc = new TimedInputArcComponent(new TimedOutputArcComponent (_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						1,
						idInput,
						taggedArc), (inscriptionTempStorage!=null ? inscriptionTempStorage : ""));
				
				TimedPlace place = tapn.getPlaceByName(sourceIn.getName());
				TimedTransition transition = tapn.getTransitionByName(targetIn.getName());
				TimeInterval interval = TimeInterval.parse(inscriptionTempStorage);
				
				TimedInputArc inputArc = new TimedInputArc(place, transition, interval);
				((TimedInputArcComponent)tempArc).setUnderlyingArc(inputArc);
				guiModel.addPetriNetObject(tempArc);
				addListeners(tempArc);
				tapn.add(inputArc);
				
				sourceIn.addConnectFrom(tempArc);
				targetIn.addConnectTo(tempArc);
				
			}else if (type.equals("transport")){
				String[] inscriptionSplit = {};
				if (inscriptionTempStorage.contains(":")){
					inscriptionSplit = inscriptionTempStorage.split(":");
				}
				boolean isInPreSet = false;
				if ( sourceIn instanceof Place ) {
					isInPreSet = true;
				}
				tempArc = new TransportArcComponent( new TimedInputArcComponent( new TimedOutputArcComponent(_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						1,
						idInput,
						taggedArc), inscriptionSplit[0]), Integer.parseInt(inscriptionSplit[1]), isInPreSet );
				
				sourceIn.addConnectFrom(tempArc);
				targetIn.addConnectTo(tempArc);
				
				if(isInPreSet) {
					if(postsetArcs.containsKey((TimedTransitionComponent)targetIn)){
						TransportArcComponent postsetTransportArc = postsetArcs.get((TimedTransitionComponent)targetIn);
						TimedPlace sourcePlace = tapn.getPlaceByName(sourceIn.getName());
						TimedTransition trans = tapn.getTransitionByName(targetIn.getName());
						TimedPlace destPlace = tapn.getPlaceByName(postsetTransportArc.getTarget().getName());
						TimeInterval interval = TimeInterval.parse(inscriptionSplit[0]);
						
						assert(sourcePlace != null);
						assert(trans != null);
						assert(destPlace != null);
						
						TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval);
						
						((TransportArcComponent)tempArc).setUnderlyingArc(transArc);
						postsetTransportArc.setUnderlyingArc(transArc);
						guiModel.addPetriNetObject(tempArc);
						addListeners(tempArc);
						guiModel.addPetriNetObject(postsetTransportArc);
						addListeners(postsetTransportArc);
						tapn.add(transArc);
	
						
						postsetArcs.remove((TimedTransitionComponent)targetIn);
					} else {
						presetArcs.put((TimedTransitionComponent)targetIn, (TransportArcComponent)tempArc);
						transportArcsTimeIntervals.put((TransportArcComponent)tempArc, TimeInterval.parse(inscriptionSplit[0]));
					}
				}
				else {
					if(presetArcs.containsKey((TimedTransitionComponent)sourceIn)) {
						TransportArcComponent presetTransportArc = presetArcs.get((TimedTransitionComponent)sourceIn);
						TimedPlace sourcePlace = tapn.getPlaceByName(presetTransportArc.getSource().getName());
						TimedTransition trans = tapn.getTransitionByName(sourceIn.getName());
						TimedPlace destPlace = tapn.getPlaceByName(targetIn.getName());
						TimeInterval interval = transportArcsTimeIntervals.get((TransportArcComponent) presetTransportArc);
						
						assert(sourcePlace != null);
						assert(trans != null);
						assert(destPlace != null);
						
						TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval);
						
						((TransportArcComponent)tempArc).setUnderlyingArc(transArc);
						presetTransportArc.setUnderlyingArc(transArc);
						guiModel.addPetriNetObject(presetTransportArc);
						addListeners(presetTransportArc);
						guiModel.addPetriNetObject(tempArc);
						addListeners(tempArc);
						tapn.add(transArc);
						
						presetArcs.remove((TimedTransitionComponent)sourceIn);
						transportArcsTimeIntervals.remove((TransportArcComponent)presetTransportArc);
					} else {
						postsetArcs.put((TimedTransitionComponent)sourceIn, (TransportArcComponent)tempArc);
					}
				}
				
			}else {
				tempArc = new TimedOutputArcComponent(	_startx, _starty,
						_endx, _endy,
						sourceIn,
						targetIn,
						//inscribtion is inserted as the arcs weight    				  					
						Integer.valueOf(inscriptionTempStorage),
						idInput,
						taggedArc);
				
				TimedPlace place = tapn.getPlaceByName(targetIn.getName());
				TimedTransition transition = tapn.getTransitionByName(sourceIn.getName());
				
				TimedOutputArc outputArc = new TimedOutputArc(transition, place);
				((TimedOutputArcComponent)tempArc).setUnderlyingArc(outputArc);
				guiModel.addPetriNetObject(tempArc);
				addListeners(tempArc);
				tapn.add(outputArc);
				
				sourceIn.addConnectFrom(tempArc);
				targetIn.addConnectTo(tempArc);
			}

		}
		
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
						String arcTempType = element.getAttribute("curvePoint");
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
	}
	
	public TAPNQuery createQuery(Element queryElement) {
		String comment = getQueryComment(queryElement);
		TraceOption traceOption = getQueryTraceOption(queryElement);
		SearchOption searchOption = getQuerySearchOption(queryElement);
		HashTableSize hashTableSize = getQueryHashTableSize(queryElement);
		ExtrapolationOption extrapolationOption = getQueryExtrapolationOption(queryElement);
		ReductionOption reductionOption = getQueryReductionOption(queryElement);
		int capacity = getQueryCapacity(queryElement);
		
		TCTLAbstractProperty query;
		query = parseQuery(queryElement);

		if(query != null)
			return new TAPNQuery(comment, capacity, query, traceOption, searchOption, reductionOption, hashTableSize, extrapolationOption);
		else
			return null;
	}

	private TCTLAbstractProperty parseQuery(Element queryElement) {
		TCTLAbstractProperty query = null;
		TAPAALQueryParser queryParser = new TAPAALQueryParser();
		
		String queryToParse = getValueChildNodeContentAsString(queryElement,"query");
		
		try{
			query = queryParser.parse(queryToParse);
		}catch (Exception e) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), "TAPAAL encountered an error trying to parse the queries in the model.\n\nThe queries that could not be parsed will not show up in the query list.", "Error Parsing Query", JOptionPane.ERROR_MESSAGE);
			System.err.println("No query was specified: " + e.getStackTrace());
		}
		return query;
	}

	private int getQueryCapacity(Element queryElement) {
		return getValueChildNodeContentAsInt(queryElement, "capacity");
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
		ReductionOption reductionOption;
		try{
			reductionOption = ReductionOption.valueOf(queryElement.getAttribute("reductionOption"));
		}catch (Exception e) {
			reductionOption = ReductionOption.STANDARD;
		}
		return reductionOption;
	}

	private ExtrapolationOption getQueryExtrapolationOption(Element queryElement) {
		ExtrapolationOption extrapolationOption;
		try{
			extrapolationOption = ExtrapolationOption.valueOf(queryElement.getAttribute("extrapolationOption"));		
		}catch (Exception e) {
			extrapolationOption = ExtrapolationOption.AUTOMATIC;
		}
		return extrapolationOption;
	}

	private HashTableSize getQueryHashTableSize(Element queryElement) {
		HashTableSize hashTableSize;
		try{
			hashTableSize = HashTableSize.valueOf(queryElement.getAttribute("hashTableSize"));		
		}catch (Exception e) {
			hashTableSize = HashTableSize.MB_16;
		}
		return hashTableSize;
	}

	private SearchOption getQuerySearchOption(Element queryElement) {
		SearchOption searchOption;
		try{
			searchOption = SearchOption.valueOf(queryElement.getAttribute("searchOption"));
		}catch (Exception e) {
			searchOption = SearchOption.BFS;
		}
		return searchOption;
	}

	private TraceOption getQueryTraceOption(Element queryElement) {
		TraceOption traceOption;
		try{
			traceOption = TraceOption.valueOf(queryElement.getAttribute("traceOption"));
		}catch (Exception e) {
			traceOption = TraceOption.NONE;
		}
		return traceOption;
	}

	private String getQueryComment(Element queryElement) {
		String comment;
		try{
			comment = queryElement.getAttribute("name");
		}catch (Exception e) {
			comment = "No comment specified";
		}
		return comment;
	}

}
