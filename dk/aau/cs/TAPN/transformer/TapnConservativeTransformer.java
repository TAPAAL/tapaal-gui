package dk.aau.cs.TAPN.transformer;

import java.util.Collection;
import dk.aau.cs.debug.Logger;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.TimedArc;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
/*  Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
 */
public class TapnConservativeTransformer {

	
	
	// Transforms a TAPN into a TAPN with transitions of input degree 2	
	public static DataLayer transform(DataLayer input) {
		
		//Make the net bounded and conservative
		Logger.log("lalal");
		DataLayer toReturn = new DataLayer();
		
		// Add p_lock and p_capasity

		//toReturn.addPetriNetObject(new TimedPlace("P_lock", "P_lock", 0, 0, ""));
		TimedPlace capasity =new TimedPlace("P_capasity", "P_capasity", 0, 0, "");
		toReturn.addPetriNetObject(capasity);
				
		// For each transition check the size of the pre and post set
		
		Transition[] transitions = input.getTransitions();
		
		for (Transition t : transitions){
			
			// Get size of pre and post set
			Collection<Arc> postset = t.getPostset();
			Collection<Arc> preset = t.getPreset();
			
		    int sizePreset = preset.size();
		    int sizePostset = postset.size();
		    
		    if (sizePreset > sizePostset) {
		    	// FIX IT
		    	// Add arc from trans to cap
		    	for (int i=0;i<sizePreset-sizePostset; i++) {
		    		
		    		NormalArc newarc = new NormalArc(t);
		    		t.addConnectFrom(newarc);
		    		newarc.setTarget(capasity);
		    		capasity.addConnectTo(newarc);
		    		
		    	}
		    	
		    	
		    } else if (sizePreset < sizePostset) {
		    	// FIX 
		    	// Add timedarc from place to trans
		    	for (int i=0;i<sizePostset-sizePreset; i++) {	
		    		
		    		NormalArc newarc = new TimedArc(new NormalArc(capasity));
		    		capasity.addConnectFrom(newarc);
		    		newarc.setTarget(t);
		    		t.addConnectTo(newarc);
		    		
		    	}
		    	
		    	
		    }
			
			
		}
		
		return toReturn;
		
	}
	
	
	
	
}
