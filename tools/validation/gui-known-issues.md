# Known GUI issues and their reproducers

Each `KnownIssue*Test` in `net.tapaal.gui.smoke` reproduces a confirmed product bug. These tests fail on the current code. They are skipped unless `TAPAAL_GUI_KNOWN_ISSUES=true`, so the ordinary `guiSmokeTest` stays green and reports them as skipped.

```sh
# Run the reproducers. Each one should fail with the message listed below.
TAPAAL_GUI_KNOWN_ISSUES=true xvfb-run -a -s '-screen 0 1280x900x24' ./gradlew --no-daemon guiSmokeTest --tests '*KnownIssue*'
# Run one of them.
TAPAAL_GUI_KNOWN_ISSUES=true xvfb-run -a -s '-screen 0 1280x900x24' ./gradlew --no-daemon guiSmokeTest --tests '*KnownIssuePopupDeleteTarget*'
```

The first three cases use `src/test/resources/validation/known-issue-workbench/two-places.tapn`. The colored editor case uses `src/test/resources/validation/historic-canvas-place-editor-undo/before.tapn`.

When you fix one of these bugs, remove the `@EnabledIfEnvironmentVariable` line and rename the class to the ordinary `<Area><Behaviour>Test` form. The test then joins the normal suite as a regression test. Do not change an assertion so that it accepts the bug.

## Confirmed issues

Each reproducer failed the same way in 5 of 5 independent `guiSmokeTest` runs on base `77445ab35`, with a fixed 1280x900 Xvfb display and a fresh JVM for each class.

| Test | User steps | Expected | Actual | Where |
| --- | --- | --- | --- | --- |
| `KnownIssueFailedSaveKeepsChangesTest` | Drag P0, make the open file read-only, File > Save, then dismiss "File Output Error". | The file is unchanged. The net stays marked as changed and Edit > Undo is still enabled. | The file is unchanged and the error is shown, but `netChanged=false` and Undo is disabled. Closing the tab then discards the drag without a prompt. | `PetriNetTab.saveNet` clears the dirty flag and undo history after `writeNetToFile`, which catches the exception and shows the dialog itself. |
| `KnownIssuePopupDeleteTargetTest` | Click P0, right-click P1, choose Delete. | P1 is deleted and P0 remains. | P0 is deleted and P1 remains (`{P1=0}`). | The right-click does not change the selection, because `GraphicalElement` sends only left presses to selection. The popup's Delete is the global delete action, which deletes the selection. |
| `KnownIssuePopupNameVisibilityTest` | Right-click P0, choose Hide Place Name. | The written document stores `displayName="false"`. The net is marked as changed and Undo is enabled. | The document stores the change, but `netChanged=false` and Undo is disabled. | `ShowHideInfoAction` only toggles the flag. Edit Place records a `ChangeAllNamesVisibilityCommand` for the same change. |
| `KnownIssueHistoricCanvasPlaceEditorUndoTest` | Add one token in a timed, colored place editor. Reopen the editor, accept it without edits, and invoke Undo. | One Undo removes the token added in the first dialog. | The second OK preserves the colored tokens and the serialized model. Undo enables Redo but the marking stays at `{P0=1}`. | This reproduces the symptom of [Launchpad #1870888](https://bugs.launchpad.net/tapaal/+bug/1870888) through the current colored editor. |

The read-only file in the first test is a real file-system failure, so it needs no production hook. `PlaceDragSaveRegressionTest` injects a transformer failure that the writer swallows, and it deliberately only observes the dirty state. This test asserts the dirty state, using the path where the error does reach the user.

## Probes that did not confirm a product issue

| Probe | Result |
| --- | --- |
| Hide Queries when it is the only side panel (View > Show simple workspace, then View > Display queries twice) | With Gradle's default `-ea`, the first hide throws `AssertionError` at `org.jdesktop.swingx.MultiSplitLayout.minimizeSplitBounds` on the event thread. This is the failure seen in the #395 validation. TAPAAL runs without `-ea`. With swingx assertions disabled, the panel hides, the menu check clears, and showing it again restores a 301x612 panel with no exception. The failure therefore depends on the test JVM's assertion setting, not on what a user sees. A scenario that hides the last panel should disable swingx assertions in the test JVM or expect this library assertion. |
| Error dialog ownership in Edit Constant (Global constants > New, OK with an empty name) | The "Error" dialog's owner is the main frame, not the Edit Constant dialog (`ConstantsDialogPanel` passes `TAPAALGUI.getApp()`). On the display the error appears on top and can be dismissed. The harness refused the OK click because its overlap check exempts only a window's owner. That refusal is a harness limitation. No user-visible failure was shown, so this is not kept as a reproducer. |

## Limits

- Only Linux, Xvfb and JDK 17 were used. Behavior under a real window manager, for example how a frame-owned error stacks over a modal dialog, was not checked.
- The read-only file test assumes that the test user is not root, because root can write to a read-only file.
- No verification engine, native file chooser or packaged build was involved.
