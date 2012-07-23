package pipe.gui.widgets;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dk.aau.cs.model.tapn.TimedToken;

public class ArcTokenSelector extends JPanel{
	String placeName;
	HashMap<JCheckBox, TimedToken> checkboxToToken = new HashMap<JCheckBox, TimedToken>();
	int missingToSelect;
	JLabel informationLabel;
	ArrayList<ArcTokenSelectorListener> listeners = new ArrayList<ArcTokenSelectorListener>();
	
	public ArcTokenSelector(String placeName, List<TimedToken> elligibleTokens, int weight) {
		super(new GridLayout(0, 1));
		
		this.placeName = placeName;
		missingToSelect = weight;
		informationLabel = new JLabel();
		updateInformationLabel();

		this.setBorder(BorderFactory.createTitledBorder("Place " + placeName));
		
		for(TimedToken token : elligibleTokens){
			JCheckBox checkBox = new JCheckBox(token.toString());
			checkBox.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent arg0) {
					if(((JCheckBox)arg0.getSource()).isSelected()){
						missingToSelect--;
					} else {
						missingToSelect++;
					}
					updateInformationLabel();
					notifyListeners();
				}
			});
			
			//All has to be selected
			if(weight == elligibleTokens.size()){
				checkBox.setSelected(true);
			}
			
			this.add(checkBox);
			checkboxToToken.put(checkBox, token);
		}
		
		this.add(informationLabel);
		
		//There is no reason you can change this
		if(weight == elligibleTokens.size()){
			this.setEnabled(false);
		}
	}
	
	private void updateInformationLabel(){
		informationLabel.setText("Select " + missingToSelect + " more");
	}
	
	public List<TimedToken> getSelected(){
		ArrayList<TimedToken> result = new ArrayList<TimedToken>();
		for(Entry<JCheckBox, TimedToken> entry : checkboxToToken.entrySet()){
			if(entry.getKey().isSelected()){
				result.add(entry.getValue());
			}
		}
		return result;
	}
	
	
	
	public boolean allChosen(){
		return missingToSelect == 0;
	}
	
	public void addArcTokenSelectorListener(ArcTokenSelectorListener listener){
		listeners.add(listener);
	}
	
	public void removeArcTokenSelectorListener(ArcTokenSelectorListener listener){
		listeners.remove(listener);
	}
	
	public void notifyListeners(){
		for(ArcTokenSelectorListener listener : listeners){
			listener.arcTokenSelectorActionPreformed(new ArcTokenSelectorListenerEvent(this));
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for(Component c : getComponents()){
			c.setEnabled(enabled);
		}
	}
	
	public interface ArcTokenSelectorListener{
		void arcTokenSelectorActionPreformed(ArcTokenSelectorListenerEvent e);
	}
	
	public class ArcTokenSelectorListenerEvent{
		private ArcTokenSelector source;
		public ArcTokenSelectorListenerEvent(ArcTokenSelector source){
			this.source = source;
		}
		
		ArcTokenSelector getSource(){
			return source;
		}
	}
}
