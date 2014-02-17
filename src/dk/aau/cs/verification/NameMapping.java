package dk.aau.cs.verification;

import java.util.HashMap;

import dk.aau.cs.util.Tuple;

public class NameMapping {
	private HashMap<String, Tuple<String, String>> mappedNamesToOriginalNames;
	private HashMap<Tuple<String, String>, String> originalToMappedNames;

	public NameMapping() {
		mappedNamesToOriginalNames = new HashMap<String, Tuple<String, String>>();
		originalToMappedNames = new HashMap<Tuple<String, String>, String>();
	}

	public void addMappingForShared(String objectName, String mappedName){
		this.addMapping("", objectName, mappedName);
	}
	public void addMapping(String templateName, String objectName, String mappedName) {
		Tuple<String, String> value = new Tuple<String, String>(templateName, objectName);
		if (mappedNamesToOriginalNames.containsKey(mappedName))
			throw new RuntimeException(mappedName + " already maps to a value");
		if (originalToMappedNames.containsKey(value))
			throw new RuntimeException(templateName + "." + objectName + " already maps to a value");

		mappedNamesToOriginalNames.put(mappedName, value);
		originalToMappedNames.put(value, mappedName);
	}

	public Tuple<String, String> map(String key) {
		return mappedNamesToOriginalNames.get(key);
	}
	
	public String map(String templateName, String objectName) {
		return originalToMappedNames.get(new Tuple<String, String>(templateName, objectName));
	}
	
	public HashMap<Tuple<String, String>, String> getOrgToMapped(){
		return originalToMappedNames;
	}
}
