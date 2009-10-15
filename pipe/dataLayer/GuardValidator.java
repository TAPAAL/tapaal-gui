package pipe.dataLayer;

import java.util.ArrayList;
import java.util.HashMap;

import pipe.dataLayer.constraints.ConstraintPair;
import pipe.dataLayer.constraints.GuardConstraint;

public class GuardValidator {
	private ConstantStore constants;
	private HashMap<String, ArrayList<GuardConstraint>> constraints = new HashMap<String, ArrayList<GuardConstraint>>();
	private HashMap<TimedArc, ArrayList<ConstraintPair>> arcToConstraints = new HashMap<TimedArc, ArrayList<ConstraintPair>>();
	
	public GuardValidator(ConstantStore constants){
		this.constants = constants;
	}
	
	public boolean allowUpdate(final String oldName, final Constant constant){
		ConstantStore constStore = new ConstantStore(){
			public Constant getConstant(String constantName){
				if(constantName.equals(oldName)) 
					return constant;
				else
					return constants.getConstant(constantName);
			}
		};
		
		ArrayList<GuardConstraint> constraintsToCheck = constraints.get(oldName);
		if(constraintsToCheck != null){
			for(GuardConstraint gc : constraintsToCheck){
				gc.setConstantStore(constStore);
				if(!gc.isSatisfied())return false;
			}
		}	
		
		return true;
	}
	
	public void AddConstraint(TimedArc arc, String constantName, GuardConstraint gc){
		if(!constraints.containsKey(constantName))
			constraints.put(constantName, new ArrayList<GuardConstraint>());
		
		constraints.get(constantName).add(gc);	
		constants.incrementConstantUsage(constantName);
		
		if(!arcToConstraints.containsKey(arc))
			arcToConstraints.put(arc, new ArrayList<ConstraintPair>());
		
		arcToConstraints.get(arc).add(new ConstraintPair(constantName, gc));
	}
	
	public void removeConstraintsFor(TimedArc arc){
		ArrayList<ConstraintPair> gcs = arcToConstraints.get(arc);
		for(ConstraintPair cp : gcs){
			constraints.get(cp.getConstantName()).remove(cp.getConstraint());
			constants.decrementConstantUsage(cp.getConstantName());
		}
		
		arcToConstraints.remove(arc);
	}

	public boolean containsConstraintsFor(TimedArc arc) {
		ArrayList<ConstraintPair> gcs = arcToConstraints.get(arc);
		if(gcs == null)
			return false;
		else
			return !gcs.isEmpty();
	}

	public void clearAll() {
		arcToConstraints.clear();
		constraints.clear();		
	}
}
