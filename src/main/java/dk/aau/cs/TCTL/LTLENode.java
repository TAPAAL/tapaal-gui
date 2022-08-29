package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class LTLENode extends TCTLAbstractPathProperty {
    TCTLAbstractStateProperty property;
    String trace;

    public void setProperty(TCTLAbstractStateProperty property) {
        this.property = property;
        this.property.setParent(this);
    }

    public TCTLAbstractStateProperty getProperty() {
        return property;
    }

    public LTLENode(TCTLAbstractStateProperty property, String trace) {
        this.property = property;
        this.property.setParent(this);
        this.trace = trace;
    }

    public LTLENode(TCTLAbstractStateProperty property) {
        this.property = property;
        this.property.setParent(this);
        this.trace = "";
    }

    public LTLENode(String trace) {
        this.property = new TCTLStatePlaceHolder();
        this.property.setParent(this);
        this.trace = trace;
    }

    public LTLENode() {
        this.property = new TCTLStatePlaceHolder();
        this.property.setParent(this);
        this.trace = "";
    }

    public String getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        if(trace.equals("")) {
            String s = property.isSimpleProperty() ? property.toString() : "("
                + property.toString() + ")";
            return "E " + s;
        }

        String s = property.isSimpleProperty() ? property.toString() : "("
            + property.toString() + ")";
        return "E " + trace + " " + s;
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public StringPosition[] getChildren() {
        int offset = 0;
        if(!trace.equals("")) {
            offset = trace.length() + 1;
        }

        int start = property.isSimpleProperty() ? 0 : 1;
        start = start + 2 + offset;
        int end = start + property.toString().length() + offset;
        StringPosition position = new StringPosition(start, end, property);

        StringPosition[] children = { position };
        return children;
    }

    @Override
    public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
            return (TCTLAbstractPathProperty) object2;
        } else {
            property = property.replace(object1, object2);
            return this;
        }
    }

    @Override
    public boolean containsPlaceHolder() {
        return property.containsPlaceHolder();
    }

    @Override
    public void convertForReducedNet(String templateName) {
        property.convertForReducedNet(templateName);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LTLENode) {
            LTLENode node = (LTLENode) o;
            return property.equals(node.getProperty()) && trace.equals(node.getTrace());
        }
        return false;
    }

    @Override
    public TCTLAbstractPathProperty copy() {
        return new LTLENode(property.copy(), trace);
    }

    @Override
    public boolean hasNestedPathQuantifiers() {
        return property instanceof TCTLPathToStateConverter || property.hasNestedPathQuantifiers();
    }

    public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
        return property.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
    }

    public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
        return property.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
    }

    @Override
    public TCTLAbstractProperty findFirstPlaceHolder() {
        return property.findFirstPlaceHolder();
    }

    @Override
    public void accept(ITCTLVisitor visitor, Object context) {
        visitor.visit(this, context);
    }
}
