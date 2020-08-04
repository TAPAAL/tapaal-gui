package dk.aau.cs.io;

import java.util.Collection;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedModel{

    private final Collection<Template> templates;
	private final Collection<TAPNQuery> queries;
	private final TimedArcPetriNetNetwork network;
    private final boolean isTimed;
    private final boolean isGame;

	public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries){
		this(network, templates, queries, true, false);
	}

    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, boolean isTimed, boolean isGame){
        this.templates = templates;
        this.network = network;
        this.queries = queries;
        this.isTimed = isTimed;
        this.isGame = isGame;
    }

	public Collection<Template> templates(){ return templates; }
	public Collection<TAPNQuery> queries(){ return queries; }
	public TimedArcPetriNetNetwork network(){ return network; }
	public boolean isTimed() {
	    return isTimed;
    }
    public boolean isGame() {
	    return isGame;
    }
}