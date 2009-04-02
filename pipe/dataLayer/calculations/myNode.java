/*
 * Created on Feb 10, 2004
 */
package pipe.dataLayer.calculations;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import pipe.dataLayer.calculations.myNode;

import pipe.gui.Pipe;
import pipe.io.ImmediateAbortException;
import pipe.io.StateRecord;
import pipe.io.TransitionRecord;


/**
 * @author Matthew Cook after James Bloom/Clare Clark
 * Class used in state space and GSPN modules for generation of trees and arrays 
 * of possible state spaces
 */
public class  myNode {
   
   boolean[] transArray;      // array of transitions
   myNode parent;             // parent node
   myNode[] children;         // child nodes
   myTree tree;               // tree that contains the node
   int[] markup;              // The marking of the node
   myNode previousInstance;   // same node found in tree
   int depth;                 // The depth this node is in the tree
   int id;                    // Node id
   

   
   /**
    * Function: void myNode(int[] markingArray, myNode parentNode, myTree atree)
    * The Node constructor called by a node object
    */
   public myNode(int [] markingArray, myNode parentNode, myTree atree, 
            int treeDepth) {
      
      //Node now knows its tree in order to use its attributes
      tree = atree;
      
      depth = treeDepth;
      
      // id of a node is the number of different states generated
      id = tree.states; 
      
      // Update the count of nodes
      tree.nodeCount++;
      
      // Set up marking for this node
      markup = markingArray;  //created in myTree
      
      // Assign the nodes parents
      parent = parentNode;
      
      //System.out.print("Node " + tree.nodeCount + " Created \n" );
      
      // Create an array of transitions for each node
      transArray = new boolean[tree.transitionCount];
      
      // Create an array of nodes for each node (for children)
      // Number of children limited to total number of transitions.
      children = new myNode[tree.transitionCount];

      //Initialise transArray
      for (int i = 0; i < tree.transitionCount; i++) {
         transArray[i] = false;
      }
      
      //print("Markup: ", markup);
   }   

   
   /**
    * Function: int[] fire(int trans)
    * Produces a new markup vector to simulate the firing of a transition.
    * Destroys the number of tokens shown in CMinus for a given place and
    * transition, and creates the number of tokens shown in CPlus.
    * TransIndex refers to the actual transition number ie starting at 1.
    */
   private int[] fire(int transIndex) {
      int CMinusValue;               //Value from C- matrix
      int CPlusValue;                //Value from C+ matrix
      
      //Create marking array to return
      int[] marking = new int[tree.placeCount];
      
      //System.out.println("\nFire transition " + transIndex);
      for (int count = 0; count < tree.placeCount; count++) {
         CMinusValue = (tree.CMinus).get(count, (transIndex));
         CPlusValue = (tree.CPlus).get(count, (transIndex));
         
         if (markup[count] != -1) {
            marking[count] = markup[count] - CMinusValue + CPlusValue;
         } else {
            marking[count] = markup[count];
         }
      }
      
      //print( "Markup: ", marking); //debug
      
      //Return this new marking to RecursiveExpansion function
      return marking;
   }
   
   
   /**
    * Function: void RecursiveExpansion()
    * Undertakes a recursive expansion of the tree
    * Called on root node from within the tree constructor.
    */
    public void RecursiveExpansion() throws TreeTooBigException {
       int transIndex;                       //Index to count transitions
       int[] newMarkup;                      //markup used to create new node
       boolean aTransitionIsEnabled = false; //To check for deadlock
       //Attribute used for assessing whether a node has occured before
       boolean repeatedNode = false;       
       
       boolean allOmegas = false;
      
       boolean[] enabledTransitions = 
               tree.dataLayer.getTransitionEnabledStatusArray(markup);

       //For each transition
       for (int i = 0; i < enabledTransitions.length; i++) {
          if (enabledTransitions[i] == true) {
             //Set transArray of to true for this index
             transArray[i] = true;
             
             //System.out.println("\n Transition " + i + " Enabled" );
             aTransitionIsEnabled = true;
             
             //print ("\n Old Markup is :", markup);//debug
             
             //Fire transition to produce new markup vector
             newMarkup = fire(i);
            
             //print ("\n New Markup is :", newMarkup);//debug
             
             //Check for safeness. If any of places have > 1 token set variable.
             for (int j = 0; j < newMarkup.length; j++) {
                if (newMarkup[j] > 1 || newMarkup[j] == -1) {
                   tree.moreThanOneToken = true;
                   break;
                }
             } 
            
             //Create a new node using the new markup vector and attach it to
             //the current node as a child.
             children[i] = 
                     new myNode(newMarkup, this, tree, depth + 1);
            
             /* Now need to (a) check if any omegas (represented by -1) need to 
              * be inserted in the markup vector of the new node, and (b) if the 
              * resulting markup vector has appeared anywhere else in the tree. 
              * We must do (a) before (b) as we need to know if the new node 
              * contains any omegas to be able to compare it with existing nodes. 
              */            
             allOmegas = children[i].InsertOmegas();
            
             //check if the resulting markup vector has occured anywhere else in the tree
             repeatedNode = (tree.root).FindMarkup(children[i]);
            
             if (tree.nodeCount >= Pipe.MAX_NODES && !tree.tooBig) {
                tree.tooBig = true;
                throw new TreeTooBigException();
             }
            
             if (!repeatedNode && !allOmegas) {
                children[i].RecursiveExpansion();
             }
          }
       }
      
       if (!aTransitionIsEnabled) {
          System.err.println("No transition enabled");
          if (!tree.noEnabledTransitions || tree.pathToDeadlock.length < depth-1) {
             RecordDeadlockPath();
             tree.noEnabledTransitions = true;
          } else {
             System.err.println("Deadlocked node found, but path is not shorter"
                     + " than current path.");
          }
       } else {
          //System.out.println("Transitions enabled.");
       }
    }
 
          
   /**
    * Function: void RecordDeadlockPath()
    * If there is a deadlock, calculates the path
    */
   public void RecordDeadlockPath() {
      myNode currentNode;      //The current node we're considering
      int pos;                 //position in path array
      int i;
      
      //Set up array to return
      tree.pathToDeadlock = new int[depth - 1];
      pos = depth - 2; // Start filling in at the end of the array
      
      currentNode = this;
      
      //For each ancestor node until root
      while(currentNode != tree.root) {
         // Work out which transition we followed to get to to currentNode
         loop: for(i = 0; i < tree.transitionCount; i++) {
            if (currentNode.parent.transArray[i]
                     && currentNode.parent.children[i] == currentNode ) {
               // That's the one!
               break loop;
            }
         }
         
         tree.pathToDeadlock[pos] = i;
         pos--;
         //Update current node to look at an earlier ancestor
         currentNode = currentNode.parent;
      }
      
      //print("Path to deadlock is: ", tree.pathToDeadlock);//debug
   }
   
   
   /**
    * Function: void InsertOmegas()
    * Checks if any omegas need to be inserted in the places of a given node.
    * Omegas (shown by -1 here) represent unbounded places and are therefore
    * important when testing whether a petri net is bounded. This function
    * checks each of the ancestors of a given node.
    * Returns true iff all places now contain an omega.
    */
   public boolean InsertOmegas() {
      //Attributes used for assessing boundedness of the net
      boolean allElementsGreaterOrEqual = true;
      boolean insertedOmega = false;
      myNode ancestorNode;       //one of the ancestors of this node
      
      //Set up array used for checking boundedness
      boolean [] elementIsStrictlyGreater = new boolean[tree.placeCount];
      
      //Initialise this array to false
      for (int i = 0; i < tree.placeCount; i++) {
         elementIsStrictlyGreater[i] = false;
      }
      
      ancestorNode = this;
      
      //For each ancestor node until root
      while (ancestorNode != tree.root && !insertedOmega) {
         //Update ancestor node to look at an earlier ancestor
         ancestorNode = ancestorNode.parent;
         
         allElementsGreaterOrEqual = true;
         
         //compare markup of this node with that of each ancestor node
         //for each place in the markup
         loop: for (int i = 0; i < tree.placeCount; i++) {
            if (markup[i] != -1) {
               //If M(p) for new node less than M(p) for ancestor
               if (markup[i] < ancestorNode.markup[i]) {
                  allElementsGreaterOrEqual = false;
                  break loop;
               }
               
               //If M(p) for new node strictly greater than M(p) for ancestor
               elementIsStrictlyGreater[i] = (markup[i] > ancestorNode.markup[i]);
            }
         }
         
         //Now assess the information obtained for this node
         if (allElementsGreaterOrEqual == true) {
            //for each place p in the markup of this node
            for (int p = 0; p < tree.placeCount; p++) {
               // Check if there is an inhibition
               boolean inhibition = false;
               for (int t = 0; t < tree.transitionCount; t++) {
                  if ((tree.inhibition.get(p, t) > 0) &&
                          (markup[p] <= tree.inhibition.get(p, t))) {
                     inhibition = true;
                     break;
                  }
               }
               
               if (!inhibition) { 
                  if (markup[p] != -1 &&
                          elementIsStrictlyGreater[p] &&
                          tree.capacity[p] == 0) {
                     //Set M(p) in this new markup to be omega
                     markup[p] = -1;
                     insertedOmega = true;
                     //print("\n Omega added", markup); //debug
                     
                     //Set variable in tree to true - net unbound
                     tree.foundAnOmega = true;
                  }
               }
            }
         }
      }
      
      for (int i = 0; i < tree.placeCount; i++) {
         if (markup[i] != -1) {
            return false;
         }
      }
      
      return true; // All places have omegas
   }

   
   /**
    * Function: boolean FindMarkup(int[] mark)
    * Checks if the markup has occured previously in the tree. This means
    * previously during the creation of the tree, and not just on the current
    * branch as a direct ancestor of the node. Function updates arrays of end
    * nodes stored in the tree class for the liveness tests. Explore the tree
    * by starting with the root node and investigating the children.
    */
   public boolean FindMarkup(myNode n) {
      if (n == this) {
         return false;
      }
           
      if (MarkupCompare(n.markup)) {         
         //n.previousInstance = this;
         if (this.previousInstance != null) {
            n.previousInstance = this.previousInstance;
         } else {
            n.previousInstance = this;
         }         
         //System.out.println("\nFound duplicate markup!\n");
         return true;
      }
      
      for (int i = 0; i < tree.transitionCount; i++) {
         if (transArray[i]) {
            if (children[i].FindMarkup(n)) {
               return true;
            }
         }
      }
      return false;
   }
   
   
   /**
    * Function: boolean MarkupCompare(int[] check)
    * Takes two integer arrays (Markups) and compares the values.
    * Returns a boolean showing the results of the comparison.
    */
   public boolean MarkupCompare(int[] check) {
      //Comparison only possible on same length arrays
      if (this.markup.length == check.length) {
         for (int i = 0; i < markup.length; i++) {
            if (this.markup[i] != check[i]) {
               return false;
            }
         }
         return true;
      }
      return false;
   }
   
   
   //Temp function for debugging - delete when done.
   public void print(String s, boolean[] array) {
      int size = array.length;
      
      System.out.println(s);
      for (int i = 0; i < size ; i++) {
         System.out.print( array[i] +" ");
      }
      System.out.println();
   }
   
   
   //Temp function for debugging - delete when done.
   public void print(String s, int[] array) {
      int size = array.length;
      
      System.out.println(s);
      for (int i = 0; i < size ; i++) {
         System.out.print( array[i] +" ");
      }
      System.out.println();
   }   
   
}
