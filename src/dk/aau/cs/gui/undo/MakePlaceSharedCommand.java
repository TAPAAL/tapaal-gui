package dk.aau.cs.gui.undo;

import java.util.List;

import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class MakePlaceSharedCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace timedPlace;
	
	private final String oldName;
	private final TimeInvariant oldInvariant;
	private final List<TimedToken> oldTokens;
	
	public MakePlaceSharedCommand(SharedPlace sharedPlace, TimedPlace timedPlace){
		this.sharedPlace = sharedPlace;
		this.timedPlace = timedPlace;
		this.oldName = timedPlace.name();
		this.oldInvariant = timedPlace.invariant();
		this.oldTokens = timedPlace.tokens();
	}
	
	@Override
	public void redo() {
		sharedPlace.makeShared(timedPlace);
	}

	@Override
	public void undo() {
		timedPlace.unshare();
		timedPlace.setName(oldName);
		timedPlace.setInvariant(oldInvariant);
		timedPlace.addTokens(oldTokens);
	}
}
