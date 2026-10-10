---
name: test-tapaal
description: Run and diagnose TAPAAL unit, GUI, and Gradle verification tasks. Use when adding or changing tests, validating code changes, or investigating test failures in this Swing project.
---

# Test TAPAAL

Run Gradle verification inside Xvfb because tests may initialize Swing even when they look like ordinary unit tests:

```bash
xvfb-run -a --server-args="-screen 0 1280x1024x24" \
  ./gradlew -Djava.awt.headless=false test
```

Use the same wrapper for focused tests:

```bash
xvfb-run -a --server-args="-screen 0 1280x1024x24" \
  ./gradlew -Djava.awt.headless=false test --tests 'package.ClassName'
```

Use `build` instead of `test` for the final check when the change can affect compilation, packaging, generated JavaCC sources, or distribution setup. Match CI with:

```bash
xvfb-run -a --server-args="-screen 0 800x600x24+32" \
  ./gradlew -Djava.awt.headless=false build
```

Report the command and outcome. On failure, inspect the console output and `build/reports/tests/test/index.html`; rerun the narrowest failing test under the same Xvfb setup after making a fix.
