package net.tapaal.gui.petrinet.verification;

public class UPPAALStandardOptions extends EngineSupportOptions {

    public UPPAALStandardOptions() {
        super (
            "UPPAAL: Standard Reduction",//name of engine
            false,// support fastest trace
            false,// support deadlock with net degree 2 and (EF or AG)
            false,// support deadlock with EG or AF
            false,// support deadlock with inhibitor arcs
            false, //support weights
            false, //support inhibitor arcs
            false,// support urgent transitions
            false,// support EG or AF
            true,// support strict nets
            true,// support timed nets/time intervals
            false,// support deadlock with net degree > 2
            false, //support games
            false, //support EG or AF with net degree > 2);
            false, //support for nested quantification,
            false,
            false,
            false
        );
    }
}
