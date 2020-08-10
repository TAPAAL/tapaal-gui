package dk.aau.cs.gui.components;

import javax.swing.AbstractListModel;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.event.ConstantChangedEvent;
import dk.aau.cs.model.tapn.event.ConstantEvent;
import dk.aau.cs.model.tapn.event.ConstantsListener;

public class ConstantsListModel extends AbstractListModel<Constant> {
	
	private TimedArcPetriNetNetwork network;

	private final ConstantsListener listener;

	public ConstantsListModel(TimedArcPetriNetNetwork network){
		listener = new ConstantsListener() {
			public void constantRemoved(ConstantEvent e) {
				fireIntervalRemoved(this, e.index(), e.index());			}
			
			public void constantChanged(ConstantChangedEvent e) {
				fireContentsChanged(this, e.index(), e.index());
			}
			
			public void constantAdded(ConstantEvent e) {
				fireIntervalAdded(this, e.index(), e.index());
			}
		};
		setNetwork(network);
	}
	
	// TODO: Due to stupid programming, we have to allow it to be set later
	//       since the Network is sometimes set after the TabContent is created.
	public void setNetwork(TimedArcPetriNetNetwork newNetwork){
		if(network != null){
			network.removeConstantsListener(listener);
		}
		network = newNetwork;
		network.addConstantsListener(listener);	
		fireContentsChanged(this,0, Integer.MAX_VALUE);
	}
	
	public Constant getElementAt(int index) {
		return network.getConstant(index);
	}

	public int getSize() {
		return network.constants().size();
	}
	
	public void updateAll() {
		fireContentsChanged(this,0, Integer.MAX_VALUE);
	}

}
