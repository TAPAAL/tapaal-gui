package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.HashSet;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.util.Tuple;

public class VerifyPlaceNamesVisitor extends VisitorBase {

	private ArrayList<Tuple<String, String>> templatePlaceNames;

	public VerifyPlaceNamesVisitor(ArrayList<Tuple<String, String>> templatePlaceNames) {
		this.templatePlaceNames = templatePlaceNames;
	}

	public Context verifyPlaceNames(TCTLAbstractProperty query) {
		Context c = new Context();

		query.accept(this, c);

		return c;
	}

	public void visit(TCTLPlaceNode placeNode, Object context) {
		Context c = (Context) context;
		if (!templatePlaceNames.contains(new Tuple<String,String>(placeNode.getTemplate(), placeNode.getPlace()))) {
                        String temp = placeNode.getTemplate() != "" ? placeNode.getTemplate() + "." : "";
			c.addIncorrectPlaceName(temp + placeNode.getPlace());
			c.setResult(false);
		}
	}
	
	// / context class
	public class Context {
		private Boolean result;
		private HashSet<String> incorrectPlaceNames;

		public Boolean getResult() {
			return result;
		}

		public void setResult(Boolean result) {
			this.result = result;
		}

		public HashSet<String> getIncorrectPlaceNames() {
			return incorrectPlaceNames;
		}

		public void addIncorrectPlaceName(String incorrectPlaceName) {
			incorrectPlaceNames.add(incorrectPlaceName);
		}

		public Context() {
			result = true;
			incorrectPlaceNames = new HashSet<String>();
		}
	}

}
