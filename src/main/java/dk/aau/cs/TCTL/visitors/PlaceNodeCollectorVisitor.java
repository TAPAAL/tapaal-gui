package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLPlaceNode;

import java.util.ArrayList;
import java.util.List;

public final class PlaceNodeCollectorVisitor extends VisitorBase {
    private final List<TCTLPlaceNode> places = new ArrayList<>();

    public static List<TCTLPlaceNode> collect(TCTLAbstractProperty query) {
        var visitor = new PlaceNodeCollectorVisitor();
        query.accept(visitor, null);
        return visitor.places;
    }

    @Override
    public void visit(TCTLPlaceNode placeNode, Object context) {
        places.add(placeNode);
    }
}
