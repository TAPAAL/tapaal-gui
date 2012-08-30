package dk.aau.cs.model.tapn;

public abstract class Weight {
	public abstract int value();
	public abstract Weight copy();
	public abstract String toString(boolean displayConstantnames);
	public abstract String nameForSaving(boolean writeConstantNames);
	
	public static Weight parseWeight(String attribute, ConstantStore constants) {
		Weight weight;
		try{
			int weightAsInt = Integer.parseInt(attribute);
			weight = new IntWeight(weightAsInt);
		} catch (NumberFormatException e){
			if(constants.containsConstantByName(attribute)){
				weight = new ConstantWeight(constants.getConstantByName(attribute));
			} else {
				throw new RuntimeException("A constant which was not declared was used in an time interval of an arc.");
			}
		}
		return weight;
	}
}
