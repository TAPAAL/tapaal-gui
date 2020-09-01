package pipe.gui;

public class UPPAALOptimizedStandardOptions extends EngineSupportOptions {

    public UPPAALOptimizedStandardOptions() {
        super (
            "UPPAAL: Optimised Standard Reduction",//name of engine
            false,//  support fastest trace
            false,// support deadlock with net degree 2 and (EF or AG)
            false,//  support deadlock with EG or AF
            false,// support deadlock with inhibitor arcs
            false, //support weights
            false, //support inhibitor arcs
            false,// support urgent transitions
            true,// support EG or AF
            true,// support strict nets
            true,//  support timed nets/time intervals
            false,// support deadlock with net degree > 2
            false, //support games
            false,//support EG or AF with net degree > 2);
            false //support for nested quantification
        );
    }
}
