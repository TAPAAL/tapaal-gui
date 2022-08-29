<<<<<<< HEAD
/*
 * Note.java
 */
package pipe.gui.petrinet.graphicElements;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;

import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;

import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.undo.AnnotationBorderEditCommand;
import net.tapaal.gui.petrinet.undo.Command;

public abstract class Note extends PetriNetObject {

	protected final JTextArea note = new JTextArea();
	protected boolean drawBorder = true;
	protected final RectangularShape noteRect = new Rectangle();

	public Note(int x, int y) {
		super("", x, y);

		note.setAlignmentX(CENTER_ALIGNMENT);
		note.setAlignmentY(CENTER_ALIGNMENT);
		note.setOpaque(false);
		note.setEditable(false);
		note.setEnabled(false);
		note.setLineWrap(true);
		note.setWrapStyleWord(true);

		// Set minimum size the preferred size for an empty string:
		note.setText("");
		note.setFont(new Font(Constants.ANNOTATION_DEFAULT_FONT, Font.PLAIN, Constants.ANNOTATION_DEFAULT_FONT_SIZE));
		note.setSize(
				note.getPreferredSize().width,
				note.getPreferredSize().height
		);
		note.setMinimumSize(note.getPreferredSize());
		note.setHighlighter(new DefaultHighlighter());
		note.setDisabledTextColor(Constants.NOTE_DISABLED_COLOUR);
		note.setForeground(Constants.NOTE_EDITING_COLOUR);
		add(note);
	}


	public Note(String text, int x, int y, int w, int h, boolean border) {
		this(x, y);
		note.setText(text);
		drawBorder = border;
		note.setSize(w, h);
		updateBounds();
	}

	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		int newHeight = note.getPreferredSize().height;

		if ((note.getHeight() < newHeight) && (newHeight >= note.getMinimumSize().height)) {
			note.setSize(note.getWidth(), newHeight);
		}

		int rectWidth = note.getWidth() + Constants.RESERVED_BORDER;
		int rectHeight = note.getHeight() + Constants.RESERVED_BORDER;

		noteRect.setFrame(Constants.RESERVED_BORDER / 2, Constants.RESERVED_BORDER / 2, rectWidth, rectHeight);
		setSize(rectWidth + Constants.ANNOTATION_SIZE_OFFSET, rectHeight + Constants.ANNOTATION_SIZE_OFFSET);

		note.setLocation(
				(int) noteRect.getX() + (rectWidth - note.getWidth()) / 2,
				(int) noteRect.getY() + (rectHeight - note.getHeight()) / 2
		);

		Rectangle bounds = new Rectangle();
		bounds.setBounds(
				Zoomer.getZoomedValue(originalX, getZoom()),
				Zoomer.getZoomedValue(originalY, getZoom()),
				(int) ((rectWidth + Constants.RESERVED_BORDER + Constants.ANNOTATION_SIZE_OFFSET) * Zoomer.getScaleFactor(getZoom())),
				(int) ((rectHeight + Constants.RESERVED_BORDER + Constants.ANNOTATION_SIZE_OFFSET) * Zoomer.getScaleFactor(getZoom()))
		);
		setBounds(bounds);
	}

	public abstract void enableEditMode();

	public boolean isShowingBorder() {
		return drawBorder;
	}

	public Command showBorder(boolean show) {
		drawBorder = show;
		repaint();
		return new AnnotationBorderEditCommand(this);
	}

	public JTextArea getNote() {
		return note;
	}

	public String getNoteText() {
		return note.getText();
	}

	public int getNoteWidth() {
		return note.getWidth();
	}

	public int getNoteHeight() {
		return note.getHeight();
	}

	/** Translates the component by x,y */
	public void translate(int x, int y) {
		setLocation(getX() + x, getY() + y);
		originalX += Zoomer.getUnzoomedValue(x, getZoom());
		originalY += Zoomer.getUnzoomedValue(y, getZoom());
		updateBounds();
	}

	protected void adjustTop(int dy) {
		if (note.getPreferredSize().height <= (note.getHeight() - dy)) {
			note.setSize(new Dimension(note.getWidth(), note.getHeight() - dy));
			setLocation(getX(), getY() + dy);
			originalY += dy;
		}
	}

	protected void adjustBottom(int dy) {
		if (note.getPreferredSize().height <= (note.getHeight() + dy)) {
			note.setSize(new Dimension(note.getWidth(), note.getHeight() + dy));
		}
	}

	protected void adjustLeft(int dx) {
		if (Constants.ANNOTATION_MIN_WIDTH <= (note.getWidth() - dx)) {
			note.setSize(new Dimension(note.getWidth() - dx, note.getHeight()));
			setLocation(getX() + dx, getY());
			originalX += dx;
		}
	}

	protected void adjustRight(int dx) {
		if (Constants.ANNOTATION_MIN_WIDTH <= (note.getWidth() + dx)) {
			note.setSize(new Dimension(note.getWidth() + dx, note.getHeight()));
		}
	}

	@Override
	public boolean contains(int x, int y) {
		return noteRect.contains(x / Zoomer.getScaleFactor(getZoom()), y
				/ Zoomer.getScaleFactor(getZoom()));
	}

	@Override
	public void addedToGui() {
        setDeleted(false);
        updateBounds();
    }

	@Override
	public void removedFromGui() {

	}

	public void setText(String text) {
		note.setText(text);
		note.setSize(note.getPreferredSize());
	}

	public String getText() {
		return note.getText();
	}

	@Override
	public int getLayerOffset() {
		return Constants.NOTE_LAYER_OFFSET;
	}

	public void zoomUpdate(int percent) {
		super.zoomUpdate(percent);
		updateBounds();
	}

}
=======
/*
 * Note.java
 */
package pipe.gui.petrinet.graphicElements;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;

import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;

import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.undo.AnnotationBorderEditCommand;
import net.tapaal.gui.petrinet.undo.Command;

public abstract class Note extends PetriNetObject {

	protected final JTextArea note = new JTextArea();
	protected boolean drawBorder = true;
	protected final RectangularShape noteRect = new Rectangle();

	public Note(int x, int y) {
		super("", x, y);

		note.setAlignmentX(CENTER_ALIGNMENT);
		note.setAlignmentY(CENTER_ALIGNMENT);
		note.setOpaque(false);
		note.setEditable(false);
		note.setEnabled(false);
		note.setLineWrap(true);
		note.setWrapStyleWord(true);

		// Set minimum size the preferred size for an empty string:
		note.setText("");
		note.setFont(new Font(Constants.ANNOTATION_DEFAULT_FONT, Font.PLAIN, Constants.ANNOTATION_DEFAULT_FONT_SIZE));
		note.setSize(
				note.getPreferredSize().width,
				note.getPreferredSize().height
		);
		note.setMinimumSize(note.getPreferredSize());
		note.setHighlighter(new DefaultHighlighter());
		note.setDisabledTextColor(Constants.NOTE_DISABLED_COLOUR);
		note.setForeground(Constants.NOTE_EDITING_COLOUR);
		add(note);
	}


	public Note(String text, int x, int y, int w, int h, boolean border) {
		this(x, y);
		note.setText(text);
		drawBorder = border;
		note.setSize(w, h);
		updateBounds();
	}

	/** Calculates the BoundsOffsets used for setBounds() method */
	public void updateBounds() {
		int newHeight = note.getPreferredSize().height;

		if ((note.getHeight() < newHeight) && (newHeight >= note.getMinimumSize().height)) {
			note.setSize(note.getWidth(), newHeight);
		}

		int rectWidth = note.getWidth() + Constants.RESERVED_BORDER;
		int rectHeight = note.getHeight() + Constants.RESERVED_BORDER;

		noteRect.setFrame(Constants.RESERVED_BORDER / 2, Constants.RESERVED_BORDER / 2, rectWidth, rectHeight);
		setSize(rectWidth + Constants.ANNOTATION_SIZE_OFFSET, rectHeight + Constants.ANNOTATION_SIZE_OFFSET);

		note.setLocation(
				(int) noteRect.getX() + (rectWidth - note.getWidth()) / 2,
				(int) noteRect.getY() + (rectHeight - note.getHeight()) / 2
		);

		Rectangle bounds = new Rectangle();
		bounds.setBounds(
				Zoomer.getZoomedValue(originalX, getZoom()),
				Zoomer.getZoomedValue(originalY, getZoom()),
				(int) ((rectWidth + Constants.RESERVED_BORDER + Constants.ANNOTATION_SIZE_OFFSET) * Zoomer.getScaleFactor(getZoom())),
				(int) ((rectHeight + Constants.RESERVED_BORDER + Constants.ANNOTATION_SIZE_OFFSET) * Zoomer.getScaleFactor(getZoom()))
		);
		setBounds(bounds);
	}

	public abstract void enableEditMode();

	public boolean isShowingBorder() {
		return drawBorder;
	}

	public Command showBorder(boolean show) {
		drawBorder = show;
		repaint();
		return new AnnotationBorderEditCommand(this);
	}

	public JTextArea getNote() {
		return note;
	}

	public String getNoteText() {
		return note.getText();
	}

	public int getNoteWidth() {
		return note.getWidth();
	}

	public int getNoteHeight() {
		return note.getHeight();
	}

	/** Translates the component by x,y */
	public void translate(int x, int y) {
		setLocation(getX() + x, getY() + y);
		originalX += Zoomer.getUnzoomedValue(x, getZoom());
		originalY += Zoomer.getUnzoomedValue(y, getZoom());
		updateBounds();
	}

	protected void adjustTop(int dy) {
		if (note.getPreferredSize().height <= (note.getHeight() - dy)) {
			note.setSize(new Dimension(note.getWidth(), note.getHeight() - dy));
			setLocation(getX(), getY() + dy);
			originalY += dy;
		}
	}

	protected void adjustBottom(int dy) {
		if (note.getPreferredSize().height <= (note.getHeight() + dy)) {
			note.setSize(new Dimension(note.getWidth(), note.getHeight() + dy));
		}
	}

	protected void adjustLeft(int dx) {
		if (Constants.ANNOTATION_MIN_WIDTH <= (note.getWidth() - dx)) {
			note.setSize(new Dimension(note.getWidth() - dx, note.getHeight()));
			setLocation(getX() + dx, getY());
			originalX += dx;
		}
	}

	protected void adjustRight(int dx) {
		if (Constants.ANNOTATION_MIN_WIDTH <= (note.getWidth() + dx)) {
			note.setSize(new Dimension(note.getWidth() + dx, note.getHeight()));
		}
	}

	@Override
	public boolean contains(int x, int y) {
		return noteRect.contains(x / Zoomer.getScaleFactor(getZoom()), y
				/ Zoomer.getScaleFactor(getZoom()));
	}

	@Override
	public void addedToGui() {
        setDeleted(false);
        updateBounds();
    }

	@Override
	public void removedFromGui() {

	}

	public void setText(String text) {
		note.setText(text);
		note.setSize(note.getPreferredSize());
	}

	public String getText() {
		return note.getText();
	}

	@Override
	public int getLayerOffset() {
		return Constants.NOTE_LAYER_OFFSET;
	}

	public void zoomUpdate(int percent) {
		super.zoomUpdate(percent);
		updateBounds();
	}

}
>>>>>>> origin/cpn
