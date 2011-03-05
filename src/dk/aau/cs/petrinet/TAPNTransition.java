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
/**
 * @Deprecated use dk.aau.cs.model.tapn.TimedTransition instead
 */
@Deprecated
public class TAPNTransition extends Transition {

	boolean isUrgens = false;
	private List<TAPNInhibitorArc> inhibs = new ArrayList<TAPNInhibitorArc>();
	private boolean fromOriginalNet = false;

	public TAPNTransition(TAPNTransition t) {

	}

	public TAPNTransition(String name, boolean urgent) {

		this.name = name;
		this.isUrgens = urgent;

	}

	public TAPNTransition(String name) {

		this.name = name;

	}

	public TAPNTransition() {
		// TODO do stuff
	}

	public TAPNTransition(boolean urgent) {
		this.isUrgens = urgent;
	}

	public boolean isUrgent() {
		return isUrgens;
	}

	public void setUrgent(boolean urgens) {
		this.isUrgens = urgens;
	}

	public boolean hasInhibitorArcs() {
		return inhibs.size() > 0;
	}

	public void addToInhibitorSet(TAPNInhibitorArc a) {
		inhibs.add(a);
	}

	public boolean removeFromInhibitorSet(TAPNInhibitorArc a) {
		return inhibs.remove(a);
	}

	public List<TAPNInhibitorArc> getInhibitorArcs() {
		return inhibs;
	}

	public void setFromOriginalNet(boolean fromOriginalNet) {
		this.fromOriginalNet = fromOriginalNet;
	}

	public boolean isFromOriginalNet() {
		return fromOriginalNet;
	}

	public boolean isDegree2() {
		return preset.size() <= 2 && postset.size() <= 2;
	}

	public boolean isDegree1() {
		return preset.size() == 1 && postset.size() == 1;
	}
}
