package dk.aau.cs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class Require {
	public static void that(boolean condition, String message) {
		if (!condition)
			throw new RequireException(message);
	}
	
	public static void notNull(Object o, String message) {
		if (o == null)
			throw new RequireException(message);
	}

    public static void notNull(Object... o) {
        if (Arrays.stream(o).anyMatch(Objects::isNull)) {
            throw new RequireException("One or more objects are null");
        }
    }

    public static <T> void notNull(Collection<T> col, String message) {
        if (col.stream().anyMatch(Objects::isNull)) {
            throw new RequireException(message);
        }
    }
}
