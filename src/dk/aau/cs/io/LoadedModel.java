package dk.aau.cs.io;

import java.util.Collection;
import java.util.List;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedModel implements LoadedBatchProcessingModel {

    private final Collection<Template> templates;
	private final Collection<TAPNQuery> queries;
	private final TimedArcPetriNetNetwork network;
    private final Collection<String> messages;
    private final TabContent.TAPNLens lens;

    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, Collection<String> messages, TabContent.TAPNLens lens){
        this.templates = templates;
        this.network = network;
        this.queries = queries;
        this.lens = lens;
        this.messages = messages;
    }

	public Collection<Template> templates(){ return templates; }
	public Collection<TAPNQuery> queries(){ return queries; }
	public TimedArcPetriNetNetwork network(){ return network; }
    public Collection<String> getMessages() { return messages; }

    public TabContent.TAPNLens getLens(){
        if (lens != null) {
            return lens;
        } else {
            boolean isNetTimed = !network().isUntimed();
            boolean isNetGame = network().hasUncontrollableTransitions();

            return new TabContent.TAPNLens(isNetTimed, isNetGame);
        }
    }

}