package dk.aau.cs.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;

public class BugHandledJXMultisplitPane extends JXMultiSplitPane {
	private static final long serialVersionUID = -1705954511667020833L;
	
	public BugHandledJXMultisplitPane() {
		super();
		
		super.setLayout(new BugHandledMultiSplitLayout());
	}
	
	private class BugHandledMultiSplitLayout extends MultiSplitLayout{
		private static final long serialVersionUID = 6869283686441692830L;
		
		public Component getComponentForName(String name){
			return getComponentForNode(getNodeForName(name));
		}
		
		@Override
		public void displayNode(String name, boolean visible) {
			super.displayNode(name, visible);
			Component c = getComponentForName(name);
			
			fixDividersError(c);
		}
		
		/*
		 * When hiding the two bottom children of a JXMultisplitpane something goes
		 * wrong and the bottom divider stay there (it should have been removed)
		 * This method removes this extra divider (bug in the swingx package)
		 * 
		 * The method also resets the sizes of the components this is done as if you 
		 * remove the bottom component, then pulls the bottom component all the way down
		 * and then adds the removed component again its shown outside the window
		 * 
		 * This method will hopefully become unnecessary as the JXMultisplitPane matures
		 */
		private void fixDividersError(Component changedComponent){
			//Make sure there are no extra dividers
			java.util.List<Node> t = ((Split) this.getModel()).getChildren();
			for(int i = t.size()-1; i>-1; i--){
				Node n = t.get(i);
				if(n.isVisible()){
					if(n instanceof Divider){
						n.setVisible(false);
					}
					break;
				}
			}
			
			//Makes sure all components are visible
			if(changedComponent.isVisible() && getNodeForComponent(changedComponent).isVisible()){
				int heigh = 0;
				int i = 0;
				int[] distribution = getComponentDistribution();
				for(Node n : t){
					if(n instanceof Leaf && n.isVisible()){
						Component component = this.getComponentForNode(n);
						n.setBounds(new Rectangle(new Point(0, heigh), new Dimension(component.getPreferredSize().width, distribution[i])));
						heigh +=distribution[i++];
					} else if (n instanceof Divider && n.isVisible()){
						
						n.setBounds(new Rectangle(0, heigh, n.getBounds().width, n.getBounds().height));
						heigh = heigh + n.getBounds().height;
					}
				}
			}

			BugHandledJXMultisplitPane.this.repaint();
		}
		
		private int getNumberOfShownComponents(){
			int result = 0;
			
			java.util.List<Node> t = ((Split) this.getModel()).getChildren();
			for(Node n : t){
				if(n instanceof Leaf && n.isVisible()){
					result++;
				}
			}

			return result;
		}
		
		private int[] getComponentDistribution(){
			double totalMinSize = 0;
			java.util.List<Node> t = ((Split) this.getModel()).getChildren();
			for(Node n : t){
				if(n instanceof Leaf && n.isVisible()){
					totalMinSize += getComponentForNode(n).getPreferredSize().getHeight();
				}
			}
			
			int i = 0;
			int numberOfShownComponents = getNumberOfShownComponents();
			int[] result = new int[numberOfShownComponents];
			
			double componentArea = BugHandledJXMultisplitPane.this.getSize().getHeight() - 7 * (numberOfShownComponents - 1);
			
			for(Node n : t){
				if(n instanceof Leaf && n.isVisible()){
					Component c = getComponentForNode(n);
					result[i++] = (int)Math.floor((componentArea * (c.getPreferredSize().getHeight() / totalMinSize))+0.5d);
				}
			}
			
			return result;
		}
	}
}
