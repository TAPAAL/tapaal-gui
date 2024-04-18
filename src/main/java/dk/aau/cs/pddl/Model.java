package dk.aau.cs.pddl;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.*;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
    private String name;
    private ArrayList<Extension> extensions = new ArrayList<>();
    private HashMap<String, UserType> types = new HashMap<>();
    private HashMap<String, FunctionSignature> functions = new HashMap<>();
    private HashMap<String, Predicate> predicates = new HashMap<>();
    private HashMap<String, ActionSchema> actionSchemas = new HashMap<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(ArrayList<Extension> extensions) {
        this.extensions = extensions;
    }


    public HashMap<String, UserType> getTypes() {
        return types;
    }

    public void setTypes(HashMap<String, UserType> types) {
        this.types = types;
    }


    public HashMap<String, FunctionSignature> getFunctions() {
        return functions;
    }

    public void setFunctions(HashMap<String, FunctionSignature> functions) {
        this.functions = functions;
    }


    public HashMap<String, Predicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(HashMap<String, Predicate> predicates) {
        this.predicates = predicates;
    }

    public HashMap<String, ActionSchema> getActionSchemas() {
        return actionSchemas;
    }

    public void setActionSchemas(HashMap<String, ActionSchema> actionSchemas) {
        this.actionSchemas = actionSchemas;
    }




    public Model() { }


    //region Parse from CPN

    public void parse(
        TimedArcPetriNetNetwork network,
        Iterable<Template> templates,
        Iterable<TAPNQuery> queries,
        Iterable<Constant> constants,
        TAPNLens lens
    ) {
        this.parseName(network);
        this.parseExtensions();
        this.parseTypes(network);
        this.parseFunctions(network);
        this.parseActionSchemas(network.allTemplates().get(0));
    }

    public void parse(LoadedModel ptModel) {
        this.parse(
            ptModel.network(),
            ptModel.templates(),
            ptModel.queries(),
            ptModel.network().constants(),
            ptModel.getLens()
        );
    }

    private void parseName(TimedArcPetriNetNetwork network) {
        this.name = network.allTemplates().get(0).name();
    }

    private void parseExtensions() {
        this.extensions.add(new Extension("typing"));
        this.extensions.add(new Extension("fluents"));
    }

    private void parseTypes(TimedArcPetriNetNetwork network) {
        List<ColorType> colorTypes = network.colorTypes();
        for (ColorType colorType : colorTypes) {
            if(colorType instanceof ProductType)
                continue;
            this.types.put(colorType.getName(), new UserType(colorType.getName()));
        }
    }


    private void makeParameters() {

    }
    private void parseFunctions(TimedArcPetriNetNetwork network) {
        for (TimedPlace place : network.allTemplates().get(0).places()) {

            // Parameters
            ArrayList<Parameter> parameters = new ArrayList<>();

            ColorType placeType = place.getColorType();
            if (placeType instanceof ProductType) {
                int i=1;
                for (ColorType subType : placeType.getProductColorTypes()) {
                    parameters.add(new Parameter("x"+i, this.types.get(subType.getName())));
                    i++;
                }
            }
            else {
                parameters.add(new Parameter("x", this.types.get(placeType.getName())));
            }

            // Name
            String name = place.name();

            // Function
            this.functions.put(name, new FunctionSignature(name, parameters));
        }
    }


    public void parseActionSchemas(TimedArcPetriNet petriNet) {
        var transitions = petriNet.transitions();

        for(var t: transitions) {
            actionSchemas.put(t.name(), this.parseActionSchema(t));
        }
    }

    public ActionSchema parseActionSchema(TimedTransition transition) {
        ActionSchema actionSchema = new ActionSchema(transition);


        return actionSchema;
        // Place x Color x (out - in)
//        HashMap<Place, HashMap<Color, Integer>> effects = new HashMap<>();
//
//        List<TimedInputArc> inArcs = transition.getInputArcs();
//        List<TimedOutputArc> outArcs = transition.getOutputArcs();
//
//        for(var arc: inArcs) {
//            var exp = arc.getArcExpression();
//            System.out.println(exp.toString());
//        }
//
//
//
//        // Parameters = distinct places in arcs
//
//        // Pre = in arcs
//
//        // Effects = outArcs - inArcs
//
//        Function func = new Function(transition.name());
//
//
//        return actionSchema;
    }


    //region Parameters
//    public void
    //endregion

    //endregion

}
