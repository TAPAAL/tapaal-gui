package dk.aau.cs.model.NTA;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SupQuery implements UPPAALQuery {
	private static final String SUP = "sup:";
	private final List<String> elements = new ArrayList<String>();

	public SupQuery(String... elems) {
		for (String e : elems) {
			elements.add(e);
		}
	}

	public void addElement(String elem) {
		elements.add(elem);
	}

	public void removeElement(String elem) {
		elements.remove(elem);
	}

	public void output(PrintStream file) {
		file.append(SUP);
		boolean first = true;
		for (String elem : elements) {
			if (!first) {
				file.append(',');
			}
			file.append(elem);
		}
		file.append('\n');
	}

}
