package pipe.gui.petrinet.undo;

import net.tapaal.gui.petrinet.undo.Command;
import pipe.gui.petrinet.graphicElements.AnnotationNote.ResizePoint;
import java.awt.Point;
import java.awt.Dimension;

public class AnnotationResizeCommand implements Command {
    private final ResizePoint resizePoint;
    private final Point oldPoint;
    private final Dimension oldSize;
    private final Point newPoint;
    private final Dimension newSize;

    public AnnotationResizeCommand(ResizePoint resizePoint, Point oldPoint, Dimension oldSize) {
        this.resizePoint = resizePoint; 
        this.oldPoint = oldPoint;
        this.oldSize = oldSize;
        this.newPoint = resizePoint.getNote().getLocation();
        this.newSize = resizePoint.getNote().getNote().getSize();
    }
    
    @Override
    public void undo() {
        update(oldPoint, oldSize);
    }

    @Override
    public void redo() {
        update(newPoint, newSize);
    }

    private void update(Point point, Dimension size) {
        resizePoint.getNote().setPosition(point);
        resizePoint.getNote().setInnerNoteSize(size);
        resizePoint.getNote().updateBounds();
        resizePoint.repaint();
    }
}
