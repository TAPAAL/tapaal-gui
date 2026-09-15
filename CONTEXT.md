# TAPAAL Verification

This context describes how a model and query are prepared for verification by a supported engine.

## Language

**Verification engine**:
An executable verifier capable of checking particular combinations of model and query features.
_Avoid_: Reduction, backend

**Engine eligibility**:
Whether a verification engine can check the current combination of model features, query features, and requested verification behavior.
_Avoid_: Engine support, compatibility

**Verification option**:
A user-selectable choice that changes how an eligible verification engine performs verification, including trace, search, approximation, and reduction choices.
_Avoid_: Flag, setting

**Verification-option state**:
The availability and current value of every verification option for the current model, query, and selected verification engine.
_Avoid_: UI state, dialog state
