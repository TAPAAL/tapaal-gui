package dk.aau.cs.model.tapn;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.verification.QueryType;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;

import java.util.ArrayList;

public class TAPNQuery {
	private TCTLAbstractProperty property;
	private int extraTokens = 0;
	private QueryCategory queryCategory = QueryCategory.Default;
	private ArrayList<String> traceList;

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
	
	public QueryType queryType() {
		if (property instanceof TCTLEFNode) return QueryType.EF;
		else if (property instanceof TCTLEGNode) return QueryType.EG;
		else if (property instanceof TCTLAFNode) return QueryType.AF;
        else if(queryCategory == QueryCategory.SMC && property instanceof LTLFNode) return QueryType.PF;
        else if(queryCategory == QueryCategory.SMC && property instanceof LTLGNode) return QueryType.PG;
		else if (property instanceof LTLENode) return  QueryType.E;
		else if (property instanceof LTLANode) return QueryType.A;
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

    public void setTraceList(ArrayList<String> traces) {
	    this.traceList = traces;
    }

    public ArrayList<String> getTraceList() {
	    return this.traceList;
    }
}

