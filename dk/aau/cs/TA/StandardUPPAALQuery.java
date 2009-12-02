package dk.aau.cs.TA;

import java.io.PrintStream;

public class StandardUPPAALQuery implements UPPAALQuery {
	private String pathQuantifier;
	private String nodeQuantifier;
	
	private String query; // TODO: make this more object oriented
	
	public StandardUPPAALQuery(String inputQuery){ 
		String query = inputQuery.trim();
		setPathQuantifier(query.substring(0,1));
		setNodeQuantifier(query.substring(1,3));
		this.query = query.substring(3, query.length());
	}
		
	@Override
	public void output(PrintStream file) {
		file.append(pathQuantifier);
		file.append(nodeQuantifier);
		file.append(query);
	}

	public void setPathQuantifier(String pathQuantifier) {
		if(pathQuantifier.equals("E") || pathQuantifier.equals("A")){
			this.pathQuantifier = pathQuantifier;
		}
	}

	public String getPathQuantifier() {
		return pathQuantifier;
	}

	public void setNodeQuantifier(String nodeQuantifier) {
		if(nodeQuantifier.equals("<>") || nodeQuantifier.equals("[]")){
			this.nodeQuantifier = nodeQuantifier;
		}
	}

	public String getNodeQuantifier() {
		return nodeQuantifier;
	}	
}
