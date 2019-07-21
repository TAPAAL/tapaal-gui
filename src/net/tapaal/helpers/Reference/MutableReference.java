package net.tapaal.helpers.Reference;

import dk.aau.cs.util.Require;

/**
 * Represents an reference to an object, what can be updated
 * Never pass the mutablereference to an other object, pass read only Reference
 */
public class MutableReference<T> extends Reference<T> {

    public MutableReference(T ref) {
        super(ref);
    }

    public void setReference(T ref) {
        Require.notNull(ref, "Can't make a reference to null");
        super.setReference(ref);
    }
}
