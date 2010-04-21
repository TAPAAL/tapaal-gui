package pipe.gui.widgets;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.IntOrConstant;

public class TokenTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6411895270257181310L;
	private String[] columnNames = new String[] { "Age", "Value" };
	private List<ColoredToken> tokens;
	//private List<UndoableEdit> edits = new ArrayList<UndoableEdit>();
	
	
	public TokenTableModel(ColoredTimedPlace place) {
		tokens = new ArrayList<ColoredToken>();
		
		for(ColoredToken token : place.getColoredTokens()){
			getTokens().add(new ColoredToken(token.getAge(), token.getColor()));
		}
	}

	
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0){
			return BigDecimal.class;
		}else{
			return IntOrConstant.class;
		}
	}
		
	
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return getTokens().size();
	}
	
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
	
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex == 1){
			ColoredToken token = tokens.get(rowIndex);
			token.setColor((IntOrConstant)aValue);
		}
	}
	
	

	
	public Object getValueAt(int i, int j) {
		ColoredToken token = getTokens().get(i);
		if(j == 0){
			return token.getAge();
		}else{
			return token.getColor();
		}
	}

	public void addColoredToken(ColoredToken token) {
		getTokens().add(token);
		fireTableDataChanged();		
	}

	public void removeColoredToken(int selectedRow) {
		ColoredToken token = getTokens().get(selectedRow);
		getTokens().remove(token);
		fireTableDataChanged();		
	}

	public List<ColoredToken> getTokens() {
		return tokens;
	}

}
