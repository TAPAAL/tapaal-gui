package dk.aau.cs.io;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.*;


public class writeTACPN { // both export and save share some of the same syntax for CPN and TACPN. That shared code is presented here.
    private TimedArcPetriNetNetwork network;

    public writeTACPN(TimedArcPetriNetNetwork network) {
        this.network = network;
    }

    public void appendColoredArcsDependencies(Arc arc, DataLayer guiModel, Document document, Element arcElement) {
        ArcExpression arcExpr = null;
        if (arc instanceof TimedTransportArcComponent) {
            Transition trans =guiModel.getTransitionByName(arc.getSource().getName());
            if (trans != null) { // we check if the source is a valid transition, if it is we know this is an output transport arc and we have to access the second expression
                arcExpr = ((TimedTransportArcComponent) arc).underlyingTransportArc().getOutputExpression();
            } else {
                arcExpr = ((TimedTransportArcComponent) arc).underlyingTransportArc().getInputExpression();
            }
            if (arcExpr != null) {
                arcElement.appendChild(createArcExpressionElement(document, arcExpr));
            }
        } else {
            if(arc instanceof TimedInhibitorArcComponent){
                arcExpr = ((TimedInhibitorArcComponent) arc).underlyingTimedInhibitorArc().getArcExpression();
            } else if( arc instanceof TimedInputArcComponent) {
                arcExpr = ((TimedInputArcComponent) arc).underlyingTimedInputArc().getArcExpression();
            } else if (arc instanceof TimedOutputArcComponent) {
                arcExpr = ((TimedOutputArcComponent) arc).underlyingArc().getExpression();
            }
            if (arcExpr != null) {
                arcElement.appendChild(createArcExpressionElement(document, arcExpr));
            }
        }
    }

    public Element createArcExpressionElement(Document document, ArcExpression arcExpr) {

        Element hlinscriptionElement = document.createElement("hlinscription");
        Element textElement = document.createElement("text");
        textElement.setTextContent(arcExpr.toString());
        hlinscriptionElement.appendChild(textElement);
        Element structureElement = document.createElement("structure");
        hlinscriptionElement.appendChild(structureElement);
        hlinscriptionElement.appendChild(parseArcExpression(arcExpr, document, structureElement));
        /*
        if(arc instanceof ColoredInputArcComponent)  {
            ColoredInputArc inputArc = (ColoredInputArc)((ColoredInputArcComponent) arc).underlyingTimedInputArc();
            if(inputArc.getArcExpression() != null) {
                textElement.setTextContent(inputArc.getArcExpression().toString());
            }
        }
        else if(arc instanceof ColoredOutputArcComponent) {
            ColoredOutputArcComponent coloredArc = (ColoredOutputArcComponent) arc;
            if(coloredArc.getExpression() != null) {
                textElement.setTextContent(coloredArc.getExpression().toString());
            }
        }
        hlinscriptionElement.appendChild(textElement);
        Element structureElement = document.createElement("structure");
        hlinscriptionElement.appendChild(structureElement);


        if( arc instanceof ColoredInputArcComponent) {
            ColoredInputArc inputArc = (ColoredInputArc) ((ColoredInputArcComponent) arc).underlyingTimedInputArc();
            ArcExpression expr;

            if (inputArc.getArcExpression() != null) {
                expr = inputArc.getArcExpression();
                hlinscriptionElement.appendChild(parseArcExpression(expr, document, structureElement));
            }
        }
        else if (arc instanceof ColoredOutputArcComponent) {
            ColoredOutputArc outputArc = (ColoredOutputArc) ((ColoredOutputArcComponent) arc).underlyingArc();
            ArcExpression expr;
            if(outputArc.getExpression() != null){
                expr = outputArc.getExpression();
                hlinscriptionElement.appendChild(parseArcExpression(expr, document, structureElement));
            }
        }
        else if (arc instanceof ColoredTransportArcComponent) {
            ColoredTransportArc transportArc = (ColoredTransportArc) ((ColoredTransportArcComponent)arc).underlyingTransportArc();
            ArcExpression expr;
            if(transportArc.getInputExpression() != null) {
                expr = transportArc.getInputExpression();
                hlinscriptionElement.appendChild(parseArcExpression(expr, document, structureElement));
            }
        }
           */
        return hlinscriptionElement;
    }

    private Element parseArcExpression(Expression expression, Document document, Element structureElement) {
        Require.notNull(expression, "We cannot save a null expression for " + structureElement.getNodeName());
        if(expression instanceof DotConstantExpression) {
            Element dotConstantElement = document.createElement("dotconstant");
            structureElement.appendChild(dotConstantElement);
        }
        else if (expression instanceof AndExpression) {
            Element andElement = document.createElement("and");
            Element subtermElement = document.createElement("subterm");
            andElement.appendChild(subtermElement);
            AndExpression expr = (AndExpression) expression;
            andElement.appendChild(parseArcExpression(expr.getRightExpression(), document, subtermElement));
            andElement.appendChild(parseArcExpression(expr.getLeftExpression(), document, subtermElement));
            structureElement.appendChild(andElement);

        }
        else if(expression instanceof NumberOfExpression) {
            Element numberOfElement = document.createElement("numberof");
            Element subtermElement = document.createElement("subterm");
            numberOfElement.appendChild(subtermElement);
            Element numberConstantElement = document.createElement("numberconstant");
            numberConstantElement.setAttribute("value", ((NumberOfExpression)expression).getNumber().toString());
            subtermElement.appendChild(numberConstantElement);
            Element positiveElement = document.createElement("positive");
            numberConstantElement.appendChild(positiveElement);
            Element subtermElement2 = document.createElement("subterm");


            NumberOfExpression expr = (NumberOfExpression) expression;
            for (Expression colorExpression : expr.getNumberOfExpression()) {
                numberOfElement.appendChild(parseArcExpression(colorExpression, document, subtermElement2));
            }

            structureElement.appendChild(numberOfElement);
        }
        else if(expression instanceof AllExpression){
            Element allElement = document.createElement("all");
            structureElement.appendChild(allElement);
            Element usersortElement = document.createElement("usersort");
            allElement.appendChild(usersortElement);
            usersortElement.setAttribute("declaration", ((AllExpression) expression).getColorType().getName());
        }

        else if(expression instanceof UserOperatorExpression) {
            Element userOperationElement = document.createElement("useroperator");
            userOperationElement.setAttribute("declaration", ((UserOperatorExpression) expression).getUserOperator().getColorName());
            structureElement.appendChild(userOperationElement);
        }
        else if(expression instanceof VariableExpression) {
            Element variableElement = document.createElement("variable");
            variableElement.setAttribute("refvariable", ((VariableExpression) expression).getVariable().getId());
            structureElement.appendChild(variableElement);
        }
        else if (expression instanceof SuccessorExpression) {
            Element succElement = document.createElement("successor");
            Element subtermElement = document.createElement("subterm");
            succElement.appendChild(subtermElement);
            SuccessorExpression expr = (SuccessorExpression) expression;
            succElement.appendChild(parseArcExpression(expr.getSuccessorExpression(), document, subtermElement));
            structureElement.appendChild(succElement);
        }
        else if (expression instanceof PredecessorExpression) {
            Element predElement = document.createElement("predecessor");
            Element subtermElement = document.createElement("subterm");
            predElement.appendChild(subtermElement);
            PredecessorExpression expr = (PredecessorExpression) expression;
            predElement.appendChild(parseArcExpression(expr.getPredecessorExpression(), document, subtermElement));
            structureElement.appendChild(predElement);
        }
        else if (expression instanceof AddExpression) {
            Element addElement = document.createElement("add");
            AddExpression expr = (AddExpression) expression;
            for (ArcExpression arcExpression: expr.getAddExpression()) {
                Element subtermElement = document.createElement("subterm");
                addElement.appendChild(parseArcExpression(arcExpression,document,subtermElement));
            }
            structureElement.appendChild(addElement);
        }
        else if (expression instanceof SubtractExpression) {
            Element subtractElement = document.createElement("subtract");
            Element subtermLeftElement = document.createElement("subterm");
            Element subtermRightElement = document.createElement("subterm");
            subtractElement.appendChild(subtermLeftElement);
            subtractElement.appendChild(subtermRightElement);
            SubtractExpression expr = (SubtractExpression) expression;
            subtractElement.appendChild(parseArcExpression(expr.getLeftExpression(), document, subtermLeftElement));
            subtractElement.appendChild(parseArcExpression(expr.getRightExpression(), document, subtermRightElement));
            structureElement.appendChild(subtractElement);
        }
        else if (expression instanceof TupleExpression) {
            Element tupleElement = document.createElement("tuple");
            TupleExpression expr = (TupleExpression) expression;
            for (Expression colorExpressions : expr.getColors()) {
                Element subtermElement = document.createElement("subterm");
                tupleElement.appendChild(parseArcExpression(colorExpressions, document, subtermElement));
            }
            structureElement.appendChild(tupleElement);
        } else if (expression instanceof  ScalarProductExpression) {
            Element ScalarElement = document.createElement("scalarproduct");

        }
        return structureElement;
    }

    public void appendColoredTransitionDependencies(TimedTransition inputTransition, Document document, Element transitionElement) {
        Expression expr = inputTransition.getGuard();
        if(expr != null) {
            Element conditionElement = document.createElement("condition");
            transitionElement.appendChild(conditionElement);
            Element textElement = document.createElement("text");
            String expressionName = ReplaceExpressionWithNames(expr.toString());
            textElement.setTextContent(expressionName);
            conditionElement.appendChild(textElement);
            Element structureElement = document.createElement("structure");
            conditionElement.appendChild(parseGuardExpression(expr, document, structureElement));
        }
    }

    private String ReplaceExpressionWithNames (String expression) {
        expression = expression.replace(">=", "gte");
        expression = expression.replace("<=", "lte");
        expression = expression.replace("<", "lt");
        expression = expression.replace(">", "gt");
        expression = expression.replace("=", "eq");
        expression = expression.replace("!=", "neq");
        return expression;
    }

    private Element parseGuardExpression(Expression expression, Document document, Element structureElement) {
        if(structureElement != null) {
            if (expression instanceof AndExpression) {
                Element andElement = document.createElement("and");
                Element subtermElement = document.createElement("subterm");
                andElement.appendChild(subtermElement);
                AndExpression expr = (AndExpression) expression;
                andElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                andElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(andElement);
            } else if (expression instanceof OrExpression) {
                Element orElement = document.createElement("or");
                Element subtermElement = document.createElement("subterm");
                orElement.appendChild(subtermElement);
                OrExpression expr = (OrExpression) expression;
                orElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                orElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(orElement);
            } else if (expression instanceof EqualityExpression) {
                Element equalElement = document.createElement("equality");
                Element subtermElement = document.createElement("subterm");
                equalElement.appendChild(subtermElement);
                EqualityExpression expr = (EqualityExpression) expression;
                equalElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                equalElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(equalElement);
            } else if (expression instanceof GreaterThanEqExpression) {
                Element gteElement = document.createElement("greaterthanorequal");
                Element subtermElement = document.createElement("subterm");
                gteElement.appendChild(subtermElement);
                GreaterThanEqExpression expr = (GreaterThanEqExpression) expression;
                gteElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                gteElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(gteElement);
            } else if (expression instanceof GreaterThanExpression) {
                Element gtElement = document.createElement("greaterthan");
                Element subtermElement = document.createElement("subterm");
                gtElement.appendChild(subtermElement);
                GreaterThanExpression expr = (GreaterThanExpression) expression;
                gtElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                gtElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(gtElement);
            } else if (expression instanceof InequalityExpression) {
                Element ineuqlElement = document.createElement("inequality");
                Element subtermElement = document.createElement("subterm");
                ineuqlElement.appendChild(subtermElement);
                InequalityExpression expr = (InequalityExpression) expression;
                ineuqlElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                ineuqlElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(ineuqlElement);
            } else if (expression instanceof LessThanEqExpression) {
                Element lteElement = document.createElement("lessthanorequal");
                Element subtermElement = document.createElement("subterm");
                lteElement.appendChild(subtermElement);
                LessThanEqExpression expr = (LessThanEqExpression) expression;
                lteElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                lteElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(lteElement);
            } else if (expression instanceof LessThanExpression) {
                Element ltElement = document.createElement("lessthan");
                Element subtermElement = document.createElement("subterm");
                ltElement.appendChild(subtermElement);
                LessThanExpression expr = (LessThanExpression) expression;
                ltElement.appendChild(parseGuardExpression(expr.getLeftExpression(), document, subtermElement));
                Element subtermElement2 = document.createElement("subterm");
                ltElement.appendChild(parseGuardExpression(expr.getRightExpression(), document, subtermElement2));
                structureElement.appendChild(ltElement);
            } else if (expression instanceof NotExpression) {
                Element notElement = document.createElement("not");
                Element subtermElement = document.createElement("subterm");
                notElement.appendChild(subtermElement);
                NotExpression expr = (NotExpression) expression;
                notElement.appendChild(parseGuardExpression(expr.getExpression(), document, subtermElement));
                structureElement.appendChild(notElement);
            } else if (expression instanceof PredecessorExpression) {
                Element predElement = document.createElement("predecessor");
                Element subtermElement = document.createElement("subterm");
                predElement.appendChild(subtermElement);
                PredecessorExpression expr = (PredecessorExpression) expression;
                predElement.appendChild(parseGuardExpression(expr.getPredecessorExpression(), document, subtermElement));
                structureElement.appendChild(predElement);
            } else if (expression instanceof SuccessorExpression) {
                Element succElement = document.createElement("successor");
                Element subtermElement = document.createElement("subterm");
                succElement.appendChild(subtermElement);
                SuccessorExpression expr = (SuccessorExpression) expression;
                subtermElement.appendChild(parseGuardExpression(expr.getSuccessorExpression(), document, subtermElement));
                structureElement.appendChild(succElement);
            } else if (expression instanceof TupleExpression) {
                Element tupleElement = document.createElement("tuple");
                TupleExpression expr = (TupleExpression) expression;
                for (Expression colorExpressions : expr.getColors()) {
                    Element subtermElement = document.createElement("subterm");
                    tupleElement.appendChild(parseGuardExpression(colorExpressions, document, subtermElement));
                }
                structureElement.appendChild(tupleElement);
            } else if (expression instanceof UserOperatorExpression) {
                Element userOperationElement = document.createElement("useroperator");
                userOperationElement.setAttribute("declaration", ((UserOperatorExpression) expression).getUserOperator().getColorName());
                structureElement.appendChild(userOperationElement);
            } else if (expression instanceof VariableExpression) {
                Element variableElement = document.createElement("variable");
                variableElement.setAttribute("refvariable", ((VariableExpression) expression).getVariable().getId());
                structureElement.appendChild(variableElement);
            }
        }
        return structureElement;

    }

    public void appendColoredPlaceDependencies(TimedPlace inputPlace, Document document, Element placeElement) {
        ColorType colorType = inputPlace.getColorType();
        Element type = document.createElement("type");
        Element typeText = document.createElement("text");
        if (colorType != null) {
            typeText.setTextContent(colorType.getName());
        }
        type.appendChild(typeText);
        Element typeStructure = document.createElement("structure");
        type.appendChild(typeStructure);
        Element typeUsersort = document.createElement("usersort");
        typeUsersort.setAttribute("declaration", colorType.getName());

        typeStructure.appendChild(typeUsersort);
        placeElement.appendChild(type);

        if(inputPlace.getTokensAsExpression() != null) {
            Element hlInitialMarking = document.createElement("hlinitialMarking");
            Element hlInitialMarkingText = document.createElement("text");
            hlInitialMarking.appendChild(hlInitialMarkingText);
            String tokenNames = "";
            tokenNames = inputPlace.getTokensAsExpression().toString();
            hlInitialMarkingText.setTextContent(tokenNames);
            placeElement.appendChild(hlInitialMarking);
            Element hlStructure = document.createElement("structure");
            hlInitialMarking.appendChild(hlStructure);
            hlInitialMarking.appendChild(parseArcExpression(inputPlace.getTokensAsExpression(), document, hlStructure));
        }
    }

    public void appendDeclarations (Document document, Element NET) {
        Require.that(document != null, "Error: document was null");

        Element declarationElement = document.createElement("declaration");
        NET.appendChild(declarationElement);
        Element structureElement = document.createElement("structure");
        declarationElement.appendChild(structureElement);
        Element declarationsElement = document.createElement("declarations");
        structureElement.appendChild(parseSorts(document, declarationsElement));
    }

    private Element parseSorts(Document document, Element declarationsElement) {
        if(!network.colorTypes().isEmpty()) {
            for (ColorType colorType : network.colorTypes()) {
                if(colorType.getName().equals("dot")) {
                    Element namedsortElement = document.createElement("namedsort");
                    declarationsElement.appendChild(namedsortElement);
                    namedsortElement.setAttribute("id", colorType.getId());
                    namedsortElement.setAttribute("name", "dot");
                    Element dotElement = document.createElement("dot");
                    namedsortElement.appendChild(dotElement);

                }
                else {
                    Element namedsortElement = document.createElement("namedsort");
                    declarationsElement.appendChild(namedsortElement);
                    namedsortElement.setAttribute("id", colorType.getId());
                    namedsortElement.setAttribute("name", colorType.getName());
                    if (colorType instanceof ProductType) {
                        Element productSortElement = document.createElement("productsort");
                        namedsortElement.appendChild(productSortElement);
                        for (ColorType ct : ((ProductType) colorType).getColorTypes()) {
                            Element usersortElement = document.createElement("usersort");
                            usersortElement.setAttribute("declaration", ct.getName());
                            productSortElement.appendChild(usersortElement);
                        }
                    } else {
                        Element cyclicElement = document.createElement("cyclicenumeration");
                        namedsortElement.appendChild(cyclicElement);
                        for (Color color : colorType) {
                            Element feConstantElement = document.createElement("feconstant");
                            feConstantElement.setAttribute("id", color.getColorName());
                            feConstantElement.setAttribute("name", colorType.getName());
                            cyclicElement.appendChild(feConstantElement);
                        }
                    }
                }
            }
        }
        for(Variable variable : network.variables()) {
            Element variableDeclearationElement = document.createElement("variabledecl");
            declarationsElement.appendChild(variableDeclearationElement);
            variableDeclearationElement.setAttribute("id", variable.getId());
            variableDeclearationElement.setAttribute("name", variable.getName());
            if(variable.getColorType().getName().equals("Dot")) {
                Element namedsortElement = document.createElement("namedsort");
                variableDeclearationElement.appendChild(namedsortElement);
                namedsortElement.setAttribute("id", variable.getId());
                namedsortElement.setAttribute("name", variable.getName());
                Element dotElement = document.createElement("dot");
                namedsortElement.appendChild(dotElement);
            }
            Element usersortElement = document.createElement("usersort");
            variableDeclearationElement.appendChild(usersortElement);
            usersortElement.setAttribute("declaration", variable.getColorType().getName());

        }

        return declarationsElement;
    }
}
