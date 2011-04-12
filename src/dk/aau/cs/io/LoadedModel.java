/**
 * 
 */
package dk.aau.cs.io;

import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedModel{
	private Collection<Template> templates;
	private Collection<TAPNQuery> queries;
	private TimedArcPetriNetNetwork network; 
	
	public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries){
		this.templates = templates;
		this.network = network;
		this.queries = queries; 
	}

	public Collection<Template> templates(){ return templates; }
	public Collection<TAPNQuery> queries(){ return queries; }
	public TimedArcPetriNetNetwork network(){ return network; }
}