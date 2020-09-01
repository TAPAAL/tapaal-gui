package pipe.gui;

public class VerifyPNEngineOptions extends EngineSupportOptions {

    public VerifyPNEngineOptions() {
        super(
            "TAPAAL: Untimed Engine (verifypn)",
            false, //support fastest trace
            true, //support deadlock with net degree 2 and (EF or AG)
            true, //support deadlock with EG or AF
            true, //support deadlock with inhibitor arcs
            true, //support weights
            true, //support inhibitor arcs
            false, //support urgent transitions
            true, //support EG or AF
            false, //support strict nets
            false, //support timed nets/time intervals
            true, //support deadlock with net degree > 2
            false, //support games
            true, //support EG or AF with net degree > 2
            true //support for nested quantification
        );
    }
}