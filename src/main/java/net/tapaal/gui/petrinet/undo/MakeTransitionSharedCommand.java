package net.tapaal.gui.petrinet.undo;

import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.MakeTransitionSharedVisitor;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import java.util.Hashtable;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

public class MakeTransitionSharedCommand implements Command {
        private final TimedArcPetriNet tapn;
	private final SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final String oldName;
        private final Hashtable<TAPNQuery, TAPNQuery> newQueryToOldQueryMapping;
        private final PetriNetTab currentTab;
	
	public MakeTransitionSharedCommand(TimedArcPetriNet tapn, SharedTransition sharedTransition, TimedTransition timedTransition, PetriNetTab currentTab){
                Require.that(tapn != null, "tapn cannot be null");
		Require.that(sharedTransition != null, "sharedTransition cannot be null");
		Require.that(timedTransition != null, "timedTransition cannot be null");
		Require.that(currentTab != null, "currentTab cannot be null");
                this.tapn = tapn;
		this.sharedTransition = sharedTransition;
		this.timedTransition = timedTransition;
                this.currentTab = currentTab;
		oldName = timedTransition.name();
                newQueryToOldQueryMapping = new Hashtable<TAPNQuery, TAPNQuery>();
	}
	
	@Override
	public void redo() {
        updateQueries(timedTransition, sharedTransition);
		sharedTransition.makeShared(timedTransition);
	}

	@Override
	public void undo() {
        undoQueryChanges(sharedTransition, timedTransition);
		timedTransition.unshare();
		timedTransition.setName(oldName);
	}
        
        private void updateQueries(TimedTransition toReplace, SharedTransition replacement) {
		MakeTransitionSharedVisitor visitor = new MakeTransitionSharedVisitor(tapn.name(), toReplace.name(), "", replacement.name());
		for(TAPNQuery query : currentTab.queries()) {
			TAPNQuery oldCopy = query.copy();
			BooleanResult isQueryAffected = new BooleanResult(false);
			query.getProperty().accept(visitor, isQueryAffected);
			
			if(isQueryAffected.result()) {
                newQueryToOldQueryMapping.put(query, oldCopy);
            }
		}
	}
		
	private void undoQueryChanges(SharedTransition toReplace, TimedTransition replacement) {
		for(TAPNQuery query : currentTab.queries()) {
			if(newQueryToOldQueryMapping.containsKey(query))
				query.set(newQueryToOldQueryMapping.get(query));
		}
		
		newQueryToOldQueryMapping.clear();
	}

}
