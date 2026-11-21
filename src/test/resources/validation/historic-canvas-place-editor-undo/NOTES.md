# Colored place editor undo

This reproduces the symptom of Launchpad #1870888 in the current timed, colored place editor. Open `before.tapn`, double-click P0, and add one token through the Tokens panel. Click OK, reopen P0, and click OK again with no edits.

Five fresh 1280×900 Xvfb JVMs confirmed that the second OK preserves the colored tokens and produces a byte-identical TAPN snapshot. Ctrl+Z enables Redo, but the canvas and model still show `{P0=1}`. The first Undo should restore `{P0=0}`.

The targeted invocation skips the test when the environment variable is unset. To reproduce the failure, opt in explicitly.

```sh
TAPAAL_GUI_KNOWN_ISSUES=true xvfb-run -a -s '-screen 0 1280x900x24' \
  ./gradlew --no-daemon guiSmokeTest --tests '*KnownIssueHistoricCanvasPlaceEditorUndoTest'
```
