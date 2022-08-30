package dk.aau.cs.model.tapn.event;

import dk.aau.cs.model.tapn.Constant;

public class ConstantEvent {
	private final Constant constant;
	private final int index;

	public ConstantEvent(Constant constant, int index){
		this.constant = constant;
		this.index = index;
	}
	
	public Constant constant(){
		return constant;
	}
	
	public int index(){
		return index;
	}
}
