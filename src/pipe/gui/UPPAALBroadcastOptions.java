package pipe.gui;

public class UPPAALBroadcastOptions extends EngineSupportOptions {

    public UPPAALBroadcastOptions() {
       super(
           "UPPAAL: Broadcast Reduction",//name of engine
           false,//  support fastest trace
            true,// support deadlock with net degree 2 and (EF or AG)
            false,//  support deadlock with EG or AF
            false,// support deadlock with inhibitor arcs
            false, //support weights
            true, //support inhibitor arcs
            false,// support urgent transitions
            true,// support EG or AF
            true,// support strict nets
            true,//  support timed nets/time intervals
            false,// support deadlock with net degree > 2
            false, //support games
            true, //support EG or AF with net degree > 2);
            false //support for nested quantification
       );
    }
}
