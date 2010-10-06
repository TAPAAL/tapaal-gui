package dk.aau.cs.TCTL.Parsing;

import java.io.*;

import dk.aau.cs.Messenger;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TCTL.*;
import goldengine.java.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@4email.net) 
 */
 
public class TAPAALQueryParser implements GPMessageConstants
{	
	private interface SymbolConstants 
    {
		final int SYMBOL_EOF                   =  0;  // (EOF)
	       final int SYMBOL_ERROR                 =  1;  // (Error)
	       final int SYMBOL_WHITESPACE            =  2;  // (Whitespace)
	       final int SYMBOL_EXCLAM                =  3;  // '!'
	       final int SYMBOL_AMPAMP                =  4;  // '&&'
	       final int SYMBOL_LPARAN                =  5;  // '('
	       final int SYMBOL_RPARAN                =  6;  // ')'
	       final int SYMBOL_PIPEPIPE              =  7;  // '||'
	       final int SYMBOL_LT                    =  8;  // '<'
	       final int SYMBOL_LTEQ                  =  9;  // '<='
	       final int SYMBOL_EQ                    = 10;  // '='
	       final int SYMBOL_GT                    = 11;  // '>'
	       final int SYMBOL_GTEQ                  = 12;  // '>='
	       final int SYMBOL_ALBRACKETRBRACKET     = 13;  // 'A[]'
	       final int SYMBOL_ALTGT                 = 14;  // 'A<>'
	       final int SYMBOL_AF                    = 15;  // AF
	       final int SYMBOL_AG                    = 16;  // AG
	       final int SYMBOL_AND                   = 17;  // and
	       final int SYMBOL_ELBRACKETRBRACKET     = 18;  // 'E[]'
	       final int SYMBOL_ELTGT                 = 19;  // 'E<>'
	       final int SYMBOL_EF                    = 20;  // EF
	       final int SYMBOL_EG                    = 21;  // EG
	       final int SYMBOL_IDENTIFIER            = 22;  // Identifier
	       final int SYMBOL_NOT                   = 23;  // not
	       final int SYMBOL_NUM                   = 24;  // Num
	       final int SYMBOL_OR                    = 25;  // or
	       final int SYMBOL_ABSTRACTPATHPROPERTY  = 26;  // <AbstractPathProperty>
	       final int SYMBOL_ABSTRACTPROPERTY      = 27;  // <AbstractProperty>
	       final int SYMBOL_ABSTRACTSTATEPROPERTY = 28;  // <AbstractStateProperty>
	       final int SYMBOL_AF2                   = 29;  // <AF>
	       final int SYMBOL_AG2                   = 30;  // <AG>
	       final int SYMBOL_AND2                  = 31;  // <And>
	       final int SYMBOL_ATOMICPROPOSITION     = 32;  // <AtomicProposition>
	       final int SYMBOL_EF2                   = 33;  // <EF>
	       final int SYMBOL_EG2                   = 34;  // <EG>
	       final int SYMBOL_EXPR                  = 35;  // <Expr>
	       final int SYMBOL_FACTOR                = 36;  // <Factor>
	       final int SYMBOL_NOT2                  = 37;  // <Not>
	       final int SYMBOL_OR2                   = 38;  // <Or>
    };

    private interface RuleConstants
    {
    	final int RULE_ABSTRACTPROPERTY                      =  0;  // <AbstractProperty> ::= <AbstractPathProperty> <AbstractStateProperty>
        final int RULE_ABSTRACTPATHPROPERTY                  =  1;  // <AbstractPathProperty> ::= <EF>
        final int RULE_ABSTRACTPATHPROPERTY2                 =  2;  // <AbstractPathProperty> ::= <EG>
        final int RULE_ABSTRACTPATHPROPERTY3                 =  3;  // <AbstractPathProperty> ::= <AF>
        final int RULE_ABSTRACTPATHPROPERTY4                 =  4;  // <AbstractPathProperty> ::= <AG>
        final int RULE_EF_EF                                 =  5;  // <EF> ::= EF
        final int RULE_EF_ELTGT                              =  6;  // <EF> ::= 'E<>'
        final int RULE_EG_EG                                 =  7;  // <EG> ::= EG
        final int RULE_EG_ELBRACKETRBRACKET                  =  8;  // <EG> ::= 'E[]'
        final int RULE_AF_AF                                 =  9;  // <AF> ::= AF
        final int RULE_AF_ALTGT                              = 10;  // <AF> ::= 'A<>'
        final int RULE_AG_AG                                 = 11;  // <AG> ::= AG
        final int RULE_AG_ALBRACKETRBRACKET                  = 12;  // <AG> ::= 'A[]'
        final int RULE_ABSTRACTSTATEPROPERTY                 = 13;  // <AbstractStateProperty> ::= <Expr>
        final int RULE_EXPR                                  = 14;  // <Expr> ::= <Or>
        final int RULE_OR_OR                                 = 15;  // <Or> ::= <Or> or <And>
        final int RULE_OR_PIPEPIPE                           = 16;  // <Or> ::= <Or> '||' <And>
        final int RULE_OR                                    = 17;  // <Or> ::= <And>
        final int RULE_AND_AND                               = 18;  // <And> ::= <And> and <Not>
        final int RULE_AND_AMPAMP                            = 19;  // <And> ::= <And> '&&' <Not>
        final int RULE_AND                                   = 20;  // <And> ::= <Not>
        final int RULE_NOT_NOT_LPARAN_RPARAN                 = 21;  // <Not> ::= not '(' <Factor> ')'
        final int RULE_NOT_EXCLAM_LPARAN_RPARAN              = 22;  // <Not> ::= '!' '(' <Factor> ')'
        final int RULE_NOT                                   = 23;  // <Not> ::= <Factor>
        final int RULE_FACTOR                                = 24;  // <Factor> ::= <AtomicProposition>
        final int RULE_FACTOR_LPARAN_RPARAN                  = 25;  // <Factor> ::= '(' <Expr> ')'
        final int RULE_ATOMICPROPOSITION_IDENTIFIER_LT_NUM   = 26;  // <AtomicProposition> ::= Identifier '<' Num
        final int RULE_ATOMICPROPOSITION_IDENTIFIER_LTEQ_NUM = 27;  // <AtomicProposition> ::= Identifier '<=' Num
        final int RULE_ATOMICPROPOSITION_IDENTIFIER_EQ_NUM   = 28;  // <AtomicProposition> ::= Identifier '=' Num
        final int RULE_ATOMICPROPOSITION_IDENTIFIER_GTEQ_NUM = 29;  // <AtomicProposition> ::= Identifier '>=' Num
        final int RULE_ATOMICPROPOSITION_IDENTIFIER_GT_NUM   = 30;  // <AtomicProposition> ::= Identifier '>' Num
    };

    /***************************************************************
     * This class will run the engine, and needs a file called config.dat
     * in the current directory. This file should contain two lines,
     * The first should be the absolute path name to the .cgt file, the second
     * should be the source file you wish to parse.
     * @param text Array of arguments.
     * @return TODO
     * @throws IOException 
     ***************************************************************/
    public TCTLAbstractProperty parse(String query)
    {
    	
       String textToParse = query, compiledGrammar = "./dk/aau/cs/TCTL/Parsing/TAPAALQuery.cgt";

       GOLDParser parser = new GOLDParser();

       File temp;
       
       
       try
       {
    	  
          parser.loadCompiledGrammar(compiledGrammar);
          
          // put the text to parse in a temp file since parser requires it to be in a file
          temp = File.createTempFile("queryToParse", ".q");
          temp.deleteOnExit();
          PrintStream out = new PrintStream(temp); 
          out.append(textToParse);
          out.append("\n");
          out.close();

          // open temp file
          parser.openFile(temp.getPath());
//          parser.openFile("/home/lassejac/Desktop/test_queries/test");
       }
       catch(ParserException parse)
       {
          System.out.println("**PARSER ERROR**\n" + parse.toString());
          System.exit(1);
       } catch (IOException e) {
		
    	// TODO: handle the error
		e.printStackTrace();
	}

       boolean done = false;
       int response = -1;
       TCTLAbstractProperty root = null;
       

       while(!done)
       {
          try
            {
                  response = parser.parse();
            }
            catch(ParserException parse)
            {
                System.out.println("**PARSER ERROR**\n" + parse.toString());
                System.exit(1);
            }

            switch(response)
            {
               case gpMsgTokenRead:
                   /* A token was read by the parser. The Token Object can be accessed
                      through the CurrentToken() property:  Parser.CurrentToken */
                   break;

               case gpMsgReduction:
                   /* This message is returned when a rule was reduced by the parse engine.
                      The CurrentReduction property is assigned a Reduction object
                      containing the rule and its related tokens. You can reassign this
                      property to your own customized class. If this is not the case,
                      this message can be ignored and the Reduction object will be used
                      to store the parse tree.  */

                      switch(parser.currentReduction().getParentRule().getTableIndex())
                      {
                         case RuleConstants.RULE_ABSTRACTPROPERTY:
                            //<AbstractProperty> ::= <AbstractPathProperty> <AbstractStateProperty>
                        	 TCTLAbstractPathProperty quantifier = (TCTLAbstractPathProperty)createObject(parser.currentReduction().getToken(0));
                        	 root = quantifier.replace(new TCTLStatePlaceHolder(),(TCTLAbstractStateProperty)createObject(parser.currentReduction().getToken(1)));
                            break;
                         case RuleConstants.RULE_ABSTRACTPATHPROPERTY://<AbstractPathProperty> ::= <EF>
                         case RuleConstants.RULE_ABSTRACTPATHPROPERTY2: //<AbstractPathProperty> ::= <EG>
                         case RuleConstants.RULE_ABSTRACTPATHPROPERTY3: //<AbstractPathProperty> ::= <AF>
                         case RuleConstants.RULE_ABSTRACTPATHPROPERTY4: //<AbstractPathProperty> ::= <AG>
                         case RuleConstants.RULE_EF_EF: //<EF> ::= EF
                         case RuleConstants.RULE_EF_ELTGT: //<EF> ::= 'E<>'
                         case RuleConstants.RULE_EG_EG:  //<EG> ::= EG
                         case RuleConstants.RULE_EG_ELBRACKETRBRACKET:  //<EG> ::= 'E[]'
                         case RuleConstants.RULE_AF_AF: //<AF> ::= AF
                         case RuleConstants.RULE_AF_ALTGT:  //<AF> ::= 'A<>'
                         case RuleConstants.RULE_AG_AG: //<AG> ::= AG
                         case RuleConstants.RULE_AG_ALBRACKETRBRACKET: //<AG> ::= 'A[]'
                         case RuleConstants.RULE_ABSTRACTSTATEPROPERTY: //<AbstractStateProperty> ::= <Expr>
                         case RuleConstants.RULE_EXPR: //<Expr> ::= <Or>
                         case RuleConstants.RULE_OR_OR: //<Or> ::= <Or> or <And>
                         case RuleConstants.RULE_OR_PIPEPIPE: //<Or> ::= <Or> '||' <And>
                         case RuleConstants.RULE_OR: //<Or> ::= <And>
                         case RuleConstants.RULE_AND_AND: //<And> ::= <And> and <Not>
                         case RuleConstants.RULE_AND_AMPAMP: //<And> ::= <And> '&&' <Not>
                         case RuleConstants.RULE_AND: //<And> ::= <Not>
                         case RuleConstants.RULE_NOT_NOT_LPARAN_RPARAN:  //<Not> ::= not '(' <Factor> ')'
                         case RuleConstants.RULE_NOT_EXCLAM_LPARAN_RPARAN: //<Not> ::= '!' '(' <Factor> ')'
                         case RuleConstants.RULE_NOT: //<Not> ::= <Factor>
                         case RuleConstants.RULE_FACTOR:  //<Factor> ::= <AtomicProposition>
                         case RuleConstants.RULE_FACTOR_LPARAN_RPARAN: //<Factor> ::= '(' <Expr> ')'
                         case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_LT_NUM: // <AtomicProposition> ::= Identifier '<' Num
                         case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_LTEQ_NUM:  // <AtomicProposition> ::= Identifier '<=' Num
                         case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_EQ_NUM:  // <AtomicProposition> ::= Identifier '=' Num
                         case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_GTEQ_NUM:  // <AtomicProposition> ::= Identifier '>=' Num
                         case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_GT_NUM:  // <AtomicProposition> ::= Identifier '>' Num
                        	 break;
                      }

                          //Parser.Reduction = //Object you created to store the rule

                    // ************************************** log file
                    System.out.println("gpMsgReduction");
                    Reduction myRed = parser.currentReduction();
                    System.out.println(myRed.getParentRule().getText());
                    // ************************************** end log

                    break;

                case gpMsgAccept:
                    /* The program was accepted by the parsing engine */

                    // ************************************** log file
                    System.out.println("gpMsgAccept");
                    // ************************************** end log

                    done = true;

                    break;

                case gpMsgLexicalError:
                    /* Place code here to handle a illegal or unrecognized token
                           To recover, pop the token from the stack: Parser.PopInputToken */

                    // ************************************** log file
                    System.out.println("gpMsgLexicalError");
                    // ************************************** end log

                    parser.popInputToken();

                    break;

                case gpMsgNotLoadedError:
                    /* Load the Compiled Grammar Table file first. */

                    // ************************************** log file
                    System.out.println("gpMsgNotLoadedError");
                    // ************************************** end log

                    done = true;

                    break;

                case gpMsgSyntaxError:
                    /* This is a syntax error: the source has produced a token that was
                           not expected by the LALR State Machine. The expected tokens are stored
                           into the Tokens() list. To recover, push one of the
                              expected tokens onto the parser's input queue (the first in this case):
                           You should limit the number of times this type of recovery can take
                           place. */

                    done = true;
                    Token theTok = parser.currentToken();
                    System.out.println("Token not expected: " + (String)theTok.getData());

                    // ************************************** log file
                    System.out.println("gpMsgSyntaxError");
                    // ************************************** end log

                    break;

                case gpMsgCommentError:
                    /* The end of the input was reached while reading a comment.
                             This is caused by a comment that was not terminated */

                    // ************************************** log file
                    System.out.println("gpMsgCommentError");
                    // ************************************** end log

                    done = true;

                              break;

                case gpMsgInternalError:
                    /* Something horrid happened inside the parser. You cannot recover */

                    // ************************************** log file
                    System.out.println("gpMsgInternalError");
                    // ************************************** end log

                    done = true;

                    break;
            }
        }
        try
        {
              parser.closeFile();
        }
        catch(ParserException parse)
        {
            System.out.println("**PARSER ERROR**\n" + parse.toString());
            System.exit(1);
        }
        
        return root;
    }
    
    private Object createObject(Token token){
    	
    	TCTLAbstractProperty retObj = null;
    
    	
    	
    	switch(((Reduction)token.getData()).getParentRule().getTableIndex())
        {
           case RuleConstants.RULE_ABSTRACTPROPERTY: //<AbstractProperty> ::= <AbstractPathProperty> <AbstractStateProperty>
              retObj = null;
              break;
           case RuleConstants.RULE_ABSTRACTPATHPROPERTY:   //<AbstractPathProperty> ::= <EF>
           case RuleConstants.RULE_ABSTRACTPATHPROPERTY2: //<AbstractPathProperty> ::= <EG>
           case RuleConstants.RULE_ABSTRACTPATHPROPERTY3: //<AbstractPathProperty> ::= <AF>
           case RuleConstants.RULE_ABSTRACTPATHPROPERTY4:  //<AbstractPathProperty> ::= <AG>
        	   retObj = (TCTLAbstractPathProperty)createObject(((Reduction)token.getData()).getToken(0));
              break;
           case RuleConstants.RULE_EF_EF:  //<EF> ::= EF
           case RuleConstants.RULE_EF_ELTGT:  //<EF> ::= 'E<>'
        	   retObj = new TCTLEFNode();
              break;
           case RuleConstants.RULE_EG_EG:  //<EG> ::= EG
           case RuleConstants.RULE_EG_ELBRACKETRBRACKET: //<EG> ::= 'E[]'
        	  retObj = new TCTLEGNode();
              break;
           case RuleConstants.RULE_AF_AF: //<AF> ::= AF
           case RuleConstants.RULE_AF_ALTGT: //<AF> ::= 'A<>'
        	   retObj = new TCTLAFNode();
              break;
           case RuleConstants.RULE_AG_AG: //<AG> ::= AG
           case RuleConstants.RULE_AG_ALBRACKETRBRACKET:  //<AG> ::= 'A[]'
        	   retObj = new TCTLAGNode();
              break;
           case RuleConstants.RULE_OR_OR: //<Or> ::= <Or> or <And>
           case RuleConstants.RULE_OR_PIPEPIPE: //<Or> ::= <Or> '||' <And>
              TCTLOrNode orNode = new TCTLOrNode();
              orNode.setProperty1((TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(0)));
              orNode.setProperty2((TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(2)));
              retObj = orNode;
              break;
           case RuleConstants.RULE_OR: //<Or> ::= <And>
        	   retObj = (TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(0));
               break;
           case RuleConstants.RULE_AND_AND: //<And> ::= <And> and <Not>
           case RuleConstants.RULE_AND_AMPAMP: //<And> ::= <And> '&&' <Not>
              TCTLAndNode andNode = new TCTLAndNode();
              andNode.setProperty1((TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(0)));
              andNode.setProperty2((TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(2)));
        	  retObj = andNode;
              break;
           case RuleConstants.RULE_AND: //<And> ::= <Not>
        	   retObj = (TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(0));
               break;
           case RuleConstants.RULE_NOT_NOT_LPARAN_RPARAN:  //<Not> ::= not '(' <Factor> ')'
           case RuleConstants.RULE_NOT_EXCLAM_LPARAN_RPARAN:  //<Not> ::= '!' '(' <Factor> ')'
        	  // TODO Implement support for negation
        	   retObj = (TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(2));
              break;
              
           case RuleConstants.RULE_ABSTRACTSTATEPROPERTY: //<AbstractStateProperty> ::= <Expr>
           case RuleConstants.RULE_EXPR: //<Expr> ::= <Or>
           case RuleConstants.RULE_NOT:     //<Not> ::= <Factor>
           case RuleConstants.RULE_FACTOR: //<Factor> ::= <AtomicProposition>
        	   retObj = (TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(0));
               break;
           case RuleConstants.RULE_FACTOR_LPARAN_RPARAN:  //<Factor> ::= '(' <Expr> ')'
        	   retObj = (TCTLAbstractStateProperty)createObject(((Reduction)token.getData()).getToken(1));
               break;
           case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_LT_NUM: // <AtomicProposition> ::= Identifier '<' Num
           case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_LTEQ_NUM:  // <AtomicProposition> ::= Identifier '<=' Num
           case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_EQ_NUM:  // <AtomicProposition> ::= Identifier '=' Num
           case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_GTEQ_NUM:  // <AtomicProposition> ::= Identifier '>=' Num
           case RuleConstants.RULE_ATOMICPROPOSITION_IDENTIFIER_GT_NUM:  // <AtomicProposition> ::= Identifier '>' Num
        	   String place = (String)createObjectFromTerminal(((Reduction)token.getData()).getToken(0));
        	   String op = (String)createObjectFromTerminal(((Reduction)token.getData()).getToken(1));
        	   Integer n = (Integer)createObjectFromTerminal(((Reduction)token.getData()).getToken(2));
        	   retObj = new TCTLAtomicPropositionNode(place, op, n);
        	   break;
        }
    	
    	return retObj;
    }

	private Object createObjectFromTerminal(Token token) {
		Object retObj = null;
		
		switch(token.getTableIndex())
		{
		 
		case SymbolConstants.SYMBOL_LT:  // '<'
		case SymbolConstants.SYMBOL_LTEQ: // '<='
		case SymbolConstants.SYMBOL_EQ: // '='
		case SymbolConstants.SYMBOL_GT: // '>'
		case SymbolConstants.SYMBOL_GTEQ: // '>='
	       String op = (String)token.getData();
	       retObj = op;
	       break;
		case SymbolConstants.SYMBOL_IDENTIFIER: // Identifier
			String id = (String)token.getData();
			retObj = id;
			break;
		case SymbolConstants.SYMBOL_NUM: // Num
			Integer n = Integer.parseInt((String)token.getData());
			retObj = n;
	       default:
	    	   break; // should not happen
		}
		
		return retObj;
	}
}
