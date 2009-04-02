/*
 * Converter.java
 *
 * Created on 20 / setembre / 2007, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package expressions;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
/**
 *
 * @author marc
 */
class Converter {
    
    protected static Double convert(double value){
        DecimalFormatSymbols dfs =new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.########");
        df.setDecimalFormatSymbols(dfs);
        try{
            return df.parse(df.format(value)).doubleValue();
        }catch(java.text.ParseException e){
            System.out.println(e);
            return null;
        }
    }
    
}
