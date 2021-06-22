package dk.aau.cs.gui.undo;

import dk.aau.cs.model.CPN.Expressions.*;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;

import java.util.Vector;

// TODO: Fix this to work on the model class instead of the GUI class
//TODO: add colors to this
public class TimedPlaceMarkingEdit extends Command {
	private final int numberOfTokens;
	private final TimedPlaceComponent timedPlaceComponent;

	public TimedPlaceMarkingEdit(TimedPlaceComponent tpc, int numberOfTokens) {
		timedPlaceComponent = tpc;
		this.numberOfTokens = numberOfTokens;
	}

	@Override
	public void redo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.underlyingPlace().addTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.underlyingPlace().removeTokens(Math.abs(numberOfTokens));
		}
        //This is needed to keep the colored token expression consistent
        if(timedPlaceComponent.underlyingPlace().numberOfTokens() > 0){
            Vector<ColorExpression> v = new Vector<>();
            v.add(new DotConstantExpression());
            Vector<ArcExpression> numbOfExpression = new Vector<>();
            numbOfExpression.add(new NumberOfExpression(timedPlaceComponent.underlyingPlace().numberOfTokens(), v));
            timedPlaceComponent.underlyingPlace().setTokenExpression(new AddExpression(numbOfExpression));
        }
		timedPlaceComponent.repaint();
	}

	@Override
	public void undo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.underlyingPlace().removeTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.underlyingPlace().addTokens(Math.abs(numberOfTokens));
		}
		//This is needed to keep the colored token expression consistent
        if(timedPlaceComponent.underlyingPlace().numberOfTokens() > 0){
            Vector<ColorExpression> v = new Vector<>();
            v.add(new DotConstantExpression());
            Vector<ArcExpression> numbOfExpression = new Vector<>();
            numbOfExpression.add(new NumberOfExpression(timedPlaceComponent.underlyingPlace().numberOfTokens(), v));
            timedPlaceComponent.underlyingPlace().setTokenExpression(new AddExpression(numbOfExpression));
        }
		timedPlaceComponent.repaint();
	}

}
