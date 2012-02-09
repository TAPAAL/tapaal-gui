package pipe.gui.widgets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class WidthAdjustingComboBox extends JComboBox{

	private static final long serialVersionUID = 1L;
	
	public WidthAdjustingComboBox(int maxNumberOfPlacesToShowAtOnce) {
		this.addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
		
	
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
			
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				adjustWidthOfPopUpMenu();
			}
		});
		this.setMaximumRowCount(maxNumberOfPlacesToShowAtOnce);
	}
	
	private void adjustWidthOfPopUpMenu() {
		if (this.getItemCount() == 0) {
			return;
		}
		Object comp = this.getUI().getAccessibleChild(this, 0);
		if (!(comp instanceof JPopupMenu)) {
			return;
		}
		FontMetrics metrics = this.getFontMetrics(this.getFont()); 
		int maxWidth=0;
		for(int i=0;i<this.getItemCount();i++){
			if(this.getItemAt(i)==null)
				continue;
			int currentWidth=metrics.stringWidth(this.getItemAt(i).toString());
			if(maxWidth<currentWidth)
				maxWidth=currentWidth;
		}
		JPopupMenu popup = (JPopupMenu) comp;
		//the following is a hack to fix a compatibility issue with the PopUpMenu-class
		Component[] listOfComponents = popup.getComponents();
		JScrollPane scrollPane = null;
		for (Component element : listOfComponents) {
			if (element instanceof JScrollPane) {
				scrollPane = (JScrollPane) element;
				Dimension size = scrollPane.getPreferredSize();
				if (size.width < maxWidth+70) {
					size.width = maxWidth+70;
				}
				scrollPane.setPreferredSize(size);
				scrollPane.setMaximumSize(size);
				break;
			}
		}		
	}
}
