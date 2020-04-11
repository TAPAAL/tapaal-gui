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
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.TabContent;
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
		this.newSharedName = newSharedName;
	}
	
	@Override
	public void redo() {
		SharedTransition sharedTransition = null;
		int i = 0;
		for(Template template : context.tabContent().allTemplates()) {
			TimedTransitionComponent component = (TimedTransitionComponent)template.guiModel().getTransitionByName(transition.getName());
			//We make a new shared transition with the first transition
			if(component != null && i < 1) {
				command = new MakeTransitionNewSharedCommand(template.model(), newSharedName, component.underlyingTransition(), context.tabContent(), true);
				command.redo();
				sharedTransition = component.underlyingTransition().sharedTransition();
				commands.add(command);
				i++;
				//For the rest we make them shared with the recently made transition
			} else if (component != null && i >= 1){
				command = new MakeTransitionSharedCommand(context.activeModel(), sharedTransition, component.underlyingTransition(), context.tabContent());
				command.redo();
				commands.add(command);
			}
		}
	}

	@Override
	public void undo() {	
		for(Command command : commands)
			command.undo();
	}
}
