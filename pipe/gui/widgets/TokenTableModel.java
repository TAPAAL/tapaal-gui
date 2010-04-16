package pipe.gui.widgets;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.gui.CreateGui;
import pipe.gui.undo.UndoableEdit;

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

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0){
			return BigDecimal.class;
		}else{
			return IntOrConstant.class;
		}
	}
		
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return getTokens().size();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex == 1){
			ColoredToken token = tokens.get(rowIndex);
			token.setColor((IntOrConstant)aValue);
		}
	}
	
	

	@Override
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
