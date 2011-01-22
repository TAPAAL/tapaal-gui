package dk.aau.cs.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import dk.aau.cs.model.tapn.NetworkMarking;

public class AnimationHistory extends DefaultListModel {
	private List<NetworkMarking> markings = new ArrayList<NetworkMarking>();
	private List<String> actions = new ArrayList<String>();
	private int currentIndex = 0;
	
	public void add(String action, NetworkMarking resultingMarking){
		clearForward();
		markings.add(resultingMarking);
		actions.add(action);
		currentIndex = markings.size();
		
		fireContentsChanged(this, 0, markings.size());
	}
	
	public NetworkMarking currentMarking(){
		return markings.get(currentIndex);
	}

	private void clearForward() {
		for(int i = markings.size(); i > currentIndex; i--){
			markings.remove(i);
			actions.remove(i);
		}		
	}
}
