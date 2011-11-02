package pipe.gui.widgets;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener
{
	public RequestFocusListener()
	{
	}

	@Override
	public void ancestorAdded(final AncestorEvent arg0)
	{
		//JComponent component = arg0.getComponent();
		//component.requestFocusInWindow();
		
		 final AncestorListener al= this;   
		    SwingUtilities.invokeLater(new Runnable(){

		        @Override
		        public void run() {
		            JComponent component = (JComponent)arg0.getComponent();
		            component.requestFocusInWindow();
		            component.removeAncestorListener( al );
		        }
		    });
	}

	@Override
	public void ancestorMoved(AncestorEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ancestorRemoved(AncestorEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
