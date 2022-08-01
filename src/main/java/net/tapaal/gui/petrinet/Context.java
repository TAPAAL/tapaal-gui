package net.tapaal.gui.petrinet;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.undo.UndoManager;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class Context {
	private final TimedArcPetriNetNetwork network;
	private final Template selectedTemplate;
	
	private final UndoManager undoManager;
    //XXX Temp solution while refactoring
	public final PetriNetTab currentTab;
	
	public Context(PetriNetTab tab) {
		Require.that(tab != null, "tab cannot be null");
		currentTab = tab;
		network = tab.network();
		selectedTemplate = tab.currentTemplate();
		undoManager = tab.getUndoManager();
	}
	
	public TimedArcPetriNetNetwork network(){
		return network;
	}
	
	public TimedArcPetriNet activeModel(){
		return selectedTemplate.model();
	}
	
	public UndoManager undoManager(){
		return undoManager;
	}

	public Iterable<TAPNQuery> queries() {
		return currentTab.queries();
	}

	public PetriNetTab tabContent() {
		return currentTab;
	}

	public NameGenerator nameGenerator() {
		return currentTab.getNameGenerator();
	}
}
