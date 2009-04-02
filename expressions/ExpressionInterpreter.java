/*
 * ExpressionInterpreter.java
 *
 * Created on 1 / agost / 2007, 10:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package expressions;

import expressions.java_cup.runtime.ComplexSymbolFactory;
import expressions.java_cup.runtime.SymbolFactory;
import java.util.Hashtable;
import java.io.StringReader;

/**
 *
 * @author marc
 */
public class ExpressionInterpreter {

    private Hashtable<String, Double> variables;
    
    /**
     * Creates a new instance of ExpressionInterpreter
     */
    public ExpressionInterpreter() {
        variables = new Hashtable<String,Double>();
    }
    
    /**
     * Adds a new variable with null value to the ExpressionInterpreter
     * 
     * @param vName variable name
     */
    private void newVariable(String vName){
        variables.put(vName,null);
    }
    
    /** Assigns a value to a variable. If the variable doesn't exist, it is created.
     * @param vName variable name
     * @param value value to be set
     */
    
    public void setValue(String vName, Double value){
        variables.put(vName, value);
    }
    
    /** Returns the current value of the variable. If there is no such variable or its value
     * is null, it throws VariableNotInitializedException.
     * @param vName variable name
     * @return the current value of the variable
     * @throws VariableNotInitializedException
     */
    public Double getValue(String vName) throws VariableNotInitializedException{
        Double value = (Double) variables.get(vName);
        if (value == null) throw new VariableNotInitializedException(vName);
        return value;
    }
    
    /** Returns the Double value of the expression. If there is a syntax error,
     * a java.lang.Exception is thrown. If the expression value is not Double,
     * an InvalidTypeException is thrown.
     *
     * @param expression expression to be evaluated
     * @return expression result
     * @throws InvalidTypeException
     * @throws Exception
     */
    public Double solveMathExpression(String expression) throws InvalidTypeException, SyntaxException{
        SymbolFactory sf = new ComplexSymbolFactory();
        parser p= new parser(expression,sf);
        p.setExpressionInterpreter(this);
        try{
            Double res = p.DoubleResult();
            return res;
        }catch(InvalidTypeException e){
            System.out.println(e);
            throw e;
        }catch(Exception e){
            System.out.println(e);
            throw new SyntaxException();
        }
    }
    
    /** Returns the Boolean value of the expression. If there is a syntax error,
     * a java.lang.Exception is thrown. If the expression value is not Boolean,
     * an InvalidTypeException is thrown.
     *
     * @param expression expression to be evaluated
     * @return expression result
     * @throws InvalidTypeException
     * @throws Exception
     */
    public Boolean solveBooleanExpression(String expression)throws InvalidTypeException, SyntaxException{
        SymbolFactory sf = new ComplexSymbolFactory();
        parser p= new parser(expression,sf);
        p.setExpressionInterpreter(this);
        try{
            Boolean res = p.BooleanResult();
            return res;
        }catch(InvalidTypeException e){
            throw e;
        }
        catch(Exception e){
            throw new SyntaxException();
        }
        
    }
    
}
