package dk.aau.cs.io;

import java.util.Collection;

import pipe.gui.PetriNetTab;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import net.tapaal.gui.verification.TAPNQuery;
import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedModel implements LoadedBatchProcessingModel {

    private final Collection<Template> templates;
	private final Collection<TAPNQuery> queries;
	private final TimedArcPetriNetNetwork network;
    private final Collection<String> messages;
    private final PetriNetTab.TAPNLens lens;

    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, Collection<String> messages, PetriNetTab.TAPNLens lens){
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

    public PetriNetTab.TAPNLens getLens(){
        if (lens != null) {
            return lens;
        } else {
            boolean isNetTimed = !network().isUntimed();
            boolean isNetGame = network().hasUncontrollableTransitions();
            boolean isNetColored = network.isColored();

            return new PetriNetTab.TAPNLens(isNetTimed, isNetGame, isNetColored);
        }
    }
    public boolean isColored(){
	    return lens.isColored();
    }
}