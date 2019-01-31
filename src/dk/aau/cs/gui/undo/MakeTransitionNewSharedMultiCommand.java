package dk.aau.cs.gui.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.undo.UndoManager;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.MakePlaceSharedVisitor;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Require;

public class MakeTransitionNewSharedMultiCommand extends Command {
	private final String newSharedName;
	private final List<TimedArcPetriNet> tapns;
	private Hashtable<TAPNQuery, TAPNQuery> newQueryToOldQueryMapping;
	//private final List<TimedToken> oldTokens;
	private final TabContent currentTab;
	private SharedPlacesAndTransitionsPanel sharedPanel;
	private ArrayList<SharedPlace> sharedPlaces;
	private HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private String originalName;
	private ArrayList<TimedPlace> timedPlaces = new ArrayList<TimedPlace>();
	private NameGenerator nameGenerator;
	private pipe.gui.undo.UndoManager undoManager;
	private Context context;
	private Transition transition;
	private Command command;
	private List<Command> commands = new ArrayList<Command>();


	
	public MakeTransitionNewSharedMultiCommand(dk.aau.cs.gui.Context context, String newSharedName, TimedTransitionComponent transition){
		Require.that(context.network().allTemplates() != null, "tapns cannot be null");
		Require.that(newSharedName != null, "newSharedName cannot be null");
		Require.that(context.tabContent() != null, "currentTab cannot be null");
		
		this.transition = transition;
		this.context = context;
		this.tapns = context.network().allTemplates();
		this.newSharedName = newSharedName;
		this.currentTab = context.tabContent();
		this.sharedPanel = currentTab.getSharedPlacesAndTransitionsPanel();
		guiModels = context.tabContent().getGuiModels();
		undoManager = currentTab.drawingSurface().getUndoManager();
		//oldTokens = place.tokens();
		newQueryToOldQueryMapping = new Hashtable<TAPNQuery, TAPNQuery>();
	}
	
	@Override
	public void redo() {
		for(Template template : context.tabContent().allTemplates()) {
			TimedTransitionComponent component = (TimedTransitionComponent)template.guiModel().getTransitionByName(transition.getName());
			if(component != null) {
				command = new MakeTransitionNewSharedCommand(template.model(), newSharedName, component.underlyingTransition(), context.tabContent(), true);
				command.redo();
				commands.add(command);
			}
			//undoManager.addNewEdit(command);
		}
		/*for(TimedArcPetriNet tapn : tapns) {
			for(Place placeComponent : guiModels.get(tapn).getPlaces()) {
				if(placeComponent.getName().equals(originalName)) {
					TimedPlace place = ((TimedPlaceComponent)placeComponent).underlyingPlace();
					timedPlaces.add(place);
					tapn.remove(place);
					
					SharedPlace sharedPlace = new SharedPlace(newSharedName);
					sharedPlaces.add(sharedPlace);
					
					sharedPanel.addSharedPlace(sharedPlace, true);
					updateArcs(place, sharedPlace, tapn);
					tapn.add(sharedPlace, true);
					((TimedPlaceComponent)placeComponent).setUnderlyingPlace(sharedPlace);
					
					updateQueries(place, sharedPlace, tapn);
				}
			}
		}*/
	}

	@Override
	public void undo() {	
		for(Command command : commands)
			command.undo();
		/*for(Template template : context.tabContent().allTemplates()) {
			System.out.println("Hej " + template.toString());
			undoManager.undo();
		}*/
		/*Hashtable<LocalTimedPlace, String> createdPlaces = new Hashtable<LocalTimedPlace, String>();
		for(Template template : currentTab.allTemplates()) {
			TimedPlace place = template.model().getPlaceByName(newSharedName);
			TimedPlaceComponent component = (TimedPlaceComponent) template.guiModel().getPlaceByName(newSharedName);
			if(place != null) {
				String name = nameGenerator.getNewPlaceName(template.model());
				LocalTimedPlace localPlace = new LocalTimedPlace(name);
				createdPlaces.put(localPlace, name);
				Command command = new MakePlaceNewSharedCommand(template.model(), originalName, localPlace, component, currentTab);
				command.undo();
				undoManager.addEdit(command);
				
			}
			
			sharedPanel.removeSharedPlace((SharedPlace)place);
			for(Entry<LocalTimedPlace, String> entry : createdPlaces.entrySet()){
				Command renameCmd = new RenameTimedPlaceCommand(currentTab, entry.getKey(), entry.getValue(), place.name());
				renameCmd.redo();
				undoManager.addEdit(renameCmd);
			}
		}*/
		/*SharedPlace sharedPlace = sharedPlaces.get(0);
		int i = 0;
		for(TimedArcPetriNet tapn : tapns) {
			for(Place placeComponent : guiModels.get(tapn).getPlaces()) {
				if(sharedPlaces.contains(((TimedPlaceComponent)placeComponent).underlyingPlace())) {
					TimedPlace place = timedPlaces.get(i);
					System.out.println("timed Place " + place.isShared());

					
					sharedPlace = sharedPlaces.get(i);
					System.out.println("Shared Place " + sharedPlace.isShared());

					updateArcs(sharedPlace, place, tapn);
					tapn.remove(sharedPlace);
					if(sharedPlace != null && i == 0){
						System.out.println("Jeg kommer her ind");
						sharedPanel.removeSharedPlace(sharedPlace);
					}
					tapn.add(place, true);
					//place.addTokens(oldTokens);
					((TimedPlaceComponent)placeComponent).setUnderlyingPlace(place);
					System.out.println(((TimedPlaceComponent)placeComponent).underlyingPlace() + " underlying place " + ((TimedPlaceComponent)placeComponent).underlyingPlace().isShared());
					System.out.println(tapn.places().contains(sharedPlace));
					
					undoQueryChanges(sharedPlace, place);
					i++;
				}
			}
		}*/
	}
}
