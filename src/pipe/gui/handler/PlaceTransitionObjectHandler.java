package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JOptionPane;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TimedInhibitorArcComponent;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArcComponent;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.GuiFrame;
import pipe.gui.Pipe;
import pipe.gui.undo.AddTimedInhibitorArcCommand;
import pipe.gui.undo.AddTimedInputArcCommand;
import pipe.gui.undo.AddTimedOutputArcCommand;
import pipe.gui.undo.AddTransportArcCommand;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.RequireException;

/**
 * Class used to implement methods corresponding to mouse events on places.
 * 
 * @author Pere Bonet - changed the mousePressed method to only allow the
 *         creation of an arc by left-clicking
 * @author Matthew Worthington - modified the handler which was causing the null
 *         pointer exceptions and incorrect petri nets xml representation.
 */

public class PlaceTransitionObjectHandler extends PetriNetObjectHandler {
	private static final String ERROR_MSG_TWO_ARCS = "We do not allow two arcs from a place to a transition or a transition to a place.";
	private DataLayer guiModel;
	private TimedArcPetriNet model;
	ArcKeyboardEventHandler keyHandler = null;

	public PlaceTransitionObjectHandler(Container contentpane,
			PlaceTransitionObject obj, DataLayer guiModel,
			TimedArcPetriNet model) {
		this(contentpane, obj);
		this.guiModel = guiModel;
		this.model = model;
	}

	// constructor passing in all required objects
	public PlaceTransitionObjectHandler(Container contentpane,
			PlaceTransitionObject obj) {
		super(contentpane, obj);
		enablePopup = true;
	}

	private void createArc(Arc newArc, PlaceTransitionObject currentObject) {
		newArc.setZoom(CreateGui.getView().getZoom());
		contentPane.add(newArc);
		currentObject.addConnectFrom(newArc);
		CreateGui.getView().createArc = newArc;
		// addPetriNetObject a handler for shift & esc actions drawing arc
		// this is removed when the arc is finished drawing:
		keyHandler = new ArcKeyboardEventHandler(newArc);
		newArc.addKeyListener(keyHandler);
		newArc.requestFocusInWindow();
		newArc.setSelectable(false);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		// Prevent creating arcs with a right-click or a middle-click
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		PlaceTransitionObject currentObject = (PlaceTransitionObject) myObject;
		
		switch (CreateGui.getApp().getMode()) {

		case Pipe.FAST_TAPNPLACE:
		case Pipe.FAST_TAPNTRANSITION:
		case Pipe.TAPNARC:
			if (CreateGui.getView().createArc == null) {

				if (Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET) {
					// We only create a TAPNArc if source is not at TimedPlace
					// NormalArc tmparc = new NormalArc(currentObject);

					if (currentObject instanceof TimedPlaceComponent) {
						Arc arc = new TimedInputArcComponent(currentObject);
						createArc(arc, currentObject);
					} else {
						Arc arc = new TimedOutputArcComponent(currentObject);
						createArc(arc, currentObject);
					}
				} else {
					// XXX - Dont know why this has to be here, but i kind of
					// works now?? -- kyrke

				}
			}

			break;

		case Pipe.TAPNINHIBITOR_ARC:
			if (CreateGui.getView().createArc == null) {
					if (currentObject instanceof Place) {
						Arc arc = new TimedInhibitorArcComponent(currentObject);
						createArc(arc, currentObject);
					}
			}
			break;
		case Pipe.TRANSPORTARC:
			if (CreateGui.getView().createArc == null) {
				boolean isInPreSet = false;
				if (currentObject instanceof Place) {
					isInPreSet = true;
					Arc arc = new TransportArcComponent(currentObject, 1,
									isInPreSet);
					createArc(arc, currentObject);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		DrawingSurfaceImpl view = CreateGui.getView();
		UndoManager undoManager = view.getUndoManager();
		GuiFrame app = CreateGui.getApp();

		super.mouseReleased(e);

		PlaceTransitionObject currentObject = (PlaceTransitionObject) myObject;

		//Check if the mouse was moved since key down event, and we are looking at the target
		//Break the drawing if this is the case
		Arc createArc = view.createArc; 
		if (createArc != null && currentObject != createArc.getSource()){
		if (createArc.getTarget() == null) {
			cleanupArc(createArc, view);
			return;
		}
		}
		
		switch (app.getMode()) {
		case Pipe.TAPNINHIBITOR_ARC:
			TimedInhibitorArcComponent createTAPNInhibitorArc = (TimedInhibitorArcComponent) view.createArc;
			if (createTAPNInhibitorArc != null) {
				if (currentObject != createTAPNInhibitorArc.getSource()) {

					try {
						dk.aau.cs.model.tapn.TimedInhibitorArc tia = new TimedInhibitorArc(
								((TimedPlaceComponent) createTAPNInhibitorArc
										.getSource()).underlyingPlace(),
								((TimedTransitionComponent) createTAPNInhibitorArc
										.getTarget()).underlyingTransition(),
								TimeInterval.ZERO_INF);
						model.add(tia);
						createTAPNInhibitorArc.setUnderlyingArc(tia);
						createTAPNInhibitorArc.updateWeightLabel(true);
					} catch (RequireException ex) {
						cleanupArc(createTAPNInhibitorArc, view);
						JOptionPane.showMessageDialog(CreateGui.getApp(),
										"There was an error drawing the arc. Possible problems:\n"
												+ " - There is already an arc between the selected place and transition\n"
												+ " - You are attempting to draw an arc between a shared transition and a shared place",
										"Error", JOptionPane.ERROR_MESSAGE);
						break;
					}

					createTAPNInhibitorArc.setSelectable(true);
					createTAPNInhibitorArc.setTarget(currentObject);

					currentObject.addConnectTo(createTAPNInhibitorArc);
					createTAPNInhibitorArc.getTransition().updateConnected();

					// Evil hack to prevent the arc being added to GuiView twice
					contentPane.remove(createTAPNInhibitorArc);

					guiModel.addArc(createTAPNInhibitorArc);

					view.addNewPetriNetObject(createTAPNInhibitorArc);

					undoManager.addNewEdit(new AddTimedInhibitorArcCommand(
							createTAPNInhibitorArc, model, guiModel, view));

					// arc is drawn, remove handler:
					createTAPNInhibitorArc.removeKeyListener(keyHandler);
					keyHandler = null;
					
					view.createArc = null;
				}
			}
			break;
		case Pipe.TRANSPORTARC:

			Arc transportArcToCreate = view.createArc;
			if (transportArcToCreate != null) {
				if (currentObject != transportArcToCreate.getSource()) {

					transportArcToCreate.setSelectable(true);

					// This is the first step
					if (transportArcToCreate.getSource() instanceof Place) {

						// mikaelhm - Dont allow a transport arc from place to
						// transition if there is another arc.
						boolean existsArc = false;

						// Check if arc has leagal target
						PlaceTransitionObject target = transportArcToCreate
								.getTarget();
						if (!(target instanceof Transition && target != null)) {
							System.err.println("Error creating transport arc, invalid target");
							transportArcToCreate.delete();
							break;
						}

						Iterator<Arc> arcsFrom = transportArcToCreate
								.getSource().getConnectFromIterator();
						// search for pre-existent arcs from
						// transportArcToCreate's source to
						// transportArcToCreate's target
						while (arcsFrom.hasNext()) {
							Arc someArc = (arcsFrom.next());
							if (someArc == transportArcToCreate) {
								break;
							} else if (someArc.getSource() == transportArcToCreate.getSource() && someArc.getTarget() == currentObject) {
								existsArc = true;

								if (someArc instanceof TimedInhibitorArcComponent) {
									// user has drawn a transport arc where
									// there is
									// a TAPNInhibitorArc arc already - This
									// does not make sense.
									cleanupArc(transportArcToCreate, view);
									System.out.println(ERROR_MSG_TWO_ARCS);
									JOptionPane.showMessageDialog(
													CreateGui.getApp(),
													ERROR_MSG_TWO_ARCS,
													"Error",
													JOptionPane.ERROR_MESSAGE);

								} else if (someArc instanceof TransportArcComponent) {
									// user has drawn a transport arc where
									// there is
									// a transport arc already - We do not allow
									// that.
									cleanupArc(transportArcToCreate, view);
									System.out.println(ERROR_MSG_TWO_ARCS);
									JOptionPane.showMessageDialog(CreateGui
											.getApp(), ERROR_MSG_TWO_ARCS,
											"Error", JOptionPane.ERROR_MESSAGE);

								} else if (someArc instanceof TimedOutputArcComponent) {
									// user has drawn a transport arc where
									// there is
									// a normal arc already - we increment arc's
									// weight
									cleanupArc(transportArcToCreate, view);
									System.out.println(ERROR_MSG_TWO_ARCS);
									JOptionPane.showMessageDialog(CreateGui
											.getApp(), ERROR_MSG_TWO_ARCS,
											"Error", JOptionPane.ERROR_MESSAGE);

								} else {
									// This should not happen - since all types
									// of arcs are listed above.
								}
								break;
							}
						}
						if (existsArc) {
							transportArcToCreate.delete();
							break;
						}

						int groupMaxCounter = 0;

						for (Object pt : transportArcToCreate.getTarget()
								.getPostset()) {
							if (pt instanceof TransportArcComponent) {
								if (((TransportArcComponent) pt).getGroupNr() > groupMaxCounter) {
									groupMaxCounter = ((TransportArcComponent) pt).getGroupNr();
								}
							}
						}

						((TransportArcComponent) transportArcToCreate).setGroupNr(groupMaxCounter + 1);

						currentObject.addConnectTo(transportArcToCreate);

						// Evil hack to prevent the arc being added to GuiView
						// twice
						contentPane.remove(transportArcToCreate);

						guiModel.addArc((TimedOutputArcComponent) transportArcToCreate);
						view.addNewPetriNetObject(transportArcToCreate);

						// arc is drawn, remove handler:
						transportArcToCreate.removeKeyListener(keyHandler);
						keyHandler = null;
						view.createArc = null;
						
						// Create the next arc
						TransportArcComponent arc2 = new TransportArcComponent(currentObject, groupMaxCounter + 1, false);
						
						//Update the partners for the arcs
						TransportArcComponent arc1 = ((TransportArcComponent) transportArcToCreate);
						
						arc2.setConnectedTo(arc1);
						arc1.setConnectedTo(arc2);
										
						//Draw part 2 of the transport arc
						createArc(arc2, currentObject);
						

					} else if (transportArcToCreate.getSource() instanceof Transition) {
						
						TransportArcComponent arc2 = (TransportArcComponent) transportArcToCreate;
						TransportArcComponent arc1 = arc2.getConnectedTo();
						
						dk.aau.cs.model.tapn.TransportArc ta;
						try {
							ta = new dk.aau.cs.model.tapn.TransportArc(
									((TimedPlaceComponent) arc1.getSource()).underlyingPlace(),
									((TimedTransitionComponent) arc2.getSource()).underlyingTransition(),
									((TimedPlaceComponent) arc2.getTarget()).underlyingPlace(),
									TimeInterval.ZERO_INF);
							model.add(ta);
							((TransportArcComponent) transportArcToCreate).setUnderlyingArc(ta);
							arc1.setUnderlyingArc(ta);
							arc1.updateWeightLabel(true);
							((TransportArcComponent) transportArcToCreate).updateWeightLabel(true);
						} catch (RequireException ex) {
							cleanupArc(arc1, view);
							cleanupArc(arc2, view);
							JOptionPane.showMessageDialog(
											CreateGui.getApp(),
											"There was an error drawing the arc. Possible problems:\n"
													+ " - There is already an arc between the source place and transition\n"
													+ " - There is already an arc between the transtion and the target place\n"
													+ " - You are attempting to draw an arc between a shared transition and a shared place",
											"Error", JOptionPane.ERROR_MESSAGE);
							break;
						}

						// Evil hack to prevent the arc being added to GuiView
						// twice
						contentPane.remove(arc2);

						guiModel.addArc(arc2);
						view.addNewPetriNetObject(arc2);
						
						undoManager.newEdit();

						undoManager.addEdit(
								new AddTransportArcCommand(
										arc2,
										(dk.aau.cs.model.tapn.TransportArc) (arc2.underlyingTransportArc()), 
										model, 
										guiModel, 
										view));

						// arc is drawn, remove handler:
						transportArcToCreate.removeKeyListener(keyHandler);
						keyHandler = null;
					
						view.createArc = null;

						arc2.setGroupNr(arc1.getGroupNr());

						
					}

				}

			}

			break;
		case Pipe.TAPNARC:

			Arc timedArcToCreate = view.createArc;

			if (timedArcToCreate != null) {
				if (currentObject != timedArcToCreate.getSource()) {
					view.createArc = null;

					timedArcToCreate.setSelectable(true);

					// We create NormalArcs when source of arc is Transition(
					// since there are no intervals on output arcs.) ...except
					// if the arc is a TransportArc
					if (!(timedArcToCreate instanceof TimedInputArcComponent)) {

						// Set underlying TimedInputArc
						TimedOutputArcComponent outputArc = (TimedOutputArcComponent) timedArcToCreate;
						
						try {
							if(hasArcFromTransitionToPlace(model,((TimedTransitionComponent) outputArc.getSource()), ((TimedPlaceComponent) outputArc.getTarget()))){
								throw new RequireException(ERROR_MSG_TWO_ARCS);
							}
							
							dk.aau.cs.model.tapn.TimedOutputArc timedOutputArc = new TimedOutputArc(
									((TimedTransitionComponent) outputArc.getSource()).underlyingTransition(),
									((TimedPlaceComponent) outputArc.getTarget()).underlyingPlace());
							model.add(timedOutputArc);
							outputArc.setUnderlyingArc(timedOutputArc);
							outputArc.updateWeightLabel(true);
						} catch (RequireException ex) {
							cleanupArc(timedArcToCreate, view);
							JOptionPane.showMessageDialog(
											CreateGui.getApp(),
											"There was an error drawing the arc. Possible problems:\n"
													+ " - There is already an arc between the selected place and transition\n"
													+ " - You are attempting to draw an arc between a shared transition and a shared place",
											"Error", JOptionPane.ERROR_MESSAGE);
							break;
						}
						currentObject.addConnectTo(timedArcToCreate);

						// Evil hack to prevent the arc being added to GuiView
						// twice
						contentPane.remove(timedArcToCreate);

						guiModel.addArc((TimedOutputArcComponent) timedArcToCreate);
						view.addNewPetriNetObject(timedArcToCreate);
						
						undoManager.newEdit(); // new "transaction""
						
						undoManager.addEdit(new AddTimedOutputArcCommand(
								(TimedOutputArcComponent) timedArcToCreate,
								model, guiModel, view));

						// else source is a place (not transition)
					} else {

						// Set underlying TimedInputArc
						TimedInputArcComponent timedArc = (TimedInputArcComponent) timedArcToCreate;
						try {
							if(hasArcFromPlaceToTransition(model,((TimedPlaceComponent) timedArc.getSource()), ((TimedTransitionComponent) timedArc.getTarget()))){
								throw new RequireException("Cannot have two arcs between the same place and transition");
							}
							
							dk.aau.cs.model.tapn.TimedInputArc tia = new TimedInputArc(
									((TimedPlaceComponent) timedArc.getSource()).underlyingPlace(),
									((TimedTransitionComponent) timedArc.getTarget()).underlyingTransition(),
									TimeInterval.ZERO_INF);
							model.add(tia);
							timedArc.setUnderlyingArc(tia);
							timedArc.updateWeightLabel(true);
						} catch (RequireException ex) {
							cleanupArc(timedArcToCreate, view);
							JOptionPane
									.showMessageDialog(
											CreateGui.getApp(),
											"There was an error drawing the arc. Possible problems:\n"
													+ " - There is already an arc between the selected place and transition\n"
													+ " - You are attempting to draw an arc between a shared transition and a shared place",
											"Error", JOptionPane.ERROR_MESSAGE);
							break;
						}

						currentObject.addConnectTo(timedArcToCreate);
						timedArcToCreate.getTransition().updateConnected();

						// Evil hack to prevent the arc being added to GuiView
						// twice
						contentPane.remove(timedArcToCreate);
						guiModel
								.addArc((TimedOutputArcComponent) timedArcToCreate);
						view.addNewPetriNetObject(timedArcToCreate);

						undoManager.newEdit(); // new "transaction""

						undoManager.addEdit(new AddTimedInputArcCommand(
								(TimedInputArcComponent) timedArcToCreate,
								model, guiModel, view));

					}
					// arc is drawn, remove handler:
					timedArcToCreate.removeKeyListener(keyHandler);
					keyHandler = null;
					
					view.createArc = null;
				}
			}
			break;
		default:
			break;
		}
	}

	private boolean hasArcFromTransitionToPlace(TimedArcPetriNet model, TimedTransitionComponent transition, TimedPlaceComponent place) {
		return model.hasArcFromTransitionToPlace(transition.underlyingTransition(), place.underlyingPlace());
	}
	
	private boolean hasArcFromPlaceToTransition(TimedArcPetriNet model, TimedPlaceComponent place, TimedTransitionComponent transition) {
		return model.hasArcFromPlaceToTransition(place.underlyingPlace(), transition.underlyingTransition());
	}

	private void cleanupArc(Arc arc, DrawingSurfaceImpl view) {
		arc.delete();
		arc.removeKeyListener(keyHandler);
		keyHandler = null;
		view.remove(arc);
		view.createArc = null;
		view.repaint();
	}
}
