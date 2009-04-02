/*
 * BlankLayer.java
 */

package pipe.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * This class must be removed after zoom functionality is improved!!!
 */
public class BlankLayer extends JComponent {
   
   private static final Color gridColor = new Color(255,255,255);
   private static GuiFrame f;

   
   public BlankLayer(GuiFrame f){
      this.f = f;
   }
   
   
   public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(gridColor);
      g2d.fillRect(0,0, 1000, 1000);      
      f.hideNet(false);
   }
   
}
