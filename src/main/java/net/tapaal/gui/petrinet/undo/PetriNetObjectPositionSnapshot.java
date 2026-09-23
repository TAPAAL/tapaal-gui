package net.tapaal.gui.petrinet.undo;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import pipe.gui.canvas.Grid;
import pipe.gui.petrinet.graphicElements.PetriNetObject;

public final class PetriNetObjectPositionSnapshot {
    private final Map<PetriNetObject, Point> locations;

    private PetriNetObjectPositionSnapshot(Map<PetriNetObject, Point> locations) {
        this.locations = locations;
    }

    public static PetriNetObjectPositionSnapshot capture(Iterable<PetriNetObject> objects) {
        Map<PetriNetObject, Point> locations = new HashMap<>();
        for (PetriNetObject object : objects) {
            locations.put(object, new Point(object.getOriginalX(), object.getOriginalY()));
        }
        return new PetriNetObjectPositionSnapshot(locations);
    }

    public void restore() {
        boolean gridWasEnabled = Grid.isEnabled();
        Grid.disableGrid();
        try {
            for (Map.Entry<PetriNetObject, Point> entry : locations.entrySet()) {
                PetriNetObject object = entry.getKey();
                Point location = entry.getValue();
                object.setOriginalX(location.x);
                object.setOriginalY(location.y);
                object.updateOnMoveOrZoom();
            }
        } finally {
            if (gridWasEnabled) {
                Grid.enableGrid();
            }
        }
    }
}
