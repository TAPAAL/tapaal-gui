/*
 * EditNoteAction.java
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.gui.graphicElements.Note;

public class EditNoteAction extends AbstractAction {

	private static final long serialVersionUID = -4438126438814153880L;
	private Note selected;

	public EditNoteAction(Note component) {
		selected = component;
	}

	/** Action for editing the text in a Note */
	public void actionPerformed(ActionEvent e) {
		selected.enableEditMode();
	}

}
