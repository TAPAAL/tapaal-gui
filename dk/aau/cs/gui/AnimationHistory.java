package dk.aau.cs.gui;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.model.tapn.NetworkMarking;

// TODO: Delete me?
public class AnimationHistory {
	private List<NetworkMarking> markings = new ArrayList<NetworkMarking>();
	private List<String> actions = new ArrayList<String>();
	private int currentIndex = 0;
	
	public void add(String action, NetworkMarking resultingMarking){
		clearForward();
		markings.add(resultingMarking);
		actions.add(action);
		currentIndex = markings.size();
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
