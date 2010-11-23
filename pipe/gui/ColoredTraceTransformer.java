package pipe.gui;

import java.math.BigDecimal;
import java.util.List;

import pipe.dataLayer.ColoredDiscreteFiringAction;
import pipe.dataLayer.DataLayer;
import dk.aau.cs.petrinet.colors.ColoredToken;

public class ColoredTraceTransformer extends TraceTransformer {

	public ColoredTraceTransformer(DataLayer model) {
		super(model);
	}

	@Override
	protected pipe.dataLayer.FiringAction transformDiscreteTransition(dk.aau.cs.petrinet.trace.TAPNFiringAction action, boolean isConcreteTrace) {
		pipe.dataLayer.ColoredDiscreteFiringAction firingAction = null;
		if(action instanceof dk.aau.cs.petrinet.trace.ColoredTransitionFiringAction){
			dk.aau.cs.petrinet.trace.ColoredTransitionFiringAction transitionFiringAction = (dk.aau.cs.petrinet.trace.ColoredTransitionFiringAction)action;

			pipe.dataLayer.TimedTransitionComponent transition = (pipe.dataLayer.TimedTransitionComponent)model().getTransitionByName(transitionFiringAction.transition());
			firingAction = new pipe.dataLayer.ColoredDiscreteFiringAction(transition);

			if(isConcreteTrace){
				convertAndAddConsumedTokens(firingAction, transitionFiringAction.consumedTokens());
				convertAndAddProducedTokens(firingAction, transitionFiringAction.producedTokens());
			}
		}

		return firingAction;
	}

	private void convertAndAddProducedTokens(
			ColoredDiscreteFiringAction firingAction,
			List<ColoredToken> producedTokens) {
		for(dk.aau.cs.petrinet.colors.ColoredToken aauToken : producedTokens){
			pipe.dataLayer.colors.ColoredTimedPlace place = (pipe.dataLayer.colors.ColoredTimedPlace)model().getPlaceByName(aauToken.place().getName());
			BigDecimal age = aauToken.age();
			int color = aauToken.getColor();
			pipe.dataLayer.colors.ColoredToken pipeToken = new pipe.dataLayer.colors.ColoredToken(age, color);
			firingAction.addProducedToken(place, pipeToken);
		}

	}

	private void convertAndAddConsumedTokens(pipe.dataLayer.ColoredDiscreteFiringAction firingAction, List<dk.aau.cs.petrinet.colors.ColoredToken> consumedTokens) {
		for(dk.aau.cs.petrinet.colors.ColoredToken aauToken : consumedTokens){
			pipe.dataLayer.colors.ColoredTimedPlace place = (pipe.dataLayer.colors.ColoredTimedPlace)model().getPlaceByName(aauToken.place().getName());
			BigDecimal age = aauToken.age();
			int color = aauToken.getColor();
			pipe.dataLayer.colors.ColoredToken pipeToken = new pipe.dataLayer.colors.ColoredToken(age, color);
			firingAction.addConsumedToken(place, pipeToken);
		}
	}
}
