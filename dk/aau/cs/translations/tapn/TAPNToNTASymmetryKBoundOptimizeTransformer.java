package dk.aau.cs.translations.tapn;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.KBoundOptmizerUPPAALQuery;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class TAPNToNTASymmetryKBoundOptimizeTransformer extends
		TAPNToNTASymmetryTransformer {
	private final String usedExtraTokens = "usedExtraTokens";
	private int tokens = 0;
	private final int SUBTRACT = 0;
	private final int ADD = 1;

	public TAPNToNTASymmetryKBoundOptimizeTransformer(int extraNumberOfTokens) {
		super(extraNumberOfTokens);
	}

	@Override
	protected List<TimedAutomaton> createAutomata(TimedArcPetriNet model){
		List<TimedAutomaton> tas = super.createAutomata(model);
		tokens = model.getTokens().size();
		
		for(TimedAutomaton ta : tas){
			if(ta.getName().equals("Token")){
				addKBoundUpdates(ta);
				break;
			}
		}
		
		return tas;
	}
	
	private void addKBoundUpdates(TimedAutomaton ta) {
		Location pcapacity = getLocationByName("P_capacity");
		
		for(Edge e : ta.getTransitions()){
			if(e.getSource() == pcapacity && isNotInitializationEdge(e)){
				String newUpdate = createUpdate(e.getUpdate(),ADD);
				e.setUpdate(newUpdate);
			}else if(e.getDestination() == pcapacity){
				String newUpdate = createUpdate(e.getUpdate(),SUBTRACT);
				e.setUpdate(newUpdate);
			}
		}
	}

	private boolean isNotInitializationEdge(Edge e) {
		Pattern pattern = Pattern.compile("^c(?:\\d)+\\?$");
		Matcher matcher = pattern.matcher(e.getSync());
		return !matcher.find();
	}

	private String createUpdate(String update, int method) {
		String newUpdate = update;
		if(update != null && !update.isEmpty()){
			newUpdate += ",";
		}
		newUpdate += usedExtraTokens;
		if(method == ADD){
			newUpdate += "++";
		}else{
			newUpdate += "--";
		}
		
		return newUpdate;
	}

	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder("int["); 
		builder.append(-tokens);
		builder.append(",");
		builder.append(tokens);
		builder.append("] ");
		builder.append(usedExtraTokens);
		builder.append(" = 0;\n");
		builder.append(super.createGlobalDeclarations(model));
		return builder.toString();
	}
	
	@Override
	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		UPPAALQuery query = super.transformQuery(tapnQuery);
		return new KBoundOptmizerUPPAALQuery(query);
	}
}
