package pipe.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
         
import javax.swing.BorderFactory;
import javax.swing.DebugGraphics;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.text.BadLocationException;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.JSplitPaneFix;
import pipe.gui.widgets.LeftQueryPane;
import pipe.gui.widgets.ParameterPanel;


public class CreateGui {
   
   private static final double DIVIDER_LOCATION = 0.5;
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
   private static JSplitPane pane;
   private static AnimationHistory abstractAnimationPane=null;
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
      leftPane = new JSplitPaneFix(JSplitPane.VERTICAL_SPLIT, scrollerQueries, leftBottomPanel);
      ((JScrollPane)leftPane.getTopComponent()).setViewportView(queries);
      
      leftPane.setContinuousLayout(true);
      leftPane.setDividerSize(0);
      leftPane.setDividerLocation(DIVIDER_LOCATION);
      leftPane.setResizeWeight(0.5);
      
       pane = 
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
         leftPane.setResizeWeight(0.05f);
         	
         leftPane.setDividerSize(8);
      } catch (javax.swing.text.BadLocationException be) {
         be.printStackTrace();
      }
   }
   
   public static AnimationHistory getAbstractAnimationPane(){
	   return abstractAnimationPane;
   }
   
   public static void addAbstractAnimationPane() {
	// TODO Auto-generated method stub
	   	
	   try {
		   abstractAnimationPane=new AnimationHistory("Untimed Trace\n");
	   } catch (BadLocationException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   //abstractAnimationPane.setVerticalAlignment(SwingConstants.TOP);
	   
	   //Create a new empty animBox
	   try {
		   animBox = new AnimationHistory("Simulation history\n");
		   animBox.setEditable(false);
	   } catch (BadLocationException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
       
	   
	   JSplitPane pane2 = 
              new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,animBox,abstractAnimationPane);

      pane2.setContinuousLayout(true);
      pane2.setOneTouchExpandable(true);
      pane2.setBorder(null); // avoid multiple borders
      
      pane2.setDividerSize(8);
      
	  leftPane.setBottomComponent(pane2);
	  abstractAnimationPane.setBorder(new LineBorder(Color.black));
	
   }
   
   public static void removeAbstractAnimationPane() {
	      abstractAnimationPane=null;
	      scroller = new JScrollPane(animBox);
	      scroller.setBorder(new EmptyBorder(0,0,0,0)); // make it less bad on XP
	      leftPane.setBottomComponent(scroller);
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
         leftPane.setDividerLocation(DIVIDER_LOCATION);
         leftPane.setDividerSize(0);
      }
   }
   public static void removeAnimationControler() {
	   if (scroller != null) {
		   leftPane.remove(scroller2);
		   leftPane.setDividerLocation(DIVIDER_LOCATION);
		   leftPane.setDividerSize(0);
	   }
   }
   
   
   public static AnimationHistory getAnimationHistory() {
      return animBox;
   }
   
   public static void setLeftPaneToQueries(){
	   leftBottomPanel = new JPanel();
	   leftBottomPanel.add(new JLabel("test"));
	   queries = new LeftQueryPane(getModel().getQueries());
	   scrollerQueries.setViewportView(queries);
	   leftPane.setDividerLocation(DIVIDER_LOCATION);
	   leftPane.setTopComponent(scrollerQueries);
	   leftPane.setBottomComponent(leftBottomPanel);
	   leftPane.setContinuousLayout(true);
	   leftPane.setDividerSize(0);
	   leftPane.validate();
   }
   
   public static void updateLeftPanel() {

//	   if (queries.getBounds().height > getView().getBounds().height -50){
//		   leftBottomPanel = null;
//	   }else {
//		   if (leftBottomPanel == null){
//			   leftBottomPanel = new JPanel();
//		   }
//	   }
	   leftPane.validate();
   }





//   public static BigDecimal newBigDecimal(Long i) {
//	   DecimalFormat df = new DecimalFormat();
//	   df.setMaximumFractionDigits(Pipe.AGE_PRECISION);
//	   df.setRoundingMode(RoundingMode.DOWN);
//	   String toReturnFrom = df.format(i);
//	   return new BigDecimal(toReturnFrom, new MathContext(Pipe.AGE_PRECISION));
//   }


//   public static BigDecimal newBigDecimal(String i) {
////	   DecimalFormat df = new DecimalFormat();
////	   df.setMaximumFractionDigits(Pipe.AGE_PRECISION);
////	   df.setRoundingMode(RoundingMode.DOWN);
////	   String toReturnFrom = df.format(i);
//	   return new BigDecimal(i, new MathContext(Pipe.AGE_DECIMAL_PRECISION));
//   }
}
