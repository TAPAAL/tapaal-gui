package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.io.NamePurifier;

public class HyperLTLPathScopeNode extends TCTLAbstractStateProperty {

    private TCTLAbstractStateProperty property;
    private String trace;

    public HyperLTLPathScopeNode(TCTLAbstractStateProperty property, String trace) {
        this.property = property;
        this.trace = trace;
    }

    public TCTLAbstractStateProperty getProperty() {
        return property;
    }

    public void setProperty(TCTLAbstractStateProperty property) {
        this.property = property;
        this.property.setParent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HyperLTLPathScopeNode) {
            HyperLTLPathScopeNode node = (HyperLTLPathScopeNode) o;
            return property.equals(node.property) && trace.equals(node.getTrace());
        }
        return false;
    }

    @Override
    public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
                                             TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
            TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            property = property.replace(object1, object2);
            return this;
        }
    }
    @Override
    public StringPosition[] getChildren() {
        int start = trace.length();
        int end = start + property.toString().length();
        StringPosition position = new StringPosition(start, end, property);

        StringPosition[] children = { position };
        return children;
    }

    @Override
    public void convertForReducedNet(String templateName) {
        property.convertForReducedNet(templateName);
    }

    @Override
    public TCTLAbstractStateProperty copy() {
        return new HyperLTLPathScopeNode(property.copy(), trace);
    }

    @Override
    public void accept(ITCTLVisitor visitor, Object context) {
        visitor.visit(this, context);
    }

    public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
        return property.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
    }

    public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
        return property.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
    }

    @Override
    public boolean containsPlaceHolder() {
        return property.containsPlaceHolder();
    }

    @Override
    public boolean hasNestedPathQuantifiers() {
        return property instanceof TCTLPathToStateConverter || property.hasNestedPathQuantifiers();
    }

    @Override
    public TCTLAbstractProperty findFirstPlaceHolder() {
        return property.findFirstPlaceHolder();

    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return trace + "." + property.toString();
    }
}
