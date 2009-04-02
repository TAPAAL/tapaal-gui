package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import pipe.dataLayer.DataLayer;


/**
 * Makes a filebrowser panel which is a JPanel containing the required stuff
 * @author Maxim
 */
public class PetriNetChooserPanel 
        extends JPanel {
   
   JCheckBox  useCurrent;
   JLabel     label;
   JTextField textField;
   JButton    browse;
   private DataLayer  defaultNet;
   
   
   public PetriNetChooserPanel(String title, DataLayer _defaultNet) {
      super();
      
      this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
      
      defaultNet = _defaultNet;
      if (defaultNet != null) {
         useCurrent=new JCheckBox("Use current net",true);
         useCurrent.addActionListener(useCurrentClick);
         this.add(useCurrent);
         this.add(Box.createHorizontalStrut(10));
      }
      
      label=new JLabel("Filename:");
      this.add(label);
      this.add(Box.createHorizontalStrut(5));
      
      textField = new JTextField((defaultNet!=null?defaultNet.getURI():null),15);
//    textField.setMaximumSize(new Dimension(Integer.MAX_VALUE,this.getPreferredSize().height));
//    textField.setPreferredSize(new Dimension());
      this.add(textField);
      this.add(Box.createHorizontalStrut(5));
      
      browse=new JButton("Browse");
      browse.addActionListener(browseButtonClick);
      this.add(browse);
      
      this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),title));
      
      this.setMaximumSize(new Dimension(Integer.MAX_VALUE,this.getPreferredSize().height));
      
      if (useCurrent!=null) {
         useCurrent.getActionListeners()[0].actionPerformed(null); // update
      }
   }
   
   
   private ActionListener useCurrentClick = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
         boolean enabled = !useCurrent.isSelected();
         label.setEnabled(enabled);
         textField.setEnabled(enabled);
         browse.setEnabled(enabled);
      }
   };
   
   
   private ActionListener browseButtonClick = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
         File f = new FileBrowser(textField.getText()).openFile();
         if (f != null) {
            textField.setText(f.getAbsolutePath());
         }
      }
   };
   
   
   public DataLayer getDataLayer() {
      if ((useCurrent != null) && (useCurrent.isSelected())) {
         return defaultNet;
      } else {
         String fileName = textField.getText();
         if (fileName == null || fileName.equals("")) {
            return null;
         } else {
            try {
               DataLayer result = new DataLayer(fileName);
               return result;
            } catch (Exception e) {
               JOptionPane.showMessageDialog( null, "Error loading\n" + fileName +
                       "\nPlease check it is a valid PNML file.", "Error",
                       JOptionPane.ERROR_MESSAGE);
            }
         }
         return null;
      }
   }
   
}
