package dk.aau.cs.model.tapn.event;

import dk.aau.cs.model.tapn.Constant;

public class ConstantChangedEvent {
	private final Constant oldConstant;
	private final Constant newConstant;
	private final int index;
	
	public ConstantChangedEvent(Constant oldConstant, Constant newConstant, int index){
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.index = index;
	}
	
	public Constant oldConstant(){
		return oldConstant;
	}
	
	public Constant newConstant(){
		return newConstant;
	}

	public int index() {
		return index;
	}
}
