package net.tapaal.gui.undo;

//WIP: Implemented 2021-10 to takeover current undo/redo implementation
import java.util.ArrayList;
import java.util.Arrays;

public class UndoRedoBuffer<T> {

    public final int CAPACITY;
    private final T[] buffer;

    private int currentIndex;

    private int elements = 0;
    private int elementsOnTop = 0;

    UndoRedoBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must by larger than zero");
        }

        this.CAPACITY = capacity;
        this.currentIndex = CAPACITY-1;

        buffer = (T[])new Object[CAPACITY];

    }

    private int nextIndex() {
        return (CAPACITY + currentIndex + 1) % CAPACITY;
    }
    private int previousIndex() {
        return (CAPACITY + currentIndex - 1) % CAPACITY;
    }

    public void add(T e) {
        elementsOnTop = 0;
        currentIndex = nextIndex();
        elements = Math.min(elements+1, CAPACITY);
        buffer[currentIndex] = e;
    }

    public T rollback() {
        if (elements > 0) {
            var t = top();

            elementsOnTop += 1;
            elements -= 1;

            currentIndex = previousIndex();
            return t;
        }
        return null;
    }

    public T repeat() {
        if (elementsOnTop > 0) {
            elementsOnTop -= 1;
            elements +=1;
            currentIndex = nextIndex();
            return top();
        }
        return null;
    }

    public T top() {
        if ( elements > 0 ) {
            return buffer[currentIndex];
        } else {
            return null;
        }
    }

    public boolean canRepeat() {
        return elementsOnTop > 0;
    }

    public boolean canRollback() {
        return elements > 0;
    }

    @Override
    public String toString() {
        return "UndoRedoBuffer{" +
            "CAPACITY=" + CAPACITY +
            ", buffer=" + Arrays.toString(buffer) +
            ", currentIndex=" + currentIndex +
            ", elements=" + elements +
            ", elementsOnTop=" + elementsOnTop +
            '}';
    }

    public void print() {
        System.out.println(this);
    }
}
