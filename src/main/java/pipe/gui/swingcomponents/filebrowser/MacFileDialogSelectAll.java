package pipe.gui.swingcomponents.filebrowser;

import com.sun.jna.Callback;
import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import dk.aau.cs.debug.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The native Mac open panel selects every file when the Edit menu's Select All item sends
 * {@code selectAll:} to the first responder. TAPAAL's item uses that Command-A
 * shortcut for the net, so the panel never receives it. Control-A is not a
 * select-all shortcut in the panel either.
 * <p>
 * While a multi-file dialog is open, point the existing Command-A item at
 * {@code selectAll:} and add Control-A with the same action. A modal dialog
 * such as batch processing also disables the menu bar; those menus are enabled
 * for the same stretch. All of it is put back when the dialog closes.
 */
final class MacFileDialogSelectAll {
    private static final long DEVICE_INDEPENDENT_MODIFIERS = 0xffff0000L;
    /** NSEventModifierFlagCommand */
    private static final long COMMAND = 1L << 20;
    /** NSEventModifierFlagControl */
    private static final long CONTROL = 1L << 18;

    private static final Runnable NOOP = () -> { };

    private MacFileDialogSelectAll() {}

    static Runnable redirectWhileOpen() {
        if (!Platform.isMac()) {
            return NOOP;
        }
        AtomicReference<Runnable> restore = new AtomicReference<>(NOOP);
        try {
            // The open panel is AppKit, which is not always the Swing event thread.
            onMainThread(() -> restore.set(patchMenu()));
        } catch (Throwable failure) {
            Logger.log(failure instanceof Exception ? (Exception) failure : new Exception(failure));
            return NOOP;
        }
        Runnable undo = restore.get();
        if (undo == NOOP) {
            return NOOP;
        }
        return () -> onMainThread(undo);
    }

    private static Runnable patchMenu() {
        Pointer item = null;
        Pointer oldAction = null;
        Pointer oldTarget = null;
        boolean oldEnabled = false;
        boolean patched = false;
        Pointer extraItem = null;
        Pointer extraMenu = null;
        List<AncestorState> ancestors = List.of();
        WindowEnableState windowState = null;
        try {
            CommandA commandA = findCommandA();
            if (commandA == null) {
                return NOOP;
            }
            item = commandA.item;
            oldAction = msg(item, "action");
            oldTarget = msg(item, "target");
            oldEnabled = enabled(item);

            // The item state is already saved. Enabling the owner window can refresh the menu bar.
            windowState = allowMenusWhileModal();
            ancestors = enableAncestors(commandA.menu);
            patched = true;
            msgVoid(item, "setEnabled:", 1L);
            msgVoid(item, "setAction:", selector("selectAll:"));
            msgVoid(item, "setTarget:", Pointer.NULL);

            extraMenu = commandA.menu;
            extraItem = addControlSelectAll(extraMenu, commandA.title);
            Pointer restoreItem = item;
            Pointer restoreAction = oldAction;
            Pointer restoreTarget = oldTarget;
            boolean restoreEnabled = oldEnabled;
            Pointer removeItem = extraItem;
            Pointer removeMenu = extraMenu;
            List<AncestorState> restoreAncestors = ancestors;
            WindowEnableState restoreWindow = windowState;
            return () -> {
                restore(restoreItem, restoreAction, restoreTarget, restoreEnabled, removeMenu, removeItem);
                restoreAncestors(restoreAncestors);
                restoreWindow(restoreWindow);
            };
        } catch (Throwable failure) {
            Logger.log(failure instanceof Exception ? (Exception) failure : new Exception(failure));
            if (patched) {
                restore(item, oldAction, oldTarget, oldEnabled, extraMenu, extraItem);
                restoreAncestors(ancestors);
            }
            restoreWindow(windowState);
            return NOOP;
        }
    }

    /**
     * A modal dialog disables the owner window. AWT copies that into the menu bar,
     * and a disabled menu ignores key equivalents, so Select All never reaches the panel.
     * Clearing the flag makes a later menu refresh leave the menus enabled.
     * The window's own enabled state is restored with the menu.
     */
    private static WindowEnableState allowMenusWhileModal() {
        Pointer window = menuBarWindow();
        if (window == null) {
            return null;
        }
        boolean wasEnabled = enabled(window);
        if (!wasEnabled) {
            msgVoid(window, "setIsEnabled:", 1L);
        }
        return new WindowEnableState(window, wasEnabled);
    }

    private static void restoreWindow(WindowEnableState state) {
        if (state == null || state.enabled) {
            return;
        }
        try {
            msgVoid(state.window, "setIsEnabled:", 0L);
        } catch (Throwable failure) {
            Logger.log(failure instanceof Exception ? (Exception) failure : new Exception(failure));
        }
    }

    private static Pointer menuBarWindow() {
        Pointer app = msg(cls("NSApplication"), "sharedApplication");
        if (app == null) {
            return null;
        }
        Pointer owner = topOwner(msg(app, "keyWindow"));
        if (owner != null) {
            return owner;
        }
        return topOwner(msg(app, "mainWindow"));
    }

    private static Pointer topOwner(Pointer nsWindow) {
        if (nsWindow == null) {
            return null;
        }
        Pointer delegate = msg(nsWindow, "delegate");
        if (!responds(delegate, "ownerWindow") || !responds(delegate, "setIsEnabled:")) {
            return null;
        }
        Pointer current = delegate;
        for (int depth = 0; depth < 8; depth++) {
            Pointer owner = msg(current, "ownerWindow");
            if (owner == null) {
                break;
            }
            current = owner;
        }
        return current;
    }

    private static List<AncestorState> enableAncestors(Pointer menu) {
        List<AncestorState> states = new ArrayList<>();
        try {
            Pointer current = menu;
            for (int depth = 0; current != null && depth < 8; depth++) {
                boolean autoenables = enabled(current, "autoenablesItems");
                Pointer parent = parentItem(current);
                boolean parentEnabled = parent != null && enabled(parent);
                states.add(new AncestorState(current, autoenables, parent, parentEnabled));
                msgVoid(current, "setAutoenablesItems:", 0L);
                if (parent != null) {
                    msgVoid(parent, "setEnabled:", 1L);
                }
                Pointer next = msg(current, "supermenu");
                if (next == null || next.equals(current)) {
                    break;
                }
                current = next;
            }
            return states;
        } catch (Throwable failure) {
            restoreAncestors(states);
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (failure instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(failure);
        }
    }

    private static void restoreAncestors(List<AncestorState> states) {
        for (int i = states.size() - 1; i >= 0; i--) {
            AncestorState state = states.get(i);
            try {
                if (state.item != null) {
                    msgVoid(state.item, "setEnabled:", state.enabled ? 1L : 0L);
                }
                msgVoid(state.menu, "setAutoenablesItems:", state.autoenables ? 1L : 0L);
            } catch (Throwable failure) {
                Logger.log(failure instanceof Exception ? (Exception) failure : new Exception(failure));
            }
        }
    }

    private static Pointer parentItem(Pointer menu) {
        Pointer supermenu = msg(menu, "supermenu");
        if (supermenu == null) {
            return null;
        }
        long count = msgLong(supermenu, "numberOfItems");
        if (count < 0 || count > 500) {
            return null;
        }
        for (long i = 0; i < count; i++) {
            Pointer candidate = msg(supermenu, "itemAtIndex:", i);
            if (candidate == null) {
                continue;
            }
            if (menu.equals(msg(candidate, "submenu"))) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean enabled(Pointer object) {
        return enabled(object, "isEnabled");
    }

    private static boolean enabled(Pointer object, String selector) {
        return (msgLong(object, selector) & 0xffL) != 0;
    }

    private static boolean responds(Pointer receiver, String name) {
        if (receiver == null) {
            return false;
        }
        return (ObjC.I.msgLong(receiver, selector("respondsToSelector:"), selector(name)) & 0xffL) != 0;
    }

    private static void restore(Pointer item, Pointer action, Pointer target, boolean enabled, Pointer extraMenu, Pointer extraItem) {
        try {
            if (extraItem != null && extraMenu != null) {
                msgVoid(extraMenu, "removeItem:", extraItem);
            }
            if (item != null) {
                msgVoid(item, "setAction:", action == null ? Pointer.NULL : action);
                msgVoid(item, "setTarget:", target == null ? Pointer.NULL : target);
                msgVoid(item, "setEnabled:", enabled ? 1L : 0L);
            }
        } catch (Throwable failure) {
            Logger.log(failure instanceof Exception ? (Exception) failure : new Exception(failure));
        }
    }

    private static Pointer addControlSelectAll(Pointer menu, String title) {
        Pointer allocated = msg(cls("NSMenuItem"), "alloc");
        Pointer item = msg(allocated, "initWithTitle:action:keyEquivalent:",
                nsString(title), selector("selectAll:"), nsString("a"));
        msgVoid(item, "setKeyEquivalentModifierMask:", CONTROL);
        msgVoid(item, "setTarget:", Pointer.NULL);
        msgVoid(menu, "addItem:", item);
        // The menu retains the item. Balance the alloc.
        msgVoid(item, "release");
        return item;
    }

    private static CommandA findCommandA() {
        return findKeyEquivalent("a", COMMAND);
    }

    private static CommandA findKeyEquivalent(String key, long modifier) {
        Pointer app = msg(cls("NSApplication"), "sharedApplication");
        if (app == null) {
            return null;
        }
        Pointer mainMenu = msg(app, "mainMenu");
        if (mainMenu == null) {
            return null;
        }
        return findInMenu(mainMenu, key, modifier, 0);
    }

    private static CommandA findInMenu(Pointer menu, String key, long modifier, int depth) {
        if (menu == null || depth > 8) {
            return null;
        }
        long count = msgLong(menu, "numberOfItems");
        if (count < 0 || count > 500) {
            return null;
        }
        for (long i = 0; i < count; i++) {
            Pointer item = msg(menu, "itemAtIndex:", i);
            if (item == null) {
                continue;
            }
            if (key.equalsIgnoreCase(keyEquivalent(item)) && deviceModifiers(item) == modifier) {
                return new CommandA(item, menu, title(item));
            }
            Pointer submenu = msg(item, "submenu");
            if (submenu != null) {
                CommandA found = findInMenu(submenu, key, modifier, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static long deviceModifiers(Pointer item) {
        return msgLong(item, "keyEquivalentModifierMask") & DEVICE_INDEPENDENT_MODIFIERS;
    }

    private static String keyEquivalent(Pointer item) {
        return nsToJava(msg(item, "keyEquivalent"));
    }

    private static String title(Pointer item) {
        String title = nsToJava(msg(item, "title"));
        return title.isEmpty() ? "Select All" : title;
    }

    private static String nsToJava(Pointer nsString) {
        if (nsString == null) {
            return "";
        }
        Pointer utf8 = msg(nsString, "UTF8String");
        if (utf8 == null) {
            return "";
        }
        return utf8.getString(0, StandardCharsets.UTF_8.name());
    }

    private static Pointer nsString(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        com.sun.jna.Memory memory = new com.sun.jna.Memory(bytes.length + 1L);
        memory.write(0, bytes, 0, bytes.length);
        memory.setByte(bytes.length, (byte) 0);
        return msg(cls("NSString"), "stringWithUTF8String:", memory);
    }

    private static boolean isMainThread() {
        return (msgLong(cls("NSThread"), "isMainThread") & 0xffL) != 0;
    }

    private static void onMainThread(Runnable action) {
        if (isMainThread()) {
            action.run();
            return;
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        DispatchWork work = context -> {
            try {
                action.run();
            } catch (Throwable thrown) {
                failure.set(thrown);
            }
        };
        LibDispatch.I.dispatch_sync_f(mainQueue(), Pointer.NULL, work);
        if (failure.get() != null) {
            Throwable thrown = failure.get();
            if (thrown instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new RuntimeException(thrown);
        }
    }

    private static Pointer mainQueue() {
        return NativeLibrary.getInstance("System").getGlobalVariableAddress("_dispatch_main_q");
    }

    private interface DispatchWork extends Callback {
        void invoke(Pointer context);
    }

    private interface LibDispatch extends Library {
        LibDispatch I = Native.load("System", LibDispatch.class);

        void dispatch_sync_f(Pointer queue, Pointer context, DispatchWork work);
    }

    private static Pointer cls(String name) {
        return ObjC.I.objc_getClass(name);
    }

    private static Pointer selector(String name) {
        return ObjC.I.sel_registerName(name);
    }

    private static Pointer msg(Pointer receiver, String selector, Object... args) {
        Pointer sel = selector(selector);
        if (args.length == 0) {
            return ObjC.I.msg(receiver, sel);
        }
        if (args.length == 1 && (args[0] == null || args[0] instanceof Pointer)) {
            return ObjC.I.msg(receiver, sel, (Pointer) args[0]);
        }
        if (args.length == 1 && args[0] instanceof Long value) {
            return ObjC.I.msg(receiver, sel, value);
        }
        if (args.length == 3) {
            return ObjC.I.msg(receiver, sel, (Pointer) args[0], (Pointer) args[1], (Pointer) args[2]);
        }
        throw new IllegalArgumentException("Unsupported message " + selector);
    }

    private static long msgLong(Pointer receiver, String selector) {
        return ObjC.I.msgLong(receiver, selector(selector));
    }

    private static void msgVoid(Pointer receiver, String selector, Object... args) {
        Pointer sel = selector(selector);
        if (args.length == 0) {
            ObjC.I.msgVoid(receiver, sel);
            return;
        }
        if (args.length == 1 && (args[0] == null || args[0] instanceof Pointer)) {
            ObjC.I.msgVoid(receiver, sel, (Pointer) args[0]);
            return;
        }
        if (args.length == 1 && args[0] instanceof Long value) {
            ObjC.I.msgVoid(receiver, sel, value);
            return;
        }
        throw new IllegalArgumentException("Unsupported message " + selector);
    }

    private static final class CommandA {
        final Pointer item;
        final Pointer menu;
        final String title;

        CommandA(Pointer item, Pointer menu, String title) {
            this.item = item;
            this.menu = menu;
            this.title = title;
        }
    }

    private static final class AncestorState {
        final Pointer menu;
        final boolean autoenables;
        final Pointer item;
        final boolean enabled;

        AncestorState(Pointer menu, boolean autoenables, Pointer item, boolean enabled) {
            this.menu = menu;
            this.autoenables = autoenables;
            this.item = item;
            this.enabled = enabled;
        }
    }

    private static final class WindowEnableState {
        final Pointer window;
        final boolean enabled;

        WindowEnableState(Pointer window, boolean enabled) {
            this.window = window;
            this.enabled = enabled;
        }
    }

    private interface ObjC extends Library {
        ObjC I = Native.load("objc", ObjC.class, Collections.singletonMap(
                OPTION_FUNCTION_MAPPER,
                (FunctionMapper) (library, method) -> method.getName().startsWith("msg") ? "objc_msgSend" : method.getName()
        ));

        Pointer objc_getClass(String className);
        Pointer sel_registerName(String selectorName);

        Pointer msg(Pointer receiver, Pointer selector);
        long msgLong(Pointer receiver, Pointer selector);
        long msgLong(Pointer receiver, Pointer selector, Pointer arg);
        Pointer msg(Pointer receiver, Pointer selector, Pointer arg);
        Pointer msg(Pointer receiver, Pointer selector, long arg);
        Pointer msg(Pointer receiver, Pointer selector, Pointer title, Pointer action, Pointer keyEquivalent);
        void msgVoid(Pointer receiver, Pointer selector);
        void msgVoid(Pointer receiver, Pointer selector, Pointer arg);
        void msgVoid(Pointer receiver, Pointer selector, long arg);
    }
}
