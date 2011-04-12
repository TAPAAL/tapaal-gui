package dk.aau.cs.model.NTA;

import java.util.ArrayList;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class TimedAutomaton {
	private ArrayList<Edge> transitions = new ArrayList<Edge>();
	private ArrayList<Location> locations = new ArrayList<Location>();

	private String declarations = "";
	private String parameters = "";

	private String name = "";

	private Location initialLocation;

	public TimedAutomaton() {

	}

	public TimedAutomaton(String name, ArrayList<Edge> transitions,
			ArrayList<Location> locations, Location initLocation,
			String parameters) {
		this(name, transitions, locations, initLocation);

		this.parameters = parameters;
	}

	public TimedAutomaton(String name, ArrayList<Edge> transitions,
			ArrayList<Location> locations, Location initLocation) {
		this.name = name;
		this.transitions = transitions;
		this.locations = locations;
		initialLocation = initLocation;
	}

	public Location getInitLocation() {
		return initialLocation;
	}

	public void setInitLocation(Location initLocation) {
		initialLocation = initLocation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public ArrayList<Edge> getTransitions() {
		return transitions;
	}

	public void setTransitions(ArrayList<Edge> transitions) {
		this.transitions = transitions;
	}

	public ArrayList<Location> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<Location> locations) {
		this.locations = locations;
	}

	public String getDeclarations() {
		return declarations;
	}

	public void setDeclarations(String declarations) {
		this.declarations = declarations;
	}

	public void addLocation(Location l) {
		locations.add(l);
	}

	public void addTransition(Edge e) {
		transitions.add(e);
	}

	public StringBuffer toXML() {
		StringBuffer res = new StringBuffer();

		res.append("<template>\n");
		res.append("<name x=\"5\" y=\"5\">" + name + "</name>\n");

		if (!parameters.equals(""))
			res.append("<parameter>" + parameters + "</parameter>\n");

		if (!declarations.equals(""))
			res.append("<declaration>" + declarations + "</declaration>\n");

		// locations
		StringBuffer a = new StringBuffer();

		for (Location l : locations) {
			a = l.toXML();
			res.append(a);
		}

		// initial location
		res.append("<init ref=\"a" + initialLocation.getID() + "\" />\n");

		// transitions
		for (Edge t : transitions) {
			a = t.toXML();
			res.append(a);
		}

		res.append("</template>\n");

		return res;
	}
}
