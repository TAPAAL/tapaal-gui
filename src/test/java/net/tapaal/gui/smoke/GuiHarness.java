package net.tapaal.gui.smoke;

import net.tapaal.TAPAAL;
import net.tapaal.gui.CaughtExceptionRecorder;
import net.tapaal.gui.TapnXmlComparison;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.AssertionFailedError;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs one test against the real TAPAAL window and keeps evidence when it fails.
 *
 * <pre>{@code
 * @RegisterExtension
 * final GuiHarness gui = new GuiHarness();
 *
 * @Test
 * void savesTheEdit() throws Exception {
 *     var tab = gui.open("my-case/before.tapn");
 *     gui.dragPlace(tab, "P0", 90, 60);
 *     gui.menu("File", "Save");
 *     gui.assertSavedMatches("my-case/expected.tapn");
 * }
 * }</pre>
 *
 * <p>For each test the harness
 * <ul>
 *   <li>copies the fixture to a temporary file and starts the application on it through {@link TAPAAL#main},</li>
 *   <li>records failures that production code catches and continues after, see {@link #caught()},</li>
 *   <li>on failure writes a screenshot and the working copy of the file to
 *       {@code build/reports/gui-smoke/<test class>/<test method>/},</li>
 *   <li>disposes all windows afterwards.</li>
 * </ul>
 *
 * <p>The application keeps its frame and tabs in static fields that disposing windows does not reset.
 * The {@code guiSmokeTest} Gradle task therefore starts a fresh JVM for each test class in this
 * package; keep one {@code @Test} per class unless the second one tolerates the first one's state.
 *
 * <p>Input goes through the same paths a user's does: {@link #click} and {@link #drag} move the real
 * mouse with {@link Robot}, and {@link #menu} clicks the menu item, which runs its Swing action.
 */
public final class GuiHarness implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
    /** Fixture names are resolved against this classpath directory, {@code src/test/resources/validation/}. */
    public static final String FIXTURES = "/validation/";
    private static final long EDT_TIMEOUT_SECONDS = 15;
    private static final long AWAIT_TIMEOUT_SECONDS = 10;
    private static final int DRAG_STEPS = 10;

    private final CaughtExceptionRecorder caught = new CaughtExceptionRecorder()
        .allow("TAPAALGUI.init/native-about-handler", UnsupportedOperationException.class, "no native about menu on this desktop")
        .allow("TAPAALGUI.init/native-quit-handler", UnsupportedOperationException.class, "no native quit menu on this desktop")
        .allow("TAPAALGUI.init/taskbar-icon", UnsupportedOperationException.class, "no taskbar on this desktop");

    private Robot robot;
    private Path workDirectory;
    private Path artifacts;
    private Path file;
    private boolean recording;

    /** Code to run on the Swing event thread that returns nothing. */
    @FunctionalInterface
    public interface EdtAction {
        void run() throws Exception;
    }

    // ---- Starting the application ---------------------------------------------------------------

    /**
     * Copies a fixture from {@link #FIXTURES} to a temporary file, starts TAPAAL on it and returns its tab.
     * Call it once per test; {@link #file()} is the working copy that Save writes to.
     */
    public PetriNetTab open(String fixture) throws Exception {
        assertTrue(file == null, "open() starts the application, so call it once per test");
        file = workDirectory.resolve(Path.of(fixture).getFileName().toString());
        Files.copy(fixture(fixture), file);
        return onEdt(() -> {
            TAPAAL.main(new String[]{file.toString()});
            assertTrue(TAPAALGUI.getAppGui().isShowing(), "The main window should be showing");
            var tab = TAPAALGUI.getCurrentTab();
            assertNotNull(tab, "TAPAAL did not open " + FIXTURES + fixture + " in a tab");
            return tab;
        });
    }

    /** The working copy that {@link #open} started the application on. */
    public Path file() {
        assertNotNull(file, "Call open() first");
        return file;
    }

    /** The classpath fixture as a file. */
    public static Path fixture(String name) throws Exception {
        var resource = GuiHarness.class.getResource(FIXTURES + name);
        assertNotNull(resource, "Missing fixture src/test/resources" + FIXTURES + name);
        return Path.of(resource.toURI());
    }

    // ---- Swing event thread ---------------------------------------------------------------------

    /** Runs on the event thread and returns the result. Read and change Swing and model state only here. */
    public <T> T onEdt(Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        SwingUtilities.invokeLater(task);
        try {
            return task.get(EDT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException failure) {
            // Rethrow the assertion or exception from the event thread as-is, so JUnit reports it directly.
            Throwable cause = failure.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw failure;
        } catch (TimeoutException timeout) {
            throw new AssertionFailedError("The event thread did not finish within " + EDT_TIMEOUT_SECONDS
                + " seconds. A modal dialog may be waiting for input; see the failure screenshot.", timeout);
        }
    }

    /** Runs on the event thread. */
    public void runOnEdt(EdtAction action) throws Exception {
        onEdt(() -> {
            action.run();
            return null;
        });
    }

    /** Polls on the event thread until the probe returns a value other than null, for work that completes later. */
    public <T> T await(String description, Callable<T> probe) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(AWAIT_TIMEOUT_SECONDS);
        do {
            T value = onEdt(probe);
            if (value != null) {
                return value;
            }
            Thread.sleep(25);
        } while (System.nanoTime() < deadline);
        return fail("Timed out after " + AWAIT_TIMEOUT_SECONDS + " seconds waiting for " + description);
    }

    // ---- User input -----------------------------------------------------------------------------

    /** Clicks a menu item in the main window's menu bar, which runs its action like a user's click. */
    public void menu(String menuText, String itemText) throws Exception {
        runOnEdt(() -> {
            var bar = TAPAALGUI.getAppGui().getJMenuBar();
            var menuTexts = new ArrayList<String>();
            for (int index = 0; index < bar.getMenuCount(); index++) {
                JMenu menu = bar.getMenu(index);
                if (menu == null) {
                    continue;
                }
                menuTexts.add(menu.getText());
                if (!menuText.equals(menu.getText())) {
                    continue;
                }
                var itemTexts = new ArrayList<String>();
                for (int itemIndex = 0; itemIndex < menu.getItemCount(); itemIndex++) {
                    JMenuItem item = menu.getItem(itemIndex);
                    if (item == null) {
                        continue;
                    }
                    if (itemText.equals(item.getText())) {
                        assertTrue(item.isEnabled(), menuText + " > " + itemText + " must be enabled");
                        item.doClick(0);
                        return;
                    }
                    itemTexts.add(item.getText());
                }
                fail("No item " + itemText + " in menu " + menuText + "; it has " + itemTexts);
            }
            fail("No menu " + menuText + " in the menu bar; it has " + menuTexts);
        });
    }

    /** The screen position of the middle of the component's visible part. The lookup runs on the event thread. */
    public Point centerOnScreen(Callable<? extends JComponent> component) throws Exception {
        return onEdt(() -> {
            JComponent target = component.call();
            assertTrue(target.isShowing(), target + " must be showing");
            Rectangle visible = target.getVisibleRect();
            assertFalse(visible.isEmpty(), target + " must be visible on screen");
            var point = new Point(visible.x + visible.width / 2, visible.y + visible.height / 2);
            SwingUtilities.convertPointToScreen(point, target);
            return point;
        });
    }

    /** Moves the real mouse to the screen position and clicks the left button. */
    public void click(Point screen) {
        robot.mouseMove(screen.x, screen.y);
        robot.waitForIdle();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
    }

    /** Presses the left button at the screen position, moves the real mouse by (dx, dy) in steps and releases. */
    public void drag(Point from, int dx, int dy) {
        robot.mouseMove(from.x, from.y);
        robot.waitForIdle();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        for (int step = 1; step <= DRAG_STEPS; step++) {
            robot.mouseMove(from.x + dx * step / DRAG_STEPS, from.y + dy * step / DRAG_STEPS);
            robot.waitForIdle();
        }
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
    }

    /** Drags a named place on the canvas with the real mouse. */
    public void dragPlace(PetriNetTab tab, String name, int dx, int dy) throws Exception {
        drag(centerOnScreen(() -> place(tab, name)), dx, dy);
    }

    /** For input that {@link #click} and {@link #drag} do not cover, such as keys or other buttons. */
    public Robot robot() {
        return robot;
    }

    // ---- Lookups; call these inside onEdt ------------------------------------------------------

    /** The drawing tool with this name, such as "Select" or "Place". */
    public static GuiAction tool(PetriNetTab tab, String name) {
        return tab.getAvailableDrawActions().stream()
            .filter(action -> name.equals(action.getValue(GuiAction.NAME)))
            .findFirst().orElseThrow(() -> new AssertionError("No drawing tool named " + name));
    }

    /** The place on the canvas of the tab's current template with this name. */
    public static TimedPlaceComponent place(PetriNetTab tab, String name) {
        return Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces())
            .map(TimedPlaceComponent.class::cast)
            .filter(place -> name.equals(place.getName()))
            .findFirst().orElseThrow(() -> new AssertionError("No place named " + name + " on the canvas"));
    }

    // ---- Results --------------------------------------------------------------------------------

    /**
     * Asserts that the working copy equals the expected fixture, ignoring only XML syntax that does not
     * change a saved net (see {@link TapnXmlComparison}). On a difference the expected file and the list
     * of differences are written next to the failure screenshot.
     */
    public void assertSavedMatches(String expectedFixture) throws Exception {
        assertSavedMatches(expectedFixture, "The saved document should match the expected document");
    }

    /** As {@link #assertSavedMatches(String)}, with a message that says why the documents should match. */
    public void assertSavedMatches(String expectedFixture, String message) throws Exception {
        var expected = fixture(expectedFixture);
        var differences = TapnXmlComparison.differences(expected, file());
        if (differences.isEmpty()) {
            return;
        }
        Files.createDirectories(artifacts);
        Files.copy(expected, artifacts.resolve("expected.tapn"), StandardCopyOption.REPLACE_EXISTING);
        Files.write(artifacts.resolve("differences.txt"), differences);
        fail(message + ". The saved copy of " + file().getFileName() + " differs from " + FIXTURES + expectedFixture
            + " (artifacts in " + artifacts + "):\n  " + String.join("\n  ", differences));
    }

    /**
     * Failures that production code reported through {@code CaughtExceptions} during this test. Take and
     * assert on the ones a test provokes; any others fail the test after it finishes. The desktop hooks
     * that a Linux virtual display does not support are allowed.
     */
    public CaughtExceptionRecorder caught() {
        return caught;
    }

    // ---- JUnit lifecycle ------------------------------------------------------------------------

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        artifacts = Path.of(System.getProperty("tapaal.smoke.artifacts", "build/reports/gui-smoke"))
            .resolve(context.getRequiredTestClass().getSimpleName())
            .resolve(context.getRequiredTestMethod().getName());
        workDirectory = Files.createTempDirectory("tapaal-gui-smoke");
        file = null;
        robot = new Robot();
        caught.beforeEach(context);
        recording = true;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable failure) throws Throwable {
        captureFailureArtifacts(failure);
        throw failure;
    }

    private void captureFailureArtifacts(Throwable failure) {
        try {
            Files.createDirectories(artifacts);
            var screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            ImageIO.write(robot.createScreenCapture(screen), "png", artifacts.resolve("failure.png").toFile());
            if (file != null && Files.exists(file)) {
                Files.copy(file, artifacts.resolve("saved.tapn"), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Wrote GUI failure artifacts to " + artifacts);
        } catch (Exception artifactError) {
            failure.addSuppressed(artifactError);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            if (recording) {
                recording = false;
                caught.afterEach(context);
            }
        } catch (AssertionFailedError failure) {
            if (context.getExecutionException().isEmpty()) {
                captureFailureArtifacts(failure);
            }
            throw failure;
        } finally {
            try {
                if (file != null) {
                    runOnEdt(() -> {
                        for (var window : Window.getWindows()) {
                            window.dispose();
                        }
                    });
                }
            } finally {
                if (workDirectory != null) {
                    deleteRecursively(workDirectory);
                }
            }
        }
    }

    private static void deleteRecursively(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }
}
