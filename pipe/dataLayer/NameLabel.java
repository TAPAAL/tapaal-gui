package pipe.dataLayer;

import java.awt.Font;

import javax.swing.JTextArea;

import pipe.gui.Pipe;
import pipe.gui.Translatable;
import pipe.gui.Zoomer;
import pipe.gui.Zoomable;


/**
 * <b>NameLabel</b> - This class is for the labels of PN Objects
 *
 * @see </p><p><a href="uml\NameLabel.png">NameLabel UML</a></p>
 * @version 1.0
 * @author Camilla Clifford
 */
public class NameLabel 
        extends JTextArea 
        implements Cloneable, Translatable, Zoomable {
   
   private int positionX;
   private int positionY;
   private String name = "";
   private String text = "";
   
   private Font font = 
           new Font(Pipe.LABEL_FONT, Font.BOLD, Pipe.LABEL_DEFAULT_FONT_SIZE);

   private boolean doZoom = false;

   
   public NameLabel(){
      this("");
   }
   

   public NameLabel(int zoom) {
      this("");
      setFont(getFont().deriveFont(Zoomer.getZoomedValue(
              (float)Pipe.LABEL_DEFAULT_FONT_SIZE, zoom)));
   }
   
   
   public NameLabel(String nameInput){
      super(nameInput);
      name = nameInput;
      setFont(font);
      setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
      setEditable(false);
      setFocusable(false);    
      setOpaque(false);
      setBackground(Pipe.BACKGROUND_COLOR);
      //setAlignmentY(TOP_ALIGNMENT);
      //setAlignmentX(RIGHT_ALIGNMENT);

      //setHorizontalAlignment(RIGHT);
      //setVerticalAlignment(TOP);
   }

   
   public void setPosition(int x, int y) {
      positionX = x;
      positionY = y;
      updatePosition();
   }
   
   
   public void updateSize() {
      // To get round Java bug #4352983 I have to expand the size a bit
      setSize((int)(getPreferredSize().width * 1.2),              
              (int)(getPreferredSize().height * 1.2));
      updatePosition();
   }
   
   
   public void updatePosition() {
      setLocation(positionX - getPreferredSize().width,
                  positionY - Pipe.NAMELABEL_OFFSET);
   }
   
   
   public void translate(int x, int y) {
      setPosition(positionX + x, positionY + y);
   }
   
   
   public double getYPosition() {
      return positionY;
   }
   
   
   public double getXPosition() {
      return positionX;
   }
   
   
   public void setName(String nameInput){
      name = nameInput;
      setText(text);
      updateSize();
   }
   
   
   public void setText(String s) {
      text = s;
      if (name != null) {
         super.setText(name + s);
      } else {
         super.setText(s);
      }
      updateSize();
   }
   
   
   public String getName(){
      return name;
   }
   

   public String getText(){
      return text;
   }   

   
   public void zoomUpdate(int value) {
      setFont(getFont().deriveFont(Zoomer.getZoomedValue(
              (float)Pipe.LABEL_DEFAULT_FONT_SIZE, value)));
      updateSize();
   }
   
   
   public Object clone() {
      try {
         return super.clone();
      } catch (CloneNotSupportedException e){
         e.printStackTrace();
      }
      return null;
   }   
   
}
