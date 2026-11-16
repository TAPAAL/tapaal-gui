package dk.aau.cs.io;

public class NamePurifier {
	public static String purify(String name){
		var purified = name.trim().
				replace(".", "_dot_").
				replace(" ", "_space_").
				replace("-", "_dash_").
				replace("/", "_slash_").
				replace("=", "_equals_").
				replace(",", "_comma_").
				replace("(", "_openparen_").
				replace(")", "_closeparen_");
                
        // Double underscores are used to seperate components from transitions & places
		purified = purified.replaceAll("_+", "_");

		return purified;
	}

	// Reduced-net PNML ids are component__name or Shared__name. The loader then collapses underscores.
	public static String purifiedComposedName(String template, String name) {
		String owner = template == null || template.isEmpty() ? "Shared" : template;
		return purify(owner + "__" + name);
	}
}
