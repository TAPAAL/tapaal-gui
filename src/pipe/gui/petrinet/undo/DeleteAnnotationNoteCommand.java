package pipe.gui.petrinet.undo;

import net.tapaal.gui.undo.Command;
import pipe.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

public class DeleteAnnotationNoteCommand extends Command {

    private final AnnotationNote note;
    private final DataLayer model;

    public DeleteAnnotationNoteCommand(AnnotationNote note, DataLayer model){
        this.note = note;
        this.model = model;
    }

    @Override
    public void undo() {
        model.addPetriNetObject(note);
    }

    @Override
    public void redo() {
        note.deselect();
        model.removePetriNetObject(note);
    }
}
