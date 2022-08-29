package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.util.Tuple;
import java.util.ArrayList;

public class FixAbbrivPlaceNames extends VisitorBase {

        private final ArrayList<Tuple<String, String>> templatePlaceNames;

        public FixAbbrivPlaceNames(ArrayList<Tuple<String, String>> templatePlaceNames) {
                this.templatePlaceNames = templatePlaceNames;
        }

        public void visit(TCTLPlaceNode placeNode, Object context) {
                if (placeNode.getTemplate().equals("")
                        && !templatePlaceNames.contains(new Tuple<String, String>(placeNode.getTemplate(), placeNode.getPlace()))) {
                        for (Tuple<String, String> place : templatePlaceNames) {
                                if (placeNode.getPlace().equals(place.value2())){
                                        placeNode.setTemplate(place.value1());
                                }
                        }
                }
        }
        
        public static void fixAbbrivPlaceNames(
                ArrayList<Tuple<String, String>> templatePlaceNames,
                TCTLAbstractProperty query){
                
                query.accept(new FixAbbrivPlaceNames(templatePlaceNames), null);
        }

}
