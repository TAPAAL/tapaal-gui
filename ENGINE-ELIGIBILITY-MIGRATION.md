# Engine eligibility migration

`EngineEligibility` is the pure policy seam for engine availability and verification-option eligibility. `QueryDialog` supplies model/query facts and applies the returned decisions to Swing controls; it does not derive the engine feature set itself.

## Validation contract

Run the focused characterization suite after changing eligibility policy:

```text
./gradlew test --tests net.tapaal.gui.petrinet.verification.EngineEligibilityTest --no-daemon
```

The suite currently checks:

- all `2^17` combinations of semantic input facts against an independent legacy feature-derivation oracle;
- all `2^20 * 8` feature/catalog compatibility comparisons;
- timed, stochastic, and untimed engine filtering and catalog order;
- trace, search, Tarjan, trace-refinement, explicit-search, symmetry, approximation, colored-reduction, inclusion, stubborn-reduction, and discrete forced-disable policies;
- state restoration for explicit search and the selected-option fallback rules.

The browser prototype at `src/main/java/net/tapaal/gui/petrinet/verification/PROTOTYPE-engine-eligibility.html` is the transition-level oracle. It compares legacy-style and proposed reducers across named action sequences and includes an exhaustive catalog run. The regression-injection checkbox must report mismatches; with the injection disabled it must report a clean match.

## Option coverage checklist

- [x] Available engine catalog and feature requirements
- [x] Trace choices: none, some, fastest
- [x] Search strategies and heuristic label
- [x] Trace refinement
- [x] Tarjan visibility and HyperLTL forced-off state
- [x] Explicit-search disable/restore memory
- [x] Colored reduction
- [x] Discrete inclusion visibility
- [x] Symmetry reduction
- [x] Skeleton/approximation controls
- [x] Time Darts, GCD, stubborn reduction, and deadlock/liveness restrictions

The remaining Swing code is intentionally an adapter for stateful restoration flags and raw-verification mode. Those flags are kept in the dialog until a later migration can give them an explicit owner without changing saved-query behavior.

## Full-suite note

The focused suite passes. The repository-wide suite currently has 13 pre-existing `TapnXmlLoaderTest` failures caused by `TAPAALGUI` static initialization throwing `HeadlessException` in this environment; the eligibility suite itself is independent of that Swing initialization path.
