package dk.aau.cs.io;

public class NamePurifier {
	public static String purify(String name){
		return name.trim().
				replace(".", "_dot_").
				replace(" ", "_space_").
				replace("-", "_dash_").
				replace("/", "_slash_").
				replace("=", "_equals_");
	}
}
