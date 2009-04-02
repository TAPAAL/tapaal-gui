package pipe.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.ArrayList;
         
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.LeftQueryPane;


public class CreateGui {
   
   public static GuiFrame appGui;
   private static Animator animator;
   private static JTabbedPane appTab;
   private static int freeSpace;
   
   private static ArrayList tabs = new ArrayList();
   
   public static String imgPath, userPath; // useful for stuff
   
   private static class TabData { // a structure for holding a tab's data
      
      public DataLayer appModel;
      public GuiView appView;
      public File appFile;
   }
   
   /** The Module will go in the top pane, the animation window in the bottom pane */
   private static JSplitPane leftPane;
   private static AnimationHistory animBox;
   static AnimationControler animControlerBox;
   private static JScrollPane scroller;
   private static JScrollPane scroller2;
   private static JScrollPane scrollerQueries;
   private static JPanel queries;
   private static JPanel leftBottomPanel;
   
   public static void init() {
      
      imgPath = "Images" + System.getProperty("file.separator");
      
      // make the initial dir for browsing be My Documents (win), ~ (*nix), etc
      userPath = null; 
        
      
      appGui = new GuiFrame(Pipe.TOOL + " " + Pipe.VERSION);
      
      
      
      Grid.enableGrid();
      
      appTab = new JTabbedPane();

      animator = new Animator();
      appGui.setTab();   // sets Tab properties

      
      // create the tree
//      ModuleManager moduleManager = new ModuleManager();
      
//      JTree moduleTree = moduleManager.getModuleTree();
      leftBottomPanel = new JPanel();
      queries = new LeftQueryPane(new ArrayList<TAPNQuery>());
      scrollerQueries = new JScrollPane(queries);
      leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollerQueries, leftBottomPanel);
      ((JScrollPane)leftPane.getTopComponent()).setViewportView(queries);
      
      leftPane.setContinuousLayout(true);
      leftPane.setDividerSize(0);
      
      JSplitPane pane = 
              new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPane,appTab);

      pane.setContinuousLayout(true);
      pane.setOneTouchExpandable(true);
      pane.setBorder(null); // avoid multiple borders
      
      pane.setDividerSize(8);
      
//      pane.setDividerSize(0);
      
      appGui.getContentPane().add(pane);
          
      appGui.createNewTab(null,false);
      
      appGui.setVisible(true);
      appGui.init();
   }
   
   
   public static GuiFrame getApp() {  //returns a reference to the application
      return appGui;
   }
   
   
   public static DataLayer getModel() {
      return getModel(appTab.getSelectedIndex());
   }
   
   public static DataLayer getModel(int index) {
      if (index < 0) {
         return null;
      }
      
      TabData tab = (TabData)(tabs.get(index));
      if (tab.appModel == null) {
         tab.appModel = new DataLayer();
      }
      return tab.appModel;
   }
   

   public static GuiView getView(int index) {
      if (index < 0) {
         return null;
      }
      
      TabData tab = (TabData)(tabs.get(index));
      while (tab.appView == null) {
         try {
            tab.appView = new GuiView(tab.appModel);
         } catch (Exception e){
            e.printStackTrace();
         }
      }
      return tab.appView;
   }
   
   
   public static GuiView getView() {
      return getView(appTab.getSelectedIndex());
   }
   
   
   public static File getFile() {
      TabData tab = (TabData)(tabs.get(appTab.getSelectedIndex()));
      return tab.appFile;
   }
   
   
   public static void setFile(File modelfile, int fileNo) {
      if (fileNo >= tabs.size()) {
         return;
      }
      TabData tab = (TabData)(tabs.get(fileNo));
      tab.appFile = modelfile;
   }
   
   
   public static int getFreeSpace() {
      tabs.add(new TabData());
      return tabs.size() - 1;
   }
   
   
   public static void removeTab(int index) {
      tabs.remove(index);
   }
   
   
   public static JTabbedPane getTab() {
      return appTab;
   }
   
   public static Animator getAnimator() {
      return animator;
   }
   
   /** returns the current dataLayer object - 
    *  used to get a reference to pass to the modules */
   public static DataLayer currentPNMLData() {
      if (appTab.getSelectedIndex() < 0) {
         return null;
      }
      TabData tab = (TabData)(tabs.get(appTab.getSelectedIndex()));
      return tab.appModel;
   }
   
   
   /** Creates a new animationHistory text area, and returns a reference to it*/
   public static void addAnimationHistory() {
       try {
         animBox = new AnimationHistory("Simulation history\n");
         animBox.setEditable(false);
         
         scroller = new JScrollPane(animBox);
         scroller.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
         
         leftPane.setBottomComponent(scroller);
         
//         leftPane.setDividerLocation(0.5);
         leftPane.setDividerSize(8);
      } catch (javax.swing.text.BadLocationException be) {
         be.printStackTrace();
      }
   }
   
   public static void addAnimationControler() {
       try {
    	 animControlerBox = new AnimationControler("Simulation Controler\n");
         
         scroller2 = new JScrollPane(animControlerBox);
         scroller2.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
         
         leftPane.setTopComponent(scroller2);
         
//         leftPane.setDividerLocation(0.5);
         leftPane.setDividerSize(8);
         leftPane.resetToPreferredSizes();
         //shortcutBottons should be usable from start of
         animControlerBox.requestFocus(true);
      } catch (javax.swing.text.BadLocationException be) {
         be.printStackTrace();
         System.out.println("There where an error in creating the AnimationControler");
      }
   }
   
   public static void removeAnimationHistory() {
      if (scroller != null) {
         leftPane.remove(scroller);
         leftPane.setDividerLocation(0);
         leftPane.setDividerSize(0);
      }
   }
   public static void removeAnimationControler() {
	   if (scroller != null) {
		   leftPane.remove(scroller2);
		   leftPane.setDividerLocation(0);
		   leftPane.setDividerSize(0);
	   }
   }
   
   
   public static AnimationHistory getAnimationHistory() {
      return animBox;
   }
   
   public static void setLeftPaneToQueries(){
	   
	   leftBottomPanel = new JPanel();
	   queries = new LeftQueryPane(getModel().getQueries());
	   scrollerQueries.setViewportView(queries);
	   leftPane.setTopComponent(scrollerQueries);
	   leftPane.setBottomComponent(leftBottomPanel);
	   
	   leftPane.setContinuousLayout(true);
	   leftPane.setDividerSize(0);
	   leftPane.resetToPreferredSizes();
   }
   
   public static void updateLeftPanel() {

//	   if (queries.getBounds().height > getView().getBounds().height -50){
//		   leftBottomPanel = null;
//	   }else {
//		   if (leftBottomPanel == null){
//			   leftBottomPanel = new JPanel();
//		   }
//	   }
	   leftPane.resetToPreferredSizes();
   }
}
