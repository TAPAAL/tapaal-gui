package dk.aau.cs.verification;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VerificationArguments {
    private static final Pattern K_BOUND = Pattern.compile("(?:\\s|^)(?:--k-bound|-k)(?:\\s+|=)(\\d+)");

    private VerificationArguments() { }

    public static OptionalInt getKBound(String arguments) {
        if (arguments == null) return OptionalInt.empty();
        Matcher matcher = K_BOUND.matcher(arguments);
        if (!matcher.find()) return OptionalInt.empty();
        int bound = Integer.parseInt(matcher.group(1));
        return bound > 0 ? OptionalInt.of(bound) : OptionalInt.empty();
    }

    public static boolean hasKBound(String arguments) {
        return getKBound(arguments).isPresent();
    }

    public static TokenBounds tokenBounds(boolean hasKBound, int modelTokens, int extraTokens) {
        return hasKBound
            ? new TokenBounds(modelTokens + extraTokens, extraTokens)
            : new TokenBounds(Integer.MAX_VALUE, 0);
    }

    public record TokenBounds(int totalTokens, int extraTokens) { }
}
