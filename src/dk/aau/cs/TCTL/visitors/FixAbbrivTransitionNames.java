/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.util.Tuple;
import java.util.ArrayList;

public class FixAbbrivTransitionNames extends VisitorBase {

        private ArrayList<Tuple<String, String>> templateTransitionNames;

        public FixAbbrivTransitionNames(ArrayList<Tuple<String, String>> templateTransitionNames) {
                this.templateTransitionNames = templateTransitionNames;
        }

        public void visit(TCTLTransitionNode transitionNode, Object context) {
                if (transitionNode.getTemplate().equals("")
                        && !templateTransitionNames.contains(new Tuple<String, String>(transitionNode.getTemplate(), transitionNode.getTransition()))) {
                        for (Tuple<String, String> transition : templateTransitionNames) {
                                if (transitionNode.getTransition().equals(transition.value2())){
                                        transitionNode.setTemplate(transition.value1());
                                }
                        }
                }
        }
        
        public static void fixAbbrivTransitionNames(
                ArrayList<Tuple<String, String>> templateTransitionNames,
                TCTLAbstractProperty query){
                
                query.accept(new FixAbbrivTransitionNames(templateTransitionNames), null);
        }

}
