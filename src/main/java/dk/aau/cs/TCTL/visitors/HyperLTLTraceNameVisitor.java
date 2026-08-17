package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

import java.util.ArrayList;
import java.util.List;

public class HyperLTLTraceNameVisitor extends VisitorBase {

    public HyperLTLTraceNameVisitor() {

    }

    public Context getTraceContext(TCTLAbstractProperty property) {
        var c = new Context();

        if (property != null) {
            property.accept(this, c);
            if (c.getPathNodes().isEmpty()) {
                c.setResult(false);
            }
        } else {
            c.setResult(false);
        }

        return c;
    }

    @Override
    public void visit(HyperLTLPathScopeNode pathScopeNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);

        var trace = pathScopeNode.getTrace();
        if (trace == null || trace.trim().isEmpty() || !c.getTraceNames().contains(trace)) {
            c.setResult(false);
            c.setHasUnquantifiedTraces(true);
        }

        var prevInside = c.isInsidePathScope();
        c.setInsidePathScope(true);
        pathScopeNode.getProperty().accept(this, context);
        c.setInsidePathScope(prevInside);
    }

    @Override
    public void visit(LTLANode aNode, Object context) {
        visitQuantifier(aNode, aNode.getProperty(), aNode.getTrace(), (Context)context);
    }

    @Override
    public void visit(LTLENode eNode, Object context) {
        visitQuantifier(eNode, eNode.getProperty(), eNode.getTrace(), (Context)context);
    }

    private void visitQuantifier(TCTLAbstractPathProperty node, TCTLAbstractStateProperty property, String trace, Context c) {
        if (c.isInsideFormulaBody()) {
            c.setResult(false);
            c.setHasNestedQuantifiers(true);
        }

        if (trace == null || trace.trim().isEmpty()) {
            c.setResult(false);
            c.setHasEmptyQuantifierTrace(true);
        } else if (c.getTraceNames().contains(trace)) {
            c.setResult(false);
            c.setHasDuplicateTraces(true);
        } else {
            c.getTraceNames().add(trace);
        }

        c.getPathNodes().add(node);
        property.accept(this, c);
    }

    @Override
    public void visit(LTLFNode afNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        afNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(LTLGNode agNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        agNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(LTLXNode axNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        axNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(LTLUNode auNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        auNode.getLeft().accept(this, context);
        auNode.getRight().accept(this, context);
    }

    @Override
    public void visit(TCTLPathToStateConverter pathConverter, Object context) {
        pathConverter.getProperty().accept(this, context);
    }

    @Override
    public void visit(TCTLStateToPathConverter stateConverter, Object context) {
        stateConverter.getProperty().accept(this, context);
    }

    @Override
    public void visit(TCTLNotNode notNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        notNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(TCTLAndListNode andListNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        createList(andListNode.getProperties(), context);
    }

    @Override
    public void visit(TCTLOrListNode orListNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        createList(orListNode.getProperties(), context);
    }

    @Override
    public void visit(TCTLPlusListNode plusListNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        createList(plusListNode.getProperties(), context);
    }

    @Override
    public void visit(TCTLTermListNode termListNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        createList(termListNode.getProperties(), context);
    }

    private void createList(List<TCTLAbstractStateProperty> properties, Object context) {
        for (TCTLAbstractStateProperty p : properties) {
            p.accept(this, context);
        }
    }

    @Override
    public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        atomicPropositionNode.getLeft().accept(this, context);
        atomicPropositionNode.getRight().accept(this, context);
    }

    @Override
    public void visit(TCTLPlaceNode placeNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        if (!c.isInsidePathScope()) {
            c.setResult(false);
            c.setHasMissingTraces(true);
        }
    }

    @Override
    public void visit(TCTLTransitionNode transitionNode, Object context) {
        var c = (Context)context;
        c.setInsideFormulaBody(true);
        if (!c.isInsidePathScope()) {
            var trace = transitionNode.getTrace();
            if (trace == null || trace.trim().isEmpty() || !c.getTraceNames().contains(trace)) {
                c.setResult(false);
                if (trace == null || trace.trim().isEmpty()) {
                    c.setHasMissingTraces(true);
                } else {
                    c.setHasUnquantifiedTraces(true);
                }
            }
        }
    }

    public static class Context {
        private final List<String> quantifiedTraceNames = new ArrayList<>();
        private final List<TCTLAbstractPathProperty> pathNodes = new ArrayList<>();
        private boolean insideFormulaBody;
        private boolean insidePathScope;
        private boolean hasMissingTraces;
        private boolean hasUnquantifiedTraces;
        private boolean hasDuplicateTraces;
        private boolean hasEmptyQuantifierTrace;
        private boolean hasNestedQuantifiers;
        private Boolean result = true;

        public Boolean getResult() {
            return this.result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public List<TCTLAbstractPathProperty> getPathNodes() {
            return this.pathNodes;
        }

        public List<String> getTraceNames() {
            return this.quantifiedTraceNames;
        }

        public boolean isInsideFormulaBody() {
            return insideFormulaBody;
        }

        public void setInsideFormulaBody(boolean insideFormulaBody) {
            this.insideFormulaBody = insideFormulaBody;
        }

        public boolean isInsidePathScope() {
            return insidePathScope;
        }

        public void setInsidePathScope(boolean insidePathScope) {
            this.insidePathScope = insidePathScope;
        }

        public boolean hasMissingTraces() {
            return hasMissingTraces;
        }

        public void setHasMissingTraces(boolean hasMissingTraces) {
            this.hasMissingTraces = hasMissingTraces;
        }

        public boolean hasUnquantifiedTraces() {
            return hasUnquantifiedTraces;
        }

        public void setHasUnquantifiedTraces(boolean hasUnquantifiedTraces) {
            this.hasUnquantifiedTraces = hasUnquantifiedTraces;
        }

        public boolean hasDuplicateTraces() {
            return hasDuplicateTraces;
        }

        public void setHasDuplicateTraces(boolean hasDuplicateTraces) {
            this.hasDuplicateTraces = hasDuplicateTraces;
        }

        public boolean hasEmptyQuantifierTrace() {
            return hasEmptyQuantifierTrace;
        }

        public void setHasEmptyQuantifierTrace(boolean hasEmptyQuantifierTrace) {
            this.hasEmptyQuantifierTrace = hasEmptyQuantifierTrace;
        }

        public boolean hasNestedQuantifiers() {
            return hasNestedQuantifiers;
        }

        public void setHasNestedQuantifiers(boolean hasNestedQuantifiers) {
            this.hasNestedQuantifiers = hasNestedQuantifiers;
        }
    }
}
