package dk.aau.cs.pddl;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.pddl.expression.*;
import dk.aau.cs.verification.QueryType;
import net.tapaal.gui.petrinet.TAPNLens;
import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.util.*;
import java.util.stream.Collectors;

public class Model {
    private String name;
    private ArrayList<Extension> extensions = new ArrayList<>();
    private HashMap<String, UserType> types = new HashMap<>();
    private HashMap<String, FunctionSignature> functions = new HashMap<>();
    private ArrayList<Expression_Predicate> predicates = new ArrayList<>();
    private HashMap<String, ActionSchema> actionSchemas = new HashMap<>();
    private HashMap<String, QueryParser> queries = new HashMap<>();

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


    public ArrayList<Expression_Predicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(ArrayList<Expression_Predicate> predicates) {
        this.predicates = predicates;
    }

    public HashMap<String, ActionSchema> getActionSchemas() {
        return actionSchemas;
    }

    public void setActionSchemas(HashMap<String, ActionSchema> actionSchemas) {
        this.actionSchemas = actionSchemas;
    }

    public HashMap<String, QueryParser> getQueries() {
        return this.queries;
    }
    private PlaceWeights state;

    public PlaceWeights getState() {
        return state;
    }

    public Model() {}

    public Model(LoadedModel petriNet) {
        parse(
            petriNet.network(),
            petriNet.templates(),
            petriNet.queries(),
            petriNet.network().constants(),
            petriNet.getLens()
        );
    }

    public Model(LoadedModel petriNet, Iterable<TAPNQuery> queries) {
        parse(
            petriNet.network(),
            petriNet.templates(),
            queries,
            petriNet.network().constants(),
            petriNet.getLens()
        );
    }

    //region Parse from CPN

    public void parse(
        TimedArcPetriNetNetwork network,
        Iterable<Template> templates,
        Iterable<TAPNQuery> queries,
        Iterable<Constant> constants,
        TAPNLens lens
    ) {
        TimedArcPetriNet firstNet = network.allTemplates().get(0);

        this.parseName(firstNet);
        this.parseExtensions();
        this.parseTypes(network);
        this.setPredicates(this.createSiblingPredicates(network));
        this.parseFunctions(firstNet);
        this.parseActionSchemas(network.allTemplates().get(0));
        state = this.parseInitialState(firstNet);


        this.parseQueries(firstNet, queries);
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

    private void parseName(TimedArcPetriNet net) {
        this.name = net.name();
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
            this.types.put(colorType.getName(), new UserType(colorType));
        }
    }


    private void makeParameters() {

    }
    private void parseFunctions(TimedArcPetriNet net) {
        for (TimedPlace place : net.places()) {

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
    }

    public ArrayList<Expression_Predicate> createSiblingPredicates(TimedArcPetriNetNetwork network) {
        ArrayList<Expression_Predicate> predicates = new ArrayList<>();

        for (ColorType colorType : network.colorTypes()) {
            if(colorType instanceof ProductType)
                continue;

            var colorIter = colorType.iterator();
            var firstColor = colorIter.next();
            var prevColor = firstColor;

            while(colorIter.hasNext()) {
                var nextColor = colorIter.next();

                predicates.add(new Expression_Predicate_IsPredecessor(prevColor, nextColor));
                predicates.add(new Expression_Predicate_IsSuccessor(nextColor, prevColor));

                prevColor = nextColor;
            }

            // Link first and last color
            predicates.add(new Expression_Predicate_IsPredecessor(prevColor, firstColor));
            predicates.add(new Expression_Predicate_IsSuccessor(firstColor, prevColor));

        }

        return predicates;
    }

    public PlaceWeights parseInitialState(TimedArcPetriNet petriNet) {

        PlaceWeights placeWeights = new PlaceWeights();

        for (TimedPlace place : petriNet.places()) {
            Weights weights = new Weights();

            var allPossibleColors = util.getAllPossibleColors(place);
            for(ArrayList<Color> colors : allPossibleColors) {
                var valueList = colors.stream().map(Expression_ColorLiteral::new).collect(Collectors.toCollection(ArrayList<IExpression_Value>::new));
                weights.put(valueList, 0);
            }

            Map<TimedToken, Long> marking = place.tokens().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
            for(var entry: marking.entrySet()) {
                TimedToken token = entry.getKey();
                var color = token.getColor();
                int weight = entry.getValue().intValue();
                var colors = color.getTuple();
                if(colors == null)
                    colors = new Vector<>() {{ add(color); }};

                var valueList = colors.stream().map(Expression_ColorLiteral::new).collect(Collectors.toCollection(ArrayList<IExpression_Value>::new));
                weights.put(valueList, weight);
            }


            placeWeights.put(place, weights);
        }

        return placeWeights;
    }



    private void parseQueries(TimedArcPetriNet net, Iterable<TAPNQuery> queries) {
        int queryIndex = 1;
        for(var query: queries) {
            queryIndex++;
            if(query.getCategory().name() != "CTL")
                continue;

            if(!query.queryType().equals(QueryType.EF))
                continue;

            try {
                var parser = new QueryParser(net, queryIndex);
                parser.parseQuery(query);
                this.queries.put(query.getName(), parser);

            }
            catch(UnhandledExpressionType e) {
                continue;
            }
        }
    }

    //region Parameters
//    public void
    //endregion

    //endregion

}
