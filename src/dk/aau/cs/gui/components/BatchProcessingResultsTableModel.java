package dk.aau.cs.gui.components;

import java.util.ArrayList;
import java.util.List;

import javax.management.Query;
import javax.swing.table.AbstractTableModel;

import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;

public class BatchProcessingResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] HEADINGS = new String[]{ "Method", "Model", "Query", "Result", "Verification Time", "Memory Usage" }; // TODO: somehow display discovered/explored/stored states. Maybe in tooltip
	private List<BatchProcessingVerificationResult> results;
	
	public BatchProcessingResultsTableModel(){
		results = new ArrayList<BatchProcessingVerificationResult>();
	}
	
	public void addResult(BatchProcessingVerificationResult result){
		int lastRow = results.size();
		results.add(result);
		fireTableRowsInserted(lastRow, lastRow);
	}
		

	public String getColumnName(int column) {
		return HEADINGS[column];
	}
	
	
	public int getColumnCount() {
		return HEADINGS.length;
	}

	
	public int getRowCount() {
		return results.size();
	}


	public Object getValueAt(int row, int col) {
		if(row >= results.size())	return null;
		BatchProcessingVerificationResult result = results.get(row);
		
		switch(col){
		case 0: return getVerificationAcrynom(result);
		case 1: return result.modelFile();
		case 2: return result.query();
		case 3: return result.verificationResult();
		case 4: return (result.verificationTimeInMs() / 1000.0) + " s";
		case 5: return result.verificationMemory();
		default:
			return null;
		}
	}

	private String getVerificationAcrynom(BatchProcessingVerificationResult result) {
		if(result.query() == null) return "";
		
		ReductionOption reduction = result.query().getReductionOption();
		
		if(reduction == ReductionOption.VerifyTAPN) {
			if(!result.query().discreteInclusion())
				return "A";
			else
				return "B";
		} else if(reduction == ReductionOption.VerifyTAPNdiscreteVerification)
			if(result.query().useTimeDarts() && result.query().usePTrie()){
				return "C";
			} else if(result.query().useTimeDarts()){
				return "D";
			} else if(result.query().usePTrie()){
				return "E";
			} else {
				return "F";
			}
		else if(reduction == ReductionOption.COMBI)
			return "G";
		else if(reduction == ReductionOption.STANDARD)
			return "H";
		else if(reduction == ReductionOption.OPTIMIZEDSTANDARD)
			return "I";
		else if(reduction == ReductionOption.BROADCAST)
			return "J";
		else if(reduction == ReductionOption.DEGREE2BROADCAST)
			return "K";
		else if(reduction == ReductionOption.VerifyPN || reduction == ReductionOption.VerifyPNApprox || reduction == ReductionOption.VerifyPNReduce){
			return "L";
		//	if(reduction == ReductionOption.VerifyPN || reduction == ReductionOption.VerifyPNApprox || reduction == ReductionOption.VerifyPNReduce){
		//	if(result.query().useReduction())	return "N";
	        //		return result.query().useOverApproximation()? "M":"L";
		}else
			return "";
	}

	public void clear() {
		results.clear();
		fireTableDataChanged();
	}
	
	public Class<?> getColumnClass(int c) {
		Object t = getValueAt(0, c);
		if(t == null){
			return Object.class;
		}
        return t.getClass();
    }

	public Iterable<BatchProcessingVerificationResult> getResults() {
		return results;
	}
	
	public BatchProcessingVerificationResult getResult(int index) {
		Require.that(index >= 0 && index < results.size(), "Index out of range");
		
		return results.get(index);
	}

}
