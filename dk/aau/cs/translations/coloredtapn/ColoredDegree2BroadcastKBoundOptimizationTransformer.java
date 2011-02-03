package dk.aau.cs.translations.coloredtapn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.SupQuery;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class ColoredDegree2BroadcastKBoundOptimizationTransformer extends
		ColoredDegree2BroadcastTranslation {

	private final String usedExtraTokens = "usedExtraTokens";
	private int tokens = 0;
	private final int SUBTRACT = 0;
	private final int ADD = 1;

	public ColoredDegree2BroadcastKBoundOptimizationTransformer(int extraTokens) {
		super(extraTokens, true);
	}

	@Override
	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		tokens = model.getTokens().size();
		NTA nta = super.transformModel(model);

		for (TimedAutomaton ta : nta.getTimedAutomata()) {
			if (ta.getName().equals("Token")) {
				addKBoundUpdates(ta);
			}
		}

		return nta;
	}

	private void addKBoundUpdates(TimedAutomaton ta) {
		Location pcapacity = getLocationByName("P_capacity");

		for (Edge e : ta.getTransitions()) {
			if (e.getSource() == pcapacity && isNotInitializationEdge(e)
					&& isNotTestingEdge(e)) {

				String newUpdate = createUpdate(e.getUpdate(), ADD);
				e.setUpdate(newUpdate);
			} else if (e.getDestination() == pcapacity && isNotTestingEdge(e)) {
				String newUpdate = createUpdate(e.getUpdate(), SUBTRACT);
				e.setUpdate(newUpdate);
			}
		}
	}

	private boolean isNotTestingEdge(Edge e) {
		Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*_test\\?$");
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
		if (method == ADD) {
			newUpdate += "++";
		} else {
			newUpdate += "--";
		}

		return newUpdate;
	}

	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet degree2Net,
			TimedArcPetriNet originalModel) {
		StringBuilder builder = new StringBuilder("int[");
		builder.append(-(tokens + extraTokens));
		builder.append(",");
		builder.append(tokens + extraTokens);
		builder.append("] ");
		builder.append(usedExtraTokens);
		builder.append(" = 0;\n");
		builder.append(super
				.createGlobalDeclarations(degree2Net, originalModel));
		return builder.toString();
	}

	@Override
	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		return new SupQuery(usedExtraTokens);
	}

}
