package dk.aau.cs.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.AddExpression;
import dk.aau.cs.model.CPN.Expressions.AllExpression;
import dk.aau.cs.model.CPN.Expressions.AndExpression;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.DotConstantExpression;
import dk.aau.cs.model.CPN.Expressions.EqualityExpression;
import dk.aau.cs.model.CPN.Expressions.GreaterThanEqExpression;
import dk.aau.cs.model.CPN.Expressions.GreaterThanExpression;
import dk.aau.cs.model.CPN.Expressions.GuardExpression;
import dk.aau.cs.model.CPN.Expressions.InequalityExpression;
import dk.aau.cs.model.CPN.Expressions.LessThanEqExpression;
import dk.aau.cs.model.CPN.Expressions.LessThanExpression;
import dk.aau.cs.model.CPN.Expressions.NotExpression;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.CPN.Expressions.OrExpression;
import dk.aau.cs.model.CPN.Expressions.PredecessorExpression;
import dk.aau.cs.model.CPN.Expressions.ScalarProductExpression;
import dk.aau.cs.model.CPN.Expressions.SubtractExpression;
import dk.aau.cs.model.CPN.Expressions.SuccessorExpression;
import dk.aau.cs.model.CPN.Expressions.TupleExpression;
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression;
import dk.aau.cs.model.CPN.Expressions.VariableExpression;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.NameTransformer;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class LoadTACPN { //the import feature for CPN and load for TACPN share similarities. These similarities are shared here. Feel free to find a better name for this class

    private final HashMap<String, ColorType> colortypes = new HashMap<>();
    private final HashMap<String, Variable> variables = new HashMap<>();
    private final HashMap<String, ColorExpression> tupleVarExpressions = new HashMap<>();
    private final Collection<String> messages = new ArrayList<>(10);
    private final Collection<Node> productTypes = new ArrayList<>();
    private final NameTransformer nc = new NameTransformer();

    public HashMap<String, ColorType> getColortypes() {
        return colortypes;
    }

    public HashMap<String, Variable> getVariables() {
        return variables;
    }

    public Collection<String> getMessages() {return  messages;}
    //skipWS
    //  Takes a given node and returns it if it is an element,
    //  otherwise returns its first element sibling.
    //  This allows skipping past whitespace nodes.
    private static Node skipWS(Node node) {
        if (node != null && !(node instanceof Element)) {
            return skipWS(node.getNextSibling());
        } else {
            return node;
        }
    }

    private static Node getAttribute(Node node, String attribute) {
        return node.getAttributes().getNamedItem(attribute);
    }

    private Element expectElementTagOrEmpty(String tag, Node node) throws FormatException {
        if (node == null) {
            return null;
        }

        if (!(node instanceof Element) || !node.getNodeName().equals(tag)) {
            throw new FormatException("Unexpected node, expected " + tag + " got " + node.getNodeName());
        }
        return (Element)node;
    }

    private void forEachElementInNodeList(NodeList list, Consumer<Element> function) {
        for (int i = 0; i < list.getLength(); i++) {
            var node = list.item(i);
            if (node instanceof Element) {
              var el = ((Element) node);
              function.accept(el);
            }
        }
    }

    public void parseDeclarations(Node node, TimedArcPetriNetNetwork network) throws FormatException {
        var declaration = expectElementTagOrEmpty("declaration", node);
        var structure = expectElementTagOrEmpty("structure", skipWS(declaration.getFirstChild()));
        var declarations = expectElementTagOrEmpty("declarations", skipWS(structure.getFirstChild()));

        var namedSorts = declarations.getElementsByTagName("namedsort");

        forEachElementInNodeList(namedSorts, element -> {
            parseNamedSort(element, network);
        });

        // Handle product colors after all other color types are parsed.
        for (Node n : productTypes) {
            var type = skipWS(n.getFirstChild());
            String typeTag = type.getNodeName();
            if (!typeTag.equals("productsort")) throw new FormatException("Expected productsort type got: " + typeTag);
            String name = nc.transform(getAttribute(n, "name").getNodeValue());
            String id = getAttribute(n, "id").getNodeValue();

            ProductType pt = new ProductType(name);
            Node typechild = skipWS(type.getFirstChild());
            while (typechild != null) {
                if (typechild.getNodeName().equals("usersort")) {
                    String constituent = getAttribute(typechild, "declaration").getNodeValue();
                    pt.addType(colortypes.get(constituent));
                }
                typechild = skipWS(typechild.getNextSibling());
            }
            Require.that(colortypes.put(id, pt) == null, "the name " + id + ", was already used");
            network.add(pt);
        }
        productTypes.clear();

        // Variables can only be handled after all color types are parsed
        var variabledecl = declarations.getElementsByTagName("variabledecl");
        forEachElementInNodeList(variabledecl, element -> {
            String id = getAttribute(element, "id").getNodeValue();
            String name = nc.transform(getAttribute(element, "name").getNodeValue());
            ColorType ct = parseUserSort(element);
            Variable var = new Variable(name, id, ct);
            Require.that(variables.put(id, var) == null, "the id " + id + ", was already used");
            network.add(var);
        });

        Vector<String> variablesForRemoval = new Vector<>();
        HashMap<String, Variable> newVars = new HashMap<>();
        StringBuilder renameWarnings = new StringBuilder();
        // Convert product variables into variables for the constituent of the product
        for(String varName : variables.keySet()){
            Variable var = variables.get(varName);
            if(var.getColorType().isProductColorType()){
                int constituentCounter = 1;

                Vector<ColorExpression> constituentVarExpressions = new Vector<>();
                renameWarnings.append("The product variable ").append(var.getName()).append(", was unfolded to (");

                for(ColorType colorType : var.getColorType().getProductColorTypes()){
                    StringBuilder elementSubstring = new StringBuilder("_" + constituentCounter);
                    while (variables.containsKey(varName + elementSubstring) || newVars.containsKey(varName + elementSubstring)){
                        elementSubstring.append("_1");
                    }

                    renameWarnings.append(varName).append(elementSubstring).append(",");

                    String name = nc.transform(var.getName() + elementSubstring);
                    Variable newVar = new Variable(name, varName + elementSubstring, colorType);
                    Require.that(newVars.put(varName + elementSubstring, newVar) == null, "the id " + varName + elementSubstring + ", was already used");
                    network.add(newVar);
                    constituentVarExpressions.addElement(new VariableExpression(newVar));
                    constituentCounter++;
                }
                renameWarnings.deleteCharAt(renameWarnings.length()-1);
                renameWarnings.append(")\n");

                tupleVarExpressions.put(varName, new TupleExpression(constituentVarExpressions));
                network.remove(var);
                variablesForRemoval.addElement(varName);
            }
        }
        for(String varName : variablesForRemoval){
            variables.remove(varName);
        }
        for(String varName : newVars.keySet()){
            variables.put(varName, newVars.get(varName));
        }

        if(renameWarnings.length() > 0){
            messages.add(renameWarnings.toString());
            //JOptionPane.showConfirmDialog(CreateGui.getApp(), renameWarnings.toString(), "Product Variables unfolded", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
        }
    }
    private void parseNamedSort(Node node, TimedArcPetriNetNetwork network) throws FormatException {
        //We always use the dot colortype
        colortypes.put("dot", ColorType.COLORTYPE_DOT);
        network.add(ColorType.COLORTYPE_DOT);

        Node type = skipWS(node.getFirstChild());
        String typetag = type.getNodeName();
        String name = nc.transform((getAttribute(node, "name").getNodeValue()));
        String id = getAttribute(node, "id").getNodeValue();

        if (typetag.equals("productsort")) {
            // Parse prodcut types last as they can used color types not yet declared
            productTypes.add(node);
        } else {
            ColorType ct = new ColorType(name);
            if (typetag.equals("dot")) {
                return;
            } else if (typetag.equals("finiteintrange")){
                int start = Integer.parseInt(getAttribute(type, "start").getNodeValue());
                int end = Integer.parseInt(getAttribute(type, "end").getNodeValue());
                for(int i = start; i <= end; i++){
                    ct.addColor(String.valueOf(i));
                }
            } else {
                Node typechild = skipWS(type.getFirstChild());
                while (typechild != null) {
                    Node colorId = getAttribute(typechild, "id");
                    if (colorId != null) {
                        String colorName = nc.transform(colorId.getNodeValue());
                        ct.addColor(colorName);
                        typechild = skipWS(typechild.getNextSibling());
                    } else {
                        throw new FormatException(String.format("No id found on %s\n", typechild.getNodeName()));
                    }
                }
            }
            Require.that(colortypes.put(id, ct) == null, "the name " + id + ", was already used");
            network.add(ct);
        }
    }

    public ColorType parseUserSort(Node node) throws FormatException {
        if (node instanceof Element) {
            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                String name = child.getNodeName();
                if (name.equals("usersort")) {
                    Node decl = getAttribute(child, "declaration");
                    return colortypes.get(decl.getNodeValue());
                } else if (name.matches("structure|type|subterm")) {
                    return parseUserSort(child);
                }
                child = skipWS(child.getNextSibling());
            }
        }
        throw new FormatException(String.format("Could not parse %s as an usersort\n", node.getNodeName()));
    }

    public ArcExpression parseArcExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.equals("numberof")) {
            return parseNumberOfExpression(node);
        } else if (name.equals("add")) {
            Vector<ArcExpression> constituents = new Vector<>();
    
            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ArcExpression subterm = parseArcExpression(child);
                constituents.add(subterm);
                child = skipWS(child.getNextSibling());
            }

            AddExpression addExpr = new AddExpression(constituents);
            for (ArcExpression expr : constituents) {
                expr.setParent(addExpr);
            }

            return addExpr;
        } else if (name.equals("subtract")) {
            Node headchild = skipWS(node.getFirstChild());
            ArcExpression headexp = parseArcExpression(headchild);
    
            Node nextchild = skipWS(headchild.getNextSibling());
            while (nextchild != null) {
                ArcExpression nextexp = parseArcExpression(nextchild);
                SubtractExpression subExpr = new SubtractExpression(headexp, nextexp);
                headexp.setParent(subExpr);
                nextexp.setParent(subExpr);
                headexp = subExpr;
                nextchild = skipWS(nextchild.getNextSibling());
            }

            return headexp;
        } else if (name.equals("scalarproduct")) {
            Node scalar = skipWS(node.getFirstChild());
            Integer scalarval = parseNumberConstantExpression(scalar);
    
            Node child = skipWS(scalar.getNextSibling());
            ArcExpression childexp = parseArcExpression(child);
    
            ScalarProductExpression scalarExpr = new ScalarProductExpression(scalarval, childexp);
            childexp.setParent(scalarExpr);
            return scalarExpr;
        } else if (name.equals("all")){
            ColorType ct = parseUserSort(node);
            Vector<ColorExpression> ceVector = new Vector<>();
            ceVector.add(new AllExpression(ct));
            return new NumberOfExpression(1,ceVector);
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseArcExpression(child);
        } else if (name.matches("tuple")){
            Vector<ColorExpression> ceVector = new Vector<>();
            ceVector.add(parseColorExpression(node));
            return new NumberOfExpression(1, ceVector);
        } else {
            throw new FormatException(String.format("Could not parse %s as an arc expression\n", name));
        }
    }

    private NumberOfExpression parseNumberOfExpression(Node node) throws FormatException {
        Node number = skipWS(node.getFirstChild());
        //The number constant may be omitted.
        //In that case, this parsing returns null.
        Integer numberval = parseNumberConstantExpression(number);
        Node subnode;
        if (numberval != null) {
            //The subexpression comes after the number constant.
            subnode = skipWS(number.getNextSibling());
        } else {
            //The number we read was actually the subexpression.
            subnode = number;
            numberval = 1;
        }
        Vector<ColorExpression> colorexps = new Vector<>();
        while (subnode != null) {
            ColorExpression colorexp = parseColorExpression(subnode);
            colorexps.add(colorexp);
            subnode = skipWS(subnode.getNextSibling());
        }
        return new NumberOfExpression(numberval, colorexps);

    }

    private Integer parseNumberConstantExpression(Node node) {
        String name = node.getNodeName();
        if (name.equals("numberconstant")) {
            String value = getAttribute(node, "value").getNodeValue();
            return Integer.valueOf(value);
        } else if (name.equals("subterm")) {
            Node child = skipWS(node.getFirstChild());
            return parseNumberConstantExpression(child);
        } else {
            return null;
        }
    }

    private ColorExpression parseColorExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.equals("dotconstant")) {
            return new DotConstantExpression();
        } else if (name.equals("variable")) {
            String varname = getAttribute(node, "refvariable").getNodeValue();
            Variable var = variables.get(varname);
            if(var != null){
                return new VariableExpression(var);
            } else {
                return tupleVarExpressions.get(varname);
            }
        } else if (name.equals("useroperator")) {
            String colorname = nc.transform(getAttribute(node, "declaration").getNodeValue());
            Color color = getColor(colorname);
            return new UserOperatorExpression(color);
        } else if (name.equals("successor")) {
            Node child = skipWS(node.getFirstChild());
            ColorExpression childexp = parseColorExpression(child);
            return new SuccessorExpression(childexp);
        } else if (name.equals("predecessor")) {
            Node child = skipWS(node.getFirstChild());
            ColorExpression childexp = parseColorExpression(child);
            return new PredecessorExpression(childexp);
        } else if(name.equals("all")){
            ColorType ct = parseUserSort(node);
            return new AllExpression(ct);
        } else if (name.equals("tuple")) {
            Vector<ColorExpression> colorexps = new Vector<>();

            Node child = skipWS(node.getFirstChild());
            while (child != null) {
                ColorExpression colorexp = parseColorExpression(child);
                colorexps.add(colorexp);
                child = skipWS(child.getNextSibling());
            }
            //Sometimes PNML nets have tuples with only 1 color, we just remove the tuple
            if(colorexps.size() < 2){
                return colorexps.get(0);
            }
            return new TupleExpression(colorexps);
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseColorExpression(child);
        } else if (name.equals("finiteintrangeconstant")){
            String value = getAttribute(node, "value").getNodeValue();
            //we assume first child is finiteintrange
            Node intRangeElement = skipWS(node.getFirstChild());
            String start = getAttribute(intRangeElement, "start").getNodeValue();
            String end = getAttribute(intRangeElement, "end").getNodeValue();
            return new UserOperatorExpression(findColorForIntRange(value,start,end));

        } else {
            throw new FormatException(String.format("Could not parse %s as an color expression\n", name));
        }
    }

    /// This will select the wrong color if the is overlap in naming, eg for IntegerRangeExpr (1,2,3,4) & (1,2)
    private dk.aau.cs.model.CPN.Color getColor(String colorname) throws FormatException {
        for (ColorType ct : colortypes.values()) {
            for (dk.aau.cs.model.CPN.Color c : ct) {
                if (c.getName().equals(colorname)) {
                    return c;
                }
            }
        }
        throw new FormatException(String.format("The color \"%s\" was not declared\n", colorname));
    }

    public GuardExpression parseGuardExpression(Node node) throws FormatException {
        String name = node.getNodeName();
        if (name.matches("lt|lessthan")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new LessThanExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("gt|greaterthan")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new GreaterThanExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("leq|lessthanorequal")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new LessThanEqExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("geq|greaterthanorequal")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new GreaterThanEqExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("eq|equality")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new EqualityExpression(subexps.value1(), subexps.value2());
        } else if (name.matches("neq|inequality")) {
            Tuple<ColorExpression, ColorExpression> subexps = parseLRColorExpressions(node);
            return new InequalityExpression(subexps.value1(), subexps.value2());
        } else if (name.equals("not")) {
            Node child = skipWS(node.getFirstChild());
            GuardExpression childexp = parseGuardExpression(child);
            return new NotExpression(childexp);
        } else if (name.equals("and")) {
            Tuple<GuardExpression, GuardExpression> subexps = parseLRGuardExpressions(node);
            AndExpression andExpr = new AndExpression(subexps.value1(), subexps.value2());
            Node isSimpleNode = node.getAttributes().getNamedItem("isSimple");
            if (isSimpleNode != null) {
                andExpr.setSimpleProperty(Boolean.parseBoolean(isSimpleNode.getNodeValue()));
            }
            return andExpr;
        } else if (name.equals("or")) {
            Node left = skipWS(node.getFirstChild());
            Node right = skipWS(left.getNextSibling());
            if(right == null){
                return parseGuardExpression(left);
            }
            OrExpression orExpr = new OrExpression(parseGuardExpression(left), parseGuardExpression(right));
            Node isSimpleNode = node.getAttributes().getNamedItem("isSimple");
            if (isSimpleNode != null) {
                orExpr.setSimpleProperty(Boolean.parseBoolean(isSimpleNode.getNodeValue()));
            }
            for (var it = skipWS(right.getNextSibling()); it != null; it = skipWS(it.getNextSibling())){
                orExpr = new OrExpression(orExpr, parseGuardExpression(it));
            }
            return orExpr;
        } else if (name.matches("subterm|structure")) {
            Node child = skipWS(node.getFirstChild());
            return parseGuardExpression(child);
        } else {
            throw new FormatException(String.format("Could not parse %s as a guard expression\n", name));
        }
    }

    private Tuple<ColorExpression,ColorExpression> parseLRColorExpressions(Node node) throws FormatException {
        Node left = skipWS(node.getFirstChild());
        ColorExpression leftexp = parseColorExpression(left);
        Node right = skipWS(left.getNextSibling());
        ColorExpression rightexp = parseColorExpression(right);
        return new Tuple<>(leftexp, rightexp);
    }

    private Tuple<GuardExpression,GuardExpression> parseLRGuardExpressions(Node node) throws FormatException {
        Node left = skipWS(node.getFirstChild());
        GuardExpression leftexp = parseGuardExpression(left);
        Node right = skipWS(left.getNextSibling());
        GuardExpression rightexp = parseGuardExpression(right);
        return new Tuple<>(leftexp, rightexp);
    }

    Color findColorForIntRange(String value, String start, String end) throws FormatException {
        for(var ct : colortypes.values()){
            List<Color> colors = ct.getColorList();
            if (colors.size() == 0) continue;
            if(colors.get(0).getColorName().equals(start) && colors.get(colors.size()-1).getColorName().equals(end)){
                for (Color c : ct) {
                    if (c.getName().equals(value)) {
                        return c;
                    }
                }
            }
        }
        throw new FormatException(String.format("The color \"%s\" was not declared in an int range\n", value));
    }

    public AddExpression constructCleanAddExpression(ColorType ct, ColorMultiset multiset){
        Vector<ArcExpression> coloredTokenList = new Vector<>();

        for(Color c : ct.getColors()){
            int numberOf = multiset.get(c);
            if(numberOf < 1){
                continue;
            }
            Vector<ColorExpression> v = new Vector<>();
            if(ct.isProductColorType()){
                Vector<ColorExpression> tupleColors = new Vector<>();
                for(Color tupleCol : c.getTuple()){
                    UserOperatorExpression color = new UserOperatorExpression(tupleCol);
                    tupleColors.add(color);
                }
                TupleExpression tupleExpr = new TupleExpression(tupleColors);
                v.add(tupleExpr);
            } else {
                UserOperatorExpression color = new UserOperatorExpression(c);
                v.add(color);
            }
            NumberOfExpression numOf = new NumberOfExpression(numberOf,v);
            coloredTokenList.add(numOf);

        }
        if (coloredTokenList.isEmpty()) return null;
        return new AddExpression(coloredTokenList);
    }
}
