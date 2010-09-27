package pipe.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pipe.dataLayer.TAPNTrace;

import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UppaalTrace;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.verification.FiringAction;

public class TraceTransformer {
	public TAPNTrace interpretTrace(UppaalTrace trace, TranslationNamingScheme namingScheme){
		Iterator<FiringAction> iterator = trace.iterator();
		FiringAction action;

		List<String> firingSequence = new ArrayList<String>();
		while(iterator.hasNext()){
			while(iterator.hasNext() && 
					(action = iterator.next()) instanceof TransitionFiringAction){
				firingSequence.add(((TransitionFiringAction)action).channel());
			}
			
			String[] transitions = namingScheme.interpretTransitionSequence(firingSequence);
			
		}

		return null;
	}
}
