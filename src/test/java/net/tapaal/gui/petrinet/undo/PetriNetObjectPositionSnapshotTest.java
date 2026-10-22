package net.tapaal.gui.petrinet.undo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pipe.gui.canvas.Grid;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        PetriNetObjectPositionSnapshot snapshot = capture(note);

        note.setOriginalX(125);
        note.setOriginalY(129);
        note.zoomUpdate(200);
        snapshot.restore();

        assertEquals(101, note.getOriginalX());
        assertEquals(103, note.getOriginalY());
        assertEquals(202, note.getPositionX());
        assertEquals(206, note.getPositionY());
        assertFalse(Grid.isEnabled());
    }

    @Test
    void restoresExactModelPositionWhenGridIsEnabled() {
        AnnotationNote note = new AnnotationNote(101, 103);
        PetriNetObjectPositionSnapshot snapshot = capture(note);

        note.setOriginalX(125);
        note.setOriginalY(129);
        Grid.enableGrid();
        snapshot.restore();

        assertEquals(101, note.getOriginalX());
        assertEquals(103, note.getOriginalY());
        assertTrue(Grid.isEnabled());
    }

    @Test
    void restoresAnnotationVisualPositionAtTheCurrentZoom() {
        AnnotationNote note = new AnnotationNote(101, 103);
        note.addedToGui();
        PetriNetObjectPositionSnapshot snapshot = capture(note);

        note.setPosition(new Point(151, 153));
        snapshot.restore();

        assertEquals(101, note.getOriginalX());
        assertEquals(103, note.getOriginalY());
        assertEquals(new Point(101, 103), note.getLocation());
    }

    private PetriNetObjectPositionSnapshot capture(AnnotationNote note) {
        DataLayer guiModel = new DataLayer();
        guiModel.addPetriNetObject(note);
        return PetriNetObjectPositionSnapshot.capture(guiModel);
    }
}
