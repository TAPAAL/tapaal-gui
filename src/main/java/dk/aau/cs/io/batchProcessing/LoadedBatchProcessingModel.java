package dk.aau.cs.io.batchProcessing;

import java.util.Collection;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public interface LoadedBatchProcessingModel{

	Collection<TAPNQuery> queries();
	TimedArcPetriNetNetwork network();
}