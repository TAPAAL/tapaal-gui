package pipe.gui.graphicElements.tapn;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JTextArea;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.undo.TimedPlaceInvariantEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PlaceEditorPanel;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;

public class TimedPlaceComponent extends Place {
	private static final long serialVersionUID = 1L;

	private dk.aau.cs.model.tapn.TimedPlace place;
	private dk.aau.cs.model.tapn.event.TimedPlaceListener listener;

	private Window ageOfTokensWindow;
	private Shape dashedOutline = createDashedOutline();

	public TimedPlaceComponent(double positionXInput, double positionYInput, dk.aau.cs.model.tapn.TimedPlace place) {
		super(positionXInput, positionYInput);
		this.place = place;
		this.listener = timedPlaceListener();		
		this.place.addTimedPlaceListener(listener);

		attributesVisible = true;
		ageOfTokensWindow = new Window(new Frame());
	}

	public TimedPlaceComponent(double positionXInput, double positionYInput,
			String idInput, String nameInput, Double nameOffsetXInput,
			Double nameOffsetYInput, int initialMarkingInput,
			double markingOffsetXInput, double markingOffsetYInput,
			int capacityInput) {

		super(positionXInput, positionYInput, idInput, nameInput,
				nameOffsetXInput, nameOffsetYInput, initialMarkingInput,
				markingOffsetXInput, markingOffsetYInput, capacityInput);
		listener = timedPlaceListener();
		attributesVisible = true;
		ageOfTokensWindow = new Window(new Frame());
	}

	private TimedPlaceListener timedPlaceListener() {
		return new TimedPlaceListener() {
			public void nameChanged(TimedPlaceEvent e) {
				TimedPlace place = e.source();
				TimedPlaceComponent.super.setName(place.name());				
			}
			public void invariantChanged(TimedPlaceEvent e) { update(true); }
			public void markingChanged(TimedPlaceEvent e) { repaint(); }
		};
	}
	
	@Override
	public TimedPlaceComponent clone() {
		TimedPlaceComponent toReturn = (TimedPlaceComponent) super.clone();

		toReturn.setInvariant(this.getInvariant());
		return toReturn;

	}

	@Override
	public TimedPlaceComponent copy() {
		TimedPlaceComponent copy = new TimedPlaceComponent(Zoomer
				.getUnzoomedValue(this.getX(), zoom), Zoomer.getUnzoomedValue(
						this.getY(), zoom), this.place);
		copy.pnName.setName(this.getName());
		copy.nameOffsetX = this.nameOffsetX;
		copy.nameOffsetY = this.nameOffsetY;
		copy.capacity = this.capacity;
		copy.attributesVisible = this.attributesVisible;
		copy.initialMarking = this.initialMarking;
		copy.currentMarking = this.currentMarking;
		copy.markingOffsetX = this.markingOffsetX;
		copy.markingOffsetY = this.markingOffsetY;
		copy.setOriginal(this);
		return copy;
	}

	@Override
	public void delete() {
		super.delete();
	}

	public String getInvariantAsString() {

		return getInvariant().toString();
	}

	public TimeInvariant getInvariant() {
		return place.invariant();
	}

	public String getStringOfTokens() {
		StringBuffer buffer = new StringBuffer("{");
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		Iterable<TimedToken> tokens = place.tokens();
		boolean first = true;
		for (TimedToken token : tokens) {
			if (!first)
				buffer.append(", ");
			buffer.append(df.format(token.age()));
			first = false;
		}
		buffer.append("}");

		return buffer.toString();
	}

	// TODO: get rid of
	public ArrayList<BigDecimal> getTokens() {
		ArrayList<BigDecimal> tokensToReturn = new ArrayList<BigDecimal>();

		for (TimedToken t : place.tokens()) {
			tokensToReturn.add(t.age());
		}

		return tokensToReturn;
	}

	public boolean isAgeOfTokensShown() {
		return ageOfTokensWindow.isVisible();
	}

	// overide method
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintTokens(g);
		if(place.isShared()){
			Graphics2D graphics = (Graphics2D)g;
			Stroke oldStroke = graphics.getStroke();

			BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {5.0f}, 0.0f);
			graphics.setStroke(dashed);

			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.draw(dashedOutline);

			graphics.setStroke(oldStroke);
		}
	}

	protected void paintTokens(Graphics g) {
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(1);
		df.setRoundingMode(RoundingMode.DOWN);

		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;

		// int marking = getCurrentMarking();
		List<TimedToken> myTokens = place.tokens();
		int marking = place.numberOfTokens();

		// structure sees how many markings there are and fills the place in
		// with
		// the appropriate number.
		switch (marking) {
		case 2:
			if (myTokens.get(1).age().compareTo(BigDecimal.valueOf(9)) > 0) {
				g.setFont(new Font("new font", Font.PLAIN, 11));
				g.drawString(df.format(myTokens.get(1).age()), x + 17 - 12,
						y + 13 + 1);
			} else {
				g.drawString(df.format(myTokens.get(1).age()), x + 17 - 10,
						y + 13 + 1);
			}
			// g.fillOval(x + 18, y + 6, tWidth, tHeight);
			/* falls through */
		case 1:
			if (myTokens.get(0).age().compareTo(BigDecimal.valueOf(9)) > 0) {
				g.setFont(new Font("new font", Font.PLAIN, 11));
				g.drawString(df.format(myTokens.get(0).age()), x + 11 - 6,
						y + 20 + 6);
			} else {
				g.drawString(df.format(myTokens.get(0).age()), x + 11 - 4,
						y + 20 + 6);
			}
			// g.fillOval(x + 12, y + 13, tWidth, tHeight);
			break;
		case 0:
			break;
		default:
			if (marking > 999) {
				// XXX could be better...
				g.drawString("#" + String.valueOf(marking), x, y + 20);
			} else if (marking > 99) {
				g.drawString("#" + String.valueOf(marking), x, y + 20);
			} else if (marking > 9) {
				g.drawString("#" + String.valueOf(marking), x + 2, y + 20);
			} else {
				g.drawString("#" + String.valueOf(marking), x + 6, y + 20);
			}
			break;
		}
	}

	public Command setInvariant(TimeInvariant inv) {
		TimeInvariant old = place.invariant();
		place.setInvariant(inv);

		update(true);

		return new TimedPlaceInvariantEdit(this, old, inv);
	}

	public void showAgeOfTokens(boolean show) {
		if (ageOfTokensWindow != null)
			ageOfTokensWindow.dispose();
		// Build interface
		if (show) {
			ageOfTokensWindow = new Window(new Frame());
			ageOfTokensWindow.add(new JTextArea(getStringOfTokens()));
			ageOfTokensWindow.getComponent(0).setBackground(Color.lightGray);

			// Make window fit contents' preferred size
			ageOfTokensWindow.pack();

			// Move window to the middle of the screen
			ageOfTokensWindow.setLocationRelativeTo(this);
			int numberOfTokens = place.numberOfTokens();
			if (numberOfTokens <= 12) {
				ageOfTokensWindow.setLocation(
						ageOfTokensWindow.getLocation().x, ageOfTokensWindow
						.getLocation().y
						+ 31 + numberOfTokens);
			} else if (numberOfTokens > 12 && numberOfTokens < 20) {
				ageOfTokensWindow.setLocation(
						ageOfTokensWindow.getLocation().x, ageOfTokensWindow
						.getLocation().y
						+ 40 + numberOfTokens);
			} else if (numberOfTokens >= 20 && numberOfTokens <= 35) {
				ageOfTokensWindow.setLocation(
						ageOfTokensWindow.getLocation().x, ageOfTokensWindow
						.getLocation().y
						+ 35 + numberOfTokens);
			} else if (numberOfTokens > 35 && numberOfTokens < 40) {
				ageOfTokensWindow.setLocation(
						ageOfTokensWindow.getLocation().x, ageOfTokensWindow
						.getLocation().y
						+ 40 + numberOfTokens);
			} else {
				ageOfTokensWindow.setLocation(
						ageOfTokensWindow.getLocation().x, ageOfTokensWindow
						.getLocation().y
						+ 20 + numberOfTokens);
			}
			ageOfTokensWindow.setVisible(show);
		}
	}

	@Override
	public void showEditor() {
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Place", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new PlaceEditorPanel(guiDialog.getRootPane(), this, new Context(CreateGui.getCurrentTab())));

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	@Override
	public void update(boolean displayConstantNames) {
		if(place != null) {
			pnName.setName(place.name());

			if (!(place.invariant().upperBound() instanceof InfBound) && attributesVisible) {
				pnName.setText("\nInv: " + place.invariant().toString(displayConstantNames));
			}else{
				pnName.setText("");
			}
		} else {
			pnName.setName("");
			pnName.setText("");
		}
		pnName.zoomUpdate(zoom);
		updateOnMoveOrZoom();
		repaint();
	}

	@Override
	public String toString() {
		return getName();
	}

	public TimedPlace underlyingPlace() {
		return place;
	}

	public void setUnderlyingPlace(TimedPlace place) {
		if(this.place != null && listener != null){
			this.place.removeTimedPlaceListener(listener);
		}
		place.addTimedPlaceListener(listener);
		this.place = place;
		this.update(true);
	}

	public void addTokens(int numberOfTokensToAdd) {
		for (int i = 0; i < numberOfTokensToAdd; i++) {
			place.addToken(new TimedToken(place, BigDecimal.ZERO));
		}
	}

	public void removeTokens(int numberOfTokensToRemove) {
		for (int i = 0; i < numberOfTokensToRemove; i++) {
			place.removeToken();
		}
	}

	public int getNumberOfTokens() {
		return place.numberOfTokens(); 
	}

	@Override
	public void setName(String nameInput) {
		place.setName(nameInput);
		super.setName(nameInput);
	}

	private static Shape createDashedOutline(){
		return new Ellipse2D.Double(-Pipe.DASHED_PADDING/2, -Pipe.DASHED_PADDING/2, DIAMETER + Pipe.DASHED_PADDING, DIAMETER + Pipe.DASHED_PADDING);
	}

	public TimedPlaceComponent copy(TimedArcPetriNet tapn, DataLayer guiModel) {
		TimedPlaceComponent placeComponent = new TimedPlaceComponent(positionX, positionY, id, place.name(), nameOffsetX, nameOffsetY, initialMarking, markingOffsetX, markingOffsetY, capacity);
		placeComponent.setUnderlyingPlace(tapn.getPlaceByName(place.name()));

		LabelHandler labelHandler = new LabelHandler(placeComponent.getNameLabel(), placeComponent);
		placeComponent.getNameLabel().addMouseListener(labelHandler);
		placeComponent.getNameLabel().addMouseMotionListener(labelHandler);
		placeComponent.getNameLabel().addMouseWheelListener(labelHandler);

		PlaceHandler placeHandler = new PlaceHandler((DrawingSurfaceImpl)getParent(), placeComponent, guiModel, tapn);
		placeComponent.addMouseListener(placeHandler);
		placeComponent.addMouseWheelListener(placeHandler);
		placeComponent.addMouseMotionListener(placeHandler);

		placeComponent.setGuiModel(guiModel);

		return placeComponent;
	}
}
