package pipe.gui.petrinet.graphicElements.tapn;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.swing.*;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.CPN.Expressions.AddExpression;
import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.petrinet.editor.PlaceEditorPanel;
import net.tapaal.gui.petrinet.Context;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;

public class TimedPlaceComponent extends Place {

    private dk.aau.cs.model.tapn.TimedPlace place;
    private final dk.aau.cs.model.tapn.event.TimedPlaceListener listener = timedPlaceListener();

    private Window ageOfTokensWindow = new Window(new Frame());
    private final Shape dashedOutline = createDashedOutline();

    public TimedPlaceComponent (
        int positionXInput,
        int positionYInput,
        dk.aau.cs.model.tapn.TimedPlace place,
        TAPNLens lens
    ) {
        super(positionXInput, positionYInput);
        this.place = place;
        this.place.addTimedPlaceListener(listener);
        this.lens = lens;
        attributesVisible = true;
    }

    public TimedPlaceComponent(
        int positionXInput,
        int positionYInput,
        String idInput,
        int nameOffsetXInput,
        int nameOffsetYInput,
        TAPNLens lens
    ) {
        super(positionXInput, positionYInput, idInput, nameOffsetXInput, nameOffsetYInput);
        attributesVisible = true;
        this.lens = lens;

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

    private String getStringOfTokens() {
        StringBuilder buffer = new StringBuilder("{");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(Constants.AGE_DECIMAL_PRECISION);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        boolean first = true;
        for (TimedToken token : place.tokens()) {
            if (!first) {
                buffer.append(", ");
            }
            buffer.append(df.format(token.age()));

            first = false;
        }
        buffer.append('}');

        return buffer.toString();
    }

    private String getStringOfColoredTimedTokens() {
        StringBuilder buffer = new StringBuilder("{");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(Constants.AGE_DECIMAL_PRECISION);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        boolean first = true;
        for (TimedToken token : place.tokens()) {
            if (!first) {
                buffer.append(", ");
            }
            buffer.append("(");
            if (token.color().getColorName().equals("")) {
                buffer.append(token.color().toString());
            } else {
                buffer.append(token.color().getColorName());
            }
            buffer.append(", ");
            buffer.append(df.format(token.age()));
            buffer.append(")");


            first = false;
        }
        buffer.append('}');

        return buffer.toString();
    }

    private String getStringOfColoredTokens() {
        StringBuilder buffer = new StringBuilder("{");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(Constants.AGE_DECIMAL_PRECISION);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        boolean first = true;
        for (TimedToken token : place.tokens()) {
            if (!first) {
                buffer.append(", ");
            }
            if (token instanceof TimedToken) {
                if (token.color().getColorName().equals("")) {
                    buffer.append(token.color().toString());
                } else {
                    buffer.append(token.color().getColorName());
                }
            }

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

            int margin = 1;
            graphics.setClip(new Rectangle2D.Double(dashedOutline.getBounds().x - margin, dashedOutline.getBounds().y - margin, dashedOutline.getBounds().width + margin * 2, dashedOutline.getBounds().height + margin * 2));
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
        if(!TAPAALGUI.getAppGui().showTokenAge()) {
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
        if(!lens.isTimed()){
            drawDots = (marking > 0 && marking < 6);
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
        } else if(!isColored()) {	// print token age
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
                    if (marking > 99999999) {
                        String subMarking1 = String.valueOf(marking).substring(0, 4);
                        String subMarking2 = String.valueOf(marking).substring(4);
                        g.drawString(">" + subMarking1, x - 5, y + 15);
                        g.drawString(subMarking2, x + 4, y + 25);
                    } else if (marking > 9999) {
                        // XXX could be better...
                        String subMarking1 = String.valueOf(marking).substring(0, 4);
                        String subMarking2 = String.valueOf(marking).substring(4);
                        g.drawString("#" + subMarking1, x - 5, y + 15);
                        g.drawString(subMarking2, x + 4, y + 25);
                    } else if (marking > 999) {
                        // XXX could be better...
                        g.drawString("#" + marking, x - 5, y + 20);
                    } else if (marking > 99) {
                        g.drawString("#" + marking, x, y + 20);
                    } else if (marking > 9) {
                        g.drawString("#" + marking, x + 2, y + 20);
                    } else {
                        g.drawString("#" + marking, x + 6, y + 20);
                    }
                    break;
            }
        } else{
            if (marking > 9999) {
                // XXX could be better...
                String subMarking1 = String.valueOf(marking).substring(0, 4);
                String subMarking2 = String.valueOf(marking).substring(4);
                g.drawString("#" + subMarking1, x - 5, y + 15);
                g.drawString(subMarking2, x + 4, y + 25);
            } else if (marking > 999) {
                // XXX could be better...
                g.drawString("#" + marking, x - 5, y + 20);
            } else if (marking > 99) {
                g.drawString("#" + marking, x, y + 20);
            } else if (marking > 9) {
                g.drawString("#" + marking, x + 2, y + 20);
            } else if (marking > 0) {
                g.drawString("#" + marking, x + 6, y + 20);
            }
        }
    }

    public void setInvariant(TimeInvariant inv) {
        place.setInvariant(inv);

        update(true);
    }

    public void showAgeOfTokens(boolean show) {

        if (ageOfTokensWindow != null){
            ageOfTokensWindow.dispose();
        }

        // Build interface
        if (show && (isTimed() || isColored())) {
            ageOfTokensWindow = new Window(new Frame());
            if (isColored() && !isTimed()) {
                ageOfTokensWindow.add(new JTextArea(getStringOfColoredTokens()));
            } else if (!isColored() && isTimed()){
                ageOfTokensWindow.add(new JTextArea(getStringOfTokens()));
            } else {
                ageOfTokensWindow.add(new JTextArea(getStringOfColoredTimedTokens()));

            }
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
        } else if (show && isColored()) {

        }
    }

    @Override
    public void showEditor() {
        // Build interface
        EscapableDialog guiDialog = new EscapableDialog(TAPAALGUI.getApp(), "Edit Place", true);

        Container contentPane = guiDialog.getContentPane();

        // 1 Set layout
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        // 2 Add Place editor
        JPanel placeEditorPanel = new PlaceEditorPanel(guiDialog, guiDialog.getRootPane(), this, new Context(TAPAALGUI.getCurrentTab()));
        contentPane.add(placeEditorPanel);
        guiDialog.setResizable(true);

        // Make window fit contents' preferred size
        guiDialog.pack();

        // Move window to the middle of the screen
        guiDialog.setLocationRelativeTo(null);
        guiDialog.setVisible(true);
    }

    @Override
    public void update(boolean displayConstantNames, boolean alignToGrid) {
        if (place != null) {
            getNameLabel().setName(place.name());

            if (lens == null || this.isTimed()) {
                StringBuilder buffer = new StringBuilder();
                if (!(place.invariant().upperBound() instanceof InfBound)) {
                    buffer.append("\nInv: ");
                    buffer.append(place.invariant().toString(displayConstantNames));
                }
                getNameLabel().setText(buffer.toString());
            }
            if (lens != null && this.isColored()) {
                StringBuilder buffer = new StringBuilder();
                String placeColorType = "[" + place.getColorType().getName() +"]";
                buffer.append("\n");
                buffer.append(placeColorType);

                if (isTimed()) {
                    //Add default invariant
                    buffer.append("\nInv: ");
                    buffer.append(place.invariant().toString(displayConstantNames));
                    if (place.getCtiList() != null) {
                        //Add color specific invariants
                        for (ColoredTimeInvariant cti : place.getCtiList()) {
                            if (cti != null) {
                                buffer.append("\n");
                                buffer.append(cti);
                            }
                        }
                    }
                }
                if (TAPAALGUI.getAppGui().showColoredTokens()) {
                    if (underlyingPlace().getTokensAsExpression() != null) {
                        buffer.append("\n");
                        buffer.append(((AddExpression)underlyingPlace().getTokensAsExpression()).toTokenString());
                    }
                }

                getNameLabel().setText(buffer.toString());
            }

            // Handle constant highlighting
            boolean focusedConstant = false;
            Bound bound = place.invariant().upperBound();
            if (bound instanceof ConstantBound) {
                Constant constant = ((ConstantBound) bound).constant();
                if (constant.hasFocus()) {
                    focusedConstant = true;
                }
                pnName.setVisible(constant.getVisible());
            }
            if (focusedConstant) {
                getNameLabel().setForeground(Constants.SELECTION_TEXT_COLOUR);
            } else {
                getNameLabel().setForeground(Constants.ELEMENT_TEXT_COLOUR);
            }

            getNameLabel().displayName(attributesVisible);

        } else {
            getNameLabel().setName("");
            getNameLabel().setText("");
        }
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
        if (this.place != null && listener != null) {
            this.place.removeTimedPlaceListener(listener);
        }
        place.addTimedPlaceListener(listener);
        this.place = place;
        this.update(true);
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
        return new Ellipse2D.Double(-Constants.DASHED_PADDING/2, -Constants.DASHED_PADDING/2, DIAMETER + Constants.DASHED_PADDING, DIAMETER + Constants.DASHED_PADDING);
    }

    public TimedPlaceComponent copy(TimedArcPetriNet tapn) {
        TimedPlaceComponent placeComponent = new TimedPlaceComponent(getOriginalX(), getOriginalY(), id, getNameOffsetX(), getNameOffsetY(), lens);
        placeComponent.setUnderlyingPlace(tapn.getPlaceByName(place.name()));

        return placeComponent;
    }
}