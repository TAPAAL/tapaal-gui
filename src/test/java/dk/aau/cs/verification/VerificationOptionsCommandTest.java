package dk.aau.cs.verification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
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
}
