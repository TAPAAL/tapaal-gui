# Writing GUI regression tests

GUI tests drive the real TAPAAL window on a virtual display. They use the real mouse and keyboard through `java.awt.Robot`, menu items and the application's own startup and file-open entry points. They assert on editor state and on the documents the application writes. The JUnit extension [`GuiHarness`](../../src/test/java/net/tapaal/gui/smoke/GuiHarness.java) provides these steps. The demonstration tests next to it are working recipes.

## Run

```sh
xvfb-run -a -s '-screen 0 1280x900x24' ./gradlew --no-daemon guiSmokeTest
xvfb-run -a -s '-screen 0 1280x900x24' ./gradlew --no-daemon guiSmokeTest --tests '*PlaceEditor*'
```

Use the fixed 1280x900 screen. The main window opens at 80% of the screen, so canvas coordinates and dialog positions depend on the screen size. A failed test leaves `failure.png`, `state.txt`, working copies and snapshots in `build/reports/gui-smoke/<class>/<method>/`. `state.txt` lists the tabs, dirty flags, current tool and open dialogs with their text. It also lists the focus owner and the last 60 mouse, key, window-focus and window-move events. Use those events to check which component received each click.

## Recipe

1. Put the input net in `src/test/resources/validation/<case>/`. Keep it small and place every object you click inside the visible canvas, roughly x < 600 and y < 400 at 100% zoom.
2. Add `<Area><Behaviour>Test` to `net.tapaal.gui.smoke` with `@RegisterExtension final GuiHarness gui = new GuiHarness();` and **one** `@Test`. Nothing needs registering.
3. Start with `gui.open(...)`, `gui.openAll(...)` or `gui.startEmpty()`. Drive the editor only through the input methods below.
4. After each meaningful step, assert on what a user would see. Use `gui.marking(tab)`, `gui.arcs(tab)` and `gui.transitionNames(tab)`, which also check that canvas and model agree. Read other state in `gui.onEdt(...)`.
5. For persistence, end with `gui.assertSavedMatches(...)` after File > Save, or `gui.assertSnapshotMatches(tab, ...)`. On the first run the test fails and writes `<name>.actual.tapn` to the artifacts. Diff it against the input, check that every difference is intended and copy it to `src/test/resources/validation/<case>/`. The comparison stays strict.
6. Close every dialog the test opens, then call `gui.awaitClosed(dialog)`.

Copy the closest demonstration:

| Demonstration | Shows |
| --- | --- |
| `PlaceDragSaveRegressionTest` | Mouse drag, File > Save, saved-file comparison, a provoked caught exception |
| `GuiWorkflowSmokeTest` | Draw menu tool, canvas click, Edit menu undo and redo, save, close and reopen |
| `DrawSelectDeleteTest` | Keyboard tool shortcut, arc drawing, shift-click selection, Delete key, toolbar Undo and popup menu |
| `PlaceEditorDialogTest` | Double-click editor, label-based fields, typing, OK, ctrl Z and ctrl Y, snapshot comparison |
| `TwoTabsCloseTest` | Several files at startup, tab click, unsaved-changes prompt, reopening through `openFiles` |
| `NewNetFromMenuTest` | Starting without a file, File > New dialog, Enter as the default button |
| `SimulationFireTest` | Simulation mode, firing by click, stepping and restoring the marking |
| `ComponentsPanelTest` | Panel buttons, component name dialog, switching components by clicking a list row |
| `HarnessSelfCheckTest` | Modal dialog from a menu, submenu lookup, lookup diagnostics, event-thread exceptions, preferences isolation |

## Verified API

Every method below is used by at least one demonstration that passed repeatedly. Except for `openFiles`, the input methods use the same mouse, keyboard and menu routes as a user.

| Area | Methods | Production route |
| --- | --- | --- |
| Start | `open(fixture)`, `openAll(fixtures...)`, `startEmpty()`, `file()`, `file(fixture)`, `workingCopy(fixture)` | `TAPAAL.main` with file arguments, as from a shell |
| Open more files | `openFiles(paths...)` | `GuiFrameController.openFiles`, the handler for files dropped on the window. File > Open calls the same loader after its native chooser |
| Menus and tools | `menu(path...)`, `toolbar(name)`, `selectTool(tab, name)` | `JMenuItem.doClick` on the menu bar item, the real mouse on a toolbar button, and the Draw menu |
| Keyboard | `keys("ctrl Z")`, `type(text)`, `typeInto(field, text)`, `holding("shift", ...)` | Robot keys to the focused window. Accelerators, key bindings and text fields handle them |
| Mouse | `click`, `doubleClick`, `rightClick`, `drag(from, dx, dy)`, `centerOf(...)`, `centerOfRow(list, row)` | Robot mouse. The harness checks that the point reaches the target and that no window, popup or tooltip covers it |
| Canvas | `clickObject`, `doubleClickObject`, `rightClickObject`, `dragObject`, `objectPoint`, `clickCanvas`, `canvasPoint` | Robot mouse on the named place or transition, or on a canvas point in model coordinates |
| Tabs | `tabs()`, `currentTab()`, `selectTab(tab)` | Robot click on the tab title |
| Dialogs and popups | `awaitDialog(title)`, `awaitClosed(dialog)`, `clickButton(container, text)`, `clickPopupItem(text)`, `dialogText(dialog)` | Robot mouse on the real buttons and items |
| Lookups (inside `onEdt`) | `menuItem`, `toolbarButton`, `tabbedPane`, `tool`, `place`, `transition`, `object`, `find`, `findAll`, `button`, `labelled` | Read-only |
| Editor state | `marking(tab)`, `transitionNames(tab)`, `arcs(tab)`, `onEdt(...)`, `await(...)`, `settle()` | Read-only and checked against the canvas |
| Documents | `snapshot(tab)`, `assertSnapshotMatches`, `assertSavedMatches`, `assertFileMatches` | `PetriNetTab.writeNetToFile`, the writer that Save uses. It leaves the file, dirty state and undo history unchanged |
| Failures | `caught()`, `takeUncaught()` | `CaughtExceptions` reports and the default uncaught-exception handler |

Escape hatches: `robot()` for other input such as the mouse wheel; call `settle()` afterwards. `find`, `button` and `labelled` scope lookups to a panel such as `tab.getQueryPane()` or `tab.getTemplateExplorer()`. `gui.onEdt(() -> ...)` reads any state. Do not use `onEdt` to call actions, set fields or change the model. That would bypass the behavior under test, and an action that opens a modal dialog inside `onEdt` blocks until the timeout.

## Isolation and speed

- **One JVM per class.** The frame, tabs, several dialogs and counters are static. `guiSmokeTest` forks a JVM for every `*Test` class, and the harness refuses a second application start in the same JVM. Write one `@Test` per class, so parameterized and repeated tests are not supported. A test can be a short user story with several checkpoints. Use one class per independent risk so that one failure does not hide the rest.
- **Cost.** A class takes about 2–5 s, including the JVM and window startup. A first drag on a fresh window takes up to 2 s. Seventy-five classes take an estimated 6–8 minutes, plus Gradle startup. All forks share one display and the mouse is global, so classes run one at a time; keep `maxParallelForks` at 1. To speed up CI, run test shards as separate jobs. Give each shard its own checkout and its own `xvfb-run` display.
- **Deterministic state.** Each JVM gets a new preferences store under `build/tmp/guiSmokeTest/preferences/` before TAPAAL loads. Do not touch TAPAAL classes in static initializers of a test class, because they would load first. Fixtures are copied to a temporary directory and the version check is kept offline. Generated names such as P0 and T0 restart for each net. Positions snap to the grid of 15 pixels at 100% zoom.
- **Window manager compensation.** Xvfb has no window manager, so the harness takes the steps a desktop or user normally takes and logs them:
  - It waits for startup focus, then clicks the status bar if focus has not arrived after 3 s.
  - After `awaitClosed`, it asks for focus to return to the dialog's owner window.
  - At startup and in `awaitDialog`, it compares the window position Swing reports with the display's. If they differ, it moves the window to where the display has it.
  - It lets the event thread react between mouse press and release, so a spinner commits its text before OK runs.
- **Timeouts.** Each step on the event thread times out after 15 s and prints the event thread's TAPAAL and test frames. Each wait times out after 10 s and lists the open dialogs. Each test method times out after 60 s.
- **Strictness.** A passing test still fails if it leaves a dialog open. It also fails if production code reports a caught exception or an exception reaches the event thread, unless the test takes it with `caught().take(site)` or `takeUncaught()`.

## Not supported, or not verified

- **Native file dialogs.** These include File > Open, Save as, Save of a net that was never saved, and the Import and Export menus. Use `openFiles`, Save on an opened fixture or `snapshot` instead.
- **File > Exit and closing the window.** Both call `System.exit` and end the test JVM.
- **Verification and engines.** The suite exercises creating, editing and saving queries, including option eligibility and game conversion. Verify, batch processing and workflow analysis require a separate setup with engines.
- **Help menu links.** Opening a browser shows an error dialog on Xvfb.
- **Coverage gaps.** Timed simulation currently uses uncolored nets. Colored simulation, stochastic workflows, clipboard operations, mouse-wheel input, scrolling and the UI Scale menu need their own scenarios.
- **Typing.** `type` covers letters, digits, space and `.-_:,/()[]<>=+` on the US layout.
- **Focus-dependent shortcuts.** Keys go to the focused window, and canvas shortcuts such as `P` need the main window to have focus.

## Application behavior seen while building the harness

These may be worth a test or an issue. Their root causes have not been investigated.

1. A right click does not select. The popup menu's **Delete** removes the current selection, not the object that was right-clicked. A right-click on an unselected new transition followed by Delete left it in place.
2. After **Undo** of a deletion, the restored objects look selected, but Delete and the popup's Delete do nothing until the objects are selected again.
3. **Save** clears the undo history (`PetriNetTab.saveNet`).
4. The writer adds `hlinitialMarking` for a marking set in the place editor. It does not add one for a marking that was loaded from `initialMarking` and left unchanged. See `place-editor/expected.tapn`.

## Run an area

The suite has nine framework and smoke scenarios plus 15 regressions in each of five areas. Pass a class prefix to Gradle, for example `guiSmokeTest --tests 'net.tapaal.gui.smoke.Files*'`.

| Class prefix | Examples |
| --- | --- |
| `Files` | Save, Discard and Cancel, independent tab histories, reopening, legacy files, file-drop handling |
| `Canvas` | Drawing and rejecting arcs, token edits, grid alignment, selection and movement, deletion, undo and redo |
| `Editors` | Committed and rejected place, transition and arc edits, component copy and removal, shared elements |
| `SimulationRegression` | Firing, delay and history, token ages, active components, edit locking, restoring the initial marking |
| `ViewQuery` | Query authoring and persistence, game conversion, trace options, workspace, statistics, zoom and visibility |

Keep scenario and fixture names descriptive so contributors can find and run the affected checks. Give each test a distinct behavior to verify and add shared helpers as repeated actions become clear. If a case exposes a bug, keep the reproduction and expected result with its diagnostics so the fix can turn it into a regression test.
