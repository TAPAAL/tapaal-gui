package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.io.NamePurifier;

public class TCTLTransitionNode extends TCTLAbstractStateProperty {

	String template;
	String transition;
<<<<<<< HEAD
	String trace;

    public TCTLTransitionNode(String template, String transition, String trace) {
        this.template = NamePurifier.purify(template);
        this.transition = NamePurifier.purify(transition);
        this.trace = trace;
    }
=======
>>>>>>> origin/cpn
	
	public TCTLTransitionNode(String template, String transition) {
		this.template = NamePurifier.purify(template);
		this.transition = NamePurifier.purify(transition);
<<<<<<< HEAD
		this.trace = "";
=======
>>>>>>> origin/cpn
	}

	public TCTLTransitionNode(String transition) {
		this("", transition);
<<<<<<< HEAD
		this.trace = "";
=======
>>>>>>> origin/cpn
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			return this;
		}
	}

    @Override
    public void convertForReducedNet(String templateName) {
        transition = template + "_" + transition;
        template = templateName;
    }

    @Override
	public TCTLAbstractStateProperty copy() {
<<<<<<< HEAD
        if(!trace.equals("")) {
            return new TCTLTransitionNode(template, transition, trace);
        }
=======
>>>>>>> origin/cpn
		return new TCTLTransitionNode(template, transition);
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
<<<<<<< HEAD
=======

>>>>>>> origin/cpn
	}

	@Override
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(
			String templateName, String placeName) {
		return false;
	}
	
	@Override
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(
			String templateName, String transitionName) {
		return transition.equals(transitionName) && template.equals(templateName);
	}

	@Override
	public boolean containsPlaceHolder() {
		return false;
	}

    @Override
    public boolean hasNestedPathQuantifiers() {
        return false;
    }

    @Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return null;
	}

	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getTransition() {
		return this.transition;
	}
	
	public void setTransition(String transition) {
		this.transition = transition;
	}
	
	@Override
	public String toString() {
<<<<<<< HEAD
        if(template.equals("")) {
            return transition;
        }
        if (trace.equals("")) {
            return template + "." + transition;
        }

        return trace + "." + template + "." + transition;
		//return (template == "" ? "" : template + ".") + transition;
=======
		return (template == "" ? "" : template + ".") + transition;
>>>>>>> origin/cpn
	}
}
