package pipe.gui.petrinet.undo;

import net.tapaal.gui.undo.Command;
import pipe.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

public class AddAnnotationNoteCommand extends Command {

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
