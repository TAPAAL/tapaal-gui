package pipe.gui.undo;

import pipe.dataLayer.Constant;
import pipe.dataLayer.ConstantStore;
import pipe.gui.CreateGui;

public class AddConstantEdit extends UndoableEdit {
	private Constant constant;
	private ConstantStore store;
	
	public AddConstantEdit(Constant constant, ConstantStore store){
		this.constant = constant;
		this.store = store;
	}
		
	@Override
	public void redo() {
		store.add(constant);
		CreateGui.updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(constant);
		CreateGui.updateConstantsList();
	}

}
