package dk.aau.cs.model.tapn;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.verification.QueryType;
import pipe.dataLayer.TAPNQuery.QueryCategory;

public class TAPNQuery {
	private TCTLAbstractProperty property;
	private int extraTokens = 0;
	private QueryCategory queryCategory = QueryCategory.Default;

	public TCTLAbstractProperty getProperty() {
		return property;
	}

	public TAPNQuery(TCTLAbstractProperty inputProperty, int extraTokens) {
		property = inputProperty;
		this.extraTokens = extraTokens;
	}

	public int getExtraTokens() {
		return extraTokens;
	}
	
	public QueryType queryType(){
		if(property instanceof TCTLEFNode) return QueryType.EF;
		else if(property instanceof TCTLEGNode) return QueryType.EG;
		else if(property instanceof TCTLAFNode) return QueryType.AF;
		else return QueryType.AG;
	}
	
	public boolean hasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(getProperty());
	}

	@Override
	public String toString() {
		return property.toString();
	}
	
	public void setProperty(TCTLAbstractProperty newProperty){
		this.property = newProperty;
	}
	
	public void setCategory(QueryCategory category){
    	this.queryCategory = category;
    }
    
    public QueryCategory getCategory(){
    	return this.queryCategory;
    }
}
