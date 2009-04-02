/*
 * ResultsProvider.java
 *
 * Created on 14 / agost / 2007, 10:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

import pipe.dataLayer.DataLayer;
import pipe.modules.stateSpace.StateSpace;
import pipe.modules.classification.Classification;
import pipe.dataLayer.calculations.TreeTooBigException;
import pipe.modules.EmptyNetException;
import pipe.dataLayer.PNMatrix;
import pipe.modules.invariantAnalysis.InvariantAnalysis;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.modules.gspn.GSPNNew;

import pipe.dataLayer.calculations.StateList;

import pipe.dataLayer.DataLayer;

/**
 *
 * @author marc
 */
class ResultsProvider {
    
    DataLayer sourceDataLayer; 
    
    private double averageTokens []; //where the average tokens for each place will be stored
    PNMatrix Presult; //where PInvariants will be stored
    PNMatrix Tresult; //where TInvariants will be stored
    
    private boolean stateSpaceResult [];
    private boolean classificationResult [];
    
    GSPNNew gspn;
    private double tokenDist[][];
    private double throughput[];
    
    double pi[] = null;
    StateList tangibleStates = null;
    
    /** Creates a new instance of ResultsProvider */
    protected ResultsProvider(DataLayer sourceDataLayer) {
        gspn = new GSPNNew();
        this.sourceDataLayer = sourceDataLayer;
        Object[] pi_tang = gspn.getPiAndTangibleStates(sourceDataLayer);
        this.pi = (double [])pi_tang[0];
        this.tangibleStates = (StateList)pi_tang[1];
    }
    
    protected void netChanged(){
        Object[] pi_tang = gspn.getPiAndTangibleStates(sourceDataLayer);
        this.pi = (double [])pi_tang[0];
        this.tangibleStates = (StateList)pi_tang[1];
    }
    
    
    protected void runStateSpace(){
        StateSpace ss = new StateSpace();
        try{
           stateSpaceResult = ss.getStateSpace(sourceDataLayer);
        }catch(TreeTooBigException e){
            System.out.println("Too many states.");
        }catch(EmptyNetException e){
            System.out.println("Empty net.");
        }
    }
    protected boolean [] getStateSpace(){
        return stateSpaceResult;
    }
    
    protected void runClassification(){
        Classification c = new Classification();
        try{
            classificationResult = c.getClassification(sourceDataLayer);
        }catch(EmptyNetException e){
            System.out.println("Empty net.");
        }
    }
    
    protected boolean[] getClassification(){
        return classificationResult;
    }
    
    protected int [] getPInvariant(String placeName){
        int result[] = new int[Presult.getColumnDimension()];
        if (result != null){
            int position = sourceDataLayer.getListPosition(sourceDataLayer.getPlaceByName(placeName));
            for(int j=0; j < Presult.getColumnDimension(); j++){
                        result[j]=Presult.get(position,j);
            }
        }
        return result;
    }
    
    protected int[][] getPInvariants(){
        if (Presult != null){
            int result[][] = new int[Presult.getColumnDimension()][Presult.getRowDimension()];
            for (int i=0; i < Presult.getColumnDimension(); i++){
                for(int j=0; j < Presult.getRowDimension(); j++){
                    //i invariant, j place
                    result[i][j] = Presult.get(j,i);
                }
            }
            return result;
        }else{
            return null;
        }
    }
    
    protected int [][] getTInvariants(){
        if (Presult != null){
            int result[][] = new int[Tresult.getColumnDimension()][Tresult.getRowDimension()];
            for (int i=0; i < Tresult.getColumnDimension(); i++){
                for(int j=0; j < Tresult.getRowDimension(); j++){
                    //i invariant, j place
                    result[i][j] = Tresult.get(j,i);
                }
            }
            return result;
        }else{
            return null;
        }
    }
    
    protected int [] getTInvariant(String transitionName){
        int result[]=null;
        if(Tresult!=null){
            result = new int[Tresult.getColumnDimension()];
            int position = sourceDataLayer.getListPosition(sourceDataLayer.getTransitionByName(transitionName));
            for(int j=0; j < Tresult.getColumnDimension(); j++){
                        result[j]=Tresult.get(position,j);
            }
        }
        return result;
    }
    protected void runInvariant(){
        InvariantAnalysis ia = new InvariantAnalysis();
        Presult = ia.getPInvariants(sourceDataLayer);
        Tresult = ia.getTInvariants(sourceDataLayer);
    }
    
    protected int[] getStateSpaceList(String placeName){
        int position = sourceDataLayer.getListPosition(sourceDataLayer.getPlaceByName(placeName));
        int result[] = null;
        if (position >= 0){
            result = new int[tangibleStates.size()];
            for (int i = 0; i < tangibleStates.size(); i++){
                result[i]=tangibleStates.get(i)[position];
            }
        }
        return result;
    }
    
    protected void printStateSpaceList(){
      int markSize = tangibleStates.get(0).length;
      
      /*Place[] places = sourceDataLayer.getPlaces();
      for (int i = 0; i < markSize; i++) {
         result.add(places[i].getName());
      }*/
      
      for (int i = 0; i < tangibleStates.size(); i++) {
         for (int j = 0; j < markSize; j++) {
            System.out.print("M"+i+"("+sourceDataLayer.getPlaces()[j].getName()+")=");
            System.out.println(Integer.toString(tangibleStates.get(i)[j]));
         }
      }
    }
    
    protected void printStateProb(){
      
      for (int i = 0; i < pi.length; i++){
          System.out.print("Prob("+tangibleStates.getID(i)+")=");
          System.out.println(pi[i]);
      }
    }
    
    protected double getAverageTokens(String placeName){
        if (averageTokens != null){
            int position = sourceDataLayer.getListPosition(sourceDataLayer.getPlaceByName(placeName));
            return averageTokens[position];
        }else{
            return -1.0;
        }        
    }
    
    protected void runAverageTokens(){
        System.out.println(sourceDataLayer.getListPosition(sourceDataLayer.getPlaceByName("P0")));
        System.out.println(sourceDataLayer.getListPosition(sourceDataLayer.getTransitionByName("T3")));
        try{
            averageTokens=gspn.getAverageTokens(sourceDataLayer,pi,tangibleStates);
            if(averageTokens!=null){
                System.out.println();
                for(int i = 0; i < averageTokens.length; i++){
                    System.out.print(averageTokens[i]+" ");
                }
                System.out.println();
            }
        }catch(pipe.modules.gspn.GSPNNew.NoTimedTransitionsException e){
            System.out.println("This Petri net has no timed transitions, " +
                    "so GSPN analysis cannot be performed.");
        }
    }
    
    protected void runTokenDist(){
        try{
            double probabilities [][] = gspn.getTokenDistribution(sourceDataLayer, pi, tangibleStates);
            //if(probabilities.length == 0) System.out.println("n/a");
            /*int rows = probabilities.length;
            int cols = probabilities[0].length;
            Place [] places = sourceDataLayer.getPlaces();
            DecimalFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(5);
            /*for (int i = 0; i < rows; i++) {
            System.out.println(places[i].getName());
            for (int j = 0; j < cols; j++) {
                System.out.println(f.format(probabilities[i][j]));
             }
            }*/
            tokenDist = probabilities;    
         }catch(pipe.modules.gspn.GSPNNew.NoTimedTransitionsException e){
            System.out.println("This Petri net has no timed transitions, " +
                    "so GSPN analysis cannot be performed.");
        }
        
    }
    
    protected double [] getTokenDist(String placeName){
        int position = sourceDataLayer.getListPosition(sourceDataLayer.getPlaceByName(placeName));
        System.out.println(position);
        if (tokenDist!=null){
            return tokenDist[position];
        }else{
            return null;
        }
    }
    
    protected void runThroughput(){
        try{
            throughput = gspn.getThroughput(sourceDataLayer,pi, tangibleStates);
        }catch(pipe.modules.gspn.GSPNNew.NoTimedTransitionsException e){
            System.out.println("This Petri net has no timed transitions, " +
                    "so GSPN analysis cannot be performed.");
        }
    }
    
    protected double getThroughput(String transitionName){
        int position = sourceDataLayer.getListPosition(sourceDataLayer.getTransitionByName(transitionName));
        return throughput[position];
    }
    
}
