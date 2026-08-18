package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

public class RenameTraceTCTLVisitor extends VisitorBase {

    private final String oldTraceName;
    private final String newTraceName;

    public RenameTraceTCTLVisitor(String oldTraceName, String newTraceName) {
        this.oldTraceName = oldTraceName;
        this.newTraceName = newTraceName;
    }

    @Override
    public void visit(LTLANode aNode, Object context) {
        if (aNode.getTrace().equals(oldTraceName)) {
            aNode.setTrace(newTraceName);
        }
        
        aNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(LTLENode eNode, Object context) {
        if (eNode.getTrace().equals(oldTraceName)) {
            eNode.setTrace(newTraceName);
        }

        eNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(HyperLTLPathScopeNode pathScopeNode, Object context) {
        if (pathScopeNode.getTrace().equals(oldTraceName)) {
            pathScopeNode.setTrace(newTraceName);
        }

        pathScopeNode.getProperty().accept(this, context);
    }

    @Override
    public void visit(TCTLTransitionNode transitionNode, Object context) {
        if (transitionNode.getTrace().equals(oldTraceName)) {
            transitionNode.setTrace(newTraceName);
        }
    }
}
