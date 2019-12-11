package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JDialog;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.GuiFrame.GUIMode;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.AnnotationNoteHandler;
import pipe.gui.undo.AnnotationTextEdit;
import pipe.gui.widgets.AnnotationPanel;
import pipe.gui.widgets.EscapableDialog;

public class AnnotationNote extends Note {

	private static final long serialVersionUID = 3503959956765396720L;

	private boolean fillNote = true;

	private ResizePoint[] dragPoints = new ResizePoint[8];

	private AffineTransform prova = new AffineTransform();
	
	private boolean isNew;

	public AnnotationNote(int x, int y, boolean isNew) {
		super(x, y);
		setDragPoints();
		this.isNew = isNew;

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		addMouseHandler();
	}

	public AnnotationNote(String text, int x, int y, int w, int h, boolean border, boolean isNew) {
		super(text, x, y, w, h, border);
		this.isNew = isNew;
		setDragPoints();

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		addMouseHandler();
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.

		AnnotationNoteHandler h = new AnnotationNoteHandler(this);
		mouseHandler = h;

		getNote().addMouseListener(h);
		getNote().addMouseMotionListener(h);

	}


	private void setDragPoints() {
		dragPoints[0] = new ResizePoint(this, ResizePoint.TOP
				| ResizePoint.LEFT);
		dragPoints[1] = new ResizePoint(this, ResizePoint.TOP);
		dragPoints[2] = new ResizePoint(this, ResizePoint.TOP
				| ResizePoint.RIGHT);
		dragPoints[3] = new ResizePoint(this, ResizePoint.RIGHT);
		dragPoints[4] = new ResizePoint(this, ResizePoint.BOTTOM
				| ResizePoint.RIGHT);
		dragPoints[5] = new ResizePoint(this, ResizePoint.BOTTOM);
		dragPoints[6] = new ResizePoint(this, ResizePoint.BOTTOM
				| ResizePoint.LEFT);
		dragPoints[7] = new ResizePoint(this, ResizePoint.LEFT);

		for (int i = 0; i < 8; i++) {
			ResizePointHandler handler = new ResizePointHandler(dragPoints[i]);
			dragPoints[i].addMouseListener(handler);
			dragPoints[i].addMouseMotionListener(handler);
			add(dragPoints[i]);
		}
	}

	@Override
	public void updateBounds() {
		super.updateBounds();
		if (dragPoints != null) {
			// TOP-LEFT
			dragPoints[0].setLocation(Zoomer.getZoomedValue(noteRect.getMinX(),
					zoom), Zoomer.getZoomedValue(noteRect.getMinY(), zoom));
			dragPoints[0].setZoom(zoom);
			// TOP-MIDDLE
			dragPoints[1].setLocation(Zoomer.getZoomedValue(noteRect
					.getCenterX(), zoom), Zoomer.getZoomedValue(noteRect
					.getMinY(), zoom));
			dragPoints[1].setZoom(zoom);
			// TOP-RIGHT
			dragPoints[2].setLocation(Zoomer.getZoomedValue(noteRect.getMaxX(),
					zoom), Zoomer.getZoomedValue(noteRect.getMinY(), zoom));
			dragPoints[2].setZoom(zoom);
			// MIDDLE-RIGHT
			dragPoints[3].setLocation(Zoomer.getZoomedValue(noteRect.getMaxX(),
					zoom), Zoomer.getZoomedValue(noteRect.getCenterY(), zoom));
			dragPoints[3].setZoom(zoom);
			// BOTTOM-RIGHT
			dragPoints[4].setLocation(Zoomer.getZoomedValue(noteRect.getMaxX(),
					zoom), Zoomer.getZoomedValue(noteRect.getMaxY(), zoom));
			dragPoints[4].setZoom(zoom);
			// BOTTOM-MIDDLE
			dragPoints[5].setLocation(Zoomer.getZoomedValue(noteRect
					.getCenterX(), zoom), Zoomer.getZoomedValue(noteRect
					.getMaxY(), zoom));
			dragPoints[5].setZoom(zoom);
			// BOTTOM-LEFT
			dragPoints[6].setLocation(Zoomer.getZoomedValue(noteRect.getMinX(),
					zoom), Zoomer.getZoomedValue(noteRect.getMaxY(), zoom));
			dragPoints[6].setZoom(zoom);
			// MIDDLE-LEFT
			dragPoints[7].setLocation(Zoomer.getZoomedValue(noteRect.getMinX(),
					zoom), Zoomer.getZoomedValue(noteRect.getCenterY(), zoom));
			dragPoints[7].setZoom(zoom);
		}
	}

	@Override
	public boolean contains(int x, int y) {
		boolean pointContains = false;

		for (int i = 0; i < 8; i++) {
			pointContains |= dragPoints[i].contains(x - dragPoints[i].getX(), y
					- dragPoints[i].getY());
		}

		return super.contains(x, y) || pointContains;
	}

	@Override
	public void enableEditMode() {
		String oldText = note.getText();
		JDialog.setDefaultLookAndFeelDecorated(true);
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(),
				"Edit Annotation", true);

		guiDialog.add(new AnnotationPanel(this));
		guiDialog.setMinimumSize(new Dimension(300, 200));
		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);

		guiDialog.setResizable(true);
		guiDialog.setVisible(true);

		guiDialog.dispose();

		String newText = note.getText();
		if (oldText != null && !newText.equals(oldText)) {
			// Text has been changed
			CreateGui.getDrawingSurface().getUndoManager().addEdit(
					new AnnotationTextEdit(this, oldText, newText));
			updateBounds();
		}
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

		g2.transform(Zoomer.getTransform(zoom));
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
		for (int i = 0; i < 8; i++) {
			dragPoints[i].myPaintComponent(g);
		}

		g2.transform(Zoomer.getTransform(zoom));
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

		private ResizePoint myPoint;
		private Point start;

		public ResizePointHandler(ResizePoint point) {
			myPoint = point;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;
			myPoint.myNote.setDraggable(false);
			myPoint.isPressed = true;
			myPoint.repaint();
			start = e.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;

			myPoint.drag(Grid.getModifiedX(e.getX() - start.x), Grid
					.getModifiedY(e.getY() - start.y));
			myPoint.myNote.updateBounds();
			myPoint.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {			
			if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)) return;

			myPoint.myNote.setDraggable(true);
			myPoint.isPressed = false;
			myPoint.myNote.updateBounds();
			myPoint.repaint();
		}

	}

	public class ResizePoint extends javax.swing.JComponent {

		private static final long serialVersionUID = -1615544376708838434L;
		private int SIZE = 3;
		private static final int TOP = 1;
		private static final int BOTTOM = 2;
		private static final int LEFT = 4;
		private static final int RIGHT = 8;

		private Rectangle shape;
		private boolean isPressed = false;
		private Note myNote;
		public int typeMask;

		public ResizePoint(Note obj, int type) {
			myNote = obj;
			setOpaque(false);
			setBounds(-SIZE - 1, -SIZE - 1, 2 * SIZE
					+ Pipe.ANNOTATION_SIZE_OFFSET + 1, 2 * SIZE
					+ Pipe.ANNOTATION_SIZE_OFFSET + 1);
			typeMask = type;
		}

		public void setLocation(double x, double y) {
			super.setLocation((int) (x - SIZE), (int) (y - SIZE));
		}

		private void drag(int x, int y) {
			if ((typeMask & TOP) == TOP) {
				myNote.adjustTop(Zoomer.getUnzoomedValue(y, zoom));
			}
			if ((typeMask & BOTTOM) == BOTTOM) {
				myNote.adjustBottom(Zoomer.getUnzoomedValue(y, zoom));
			}
			if ((typeMask & LEFT) == LEFT) {
				myNote.adjustLeft(Zoomer.getUnzoomedValue(x, zoom));
			}
			if ((typeMask & RIGHT) == RIGHT) {
				myNote.adjustRight(Zoomer.getUnzoomedValue(x, zoom));
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
			if (zoom >= 220) {
				SIZE = 5;
			} else if (zoom >= 120) {
				SIZE = 4;
			} else if (zoom >= 60) {
				SIZE = 3;
			}
		}
	}
	
	public AnnotationNote copy() {
		AnnotationNote annotation = new AnnotationNote(note.getText(), getOriginalX(), getOriginalY(),	note.getWidth(), note.getHeight(), this.isShowingBorder(), isNew);
		
		return annotation;
	}
	
	public boolean isNew(){
		if(isNew){
			isNew = false;
			return true;
		}
		return false;
	}

}
