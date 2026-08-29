package dk.aau.cs.model.tapn;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.SMCSettings;
import dk.aau.cs.verification.observations.Observation;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import net.tapaal.gui.petrinet.verification.TAPNQuery.VerificationType;

import java.util.ArrayList;
import java.util.List;

public class TAPNQuery {
	private TCTLAbstractProperty property;
	private int extraTokens = 0;
	private QueryCategory queryCategory = QueryCategory.Default;
	private ArrayList<String> traceList;
    private SMCSettings smcSettings;
    private VerificationType smcVerificationType;

	public TCTLAbstractProperty getProperty() {
		return property;
	}

	public TAPNQuery(TCTLAbstractProperty inputProperty, int extraTokens) {
		property = inputProperty;
		this.extraTokens = extraTokens;
        smcSettings = SMCSettings.Default();
	}

    public TAPNQuery(TCTLAbstractProperty inputProperty, int extraTokens, SMCSettings smcSettings) {
        property = inputProperty;
        this.extraTokens = extraTokens;
        this.smcSettings = smcSettings;
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

    public void setVerificationType(VerificationType smcVerificationType) {
        this.smcVerificationType = smcVerificationType;
    }

    public VerificationType getVerificationType() {
        return this.smcVerificationType;
    }

    public boolean isSimulate() {
        return smcVerificationType.equals(VerificationType.SIMULATE);
    }

    public void setTraceList(ArrayList<String> traces) {
	    this.traceList = traces;
    }

    public ArrayList<String> getTraceList() {
	    return this.traceList;
    }

    public void setSMCSettings(SMCSettings settings) {
        smcSettings = settings;
    }

    public SMCSettings getSMCSettings() {
        return smcSettings;
    }

    public List<Observation> getObservations() {
        return smcSettings.getObservations();
    }
}

