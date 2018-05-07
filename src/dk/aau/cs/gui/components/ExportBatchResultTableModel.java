package dk.aau.cs.gui.components;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import dk.aau.cs.util.Require;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;


public class ExportBatchResultTableModel extends AbstractTableModel {
 static final long serialVersionUID = 959574725280211159L;
	private final String[] HEADINGS = new String[]{ "File name", "Destination", "Status" };
	private ArrayList<String[]> results;
	
	public ExportBatchResultTableModel() {
		results = new ArrayList<String[]>();
	}
	
	public void addResult(String[] result){
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
		if(results == null)
			return 0;
		else		
			return results.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= results.size())	return null;
		String[] result = results.get(rowIndex);
		
		switch(columnIndex){
		case 0: return result[0];
		case 1: return result[1];
		case 2: return result[2];
		default:
			return null;
		}
	}
	public void clear() {
		results.clear();
		fireTableDataChanged();
	}
	public String[] getResult(int index) {
		Require.that(index >= 0 && index < results.size(), "Index out of range");
		
		return results.get(index);
	}

}
