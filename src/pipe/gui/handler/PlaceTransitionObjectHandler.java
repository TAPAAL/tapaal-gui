package pipe.gui.handler;

import java.awt.event.MouseEvent;
import javax.swing.*;

import dk.aau.cs.model.tapn.*;
import net.tapaal.TAPAAL;
import pipe.gui.CreateGui;
import pipe.gui.canvas.DrawingSurfaceImpl;
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

    @Override
    public JPopupMenu getPopup(MouseEvent e) {
	    JPopupMenu popup = super.getPopup(e);
        if ("DEV".equals(TAPAAL.VERSION)){
            JTextArea pane = new JTextArea();
            pane.setEditable(false);

            String preset = "";
            for (Arc arc : ((PlaceTransitionObject) myObject).getPreset()) {
                preset += "    "  + arc.getId() + "\n";
            }
            String postset = "";
            for (Arc arc : ((PlaceTransitionObject) myObject).getPostset()) {
                postset += "    "  + arc.getId() + "\n";
            }

            pane.setText(
                "(Debug) \n" +
                    "  preset: " +"\n" +
                    preset +
                "  postset: " +"\n"+
                    postset
            );

            popup.insert(pane, 1);
        }
        return popup;
    }

    private void createArc(Arc newArc, PlaceTransitionObject currentObject) {

		//XXX calling zoomUpdate will set the endpoint to 0,0, drawing the arc from source to 0,0
		//to avoid this we change the endpoint to set the end point to the same as the end point
		//needs further refactorings //kyrke 2019-09-05
		newArc.setEndPoint(currentObject.getPositionX(), currentObject.getPositionY(), false);

		CreateGui.getDrawingSurface().addPrototype(newArc);

		CreateGui.getDrawingSurface().createArc = newArc;
		// addPetriNetObject a handler for shift & esc actions drawing arc
		// this is removed when the arc is finished drawing:
		newArc.requestFocusInWindow();
		newArc.setSelectable(false);
		newArc.enableDrawingKeyBindings();
	}

	// Disable key bindings that are only available when drawing arcs.
	private static void sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(Arc newArc){
		newArc.setSelectable(true);
		newArc.sealArc();
		newArc.disableDrawingKeyBindings();
		CreateGui.getDrawingSurface().createArc = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Arc createArc = CreateGui.getDrawingSurface().createArc;
		PlaceTransitionObject pto = (PlaceTransitionObject)myObject;

		if (createArc != null) {
			if (pto.areNotSameType(createArc.getSource())){
				createArc.setTarget(pto);
				createArc.updateArcPosition();
				//pto.updateConnected();
			}
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		Arc createArc = CreateGui.getDrawingSurface().createArc;
		PlaceTransitionObject pto = (PlaceTransitionObject)myObject;
		if (createArc != null) {
			if (!pto.areNotSameType(createArc.getSource())) {
				dispatchToParentWithMouseLocationUpdated(e);
			}
		}

	}

	@Override
	public void mouseExited(MouseEvent e) {
		Arc createArc = CreateGui.getDrawingSurface().createArc;
		PlaceTransitionObject pto = (PlaceTransitionObject)myObject;

		if (createArc != null) {
			//XXX might need to check if target is pto (depends on order of events)
			createArc.setTarget(null);
			//XXX this is bad, we have to clean up internal state manually, should be refactored //kyrke - 2019-11-14
			// Relates to bug #1849786
			if (pto instanceof Transition) {
				((Transition)pto).removeArcCompareObject(createArc);
			}
			createArc.updateArcPosition();
			//pto.removeArcCompareObject(pto);
			//pto.updateConnected();
		}
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
		UndoManager undoManager = CreateGui.getCurrentTab().getUndoManager();
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

			    view.createArc = null;
                CreateGui.getDrawingSurface().removePrototype(timedArcToCreate);
                CreateGui.getCurrentTab().guiModelManager.addTimedOutputArc(
                    view.getGuiModel(),
                    (TimedTransitionComponent) timedArcToCreate.getSource(),
                    (TimedPlaceComponent) timedArcToCreate.getTarget(),
                    timedArcToCreate.getArcPath()
                );

			} else {

                view.createArc = null;
                CreateGui.getDrawingSurface().removePrototype(timedArcToCreate);
                CreateGui.getCurrentTab().guiModelManager.addTimedInputArc(
                    view.getGuiModel(),
                    (TimedPlaceComponent)timedArcToCreate.getSource(),
                    (TimedTransitionComponent)timedArcToCreate.getTarget(),
                    timedArcToCreate.getArcPath()
                );

			}


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
					cleanupArc(transportArcToCreate, view);
					return;
				}

                // search for pre-existent arcs from transportArcToCreate's source to
				// transportArcToCreate's target
                for (Arc someArc : transportArcToCreate.getSource().getPreset()) {
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
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
				if (existsArc) {
					cleanupArc(transportArcToCreate, view);
					return;
				}

				int groupMaxCounter = getTransportArcMaxGroupNumber(transportArcToCreate);
				((TimedTransportArcComponent) transportArcToCreate).setGroupNr(groupMaxCounter + 1);

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
							TimeInterval.ZERO_INF
                    );
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
							"Error", JOptionPane.ERROR_MESSAGE
                    );
					return;
				}

				removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, arc2);
				removeProtoTypeFromViewAndAddNewArcToViewAndModel(view, arc1);

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
		TimedInhibitorArcComponent timedArcToCreate = (TimedInhibitorArcComponent) view.createArc;
		if (currentObject != timedArcToCreate.getSource()) {

            view.createArc = null;
            CreateGui.getDrawingSurface().removePrototype(timedArcToCreate);
            CreateGui.getCurrentTab().guiModelManager.addInhibitorArc(
                view.getGuiModel(),
                (TimedPlaceComponent) timedArcToCreate.getSource(),
                (TimedTransitionComponent) timedArcToCreate.getTarget(),
                timedArcToCreate.getArcPath()
            );
		}
	}

	private void removeProtoTypeFromViewAndAddNewArcToViewAndModel(DrawingSurfaceImpl view, Arc arcToCreate) {

		CreateGui.getDrawingSurface().removePrototype(arcToCreate);

		view.getGuiModel().addPetriNetObject(arcToCreate);
		//view.addNewPetriNetObject(arcToCreate);

		//timedArcToCreate.getTransition().updateConnected(); (used to be called only for Outputarc and inhub arc)
	}

	public static void cleanupArc(Arc arc, DrawingSurfaceImpl view) {
		//XXX this is problematic if we are creating a transport arc and it part2, then part1 is never cleanup!

		//Called delete, only removes is from view since it finished, should be same af view.remove()
		//arc.delete();

		if (arc instanceof TimedTransportArcComponent) {
			TimedTransportArcComponent partner = ((TimedTransportArcComponent)arc).getConnectedTo();
			if (partner != null) {
				partner.disableDrawingKeyBindings();
				view.remove(partner);
			}
		}

		sealArcAndRemoveDrawKeyBindingsAndResetCreateArc(arc);
		
		view.remove(arc);
		view.repaint();
	}
}
