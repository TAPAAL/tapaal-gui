/*
 * UndoManager.java
 */
package pipe.gui.petrinet.undo;

import java.util.ArrayList;

import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.helpers.Reference.MutableReference;
import net.tapaal.helpers.Reference.Reference;
import net.tapaal.gui.GuiFrameActions;
import pipe.gui.Constants;
import pipe.gui.petrinet.PetriNetTab;

/**
 * Class to handle undo & redo functionality
 * 
 * @author pere
 */
public class UndoManager {

    private static final int UNDO_BUFFER_CAPACITY = Constants.DEFAULT_BUFFER_CAPACITY;
    private final PetriNetTab tab;

    // Normal mode undo stack
    private int normalIndexOfNextAdd = 0;
    private int normalSizeOfBuffer = 0;
    private int normalStartOfBuffer = 0;
    private int normalUndoneEdits = 0;
    private boolean normalHistoryWasTruncated = false;
    private boolean normalEditPending = false;
    private final ArrayList<ArrayList<Command>> normalEdits = new ArrayList<ArrayList<Command>>(UNDO_BUFFER_CAPACITY);

    // Animation mode undo stack
    private int animIndexOfNextAdd = 0;
    private int animSizeOfBuffer = 0;
    private int animStartOfBuffer = 0;
    private int animUndoneEdits = 0;
    private boolean animEditPending = false;
    private final ArrayList<ArrayList<Command>> animEdits = new ArrayList<ArrayList<Command>>(UNDO_BUFFER_CAPACITY);

    private Reference<GuiFrameActions> app = new MutableReference<>();

    public void setApp(Reference<GuiFrameActions> app) {
        this.app = app;

        // Undo/Redo is enabled based on undo/redo manager
        setUndoRedoStatus();
    }

    /**
     * Creates a new instance of UndoManager
     */
    public UndoManager(PetriNetTab tab) {
        this.tab = tab;

        // Initialize the buffers
        for (int i = 0; i < UNDO_BUFFER_CAPACITY; i++) {
            normalEdits.add(null);
            animEdits.add(null);
        }
    }

    private int getIndexOfNextAdd() {
        return tab != null && tab.isInAnimationMode() ? animIndexOfNextAdd : normalIndexOfNextAdd;
    }

    private void setIndexOfNextAdd(int value) {
        if (tab != null && tab.isInAnimationMode()) {
            animIndexOfNextAdd = value;
        } else {
            normalIndexOfNextAdd = value;
        }
    }

    private int getSizeOfBuffer() {
        return tab != null && tab.isInAnimationMode() ? animSizeOfBuffer : normalSizeOfBuffer;
    }

    private void setSizeOfBuffer(int value) {
        if (tab != null && tab.isInAnimationMode()) {
            animSizeOfBuffer = value;
        } else {
            normalSizeOfBuffer = value;
        }
    }

    private int getStartOfBuffer() {
        return tab != null && tab.isInAnimationMode() ? animStartOfBuffer : normalStartOfBuffer;
    }

    private void setStartOfBuffer(int value) {
        if (tab != null && tab.isInAnimationMode()) {
            animStartOfBuffer = value;
        } else {
            normalStartOfBuffer = value;
        }
    }

    private int getUndoneEdits() {
        return tab != null && tab.isInAnimationMode() ? animUndoneEdits : normalUndoneEdits;
    }

    private void setUndoneEdits(int value) {
        if (tab != null && tab.isInAnimationMode()) {
            animUndoneEdits = value;
        } else {
            normalUndoneEdits = value;
        }
    }

    private boolean isEditPending() {
        return tab != null && tab.isInAnimationMode() ? animEditPending : normalEditPending;
    }

    private void setEditPending(boolean value) {
        if (tab != null && tab.isInAnimationMode()) {
            animEditPending = value;
        } else {
            normalEditPending = value;
        }
    }

    private ArrayList<ArrayList<Command>> getEdits() {
        return tab != null && tab.isInAnimationMode() ? animEdits : normalEdits;
    }

    public void redo() {
        if (isEditPending()) {
            setEditPending(false);
        }

        if (getUndoneEdits() > 0) {
            for (Command command : getEdits().get(getIndexOfNextAdd())) {
                command.redo();
            }

            setIndexOfNextAdd((getIndexOfNextAdd() + 1) % UNDO_BUFFER_CAPACITY);
            setSizeOfBuffer(Math.min(UNDO_BUFFER_CAPACITY, getSizeOfBuffer() + 1));
            setUndoneEdits(getUndoneEdits() - 1);
        }

        updateTabChangedState();
        setUndoRedoStatus();
    }

    public void setUndoRedoStatus() {
        boolean canUndo = getSizeOfBuffer() != 0;
        app.ifPresent(a -> a.setUndoActionEnabled(canUndo));
        
        boolean canRedo = getUndoneEdits() != 0;
        app.ifPresent(a -> a.setRedoActionEnabled(canRedo));
    }

    public void undo() {
        if (isEditPending()) {
            setEditPending(false);
            setUndoRedoStatus();
            return;
        }

        if (getSizeOfBuffer() > 0) {
            int indexOfNextAdd = getIndexOfNextAdd();
            if (--indexOfNextAdd < 0) {
                indexOfNextAdd += UNDO_BUFFER_CAPACITY;
            }
            setIndexOfNextAdd(indexOfNextAdd);
            setSizeOfBuffer(getSizeOfBuffer() - 1);
            setUndoneEdits(getUndoneEdits() + 1);

            // The currentEdit to undo (reverse order)
            ArrayList<Command> currentEdit = getEdits().get(indexOfNextAdd);
            
            for (int i = currentEdit.size() - 1; i >= 0; i--) {
                currentEdit.get(i).undo();
            }
        }

        updateTabChangedState();
        setUndoRedoStatus();
    }

    private void updateTabChangedState() {
        if (tab != null && !tab.isInAnimationMode()) {
            tab.updateNetChangedFromUndoManager();
        }
    }

    /**
     * Returns whether the normal editing history still contains changes made
     * since the last clear (normally the last save).
     */
    public boolean hasAppliedNormalEdits() {
        return normalSizeOfBuffer > 0 || normalHistoryWasTruncated;
    }

    public void clear() {
        if (tab != null && tab.isInAnimationMode()) {
            animIndexOfNextAdd = 0;
            animSizeOfBuffer = 0;
            animStartOfBuffer = 0;
            animUndoneEdits = 0;
            animEditPending = false;
        } else {
            normalIndexOfNextAdd = 0;
            normalSizeOfBuffer = 0;
            normalStartOfBuffer = 0;
            normalUndoneEdits = 0;
            normalHistoryWasTruncated = false;
            normalEditPending = false;
        }

        setUndoRedoStatus();
    }

    public void undoAll() {
        if (isEditPending()) {
            setEditPending(false);
        }

        while (getSizeOfBuffer() > 0) {
            undo();
        }

        setUndoRedoStatus();
    }

    public void newEdit() {
        if (isEditPending()) {
            return;
        }

        // Delay allocating a history slot until a command is added. Dialogs
        // commonly start a transaction before validation, and those empty
        // transactions must not make the model look dirty.
        setEditPending(true);
    }

    private void beginPendingEdit() {
        setUndoneEdits(0);

        ArrayList<Command> compoundEdit = new ArrayList<Command>();
        getEdits().set(getIndexOfNextAdd(), compoundEdit);
        setIndexOfNextAdd((getIndexOfNextAdd() + 1) % UNDO_BUFFER_CAPACITY);
        if (getSizeOfBuffer() < UNDO_BUFFER_CAPACITY) {
            setSizeOfBuffer(getSizeOfBuffer() + 1);
        } else {
            if (tab == null || !tab.isInAnimationMode()) {
                normalHistoryWasTruncated = true;
            }
            setStartOfBuffer((getStartOfBuffer() + 1) % UNDO_BUFFER_CAPACITY);
        }

        updateTabChangedState();
        setUndoRedoStatus();
    }

    public void addEdit(Command undoableEdit) {
        if (isEditPending()) {
            beginPendingEdit();
            setEditPending(false);
        }

        ArrayList<Command> compoundEdit = getEdits().get(currentIndex());
        if (compoundEdit == null) {
            beginPendingEdit();
            compoundEdit = getEdits().get(currentIndex());
        }
        compoundEdit.add(undoableEdit);
        updateTabChangedState();
        // debug();
    }

    public void addNewEdit(Command undoableEdit) {
        newEdit(); // mark for a new "transaction"
        addEdit(undoableEdit);
    }

    private int currentIndex() {
        int lastAdd = getIndexOfNextAdd() - 1;
        if (lastAdd < 0) {
            lastAdd += UNDO_BUFFER_CAPACITY;
        }
        return lastAdd;
    }

    public void removeCurrentEdit() {
        if (isEditPending()) {
            setEditPending(false);
        } else if (getSizeOfBuffer() > 0 && currentIndex() >= 0 && currentIndex() < getEdits().size()) {
            int currentIndex = currentIndex();
            ArrayList<Command> currentEdit = getEdits().get(currentIndex);
            if (currentEdit != null && currentEdit.isEmpty()) {
                getEdits().set(currentIndex, null);
                setSizeOfBuffer(getSizeOfBuffer() - 1);
                setIndexOfNextAdd(currentIndex);
            }
        }
        updateTabChangedState();
        setUndoRedoStatus();
    }

    public void undoAndRemoveCurrentEdit() {
        if (isEditPending()) {
            setEditPending(false);
            setUndoRedoStatus();
            return;
        }

        int currentIdx = currentIndex();
    
        undo();
    
        getEdits().set(currentIdx, null);
        if (getUndoneEdits() > 0) {
            setUndoneEdits(getUndoneEdits() - 1);
        }
        updateTabChangedState();
        setUndoRedoStatus();
    }

    public boolean currentEditIsEmpty() {
        return isEditPending() || getEdits().get(currentIndex()) == null || getEdits().get(currentIndex()).isEmpty();
    }
}
