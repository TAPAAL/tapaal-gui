package net.tapaal.gui.petrinet.undo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import pipe.gui.canvas.Grid;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;
import pipe.gui.petrinet.graphicElements.Note;
import pipe.gui.petrinet.graphicElements.PetriNetObject;

public final class PetriNetObjectPositionSnapshot {
    private final List<Position> positions;
    private final DataLayer guiModel;

    private PetriNetObjectPositionSnapshot(List<Position> positions, DataLayer guiModel) {
        this.positions = positions;
        this.guiModel = guiModel;
    }

    public static PetriNetObjectPositionSnapshot capture(DataLayer guiModel) {
        return new PetriNetObjectPositionSnapshot(
            capturePositions(guiModel.getPetriNetObjectsWithArcPathPoint()),
            guiModel
        );
    }

    private static List<Position> capturePositions(Iterable<PetriNetObject> objects) {
        List<Position> positions = new ArrayList<>();
        for (PetriNetObject object : objects) {
            if (object instanceof Arc) {
                continue;
            }
            positions.add(new Position(object, new Point(object.getOriginalX(), object.getOriginalY())));
        }
        return positions;
    }

    public void restore() {
        boolean gridWasEnabled = Grid.isEnabled();
        Grid.disableGrid();
        try {
            // Restore all nodes before updating them because connected arc endpoints depend
            // on both ends of the arc. Arc points are restored in a separate final phase.
            for (Position position : positions) {
                if (!(position.object instanceof ArcPathPoint)) {
                    position.restoreModelPosition();
                }
            }

            for (Position position : positions) {
                PetriNetObject object = position.object;
                if (!(object instanceof ArcPathPoint)) {
                    if (object instanceof Note) {
                        ((Note) object).updateBounds();
                    } else {
                        object.updateOnMoveOrZoom();
                    }
                    object.repaint();
                }
            }

            // Node updates derive arc endpoints and may round coordinates. Reapply every
            // stored path point afterwards so old files retain their exact saved geometry.
            for (Position position : positions) {
                if (position.object instanceof ArcPathPoint) {
                    position.restoreModelPosition();
                    position.object.updateOnMoveOrZoom();
                    position.object.repaint();
                }
            }

            for (Arc arc : guiModel.getArcs()) {
                arc.getArcPath().createPath();
                arc.updateBounds();
                arc.updateLabel(true);
                arc.repaint();
            }
        } finally {
            if (gridWasEnabled) {
                Grid.enableGrid();
            }
        }
    }

    public boolean belongsTo(DataLayer guiModel) {
        return this.guiModel == guiModel;
    }

    private static final class Position {
        private final PetriNetObject object;
        private final Point location;

        private Position(PetriNetObject object, Point location) {
            this.object = object;
            this.location = location;
        }

        private void restoreModelPosition() {
            object.setOriginalX(location.x);
            object.setOriginalY(location.y);
        }
    }
}
