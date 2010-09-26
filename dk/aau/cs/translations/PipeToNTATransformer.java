package dk.aau.cs.translations;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TAPN.ModelTransformer;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;

public class PipeToNTATransformer implements ModelTransformer<DataLayer, NTA> {

	private ModelTransformer<TimedArcPetriNet, NTA> tapnToNtaTransformer;
	
	public PipeToNTATransformer(ModelTransformer<TimedArcPetriNet, NTA> tapnToNtaTransformer){
		this.tapnToNtaTransformer = tapnToNtaTransformer; // TODO: MJ -- This has to be fixed at some point, you need to specify a colored version if it requires colors
	}
	
	public NTA transformModel(DataLayer model) throws Exception {
		PipeTapnToAauTapnTransformer pipeTransformer;
		if(model.isUsingColors()){
			pipeTransformer = new ColoredPipeTapnToColoredAauTapnTransformer();
		}else{
			pipeTransformer = new PipeTapnToAauTapnTransformer();
		}
		
		TimedArcPetriNet tapn = pipeTransformer.transformModel(model);
		return tapnToNtaTransformer.transformModel(tapn);
	}
}
