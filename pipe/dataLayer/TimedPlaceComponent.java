package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JTextArea;

import pipe.dataLayer.simulation.Marking;
import pipe.dataLayer.simulation.Token;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.PlaceMarkingEdit;
import pipe.gui.undo.TimedPlaceInvariantEdit;
import pipe.gui.undo.TimedPlaceTokenEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PlaceEditorPanel;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class TimedPlaceComponent extends Place {
	private static final long serialVersionUID = 1L;

	private dk.aau.cs.model.tapn.TimedPlace place;
	private ArrayList<BigDecimal> myTokens;
	private Window ageOfTokensWindow;


	public TimedPlaceComponent(double positionXInput, double positionYInput, dk.aau.cs.model.tapn.TimedPlace place) {
		super(positionXInput, positionYInput);
		this.place = place;
		attributesVisible = true;
		this.myTokens = new ArrayList<BigDecimal>();
		ageOfTokensWindow = new Window(new Frame());
	}

	public TimedPlaceComponent(double positionXInput,  double positionYInput, 
			String idInput, 
			String nameInput, 
			Double nameOffsetXInput, Double nameOffsetYInput, 
			int initialMarkingInput, 
			double markingOffsetXInput,  double markingOffsetYInput,
			int capacityInput, String invariant){

		super( positionXInput, positionYInput, idInput, nameInput, nameOffsetXInput,  nameOffsetYInput, 
				initialMarkingInput, 
				markingOffsetXInput,   markingOffsetYInput,
				capacityInput);
		
		// XXX  - Hack to get shown attributes 
		attributesVisible = true;

				this.myTokens = new ArrayList<BigDecimal>();
		for (int i=0; i<initialMarkingInput; i++){
			this.myTokens.add(newToken());
		}
		ageOfTokensWindow = new Window(new Frame());
	}

	public TimedPlaceComponent(Place place, String invariant){		
		super(place.getX(), place.getY(), 
				place.id, 
				place.getName(), 
				place.nameOffsetX, place.nameOffsetY, 
				place.getInitialMarking(), 
				place.getMarkingOffsetXObject(),  place.getMarkingOffsetYObject(),
				place.capacity);
		attributesVisible = true;

		this.myTokens = new ArrayList<BigDecimal>();
		for (int i=0; i<place.getInitialMarking(); i++){
			this.myTokens.add(newToken());
		}
		ageOfTokensWindow = new Window(new Frame());
	}

	public TimedPlaceComponent(String idInput, 
			String nameInput, 
			int initialMarkingInput, 
			int capacityInput, String invariant){


		super(0.0, 0.0, idInput, nameInput, 0.0,  0.0, 
				initialMarkingInput, 
				0.0,   0.0,
				capacityInput);

		

		this.myTokens = new ArrayList<BigDecimal>();
		for (int i=0; i<initialMarkingInput; i++){
			this.myTokens.add(newToken());
		}
		ageOfTokensWindow = new Window(new Frame());
	}



	@Override
	public TimedPlaceComponent clone(){
		TimedPlaceComponent toReturn = (TimedPlaceComponent)super.clone();

		toReturn.setInvariantFromString(this.getInvariantAsString());
		return toReturn;

	}


	@Override
	public TimedPlaceComponent copy(){
		//TimedPlace copy = new TimedPlace(super.copy(), this.invariant);
		//		copy.setOriginal(this);

		TimedPlaceComponent copy = new TimedPlaceComponent (Zoomer.getUnzoomedValue(this.getX(), zoom), 
				Zoomer.getUnzoomedValue(this.getY(), zoom), this.place);
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
		if(place != null) place.delete();
		super.delete();
	}
	public String getInvariantAsString(){

		return place.invariant().toString();
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
		for(TimedToken token : tokens){
			if(!first) buffer.append(", ");
			buffer.append(df.format(token.age()));
			first = false;
		}
		buffer.append("}");

		return buffer.toString();
	}

	public ArrayList<BigDecimal> getTokens(){
		return myTokens;
	}


	public boolean isAgeOfTokensShown(){
		return ageOfTokensWindow.isVisible();
	}

	private BigDecimal newToken() {

		return new BigDecimal(0, new MathContext(Pipe.AGE_PRECISION));
	}

	//overide method
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;

			if (hasCapacity()){
				g2.setStroke(new BasicStroke(2.0f));
			} else {
				g2.setStroke(new BasicStroke(1.0f));
			}
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);

			if(selected && !ignoreSelection){
				g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			} else{
				g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			}
			g2.fill(placeEllipse);

			if (selected && !ignoreSelection){
				g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
			} else{
				g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
			}
			g2.draw(placeEllipse);

			g2.setStroke(new BasicStroke(1.0f));

			paintTokens(g);
	}

	protected void paintTokens(Graphics g) {
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(1);
		df.setRoundingMode(RoundingMode.DOWN);

		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;

		//int marking = getCurrentMarking();
		List<TimedToken> myTokens = place.tokens();
		int marking = place.numberOfTokens();
		
		// structure sees how many markings there are and fills the place in with 
		// the appropriate number.
		switch(marking) {
		case 2: 
			if (myTokens.get(1).age().compareTo(BigDecimal.valueOf(9)) > 0){
				g.setFont(new Font("new font", Font.PLAIN, 11));
				g.drawString(df.format(myTokens.get(1).age()), x + 17-12, y + 13+1);
			}else{
				g.drawString(df.format(myTokens.get(1).age()), x + 17-10, y + 13+1);
			}
			//			g.fillOval(x + 18, y + 6, tWidth, tHeight);
			/* falls through */
		case 1:
			if (myTokens.get(0).age().compareTo(BigDecimal.valueOf(9)) > 0){
				g.setFont(new Font("new font", Font.PLAIN, 11));
				g.drawString(df.format(myTokens.get(0).age()), x + 11-6, y + 20+6);
			}else{
				g.drawString(df.format(myTokens.get(0).age()), x + 11-4, y + 20+6);
			}
			//			g.fillOval(x + 12, y + 13, tWidth, tHeight);
			break;
		case 0:
			break;
		default:
			if (marking > 999){
				//XXX could be better...
				g.drawString("#"+String.valueOf(marking), x, y + 20);
			} else if (marking > 99){
				g.drawString("#"+String.valueOf(marking), x, y + 20);
			} else if (marking > 9){
				g.drawString("#"+String.valueOf(marking), x + 2, y + 20);
			} else {
				g.drawString("#"+String.valueOf(marking), x + 6, y + 20);
			}
			break;
		}
	}

	
	public void removeTokenofAge(BigDecimal tokenage) {
		boolean ableToRemoveToken = false;
		BigDecimal tokenToRemove = null;
		for (BigDecimal bd : myTokens){
			if (tokenage.compareTo(bd) == 0){
				ableToRemoveToken = true;
				tokenToRemove = bd;
				break;
			}
		}
		if (ableToRemoveToken){
			myTokens.remove(tokenToRemove);
			Collections.sort(myTokens);
			myTokens.trimToSize();
			currentMarking--;	
		}else {
			System.err.println(getName() + " has no token with that age");
		}
	}

	public boolean satisfiesInvariant(BigDecimal token) {
		return place.invariant().isSatisfied(token);
	}

	public Command setAgeOfTokens(ArrayList<BigDecimal> newAgeOfTokens) {

		if (newAgeOfTokens.size() == myTokens.size()){
			ArrayList<BigDecimal> oldAgeOfTokens = this.myTokens;
			this.myTokens = newAgeOfTokens;

			update(true);

			return new TimedPlaceTokenEdit(this, oldAgeOfTokens, this.myTokens);
		} else throw new IllegalArgumentException("the argument size does not match the number of tokens in this place");
	}

	//overide, so that we can take care of the age of the tokens
	@Override
	public Command setCurrentMarking(int currentMarkingInput) {
		int oldMarking = currentMarking;
		if (capacity == 0){
			setNumberOfMyTokens(currentMarkingInput);
			currentMarking = currentMarkingInput;
		} else {
			if (currentMarkingInput > capacity) {
				currentMarking = capacity;
			} else{
				setNumberOfMyTokens(currentMarkingInput);
				currentMarking = currentMarkingInput;
			}
		}
		repaint();
		return new PlaceMarkingEdit(this, oldMarking, currentMarking);      
	}

	public Command setInvariantFromString(String invariant) {
		TimeInvariant old = place.invariant();
		TimeInvariant newInv = TimeInvariant.parse(invariant);
		place.setInvariant(newInv);

		update(true);

		return new TimedPlaceInvariantEdit(this, old, newInv);
	}
	
	public Command setInvariant(TimeInvariant inv){
		TimeInvariant old = place.invariant();
		place.setInvariant(inv);
		
		update(true);
		
		return new TimedPlaceInvariantEdit(this, old, inv);
	}

	private void setNumberOfMyTokens(int currentMarkingInput){

		int toAddToMyTokens = currentMarkingInput - currentMarking;
		if (toAddToMyTokens >= 0 ){
			for (int i=0; i<toAddToMyTokens; i++){
				myTokens.add(newToken());
			}
		}else {
			int size = myTokens.size();
			while (size > currentMarkingInput){
				myTokens.trimToSize();
				myTokens.remove(myTokens.size()-1);
				myTokens.trimToSize();
				size = myTokens.size();
			}
		}
	}

	public Command setTokensAndAgeOfTokens(ArrayList<BigDecimal> newAgeOfTokens) {

		//if (newAgeOfTokens.size() == myTokens.size()){
		this.setCurrentMarking(newAgeOfTokens.size());
		ArrayList<BigDecimal> oldAgeOfTokens = this.myTokens;
		this.myTokens = newAgeOfTokens;

		update(true);

		return new TimedPlaceTokenEdit(this, oldAgeOfTokens, this.myTokens);
	}



	public void showAgeOfTokens(boolean show) {
		if(ageOfTokensWindow != null) ageOfTokensWindow.dispose();
		// Build interface
		if(show){
			ageOfTokensWindow = new Window(new Frame());
			ageOfTokensWindow.add(new JTextArea(getStringOfTokens()));
			ageOfTokensWindow.getComponent(0).setBackground(Color.lightGray);

			// Make window fit contents' preferred size
			ageOfTokensWindow.pack();

			// Move window to the middle of the screen
			ageOfTokensWindow.setLocationRelativeTo(this);
			if (myTokens.size()<=12){
				ageOfTokensWindow.setLocation(ageOfTokensWindow.getLocation().x, ageOfTokensWindow.getLocation().y+31+myTokens.size());
			}else if (myTokens.size()>12 && myTokens.size()<20){
				ageOfTokensWindow.setLocation(ageOfTokensWindow.getLocation().x, ageOfTokensWindow.getLocation().y+40+myTokens.size());
			}else if (myTokens.size()>=20 && myTokens.size()<=35){
				ageOfTokensWindow.setLocation(ageOfTokensWindow.getLocation().x, ageOfTokensWindow.getLocation().y+35+myTokens.size());
			}else if (myTokens.size()>35 &&myTokens.size()<40){
				ageOfTokensWindow.setLocation(ageOfTokensWindow.getLocation().x, ageOfTokensWindow.getLocation().y+40+myTokens.size());	
			}else{
				ageOfTokensWindow.setLocation(ageOfTokensWindow.getLocation().x, ageOfTokensWindow.getLocation().y+20+myTokens.size());
			}
			ageOfTokensWindow.setVisible(show);	
		}
	}

	@Override
	public void showEditor(){
		// Build interface
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new PlaceEditorPanel(guiDialog.getRootPane(), 
				this, CreateGui.getModel(), CreateGui.getView()));

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	@Override
	public void update(boolean displayConstantNames) {
		if (attributesVisible == true){
			//String value = getInvariantString();
			pnName.setText("\nInv: " + place.invariant().toString(displayConstantNames));

		} else {
			pnName.setText("");
		}          
		pnName.zoomUpdate(zoom);

		updateBounds();
		updateLabelLocation();
		updateConnected();

		repaint();
	}

	protected String getInvariantString() {
		String value = "";
		String invariant = getInvariantAsString();
		//Dont show invariant if its default	
		if (!invariant.equals(TimeInvariant.LESS_THAN_INFINITY.toString())){ 

			if(CreateGui.getModel().isUsingColors()){
				int offset = 1;
				if(invariant.contains("<=")) offset = 2;

				value = String.format("\n age \u2208 [0, %1$s%2$s", invariant.substring(offset).trim(),
						offset == 2 ? "]" : ")");
			}else{
				value = "\nInv: " + invariant;
			}
		}

		return value;
	}

	@Override
	public void showMarking(Marking marking) {
		myTokens.clear();
		for(Token token : marking.getTokensInPlace(this)){
			myTokens.add(token.age());
		}
		super.showMarking(marking);
	}

	@Override
	public String toString() {
		return getName();
	}

	public TimedPlace underlyingPlace() {
		return place;
	}

	
	public void setUnderlyingPlace(TimedPlace place) {
		this.place = place;
		this.setName(place.name());
		this.repaint();
	}

	public void addTokens(int numberOfTokensToAdd) {
		for(int i = 0; i < numberOfTokensToAdd; i++){
			place.addToken(new TimedToken(place, BigDecimal.ZERO));
		}
	}

	public void removeTokens(int numberOfTokensToRemove) {
		for(int i = 0; i < numberOfTokensToRemove; i++){
			place.removeToken();
		}
	}
	
	public int getNumberOfTokens(){
		return place.numberOfTokens(); // TODO: This may fail if saving while in animation mode?
	}
	
	@Override
	public void setName(String nameInput) {
		place.setName(nameInput);
		super.setName(nameInput);
	}
}
