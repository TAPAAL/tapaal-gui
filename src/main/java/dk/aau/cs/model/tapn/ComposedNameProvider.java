package dk.aau.cs.model.tapn;

/**
 * Resolves the names used when a network is flattened into one composed net.
 *
 * <p>The domain model needs this small capability when serializing markings
 * and bindings, but it should not depend on the GUI-aware composer that also
 * builds a diagram.</p>
 */
public interface ComposedNameProvider {
    String composedPlaceName(TimedPlace place);

    String composedTransitionName(TimedTransition transition);
}
