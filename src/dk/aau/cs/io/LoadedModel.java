package dk.aau.cs.io;

import java.util.Collection;
import java.util.List;

import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;


public class LoadedModel implements LoadedBatchProcessingModel {
	private final Collection<Template> templates;
	private final Collection<TAPNQuery> queries;
	private final TimedArcPetriNetNetwork network;
    private final boolean isTimed;
    private final boolean isGame;
    private final Collection<String> messages;
	
	public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, Collection<String> messages){
        this(network, templates, queries, messages, true, false);
    }
    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries) {
	    this(network, templates, queries, List.of());
    }

    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, Collection<String> messages, boolean isTimed, boolean isGame){
        this.templates = templates;
        this.network = network;
        this.queries = queries;
        this.isTimed = isTimed;
        this.isGame = isGame;
        this.messages = messages;
    }

	public Collection<Template> templates(){ return templates; }
	public Collection<TAPNQuery> queries(){ return queries; }
	public TimedArcPetriNetNetwork network(){ return network; }

    public Collection<String> getMessages() { return messages; }

	public boolean isTimed() {
	    return isTimed;
    }
    public boolean isGame() {
	    return isGame;
    }

}