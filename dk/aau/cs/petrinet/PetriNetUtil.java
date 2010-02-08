package dk.aau.cs.petrinet;

public final class PetriNetUtil {
	public static final String createGuard(String guard, TAPNPlace target, boolean isTransportArc) {
		if(!isTransportArc || target.getInvariant().equals("<inf")) return guard;

		String inv = null;
		if(target.getInvariant().contains("<=")){
			inv = target.getInvariant().substring(2);
		}else{
			inv = target.getInvariant().substring(1);
		}		

		int invariantBound = Integer.parseInt(inv);

		String upperString = guard.substring(guard.indexOf(",")+1, guard.length()-1);
		String lowerString = guard.substring(1,guard.indexOf(","));
		int upper = Integer.parseInt(upperString);
		int lower = Integer.parseInt(lowerString);

		if(invariantBound < lower){
			return "false";
		}else if(invariantBound < upper && invariantBound > lower){
			return guard.replace(String.valueOf(upper), String.valueOf(invariantBound));
		}else{
			return guard;
		}
	}
}
