/**
 * 
 */
package dk.aau.cs.io.batchProcessing;

import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedBatchProcessingModel{
	private Collection<TAPNQuery> queries;
	private TimedArcPetriNetNetwork network; 
	
	public LoadedBatchProcessingModel(TimedArcPetriNetNetwork network, Collection<TAPNQuery> queries){
		this.network = network;
		this.queries = queries; 
	}

	public Collection<TAPNQuery> queries(){ return queries; }
	public TimedArcPetriNetNetwork network(){ return network; }
}