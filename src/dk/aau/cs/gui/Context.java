package dk.aau.cs.gui;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class Context { // TODO: consider having only currentTab as a field and have methods ask it for the things?
	private final TimedArcPetriNetNetwork network;
	private final Template selectedTemplate;
	
	private final UndoManager undoManager;
	private final TabContent currentTab;
	
	public Context(TabContent tab) {
		Require.that(tab != null, "tab cannot be null");
		currentTab = tab;
		network = tab.network();
		selectedTemplate = tab.currentTemplate();
		undoManager = tab.drawingSurface().getUndoManager();
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

	public TabContent tabContent() {
		return currentTab;
	}

	public NameGenerator nameGenerator() {
		return currentTab.drawingSurface().getNameGenerator();
	}
}
