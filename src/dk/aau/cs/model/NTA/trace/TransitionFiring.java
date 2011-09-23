package dk.aau.cs.model.NTA.trace;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class TransitionFiring implements TAFiringAction {
	private static final String AUTOMATA_LOCATION_PATTERN = "([\\w\\(\\)]+)\\.(\\w+)";
	private final String channel;
	private final SymbolicState previousState;
	private final Participant[] participants;
	private SymbolicState nextState;

	public TransitionFiring(SymbolicState state, String channel,
			Participant... participants) {
		previousState = state;
		this.channel = channel;
		this.participants = participants;
	}

	public String channel() {
		return channel;
	}

	public SymbolicState sourceState() {
		return previousState;
	}

	public SymbolicState targetState() {
		return nextState;
	}

	public Participant[] participants() {
		return participants;
	}

	public void setTargetState(SymbolicState state) {
		nextState = state;
	}

	@Override
	public String toString() {
		return "Transition: channel = " + channel;
	}

	public static TransitionFiring parse(SymbolicState state, String element) {
		String[] split = element.split("\n");

		String channel = parseChannel(split[1]);
		Participant[] participants = parseParticipants(state, split);

		return new TransitionFiring(state, channel, participants);
	}

	private static Participant[] parseParticipants(SymbolicState state,
			String[] lines) {
		Participant[] participants = new Participant[lines.length - 1];

		for (int i = 1; i < lines.length; i++) {
			Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN + "->");
			String string = lines[i];
			Matcher matcher = pattern.matcher(string);
			matcher.find();

			String automata = matcher.group(1);
			String location = matcher.group(2);
			HashMap<String, ValueRange> localClocksAndVariables = state
					.getLocalClocksAndVariablesFor(automata);
			participants[i - 1] = new Participant(automata, location,
					localClocksAndVariables);
		}

		return participants;
	}

	private static String parseChannel(String string) {
		Pattern pattern = Pattern.compile("(\\w+)(?:\\?|!)");
		Matcher matcher = pattern.matcher(string);

		if (matcher.find())
			return matcher.group(1);
		else
			return "tau";
	}
}
