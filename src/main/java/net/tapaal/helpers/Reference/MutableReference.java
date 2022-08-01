package net.tapaal.helpers.Reference;

/**
 * Represents an reference to an object, what can be updated
 * Never pass the mutablereference to an other object, pass read only Reference
 */
public class MutableReference<T> extends Reference<T> {

    public MutableReference(T ref) {
        super(ref);
    }

    public MutableReference() {
        super(null);
    }

    public void setReference(T ref) {
        super.setReference(ref);
    }
}
