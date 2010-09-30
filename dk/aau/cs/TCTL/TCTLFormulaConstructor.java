package dk.aau.cs.TCTL;

public final class TCTLFormulaConstructor {
	public static TCTLAbstractProperty ConstructTCTLFormulaFrom(String query) {
		TCTLAbstractProperty property;
		String q = query.trim();
		
		String quantifier = q.substring(0, 3);
		
//		if(quantifier.equals("E<>") || quantifier.equals("EF ")){
//			property = new TCTLEFNode();
//		}
		
		
		return null;
		
	}
}
