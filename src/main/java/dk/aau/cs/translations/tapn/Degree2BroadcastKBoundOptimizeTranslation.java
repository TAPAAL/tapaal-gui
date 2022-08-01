package dk.aau.cs.translations.tapn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.NTA.Edge;
import dk.aau.cs.model.NTA.Location;
import dk.aau.cs.model.NTA.NTA;
import dk.aau.cs.model.NTA.SupQuery;
import dk.aau.cs.model.NTA.TimedAutomaton;
import dk.aau.cs.model.NTA.UPPAALQuery;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class Degree2BroadcastKBoundOptimizeTranslation extends Degree2BroadcastTranslation {
	private final String usedExtraTokens = "usedExtraTokens";
	private int tokens = 0;
	private final int subtract = 0;
	private final int add = 1;

	public Degree2BroadcastKBoundOptimizeTranslation() {
		super(true);
	}

	@Override
	protected NTA transformModel(TimedArcPetriNet model) throws Exception {
		tokens = model.marking().size();
		NTA nta = super.transformModel(model);

		for (TimedAutomaton ta : nta.getTimedAutomata()) {
			if (ta.getName().equals("Token")) {
				addKBoundUpdates(ta);
			}
		}

		return nta;
	}

	private void addKBoundUpdates(TimedAutomaton ta) {
		Location pcapacity = getLocationByName(P_CAPACITY);

		for (Edge e : ta.getTransitions()) {
			if (e.getSource() == pcapacity && isNotInitializationEdge(e) && isNotTestingEdge(e)) {

				String newUpdate = createUpdate(e.getUpdate(), add);
				e.setUpdate(newUpdate);
			} else if (e.getDestination() == pcapacity && isNotTestingEdge(e)) {
				String newUpdate = createUpdate(e.getUpdate(), subtract);
				e.setUpdate(newUpdate);
			}
		}
	}

	private boolean isNotTestingEdge(Edge e) {
		Pattern pattern = Pattern.compile("^[a-zA-Z_/=][a-zA-Z0-9_/=]*_test\\?$");
		Matcher matcher = pattern.matcher(e.getSync());
		return !matcher.find();
	}

	private boolean isNotInitializationEdge(Edge e) {
		Pattern pattern = Pattern.compile("^c(?:\\d)+\\?$");
		Matcher matcher = pattern.matcher(e.getSync());
		return !matcher.find();
	}

	private String createUpdate(String update, int method) {
		String newUpdate = update;
		if (update != null && !update.isEmpty()) {
			newUpdate += ",";
		}
		newUpdate += usedExtraTokens;
		if (method == add) {
			newUpdate += "++";
		} else {
			newUpdate += "--";
		}

		return newUpdate;
	}

	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet degree2Net,TimedArcPetriNet originalModel) {
		StringBuilder builder = new StringBuilder("int[");
		builder.append(-(tokens + extraTokens));
		builder.append(',');
		builder.append(tokens + extraTokens);
		builder.append("] ");
		builder.append(usedExtraTokens);
		builder.append(" = 0;\n");
		builder.append(super.createGlobalDeclarations(degree2Net, originalModel));
		return builder.toString();
	}

	@Override
	protected UPPAALQuery transformQuery(TAPNQuery tapnQuery, TimedArcPetriNet model) throws Exception {
		return new SupQuery(usedExtraTokens);
	}

}
