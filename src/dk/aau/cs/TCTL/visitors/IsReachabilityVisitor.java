package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;

public class IsReachabilityVisitor extends VisitorBase {

    private class Context{
        public boolean isReachability = true;
        public int nTempOp = 0; // number of temporal operators
    }

    public boolean isReachability(TCTLAbstractProperty query){
        Context c = new Context();
        TCTLAbstractProperty newQuery = query;
        if(query instanceof TCTLStateToPathConverter) { // Remove the first converters.
            if(((TCTLStateToPathConverter) query).getProperty() instanceof TCTLPathToStateConverter) {
                TCTLPathToStateConverter child = (TCTLPathToStateConverter)((TCTLStateToPathConverter) query).getProperty();
                query = child.getProperty();
            } else {
                return false;
            }
        }
        query.accept(this, c);
        return (c.isReachability && c.nTempOp == 1);
    }

    public void visit(TCTLAUNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLEUNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLAXNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLEXNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLAGNode n, Object context) {
        ((Context)context).nTempOp++;
        n.getProperty().accept(this, context);
    }

    public void visit(TCTLEGNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLAFNode n, Object context) {
        ((Context)context).nTempOp++;
        ((Context)context).isReachability = false;
    }

    public void visit(TCTLEFNode n, Object context) {
        ((Context)context).nTempOp++;
        n.getProperty().accept(this, context);
    }

    public void visit(TCTLStateToPathConverter n, Object context){
        if(n instanceof TCTLStateToPathConverter){
            ((Context)context).isReachability = false;
        }
    }
}
