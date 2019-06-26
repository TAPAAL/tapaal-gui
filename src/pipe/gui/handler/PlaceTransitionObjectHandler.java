package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JOptionPane;

import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.GuiFrame;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
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
import dk.aau.cs.util.RequireException;

/**
 * Class used to implement methods corresponding to mouse events on places.
 * 
 * @author Pere Bonet - changed the mousePressed method to only allow the
 *         creation of an arc by left-clicking
 * @author Matthew Worthington - modified the handler which was causing the null
 *         pointer exceptions and incorrect petri nets xml representation.
 * @author Kenneth Yrke Joergensen - cleanup removed unused stuff, and refactored 
 * 	       bad design.
 */

public class PlaceTransitionObjectHandler extends PetriNetObjectHandler {
	private static final String ERROR_MSG_TWO_ARCS = "We do not allow two arcs from a place to a transition or a transition to a place.";

	// constructor passing in all required objects
	public PlaceTransitionObjectHandler(PlaceTransitionObject obj) {
		super(obj);
		enablePopup = true;
	}

	private void createArc(Arc newArc, PlaceTransitionObject currentObject) {
		newArc.setZoom(CreateGui.getDrawingSurface().getZoom());
		//Need to be casted to cointainer, as we only add it to the canvas but not the model
		((Container)CreateGui.getDrawingSurface()).add(newArc);
		currentObject.addConnectFrom(newArc);
		CreateGui.getDrawingSurface().createArc = newArc;
		// addPetriNetObject a handler for shift & esc actions drawing arc
		// this is removed when the arc is finished drawing:
		newArc.requestFocusInWindow();
		newArc.setSelectable(false);
		newArc.enableDrawingKeyBindings();
	}

	// Disable key bindings that are only available when drawing arcs.
	private void sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(Arc newArc){
		newArc.setSelectable(true);
		newArc.sealArc();
		newArc.disableDrawingKeyBindings();
		CreateGui.getDrawingSurface().createArc = null;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		// Prevent creating arcs with a right-click or a middle-click
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		PlaceTransitionObject currentObject = (PlaceTransitionObject) myObject;
		if (CreateGui.getDrawingSurface().createArc == null) {
			switch (CreateGui.getApp().getMode()) {
			case TAPNARC:{

				if (currentObject instanceof TimedPlaceComponent) {
					Arc arc = new TimedInputArcComponent(currentObject);
					createArc(arc, currentObject);
				} else {
					Arc arc = new TimedOutputArcComponent(currentObject);
					createArc(arc, currentObject);
				}
				break;
			}
			case TAPNINHIBITOR_ARC:{

				if (currentObject instanceof Place) {
					Arc arc = new TimedInhibitorArcComponent(currentObject);
					createArc(arc, currentObject);
				}

				break;
			}
			case TRANSPORTARC:{
				boolean isInPreSet = false;
				if (currentObject instanceof Place) {
					isInPreSet = true;
					Arc arc = new TimedTransportArcComponent(currentObject, 1, isInPreSet);
					createArc(arc, currentObject);
				}
				break;
			}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		DrawingSurfaceImpl view = CreateGui.getDrawingSurface();
		UndoManager undoManager = view.getUndoManager();
		GuiFrame app = CreateGui.getApp();

		super.mouseReleased(e);

		PlaceTransitionObject currentObject = (PlaceTransitionObject) myObject;

		Arc createArc = view.createArc;
		if (createArc == null){
			//If we have no arc, we have nothing to do
			return;
		}

		//Check if the mouse was moved since key down event, and we are looking at the target
		//Break the drawing if this is the case
		if (currentObject != createArc.getSource()){
			if (createArc.getTarget() == null) {
				cleanupArc(createArc, view);
				return;
			}
		}

		switch (app.getMode()) {
			case TAPNINHIBITOR_ARC:
				createInhibitorArc(view, undoManager, currentObject);
				break;
			case TRANSPORTARC:
				createTransportArc(view, undoManager, currentObject);
				break;
			case TAPNARC:
				createTimedArc(view, undoManager, currentObject);
				break;
		}
	}

	private void createTimedArc(DrawingSurfaceImpl view, UndoManager undoManager, PlaceTransitionObject currentObject) {
		Arc timedArcToCreate = view.createArc;

		if (currentObject != timedArcToCreate.getSource()) {

			// Are we creating an Input or Output arc_
			if (!(timedArcToCreate instanceof TimedInputArcComponent)) {

				// Set underlying TimedInputArc
				TimedOutputArcComponent outputArc = (TimedOutputArcComponent) timedArcToCreate;

				try {

					TimedOutputArc timedOutputArc = new TimedOutputArc(
							((TimedTransitionComponent) outputArc.getSource()).underlyingTransition(),
							((TimedPlaceComponent) outputArc.getTarget()).underlyingPlace());
					view.getModel().add(timedOutputArc);
					outputArc.setUnderlyingArc(timedOutputArc);

				} catch (RequireException ex) {
					cleanupArc(timedArcToCreate, view);
					JOptionPane.showMessageDialog(
									CreateGui.getApp(),
									"There was an error drawing the arc. Possible problems:\n"
											+ " - There is already an arc between the selected place and transition\n"
											+ " - You are attempting to draw an arc between a shared transition and a shared place",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}


				undoManager.newEdit(); // new "transaction""

				undoManager.addEdit(
					new AddTimedOutputArcCommand(
						(TimedOutputArcComponent) timedArcToCreate,
						view.getModel(),
						view.getGuiModel()
					)
				);

				// else source is a place (not transition)
			} else {

				// Set underlying TimedInputArc
				TimedInputArcComponent timedArc = (TimedInputArcComponent) timedArcToCreate;
				try {

					TimedInputArc tia = new TimedInputArc(
							((TimedPlaceComponent) timedArc.getSource()).underlyingPlace(),
							((TimedTransitionComponent) timedArc.getTarget()).underlyingTransition(),
							TimeInterval.ZERO_INF);
					view.getModel().add(tia);
					timedArc.setUnderlyingArc(tia);

				} catch (RequireException ex) {
					cleanupArc(timedArcToCreate, view);
					JOptionPane
							.showMessageDialog(
									CreateGui.getApp(),
									"There was an error drawing the arc. Possible problems:\n"
											+ " - There is already an arc between the selected place and transition\n"
											+ " - You are attempting to draw an arc between a shared transition and a shared place",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				undoManager.newEdit(); // new "transaction""

				undoManager.addEdit(
					new AddTimedInputArcCommand(
						(TimedInputArcComponent) timedArcToCreate,
						view.getModel(),
						view.getGuiModel()
					)
				);

			}

			currentObject.addConnectTo(timedArcToCreate);


			sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(timedArcToCreate);
			removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, timedArcToCreate);

		}
	}

	private void createTransportArc(DrawingSurfaceImpl view, UndoManager undoManager, PlaceTransitionObject currentObject) {
		Arc transportArcToCreate = view.createArc;

		if (currentObject != transportArcToCreate.getSource()) {

			// This is the first step
			if (transportArcToCreate.getSource() instanceof Place) {

				// mikaelhm - Dont allow a transport arc from place to
				// transition if there is another arc.
				boolean existsArc = false;

				// Check if arc has leagal target
				PlaceTransitionObject target = transportArcToCreate.getTarget();
				if (!(target instanceof Transition)) {
					transportArcToCreate.delete();
					return;
				}

				Iterator<Arc> arcsFrom = transportArcToCreate.getSource().getConnectFromIterator();
				// search for pre-existent arcs from transportArcToCreate's source to
				// transportArcToCreate's target
				while (arcsFrom.hasNext()) {
					Arc someArc = (arcsFrom.next());
					if (someArc == transportArcToCreate) {
						break;
					} else if (someArc.getSource() == transportArcToCreate.getSource() && someArc.getTarget() == currentObject) {
						existsArc = true;

						// There already is a arc between this place and transition
						cleanupArc(transportArcToCreate, view);
						JOptionPane.showMessageDialog(
								CreateGui.getApp(),
								ERROR_MSG_TWO_ARCS,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
				if (existsArc) {
					transportArcToCreate.delete();
					return;
				}

				int groupMaxCounter = getTransportArcMaxGroupNumber(transportArcToCreate);
				((TimedTransportArcComponent) transportArcToCreate).setGroupNr(groupMaxCounter + 1);

				currentObject.addConnectTo(transportArcToCreate);
				removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, transportArcToCreate);

				sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(transportArcToCreate);

				// Create the next arc
				TimedTransportArcComponent arc2 = new TimedTransportArcComponent(currentObject, groupMaxCounter + 1, false);

				//Update the partners for the arcs
				TimedTransportArcComponent arc1 = ((TimedTransportArcComponent) transportArcToCreate);

				arc2.setConnectedTo(arc1);
				arc1.setConnectedTo(arc2);

				//Draw part 2 of the transport arc
				createArc(arc2, currentObject);


			} else if (transportArcToCreate.getSource() instanceof Transition) {

				TimedTransportArcComponent arc2 = (TimedTransportArcComponent) transportArcToCreate;
				TimedTransportArcComponent arc1 = arc2.getConnectedTo();

				dk.aau.cs.model.tapn.TransportArc ta;
				try {
					ta = new dk.aau.cs.model.tapn.TransportArc(
							((TimedPlaceComponent) arc1.getSource()).underlyingPlace(),
							((TimedTransitionComponent) arc2.getSource()).underlyingTransition(),
							((TimedPlaceComponent) arc2.getTarget()).underlyingPlace(),
							TimeInterval.ZERO_INF);
					view.getModel().add(ta);
					((TimedTransportArcComponent) transportArcToCreate).setUnderlyingArc(ta);
					arc1.setUnderlyingArc(ta);


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
					return;
				}

				removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, arc2);

				currentObject.addConnectTo(arc2);

				undoManager.newEdit();

				undoManager.addEdit(
						new AddTransportArcCommand(
								arc2,
								arc2.underlyingTransportArc(),
								view.getModel(),
								view.getGuiModel()
						)
				);

				sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(transportArcToCreate);

			}

		}
	}

	private int getTransportArcMaxGroupNumber(Arc transportArcToCreate) {
		int groupMaxCounter = 0;

		for (Object pt : transportArcToCreate.getTarget().getPostset()) {
			if (pt instanceof TimedTransportArcComponent) {
				if (((TimedTransportArcComponent) pt).getGroupNr() > groupMaxCounter) {
					groupMaxCounter = ((TimedTransportArcComponent) pt).getGroupNr();
				}
			}
		}


		return groupMaxCounter;
	}

	private void createInhibitorArc(DrawingSurfaceImpl view, UndoManager undoManager, PlaceTransitionObject currentObject) {
		TimedInhibitorArcComponent createTAPNInhibitorArc = (TimedInhibitorArcComponent) view.createArc;
		if (currentObject != createTAPNInhibitorArc.getSource()) {

			try {
				TimedInhibitorArc tia = new TimedInhibitorArc(
						((TimedPlaceComponent) createTAPNInhibitorArc
								.getSource()).underlyingPlace(),
						((TimedTransitionComponent) createTAPNInhibitorArc
								.getTarget()).underlyingTransition(),
						TimeInterval.ZERO_INF);
				view.getModel().add(tia);
				createTAPNInhibitorArc.setUnderlyingArc(tia);

			} catch (RequireException ex) {
				cleanupArc(createTAPNInhibitorArc, view);
				JOptionPane.showMessageDialog(CreateGui.getApp(),
								"There was an error drawing the arc. Possible problems:\n"
										+ " - There is already an arc between the selected place and transition\n"
										+ " - You are attempting to draw an arc between a shared transition and a shared place",
								"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			createTAPNInhibitorArc.setTarget(currentObject);

			currentObject.addConnectTo(createTAPNInhibitorArc);

			removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, createTAPNInhibitorArc);

			undoManager.addNewEdit(
					new AddTimedInhibitorArcCommand(createTAPNInhibitorArc, view.getModel(), view.getGuiModel())
			);

			sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(createTAPNInhibitorArc);
		}
	}

	private void removeProtoTypeFromViewAndAddNewArcToViewAndModel(DrawingSurfaceImpl view, Arc arcToCreate) {
		// Evil hack to prevent the arc being added to GuiView twice
		//Need to be casted to cointainer, as we only add it to the canvas but not the model (remove is overwritten)
		((Container) CreateGui.getDrawingSurface()).remove(arcToCreate);

		view.getGuiModel().addPetriNetObject(arcToCreate);
		//view.addNewPetriNetObject(arcToCreate);

		//timedArcToCreate.getTransition().updateConnected(); (used to be called only for Outputarc and inhub arc)
	}

	private void cleanupArc(Arc arc, DrawingSurfaceImpl view) {
		arc.delete();
		sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(arc);
		
		view.remove(arc);
		view.repaint();
	}
}
