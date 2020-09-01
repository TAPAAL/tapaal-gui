package pipe.gui;

public class VerifyDTAPNEngineOptions extends EngineSupportOptions {

    public VerifyDTAPNEngineOptions() {
        super(
            "TAPAAL: Discrete Engine (verifydtapn)",
            true,//  support fastest trace
            true,// support deadlock with net degree 2 and (EF or AG)
            true,//  support deadlock with EG or AF
            true,// support deadlock with inhibitor arcs
            true, //support weights
            true, //support inhibitor arcs
            true,// support urgent transitions
            true,// support EG or AF
            false,// support strict nets
            true,//  support timed nets/time intervals
            true,// support deadlock with net degree > 2
            true, //support games
            true, //support EG or AF with net degree > 2);
            false //support for nested quantification
        );
    }
}
