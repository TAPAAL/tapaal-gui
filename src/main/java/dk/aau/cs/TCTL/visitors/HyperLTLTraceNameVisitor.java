package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HyperLTLTraceNameVisitor extends VisitorBase {


    public HyperLTLTraceNameVisitor() {

    }

    public Context getTraceContext(TCTLAbstractProperty property) {
        Context c = new Context();

        property.accept(this,c);
        return c;
    }

    public void visit(HyperLTLPathScopeNode pathScopeNode, Object context) {
        Context c = (Context) context;
        if(!c.getTraceNames().contains(pathScopeNode.getTrace())) {
            c.getTraceNames().add(pathScopeNode.getTrace());
        }

        pathScopeNode.getProperty().accept(this, context);
    }

    public void visit(LTLANode aNode, Object context) {
        Context c = (Context) context;
        if(!c.getTraceNames().contains(aNode.getTrace())) {
            c.getTraceNames().add(aNode.getTrace());
        }

        // Check if we already have an A node with the same trace (illegal)
        for(int i = 0; i < c.getPathNodes().size(); i++) {
            TCTLAbstractPathProperty node = c.getPathNodes().get(i);
            if(node instanceof LTLANode) {
                if(((LTLANode)node).getTrace().equals(aNode.getTrace())) {
                    c.setResult(false);
                }
            }
        }

        c.getPathNodes().add(aNode);

        aNode.getProperty().accept(this, context);

    }

    public void visit(LTLENode eNode, Object context) {
        Context c = (Context) context;
        if(!c.getTraceNames().contains(eNode.getTrace())) {
            c.getTraceNames().add(eNode.getTrace());
        }

        // Check if we already have an E node with the same trace (illegal)
        for(int i = 0; i < c.getPathNodes().size(); i++) {
            TCTLAbstractPathProperty node = c.getPathNodes().get(i);
            if(node instanceof LTLENode) {
                if(((LTLENode)node).getTrace().equals(eNode.getTrace())) {
                    c.setResult(false);
                }
            }
        }

        c.getPathNodes().add(eNode);

        eNode.getProperty().accept(this, context);
    }

    public void visit(LTLFNode afNode, Object context) {
        afNode.getProperty().accept(this, context);
    }

    public void visit(LTLGNode agNode, Object context) {
        agNode.getProperty().accept(this, context);
    }

    public void visit(LTLXNode axNode, Object context) {
        axNode.getProperty().accept(this, context);
    }

    public void visit(LTLUNode auNode, Object context) {
        auNode.getLeft().accept(this, context);
        auNode.getRight().accept(this, context);
    }

    public void visit(TCTLPathToStateConverter pathConverter, Object context) {
        pathConverter.getProperty().accept(this, context);
    }

    public void visit(TCTLAndListNode andListNode, Object context) {
        createList(andListNode.getProperties(), context);
    }

    public void visit(TCTLOrListNode orListNode, Object context) {
        createList(orListNode.getProperties(), context);
    }

    public void visit(TCTLTermListNode termListNode, Object context) {
        assert termListNode.getProperties().get(1) instanceof AritmeticOperator;
        AritmeticOperator operator = (AritmeticOperator)termListNode.getProperties().get(1);
        String op = operator.toString();
        switch (op) {
            case "+":
                createList(termListNode.getProperties(), context);
                break;
            case "*":
                createList(termListNode.getProperties(), context);
                break;
            case "-":
                createList(termListNode.getProperties(), context);
                break;
        }
    }

    private void createList(List<TCTLAbstractStateProperty> properties, Object context) {
        for (TCTLAbstractStateProperty p : properties) {
            p.accept(this, context);
        }
    }

    @Override
    public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
                      Object context) {

        atomicPropositionNode.getLeft().accept(this, context);
        atomicPropositionNode.getRight().accept(this, context);
    }

    public static class Context {
        private ArrayList<String> traceNames;
        private ArrayList<TCTLAbstractPathProperty> pathNodes;
        private Boolean result;

        public Boolean getResult() {
            return this.result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public ArrayList<TCTLAbstractPathProperty> getPathNodes() {
            return this.pathNodes;
        }

        public Context() {
            traceNames = new ArrayList<String>();
            pathNodes = new ArrayList<TCTLAbstractPathProperty>();
            result = true;
        }

        public ArrayList<String> getTraceNames() {
            return this.traceNames;
        }
    }

}
