# Current-tab interaction seam

The current-tab interaction module is owned by `GuiFrameController` and is
exposed through the narrow `TabInteraction` interface. This seam is the
production test surface for tab selection; callers do not inspect the tab
collection, selected index, or `TAPAALGUI` application registry.

## Ownership and invariants

- `GuiFrameController` owns the tab collection and the current-tab reference.
- `getCurrentTab()` returns empty when no tab is active.
- A non-empty current tab is also registered in the controller's tab
  collection.
- Each `PetriNetTab` assigns itself as the owner of its `DataLayer` models.
- Tab-sensitive asynchronous work captures its owner before execution and
  uses that owner when it completes.
- `TAPAALGUI.getApp()` and `getAppGui()` remain temporary application-owner
  adapters for Swing window ownership; they are not tab-selection APIs.

## Migration rule

Pass the narrowest useful tab context from the nearest owning module. Use
`TabInteraction` when the operation needs the active tab; pass
`PetriNetTab` when an operation belongs to one specific tab. Do not introduce
another registry or universal application context.

This is an incremental seam with one production adapter today:
`GuiFrameController` implements `TabInteraction`. A DI framework should be
reconsidered only if a second real application composition or adapter emerges.
