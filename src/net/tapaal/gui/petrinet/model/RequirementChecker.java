package net.tapaal.gui.petrinet.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class RequirementChecker<R> {
    public final List<R> errors = new LinkedList<R>();

    public void Not(boolean b, R s) {
        if (b) {
            errors.add(s);
        }
    }

    public void notNull(Object c, R s) {
        if (c == null) {
            errors.add(s);
        }
    }

    public boolean failed() {
        return errors.size() != 0;
    }
    public List<R> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}