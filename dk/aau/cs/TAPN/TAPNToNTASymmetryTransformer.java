package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTASymmetryTransformer extends TAPNToNTATransformer{
	private int numberOfInitChannels = 0;
		
	public TAPNToNTASymmetryTransformer(int extraNumberOfTokens) {
		super(extraNumberOfTokens);
	}
	

	@Override
	protected ArrayList<TimedAutomata> createAutomata(TimedArcPetriNet model){
		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();

		TimedAutomata control = createControlTemplate(model);
		tas.add(control);

		TimedAutomata token = createTokenAutomata(model);
		tas.add(token);

		TimedAutomata lock = createLockTemplate(model);
		tas.add(lock);

		return tas;
	}
	
	private TimedAutomata createLockTemplate(TimedArcPetriNet model) {
		
		return null;
	}

	private TimedAutomata createTokenAutomata(TimedArcPetriNet model) {
		TimedAutomata token = new TimedAutomata();
		
		addSymmetricInitialization(token, model);
		
		return token;
	}

	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder("const int N = ");
		builder.append(model.getTokens().size());
		builder.append(";typedef scalar[N] pid_t;");
		
		builder.append(super.createGlobalDeclarations(model));
		
		return builder.toString();
	}
	
	@Override
	protected String createSystemDeclaration() {
		return "system Control, Token, Lock;";
	}

	private void addSymmetricInitialization(TimedAutomata ta, TimedArcPetriNet model){
		numberOfInitChannels = 0;
		Location pcapacity = getLocationByName("P_capacity");

		ta.setName("Token");
		ta.setParameters("const pid_t id");
		ta.setInitLocation(pcapacity);
		
		List<Token> tokens = model.getTokens();
		for(int i = 0; i < tokens.size(); i++){
			Token token = tokens.get(i);
			Location destination = getLocationByName(token.getPlace().getName());
			
			if(destination != pcapacity){
				numberOfInitChannels++;
				Edge e = new Edge(pcapacity, destination, "", "c" + i + "?", "");
				ta.addTransition(e);
			}
		}
	}
	
	private TimedAutomata createControlTemplate(TimedArcPetriNet model) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UPPAALQuery transformQuery(TAPNQuery query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
