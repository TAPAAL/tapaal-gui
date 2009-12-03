package pipe.gui.widgets;

import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class JSplitPaneFix extends JSplitPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4493433117542095206L;
	private boolean isPainted;
	private boolean hasProportionalLocation;
	private double proportionalLocation;

	public JSplitPaneFix(int verticalSplit, JScrollPane panel1,
			JPanel panel2) {
		super(verticalSplit, panel1, panel2);
		// TODO Auto-generated constructor stub
	}
	
	public JSplitPaneFix(int verticalSplit, JPanel panel1,
			JPanel panel2) {
		super(verticalSplit, panel1, panel2);
		// TODO Auto-generated constructor stub
	}
	
	public JSplitPaneFix(int verticalSplit) {
		super(verticalSplit);
	}

	@Override
	public void setDividerLocation(double proportionalLocation) {
        if (!isPainted) {       
            hasProportionalLocation = true;
            this.proportionalLocation = proportionalLocation;
        }
        else
            super.setDividerLocation(proportionalLocation);
    }

    @Override
	public void paint(Graphics g) {
        if (!isPainted) {       
            if (hasProportionalLocation)
                super.setDividerLocation(proportionalLocation);
            isPainted = true;
        }
        super.paint(g);
    } 

}
