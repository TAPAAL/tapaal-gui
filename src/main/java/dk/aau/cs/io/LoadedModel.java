package dk.aau.cs.io;

import java.util.Collection;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class LoadedModel implements LoadedBatchProcessingModel {

    private final Collection<Template> templates;
	private final Collection<TAPNQuery> queries;
	private final TimedArcPetriNetNetwork network;
    private final Collection<String> messages;
    private final TAPNLens lens;

    public LoadedModel(TimedArcPetriNetNetwork network, Collection<Template> templates, Collection<TAPNQuery> queries, Collection<String> messages, TAPNLens lens){
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

    public TAPNLens getLens(){
        if (lens != null) {
            return lens;
        } else {
            boolean isNetTimed = !network().isUntimed();
            boolean isNetGame = network().hasUncontrollableTransitions();
            boolean isNetColored = network().isColored();
            boolean isNetStochastic = network().isStochastic();

            return new TAPNLens(isNetTimed, isNetGame, isNetColored, isNetStochastic);
        }
    }
    public boolean isColored(){
	    return lens.isColored();
    }
}