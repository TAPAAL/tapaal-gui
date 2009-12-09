package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JOptionPane;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.InhibitorArc;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNInhibitorArc;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
import pipe.dataLayer.TransportArc;
import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;
import pipe.gui.GuiView;
import pipe.gui.Pipe;
import pipe.gui.undo.UndoManager;
import pipe.gui.undo.AddPetriNetObjectEdit;

/**
 * Class used to implement methods corresponding to mouse events on places.
 *
 * @author Pere Bonet - changed the mousePressed method to only allow the
 * creation of an arc by left-clicking
 * @author Matthew Worthington - modified the handler which was causing the
 * null pointer exceptions and incorrect petri nets xml representation.
 */
public class PlaceTransitionObjectHandler 
        extends PetriNetObjectHandler {
   
   ArcKeyboardEventHandler keyHandler = null;
   
   // constructor passing in all required objects
   public PlaceTransitionObjectHandler(Container contentpane,
           PlaceTransitionObject obj) {
      super(contentpane, obj);
      enablePopup = true;
   }
   
   
   private void createArc(Arc newArc, PlaceTransitionObject currentObject){
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
      
      PlaceTransitionObject currentObject = (PlaceTransitionObject)myObject;
      switch (CreateGui.getApp().getMode()) {

      case Pipe.FAST_TAPNPLACE:
      case Pipe.FAST_TAPNTRANSITION:
      case Pipe.TAPNARC:
    	  // DEBUG KYRKE

    	  //Detect qucik draw mode
    	  if (e.isControlDown()) {
    		  // user is holding Ctrl key; switch to fast mode
    		  if (this.myObject instanceof Place) {
    			  CreateGui.getApp().setFastMode(Pipe.FAST_TAPNTRANSITION);
    		  } else if (this.myObject instanceof Transition) {
    			  
    			  CreateGui.getApp().setFastMode(Pipe.FAST_TAPNPLACE);
    		  }
    	  }

    	  if (CreateGui.getView().createArc == null) {
    		  
    		  if (Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET){
    			  
    			  // We only create a TAPNArc if source is not at TimedPlace
    			  NormalArc tmparc =  new NormalArc(currentObject);

    			  if (tmparc.getSource() instanceof TimedPlace){
    				  createArc(new TimedArc(tmparc), currentObject);  
    			  }else {
    				  createArc(tmparc, currentObject);
    			  }
    		  }else {
    			  //XXX - Dont know why this has to be here, but i kind of works now?? -- kyrke 
    			  
    		  }
    	  }

    	  break;
	
      	case Pipe.ARC:     
      		if (e.isControlDown()) {
               // user is holding Ctrl key; switch to fast mode
               if (this.myObject instanceof Place) {
                  CreateGui.getApp().setFastMode(Pipe.FAST_TRANSITION);
               } else if (this.myObject instanceof Transition) {
                  CreateGui.getApp().setFastMode(Pipe.FAST_PLACE);
               }
            }
      	 case Pipe.TAPNINHIBITOR_ARC:
         case Pipe.INHIBARC:
         case Pipe.FAST_PLACE:
         case Pipe.FAST_TRANSITION:
        	 if (CreateGui.getView().createArc == null) {
        		 if (CreateGui.getApp().getMode() == Pipe.INHIBARC){
        			 if (currentObject instanceof Place) {
        				 createArc(new InhibitorArc(currentObject), currentObject);
        			 }
        		 } else if (CreateGui.getApp().getMode() == Pipe.TAPNINHIBITOR_ARC) {
        			 if (currentObject instanceof Place) {
        				 createArc(new TAPNInhibitorArc(new TimedArc(new NormalArc(currentObject))), currentObject);
        			 }
        		 } else if (CreateGui.getApp().getMode() == Pipe.TAPNARC){
        			 // XXX - kyrke - create only a TimedArc if source is not at TimedPlace
        			 NormalArc tmparc =  new NormalArc(currentObject);

        			 if (tmparc.getSource() instanceof TimedPlace){
        				 createArc(new TimedArc(tmparc), currentObject);  
        			 }else {
        				 createArc(tmparc, currentObject);
        			 }
        		 }
        	 }
            break;
         case Pipe.TRANSPORTARC:
        	 if (CreateGui.getView().createArc == null) {
        		 boolean isInPreSet = false;
        		 if (currentObject instanceof Place) {
        			 isInPreSet = true;
        			 createArc(new TransportArc(currentObject, 1, isInPreSet), currentObject);
        		 } else {
        			 //Do nothing this is not supported
        		 }
        		 
        		 

        	 }else {
        		 //XXX Do stuff - perhaps
        	 }
        	 break;
         default:
        	 break;
      }
   }
   
   
   @Override
public void mouseReleased(MouseEvent e) {
      boolean isNewArc = true; // true if we have to add a new arc to the Petri Net
      boolean fastMode = false;
      
      String error_message_two_arcs = "We do not allow two arcs from a place to a transition or a transition to a place.";
      
      GuiView view = CreateGui.getView();
      DataLayer model = CreateGui.getModel();
      UndoManager undoManager = view.getUndoManager();
      GuiFrame app = CreateGui.getApp();
      
      super.mouseReleased(e);
      
      PlaceTransitionObject currentObject = (PlaceTransitionObject)myObject;
      
      switch (app.getMode()) {
      	 case Pipe.TAPNINHIBITOR_ARC:
      		TAPNInhibitorArc createTAPNInhibitorArc = (TAPNInhibitorArc) view.createArc;
            if (createTAPNInhibitorArc != null) {
               if (!currentObject.getClass().equals(
            		   createTAPNInhibitorArc.getSource().getClass())) {
                  
                  Iterator arcsFrom =
                	  createTAPNInhibitorArc.getSource().getConnectFromIterator();
                  // search for pre-existent arcs from createInhibitorArc's 
                  // source to createInhibitorArc's target
                  while(arcsFrom.hasNext()) {
                     Arc someArc = ((Arc)arcsFrom.next());
                     if (someArc == createTAPNInhibitorArc) {
                        break;
                     } else if (someArc.getTarget() == currentObject &&
                             someArc.getSource() == createTAPNInhibitorArc.getSource()) {
                        isNewArc = false;
                        
                        if (someArc instanceof TAPNInhibitorArc) {
                            // user has drawn an inhibitor arc where there is 
                            // an inhibitor arc already - 
                        	isNewArc = true;
                        } 
                        else if (someArc instanceof TransportArc){
                        	// user has drawn an inhibitor arc where there is 
                            // a transport arc already - mikael: this does not make sense
                        	System.out.println("It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.");
  							 JOptionPane.showMessageDialog(CreateGui.getApp(),
  									 "It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.",
  									 "Error",
  									 JOptionPane.ERROR_MESSAGE);
                        }
                        else if (someArc instanceof NormalArc){
                           // user has drawn an inhibitor arc where there is 
                           // a normal arc already - mikael: this does not make sense
       						 System.out.println("It does not make sense to have both a normal arc and an inhibitor arc from a place to a transition.");
   							 JOptionPane.showMessageDialog(CreateGui.getApp(),
   									 "It does not make sense to have both a normal arc and an inhibitor arc from a place to a transition.",
   									 "Error",
   									 JOptionPane.ERROR_MESSAGE);
       					} else  {
                           // This is not supposed to happen
                        }
                        createTAPNInhibitorArc.delete();
                        someArc.getTransition().removeArcCompareObject(createTAPNInhibitorArc);
                        someArc.getTransition().updateConnected();
                        break;
                     }
                  }
                  
                  if (isNewArc == true) {
                	  createTAPNInhibitorArc.setSelectable(true);
                	  createTAPNInhibitorArc.setTarget(currentObject);
                     
                     currentObject.addConnectTo(createTAPNInhibitorArc);
                    
                     // Evil hack to prevent the arc being added to GuiView twice
                     contentPane.remove(createTAPNInhibitorArc);
                     
                     model.addArc(createTAPNInhibitorArc);
                     
                     view.addNewPetriNetObject(createTAPNInhibitorArc);

                     undoManager.addNewEdit(
                             new AddPetriNetObjectEdit(createTAPNInhibitorArc,
                             view, model));
                     if(createTAPNInhibitorArc.getTarget() == null)
                    	 JOptionPane.showMessageDialog(CreateGui.getApp(),
									 "Whooops, target was not set on last arc, please redraw. DEBUG",
									 "Error",
									 JOptionPane.ERROR_MESSAGE);
						 	 
                  }
                  
                  
                  // arc is drawn, remove handler:
                  createTAPNInhibitorArc.removeKeyListener(keyHandler);
                  keyHandler = null;
                  view.createArc = null;
               }
            }
            break;
         case Pipe.INHIBARC:
            InhibitorArc createInhibitorArc = (InhibitorArc) view.createArc;
            if (createInhibitorArc != null) {
               if (!currentObject.getClass().equals(
                       createInhibitorArc.getSource().getClass())) {
                  
                  Iterator arcsFrom =
                          createInhibitorArc.getSource().getConnectFromIterator();
                  // search for pre-existent arcs from createInhibitorArc's 
                  // source to createInhibitorArc's target
                  while(arcsFrom.hasNext()) {
                     Arc someArc = ((Arc)arcsFrom.next());
                     if (someArc == createInhibitorArc) {
                        break;
                     } else if (someArc.getTarget() == currentObject &&
                             someArc.getSource() == createInhibitorArc.getSource()) {
                        isNewArc = false;
                        if (someArc instanceof NormalArc){
                           // user has drawn an inhibitor arc where there is 
                           // a normal arc already - nothing to do
                        } else if (someArc instanceof InhibitorArc) {
                           // user has drawn an inhibitor arc where there is 
                           // an inhibitor arc already - we increment arc's 
                           // weight
                           int weight = someArc.getWeight();
                           undoManager.addNewEdit(someArc.setWeight(++weight));
                        } else {
                           // This is not supposed to happen
                        }
                        createInhibitorArc.delete();
                        someArc.getTransition().removeArcCompareObject(
                                createInhibitorArc);
                        someArc.getTransition().updateConnected();
                        break;
                     }
                  }
                  
                  if (isNewArc == true) {
                     createInhibitorArc.setSelectable(true);
                     createInhibitorArc.setTarget(currentObject);
                     currentObject.addConnectTo(createInhibitorArc);
                     // Evil hack to prevent the arc being added to GuiView twice
                     contentPane.remove(createInhibitorArc);
                     model.addArc(createInhibitorArc);
                     view.addNewPetriNetObject(createInhibitorArc);
                     undoManager.addNewEdit(
                             new AddPetriNetObjectEdit(createInhibitorArc,
                             view, model));
                  }
                  
                  // arc is drawn, remove handler:
                  createInhibitorArc.removeKeyListener(keyHandler);
                  keyHandler = null;
                  view.createArc = null;
               }
            }
            break;
            
         case Pipe.FAST_TRANSITION:
         case Pipe.FAST_PLACE:
            fastMode = true;
         case Pipe.ARC:
        	 Arc createArc = view.createArc;
        	 if (createArc != null) {
        		 if (currentObject != createArc.getSource()) {
        			 createArc.setSelectable(true);
        			 Iterator arcsFrom = createArc.getSource().getConnectFromIterator();
        			 // search for pre-existent arcs from createArc's source to 
        			 // createArc's target                  
        			 while(arcsFrom.hasNext()) {
        				 Arc someArc = ((Arc)arcsFrom.next());
        				 if (someArc == createArc) {
        					 break;
        				 } else if (someArc.getSource() == createArc.getSource() &&
        						 someArc.getTarget() == currentObject) {
        					 isNewArc = false;

        					 if (someArc instanceof NormalArc) {
        						 // user has drawn a normal arc where there is 
        						 // a normal arc already - we increment arc's weight

        						 if (!(Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET)){
        							 int weight = someArc.getWeight();
        							 undoManager.addNewEdit(
        									 someArc.setWeight(++weight));
        						 }else{
        							 //System.out.println("We dont allow more than one arc from place to transition or transition to place");
        							 //JOptionPane.showMessageDialog(CreateGui.getApp(),
        								//	 "We dont allow more than one arc from place to transition or transition to place!",
        								//	 "Error",
        								//	 JOptionPane.ERROR_MESSAGE);
        						 }
        					 } else{
        						 // user has drawn a normal arc where there is 
        						 // an inhibitor arc already - nothing to do
        						 //System.out.println("DEBUG: arc normal i arc inhibidor!");
        					 }
        					 createArc.delete();
        					 someArc.getTransition().removeArcCompareObject(createArc);
        					 someArc.getTransition().updateConnected();
        					 break; 
        				 }
        			 }

        			 NormalArc inverse = null;
        			 if (isNewArc == true) {
        				 createArc.setTarget(currentObject);

        				 //check if there is an inverse arc
        				 Iterator arcsFromTarget =
        					 createArc.getTarget().getConnectFromIterator();
        				 while (arcsFromTarget.hasNext()) {
        					 Arc anArc = (Arc)arcsFromTarget.next();
        					 if (anArc.getTarget() == createArc.getSource()) {
        						 if (anArc instanceof NormalArc) {
        							 inverse = (NormalArc)anArc;
        							 // inverse arc found
        							 if (inverse.hasInverse()){
        								 // if inverse arc has an inverse arc, it means
        								 System.out.println(error_message_two_arcs);
        	    						 JOptionPane.showMessageDialog(CreateGui.getApp(),
        	    								 	error_message_two_arcs,
        	    									"Error",
        	    									JOptionPane.ERROR_MESSAGE);
        								 isNewArc = false;
        								 int weightInverse =
        									 inverse.getInverse().getWeight();
        								 undoManager.addNewEdit(
        										 inverse.getInverse().setWeight(
        												 ++weightInverse));
        								 createArc.delete();
        								 inverse.getTransition().removeArcCompareObject(
        										 createArc);
        								 inverse.getTransition().updateConnected();
        							 }
        							 break;
        						 }
        					 }
        				 }
        			 }

        			 if (isNewArc == true) {
        				 currentObject.addConnectTo(createArc);

        				 // Evil hack to prevent the arc being added to GuiView twice
        				 contentPane.remove(createArc);

        				 model.addArc((NormalArc)createArc);
        				 view.addNewPetriNetObject(createArc);
        				 if (!fastMode) {
        					 // we are not in fast mode so we have to set a new edit
        					 // in undoManager for adding the new arc
        					 undoManager.newEdit(); // new "transaction""
        				 }
        				 undoManager.addEdit(
        						 new AddPetriNetObjectEdit(createArc, view, model));
        				 if (inverse != null) {
        					 undoManager.addEdit(
        							 inverse.setInverse((NormalArc)createArc,
        									 Pipe.JOIN_ARCS));
        				 }
        			 }

        			 // arc is drawn, remove handler:
        			 createArc.removeKeyListener(keyHandler);
        			 keyHandler = null;
        			 /**/
        			 if (isNewArc == false){                	  
        				 view.remove(createArc);                      
        			 }
        			 /* */
        			 view.createArc = null;
        		 }
        	 }

        	 if (app.getMode() == Pipe.FAST_PLACE ||
        			 app.getMode() == Pipe.FAST_TRANSITION) {
        		 if (view.newPNO == true) {
        			 // a new PNO has been created 
        			 view.newPNO = false;

        			 if (currentObject instanceof Transition) {
        				 app.setMode(Pipe.FAST_PLACE);
        			 } else if (currentObject instanceof Place) {
        				 app.setMode(Pipe.FAST_TRANSITION);
        			 }
        		 } else {
        			 if (view.createArc == null) {
        				 // user has clicked on an existent PNO
        				 app.resetMode();
        			 } else {
        				 if (currentObject instanceof Transition) {
        					 app.setMode(Pipe.FAST_PLACE);
        				 } else if (currentObject instanceof Place) {
        					 app.setMode(Pipe.FAST_TRANSITION);
        				 }
        			 }
        		 }
        	 }
        	 break;
        	 /*CB Joakim Byg - handle timed arc in the same way as normal arc*/
//      	 Lets also handle Transport arcs

         case Pipe.TRANSPORTARC:

        	 Arc transportArcToCreate = view.createArc;
        	 if (transportArcToCreate != null){
        		 if (currentObject != transportArcToCreate.getSource()) {
        			 //Remove the reference to createArc to avoid racecondision with gui  
        			 view.createArc = null;
        			 
        			 
        			 transportArcToCreate.setSelectable(true);

        			 // This is the first step
        			 if (transportArcToCreate.getSource() instanceof Place){

        				 //mikaelhm - Dont allow a transport arc from place to transition if there is another arc. 
    					 boolean existsArc = false;
    					 
    					 //Check if arc has leagal target
    					 PlaceTransitionObject target = transportArcToCreate.getTarget();
    					 if (!(target instanceof Transition && target!=null)){
    						 System.err.println("Error creating transport arc, invalid target");
    						 transportArcToCreate.delete();
    						 break;
    					 }
    					 
    					 Iterator arcsFrom = transportArcToCreate.getSource().getConnectFromIterator();
            			 // search for pre-existent arcs from transportArcToCreate's source to 
            			 // transportArcToCreate's target                  
            			 while(arcsFrom.hasNext()) {
            				 Arc someArc = ((Arc)arcsFrom.next());
            				 if (someArc == transportArcToCreate) {
            					 break;
            				 } else if (someArc.getSource() == transportArcToCreate.getSource() &&
            						 someArc.getTarget() == currentObject) {
            					 existsArc = true;

            					 if (someArc instanceof TAPNInhibitorArc) {
            						 // user has drawn a transport arc where there is 
            						 // a TAPNInhibitorArc arc already - This does not make sense.
            						 System.out.println("It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.");
          							 JOptionPane.showMessageDialog(CreateGui.getApp(),
          									 "It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.",
          									 "Error",
          									 JOptionPane.ERROR_MESSAGE);
            						 
            					 } else if (someArc instanceof TransportArc) {
            						 // user has drawn a transport arc where there is 
            						 // a transport arc already - We do not allow that.
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								 	error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 
            					 } else if (someArc instanceof NormalArc) {
            						 // user has drawn a transport arc where there is 
            						 // a normal arc already - we increment arc's weight
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								    error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 
            					 } else{
            						 //This should not happen - since all types of arcs are listed above.
            					 }
            					 break; 
            				 }
            			 }
            			 if (existsArc){
    						 transportArcToCreate.delete();
    						 break;
    					 }
        				 
        				 
        				 int groupMaxCounter = 0;

        				 for (Object pt : transportArcToCreate.getTarget().getPostset()){
        					 if (pt instanceof TransportArc) {
        						 if (((TransportArc)pt).getGroupNr() > groupMaxCounter){
        							 groupMaxCounter = ((TransportArc)pt).getGroupNr();
        						 }
        					 }
        				 }

        				 ((TransportArc) transportArcToCreate).setGroupNr(groupMaxCounter + 1);

        				 currentObject.addConnectTo(transportArcToCreate);

        				 // Evil hack to prevent the arc being added to GuiView twice
        				 contentPane.remove(transportArcToCreate);

        				 model.addArc((NormalArc)transportArcToCreate);
        				 view.addNewPetriNetObject(transportArcToCreate);
        				 undoManager.newEdit();

        				 //undoManager.addEdit(
        				//		 new AddPetriNetObjectEdit(transportArcToCreate, view, model));

        				 //arc is drawn, remove handler:
        				 transportArcToCreate.removeKeyListener(keyHandler);
        				 keyHandler = null;
        				 view.createArc = null;

        				 view.transportArcPart1 = (TransportArc)transportArcToCreate;

        				 //Create the next arc
        				 createArc(new TransportArc(currentObject, 1, false), currentObject);

        			 } else if (transportArcToCreate.getSource() instanceof Transition) {
        				 //Step 2 
        				 if (view.transportArcPart1 == null ){
        					 System.err.println("There where a error, cant creat a transport arc with out part one");
//      					 arc is drawn, remove handler:
        					 transportArcToCreate.removeKeyListener(keyHandler);
        					 keyHandler = null;
        					 view.createArc = null;
        					 break;
        				 }

        				 		 //Check if arc has leagal target
    					 if (!(transportArcToCreate.getTarget() instanceof TimedPlace && transportArcToCreate.getTarget() !=null)){
    						 System.err.println("Error creating transport arc, invalid target");
    						 transportArcToCreate.delete();
    						 view.transportArcPart1.delete();
    						 break;
    					 }
        				 
    					 //Check that there is not an other arc
    					 boolean existsArc = false;
        				 Iterator arcsFromTranasition = transportArcToCreate.getSource().getConnectFromIterator();
        				 Arc someArc = null;

        				 while ( arcsFromTranasition.hasNext() ){        					 
        					 someArc = (Arc)arcsFromTranasition.next();
        					 if (someArc == transportArcToCreate){
        						 break;
        						 //continue;
        					 }
    						 if( someArc.getSource() == transportArcToCreate.getSource()
    								 && someArc.getTarget() == currentObject) {
    							 existsArc = true;
    							 
    							 if (someArc instanceof TAPNInhibitorArc) {
            						 //mikaelhm - This can never be the case, since the user is trying to draw the 2. step of an transport arc from a trans to a place, and this is not possible for a inhibitor arc.            						 
            					 } else if (someArc instanceof TransportArc) {
            						 // user has drawn a transport arc where there is 
            						 // a transport arc already - We do not allow that.
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								     error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 
            					 } else if (someArc instanceof NormalArc) {
            						 // user htransportas drawn a transport arc where there is 
            						 // a normal arc already - we increment arc's weight
            						 if (!(Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET)){
        								 int weightToInsert = someArc.getWeight()+1;
            							 someArc.setWeight(weightToInsert);
        		                      }
            						 else{
            						 System.out.println("We dont allow more than one transport arc from a transition to a place.");
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            									"We dont allow more than one transport arc from a transition to a place.",
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 }
            						 
            					 } else{
            						 //This should not happen - since all types of arcs are listed above.
            					 }    	

    							 break;
    						 }
        				 }
    					 if (existsArc){
							 transportArcToCreate.delete();
							 
							 // remove first part of transport arc
							 view.transportArcPart1.delete();
    						 break;
    					 }
        				 currentObject.addConnectTo(transportArcToCreate);

        				 // Evil hack to prevent the arc being added to GuiView twice
        				 contentPane.remove(transportArcToCreate);

        				 model.addArc((NormalArc)transportArcToCreate);
        				 view.addNewPetriNetObject(transportArcToCreate);
        				 undoManager.newEdit();

        				 undoManager.addEdit(
        						 new AddPetriNetObjectEdit(transportArcToCreate, view, model));

        				 //arc is drawn, remove handler:
        					 transportArcToCreate.removeKeyListener(keyHandler);
        					 keyHandler = null;
        					 view.createArc = null;

        					 ((TransportArc)transportArcToCreate).setGroupNr(view.transportArcPart1.getGroupNr());

//      					 arc is drawn, remove handler:
        					 transportArcToCreate.removeKeyListener(keyHandler);
        					 keyHandler = null;
        					 view.createArc = null;

        					 //Ekstra suff
        					 view.transportArcPart1.setConnectedTo(((TransportArc)transportArcToCreate));
        					 ((TransportArc)transportArcToCreate).setConnectedTo(view.transportArcPart1);
        					 view.transportArcPart1 = null;

        					 
        			 }

        		 }

        	 }

        	 break;
         case Pipe.FAST_TAPNPLACE:
         case Pipe.FAST_TAPNTRANSITION:
         case Pipe.TAPNARC:
        	 
	         Arc timedArcToCreate = view.createArc;
	    
        	 if (timedArcToCreate != null){
        		 if (currentObject != timedArcToCreate.getSource()) {
        			 //Remove the reference to createArc to avoid racecondision with gui  
        			 view.createArc = null;
        			 
        			 timedArcToCreate.setSelectable(true);
        			 
        			 //We create NormalArcs when source of arc is Transition( since there are no intervals on output arcs.) ...except if the arc is a TransportArc
        			 if (!(timedArcToCreate instanceof TimedArc)){
        				 boolean toDrawNewArc = true;
        				 Iterator arcsFromTranasition = timedArcToCreate.getSource().getConnectFromIterator();
        				 Arc someArc = null;

        				 while ( arcsFromTranasition.hasNext() ){        					 
        					 someArc = (Arc)arcsFromTranasition.next();
        					 if (someArc == timedArcToCreate){
        						 break;
        						 //continue;
        					 }
    						 if( someArc.getSource() == timedArcToCreate.getSource()
    								 && someArc.getTarget() == currentObject) {
    							 toDrawNewArc = false;
    							 
    							 if (someArc instanceof TAPNInhibitorArc) {
            						 //mikaelhm - This can never be the case, since the user is trying to draw a normal arc from a trans to a place, and this is not possible for a inhibitor arc.            						 
            					 } else if (someArc instanceof TransportArc) {
            						 // user has drawn a normal arc where there is 
            						 // a transport arc already - We do not allow that.
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								    error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 
            					 } else if (someArc instanceof NormalArc) {
            						 // user has drawn a normal arc where there is 
            						 // a normal arc already - we increment arc's weight
            						 if (!(Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET)){
        								 int weightToInsert = someArc.getWeight()+1;
            							 someArc.setWeight(weightToInsert);
        		                      }
            						 else{
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								    error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 }
            						 
            					 } else{
            						 //This should not happen - since all types of arcs are listed above.
            					 }    							 
    							 break;
    						 }

        				 }
        				 if( ! toDrawNewArc) {
        					 timedArcToCreate.delete();
        					 someArc.getTransition().removeArcCompareObject(timedArcToCreate);
        					 someArc.getTransition().updateConnected();
        				 }else {
        					

        					 currentObject.addConnectTo(timedArcToCreate);

        					 // Evil hack to prevent the arc being added to GuiView twice
        					 contentPane.remove(timedArcToCreate);

        					 model.addArc((NormalArc)timedArcToCreate);
        					 view.addNewPetriNetObject(timedArcToCreate);
        					 if (!fastMode) {
        						 // we are not in fast mode so we have to set a new edit
        						 // in undoManager for adding the new arc
        						 undoManager.newEdit(); // new "transaction""
        					 }
        					 undoManager.addEdit(
        							 new AddPetriNetObjectEdit(timedArcToCreate, view, model));
        				 }

        				 //else source is a place (not transition)
        			 } else{
     				 
					 
    					 // XXX  -- kyrke hack to precent some race condisions in pipe gui   					 
    					 if ((timedArcToCreate.getTarget()) == null || (!(timedArcToCreate.getTransition() instanceof Transition))) {
    						 timedArcToCreate.delete();
    						 timedArcToCreate.removeKeyListener(keyHandler);
    	        			 keyHandler = null;
    	        			 
    	        			 view.createArc = null;
    	        			 break;
    					 }
    					 
    					 boolean existsArc = false;
        				 Iterator arcsFromTranasition = timedArcToCreate.getSource().getConnectFromIterator();
        				 Arc someArc = null;
    					 
    					 while ( arcsFromTranasition.hasNext() ){        					 
        					 someArc = (Arc)arcsFromTranasition.next();
        					 if (someArc == timedArcToCreate){
        						 //break;
        						 continue;
        					 }
    						 if( someArc.getSource() == timedArcToCreate.getSource()
    								 && someArc.getTarget() == currentObject) {
    							 existsArc = true;
    							 
    							 if (someArc instanceof TAPNInhibitorArc) {
    								 //user has drawn a timed arc where there is 
            						 //an inhibitor arc already - this does not make sense
    								 System.out.println("It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.");
    	  							 JOptionPane.showMessageDialog(CreateGui.getApp(),
    	  									 "It does not make sense to have both a transport arc and an inhibitor arc from a place to a transition.",
    	  									 "Error",
    	  									 JOptionPane.ERROR_MESSAGE);
            					 } else if (someArc instanceof TransportArc) {
            						 // user has drawn a timed arc where there is 
            						 // a transport arc already - We do not allow that.
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								    error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 
            					 } else if (someArc instanceof NormalArc) {
            						 // user has drawn a normal arc where there is 
            						 // a normal arc already - we increment arc's weight
            						 if (!(Pipe.drawingmode == Pipe.drawmodes.TIMEDARCPETRINET)){
        								 int weightToInsert = someArc.getWeight()+1;
            							 someArc.setWeight(weightToInsert);
        		                      }
            						 else{
            						 System.out.println(error_message_two_arcs);
            						 JOptionPane.showMessageDialog(CreateGui.getApp(),
            								    error_message_two_arcs,
            									"Error",
            									JOptionPane.ERROR_MESSAGE);
            						 }
            						 
            					 } else{
            						 //This should not happen - since all types of arcs are listed above.
            					 }
    							 timedArcToCreate.delete();
    							 break;
    						 }

        				 }
    					 
    					 if (existsArc){    						 
    						 break;
    					 }
        				 
        				 currentObject.addConnectTo(timedArcToCreate);
        				 timedArcToCreate.getTransition().updateConnected();
        				 
        				 // Evil hack to prevent the arc being added to GuiView twice        				 
        				 contentPane.remove(timedArcToCreate);
        				 model.addArc((NormalArc)timedArcToCreate);
        				 view.addNewPetriNetObject(timedArcToCreate);
        				 if (!fastMode) {
        					 // we are not in fast mode so we have to set a new edit
        					 // in undoManager for adding the new arc
        					 undoManager.newEdit(); // new "transaction""
        				 }
        				 undoManager.addEdit(
        						 new AddPetriNetObjectEdit(timedArcToCreate, view, model));
 
        			 }
        			 //arc is drawn, remove handler:
        			 timedArcToCreate.removeKeyListener(keyHandler);
        			 keyHandler = null;
        			 
        			 view.createArc = null;
        		 }
        	 }
        	 
        	 if (app.getMode() == Pipe.FAST_TAPNPLACE ||
             		app.getMode() == Pipe.FAST_TAPNTRANSITION) {
        		 
             	if (view.newPNO == true) {
             		
             		// a new PNO has been created 
             		view.newPNO = false;

             		if (currentObject instanceof Transition) {
             			app.setMode(Pipe.FAST_TAPNPLACE);
             		} else if (currentObject instanceof Place) {
             			app.setMode(Pipe.FAST_TAPNTRANSITION);
             		}
             	} else {
             		if (view.createArc == null) {
             			// user has clicked on an existent PNO
             			app.resetMode();
             		} else {
             			if (currentObject instanceof Transition) {
             				app.setMode(Pipe.FAST_TAPNPLACE);
             			} else if (currentObject instanceof Place) {
             				app.setMode(Pipe.FAST_TAPNTRANSITION);
             			}
             		}
             	}
             }
        	 break;
        	 /*EOC*/
         default:
        	 break;
      }
   }
}
