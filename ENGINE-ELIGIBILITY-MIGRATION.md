# Engine eligibility migration

`EngineEligibility` is the pure policy seam for engine availability and verification-option eligibility. `QueryDialog` supplies model/query facts and applies the returned decisions to Swing controls; it does not derive the engine feature set itself.

## Validation contract

Run the focused characterization suite after changing eligibility policy:

```text
./gradlew test --tests net.tapaal.gui.petrinet.verification.EngineEligibilityTest --no-daemon
```

The suite currently checks:

- all `2^18` combinations of semantic input facts against an independent legacy feature-derivation oracle;
- all `2^22 * 8` feature/catalog compatibility comparisons;
- timed, stochastic, and untimed engine filtering and catalog order;
- trace, search, Tarjan, trace-refinement, explicit-search, symmetry, approximation, colored-reduction, inclusion, stubborn-reduction, and discrete forced-disable policies;
- state restoration for explicit search and the selected-option fallback rules.

The browser prototype at `src/main/java/net/tapaal/gui/petrinet/verification/PROTOTYPE-engine-eligibility.html` is the transition-level oracle. It compares legacy-style and proposed reducers across named action sequences and includes an exhaustive catalog run. The regression-injection checkbox must report mismatches; with the injection disabled it must report a clean match.

## Option coverage checklist

- [x] Available engine catalog and feature requirements
- [x] Trace choices: none, some, fastest
- [x] Search strategies and heuristic label
- [x] Trace refinement
- [x] Tarjan visibility and HyperLTL/SMC forced-off state
- [x] Explicit-search disable/restore memory
- [x] Colored reduction
- [x] Discrete inclusion visibility
- [x] Symmetry reduction
- [x] Skeleton/approximation controls
- [x] Time Darts, GCD, stubborn reduction, and deadlock/liveness restrictions

The remaining Swing code is intentionally an adapter for stateful restoration flags and raw-verification mode. Those flags are kept in the dialog until a later migration can give them an explicit owner without changing saved-query behavior.

## Full-suite validation

The eligibility suite is independent of Swing initialization. Run the full build and GUI-dependent loader tests under Xvfb:

```text
xvfb-run -a ./gradlew -Djava.awt.headless=false build --no-daemon
```

The policy follows current `main`, including explicit-search feature requirements, SMC Tarjan disabling, trace-refinement selection clearing, and stubborn reduction availability for HyperLTL when Time Darts is off. Timed original-net trace controls remain in the Swing adapter.
