package dk.aau.cs.TAPN.uppaaltransform;

import java.io.PrintStream;
import java.util.ArrayList;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.TAPN;

public class AdvancedUppaalSymKBound extends AdvancedUppaalSym {
	private final String usedExtraTokens = "usedExtraTokens";
	
	protected void createGlobalDeclarations(TAPN model, PrintStream uppaalXML,
			int tokens) {
		super.createGlobalDeclarations(model, uppaalXML, tokens);
		uppaalXML.println("int[" + (-tokens) + "," + tokens + "] " + usedExtraTokens + " = 0;");
	}
	
	protected String getAssignments(String initialAssignment, Arc source, Arc destination)
	{
		String additional = null;
		boolean sourcePCapacity = source.getSource().getName().equals("P_capacity");
		boolean destPCapacity = destination.getTarget().getName().equals("P_capacity");
		
		if(sourcePCapacity)
			additional = usedExtraTokens + "++";
		else if(destPCapacity)
			additional = usedExtraTokens + "--";
		else
			additional = "";
			
		String result = null;
		if(!initialAssignment.isEmpty() && !additional.isEmpty())
			result = initialAssignment + ", " + additional;
		else if(!initialAssignment.isEmpty() && additional.isEmpty())
			result = initialAssignment;
		else
			result = additional;
		return result;
	}
}
