package dk.aau.cs.petrinet;

import java.util.ArrayList;
import java.util.List;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class PlaceTransitionObject {

	protected static int newid = 0;

	public ArrayList<Arc> preset = new ArrayList<Arc>();
	ArrayList<Arc> postset = new ArrayList<Arc>();

	int id = newid++;
	String name = "";

	protected PlaceTransitionObject() {
		name = "pt" + id;
	}

	protected PlaceTransitionObject(String name) {
		this.name = name;
	}

	public String getName() {

		return name;
	}

	public List<Arc> getPreset() {
		return preset;
	}

	public List<Arc> getPostset() {
		return postset;
	}

	public List<Place> getPresetPlaces() {
		ArrayList<Place> toReturn = new ArrayList<Place>();

		for (Arc a : preset) {
			toReturn.add((Place) a.getSource());
		}
		return toReturn;
	}

	public List<Place> getPostsetPlaces() {
		ArrayList<Place> toReturn = new ArrayList<Place>();

		for (Arc a : postset) {
			toReturn.add((Place) a.getTarget());
		}
		return toReturn;
	}

	protected void addPreset(Arc a) {
		if (a instanceof TAPNInhibitorArc) {
			throw new IllegalArgumentException(
					"Inhibitor arcs should not be part of preset!");
		}
		preset.add(a);
	}

	protected void addPostset(Arc a) {
		if (a instanceof TAPNInhibitorArc) {
			throw new IllegalArgumentException(
					"Inhibitor arcs should not be part of preset!");
		}
		postset.add(a);
	}

	protected void removePreset(Arc a) {
		preset.remove(a);
	}

	protected void removePostset(Arc a) {
		postset.remove(a);
	}

	protected void removeArc(Arc a) {
		removePreset(a);
		removePostset(a);
	}

	public int getID() {
		return id;
	}

}