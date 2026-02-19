package net.tapaal.gui.petrinet.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Result<T, R>  {

        public final T result;
        public final boolean hasErrors;
        private final List<R> errors;

        public Result(T result) {
            hasErrors = false;
            this.result = result;
            errors = new ArrayList<R>(0);
        }

        public Result(Collection<R> errors) {
            hasErrors = true;
            this.errors = new ArrayList<R>(errors);
            result = null;
        }

        public List<R> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public T getResult() {
            return result;
        }
}