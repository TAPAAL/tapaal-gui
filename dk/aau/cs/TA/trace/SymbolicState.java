package dk.aau.cs.TA.trace;


public class SymbolicState {
	private String[] locations;
	private double[] ages;
	
	public SymbolicState(String[] locations, double[] ages){
		this.locations = locations;
		this.ages = ages;
	}
	
	public static SymbolicState Parse(String state){
		String[] stateLines = state.split("\n");
		
		String[] locations = parseLocations(stateLines[1]);
		double[] ages = parseAges(stateLines[2]);
		return new SymbolicState(locations, ages);
	}

	private static double[] parseAges(String string) {
		String[] split = string.split(" ");
		double[] ages = new double[split.length - 1];
		
		for(int i = 0; i < split.length-1; i++){
			String ageAsString = split[i].replaceFirst(".*\\..*=", "");
			ages[i] = Double.parseDouble(ageAsString.trim());
		}
		
		return ages;
	}

	private static String[] parseLocations(String string) {
		String[] split = string.split(" ");
		String[] locations = new String[split.length];
		for(int i = 0; i < split.length; i++){
			String location = split[i].replace("(", "").replace(")", "").trim();
			locations[i] = location.replaceFirst(".*\\.", "");
		}
		
		return locations;
	}
}
