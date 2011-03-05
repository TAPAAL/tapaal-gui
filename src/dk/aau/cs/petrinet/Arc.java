package dk.aau.cs.petrinet;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>, Joakim Byg <jokke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
@Deprecated
public class Arc implements Comparable<Arc> {

	PlaceTransitionObject source = null;
	PlaceTransitionObject target = null;
	private boolean isPartOfTransportArc;

	// int weight = 1;
	public Arc() {

	}

	public Arc(PlaceTransitionObject source, PlaceTransitionObject target) {
		this.source = source;
		this.target = target;
	}

	public Arc(Arc a) {
		this.source = a.getSource();
		this.target = a.getTarget();
	}

	public PlaceTransitionObject getSource() {

		return source;

	}

	public PlaceTransitionObject get(String s) {

		if (s.equalsIgnoreCase("source")) {
			return source;
		} else if (s.equalsIgnoreCase("target")) {
			return target;
		}

		return null;

	}

	public PlaceTransitionObject getTarget() {

		return target;

	}

	public void setSource(PlaceTransitionObject pt) {
		this.source = pt;
	}

	public void setTarget(PlaceTransitionObject pt) {
		this.target = pt;
	}

	public void setIsTransportArc(boolean isPartOfTransportArc) {
		this.isPartOfTransportArc = isPartOfTransportArc;
	}

	public boolean isTransportArc() {
		return isPartOfTransportArc;
	}

	@Override
	public String toString() {

		return "Arc from " + source.getName() + " to " + target.getName();
	}

	public int compareTo(Arc arg0) {

		return 0;
	}
}
