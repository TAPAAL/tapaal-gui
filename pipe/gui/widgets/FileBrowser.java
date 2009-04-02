package pipe.gui.widgets;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import pipe.gui.ExtensionFilter;


/**
 * @author Maxim
 *
 * Opens a file browser with appropriate settings for the given filetype/extension
 */
public class FileBrowser 
        extends JFileChooser {
   
   private String ext;
   
   
   public FileBrowser(String filetype, String ext, String path) {
      super();
      if (filetype == null) {
         filetype = "file";
      }
      
      if (path != null) {
         File f = new File(path);
         if (f.exists()) {
            setCurrentDirectory(f);
         }
         if (!f.isDirectory()) {
            setSelectedFile(f);
         }
      }
      
      this.ext = ext;
      ExtensionFilter filter = new ExtensionFilter(ext,filetype);

      setFileFilter(filter);
   }
   
   
   public FileBrowser(String path) {
      this("Petri net","xml",path); // default parameters
   }
   
   
   public FileBrowser() {
      this(null);
   }
   
   
   public File openFile() {
      if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
         try {
            return getSelectedFile().getCanonicalFile();
         } catch (IOException e){
            /* gulp */
         }
      }
      return null;
   }
   
   
   public String saveFile() {
      if (showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
         try {
            File f = getSelectedFile();
            if (!f.getName().endsWith("." + ext)) {
               f = new File(f.getCanonicalPath() + "." + ext); // force extension
            }
            if (f.exists() && 
                    JOptionPane.showConfirmDialog(this, f.getCanonicalPath() + 
                    "\nDo you want to overwrite this file?") !=
                    JOptionPane.YES_OPTION) {
               return null;
            }
            return f.getCanonicalPath();
         } catch (IOException e) {
            /* gulp */
         };
      }
      return null;
   }
   
}
