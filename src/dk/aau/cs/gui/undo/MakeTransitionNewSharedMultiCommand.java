package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.Context;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.util.Require;
import pipe.dataLayer.Template;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;

import java.util.ArrayList;
import java.util.List;

public class MakeTransitionNewSharedMultiCommand extends Command {
	private final String newSharedName;
	private final Context context;
	private final Transition transition;
	private Command command;
	private final List<Command> commands = new ArrayList<Command>();


	
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
		boolean first = true;
		for(Template template : context.tabContent().allTemplates()) {
			TimedTransitionComponent component = (TimedTransitionComponent)template.guiModel().getTransitionByName(transition.getName());

            if (component != null) { //We make a new shared transition with the first transition
                if (first) {
                    command = new MakeTransitionNewSharedCommand(template.model(), newSharedName, component.underlyingTransition(), context.tabContent(), true);
                    command.redo();
                    sharedTransition = component.underlyingTransition().sharedTransition();
                    commands.add(command);
                    first = false;
                } else { //For the rest we make them shared with the recently made transition
                    command = new MakeTransitionSharedCommand(context.activeModel(), sharedTransition, component.underlyingTransition(), context.tabContent());
                    command.redo();
                    commands.add(command);
                }
            }
		}
	}

	@Override
	public void undo() {	
		for(Command command : commands)
			command.undo();
	}
}
