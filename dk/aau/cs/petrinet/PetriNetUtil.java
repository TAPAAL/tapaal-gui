package dk.aau.cs.petrinet;

public final class PetriNetUtil {
	public static final String createGuard(String guard, TAPNPlace target, boolean isTransportArc) {
		if(!isTransportArc || target.getInvariant().equals("<inf")) return guard;

		String inv = null;
		String invComparison = null;
		if(target.getInvariant().contains("<=")){
			inv = target.getInvariant().substring(2);
			invComparison = "<=";
		}else{
			inv = target.getInvariant().substring(1);
			invComparison = "<";
		}		

		int invariantBound = Integer.parseInt(inv);

		String firstDelim = guard.substring(0,1);
		String upperString = guard.substring(guard.indexOf(",")+1, guard.length()-1);
		String lowerString = guard.substring(1,guard.indexOf(","));
		int upper = Integer.parseInt(upperString);
		int lower = Integer.parseInt(lowerString);

		if(invariantBound < lower || (invariantBound == lower && firstDelim.equals("("))
				|| (invariantBound == lower && invComparison.equals("<"))){
			return "false";
		}else if(invariantBound > upper){
			return guard;
		}else {
			if(invComparison.equals("<=")){
				return firstDelim + lower + "," + invariantBound + "]";
			}else{
				return firstDelim + lower + "," + invariantBound + ")";
			}
		}
	}
}
