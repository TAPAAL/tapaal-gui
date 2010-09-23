package dk.aau.cs.translations;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TAPN.ModelTransformer;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class PipeToNTATransformer implements ModelTransformer<DataLayer, NTA> {

	private ModelTransformer<DataLayer, TimedArcPetriNet> pipeToTAPNtransformer;
	private ModelTransformer<TimedArcPetriNet, NTA> tapnToNtaTransformer;
	
	public PipeToNTATransformer(ModelTransformer<TimedArcPetriNet, NTA> tapnToNtaTransformer){
		this.tapnToNtaTransformer = tapnToNtaTransformer;
	}
	
	public NTA transformModel(DataLayer model) throws Exception {
		TimedArcPetriNet tapn = pipeToTAPNtransformer.transformModel(model);
		return tapnToNtaTransformer.transformModel(tapn);
	}
}
