package dk.aau.cs.model.NTA;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class Edge {

	private String guard = "";
	private String sync = "";
	private String update = "";

	private Location source;
	private Location dest;

	public Edge(Location source, Location dest, String guard, String sync,
			String update) {
		this.source = source;
		this.dest = dest;
		this.guard = guard;
		this.sync = sync;
		this.update = update;
	}

	public Location getSource() {
		return source;
	}

	public void setSource(Location source) {
		this.source = source;
	}

	public Location getDestination() {
		return dest;
	}

	public void setDestination(Location dest) {
		this.dest = dest;
	}

	public String getGuard() {
		return guard;
	}

	public void setGuard(String guard) {
		// TODO: check guard conforms to what is allowed
		this.guard = guard;
	}

	public String getSync() {
		return sync;
	}

	public void setSync(String sync) {
		// TODO: check sync conforms to what is allowed in uppaal
		this.sync = sync;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		// TODO: check update conforms to what is allowed in uppaal
		this.update = update;
	}

	public StringBuffer toXML() {
		StringBuffer res = new StringBuffer();

		res.append("<transition>\n");
		res.append("<source ref=\"a" + source.getID() + "\" />\n");
		res.append("<target ref=\"a" + dest.getID() + "\" />\n");

		// replace "<" and ">" in guard with "&lt;" and "&gt;" respectively and
		// replace "&&" with "&amp;&amp;"
		String tmp = guard.replace("<", "&lt;").replace(">", "&gt;");
		tmp = tmp.replace("&&", "&amp;&amp;");

		res.append("<label kind=\"guard\">" + tmp + "</label>\n");
		res.append("<label kind=\"synchronisation\">" + sync + "</label>\n");
		res.append("<label kind=\"assignment\">" + update + "</label>\n");

		res.append("</transition>\n");
		return res;
	}

	@Override
	public String toString() {
		return "Edge from " + source.getName() + " to " + dest.getName()
				+ ". Guard: " + guard + ", Sync: " + sync + ", Update: "
				+ update;
	}
}
