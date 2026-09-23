package net.tapaal.gui.petrinet.undo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipe.gui.canvas.Grid;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PetriNetObjectPositionSnapshotTest {
    @BeforeEach
    void startWithGridDisabled() {
        Grid.disableGrid();
    }

    @AfterEach
    void disableGrid() {
        Grid.disableGrid();
    }

    @Test
    void restoresModelPositionAfterZoomChanges() {
        AnnotationNote note = new AnnotationNote(101, 103);
        PetriNetObjectPositionSnapshot snapshot = PetriNetObjectPositionSnapshot.capture(List.of(note));

        note.setOriginalX(125);
        note.setOriginalY(129);
        note.zoomUpdate(200);
        snapshot.restore();

        assertEquals(101, note.getOriginalX());
        assertEquals(103, note.getOriginalY());
        assertEquals(202, note.getPositionX());
        assertEquals(206, note.getPositionY());
    }

    @Test
    void restoresExactModelPositionWhenGridIsEnabled() {
        AnnotationNote note = new AnnotationNote(101, 103);
        PetriNetObjectPositionSnapshot snapshot = PetriNetObjectPositionSnapshot.capture(List.of(note));

        note.setOriginalX(125);
        note.setOriginalY(129);
        Grid.enableGrid();
        snapshot.restore();

        assertEquals(101, note.getOriginalX());
        assertEquals(103, note.getOriginalY());
    }
}
