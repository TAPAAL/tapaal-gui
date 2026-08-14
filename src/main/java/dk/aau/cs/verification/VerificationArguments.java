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
        return matcher.find() ? OptionalInt.of(Integer.parseInt(matcher.group(1))) : OptionalInt.empty();
    }

    public static boolean hasKBound(String arguments) {
        return arguments != null && K_BOUND.matcher(arguments).find();
    }

    public static TokenBounds tokenBounds(boolean hasKBound, int modelTokens, int extraTokens) {
        return hasKBound
            ? new TokenBounds(modelTokens + extraTokens, extraTokens)
            : new TokenBounds(Integer.MAX_VALUE, 0);
    }

    public record TokenBounds(int totalTokens, int extraTokens) { }
}
