package pipe.gui.undo;

import pipe.dataLayer.TimedArc;

public class ArcTimeIntervalEdit extends UndoableEdit {
	private TimedArc arc;
	String oldTimeInterval;
	String newTimeInterval;
	
	public ArcTimeIntervalEdit(TimedArc arc, String oldTimeInterval, String newTimeInterval){
		this.arc = arc;
		this.oldTimeInterval = oldTimeInterval;
		this.newTimeInterval = newTimeInterval;
	}
	
	@Override
	public void redo() {
		arc.setGuard(newTimeInterval);
	}

	@Override
	public void undo() {
		arc.setGuard(oldTimeInterval);
	}
	
	@Override
	public String toString(){
		return super.toString() + " " + arc.getName() + ", oldTimeInterval: " 
		+ oldTimeInterval + ", newTimeInterval: " + newTimeInterval;   		
	}

}
