package net.tapaal.gui.smoke;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import net.tapaal.TAPAAL;
import net.tapaal.gui.CaughtExceptionRecorder;
import net.tapaal.gui.TapnXmlComparison;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.AssertionFailedError;
import pipe.gui.StatusBar;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 *     gui.dragObject(tab, "P0", 90, 60);
 *     gui.menu("File", "Save");
 *     gui.assertSavedMatches("my-case/expected.tapn");
 * }
 * }</pre>
 *
 * <p>Every input goes through a route a user can take, so the production handlers run:
 * <ul>
 *   <li>{@link #click}, {@link #doubleClick}, {@link #rightClick}, {@link #drag}, {@link #keys} and
 *       {@link #type} move the real mouse and keyboard with {@link Robot};</li>
 *   <li>{@link #menu} clicks a menu bar item, which runs its Swing action. It returns once the action has
 *       finished or has opened a modal dialog, so a test can then use {@link #awaitDialog};</li>
 *   <li>{@link #open} and {@link #openAll} start the application through {@link TAPAAL#main} with file
 *       arguments, and {@link #openFiles} opens more files through the controller entry point that dropping
 *       files on the window uses. Native file choosers are not driven.</li>
 * </ul>
 * Read editor state inside {@link #onEdt}; the static lookups below are meant to be called there.
 * Do not change the model directly: a test that does no longer checks what a user can do.
 *
 * <p>For each test the harness
 * <ul>
 *   <li>copies fixtures to a temporary directory, so the application never writes to {@code src/test/resources},</li>
 *   <li>records failures that production code catches and continues after ({@link #caught()}) and exceptions
 *       that escape to the Swing event thread ({@link #takeUncaught()}); either fails the test unless taken,</li>
 *   <li>fails a passing test that leaves a dialog open, because a forgotten dialog is often an error message,</li>
 *   <li>on failure writes a screenshot, the working copies, snapshots and a state summary to
 *       {@code build/reports/gui-smoke/<test class>/<test method>/},</li>
 *   <li>disposes all windows afterwards.</li>
 * </ul>
 *
 * <p>The application keeps its frame, tabs and several dialogs in static fields that disposing windows does not
 * reset. The {@code guiSmokeTest} Gradle task therefore starts a fresh JVM, with fresh preferences, for each test
 * class, and the harness refuses to start the application twice in one JVM. Write one {@code @Test} per class.
 */
public final class GuiHarness implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
    /** Fixture names are resolved against this classpath directory, {@code src/test/resources/validation/}. */
    public static final String FIXTURES = "/validation/";
    private static final long EDT_TIMEOUT_SECONDS = 15;
    private static final long AWAIT_TIMEOUT_SECONDS = 10;
    private static final int DRAG_STEPS = 10;

    private static final Path PREFERENCES = isolatePreferences();
    private static String startedBy;

    private final CaughtExceptionRecorder caught = new CaughtExceptionRecorder()
        .allow("TAPAALGUI.init/native-about-handler", UnsupportedOperationException.class, "no native about menu on this desktop")
        .allow("TAPAALGUI.init/native-quit-handler", UnsupportedOperationException.class, "no native quit menu on this desktop")
        .allow("TAPAALGUI.init/taskbar-icon", UnsupportedOperationException.class, "no taskbar on this desktop");
    private final List<Throwable> uncaught = Collections.synchronizedList(new ArrayList<>());

    private Robot robot;
    private Path workDirectory;
    private Path artifacts;
    private final Map<String, Path> workingCopies = new LinkedHashMap<>();
    private final List<Path> snapshots = new ArrayList<>();
    private boolean started;
    private boolean recording;
    private Thread.UncaughtExceptionHandler previousHandler;
    private final ArrayDeque<String> inputTrail = new ArrayDeque<>();
    private final AWTEventListener inputRecorder = this::recordInput;

    /** Code to run on the Swing event thread that returns nothing. */
    @FunctionalInterface
    public interface EdtAction {
        void run() throws Exception;
    }

    /** Test code, such as harness calls, to run while keys are held; see {@link #holding}. */
    @FunctionalInterface
    public interface Steps {
        void run() throws Exception;
    }

    // ---- Starting the application ---------------------------------------------------------------

    /**
     * Copies a fixture from {@link #FIXTURES} to a temporary file, starts TAPAAL on it and returns its tab.
     * {@link #file()} is the working copy that Save writes to.
     */
    public PetriNetTab open(String fixture) throws Exception {
        return openAll(fixture).get(0);
    }

    /**
     * Starts TAPAAL with the working copies of several fixtures as command-line files, like a user opening
     * them from a shell. Returns the tabs in order; the last one is selected.
     */
    public List<PetriNetTab> openAll(String... fixtures) throws Exception {
        assertTrue(fixtures.length > 0, "Name at least one fixture, or use startEmpty()");
        var arguments = new ArrayList<String>();
        for (String fixture : fixtures) {
            arguments.add(workingCopy(fixture).toString());
        }
        start(arguments.toArray(new String[0]));
        return onEdt(() -> {
            var tabs = TAPAALGUI.getAppGuiController().getTabs();
            assertEquals(fixtures.length, tabs.size(), "TAPAAL should open one tab per fixture " + Arrays.toString(fixtures));
            return List.copyOf(tabs);
        });
    }

    /** Starts TAPAAL without a file, as after launching it from a menu. */
    public void startEmpty() throws Exception {
        start(new String[0]);
    }

    private void start(String[] arguments) throws Exception {
        assertFalse(started, "The application is already running in this test");
        String test = artifacts.getParent().getFileName() + "." + artifacts.getFileName();
        assertTrue(startedBy == null, "TAPAAL already ran in this JVM for " + startedBy + " and its static Swing state "
            + "cannot be reset. Put " + test + " in its own test class; guiSmokeTest starts one JVM per class.");
        startedBy = test;
        started = true;
        runOnEdt(() -> {
            TAPAAL.main(arguments);
            assertTrue(TAPAALGUI.getAppGui().isShowing(), "The main window should be showing");
        });
        // The window is mapped and focused asynchronously, and input sent before that can be lost. Without a
        // window manager the focus occasionally never arrives; then click the status bar, which has no handlers,
        // as a user would click an unfocused window.
        if (!waitUntil(3, () -> TAPAALGUI.getAppGui().isFocused())) {
            System.out.println("The main window did not get keyboard focus; clicking its status bar");
            click(centerOf(() -> find(TAPAALGUI.getAppGui().getContentPane(), StatusBar.class, bar -> true)));
            await("the main window to get keyboard focus", () -> TAPAALGUI.getAppGui().isFocused() ? true : null);
        }
        verifyLocation(TAPAALGUI.getAppGui());
        settle();
    }

    /**
     * Checks that Swing knows where the window really is. Without a window manager, Swing occasionally keeps a
     * stale position for a new window, and every computed click point is then off. The check moves the mouse into
     * the window and compares the position that the display reports with Swing's. On a difference it moves the
     * window to where the display has it, which tells Swing the real position, and checks again.
     */
    private void verifyLocation(Window window) throws Exception {
        for (int attempt = 1; ; attempt++) {
            var observed = observeOrigin(window);
            var believed = onEdt(window::getLocationOnScreen);
            if (believed.equals(observed)) {
                return;
            }
            assertTrue(attempt < 3, "Swing places " + describe(window) + " at " + believed + " but the display at " + observed);
            System.out.println("Swing placed " + describe(window) + " at " + believed + " but the display at " + observed
                + "; moving it to tell Swing");
            var target = observed != null ? observed : believed;
            runOnEdt(() -> window.setLocation(target));
            settle();
        }
    }

    /** Where the display has the window, from a mouse event inside it, or null if no event reached it. */
    private Point observeOrigin(Window window) throws Exception {
        var events = new LinkedBlockingDeque<MouseEvent>();
        AWTEventListener listener = event -> {
            if (event instanceof MouseEvent mouse
                && (mouse.getComponent() == window || SwingUtilities.getWindowAncestor(mouse.getComponent()) == window)) {
                events.add(mouse);
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        try {
            // Near the top left corner, which is the window's border or menu bar, so the pointer starts no tooltip.
            var corner = onEdt(() -> {
                var origin = window.getLocationOnScreen();
                return new Point(origin.x + 4, origin.y + 4);
            });
            robot.mouseMove(corner.x, corner.y);
            robot.waitForIdle();
            robot.mouseMove(corner.x + 1, corner.y + 1);
            settle();
            var event = events.peekLast();
            if (event == null) {
                return null;
            }
            return onEdt(() -> {
                var local = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), window);
                return new Point(event.getXOnScreen() - local.x, event.getYOnScreen() - local.y);
            });
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    /** The working copy of the first fixture, which Save writes to. */
    public Path file() {
        assertFalse(workingCopies.isEmpty(), "Call open() first");
        return workingCopies.values().iterator().next();
    }

    /** The working copy of this fixture. */
    public Path file(String fixture) {
        var copy = workingCopies.get(fixture);
        assertNotNull(copy, "No working copy of " + fixture + "; copies: " + workingCopies.keySet());
        return copy;
    }

    /**
     * Copies a fixture into this test's temporary directory, keeping its sub-directory, and returns the copy.
     * Use it for {@link #openFiles}; {@link #open} calls it for you.
     */
    public Path workingCopy(String fixture) throws Exception {
        var existing = workingCopies.get(fixture);
        if (existing != null) {
            return existing;
        }
        var copy = workDirectory.resolve(fixture).normalize();
        assertTrue(copy.startsWith(workDirectory), "Fixture names must stay inside " + FIXTURES + ": " + fixture);
        Files.createDirectories(copy.getParent());
        Files.copy(fixture(fixture), copy);
        workingCopies.put(fixture, copy);
        return copy;
    }

    /**
     * Opens files in the running application through {@code GuiFrameController.openFiles}, the entry point that
     * dropping files on the window uses (File &gt; Open shows a native chooser and then calls the same loader).
     * Waits for the new tabs and returns them.
     */
    public List<PetriNetTab> openFiles(Path... files) throws Exception {
        assertTrue(started, "Start the application first");
        int before = onEdt(() -> TAPAALGUI.getAppGuiController().getTabs().size());
        runOnEdt(() -> TAPAALGUI.getAppGuiController().openFiles(Arrays.stream(files).map(Path::toFile).toList()));
        return await(files.length + " new tab(s) for " + Arrays.toString(files), () -> {
            var tabs = TAPAALGUI.getAppGuiController().getTabs();
            return tabs.size() == before + files.length ? List.copyOf(tabs.subList(before, tabs.size())) : null;
        });
    }

    /** The classpath fixture as a file. */
    public static Path fixture(String name) throws Exception {
        var resource = GuiHarness.class.getResource(FIXTURES + name);
        assertNotNull(resource, "Missing fixture src/test/resources" + FIXTURES + name);
        return Path.of(resource.toURI());
    }

    // ---- Swing event thread ---------------------------------------------------------------------

    /**
     * Runs on the event thread and returns the result. Read Swing and model state only here. Code that may open a
     * modal dialog does not return until the dialog closes, so trigger such code with {@link #menu}, {@link #click}
     * or {@link #keys} instead.
     */
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
                + " seconds. Code passed to onEdt may have opened a modal dialog; trigger it with menu(), click() or keys()"
                + " instead. Event thread:\n" + eventThreadStack(), timeout);
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
        return fail("Timed out after " + AWAIT_TIMEOUT_SECONDS + " seconds waiting for " + description
            + ". Open dialogs: " + onEdt(GuiHarness::openDialogTitles));
    }

    private boolean waitUntil(long seconds, Callable<Boolean> condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (!onEdt(condition)) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            Thread.sleep(25);
        }
        return true;
    }

    /**
     * Waits until the event thread has handled all input so far, then fails if any of it threw. Every input
     * method calls this; call it yourself after triggering work some other way.
     */
    public void settle() throws Exception {
        robot.waitForIdle();
        // A modal dialog runs a nested event loop, so this returns while the dialog is open.
        onEdt(() -> null);
        throwIfUncaught();
    }

    // ---- Menus, toolbar and tools ---------------------------------------------------------------

    /**
     * Clicks a menu bar item, such as {@code menu("File", "Save")} or {@code menu("File", "Export", "PNML")}.
     * The item must exist and be enabled. Returns when its action has finished or has opened a modal dialog.
     */
    public void menu(String... path) throws Exception {
        JMenuItem item = onEdt(() -> {
            JMenuItem found = menuItem(path);
            assertTrue(found.isEnabled(), String.join(" > ", path) + " must be enabled");
            return found;
        });
        SwingUtilities.invokeLater(() -> item.doClick(0));
        settle();
    }

    /** Clicks the main window's toolbar button for the action with this name, such as "Undo", with the real mouse. */
    public void toolbar(String actionName) throws Exception {
        click(centerOf(() -> toolbarButton(actionName)));
    }

    /**
     * Chooses a drawing tool, such as "Place", "Transition", "Arc" or "Select", from the Draw menu and checks
     * that the tab's tool changed. {@link #keys} with the tool's shortcut, such as {@code keys("P")}, is the
     * keyboard route.
     */
    public void selectTool(PetriNetTab tab, String name) throws Exception {
        menu("Draw", name);
        assertTrue(onEdt(() -> tool(tab, name).isSelected()), "Choosing Draw > " + name + " should select the tool");
    }

    // ---- Keyboard -------------------------------------------------------------------------------

    /**
     * Presses and releases a key with modifiers, written as for {@link KeyStroke#getKeyStroke(String)}: for
     * example {@code "ctrl Z"}, {@code "DELETE"}, {@code "shift G"} or {@code "P"}. TAPAAL uses ctrl as the menu
     * shortcut key on Linux. The focused window receives the key.
     */
    public void keys(String keyStroke) throws Exception {
        assertKeyboardFocus();
        var stroke = KeyStroke.getKeyStroke(keyStroke);
        assertNotNull(stroke, "Not a key stroke: " + keyStroke);
        assertTrue(stroke.getKeyCode() != KeyEvent.VK_UNDEFINED, "Use type() for typed characters: " + keyStroke);
        var modifiers = modifierKeys(stroke.getModifiers());
        modifiers.forEach(robot::keyPress);
        robot.keyPress(stroke.getKeyCode());
        robot.keyRelease(stroke.getKeyCode());
        Collections.reverse(modifiers);
        modifiers.forEach(robot::keyRelease);
        settle();
    }

    /** Types text with the real keyboard into the focused component. Letters, digits, space and {@code .-_:,/()[]<>=+} work. */
    public void type(String text) throws Exception {
        assertKeyboardFocus();
        for (char character : text.toCharArray()) {
            typeCharacter(character);
        }
        settle();
    }

    private void assertKeyboardFocus() throws Exception {
        assertTrue(onEdt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() != null),
            "No window has keyboard focus, so keys would go nowhere. After a dialog closes call awaitClosed(dialog),"
                + " which returns focus to its owner as a window manager would.");
    }

    /** Holds modifier keys, such as {@code "shift"} or {@code "ctrl"}, while the steps run; for shift-click and similar. */
    public void holding(String modifiers, Steps steps) throws Exception {
        var stroke = KeyStroke.getKeyStroke(modifiers + " A");
        assertNotNull(stroke, "Not modifier names: " + modifiers);
        var keys = modifierKeys(stroke.getModifiers());
        keys.forEach(robot::keyPress);
        try {
            robot.waitForIdle();
            steps.run();
        } finally {
            Collections.reverse(keys);
            keys.forEach(robot::keyRelease);
            settle();
        }
    }

    /**
     * Clicks a text field, or a spinner's text field, selects its text with ctrl A and types the new text. A
     * formatted field or spinner commits the text when it loses focus, for example when the test clicks OK.
     */
    public void typeInto(Component field, String text) throws Exception {
        var target = field instanceof JSpinner spinner ? onEdt(() -> textFieldOf(spinner)) : field;
        assertTrue(target instanceof JTextComponent, "typeInto needs a text field or spinner, not " + describe(field));
        click(centerOf(() -> (JComponent) target));
        await("keyboard focus on " + describe(target), () -> target.isFocusOwner() ? true : null);
        keys("ctrl A");
        type(text);
    }

    // ---- Mouse ----------------------------------------------------------------------------------

    /** Moves the real mouse to the screen position and clicks the left button. */
    public void click(Point screen) throws Exception {
        press(screen, InputEvent.BUTTON1_DOWN_MASK, 1);
    }

    /** Double-clicks the left button at the screen position, which for example opens a place's editor. */
    public void doubleClick(Point screen) throws Exception {
        press(screen, InputEvent.BUTTON1_DOWN_MASK, 2);
    }

    /** Clicks the right button at the screen position, which opens a popup menu on canvas objects. */
    public void rightClick(Point screen) throws Exception {
        press(screen, InputEvent.BUTTON3_DOWN_MASK, 1);
    }

    /** Presses the left button at the screen position, moves the real mouse by (dx, dy) in steps and releases. */
    public void drag(Point from, int dx, int dy) throws Exception {
        robot.mouseMove(from.x, from.y);
        robot.waitForIdle();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        for (int step = 1; step <= DRAG_STEPS; step++) {
            robot.mouseMove(from.x + dx * step / DRAG_STEPS, from.y + dy * step / DRAG_STEPS);
            robot.waitForIdle();
        }
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        settle();
    }

    private void press(Point screen, int button, int count) throws Exception {
        robot.mouseMove(screen.x, screen.y);
        robot.waitForIdle();
        for (int click = 0; click < count; click++) {
            // Let the event thread react to the press, as it does between a person's press and release. A focus
            // change starts on press, and a spinner commits its text only once it has lost focus.
            robot.mousePress(button);
            robot.waitForIdle();
            robot.mouseRelease(button);
            robot.waitForIdle();
        }
        settle();
    }

    /** The screen position of the middle of the component's visible part. The lookup runs on the event thread. */
    public Point centerOf(Callable<? extends JComponent> component) throws Exception {
        return onEdt(() -> {
            JComponent target = component.call();
            assertTrue(target.isShowing(), describe(target) + " must be showing");
            Rectangle visible = target.getVisibleRect();
            assertFalse(visible.isEmpty(), describe(target) + " must be visible on screen");
            var point = new Point(visible.x + visible.width / 2, visible.y + visible.height / 2);
            SwingUtilities.convertPointToScreen(point, target);
            assertReaches(point, target, true);
            return point;
        });
    }

    /** Same as {@link #centerOf}; kept for existing tests. */
    public Point centerOnScreen(Callable<? extends JComponent> component) throws Exception {
        return centerOf(component);
    }

    /** The screen position of the middle of a list row, for clicking a component in the components panel. */
    public Point centerOfRow(JList<?> list, int row) throws Exception {
        return onEdt(() -> {
            assertTrue(row >= 0 && row < list.getModel().getSize(), "No row " + row + " in " + describe(list));
            list.ensureIndexIsVisible(row);
            Rectangle cell = list.getCellBounds(row, row);
            var point = new Point(cell.x + cell.width / 2, cell.y + cell.height / 2);
            SwingUtilities.convertPointToScreen(point, list);
            assertReaches(point, list, true);
            return point;
        });
    }

    /** For input the methods above do not cover. Call {@link #settle} afterwards. */
    public Robot robot() {
        return robot;
    }

    // ---- Canvas ---------------------------------------------------------------------------------

    /**
     * The screen position of the middle of a named place or transition in the tab's current component. Fails if
     * the object is scrolled out of view or another canvas object would receive the click.
     */
    public Point objectPoint(PetriNetTab tab, String name) throws Exception {
        return onEdt(() -> {
            PlaceTransitionObject object = object(tab, name);
            var surface = tab.drawingSurface();
            var center = new Point(object.getWidth() / 2, object.getHeight() / 2);
            assertTrue(object.contains(center.x, center.y), name + " does not contain its own middle");
            var onSurface = SwingUtilities.convertPoint(object, center, surface);
            assertTrue(surface.getVisibleRect().contains(onSurface), name + " at " + onSurface
                + " is outside the visible canvas " + surface.getVisibleRect() + "; scroll or choose a fixture that fits");
            SwingUtilities.convertPointToScreen(center, object);
            assertReaches(center, object, true);
            return center;
        });
    }

    /**
     * The screen position of a point on the tab's canvas, in model coordinates (the positions saved in the file).
     * Fails if the point is outside the visible canvas.
     */
    public Point canvasPoint(PetriNetTab tab, int x, int y) throws Exception {
        return onEdt(() -> {
            var surface = tab.drawingSurface();
            double scale = surface.getZoom() / 100.0;
            var point = new Point((int) Math.round(x * scale), (int) Math.round(y * scale));
            assertTrue(surface.getVisibleRect().contains(point),
                "(" + x + ", " + y + ") is outside the visible canvas " + surface.getVisibleRect());
            SwingUtilities.convertPointToScreen(point, surface);
            assertReaches(point, surface, false);
            return point;
        });
    }

    /** Clicks a named place or transition with the real mouse. In simulation mode this fires an enabled transition. */
    public void clickObject(PetriNetTab tab, String name) throws Exception {
        click(objectPoint(tab, name));
    }

    /** Double-clicks a named place or transition, which opens its editor dialog in the editor. */
    public void doubleClickObject(PetriNetTab tab, String name) throws Exception {
        doubleClick(objectPoint(tab, name));
    }

    /** Right-clicks a named place or transition, which opens its popup menu; see {@link #clickPopupItem}. */
    public void rightClickObject(PetriNetTab tab, String name) throws Exception {
        rightClick(objectPoint(tab, name));
    }

    /** Drags a named place or transition by (dx, dy) screen pixels with the real mouse. */
    public void dragObject(PetriNetTab tab, String name, int dx, int dy) throws Exception {
        drag(objectPoint(tab, name), dx, dy);
    }

    /** Same as {@link #dragObject}; kept for existing tests. */
    public void dragPlace(PetriNetTab tab, String name, int dx, int dy) throws Exception {
        dragObject(tab, name, dx, dy);
    }

    /** Clicks a point on the canvas given in model coordinates, for example with the Place tool. */
    public void clickCanvas(PetriNetTab tab, int x, int y) throws Exception {
        click(canvasPoint(tab, x, y));
    }

    // ---- Tabs -----------------------------------------------------------------------------------

    /** The open tabs in order. */
    public List<PetriNetTab> tabs() throws Exception {
        return onEdt(() -> List.copyOf(TAPAALGUI.getAppGuiController().getTabs()));
    }

    /** The selected tab, or null when none is open. */
    public PetriNetTab currentTab() throws Exception {
        return onEdt(() -> (PetriNetTab) tabbedPane().getSelectedComponent());
    }

    /** Clicks the tab's title with the real mouse and checks that it became the selected tab. */
    public void selectTab(PetriNetTab tab) throws Exception {
        var title = centerOf(() -> {
            var pane = tabbedPane();
            int index = pane.indexOfComponent(tab);
            assertTrue(index >= 0, "The tab " + tab.getTabTitle() + " is not open");
            return find((Container) pane.getTabComponentAt(index), JLabel.class, label -> true);
        });
        click(title);
        assertTrue(onEdt(() -> tabbedPane().getSelectedComponent() == tab), () -> "Clicking the title of "
            + tab.getTabTitle() + " at " + title + " should select it; the mouse reached " + componentUnderMouse());
    }

    // ---- Dialogs and popup menus ----------------------------------------------------------------

    /** Waits for a showing Swing dialog with this title, such as "Edit Place", and returns it. */
    public JDialog awaitDialog(String title) throws Exception {
        JDialog found = await("a dialog titled \"" + title + "\"", () -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof Dialog dialog && dialog.isShowing() && title.equals(dialog.getTitle())) {
                    assertTrue(dialog instanceof JDialog, "\"" + title + "\" is a native dialog, which the harness cannot drive");
                    return (JDialog) dialog;
                }
            }
            return null;
        });
        verifyLocation(found);
        return found;
    }

    /**
     * Waits until the dialog is no longer showing and its owner window has keyboard focus again. The virtual
     * display has no window manager to return focus to the owner, as a desktop does, so the harness asks for it;
     * without this, keys typed after a dialog closes go nowhere.
     */
    public void awaitClosed(Dialog dialog) throws Exception {
        await("\"" + dialog.getTitle() + "\" to close", () -> dialog.isShowing() ? null : true);
        Window owner = dialog.getOwner();
        if (owner != null && onEdt(owner::isShowing)) {
            if (onEdt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() == null)) {
                runOnEdt(owner::requestFocus);
            }
            await("keyboard focus to return to the window that owned \"" + dialog.getTitle() + "\"",
                () -> owner.isFocused() ? true : null);
        }
    }

    /** Clicks the enabled button with this text inside the container, such as a dialog's "OK", with the real mouse. */
    public void clickButton(Container container, String text) throws Exception {
        click(centerOf(() -> {
            var button = button(container, text);
            assertTrue(button.isEnabled(), "The button " + text + " must be enabled");
            return button;
        }));
    }

    /** Clicks an item of the showing popup menu with the real mouse, such as "Delete" after {@link #rightClickObject}. */
    public void clickPopupItem(String text) throws Exception {
        JPopupMenu popup = await("a popup menu; the current tool may not open one", () -> {
            var popups = new ArrayList<JPopupMenu>();
            for (Window window : Window.getWindows()) {
                if (window.isShowing()) {
                    popups.addAll(findAll(window, JPopupMenu.class, JPopupMenu::isShowing));
                }
            }
            assertTrue(popups.size() <= 1, "More than one popup menu is showing");
            return popups.isEmpty() ? null : popups.get(0);
        });
        click(centerOf(() -> {
            var item = Arrays.stream(popup.getSubElements())
                .filter(element -> element instanceof JMenuItem menuItem && text.equals(menuItem.getText()))
                .map(JMenuItem.class::cast).findFirst()
                .orElseThrow(() -> new AssertionError("No popup item " + text + "; it has " + itemTexts(popup)));
            assertTrue(item.isEnabled(), "The popup item " + text + " must be enabled");
            return item;
        }));
    }

    /** The text a dialog shows: a {@link JOptionPane} message, or else its labels. Call inside {@link #onEdt}. */
    public static String dialogText(Container dialog) {
        var panes = findAll(dialog, JOptionPane.class, pane -> true);
        if (!panes.isEmpty()) {
            return String.valueOf(panes.get(0).getMessage());
        }
        return findAll(dialog, JLabel.class, JLabel::isShowing).stream()
            .map(JLabel::getText).filter(text -> text != null && !text.isBlank())
            .collect(Collectors.joining("\n"));
    }

    // ---- Lookups; call these inside onEdt ------------------------------------------------------

    /** The menu bar item at this path; see {@link #menu}. */
    public static JMenuItem menuItem(String... path) {
        assertTrue(path.length >= 2, "A menu path names a menu and an item");
        JMenuBar bar = TAPAALGUI.getAppGui().getJMenuBar();
        var menus = new ArrayList<String>();
        JMenu menu = null;
        for (int index = 0; index < bar.getMenuCount(); index++) {
            JMenu candidate = bar.getMenu(index);
            if (candidate != null) {
                menus.add(candidate.getText());
                if (path[0].equals(candidate.getText())) {
                    menu = candidate;
                }
            }
        }
        assertNotNull(menu, "No menu " + path[0] + " in the menu bar; it has " + menus);
        JMenuItem item = menu;
        for (int level = 1; level < path.length; level++) {
            assertTrue(item instanceof JMenu, String.join(" > ", Arrays.copyOf(path, level)) + " is not a menu");
            JMenu current = (JMenu) item;
            item = null;
            var items = new ArrayList<String>();
            for (int index = 0; index < current.getItemCount(); index++) {
                JMenuItem candidate = current.getItem(index);
                if (candidate != null) {
                    items.add(candidate.getText());
                    if (path[level].equals(candidate.getText())) {
                        item = candidate;
                    }
                }
            }
            assertNotNull(item, "No item " + path[level] + " in " + String.join(" > ", Arrays.copyOf(path, level)) + "; it has " + items);
        }
        return item;
    }

    /** The main window's toolbar button for the action with this name. */
    public static AbstractButton toolbarButton(String actionName) {
        var bar = TAPAALGUI.getAppGui().getJMenuBar();
        return find(TAPAALGUI.getAppGui().getContentPane(), AbstractButton.class, button -> button.getAction() != null
            && actionName.equals(button.getAction().getValue(GuiAction.NAME))
            && !SwingUtilities.isDescendingFrom(button, bar) && !(button instanceof JMenuItem));
    }

    /** The main window's tabbed pane that holds the open nets. */
    public static JTabbedPane tabbedPane() {
        return find(TAPAALGUI.getAppGui().getContentPane(), JTabbedPane.class,
            pane -> SwingUtilities.getAncestorOfClass(PetriNetTab.class, pane) == null);
    }

    /** The drawing tool with this name, such as "Select" or "Place". */
    public static GuiAction tool(PetriNetTab tab, String name) {
        return tab.getAvailableDrawActions().stream()
            .filter(action -> name.equals(action.getValue(GuiAction.NAME)))
            .findFirst().orElseThrow(() -> new AssertionError("No drawing tool named " + name + "; the tab has "
                + tab.getAvailableDrawActions().stream().map(action -> action.getValue(GuiAction.NAME)).toList()));
    }

    /** The place with this name on the canvas of the tab's current component. */
    public static TimedPlaceComponent place(PetriNetTab tab, String name) {
        return (TimedPlaceComponent) Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces())
            .filter(place -> name.equals(place.getName()))
            .findFirst().orElseThrow(() -> new AssertionError("No place named " + name + " on the canvas; it has " + canvasNames(tab)));
    }

    /** The transition with this name on the canvas of the tab's current component. */
    public static TimedTransitionComponent transition(PetriNetTab tab, String name) {
        return (TimedTransitionComponent) Arrays.stream(tab.drawingSurface().getGuiModel().getTransitions())
            .filter(transition -> name.equals(transition.getName()))
            .findFirst().orElseThrow(() -> new AssertionError("No transition named " + name + " on the canvas; it has " + canvasNames(tab)));
    }

    /** The place or transition with this name on the canvas of the tab's current component. */
    public static PlaceTransitionObject object(PetriNetTab tab, String name) {
        return Stream.concat(Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces()),
                Arrays.stream(tab.drawingSurface().getGuiModel().getTransitions()))
            .filter(object -> name.equals(object.getName()))
            .map(PlaceTransitionObject.class::cast)
            .findFirst().orElseThrow(() -> new AssertionError("No place or transition named " + name + " on the canvas; it has " + canvasNames(tab)));
    }

    /** The one showing component of this type below the root that the predicate accepts. */
    public static <T extends Component> T find(Container root, Class<T> type, Predicate<? super T> predicate) {
        var matches = findAll(root, type, component -> component.isShowing() && predicate.test(component));
        assertEquals(1, matches.size(), () -> "Expected one showing " + type.getSimpleName() + " in " + describe(root)
            + " but found " + matches.stream().map(GuiHarness::describe).toList());
        return matches.get(0);
    }

    /** All components of this type below the root, including hidden ones, that the predicate accepts. */
    public static <T extends Component> List<T> findAll(Container root, Class<T> type, Predicate<? super T> predicate) {
        var matches = new ArrayList<T>();
        collect(root, type, predicate, matches);
        return matches;
    }

    /** The one showing button with this text below the container. */
    public static AbstractButton button(Container container, String text) {
        return find(container, AbstractButton.class, button -> text.equals(button.getText()) && !(button instanceof JMenuItem));
    }

    /**
     * The showing component of this type that a label with this text names: the nearest one to its right in the
     * same row, or else the nearest one directly below it. For example {@code labelled(dialog, "Name:", JTextField.class)}.
     * Uses the laid-out positions, so the dialog must be showing.
     */
    public static <T extends Component> T labelled(Container container, String labelText, Class<T> type) {
        JLabel label = find(container, JLabel.class, candidate -> labelText.equals(candidate.getText()));
        Rectangle labelBounds = label.getBounds();
        var candidates = Arrays.stream(label.getParent().getComponents())
            .filter(type::isInstance).map(type::cast).filter(Component::isShowing).toList();
        int middle = labelBounds.y + labelBounds.height / 2;
        var right = candidates.stream()
            .filter(component -> component.getX() >= labelBounds.x + labelBounds.width
                && component.getY() <= middle && middle <= component.getY() + component.getHeight())
            .min(Comparator.comparingInt(Component::getX));
        if (right.isPresent()) {
            return right.get();
        }
        return candidates.stream()
            .filter(component -> component.getY() >= labelBounds.y + labelBounds.height
                && component.getX() < labelBounds.x + labelBounds.width && labelBounds.x < component.getX() + component.getWidth())
            .min(Comparator.comparingInt(Component::getY))
            .orElseThrow(() -> new AssertionError("No " + type.getSimpleName() + " right of or below the label " + labelText));
    }

    // ---- Editor state ---------------------------------------------------------------------------

    /** Place name to token count in the tab's current component, after checking that canvas and model agree. */
    public Map<String, Integer> marking(PetriNetTab tab) throws Exception {
        return onEdt(() -> {
            var model = new TreeMap<String, Integer>();
            for (TimedPlace place : model(tab).places()) {
                model.put(place.name(), place.numberOfTokens());
            }
            var canvas = new TreeMap<String, Integer>();
            for (var place : tab.drawingSurface().getGuiModel().getPlaces()) {
                canvas.put(place.getName(), ((TimedPlaceComponent) place).getNumberOfTokens());
            }
            assertEquals(model, canvas, "Canvas and model markings must agree");
            return model;
        });
    }

    /** The names of the transitions in the tab's current component, after checking that canvas and model agree. */
    public List<String> transitionNames(PetriNetTab tab) throws Exception {
        return onEdt(() -> {
            var model = model(tab).transitions().stream().map(TimedTransition::name).collect(Collectors.toCollection(TreeSet::new));
            var canvas = Arrays.stream(tab.drawingSurface().getGuiModel().getTransitions())
                .map(PlaceTransitionObject::getName).collect(Collectors.toCollection(TreeSet::new));
            assertEquals(model, canvas, "Canvas and model transitions must agree");
            return List.copyOf(model);
        });
    }

    /**
     * The arcs on the tab's current canvas as {@code "source -> target"}, sorted, after checking that the canvas
     * draws as many arcs as the model has (a transport arc is drawn as two).
     */
    public List<String> arcs(PetriNetTab tab) throws Exception {
        return onEdt(() -> {
            var model = model(tab);
            long modelArcs = count(model.inputArcs()) + count(model.outputArcs()) + count(model.inhibitorArcs())
                + 2 * count(model.transportArcs());
            var canvas = Arrays.stream(tab.drawingSurface().getGuiModel().getArcs())
                .map(arc -> arc.getSource().getName() + " -> " + arc.getTarget().getName())
                .sorted().toList();
            assertEquals(modelArcs, canvas.size(), "The canvas should draw every arc of the model: " + canvas);
            return canvas;
        });
    }

    private static TimedArcPetriNet model(PetriNetTab tab) {
        return tab.currentTemplate().model();
    }

    private static long count(Iterable<?> items) {
        return StreamSupport.stream(items.spliterator(), false).count();
    }

    // ---- Saved documents and snapshots ----------------------------------------------------------

    /**
     * Writes the tab's whole net, with all components and queries, to a new file with the writer that Save uses,
     * without changing the tab's file, dirty state or undo history. Returns the file; it is kept as a failure artifact.
     */
    public Path snapshot(PetriNetTab tab) throws Exception {
        var file = workDirectory.resolve("snapshots").resolve("snapshot-" + (snapshots.size() + 1) + ".tapn");
        Files.createDirectories(file.getParent());
        runOnEdt(() -> tab.writeNetToFile(file.toFile()));
        assertTrue(Files.size(file) > 0, "The writer produced an empty snapshot");
        snapshots.add(file);
        return file;
    }

    /** Asserts that a {@link #snapshot} of the tab equals the expected fixture; see {@link #assertFileMatches}. */
    public void assertSnapshotMatches(PetriNetTab tab, String expectedFixture, String message) throws Exception {
        assertFileMatches(snapshot(tab), expectedFixture, message);
    }

    /** Asserts that the working copy of the first fixture equals the expected fixture. */
    public void assertSavedMatches(String expectedFixture) throws Exception {
        assertSavedMatches(expectedFixture, "The saved document should match the expected document");
    }

    /** As {@link #assertSavedMatches(String)}, with a message that says why the documents should match. */
    public void assertSavedMatches(String expectedFixture, String message) throws Exception {
        assertFileMatches(file(), expectedFixture, message);
    }

    /**
     * Asserts that a document equals the expected fixture, ignoring only XML syntax that does not change a saved
     * net (see {@link TapnXmlComparison}). On a difference the actual and expected files and the list of differences
     * are written next to the failure screenshot. If the expected fixture does not exist yet, the test fails and
     * the actual document is written there for review, so a new expected file is never accepted unread.
     */
    public void assertFileMatches(Path actual, String expectedFixture, String message) throws Exception {
        String name = Path.of(expectedFixture).getFileName().toString().replaceFirst("\\.tapn$", "");
        var resource = GuiHarness.class.getResource(FIXTURES + expectedFixture);
        if (resource == null) {
            Files.createDirectories(artifacts);
            var proposal = artifacts.resolve(name + ".actual.tapn");
            Files.copy(actual, proposal, StandardCopyOption.REPLACE_EXISTING);
            fail("Missing expected fixture src/test/resources" + FIXTURES + expectedFixture + ". Review " + proposal
                + " and copy it there if it is right.");
        }
        var expected = Path.of(resource.toURI());
        var differences = TapnXmlComparison.differences(expected, actual);
        if (differences.isEmpty()) {
            return;
        }
        Files.createDirectories(artifacts);
        Files.copy(actual, artifacts.resolve(name + ".actual.tapn"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(expected, artifacts.resolve(name + ".expected.tapn"), StandardCopyOption.REPLACE_EXISTING);
        Files.write(artifacts.resolve(name + ".differences.txt"), differences);
        fail(message + ". " + actual.getFileName() + " differs from " + FIXTURES + expectedFixture
            + " (artifacts in " + artifacts + "):\n  " + String.join("\n  ", differences));
    }

    // ---- Recorded failures ----------------------------------------------------------------------

    /**
     * Failures that production code reported through {@code CaughtExceptions} during this test. Take and
     * assert on the ones a test provokes; any others fail the test after it finishes. The desktop hooks
     * that a Linux virtual display does not support are allowed.
     */
    public CaughtExceptionRecorder caught() {
        return caught;
    }

    /**
     * Removes and returns the exceptions that escaped to the Swing event thread so far. Input methods fail on
     * the first such exception; take them only in a test that provokes one on purpose.
     */
    public List<Throwable> takeUncaught() {
        synchronized (uncaught) {
            var taken = List.copyOf(uncaught);
            uncaught.clear();
            return taken;
        }
    }

    private void throwIfUncaught() {
        var escaped = takeUncaught();
        if (!escaped.isEmpty()) {
            var error = new AssertionFailedError("An exception escaped to the Swing event thread: " + escaped.get(0), escaped.get(0));
            escaped.stream().skip(1).forEach(error::addSuppressed);
            throw error;
        }
    }

    // ---- JUnit lifecycle ------------------------------------------------------------------------

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        artifacts = Path.of(System.getProperty("tapaal.smoke.artifacts", "build/reports/gui-smoke"))
            .resolve(context.getRequiredTestClass().getSimpleName())
            .resolve(context.getRequiredTestMethod().getName());
        workDirectory = Files.createTempDirectory("tapaal-gui-smoke");
        workingCopies.clear();
        snapshots.clear();
        uncaught.clear();
        started = false;
        robot = new Robot();
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, failure) -> {
            failure.printStackTrace();
            uncaught.add(failure);
        });
        synchronized (inputTrail) {
            inputTrail.clear();
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(inputRecorder,
            AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
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
            for (var copy : workingCopies.entrySet()) {
                if (Files.exists(copy.getValue())) {
                    var target = artifacts.resolve("working").resolve(copy.getKey());
                    Files.createDirectories(target.getParent());
                    Files.copy(copy.getValue(), target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (workingCopies.size() == 1) {
                Files.copy(file(), artifacts.resolve("saved.tapn"), StandardCopyOption.REPLACE_EXISTING);
            }
            for (var snapshot : snapshots) {
                Files.copy(snapshot, artifacts.resolve(snapshot.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(artifacts.resolve("state.txt"), stateSummary());
            System.out.println("Wrote GUI failure artifacts to " + artifacts);
        } catch (Exception artifactError) {
            failure.addSuppressed(artifactError);
        }
    }

    private String stateSummary() {
        var summary = new StringBuilder("Screen: ").append(Toolkit.getDefaultToolkit().getScreenSize()).append('\n');
        summary.append("Preferences: ").append(PREFERENCES).append('\n');
        if (!started) {
            return summary.append("The application was not started\n").toString();
        }
        try {
            summary.append(onEdt(() -> {
                var text = new StringBuilder();
                var current = tabbedPane().getSelectedComponent();
                for (var tab : TAPAALGUI.getAppGuiController().getTabs()) {
                    text.append(tab == current ? "* " : "  ").append("tab ").append(tab.getTabTitle())
                        .append(" file=").append(tab.getFile()).append(" dirty=").append(tab.getNetChanged())
                        .append(" simulation=").append(tab.isInAnimationMode())
                        .append(" tool=").append(tab.getAvailableDrawActions().stream().filter(GuiAction::isSelected)
                            .map(action -> action.getValue(GuiAction.NAME)).toList()).append('\n');
                }
                for (Window window : Window.getWindows()) {
                    if (window instanceof Dialog dialog && dialog.isShowing()) {
                        text.append("dialog \"").append(dialog.getTitle()).append("\": ").append(shorten(dialogText(dialog))).append('\n');
                    }
                }
                text.append("focus: ").append(describe(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner())).append('\n');
                return text;
            }));
        } catch (Throwable error) {
            summary.append("Could not read the editor state: ").append(error).append('\n').append(eventThreadStack());
        }
        synchronized (inputTrail) {
            summary.append("Last input, window focus and window move events, oldest first:\n  ").append(String.join("\n  ", inputTrail)).append('\n');
        }
        return summary.toString();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        boolean passed = context.getExecutionException().isEmpty();
        try {
            if (recording) {
                recording = false;
                AssertionFailedError failure = null;
                try {
                    if (passed && started) {
                        settle();
                        assertNoOpenDialogs();
                    }
                    throwIfUncaught();
                } catch (AssertionFailedError found) {
                    failure = found;
                }
                try {
                    caught.afterEach(context);
                } catch (AssertionFailedError found) {
                    if (failure == null) {
                        failure = found;
                    } else {
                        failure.addSuppressed(found);
                    }
                }
                if (failure != null) {
                    if (passed) {
                        captureFailureArtifacts(failure);
                    }
                    throw failure;
                }
            }
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previousHandler);
            Toolkit.getDefaultToolkit().removeAWTEventListener(inputRecorder);
            try {
                if (started) {
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

    private void assertNoOpenDialogs() throws Exception {
        var open = onEdt(() -> Arrays.stream(Window.getWindows())
            .filter(window -> window instanceof Dialog && window.isShowing())
            .map(window -> "\"" + ((Dialog) window).getTitle() + "\": " + shorten(dialogText((Dialog) window)))
            .toList());
        if (!open.isEmpty()) {
            throw new AssertionFailedError("The test left dialogs open; close the ones it opens and check any"
                + " unexpected message:\n  " + String.join("\n  ", open));
        }
    }

    // ---- Helpers --------------------------------------------------------------------------------

    private static Path isolatePreferences() {
        // TAPAAL reads and writes java.util.prefs. Give each JVM its own store, before the application loads,
        // so a test that changes a preference cannot change the next test class.
        try {
            var base = System.getProperty("tapaal.smoke.preferences");
            Path directory = base == null
                ? Files.createTempDirectory("tapaal-smoke-preferences")
                : Files.createTempDirectory(Files.createDirectories(Path.of(base)), "jvm-");
            System.setProperty("java.util.prefs.userRoot", directory.toString());
            return directory;
        } catch (IOException failure) {
            throw new IllegalStateException("Could not create a preferences directory", failure);
        }
    }

    /** Keeps the last mouse presses, clicks, keys, window focus changes and window moves for failure reports. */
    private void recordInput(AWTEvent event) {
        String text;
        if (event instanceof MouseEvent mouse) {
            if (mouse.getID() == MouseEvent.MOUSE_MOVED) {
                return;
            }
            text = mouse.paramString();
        } else if (event instanceof KeyEvent key) {
            text = key.paramString().replaceAll(",(scancode|extendedKeyCode|rawCode|primaryLevelUnicode|keyLocation)=[^,]*", "");
        } else if (event instanceof WindowEvent || event.getID() == ComponentEvent.COMPONENT_MOVED && event.getSource() instanceof Window) {
            text = event.paramString().split(",", 2)[0] + " bounds=" + ((Window) event.getSource()).getBounds();
        } else {
            return;
        }
        synchronized (inputTrail) {
            if (inputTrail.size() == 60) {
                inputTrail.removeFirst();
            }
            inputTrail.addLast(text + " on " + describe((Component) event.getSource()));
        }
    }

    /**
     * Fails unless a click at the screen point would reach the target, or one of its children if allowed: no other
     * window, popup or tooltip covers it and no other component of the target's window is on top.
     */
    private static void assertReaches(Point screen, Component target, boolean orChild) {
        Window window = SwingUtilities.getWindowAncestor(target);
        for (Window other : Window.getWindows()) {
            if (other != window && other.isShowing() && other.getBounds().contains(screen) && !isOwnerOf(other, window)) {
                fail("A click on " + describe(target) + " at " + screen + " would reach the window " + describe(other)
                    + " that covers it");
            }
        }
        var root = SwingUtilities.getRootPane(target);
        var point = new Point(screen);
        SwingUtilities.convertPointFromScreen(point, root);
        Component top = SwingUtilities.getDeepestComponentAt(root, point.x, point.y);
        assertTrue(top == target || orChild && top != null && SwingUtilities.isDescendingFrom(top, target),
            () -> "A click on " + describe(target) + " at " + screen + " would reach " + describe(top) + " on top of it");
    }

    private static boolean isOwnerOf(Window candidate, Window window) {
        for (Window owner = window; owner != null; owner = owner.getOwner()) {
            if (owner == candidate) {
                return true;
            }
        }
        return false;
    }

    private String componentUnderMouse() {
        try {
            return onEdt(() -> {
                var location = MouseInfo.getPointerInfo().getLocation();
                var frame = TAPAALGUI.getAppGui();
                SwingUtilities.convertPointFromScreen(location, frame);
                return describe(SwingUtilities.getDeepestComponentAt(frame, location.x, location.y)) + " at " + location;
            });
        } catch (Exception error) {
            return "unknown (" + error + ")";
        }
    }

    private static List<String> openDialogTitles() {
        return Arrays.stream(Window.getWindows())
            .filter(window -> window instanceof Dialog && window.isShowing())
            .map(window -> ((Dialog) window).getTitle()).toList();
    }

    /** The event thread's stack without JDK frames, which shows the TAPAAL and test code it is stuck in. */
    private static String eventThreadStack() {
        return Thread.getAllStackTraces().entrySet().stream()
            .filter(entry -> entry.getKey().getName().startsWith("AWT-EventQueue"))
            .map(entry -> entry.getKey().getName() + " (JDK frames left out)\n    at " + Arrays.stream(entry.getValue())
                .filter(frame -> !frame.getClassName().matches("(java|javax|jdk|sun|com\\.sun)\\..*"))
                .limit(30).map(String::valueOf).collect(Collectors.joining("\n    at ")))
            .collect(Collectors.joining("\n"));
    }

    private static String shorten(String text) {
        String line = text.replaceAll("\\s+", " ").trim();
        return line.length() <= 200 ? line : line.substring(0, 200) + "...";
    }

    private static String canvasNames(PetriNetTab tab) {
        var model = tab.drawingSurface().getGuiModel();
        return Stream.concat(Arrays.stream(model.getPlaces()), Arrays.stream(model.getTransitions()))
            .map(PlaceTransitionObject::getName).sorted().toList().toString();
    }

    private static List<String> itemTexts(MenuElement menu) {
        return Arrays.stream(menu.getSubElements())
            .filter(JMenuItem.class::isInstance).map(element -> ((JMenuItem) element).getText()).toList();
    }

    private static JTextComponent textFieldOf(JSpinner spinner) {
        assertTrue(spinner.getEditor() instanceof JSpinner.DefaultEditor, "The spinner has no text field");
        return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
    }

    private static <T extends Component> void collect(Component component, Class<T> type, Predicate<? super T> predicate, List<T> matches) {
        if (type.isInstance(component) && predicate.test(type.cast(component))) {
            matches.add(type.cast(component));
        }
        if (component instanceof JMenu menu) {
            // Menu items live in the popup menu, which is not a child component while the menu is closed.
            collect(menu.getPopupMenu(), type, predicate, matches);
        } else if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                collect(child, type, predicate, matches);
            }
        }
    }

    private static String describe(Component component) {
        if (component == null) {
            return "nothing";
        }
        String text = component instanceof AbstractButton button ? button.getText()
            : component instanceof JLabel label ? label.getText()
            : component instanceof Dialog dialog ? dialog.getTitle()
            : component.getName();
        return component.getClass().getSimpleName() + (text == null || text.isBlank() ? "" : "[" + text + "]");
    }

    private static List<Integer> modifierKeys(int modifiers) {
        var keys = new ArrayList<Integer>();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) keys.add(KeyEvent.VK_CONTROL);
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) keys.add(KeyEvent.VK_SHIFT);
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) keys.add(KeyEvent.VK_ALT);
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) keys.add(KeyEvent.VK_META);
        return keys;
    }

    private static final String SHIFTED = "_:()<>+";
    private static final int[] SHIFTED_KEYS = {KeyEvent.VK_MINUS, KeyEvent.VK_SEMICOLON, KeyEvent.VK_9, KeyEvent.VK_0,
        KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_EQUALS};
    private static final String PLAIN = " .-,/[]=";
    private static final int[] PLAIN_KEYS = {KeyEvent.VK_SPACE, KeyEvent.VK_PERIOD, KeyEvent.VK_MINUS, KeyEvent.VK_COMMA,
        KeyEvent.VK_SLASH, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_EQUALS};

    private void typeCharacter(char character) {
        boolean shift;
        int key;
        if (character >= 'a' && character <= 'z') {
            shift = false;
            key = KeyEvent.VK_A + (character - 'a');
        } else if (character >= 'A' && character <= 'Z') {
            shift = true;
            key = KeyEvent.VK_A + (character - 'A');
        } else if (character >= '0' && character <= '9') {
            shift = false;
            key = KeyEvent.VK_0 + (character - '0');
        } else if (PLAIN.indexOf(character) >= 0) {
            shift = false;
            key = PLAIN_KEYS[PLAIN.indexOf(character)];
        } else if (SHIFTED.indexOf(character) >= 0) {
            shift = true;
            key = SHIFTED_KEYS[SHIFTED.indexOf(character)];
        } else {
            throw new AssertionError("type() cannot type '" + character + "' on the US keyboard layout of the virtual display");
        }
        if (shift) robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(key);
        robot.keyRelease(key);
        if (shift) robot.keyRelease(KeyEvent.VK_SHIFT);
    }

    private static void deleteRecursively(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }
}
