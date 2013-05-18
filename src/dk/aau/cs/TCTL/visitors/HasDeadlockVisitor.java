package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLDeadlockNode;

public class HasDeadlockVisitor extends VisitorBase {

	public boolean hasDeadLock(TCTLAbstractProperty query){
		Context c = new Context();
		c.hasDeadlock = false;
		query.accept(this, c);
		return c.hasDeadlock;
	}
	
	public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) { 
		((Context)context).hasDeadlock = true;
	}

	private class Context{
		public boolean hasDeadlock = true;
	}
}
