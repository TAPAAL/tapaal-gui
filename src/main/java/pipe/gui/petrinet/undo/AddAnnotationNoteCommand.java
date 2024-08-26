package pipe.gui.petrinet.undo;

import net.tapaal.gui.petrinet.undo.Command;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

public class AddAnnotationNoteCommand implements Command {

    private final AnnotationNote note;
    private final DataLayer guiModel;

    public AddAnnotationNoteCommand(AnnotationNote note, DataLayer guiModel) {
        this.note = note;
        this.guiModel = guiModel;
    }

    @Override
    public void undo() {
        guiModel.removePetriNetObject(note);
    }

    @Override
    public void redo() {
        guiModel.addPetriNetObject(note);
    }
}
