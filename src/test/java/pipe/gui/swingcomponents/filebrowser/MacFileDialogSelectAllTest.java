package pipe.gui.swingcomponents.filebrowser;

import com.sun.jna.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.FileDialog;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacFileDialogSelectAllTest {
    static {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    private JFrame frame;

    @AfterEach
    void disposeFrame() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
            frame = null;
        }
    }

    @Test
    void redirectsCommandAndControlAWhileTheDialogIsOpen() throws Exception {
        Assumptions.assumeTrue(Platform.isMac());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        showFrameWithSelectAll();

        SwingUtilities.invokeAndWait(() -> {
            assertFalse(MacFileDialogSelectAll.commandATargetsFirstResponder());
            Runnable restore = MacFileDialogSelectAll.redirectWhileOpen();
            try {
                assertTrue(MacFileDialogSelectAll.commandATargetsFirstResponder(), MacFileDialogSelectAll.describeCommandA());
                assertTrue(MacFileDialogSelectAll.controlATargetsFirstResponder(), MacFileDialogSelectAll.shortcutDump());
            } finally {
                restore.run();
            }
            assertFalse(MacFileDialogSelectAll.commandATargetsFirstResponder());
            assertFalse(MacFileDialogSelectAll.controlATargetsFirstResponder());
        });
    }

    @Test
    @Timeout(20)
    void controlASelectsEveryFileInTheNativeDialog() throws Exception {
        Assumptions.assumeTrue(Platform.isMac());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Path dir = Files.createTempDirectory("tapaal-select-all");
        try {
            Files.createFile(dir.resolve("a.tapn"));
            Files.createFile(dir.resolve("b.tapn"));
            Files.createFile(dir.resolve("c.tapn"));
            AtomicInteger javaSelectAll = new AtomicInteger();
            showFrameWithSelectAll(javaSelectAll);

            File[] chosen = openWithShortcut(dir.toFile(), KeyEvent.VK_CONTROL);
            assertEquals(3, chosen.length, "Control-A should select every file");
            assertEquals(0, javaSelectAll.get(), "Control-A should not select net components");
        } finally {
            deleteTree(dir);
        }
    }

    @Test
    @Timeout(20)
    void commandASelectsEveryFileInTheNativeDialog() throws Exception {
        Assumptions.assumeTrue(Platform.isMac());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Path dir = Files.createTempDirectory("tapaal-select-all");
        try {
            Files.createFile(dir.resolve("a.tapn"));
            Files.createFile(dir.resolve("b.tapn"));
            Files.createFile(dir.resolve("c.tapn"));
            AtomicInteger javaSelectAll = new AtomicInteger();
            showFrameWithSelectAll(javaSelectAll);

            File[] chosen = openWithShortcut(dir.toFile(), KeyEvent.VK_META);
            assertEquals(3, chosen.length, "Command-A should select every file");
            assertEquals(0, javaSelectAll.get(), "Command-A should stay inside the open panel");
        } finally {
            deleteTree(dir);
        }
    }

    @Test
    @Timeout(20)
    void enablesSelectAllWhileAModalDialogIsOpen() throws Exception {
        Assumptions.assumeTrue(Platform.isMac());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        showFrameWithSelectAll();
        showModalDialog();
        try {
            AtomicReference<String> problem = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> {
                boolean ancestorsBefore = MacFileDialogSelectAll.selectAllAncestorsEnabled();
                Runnable restore = MacFileDialogSelectAll.redirectWhileOpen();
                try {
                    if (!MacFileDialogSelectAll.commandATargetsFirstResponder()
                            || !MacFileDialogSelectAll.controlATargetsFirstResponder()
                            || !MacFileDialogSelectAll.selectAllAncestorsEnabled()) {
                        problem.set("beforeAncestors=" + ancestorsBefore + " " + MacFileDialogSelectAll.describeCommandA());
                    }
                } finally {
                    restore.run();
                }
                if (problem.get() == null && MacFileDialogSelectAll.selectAllAncestorsEnabled() != ancestorsBefore) {
                    problem.set("ancestors not restored, before=" + ancestorsBefore + " " + MacFileDialogSelectAll.describeCommandA());
                }
                if (problem.get() == null && MacFileDialogSelectAll.controlATargetsFirstResponder()) {
                    problem.set("Control-A item was left behind");
                }
            });
            if (problem.get() != null) {
                throw new AssertionError(problem.get());
            }
        } finally {
            closeModalDialogs();
        }
    }

    @Test
    @Timeout(20)
    void controlASelectsEveryFileWhenAModalDialogIsOpen() throws Exception {
        Assumptions.assumeTrue(Platform.isMac());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Path dir = Files.createTempDirectory("tapaal-select-all");
        try {
            Files.createFile(dir.resolve("a.tapn"));
            Files.createFile(dir.resolve("b.tapn"));
            Files.createFile(dir.resolve("c.tapn"));
            showFrameWithSelectAll();
            File[] chosen = openFromModalDialog(dir.toFile(), KeyEvent.VK_CONTROL);
            assertEquals(3, chosen.length, "Control-A should select every file while a modal dialog is open: " + chosenDuring);
        } finally {
            deleteTree(dir);
        }
    }

    private String chosenDuring = "not sampled";

    private File[] openFromModalDialog(File directory, int modifier) throws Exception {
        AtomicReference<File[]> chosen = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean dialogUp = new AtomicBoolean();
        showModalDialog();
        SwingUtilities.invokeLater(() -> {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (!(window instanceof JDialog) || !window.isShowing()) {
                    continue;
                }
                JButton add = new JButton("Add files");
                add.addActionListener(event -> {
                    try {
                        FileDialog dialog = new FileDialog(frame, "Open", FileDialog.LOAD);
                        dialog.setDirectory(directory.getAbsolutePath());
                        dialog.setMultipleMode(true);
                        Runnable restore = MacFileDialogSelectAll.redirectWhileOpen();
                        dialogUp.set(true);
                        try {
                            dialog.setVisible(true);
                        } finally {
                            restore.run();
                        }
                        chosen.set(dialog.getFiles());
                    } catch (Throwable thrown) {
                        failure.set(thrown);
                    } finally {
                        window.setVisible(false);
                    }
                });
                ((JDialog) window).getContentPane().add(add);
                ((JDialog) window).getRootPane().setDefaultButton(add);
                window.revalidate();
                add.doClick();
            }
        });
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!dialogUp.get() && failure.get() == null && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        if (!dialogUp.get()) {
            closeModalDialogs();
            throw new AssertionError("file dialog was not opened");
        }
        Thread.sleep(800);
        chosenDuring = MacFileDialogSelectAll.describeCommandA();
        Robot robot = new Robot();
        robot.setAutoDelay(40);
        robot.keyPress(modifier);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(modifier);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        for (int i = 0; i < 80 && chosen.get() == null && failure.get() == null; i++) {
            Thread.sleep(100);
        }
        if (chosen.get() == null) {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        }
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        return chosen.get() == null ? new File[0] : chosen.get();
    }

    private void showModalDialog() throws Exception {
        AtomicBoolean shown = new AtomicBoolean();
        SwingUtilities.invokeLater(() -> {
            JDialog modal = new JDialog(frame, "Batch Processing", true);
            modal.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent event) {
                    shown.set(true);
                }
            });
            modal.setSize(280, 140);
            modal.setVisible(true);
        });
        for (int i = 0; i < 50 && !shown.get(); i++) {
            Thread.sleep(50);
        }
        assertTrue(shown.get(), "modal dialog did not open");
        Thread.sleep(300);
    }

    private void closeModalDialogs() throws Exception {
        SwingUtilities.invokeLater(() -> {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof JDialog && window.isShowing()) {
                    window.setVisible(false);
                }
            }
        });
        for (int i = 0; i < 50 && modalIsShowing(); i++) {
            Thread.sleep(50);
        }
    }

    private boolean modalIsShowing() throws Exception {
        AtomicBoolean showing = new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof JDialog && window.isShowing()) {
                    showing.set(true);
                }
            }
        });
        return showing.get();
    }

    private File[] openWithShortcut(File directory, int modifier) throws Exception {
        AtomicReference<File[]> chosen = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean dialogUp = new AtomicBoolean();
        Thread opener = new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    FileDialog dialog = new FileDialog(frame, "Open", FileDialog.LOAD);
                    dialog.setDirectory(directory.getAbsolutePath());
                    dialog.setMultipleMode(true);
                    dialogUp.set(true);
                    Runnable restore = MacFileDialogSelectAll.redirectWhileOpen();
                    try {
                        dialog.setVisible(true);
                    } finally {
                        restore.run();
                    }
                    chosen.set(dialog.getFiles());
                });
            } catch (Throwable thrown) {
                failure.set(thrown);
            }
        });
        opener.start();
        while (!dialogUp.get() && failure.get() == null && opener.isAlive()) {
            Thread.sleep(50);
        }
        Thread.sleep(800);
        Robot robot = new Robot();
        robot.setAutoDelay(40);
        robot.keyPress(modifier);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(modifier);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        opener.join(8000);
        if (opener.isAlive()) {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            opener.join(3000);
        }
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        assertFalse(opener.isAlive(), "native file dialog did not close");
        return chosen.get() == null ? new File[0] : chosen.get();
    }

    private void showFrameWithSelectAll() throws Exception {
        showFrameWithSelectAll(new AtomicInteger());
    }

    private void showFrameWithSelectAll(AtomicInteger selectAllCount) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            frame = new JFrame("select-all-test");
            JMenuBar bar = new JMenuBar();
            JMenu edit = new JMenu("Edit");
            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuItem selectAll = new JMenuItem("Select all");
            selectAll.setAccelerator(KeyStroke.getKeyStroke('A', shortcut));
            selectAll.addActionListener(event -> selectAllCount.incrementAndGet());
            edit.add(selectAll);
            bar.add(edit);
            frame.setJMenuBar(bar);
            frame.setSize(320, 200);
            frame.setVisible(true);
        });
        for (int i = 0; i < 50; i++) {
            AtomicBoolean found = new AtomicBoolean();
            SwingUtilities.invokeAndWait(() -> found.set(MacFileDialogSelectAll.hasCommandA()));
            if (found.get()) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Command-A menu item was not installed: " + dumpOnEdt());
    }

    private static String dumpOnEdt() throws Exception {
        AtomicReference<String> dump = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> dump.set(MacFileDialogSelectAll.shortcutDump()));
        return dump.get();
    }

    private static void deleteTree(Path dir) throws Exception {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                }
            });
        }
    }
}
