package dk.aau.cs.io;

import java.util.HashMap;

public class IdResolver {
	private final HashMap<String, HashMap<String, String>> idToNamesPerTemplate = new HashMap<String, HashMap<String, String>>();
	
	public void add(String template, String id, String name){
		if(!idToNamesPerTemplate.containsKey(template)){
			idToNamesPerTemplate.put(template, new HashMap<String, String>());
		}
		
		HashMap<String, String> idToNames = idToNamesPerTemplate.get(template);
		idToNames.put(id, name);
	}
	
	public String get(String template, String id){
		return idToNamesPerTemplate.get(template).get(id);
	}

	public void clear() {
		idToNamesPerTemplate.clear();
	}
}
