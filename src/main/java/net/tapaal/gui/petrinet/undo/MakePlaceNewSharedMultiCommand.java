package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.Context;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.util.Require;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import java.util.ArrayList;
import java.util.List;

public class MakePlaceNewSharedMultiCommand implements Command {

		private final String newSharedName;
		private final Context context;
		private final Place place;
		private Command command;
		private final List<Command> commands = new ArrayList<Command>();


		
		public MakePlaceNewSharedMultiCommand(Context context, String newSharedName, TimedPlaceComponent place){
			Require.that(context.network().allTemplates() != null, "tapns cannot be null");
			Require.that(newSharedName != null, "newSharedName cannot be null");
			Require.that(context.tabContent() != null, "currentTab cannot be null");
			
			this.place = place;
			this.context = context;
			this.newSharedName = newSharedName;
		}
		
		@Override
		public void redo() {
            commands.clear();

			SharedPlace sharedPlace = null;
			boolean first = true;
			for(Template template : context.tabContent().allTemplates()) {
				TimedPlaceComponent component = (TimedPlaceComponent)template.guiModel().getPlaceByName(place.getName());

                if (component != null) {
                    if (first) { //We make a new shared place with the first place
                        command = new MakePlaceNewSharedCommand(
                            template.model(),
                            newSharedName,
                            component.underlyingPlace(),
                            component,
                            context.tabContent(),
                            true
                        );
                        command.redo();
                        sharedPlace = (SharedPlace) component.underlyingPlace();
                        commands.add(command);
                        first = false;
                    } else { //For the rest we make them shared with the recently made place
                        command = new MakePlaceSharedCommand(template.model(), sharedPlace, component.underlyingPlace(), component, context.tabContent(), true);
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

