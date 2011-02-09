/*
 * PetriNetObjectNameEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import dk.aau.cs.TCTL.visitors.RenamePlaceTCTLVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;

/**
 * 
 * @author corveau
 */
public class PetriNetObjectNameEdit extends Command {

	PetriNetObject pno;
	String oldName;
	String newName;

	/** Creates a new instance of placeNameEdit */
	public PetriNetObjectNameEdit(PetriNetObject _pno, String _oldName,
			String _newName) {
		pno = _pno;
		oldName = _oldName;
		newName = _newName;
	}

	/** */
	@Override
	public void undo() {
		pno.setName(oldName);

		Iterable<TAPNQuery> queries = ((TabContent) CreateGui.getTab()
				.getSelectedComponent()).queries();

		RenamePlaceTCTLVisitor renameVisitor = new RenamePlaceTCTLVisitor(
				newName, oldName);
		for (TAPNQuery q : queries) {
			q.getProperty().accept(renameVisitor, null);
		}
	}

	/** */
	@Override
	public void redo() {
		pno.setName(newName);

		Iterable<TAPNQuery> queries = ((TabContent) CreateGui.getTab()
				.getSelectedComponent()).queries();

		RenamePlaceTCTLVisitor renameVisitor = new RenamePlaceTCTLVisitor(
				oldName, newName);
		for (TAPNQuery q : queries) {
			q.getProperty().accept(renameVisitor, null);
		}
	}

}
