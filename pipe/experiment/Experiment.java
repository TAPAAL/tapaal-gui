/*
 * Experiment.java
 *
 * Created on 24 / juliol / 2007, 10:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

import java.io.File;
import java.io.IOException;
import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.OutputKeys;

import org.xml.sax.SAXParseException;


import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Calendar;
import java.text.DecimalFormat;

import expressions.ExpressionInterpreter;
import expressions.InvalidTypeException;
import expressions.SyntaxException;

import pipe.dataLayer.DataLayer;

import pipe.experiment.validation.ExperimentValidator;
import pipe.experiment.validation.NotMatchingException;

import pipe.dataLayer.TNTransformer;
import pipe.dataLayer.Transition;
import pipe.dataLayer.Place;

import java.io.FileWriter;
import java.io.BufferedWriter;
/**
 * This class handles the creation, load and execution of an Experiment. The experiment file
 * must meet the ExperimentSchema.
 * @author marc
 */
public class Experiment {
    
    private DataLayer sourceDataLayer;
    private String fileName;
    private Hashtable <String, Variable> variables; //where the variables will be stored
    private SolutionSpec solutionspecs[];
    private ExpressionInterpreter ei;
    private ResultsProvider rp;
    private DataLayer newDataLayer;
    private Element currentOutputSpec;
    private File outputFile; //De moment
    private FileWriter fstream; //De moment
    private BufferedWriter out; //De moment
    private Document expDOM;
    private String outputFileName="/home/marc/Desktop/ProvaOutput.xml";
    private String currentSolutionID="";
    private Element currentSolutionElement;
    
    
    /** Creates a new instance of Experiment
     *
     * @param fileName experiment xml file name
     * @param sourceDataLayer the DataLayer which represents the net linked to the experiment
     */
    public Experiment(String fileName, DataLayer sourceDataLayer) {
        this.sourceDataLayer = sourceDataLayer;
        this.fileName = fileName;
        System.out.println("Cream nou experiment: " + fileName);
        variables = new Hashtable<String,Variable>();
        newDataLayer = new DataLayer();
        ei = new ExpressionInterpreter();
        rp = new ResultsProvider(newDataLayer);
        
        outputFile = new File("output.exp");
        try{
            fstream = new FileWriter("output.exp");
        }catch (java.io.IOException e){
            System.out.println(e);
        }
        out = new BufferedWriter(fstream);
        expDOM = null;
        /*StreamSource xsltSource = null;
	Transformer transformer = null;*/
	try {
            // Build a Petri Net XML Document
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            expDOM = builder.newDocument();
            expDOM.appendChild(expDOM.createElement("Output"));
            /*Element TN = expDOM.createElement("net"); //TN root element
            expDOM.appendChild(TN);
            Attr ns = expDOM.createAttribute("xmlns"); // TN "xmlns" Attribute
            ns.setValue("http://pdv.cs.tu-berlin.de/TimeNET/schema/eDSPN");
            TN.setAttributeNode(ns);*/
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    /**
     * Loads the xml file passed in the constructor
     * @throws SAXParseException if the Experiment does not meet the schema.
     * @throws NotMatchingException if the Experiment does not fit the Net
     */
    public void Load() throws SAXParseException, NotMatchingException, InvalidExpressionException{
                Document document = null;
                ExperimentValidator ev = null;
		
		try{ //Try to load and validate the experiment file
                    DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    document = builder.parse(fileName);
                    ev = new ExperimentValidator(document, sourceDataLayer);
                    boolean valid = ev.validate();
                    
                }catch(ParserConfigurationException e){
                    System.out.println("ParserConfigurationException thrown in Load() : Experiment Class : experiment Package");
                    e.printStackTrace(System.err);
                    
                }catch(org.xml.sax.SAXException e){
                    if(e instanceof SAXParseException){
                        throw (SAXParseException) e;
                    }else{
                        System.out.println("SAXException thrown in Load() : Experiment Class : experiment Package");
                        e.printStackTrace(System.err);
                    }
                }catch(IOException e){
                        System.out.println("IOException thrown in Load() : Experiment Class : experiment Package");
                	e.printStackTrace(System.err);
                }
                //Start to run the experiment
                NodeList nl = document.getDocumentElement().getChildNodes();
                int solutions = document.getElementsByTagName("SolutionSpec").getLength();
                solutionspecs = new SolutionSpec[solutions];
                solutions=0;
                for(int i = 0 ; i < nl.getLength() ; i++) {
                        Node node = nl.item(i);
                        if(node instanceof Element) {
                                Element element = (Element)node;
                                if ("LocalVariable".equals(element.getNodeName())){
                                        newLocalVariable(element.getAttribute("Name"), Double.valueOf(element.getAttribute("InitialValue")));
                                }
                                if ("Variable".equals(element.getNodeName())){
                                        if(element.hasAttribute("PlaceName")){
                                            newVariable(element.getAttribute("Name"),
                                                    element.getAttribute("PlaceName"),
                                                    element.getAttribute("AttributeToChange"));
                                        }else{
                                            newVariable(element.getAttribute("Name"),
                                                    element.getAttribute("TransitionName"),
                                                    element.getAttribute("AttributeToChange"));
                                        }
                                }
                                if ("OutputVariable".equals(element.getNodeName())){
                                        if(element.hasAttribute("PlaceName")){
                                            newOutputVariable(element.getAttribute("Name"),
                                                    element.getAttribute("PlaceName"),
                                                    OutputVariable.PLACE,
                                                    element.getAttribute("ResultToUse"),
                                                    element.getAttribute("InitialValue"));
                                        }else{
                                            newOutputVariable(element.getAttribute("Name"),
                                                    element.getAttribute("TransitionName"),
                                                    OutputVariable.TRANSITION,
                                                    element.getAttribute("ResultToUse"),
                                                    element.getAttribute("InitialValue"));
                                        }
                                }
                                if ("SolutionSpec".equals(element.getNodeName())){
                                    createSolutionSpec(element);
                                }
                        }
                }
                File resultFile = new File(outputFileName);
                StreamResult result = new StreamResult(resultFile);
                try{
                    Transformer xformer = TransformerFactory.newInstance().newTransformer();

                    DOMSource source = new DOMSource(expDOM);
                        
                    xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    xformer.transform(source, result);
                }catch(TransformerConfigurationException e){
                    System.out.println(e);
                }catch(TransformerException e){
                    System.out.println(e);
                }
                try{
                    out.close();
                }catch(IOException e){}
    }
    
    
    /** It creates a new local variable.
     * @param name variable name
     * @param initialValue initial value of the variable
     */
    private void newLocalVariable(String name, Double initialValue){
        Variable v = new Variable(name);
        variables.put(v.getName(),v); //the variable is added to the hash table
        v.setValue(initialValue);
        
        ei.setValue(name, initialValue); //the variable is added to the expression interpreter
    }
    
    /** Creates a new variable.
     * @param name variable name
     * @param nodeName name of the node linked to the variable
     * @param attribute attribute to change
     */
    private void newVariable(String name, String nodeName, String attribute){
        Variable v = new GlobalVariable(name,nodeName,attribute);
        variables.put(v.getName(),v);
        
        ei.setValue(name,0.0);
    }
    
    /** It creates a new output variable.
     * @param name variable name
     * @param nodeName name of the node linked to the variable
     * @param resultToUse result to use
     * @param nodeType PLACE or TRANSITION
     * @param initialValue initial value for this variable
     */
    private void newOutputVariable(String name, String nodeName, int nodeType, String resultToUse, String initialValue){
        double iValue = Double.parseDouble(initialValue);
        Variable v = new OutputVariable(name, resultToUse, nodeName, nodeType, iValue);
        variables.put(v.getName(),v);
        
        ei.setValue(name,iValue);
    }
    
    private void executeBlock (Element block) throws InvalidExpressionException{
        if("Iteration".equals(block.getNodeName())){
            iterate(block);
        }
        else if ("Alt".equals(block.getNodeName())){
            alternate(block);
        }
        else if ("Assign".equals(block.getNodeName())){
            assign(block);
        }
        else if ("Solve".equals(block.getNodeName())){
            solve(block);
        }
    }
    
    private void iterate(Element iteration) throws InvalidExpressionException{
        NodeList childNodes = iteration.getChildNodes();
        Vector<Element> ranges = new Vector<Element>();
        boolean lastRange=false;
        for(int i=0; i<childNodes.getLength() && !lastRange; i++){
            if (childNodes.item(i) instanceof Element){
                Element range = (Element)childNodes.item(i);
                if(range.getNodeName().equals("Range")){
                    ranges.add(range);
                }else{
                    if (ranges.size()>0){
                        lastRange=true;
                    }
                }
            }
        }
        boolean hasStopWhen;
        boolean stop = false;
        boolean end = false;
        Vector<IterationVector> vectors=new Vector<IterationVector>();
        for(int i = 0; i < ranges.size(); i++){
            Element range = (Element) ranges.get(i);
            String vName = range.getAttribute("VariableName");
            try{
                if (range.hasAttribute("Step") || range.hasAttribute("StepPercent")){
                    double startValue = ei.solveMathExpression(range.getAttribute("Start"));
                    ei.setValue(vName,startValue);
                    variables.get(vName).setValue(startValue);
                    double endValue = Double.parseDouble(range.getAttribute("End"));
                    double step = 0.0;
                    if(range.hasAttribute("Step")){
                        step = Double.parseDouble(range.getAttribute("Step"));
                    }else if (range.hasAttribute("StepPercent")){
                        step = Double.parseDouble(range.getAttribute("StepPercent"));
                    }
                    if (step >= 0){
                        end = endValue < startValue;
                    }else{
                        end = endValue > startValue;
                    }
                }else{
                    String content=range.getAttribute("Vector");
                    IterationVector vector = new IterationVector(vName,content);
                    ei.setValue(vName,vector.get(vector.getIndex()));
                    vectors.add(vector);
                }
            }catch(InvalidTypeException e){
                //Expressi?? booleana
                throw new InvalidExpressionException("Math expression expected. Boolean found: "+range.getAttribute("Start"));
            }catch(SyntaxException e){
                //Error sintaxi
                throw new InvalidExpressionException("Expression syntax1 error: "+range.getAttribute("Start"));
            }   
                //ei.setValue
        }
        
        childNodes = iteration.getChildNodes();
        Vector<Element> stopWhens = new Vector<Element>();
        boolean lastStop=false;
        for(int i=0; i<childNodes.getLength() && !lastStop; i++){
            if (childNodes.item(i) instanceof Element){
                Element stopWhen = (Element)childNodes.item(i);
                if(stopWhen.getNodeName().equals("StopWhen")){
                    stopWhens.add(stopWhen);
                }else{
                    if (stopWhens.size()>0){
                        lastStop=true;
                    }
                }
            }
        }
        String condition[]= new String[stopWhens.size()];
        if (stopWhens.size() > 0){
            hasStopWhen = true;
            for (int k=0; k < stopWhens.size(); k++){
                Element stopWhen = (Element) stopWhens.get(k);
                condition[k] = stopWhen.getAttribute("Test");
            }
        }else{
            hasStopWhen=false;
            stop=false;
        }
        while (!stop && !end){
            System.out.println("Iteram");
            if (hasStopWhen){
                try{
                    for (int k=0; k<condition.length; k++){
                        stop = stop || ei.solveBooleanExpression(condition[k]);
                    }
                }catch(InvalidTypeException e){

                }catch(SyntaxException e){

                }
            }
            if(!stop){
                NodeList childBlocks = iteration.getChildNodes();
                int i=0;
                while (childBlocks.item(i).getNodeName().equals("Range")){
                    i++;
                }
                for(int j=i; j<childBlocks.getLength(); j++){
                    if(childBlocks.item(j) instanceof Element){
                        executeBlock((Element)childBlocks.item(j));
                    }
                }
                for(int j=0; j < ranges.size(); j++){
                    if (ranges.get(j) instanceof Element){
                        Element range = (Element) ranges.get(j);
                        String vName = range.getAttribute("VariableName");
                        if(range.hasAttribute("Step") || range.hasAttribute("StepPercent")){
                            double endValue = Double.parseDouble(range.getAttribute("End"));
                            if (range.hasAttribute("Step")){
                                double step = Double.parseDouble(range.getAttribute("Step"));
                                try{
                                    ei.setValue(vName, ei.getValue(vName)+step);
                                    variables.get(vName).setValue(variables.get(vName).getValue()+step);
                                    System.out.println("value="+variables.get(vName).getValue()+" end="+endValue);
                                    if (step >= 0){
                                        end = end || endValue < ei.getValue(vName);
                                    }else{
                                        end = end || endValue > ei.getValue(vName);
                                    }
                                }catch (expressions.VariableNotInitializedException e){
                                    System.out.println("Variable not initialized!!!");
                                }
                            }else if (range.hasAttribute("StepPercent")){
                                double stepPercent = Double.parseDouble(range.getAttribute("StepPercent"))/100.0;
                                try{

                                    ei.setValue(vName, ei.getValue(vName)*(1+stepPercent));
                                    variables.get(vName).setValue(variables.get(vName).getValue()*(1+stepPercent));
                                    System.out.println("value="+variables.get(vName).getValue()+" end="+endValue);
                                    if (stepPercent > 0){
                                        end = end || endValue < ei.getValue(vName);
                                    }else{
                                        end = end || endValue > ei.getValue(vName);
                                    }
                                }catch (expressions.VariableNotInitializedException e){
                                    System.out.println("Variable not initialized!!!");
                                }
                            }
                        }
                    }
                }
                for(int j=0; j < vectors.size(); j++){
                        IterationVector vector =vectors.get(j);
                        vector.setIndex(vector.getIndex()+1);
                        if(vector.getIndex()==vector.size()){
                            end = true;
                        }else{
                            ei.setValue(vector.getVariableName(),vector.get(vector.getIndex()));
                            variables.get(vector.getVariableName()).setValue(vector.get(vector.getIndex()));
                        }
                }
            }
        }
        System.out.println("End"+end);
    }
    
    private void alternate(Element alt) throws InvalidExpressionException{
        NodeList childNodes = alt.getChildNodes();
        Vector<Element> guards = new Vector<Element>();
        boolean lastGuard=false;
        for(int i=0; i<childNodes.getLength() && !lastGuard; i++){
            if (childNodes.item(i) instanceof Element){
                Element guard = (Element)childNodes.item(i);
                if(guard.getNodeName().equals("Guard")){
                    guards.add(guard);
                }else{
                    if (guards.size()>0){
                        lastGuard=true;
                    }
                }
            }
        }
        boolean condition=true;
        for (int k=0; k<guards.size() && condition; k++){
            String guard = guards.get(k).getTextContent();
            try{
            condition = condition && ei.solveBooleanExpression(guard);
            }catch(InvalidTypeException e){
                //Not boolean
                throw new InvalidExpressionException("Boolean expression expected. Math found: "+guard);
            }
            catch(SyntaxException e){
                //Syntax error
                throw new InvalidExpressionException("Expression syntax error: "+guard);
            }
        }
        if(condition){
                NodeList childBlocks = alt.getChildNodes();
                int i=0;
                while (childBlocks.item(i).getNodeName().equals("Guard")){
                    i++;
                }
                for(int j=i; j<childBlocks.getLength(); j++){
                    if (childBlocks.item(j) instanceof Element){
                        executeBlock((Element)childBlocks.item(j));
                    }
                }
            }
    }
    
    private void assign(Element assign) throws InvalidExpressionException{
        String vName =assign.getAttribute("VariableName");
        String expression = assign.getAttribute("Value");
        try{
            Double value = ei.solveMathExpression(expression);
            ei.setValue(vName,value);
            variables.get(vName).setValue(value);
            if (variables.get(vName) instanceof GlobalVariable){
                String node = ((GlobalVariable) variables.get(vName)).getNodeName();
                String attribute = ((GlobalVariable) variables.get(vName)).getAttributeName();
                if (attribute.equalsIgnoreCase("capacity")){
                    newDataLayer.getPlaceByName(node).setCapacity(new Double(value).intValue());
                }else if (attribute.equalsIgnoreCase("initialMarking")){
                    newDataLayer.getPlaceByName(node).setCurrentMarking(new Double(value).intValue());
                }else if (attribute.equalsIgnoreCase("priority")){
                    newDataLayer.getTransitionByName(node).setPriority(new Double(value).intValue());
                }else if (attribute.equalsIgnoreCase("rate")){
                    newDataLayer.getTransitionByName(node).setRate(value);
                    System.out.println("Rate = "+newDataLayer.getTransitionByName(node).getRate());
                }else if (attribute.equalsIgnoreCase("weight")){
                    newDataLayer.getTransitionByName(node).setRate(value);
                }
                rp.netChanged();
            }
        }catch(InvalidTypeException e){
            //Not a double
            throw new InvalidExpressionException("Math expression expected. Boolean found: "+expression);
        }catch(SyntaxException e){
            throw new InvalidExpressionException("Expression syntax error: "+expression);
        }
    }
    
    private void solve(Element solve){
        currentSolutionID=solve.getAttribute("SolutionID");
        NodeList nl = solve.getChildNodes();
        for(int i = 0 ; i < nl.getLength() ; i++) {
            Node node = nl.item(i);
            if(node instanceof Element) {
                if (((Element) node).getTagName().equalsIgnoreCase("SolutionAnalytic") ){
                    runAnalytic();
                    updateVariables();
                    writeOutput(currentOutputSpec);
                }else if (((Element) node).getTagName().equalsIgnoreCase("InvariantAnalysis")){
                    runInvariant();
                    writeInvariants();
                }else if (((Element) node).getTagName().equalsIgnoreCase("StructuralPropertiesCheck")){
                    runStructuralProperties();
                    writeStateSpace();
                }
            }
        }
    }
    
    private void updateVariables(){
        Enumeration vList=variables.elements();
        while (vList.hasMoreElements()){
            Variable v = (Variable)vList.nextElement();
            if(v instanceof OutputVariable){
                OutputVariable ov = (OutputVariable) v;
                if(ov.getResultToUse().equalsIgnoreCase("Throughput")){
                    ei.setValue(ov.getName(),rp.getThroughput(ov.getNodeName()));
                }
                if(ov.getResultToUse().equalsIgnoreCase("Utilization")){
                    //ei.setValue(ov.getName(),rp.get)
                    double [] tDist =rp.getTokenDist(ov.getNodeName());
                    if(tDist != null){
                        double u = 1.0 - tDist[0];
                        ei.setValue(ov.getName(),u);
                    }
                }
                if(ov.getResultToUse().equalsIgnoreCase("AverageTokens")){
                    ei.setValue(ov.getName(),rp.getAverageTokens(ov.getNodeName()));
                    
                }
            }
        }
    }
    
    private void writeOutput(Element outputSpec){
        Element root = ((Element) expDOM.getElementsByTagName("Output").item(0));
        currentSolutionElement = expDOM.createElement("Solution");
        currentSolutionElement.setAttribute("ID",currentSolutionID);
        root.appendChild(currentSolutionElement);
        if (outputSpec != null){
            NodeList nl = outputSpec.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++){
                if (nl.item(i) instanceof Element){
                    if (nl.item(i).getNodeName().equalsIgnoreCase("WriteVariable")){
                        writeVariable((Element) nl.item(i));
                    }
                    else if (nl.item(i).getNodeName().equalsIgnoreCase("WriteOutput")){
                        writeMetric((Element) nl.item(i));
                    }
                }
            }
            try{
                out.newLine();
            }catch(IOException e){}
        }
    }
    
    private void writeInvariants(){
        int [][] Pinvariants = rp.getPInvariants();
        int [][] Tinvariants = rp.getTInvariants();
        if(Pinvariants != null && Tinvariants != null){
            try{
                for(int i=0; i<Pinvariants.length; i++){
                    out.write("PInvariant"+String.valueOf(i)+"(");
                    for(int j=0; j < Pinvariants[0].length - 1; j++){
                        out.write(newDataLayer.getPlace(j).getName()+",");
                    }
                    out.write(newDataLayer.getPlace(Pinvariants[0].length - 1).getName()+") = ");
                    out.write("(");
                    for(int j=0; j < Pinvariants[0].length - 1; j++){
                        //out.write(newDataLayer.getPlace(j).getName()+":");
                        out.write(String.valueOf(Pinvariants[i][j]));
                        out.write(",");
                    }
                    out.write(String.valueOf(Pinvariants[i][Pinvariants[0].length - 1]));
                    out.write(")");
                    out.newLine();
                }
                
                //TInvariants
                
                for(int i=0; i<Tinvariants.length; i++){
                    out.write("TInvariant"+String.valueOf(i)+"(");
                    for(int j=0; j < Tinvariants[0].length - 1; j++){
                        out.write(newDataLayer.getTransition(j).getName()+",");
                    }
                    out.write(newDataLayer.getTransition(Tinvariants[0].length - 1).getName()+") = ");
                    out.write("(");
                    for(int j=0; j < Tinvariants[0].length - 1; j++){
                        out.write(String.valueOf(Tinvariants[i][j]));
                        out.write(",");
                    }
                    out.write(String.valueOf(Tinvariants[i][Tinvariants[0].length - 1]));
                    out.write(")");
                    out.newLine();
                    out.newLine();
                }
            }catch(IOException e){}
        }else{
            try{
                out.write("Error. Invariants could not be calculated. " +
                    "Make sure InvariantAnalysis is called in Solve part of this SolutionSpec.");
            }catch(IOException e){}
        }
    }
    
    private void writeStateSpace(){
        boolean [] result = rp.getStateSpace();
        try{
            if (result!=null){
                out.write("Bounded: "+result[0]);
                out.write(" Safe: "+result[1]);
                out.write(" Deadlock: "+result[2]);
                out.newLine();
            }
        }catch(IOException e){
            
        }
    }
    
    private void writeClassification(){
        
        rp.runClassification();//Aixo no toca anar aqui, pero fins que no entengui be ToolCommand...
        boolean [] result =rp.getClassification();
        try{
            out.write("State machine: "+result[0]);
            out.write(". Marked graph: "+result[1]);
            out.write(". Free choice net: "+result[2]);
            out.write(". Extended free choice net: "+result[3]);
            out.write(". Simple net: "+result[4]);
            out.write(". Extended simple net: "+result[5]);
            out.newLine();
        }catch(IOException e){}
    }
    
    private void writeVariable(Element variable){
        String name = variable.getAttribute("VariableName");
        if (variables.get(name) instanceof GlobalVariable){
            GlobalVariable v = (GlobalVariable) variables.get(name);
            String nodeName = v.getNodeName();
            String attribute = v.getAttributeName();
            try{
                out.write(name+":"+nodeName+":"+attribute+"="+ei.getValue(name));
                out.newLine();
            }catch(IOException e){
                System.out.println(e);
            }catch(expressions.VariableNotInitializedException e){
                System.out.println("Variable not initialized!");
            }
        }else if (variables.get(name) instanceof OutputVariable){
            OutputVariable v = (OutputVariable) variables.get(name);
            try{
                out.write(name+":"+v.getNodeName()+":"+v.getResultToUse()+"="+ei.getValue(name));
                out.newLine();
            }catch(IOException e){System.out.println(e);}
            catch(Exception e){}
        }else{
            Variable v = variables.get(name);
            try{
                out.write(name+"="+ei.getValue(name));
                out.newLine();
            }catch (Exception e){}
        }
        Element variableElement = expDOM.createElement("ValueUsed");
        variableElement.setAttribute("VariableName",name);
        try{
            variableElement.setAttribute("VariableValue",String.valueOf(ei.getValue(name)));
        }catch(Exception e){}
        currentSolutionElement.appendChild(variableElement);
    }
    
    private void writeMetric(Element writeOutput){
        String metric = writeOutput.getAttribute("Metric");
        if(metric.equalsIgnoreCase("throughput")){
            Transition [] transitions = newDataLayer.getTransitions();
            for (int i=0; i < transitions.length; i++){
                try{
                    Element throughputElement = expDOM.createElement("OutputTransition");
                    throughputElement.setAttribute("TransitionID",transitions[i].getName());
                    throughputElement.setAttribute("Throughput",String.valueOf(rp.getThroughput(transitions[i].getName())));
                    currentSolutionElement.appendChild(throughputElement);
                    out.write("X("+transitions[i].getName()+") = ");
                    out.write(String.valueOf(rp.getThroughput(transitions[i].getName())));
                    out.newLine();
                }catch(IOException e){}
            }
        }else if (metric.equalsIgnoreCase("MeanNumberTokens")){
            Place [] places = newDataLayer.getPlaces();
            for (int i=0; i < places.length; i++){
                try{
                    Element throughputElement = expDOM.createElement("OutputPlace");
                    throughputElement.setAttribute("PlaceID",places[i].getName());
                    throughputElement.setAttribute("MeanNumberTokens",String.valueOf(rp.getAverageTokens(places[i].getName())));
                    currentSolutionElement.appendChild(throughputElement);
                    out.write("N("+places[i].getName()+") = ");
                    out.write(String.valueOf(rp.getAverageTokens(places[i].getName())));
                    out.newLine();
                }catch(IOException e){}
            }
        }else if (metric.equalsIgnoreCase("NumberTokensDensity")){
            Place [] places = newDataLayer.getPlaces();
            boolean error = false;
            for (int i=0; i < places.length; i++){
                try{
                    double [] dist = rp.getTokenDist(places[i].getName());
                    if (dist != null){
                        for (int j = 0; j < dist.length; j++){
                            out.write("P(N("+places[i].getName()+")="+j+") = ");
                            out.write(String.valueOf(dist[j]));
                            if(j<dist.length-1){
                                out.write("  ");
                            }
                        }
                        out.newLine();
                    }else{
                        error = true;
                    }
                }catch(IOException e){}
            }
            if (error){
                try{
                    out.write("Error. Token distribution could not be calculated." +
                            "Make sure that SolutionAnalytic was called in Solve element of this SolutionSpec and that your net has at least 1 place and 1 token.");
                    out.newLine();
                }catch(IOException e){}
            }
        }else if (metric.equalsIgnoreCase("Utilization")){
            Place [] places = newDataLayer.getPlaces();
            boolean error = false;
            for (int i=0; i < places.length; i++){
                try{
                    double [] tDist =rp.getTokenDist(places[i].getName());
                    if(tDist != null){
                        double u = 1.0 - tDist[0];
                         Element throughputElement = expDOM.createElement("OutputPlace");
                        throughputElement.setAttribute("PlaceID",places[i].getName());
                        throughputElement.setAttribute("Utilization",String.valueOf(u));
                        currentSolutionElement.appendChild(throughputElement);
                        out.write("U("+places[i].getName()+") = ");
                        out.write(String.valueOf(u));
                        out.newLine();
                    }else{
                        error=true;
                    }
                }catch(IOException e){}
            }
            if(error){
                try{
                    out.write("Error. Utilization could not be calculated." +
                            "Make sure that SolutionAnalytic was called in Solve element of this SolutionSpec and that your net has at least 1 place and 1 token.");
                    out.newLine();
                }catch(IOException e){}
            }
        }
        
    }
    
    private void runAnalytic(){
        rp.runAverageTokens();
        rp.runThroughput();
        rp.runTokenDist();
        
    }
    
    private void runInvariant(){
        rp.runInvariant();
    }
    
    private void runStructuralProperties(){
        rp.runStateSpace();
    }
    
    private void createSolutionSpec(Element root) throws InvalidExpressionException{
        TNTransformer tnt = new TNTransformer();
        Document doc = null;
        try{
            Calendar now = Calendar.getInstance();
            File tmp = File.createTempFile("pipe.exp",String.valueOf(now.getTimeInMillis()));
            tnt.saveTN(tmp,sourceDataLayer);
            doc = tnt.transformTN(tmp.getAbsolutePath());
            tmp.delete();
            newDataLayer.createFromPNML(doc);
            rp.netChanged();
        }catch(IOException e){
            System.out.println(e);
        }catch(javax.xml.parsers.ParserConfigurationException e){
            System.out.println(e);
        }catch(javax.xml.transform.TransformerConfigurationException e){

        }catch(javax.xml.transform.TransformerException e){

        }
        
        NodeList childNodes = root.getChildNodes();
        Vector<Element> outputs = new Vector<Element>();
        boolean lastOutput=false;
        for(int i=0; i<childNodes.getLength() && !lastOutput; i++){
            if (childNodes.item(i) instanceof Element){
                Element output = (Element)childNodes.item(i);
                if(output.getNodeName().equals("OutputSpec")){
                    outputs.add(output);
                }else{
                    if (outputs.size()>0){
                        lastOutput=true;
                    }
                }
            }
        }
        
        currentOutputSpec = (Element) outputs.get(0);
        
        NodeList nl = root.getChildNodes();
        for(int i = 0 ; i < nl.getLength() ; i++) {
            Node node = nl.item(i);
            if(node instanceof Element) {
                if(! node.getNodeName().equals("OutputSpec")){
                    executeBlock((Element) node);
                }
            }
        }
    }
    
}
