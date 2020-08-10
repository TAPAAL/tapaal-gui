package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.EnumMap;

import javax.swing.JDialog;

import net.tapaal.swinghelpers.DispatchEventsToParentHandler;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.undo.AnnotationTextEdit;
import pipe.gui.widgets.AnnotationPanel;
import pipe.gui.widgets.EscapableDialog;

public class AnnotationNote extends Note {

	private boolean fillNote = true;

	private final EnumMap<dragPoint, ResizePoint> dragPoints = new EnumMap<>(dragPoint.class);

	private AffineTransform prova = new AffineTransform();

	public AnnotationNote(int x, int y) {
		super(x, y);
		setDragPoints();

        getNote().addMouseListener(new DispatchEventsToParentHandler());
        getNote().addMouseMotionListener(new DispatchEventsToParentHandler());
	}

	public AnnotationNote(String text, int x, int y, int w, int h, boolean border) {
		super(text, x, y, w, h, border);
		setDragPoints();

        getNote().addMouseListener(new DispatchEventsToParentHandler());
        getNote().addMouseMotionListener(new DispatchEventsToParentHandler());
	}

	@Override
	protected void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.

        mouseHandler = new AnnotationNoteHandler(this);

	}


	private void setDragPoints() {
		dragPoints.put(dragPoint.TOP_LEFT, new ResizePoint(this, ResizePoint.TOP | ResizePoint.LEFT));
		dragPoints.put(dragPoint.TOP_MIDDLE, new ResizePoint(this, ResizePoint.TOP));
		dragPoints.put(dragPoint.TOP_RIGHT, new ResizePoint(this, ResizePoint.TOP | ResizePoint.RIGHT));
		dragPoints.put(dragPoint.MIDDLE_RIGHT, new ResizePoint(this, ResizePoint.RIGHT));
		dragPoints.put(dragPoint.BOTTOM_RIGHT, new ResizePoint(this, ResizePoint.BOTTOM | ResizePoint.RIGHT));
		dragPoints.put(dragPoint.BOTTOM_MIDDLE, new ResizePoint(this, ResizePoint.BOTTOM));
		dragPoints.put(dragPoint.BOTTOM_LEFT, new ResizePoint(this, ResizePoint.BOTTOM | ResizePoint.LEFT));
		dragPoints.put(dragPoint.MIDDLE_LEFT, new ResizePoint(this, ResizePoint.LEFT));

		for (ResizePoint p : dragPoints.values()) {
			ResizePointHandler handler = new ResizePointHandler(p);
			p.addMouseListener(handler);
			p.addMouseMotionListener(handler);
			add(p);
		}
	}

	enum dragPoint {
		TOP_LEFT, // 0
		TOP_MIDDLE, // 1
		TOP_RIGHT, //2
		MIDDLE_RIGHT, // 3
		BOTTOM_RIGHT, //4
		BOTTOM_MIDDLE, //5
		BOTTOM_LEFT, //6
		MIDDLE_LEFT, //7
	}

	@Override
	public void updateBounds() {
		super.updateBounds();
		if (dragPoints != null) {
			// TOP-LEFT
			dragPoints.get(dragPoint.TOP_LEFT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMinX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMinY(), getZoom())
			);

			// TOP-MIDDLE
			dragPoints.get(dragPoint.TOP_MIDDLE).setLocation(
					Zoomer.getZoomedValue(noteRect.getCenterX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMinY(), getZoom())
			);

			// TOP-RIGHT
			dragPoints.get(dragPoint.TOP_RIGHT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMaxX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMinY(), getZoom())
			);

			// MIDDLE-RIGHT
			dragPoints.get(dragPoint.MIDDLE_RIGHT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMaxX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getCenterY(), getZoom())
			);

			// BOTTOM-RIGHT
			dragPoints.get(dragPoint.BOTTOM_RIGHT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMaxX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMaxY(), getZoom())
			);

			// BOTTOM-MIDDLE
			dragPoints.get(dragPoint.BOTTOM_MIDDLE).setLocation(
					Zoomer.getZoomedValue(noteRect.getCenterX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMaxY(), getZoom())
			);

			// BOTTOM-LEFT
			dragPoints.get(dragPoint.BOTTOM_LEFT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMinX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getMaxY(), getZoom())
			);

			// MIDDLE-LEFT
			dragPoints.get(dragPoint.MIDDLE_LEFT).setLocation(
					Zoomer.getZoomedValue(noteRect.getMinX(), getZoom()),
					Zoomer.getZoomedValue(noteRect.getCenterY(), getZoom())
			);
		}
	}

	@Override
	public boolean contains(int x, int y) {
		boolean pointContains = false;

		for (ResizePoint p : dragPoints.values()) {
			pointContains |= p.contains(x - p.getX(), y - p.getY());
		}

		return super.contains(x, y) || pointContains;
	}

	@Override
	public void enableEditMode() {
	    enableEditMode(false);
    }
    //Special order if its first edit, undo/redo
    public boolean enableEditMode(boolean isFirstEdit) {
		String oldText = note.getText();
		JDialog.setDefaultLookAndFeelDecorated(true);
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Annotation", true);

		guiDialog.add(new AnnotationPanel(this));
		guiDialog.setMinimumSize(new Dimension(300, 200));
		// Make window fit contents' preferred size
		guiDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(isFirstEdit) {
					getParent().getGuiModel().removePetriNetObject(AnnotationNote.this);
				}
			}
		});
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);

		guiDialog.setResizable(true);
		guiDialog.setVisible(true);

		guiDialog.dispose();

		String newText = note.getText();
		if (oldText != null && !newText.equals(oldText)) {
			// Text has been changed

			CreateGui.getCurrentTab().getUndoManager().addNewEdit(
					new AnnotationTextEdit(this, oldText, newText)
			);
			updateBounds();
			return true;
		}
		return false;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		prova = g2.getTransform();

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);

		g2.transform(Zoomer.getTransform(getZoom()));
		if (selected) {
			g2.setPaint(Pipe.SELECTION_FILL_COLOUR);
			g2.fill(noteRect);
			if (drawBorder) {
				g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
				g2.draw(noteRect);
			}
		} else {
			g2.setPaint(Pipe.ELEMENT_FILL_COLOUR);
			if (fillNote) {
				g2.fill(noteRect);
			}
			if (drawBorder) {
				g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
				g2.draw(noteRect);
			}
		}
		dragPoints.forEach((dragPoint, resizePoint) -> resizePoint.myPaintComponent(g));
		//for (int i = 0; i < 8; i++) {
		//	dragPoints[i].myPaintComponent(g);
		//}

		g2.transform(Zoomer.getTransform(getZoom()));
	}

	@Override
	public int getLayerOffset() {
		return Pipe.NOTE_LAYER_OFFSET;
	}

	public boolean isFilled() {
		return fillNote;
	}

	public void changeBackground() {
		fillNote = !fillNote;
		note.setOpaque(fillNote);
	}

	private class ResizePointHandler extends
			javax.swing.event.MouseInputAdapter {

		private final ResizePoint myPoint;
		private Point start;

		public ResizePointHandler(ResizePoint point) {
			myPoint = point;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(CreateGui.getCurrentTab().isInAnimationMode()) return;
			myPoint.myNote.setDraggable(false);
			myPoint.isPressed = true;
			myPoint.repaint();
			start = e.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(CreateGui.getCurrentTab().isInAnimationMode()) return;

			myPoint.drag(Grid.getModifiedX(e.getX() - start.x), Grid
					.getModifiedY(e.getY() - start.y));
			myPoint.myNote.updateBounds();
			myPoint.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(CreateGui.getCurrentTab().isInAnimationMode()) return;

			myPoint.myNote.setDraggable(true);
			myPoint.isPressed = false;
			myPoint.myNote.updateBounds();
			myPoint.repaint();
		}

	}

	@Override
	public void zoomUpdate(int percent) {
		super.zoomUpdate(percent);
		for (ResizePoint p : dragPoints.values()) {
			p.setZoom(percent);
		}
	}

	public class ResizePoint extends javax.swing.JComponent {

		private int SIZE = 3;
		private static final int TOP = 1;
		private static final int BOTTOM = 2;
		private static final int LEFT = 4;
		private static final int RIGHT = 8;

		private Rectangle shape;
		private boolean isPressed = false;
		private final Note myNote;
		public int typeMask;

		public ResizePoint(Note obj, int type) {
			myNote = obj;
			setOpaque(false);
			setBounds(
			    -SIZE - 1, -SIZE - 1,
                2 * SIZE + Pipe.ANNOTATION_SIZE_OFFSET + 1,
                2 * SIZE + Pipe.ANNOTATION_SIZE_OFFSET + 1
            );
			typeMask = type;
		}

		//Adjust the point a bit to hit center on corner of box
        @Override
		public void setLocation(int x, int y) {
			super.setLocation(x - SIZE, y - SIZE);
		}

		private void drag(int x, int y) {
			if ((typeMask & TOP) == TOP) {
				myNote.adjustTop(Zoomer.getUnzoomedValue(y, getZoom()));
			}
			if ((typeMask & BOTTOM) == BOTTOM) {
				myNote.adjustBottom(Zoomer.getUnzoomedValue(y, getZoom()));
			}
			if ((typeMask & LEFT) == LEFT) {
				myNote.adjustLeft(Zoomer.getUnzoomedValue(x, getZoom()));
			}
			if ((typeMask & RIGHT) == RIGHT) {
				myNote.adjustRight(Zoomer.getUnzoomedValue(x, getZoom()));
			}
			CreateGui.getCurrentTab().setNetChanged(true);
		}

		public void myPaintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setTransform(prova);
			if (myNote.selected) {
				g2.translate(this.getLocation().x, this.getLocation().y);
				shape = new Rectangle(0, 0, 2 * SIZE, 2 * SIZE);
				g2.fill(shape);

				g2.setStroke(new BasicStroke(1.0f));
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				if (isPressed) {
					g2.setPaint(Pipe.RESIZE_POINT_DOWN_COLOUR);
				} else {
					g2.setPaint(Pipe.ELEMENT_FILL_COLOUR);
				}
				g2.fill(shape);
				g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
				g2.draw(shape);
				g2.setTransform(prova);
			}
		}

		// Change ResizePoint's size a little bit acording to the zoom percent
		private void setZoom(int percent) {
			if (getZoom() >= 220) {
				SIZE = 5;
			} else if (getZoom() >= 120) {
				SIZE = 4;
			} else if (getZoom() >= 60) {
				SIZE = 3;
			} else {
				SIZE = 2;
			}
		}
	}
	
	public AnnotationNote copy() {
        return new AnnotationNote(note.getText(), getOriginalX(), getOriginalY(),	note.getWidth(), note.getHeight(), this.isShowingBorder());
	}

}
