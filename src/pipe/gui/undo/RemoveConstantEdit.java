package pipe.gui.undo;

import pipe.dataLayer.Constant;
import pipe.dataLayer.ConstantStore;
import pipe.gui.CreateGui;

public class RemoveConstantEdit extends UndoableEdit {

	private Constant constant;
	private ConstantStore store;
	
	public RemoveConstantEdit(Constant constant, ConstantStore store){
		this.constant = constant;
		this.store = store;
	}
	
	@Override
	public void redo() {
		store.remove(constant);
		CreateGui.updateConstantsList();
	}

	@Override
	public void undo() {
		store.add(constant);
		CreateGui.updateConstantsList();
	}

}
