package dk.aau.cs.TCTL.XMLParsing;

import dk.aau.cs.TCTL.TCTLAbstractProperty;

public class QueryWrapper{

    private String name = null;
    private XMLQueryParseException exception = null;
    private TCTLAbstractProperty property;

    public QueryWrapper(){}

    public String getName(){
        return this.name;
    }

    public void setName(String s){
        this.name = s;
    }

    public void setException(XMLQueryParseException e){
        this.exception = e;
    }

    public void setProp(TCTLAbstractProperty property) {
        this.property = property;
    }
    
    public TCTLAbstractProperty getProp() {
        return this.property;
    }

    public XMLQueryParseException getException(){
        return this.exception;
    }

    public boolean hasException(){
        if(this.exception == null){
            return false;
        } else{
            return true;
        }
    }

    public String getNameAndException(){

        String result = this.name + System.lineSeparator();

        if(this.exception != null){
           result += "  Reason: " + this.exception.getMessage();
        }

        return result;
    }

    public void negateQuery(){
        this.name = "not(" + this.name + ")";
    }
}
