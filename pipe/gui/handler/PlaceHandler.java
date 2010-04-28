package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.AddTokenPanel;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.OutputValueEditorPanel;
import pipe.gui.widgets.RemoveTokenPanel;


/**
 * Class used to implement methods corresponding to mouse events on places.
 */
public class PlaceHandler 
extends PlaceTransitionObjectHandler {


	public PlaceHandler(Container contentpane, Place obj) {
		super(contentpane, obj);
	}


	/** 
	 * Creates the popup menu that the user will see when they right click on a 
	 * component 
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int index = 0;
		JPopupMenu popup = super.getPopup(e);      

		JMenuItem menuItem = new JMenuItem("Edit Place");      
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				((Place)myObject).showEditor();
			}
		}); 
		popup.insert(menuItem, index++);

		menuItem = new JMenuItem(new ShowHideInfoAction((Place)myObject));      
		if (((Place)myObject).getAttributesVisible() == true){
			menuItem.setText("Hide Attributes");
		} else {
			menuItem.setText("Show Attributes");
		}
		popup.insert(menuItem,index++);
		popup.insert(new JPopupMenu.Separator(),index);      

		return popup;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)){
			if (e.getClickCount() == 2 &&
					CreateGui.getApp().isEditionAllowed() &&
					(CreateGui.getApp().getMode() == Pipe.PLACE || 
							CreateGui.getApp().getMode() == Pipe.SELECT)) {
				((Place)myObject).showEditor(); 
			} else {
				int currentMarking = ((Place)myObject).getCurrentMarking();
				UndoManager undoManager = CreateGui.getView().getUndoManager();

				switch(CreateGui.getApp().getMode()) {
				case Pipe.ADDTOKEN:
					if(!CreateGui.getModel().isUsingColors()){
						undoManager.addNewEdit(
								((Place)myObject).setCurrentMarking(++currentMarking));
					}else{
						showAddTokenDialog((ColoredTimedPlace)myObject);
					}
					break;
				case Pipe.DELTOKEN:
					if(!CreateGui.getModel().isUsingColors()){
						if (currentMarking > 0) {
							undoManager.addNewEdit(
									((Place)myObject).setCurrentMarking(--currentMarking));
						}
					}else{
						showRemoveTokenDialog((ColoredTimedPlace)myObject);
					}
					break;
				default:
					break;
				}
			}
		}else if (SwingUtilities.isRightMouseButton(e)){
			if (CreateGui.getApp().isEditionAllowed() && enablePopup) { 
				JPopupMenu m = getPopup(e);
				if (m != null) {           
					int x = Zoomer.getZoomedValue(
							((Place)myObject).getNameOffsetXObject().intValue(),
							myObject.getZoom());
					int y = Zoomer.getZoomedValue(
							((Place)myObject).getNameOffsetYObject().intValue(),
							myObject.getZoom());
					m.show(myObject, x, y);
				}
			}
		}/* else if (SwingUtilities.isMiddleMouseButton(e)){
         // TODO - middelclick draw a arrow 
      } */
	}


	private void showRemoveTokenDialog(ColoredTimedPlace place) {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new RemoveTokenPanel(guiDialog.getRootPane(), place, CreateGui.getModel(), CreateGui.getView().getUndoManager()));

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);	
	}


	private void showAddTokenDialog(ColoredTimedPlace place) {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new AddTokenPanel(guiDialog.getRootPane(), place, CreateGui.getModel(), CreateGui.getView().getUndoManager()));

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);	
	}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// 
		if (CreateGui.getApp().isEditionAllowed() == false || 
				e.isControlDown()) {
			return;
		}

		UndoManager undoManager = CreateGui.getView().getUndoManager();
		if (e.isShiftDown()) {
			/* if ((myObject instanceof TimedPlace)==false){
    		  int oldCapacity = ((Place)myObject).getCapacity();
    		  int oldMarking = ((Place)myObject).getCurrentMarking();

    		  int newCapacity = oldCapacity - e.getWheelRotation();
    		  if (newCapacity < 0) {
    			  newCapacity = 0;
    		  }

    		  undoManager.newEdit(); // new "transaction""
    		  if ((newCapacity > 0) && (oldMarking > newCapacity)){
    			  if (((Place)myObject).getMarkingParameter() != null) {
    				  undoManager.addEdit(((Place)myObject).clearMarkingParameter());
    			  }
    			  undoManager.addEdit(((Place)myObject).setCurrentMarking(newCapacity));
    		  }
    		  undoManager.addEdit(((Place)myObject).setCapacity(newCapacity));
    	  }*/
		} else {
			if(!CreateGui.getModel().isUsingColors()){
				int oldMarking = ((Place)myObject).getCurrentMarking();
				int newMarking = oldMarking - e.getWheelRotation();

				if (newMarking < 0) {
					newMarking = 0;
				}
				if (oldMarking != newMarking) {            
					undoManager.addNewEdit(((Place)myObject).setCurrentMarking(newMarking));
					if (((Place)myObject).getMarkingParameter() != null) {
						undoManager.addEdit(((Place)myObject).clearMarkingParameter());
					}            
				} 
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e){
		if ((myObject instanceof TimedPlace) && !isDragging){//&& CreateGui.getView().isInAnimationMode()){		   
			if(CreateGui.getModel().isUsingColors() || CreateGui.getView().isInAnimationMode()){
				((TimedPlace) myObject).showAgeOfTokens(true);
			}
		}
		
		if(isDragging){
			((TimedPlace) myObject).showAgeOfTokens(false);
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e){
		if ((myObject instanceof TimedPlace)){// && CreateGui.getView().isInAnimationMode()){
			if(CreateGui.getModel().isUsingColors() || CreateGui.getView().isInAnimationMode()){
				((TimedPlace) myObject).showAgeOfTokens(false);
			}
		}
	}

	//Override
	@Override
	public void mousePressed(MouseEvent e) {
		if (CreateGui.getApp().isEditionAllowed()){
			super.mousePressed(e);
		}else{
			//do nothing except the things that one do in the simulator (handled somewhere else).
		}
	}
}
