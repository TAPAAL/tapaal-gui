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
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JTextArea;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.Place;
import pipe.gui.handler.PlaceHandler;
import pipe.gui.undo.TimedPlaceInvariantEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PlaceEditorPanel;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.ConstantBound;
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
		listener = timedPlaceListener();		
		this.place.addTimedPlaceListener(listener);

		attributesVisible = true;
		ageOfTokensWindow = new Window(new Frame());

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		addMouseHandler();
	}

	public TimedPlaceComponent(double positionXInput, double positionYInput,
			String idInput, Double nameOffsetXInput,
			Double nameOffsetYInput) {

		super(positionXInput, positionYInput, idInput,
				nameOffsetXInput, nameOffsetYInput);
		listener = timedPlaceListener();
		attributesVisible = true;
		ageOfTokensWindow = new Window(new Frame());

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		addMouseHandler();
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		mouseHandler = new PlaceHandler(this);
	}

	private TimedPlaceListener timedPlaceListener() {
		return new TimedPlaceListener() {
			public void nameChanged(TimedPlaceEvent e) {
				TimedPlace place = e.source();
				TimedPlaceComponent.super.setName(place.name());
			}
			
			public void invariantChanged(TimedPlaceEvent e) { 
				update(true); 
			}
			
			public void markingChanged(TimedPlaceEvent e) { 
				repaint();
			}
		};
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
		buffer.append('}');

		return buffer.toString();
	}

	public boolean isAgeOfTokensShown() {
		return ageOfTokensWindow.isVisible();
	}

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

        // Decide whether or not to draw tokens as dots
        boolean drawDots = false;
        if(!CreateGui.getApp().showTokenAge()) {
            // only draw dots if there're 1-5 tokens.
            drawDots = (marking > 0 && marking < 6);

            // if any token's age is > 0, do not draw dots
			for (TimedToken token : myTokens) {
				if (token.age().compareTo(BigDecimal.valueOf(0)) != 0) {
					drawDots = false;
					break;
				}
			}
        }

        // structure sees how many markings there are and fills the place in
        // with the appropriate number or tokens.
        if(drawDots) {
			int xPos, yPos;
            switch(marking){
                case 5:
                    g.fillOval(x + tMiddleX, y + tMiddleY, tWidth, tHeight); // middle
                /* falls through */
                case 4:
                    g.fillOval(x + tLeftX, y + tTopY, tWidth, tHeight); // top left
                /* falls through */
                case 3:
                    if (marking == 5 || marking == 4) {
                        xPos = x + tRightX;	// top right
                        yPos = y + tTopY;
                    } else {
                        xPos = x + tLeftX;		// top left
                        yPos = y + tTopY;
                    }
                    g.fillOval(xPos, yPos, tWidth, tHeight);
                /* falls through */
                case 2:
                    if (marking == 5 || marking == 4){
                        xPos = x + tLeftX;       // bottom left
                        yPos = y + tBotY;
                    } else if (marking == 3){
                        xPos = x + tMiddleX;   // middle
                        yPos = y + tMiddleY;
                    } else {
                        xPos = x + tLeftX;       // left middle
                        yPos = y + tMiddleY;
                    }
                    g.fillOval(xPos, yPos, tWidth, tHeight);
                /* falls through */
                case 1:
                    if(marking == 5 || marking == 4 || marking == 3){
                        xPos = x + tRightX;    // bottom right
                        yPos = y + tBotY;
                    } else if (marking == 2){
                        xPos = x + tRightX;    // right middle
                        yPos = y + tMiddleY;
                    } else {
                        xPos = x + tMiddleX; // middle
                        yPos = y + tMiddleY;
                    }
                    g.fillOval(xPos, yPos, tWidth, tHeight);
                case 0:
                    break;
            }
        } else {	// print token age
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
	}

	public Command setInvariant(TimeInvariant inv) {
		TimeInvariant old = place.invariant();
		place.setInvariant(inv);

		update(true);

		return new TimedPlaceInvariantEdit(this, old, inv);
	}

	public void showAgeOfTokens(boolean show) {
		
		if (ageOfTokensWindow != null){
			ageOfTokensWindow.dispose();
		}
		
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

			if (!(place.invariant().upperBound() instanceof InfBound)) {
				pnName.setText("\nInv: " + place.invariant().toString(displayConstantNames));
			}else{
				pnName.setText("");
			}
			
			// Handle constant highlighting
			boolean focusedConstant = false;
			if(place.invariant().upperBound() instanceof ConstantBound){
				if(((ConstantBound) place.invariant().upperBound()).constant().hasFocus()){
					focusedConstant = true;
				}
			}
			if(focusedConstant){
				pnName.setForeground(Pipe.SELECTION_TEXT_COLOUR);
			}else{
				pnName.setForeground(Pipe.ELEMENT_TEXT_COLOUR);
			}
			
			pnName.displayName(attributesVisible);
			
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

	public TimedPlaceComponent copy(TimedArcPetriNet tapn) {
		TimedPlaceComponent placeComponent = new TimedPlaceComponent(getPositionXObject(), getPositionYObject(), id, nameOffsetX, nameOffsetY);
		placeComponent.setUnderlyingPlace(tapn.getPlaceByName(place.name()));

		return placeComponent;
	}
}
