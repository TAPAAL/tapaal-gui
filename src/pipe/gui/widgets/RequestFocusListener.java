package pipe.gui.widgets;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener
{
	private boolean removeListener;

	/*
	 *  Convenience constructor. The listener is only used once and then it is
	 *  removed from the component.
	 */
	public RequestFocusListener()
	{
		this(true);
	}

	/*
	 *  Constructor that controls whether this listen can be used once or
	 *  multiple times.
	 *
	 *  @param removeListener when true this listener is only invoked once
	 *                        otherwise it can be invoked multiple times.
	 */
	public RequestFocusListener(boolean removeListener)
	{
		this.removeListener = removeListener;
	}

	@Override
	public void ancestorAdded(final AncestorEvent arg0)
	{
//		JComponent component = arg0.getComponent();
//		component.requestFocusInWindow();
//
//		if (removeListener)
//			component.removeAncestorListener( this );
		
		//the above commented out code does not work in linux for some reason. 
		//The code below is a hack to get around it.
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
