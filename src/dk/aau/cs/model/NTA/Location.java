package dk.aau.cs.model.NTA;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class Location {
	private static int newid = 0;

	private int id = newid++;

	private String name = "";
	private String invariant = "";

	private boolean isUrgent = false;
	private boolean isCommitted = false;

	public Location(String name, String invariant, int x, int y,
			boolean isUrgent, boolean isCommitted) {
		this(name, invariant);

		this.isUrgent = isUrgent;
		this.isCommitted = isCommitted;
	}

	public Location(String name, String invariant) {

		if (!name.equals("")) {
			this.name = name;
		}

		if (invariant.equals("")) {
			this.invariant = "<inf";
		} else {
			this.invariant = invariant;
		}
	}

	public int getID() {
		return id;
	}

	public String getInvariant() {
		return invariant;
	}

	public boolean isUrgent() {
		return isUrgent;
	}

	public void setUrgent(boolean isUrgent) {
		this.isUrgent = isUrgent;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public StringBuffer toXML() {
		StringBuffer res = new StringBuffer();

		res.append("<location id=\"a" + id + "\" >\n");

		res.append("<name>" + name + "</name>\n");

		if (!invariant.equals("<inf"))
			res.append("<label kind=\"invariant\">"
					+ invariant.replace("&&", "&amp;&amp;")
							.replace("<", "&lt;") + "</label>\n");

		if (isCommitted)
			res.append("<committed/>\n");
		else if (isUrgent)
			res.append("<urgent/>\n");

		res.append("</location>\n");

		return res;
	}

	@Override
	public String toString() {
		return "Location " + name + ". Invariant: " + invariant
				+ ", Committed: " + isCommitted + ", Urgent: " + isUrgent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
