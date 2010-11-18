package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.Constant;
import pipe.dataLayer.ConstantStore;
import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;

public class UpdateConstantEdit extends Command {

	private Constant newConstant;
	private ConstantStore store;
	private Constant oldConstant;
	private DataLayer model;
	
	public UpdateConstantEdit(Constant oldConstant, Constant newConstant, 
			ConstantStore store, DataLayer model){
		this.oldConstant = oldConstant;
		this.newConstant = newConstant;
		this.store = store;
		this.model = model;
	}
	
	@Override
	public void redo() {
		store.remove(oldConstant);
		store.add(newConstant);
		model.correctGuards(oldConstant.getName(), newConstant.getName());
		CreateGui.updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(newConstant);
		store.add(oldConstant);
		model.correctGuards(newConstant.getName(), oldConstant.getName());
		CreateGui.updateConstantsList();

	}

}
