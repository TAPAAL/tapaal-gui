package dk.aau.cs.verification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.TAPNQuery.QueryCategory;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerificationOptions.AlgorithmOption;
import dk.aau.cs.verification.VerificationOptions.QueryReductionTime;
import dk.aau.cs.verification.VerificationOptions.SearchOption;
import dk.aau.cs.verification.VerificationOptions.TraceOption;

class VerificationOptionsCommandTest {

    @Test
    void tapnOptionsPreserveTraceAndSearchArguments() {
        VerifyTAPNOptions options = new VerifyTAPNOptions(
            2,
            TraceOption.SOME,
            SearchOption.BFS,
            true,
            false,
            false,
            false,
            false,
            1,
            false,
            null
        );

        String arguments = options.toString();

        assertTrue(arguments.contains("--k-bound 2"));
        assertTrue(arguments.contains("--trace 1"));
        assertTrue(arguments.contains("--search-strategy BFS"));
    }

    @Test
    void pnOptionsPreserveQueryAlgorithmAndReductionArguments() {
        VerifyPNOptions options = new VerifyPNOptions(
            0,
            TraceOption.NONE,
            SearchOption.BFS,
            false,
            ModelReduction.NO_REDUCTION,
            false,
            false,
            1,
            QueryCategory.CTL,
            AlgorithmOption.CERTAIN_ZERO,
            false,
            QueryReductionTime.NoTime,
            true,
            null,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );

        String arguments = options.toString();

        assertTrue(arguments.contains("--reduction 0"));
        assertTrue(arguments.contains("--ctl-algorithm czero"));
        assertTrue(arguments.contains("--query-reduction 0"));
    }
}
