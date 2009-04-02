/**
 * Invariant analysis module.
 * @author Manos Papantoniou
 * @author Michael Camacho
 * @author Maxim (GUI and some cleanup)
 */
package pipe.modules.invariantAnalysis;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PNMatrix;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;


public class InvariantAnalysis 
        implements Module {
   
   /**
    * This class has been modified to include a reference to the Petri Net
    * being analysed so that methods other Run() can access data more easily.
    * Nadeem 28/05/2005
    */
   private DataLayer pnmlData;		// A reference to the Petri Net to be analysed
   
   private PNMatrix IncidenceMatrix;
   private PNMatrix PInvariants;
   private PNMatrix TInvariants;
   private static final String MODULE_NAME = "Invariant Analysis";
   
   private PetriNetChooserPanel sourceFilePanel;
   private ResultsHTMLPane results;
   
   
   public InvariantAnalysis() {
      ;
   }
   
   
   /**
    * @return   The module name
    */
   public String getName() {
      return MODULE_NAME;
   }
   
   
   /**
    * Call the methods that find the net invariants.
    * @param dataObj   A dataLayer type object with all the information about 
    *                  the petri net
    */
   public void run(DataLayer pnmlData) {
      // Keep a reference to the p-n for other methods in this class
      this.pnmlData = pnmlData;
      
      // Build interface
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(), MODULE_NAME, true);
      
      // 1 Set layout
      Container contentPane=guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
      
      // 2 Add file browser
      sourceFilePanel = new PetriNetChooserPanel("Source net",pnmlData);
      contentPane.add(sourceFilePanel);
      
      // 3 Add results pane
      results = new ResultsHTMLPane(pnmlData.getURI());
      contentPane.add(results);
      
      // 4 Add button
      contentPane.add(new ButtonBar("Analyse", analyseButtonClick,
              guiDialog.getRootPane()));      
      
      // 5 Make window fit contents' preferred size
      guiDialog.pack();
      
      // 6 Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      
      guiDialog.setVisible(true);
   }

   
   /**
    * Classify button click handler
    */
   ActionListener analyseButtonClick=new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         DataLayer sourceDataLayer=sourceFilePanel.getDataLayer();
         String s = "<h2>Petri net invariant analysis results</h2>";
         if (sourceDataLayer == null) {
            JOptionPane.showMessageDialog( null, "Please, choose a source net", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         
         if (!sourceDataLayer.hasPlaceTransitionObjects()) {
            s += "No Petri net objects defined!";
         } else {
            s += analyse(sourceDataLayer);
            results.setEnabled(true);
         }
         results.setText(s);
      }
   };
   
   
   /**
    * Invariant analysis was originally only performed on the initial markup
    * matrix. This is not what the user expects however as when changes are made 
    * to the petri net, the invariant analysis then does not change to reflect 
    * this. The method calls have been changed to pass the current markup matrix
    * as the parameter for invariant analysis.
    *
    * @author Nadeem Akharware
    * @version 1.2
    */
   private String analyse(DataLayer pnmlData) {
      Date start_time = new Date(); // start timer for program execution
      // extract data from PN object
      int[][] array = pnmlData.getIncidenceMatrix();
      if (array.length == 0) {
         return "";
      }
      IncidenceMatrix = new PNMatrix(array);
      
      int[] currentMarking = pnmlData.getCurrentMarkingVector(); // Nadeem 26/05/2005
      
      String output = findNetInvariants(currentMarking); // Nadeem 26/05/2005
      
      Date stop_time = new Date();
      double etime = (stop_time.getTime() - start_time.getTime())/1000.;
      return output + "<br>Analysis time: " + etime + "s";
   }
   
   
   /** Find the net invariants.
    *
    * @param M   An array containing the current marking of the net.
    * @return    A string containing the resulting matrices of P and T 
    *            Invariants or "None" in place of one of the matrices if it does 
    *            not exist.
    */
   
   public String findNetInvariants(int[] M) {
      return reportTInvariants(M) + "<br>" + reportPInvariants(M) + "<br>";
   }
   
   
   /** Reports on the P invariants.
    *
    * @param M   An array containing the current marking of the net.
    * @return    A string containing the resulting matrix of P Invariants,
    *            the P equations and some analysis
    */
   public String reportPInvariants(int[] M) {
      PInvariants = findVectors(IncidenceMatrix.transpose());
      String result = "<h3>P-Invariants</h3>"; 
      result += ResultsHTMLPane.makeTable(
              PInvariants, pnmlData.getPlaces(), false, true, true, false);
      
      if (PInvariants.isCovered()) {
         result += "The net is covered by positive P-Invariants, " +
                 "therefore it is bounded.";
      } else {
         result += "The net is not covered by positive P-Invariants, " +
                 "therefore we do not know if it is bounded.";
      }
      return result + "<br>" + findPEquations(M);
   }
   
   
   /**
    * Reports on the T invariants.
    *
    * @param   M  An array containing the current marking of the net.
    * @return     A string containing the resulting matrix of T Invariants and
    *             some analysis of it
    */ 
   public String reportTInvariants(int[] M) {
      TInvariants = findVectors(IncidenceMatrix);
      Transition[] transitionArray = pnmlData.getTransitions();

      String result = "<h3>T-Invariants</h3>";
      result += ResultsHTMLPane.makeTable(
              TInvariants, pnmlData.getTransitions(), false, true, true, false);
      
      if (TInvariants.isCovered()) {
         result += "The net is covered by positive T-Invariants, " +
                 "therefore it might be bounded and live.";
      } else {
         result += "The net is not covered by positive T-Invariants, " +
                 "therefore we do not know if it is bounded and live.";
      }
      
      return result + "<br>";
   }

   
   //<Marc>
   /* It returns a PNMatrix containing the place invariants of the sourceDataLayer net.
    * @param sourceDataLayer A dataLayer type object with all the information about 
    *                        the petri net.
    * @return a PNMatrix where each column contains a place invariant.
    */
   public PNMatrix getPInvariants(DataLayer sourceDataLayer){
      int[][] array = sourceDataLayer.getIncidenceMatrix();
      if (array.length == 0) {
         return null;
      }
      IncidenceMatrix = new PNMatrix(array);
      
      int[] currentMarking = sourceDataLayer.getCurrentMarkingVector();
      return findVectors(IncidenceMatrix.transpose());
   }
   
   
   /* It returns a PNMatrix containing the transition invariants of the sourceDataLayer net.
    * @param sourceDataLayer A dataLayer type object with all the information about 
    *                        the petri net.
    * @return a PNMatrix where each column contains a transition invariant.
    */
   public PNMatrix getTInvariants(DataLayer sourceDataLayer){
      int[][] array = sourceDataLayer.getIncidenceMatrix();
      if (array.length == 0) {
         return null;
      }
      IncidenceMatrix = new PNMatrix(array);
      
      int[] currentMarking = sourceDataLayer.getCurrentMarkingVector();
      return findVectors(IncidenceMatrix);
   }
   //</Marc>

   
   /** Find the P equations of the net.
    *
    * @param currentMarking   An array containing the current marking of the net.
    * @return                 A string containing the resulting P equations,
    *                         empty string if the equations do not exist.
    */
   public String findPEquations(int[] currentMarking) {
      Place[] placeArray = pnmlData.getPlaces();
      String eq = "<h3>P-Invariant equations</h3>";
      int m = PInvariants.getRowDimension();
      int n = PInvariants.getColumnDimension();
      if (n < 1) { // if there are no P-invariants don't return any equations
         return "";
      }
      
      PNMatrix col = new PNMatrix(m, 1);
      int rhs = 0; // the right hand side of the equations
      
      // form the column vector of current marking
      PNMatrix M = new PNMatrix(currentMarking, currentMarking.length);
      // for each column c in PInvariants, form one equation of the form
      // a1*p1+a2*p2+...+am*pm = c.transpose*M where a1, a2, .., am are the
      // elements of c
      for (int i = 0; i < n; i++) { // column index
         for (int j = 0; j < m; j++) { // row index
            // form left hand side:
            int a = PInvariants.get(j, i);
            if (a > 1) {
               eq += Integer.toString(a);
            }
            if (a > 0) {
               eq += "M(" + placeArray[j].getName() + ") + "; // Nadeem 28/05/2005
            }
         }
         // replace the last occurance of "+ "
         eq = eq.substring(0, (eq.length()-2)) + "= ";
         
         // form right hand side
         col = PInvariants.getMatrix(0, m-1, i, i);
         rhs = col.transpose().vectorTimes(M);
         eq += rhs + "<br>"; // and separate the equations
      }
      return eq;
   }
   
   
   /** Transform a matrix to obtain the minimal generating set of vectors.
    *
    * @param C    The matrix to transform.
    * @return     A matrix containing the vectors.
    */
   public PNMatrix findVectors(PNMatrix C) {
     /*
     | Tests Invariant Analysis Module
     |
     |   C          = incidence matrix.
     |   B          = identity matrix with same number of columns as C.
     |                Becomes the matrix of vectors in the end.
     |   pPlus      = integer array of +ve indices of a row.
     |   pMinus     = integer array of -ve indices of a row.
     |   pPlusMinus = set union of the above integer arrays.
      */
      
      int m = C.getRowDimension(), n = C.getColumnDimension();
      
      // generate the nxn identity matrix
      PNMatrix B = PNMatrix.identity(n, n);
      
      // arrays containing the indices of +ve and -ve elements in a row vector 
      // respectively
      int[] pPlus, pMinus; 
      
      // while there are no zero elements in C do the steps of phase 1
//--------------------------------------------------------------------------------------
      // PHASE 1:
//--------------------------------------------------------------------------------------
      while (!(C.isZeroMatrix())) {
         if (C.checkCase11()) {
            // check each row (case 1.1)
            for (int i = 0; i < m; i++) {
               pPlus = C.getPositiveIndices(i); // get +ve indices of ith row
               pMinus = C.getNegativeIndices(i); // get -ve indices of ith row
               if (isEmptySet(pPlus) || isEmptySet(pMinus) ) { // case-action 1.1.a
                  // this has to be done for all elements in the union pPlus U pMinus
                  // so first construct the union
                  int [] pPlusMinus = uniteSets(pPlus, pMinus);
                  
                  // eliminate each column corresponding to nonzero elements in pPlusMinus union
                  for (int j = pPlusMinus.length-1; j >= 0; j--) {
                     if (pPlusMinus[j] != 0) {
                        C = C.eliminateCol(pPlusMinus[j]-1);
                        B = B.eliminateCol(pPlusMinus[j]-1);
                        n--;  // reduce the number of columns since new matrix is smaller
                     }
                  }
               }
               resetArray(pPlus);   // reset pPlus and pMinus to 0
               resetArray(pMinus);
            }
         } else if ( C.cardinalityCondition() >= 0) {
            while (C.cardinalityCondition() >= 0) {
               // while there is a row in the C matrix that satisfies the cardinality condition
               // do a linear combination of the appropriate columns and eliminate the appropriate column.
               int cardRow = -1; // the row index where cardinality == 1
               cardRow = C.cardinalityCondition();
               // get the column index of the column to be eliminated
               int k = C.cardinalityOne();
               if (k == -1) {
                  System.out.println("Error");
               }
               
               // get the comlumn indices to be changed by linear combination
               int j[] = C.colsToUpdate();
               
               // update columns with linear combinations in matrices C and B
               // first retrieve the coefficients
               int[] jCoef = new int[n];
               for (int i = 0; i < j.length; i++) {
                  if (j[i] != 0) {
                     jCoef[i] = Math.abs(C.get(cardRow, (j[i]-1)));
                  }
               }
               
               // do the linear combination for C and B
               // k is the column to add, j is the array of cols to add to
               C.linearlyCombine(k, Math.abs(C.get(cardRow, k)), j, jCoef);
               B.linearlyCombine(k, Math.abs(C.get(cardRow, k)), j, jCoef);
               
               // eliminate column of cardinality == 1 in matrices C and B
               C = C.eliminateCol(k);
               B = B.eliminateCol(k);
               // reduce the number of columns since new matrix is smaller
               n--;
            }
         } else {
            // row annihilations (condition 1.1.b.2)
            // operate only on non-zero rows of C (row index h)
            // find index of first non-zero row of C (int h)
            int h = C.firstNonZeroRowIndex();
            while ((h = C.firstNonZeroRowIndex()) > -1) {
               
               // the column index of the first non zero element of row h
               int k = C.firstNonZeroElementIndex(h);
               
               // find first non-zero element at column k, chk
               int chk = C.get(h, k);
               
               // find all the other indices of non-zero elements in that row chj[]
               int[] chj = new int[n-1];
               chj = C.findRemainingNZIndices(h);
               
               while (!(isEmptySet(chj))) {
                  // chj empty only when there is just one nonzero element in the 
                  // whole row, this should not happen as this case is eliminated 
                  // in the first step, so we would never arrive at this while() 
                  // with just one nonzero element
                  
                  // find all the corresponding elements in that row (coefficients jCoef[])
                  int[] jCoef = C.findRemainingNZCoef(h);
                  
                  // adjust linear combination coefficients according to sign
                  int[] alpha, beta; // adjusted coefficients for kth and remaining columns respectively
                  alpha = alphaCoef(chk, jCoef);
                  beta = betaCoef(chk, jCoef.length);
                  
                  // linearly combine kth column, coefficient alpha, to jth columns, coefficients beta
                  C.linearlyCombine(k, alpha, chj, beta);
                  B.linearlyCombine(k, alpha, chj, beta);
                  
                  // delete kth column
                  C = C.eliminateCol(k);
                  B = B.eliminateCol(k);
                  
                  chj = C.findRemainingNZIndices(h);
               }
            }
            // show the result
            // System.out.println("Pseudodiagonal positive basis of Ker C after phase 1:");
            // B.print(2, 0);
         }
      }
      
      // END OF PHASE ONE, now B contains a pseudodiagonal positive basis of Ker C
//--------------------------------------------------------------------------------------
      // PHASE 2:
//--------------------------------------------------------------------------------------
      // h is -1 at this point, make it equal to the row index that has a -ve element.
      // rowWithNegativeElement with return -1 if there is no such row, and we exit the loop.
      int h;
      while ((h = B.rowWithNegativeElement()) > -1) {
         pPlus = B.getPositiveIndices(h); // get +ve indices of hth row (1st col = index 1)
         pMinus = B.getNegativeIndices(h); // get -ve indices of hth row (1st col = index 1)
         
         // effective length is the number of non-zero elements
         int pPlusLength = effectiveSetLength(pPlus);
         int pMinusLength = effectiveSetLength(pMinus);
         
         if (pPlusLength != 0) { // set of positive coef. indices must me non-empty
            // form the cross product of pPlus and pMinus
            // for each pair (j, k) in the cross product, operate a linear combination on the columns
            // of indices j, k, in order to get a new col with a zero at the hth element
            // The number of new cols obtained = the number of pairs (j, k)
            for (int j = 0; j < pPlusLength; j++) {
               for (int k = 0; k < pMinusLength; k++) {
                  // coefficients of row h, cols indexed j, k in pPlus, pMinus 
                  // respectively
                  int jC = pPlus[j]-1, kC = pMinus[k]-1; 
                  
                  // find coeficients for linear combination, just the abs values
                  // of elements this is more efficient than finding the least 
                  // common multiple and it does not matter since later we will 
                  // find gcd of col and we will normalise with that the col 
                  // elements
                  int a = -B.get(h, kC), b = B.get(h, jC);
                  
                  // create the linear combination a*jC-column + b*kC-column, an
                  // IntMatrix mx1 where m = number of rows of B
                  m = B.getRowDimension();
                  PNMatrix V1 = new PNMatrix(m, 1); // column vector mx1 of zeros
                  PNMatrix V2 = new PNMatrix(m, 1); // column vector mx1 of zeros
                  V1 = B.getMatrix(0, m-1, jC, jC);
                  V2 = B.getMatrix(0, m-1, kC, kC);
                  V1.timesEquals(a);
                  V2.timesEquals(b);
                  V2.plusEquals(V1);
                  
                  // find the gcd of the elements in this new col
                  int V2gcd = V2.gcd();
                  
                  // divide all the col elements by their gcd if gcd > 1
                  if (V2gcd > 1) {
                     V2.divideEquals(V2gcd);
                  }
                  
                  // append the new col to B
                  n = B.getColumnDimension();
                  PNMatrix F = new PNMatrix(m, n+1);
                  F = B.appendVector(V2);
                  B = F.copy();
               }
            } // endfor (j,k) operations
            
            // delete from B all cols with index in pMinus
            for (int ww = 0; ww < pMinusLength; ww++) {
               B = B.eliminateCol(pMinus[ww]-1);
            }
            
         } // endif
      } // end while
      // System.out.println("\nAfter column transformations in phase 2 (non-minimal generating set) B:");
      // B.print(2, 0);
      
      // delete from B all cols having non minimal support
      // k is the index of column to be eliminated, if it is -1 then there is 
      // no col to be eliminated
      int k = 0;
      // form a matrix with columns the row indices of non-zero elements
      PNMatrix Bi = B.nonZeroIndices();
      
      while (k > -1) {
         k = Bi.findNonMinimal();
         
         if (k != -1) {
            B = B.eliminateCol(k);
            Bi = B.nonZeroIndices();
         }
      }
      
      // display the result
      // System.out.println("Minimal generating set (after phase 2):");
      // B.print(2, 0);
      return B;
   }
   
   
   /** find the number of non-zero elements in a set
    *
    * @param pSet  The set count the number of non-zero elements.
    * @return      The number of non-zero elements.
    * */
   private int effectiveSetLength(int[] pSet) {
      int effectiveLength = 0; // number of non-zero elements
      int setLength = pSet.length;
      
      for (int i = 0; i < setLength; i++) {
         if (pSet[i] != 0) {
            effectiveLength++;
         } else {
            return effectiveLength;
         }
      }
      return effectiveLength;
   }
   
   
   /** adjust linear combination coefficients according to sign
    *  if sign(j) <> sign(k) then alpha = abs(j) beta = abs(k)
    *  if sign(j) == sign(k) then alpha = -abs(j) beta = abs(k)
    *
    * @param k  The column index of the first coefficient
    * @param j  The column indices of the remaining coefficients
    * @return   The adjusted alpha coefficients
    */
   private int[] alphaCoef(int k, int[] j) {
      int n = j.length; // the length of one row
      int[] alpha = new int[n];
      
      for (int i = 0; i < n; i++) {
         if ((k * j[i]) < 0) {
            alpha[i] = Math.abs(j[i]);
         } else {
            alpha[i] = -Math.abs(j[i]);
         }
      }
      return alpha;
   }
   
   
   /** adjust linear combination coefficients according to sign
    *  if sign(j) <> sign(k) then alpha = abs(j) beta = abs(k)
    *  if sign(j) == sign(k) then alpha = -abs(j) beta = abs(k)
    *
    * @param chk  The first coefficient
    * @param n    The length of one row
    * @return     The adjusted beta coefficients
    */
   private int[] betaCoef(int chk, int n) {
      int[] beta = new int[n];
      int abschk = Math.abs(chk);
      
      for (int i = 0; i < n; i++) {
         beta[i] = abschk;
      }
      return beta;
   }

   
   private void resetArray(int [] a) {
      for (int i = 0; i < a.length; i++) {
         a[i] = 0;
      }
   }
   
   
   /** Unite two sets (arrays of integers) so that if there is a common entry in
    * the arrays it appears only once, and all the entries of each array appear 
    * in the union. The resulting array size is the same as the 2 arrays and 
    * they are both equal. We are only interested in non-zero elements. One of 
    * the 2 input arrays is always full of zeros.
    *
    * @param A  The first set to unite.
    * @param B  The second set to unite.
    * @return   The union of the two input sets.
    * */
   private int[] uniteSets(int [] A, int [] B) {
      int [] union = new int[A.length];
      
      if (isEmptySet(A)) {
         union = B;
      } else{
         union = A;
      }
      return union;
   }

   
   /** check if an array is empty (only zeros)
    *
    * @param pSet  The set to check if it is empty.
    * @return      True if the set is empty.
    * */
   private boolean isEmptySet(int [] pSet) {
      int setLength = pSet.length;
      
      for (int i = 0; i < setLength; i++) {
         if (pSet[i] != 0) {
            return false;
         }
      }
      return true;
   }
   
   /** used to display intermiadiate results for checking
    *
    * @param a The array to print.
    * */
//  public void printArray(int[] a){
//    int n = a.length;
//
//    for (int i = 0; i < n; i++)
//      System.out.print(a[i] + " ");
//    System.out.println();
//  }
   
   /** Shorten spelling of print.
    * @param s The string to print.
    */
//  private void print (String s) {
//    System.out.print(s);
//  }
   
   /** Format double with Fw.d.
    *
    * @param x  The format of the string.
    * @param w  The length of the string.
    * @param d  The number of fraction digits.
    * @return   The string to print.
    * */
//  public String fixedWidthDoubletoString (double x, int w, int d) {
//    java.text.DecimalFormat fmt = new java.text.DecimalFormat();
//    fmt.setMaximumFractionDigits(d);
//    fmt.setMinimumFractionDigits(d);
//    fmt.setGroupingUsed(false);
//    String s = fmt.format(x);
//    while (s.length() < w) {
//      s = " " + s;
//    }
//    return s;
//  }
   
}
