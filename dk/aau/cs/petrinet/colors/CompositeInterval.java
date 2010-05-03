package dk.aau.cs.petrinet.colors;

public class CompositeInterval extends ColoredInterval {
	private ColoredTimeInvariant other;
	
	public CompositeInterval(ColoredInterval first, ColoredTimeInvariant second){
		super(first);
		this.other = second;
	}
	
	public String convertToTAGuardString(String clockName, String tokenValueName){
		String guard = super.convertToTAGuardString(clockName, tokenValueName);
		String otherGuard = other.convertToTAInvariantString(clockName, tokenValueName);
		
		if(!guard.isEmpty() && !otherGuard.isEmpty()){
			guard += " && ";
		}
		guard += otherGuard;
		
		return guard;
	}
}
