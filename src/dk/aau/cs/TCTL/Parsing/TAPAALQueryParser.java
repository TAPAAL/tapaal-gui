package dk.aau.cs.TCTL.Parsing;

import goldengine.java.GOLDParser;
import goldengine.java.GPMessageConstants;
import goldengine.java.ParserException;
import goldengine.java.Token;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.debug.Logger;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@4email.net) 
 */

public class TAPAALQueryParser implements GPMessageConstants {

	private static final String ERROR_PARSING_QUERY_MESSAGE = "TAPAAL countered an error trying to parse the query";

	public interface SymbolConstants {
		final int SYMBOL_EOF                =  0;  // (EOF)
	       final int SYMBOL_ERROR              =  1;  // (Error)
	       final int SYMBOL_WHITESPACE         =  2;  // (Whitespace)
	       final int SYMBOL_EXCLAM             =  3;  // '!'
	       final int SYMBOL_AMPAMP             =  4;  // '&&'
	       final int SYMBOL_LPARAN             =  5;  // '('
	       final int SYMBOL_RPARAN             =  6;  // ')'
	       final int SYMBOL_DOT                =  7;  // '.'
	       final int SYMBOL_PIPEPIPE           =  8;  // '||'
	       final int SYMBOL_LT                 =  9;  // '<'
	       final int SYMBOL_LTEQ               = 10;  // '<='
	       final int SYMBOL_EQ                 = 11;  // '='
	       final int SYMBOL_EQEQ               = 12;  // '=='
	       final int SYMBOL_GT                 = 13;  // '>'
	       final int SYMBOL_GTEQ               = 14;  // '>='
	       final int SYMBOL_ALBRACKETRBRACKET  = 15;  // 'A[]'
	       final int SYMBOL_ALTGT              = 16;  // 'A<>'
	       final int SYMBOL_AF                 = 17;  // AF
	       final int SYMBOL_AG                 = 18;  // AG
	       final int SYMBOL_AND                = 19;  // and
	       final int SYMBOL_ELBRACKETRBRACKET  = 20;  // 'E[]'
	       final int SYMBOL_ELTGT              = 21;  // 'E<>'
	       final int SYMBOL_EF                 = 22;  // EF
	       final int SYMBOL_EG                 = 23;  // EG
	       final int SYMBOL_FALSE              = 24;  // False
	       final int SYMBOL_IDENTIFIER         = 25;  // Identifier
	       final int SYMBOL_NOT                = 26;  // not
	       final int SYMBOL_NUM                = 27;  // Num
	       final int SYMBOL_OR                 = 28;  // or
	       final int SYMBOL_TRUE               = 29;  // True
	       final int SYMBOL_ABSTRACTPROPERTY   = 30;  // <AbstractProperty>
	       final int SYMBOL_AND2               = 31;  // <And>
	       final int SYMBOL_ATOMICPROPOSITION1 = 32;  // <AtomicProposition1>
	       final int SYMBOL_ATOMICPROPOSITION2 = 33;  // <AtomicProposition2>
	       final int SYMBOL_EXPR               = 34;  // <Expr>
	       final int SYMBOL_FACTOR             = 35;  // <Factor>
	       final int SYMBOL_NOT2               = 36;  // <Not>
	       final int SYMBOL_OR2                = 37;  // <Or>
	};

	public interface RuleConstants {
		final int RULE_ABSTRACTPROPERTY_EF                    =  0;  // <AbstractProperty> ::= EF <Expr>
	       final int RULE_ABSTRACTPROPERTY_ELTGT                 =  1;  // <AbstractProperty> ::= 'E<>' <Expr>
	       final int RULE_ABSTRACTPROPERTY_EG                    =  2;  // <AbstractProperty> ::= EG <Expr>
	       final int RULE_ABSTRACTPROPERTY_ELBRACKETRBRACKET     =  3;  // <AbstractProperty> ::= 'E[]' <Expr>
	       final int RULE_ABSTRACTPROPERTY_AF                    =  4;  // <AbstractProperty> ::= AF <Expr>
	       final int RULE_ABSTRACTPROPERTY_ALTGT                 =  5;  // <AbstractProperty> ::= 'A<>' <Expr>
	       final int RULE_ABSTRACTPROPERTY_AG                    =  6;  // <AbstractProperty> ::= AG <Expr>
	       final int RULE_ABSTRACTPROPERTY_ALBRACKETRBRACKET     =  7;  // <AbstractProperty> ::= 'A[]' <Expr>
	       final int RULE_EXPR                                   =  8;  // <Expr> ::= <Or>
	       final int RULE_OR_OR                                  =  9;  // <Or> ::= <Or> or <And>
	       final int RULE_OR_PIPEPIPE                            = 10;  // <Or> ::= <Or> '||' <And>
	       final int RULE_OR                                     = 11;  // <Or> ::= <And>
	       final int RULE_AND_AND                                = 12;  // <And> ::= <And> and <Not>
	       final int RULE_AND_AMPAMP                             = 13;  // <And> ::= <And> '&&' <Not>
	       final int RULE_AND                                    = 14;  // <And> ::= <Not>
	       final int RULE_NOT_NOT_LPARAN_RPARAN                  = 15;  // <Not> ::= not '(' <Expr> ')'
	       final int RULE_NOT_EXCLAM_LPARAN_RPARAN               = 16;  // <Not> ::= '!' '(' <Expr> ')'
	       final int RULE_NOT                                    = 17;  // <Not> ::= <Factor>
	       final int RULE_FACTOR_TRUE                            = 18;  // <Factor> ::= True
	       final int RULE_FACTOR_FALSE                           = 19;  // <Factor> ::= False
	       final int RULE_FACTOR                                 = 20;  // <Factor> ::= <AtomicProposition1>
	       final int RULE_FACTOR2                                = 21;  // <Factor> ::= <AtomicProposition2>
	       final int RULE_FACTOR_LPARAN_RPARAN                   = 22;  // <Factor> ::= '(' <Expr> ')'
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_LT_NUM   = 23;  // <AtomicProposition1> ::= Identifier '<' Num
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_LTEQ_NUM = 24;  // <AtomicProposition1> ::= Identifier '<=' Num
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_EQ_NUM   = 25;  // <AtomicProposition1> ::= Identifier '=' Num
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_EQEQ_NUM = 26;  // <AtomicProposition1> ::= Identifier '==' Num
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_GTEQ_NUM = 27;  // <AtomicProposition1> ::= Identifier '>=' Num
	       final int RULE_ATOMICPROPOSITION1_IDENTIFIER_GT_NUM   = 28;  // <AtomicProposition1> ::= Identifier '>' Num
	       final int RULE_ATOMICPROPOSITION2_IDENTIFIER_DOT      = 29;  // <AtomicProposition2> ::= Identifier '.' <AtomicProposition1>
	};

	private Stack<TCTLAbstractStateProperty> parseStack;

	/***************************************************************
	 * This class will run the engine, and needs a file called config.dat in the
	 * current directory. This file should contain two lines, The first should
	 * be the absolute path name to the .cgt file, the second should be the
	 * source file you wish to parse.
	 * 
	 * @param text
	 *            Array of arguments.
	 * @return TODO
	 * @throws IOException
	 ***************************************************************/
	public TCTLAbstractProperty parse(String query) throws ParseException {

		//Line below is to convert to windows format parths
		File compiledGrammarFile = new File(Thread.currentThread().getContextClassLoader().getResource("resources/TCTLParser/TAPAALQuery.cgt").getFile());
		String textToParse = query, compiledGrammar = compiledGrammarFile.getAbsolutePath();
		
		try {
			compiledGrammar = URLDecoder.decode(compiledGrammar, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			Logger.log("Error converting the URL format");
		}
		System.out.println(compiledGrammar);

		GOLDParser parser = new GOLDParser();

		parseStack = new Stack<TCTLAbstractStateProperty>();

		File temp;

		try {

			parser.loadCompiledGrammar(compiledGrammar);

			// put the text to parse in a temp file since parser requires it to
			// be in a file
			temp = File.createTempFile("queryToParse", ".q");
			temp.deleteOnExit();
			PrintStream out = new PrintStream(temp);
			out.append(textToParse);
			out.append("\n");
			out.close();

			// open temp file
			parser.openFile(temp.getPath());
			// parser.openFile("/home/lassejac/Desktop/test_queries/test");
		} catch (ParserException parse) {
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		} catch (IOException e) {

			// TODO: handle the error
			e.printStackTrace();
		}

		boolean done = false;
		int response = -1;
		TCTLAbstractProperty root = null;

		while (!done) {
			try {
				response = parser.parse();
			} catch (ParserException parse) {
				System.out.println("**PARSER ERROR**\n" + parse.toString());
				System.exit(1);
			}

			switch (response) {
			case gpMsgTokenRead:
				/*
				 * A token was read by the parser. The Token Object can be
				 * accessed through the CurrentToken() property:
				 * Parser.CurrentToken
				 */
				break;

			case gpMsgReduction:
				/*
				 * This message is returned when a rule was reduced by the parse
				 * engine. The CurrentReduction property is assigned a Reduction
				 * object containing the rule and its related tokens. You can
				 * reassign this property to your own customized class. If this
				 * is not the case, this message can be ignored and the
				 * Reduction object will be used to store the parse tree.
				 */

				switch (parser.currentReduction().getParentRule()
						.getTableIndex()) {

				case RuleConstants.RULE_ABSTRACTPROPERTY_EF: // <AbstractProperty> ::= EF <Expr>
				case RuleConstants.RULE_ABSTRACTPROPERTY_ELTGT: // <AbstractProperty> ::= 'E<>' <Expr>
					root = new TCTLEFNode(parseStack.pop());
					break;
				case RuleConstants.RULE_ABSTRACTPROPERTY_EG: // <AbstractProperty> ::= EG <Expr>
				case RuleConstants.RULE_ABSTRACTPROPERTY_ELBRACKETRBRACKET: // <AbstractProperty> ::= 'E[]' <Expr>
					root = new TCTLEGNode(parseStack.pop());
					break;
				case RuleConstants.RULE_ABSTRACTPROPERTY_AF: // <AbstractProperty> ::= AF <Expr>
				case RuleConstants.RULE_ABSTRACTPROPERTY_ALTGT: // <AbstractProperty> ::= 'A<>' <Expr>
					root = new TCTLAFNode(parseStack.pop());
					break;
				case RuleConstants.RULE_ABSTRACTPROPERTY_AG: // <AbstractProperty>::= AG <Expr>
				case RuleConstants.RULE_ABSTRACTPROPERTY_ALBRACKETRBRACKET: // <AbstractProperty> ::= 'A[]' <Expr>
					root = new TCTLAGNode(parseStack.pop());
					break;

				case RuleConstants.RULE_OR_OR: // <Or> ::= <Or> or <And>
				case RuleConstants.RULE_OR_PIPEPIPE: // <Or> ::= <Or> '||' <And>
					TCTLAbstractStateProperty orProp2 = parseStack.pop();
					TCTLAbstractStateProperty orProp1 = parseStack.pop();
					ArrayList<TCTLAbstractStateProperty> disjunctions = new ArrayList<TCTLAbstractStateProperty>();
					if (orProp1 instanceof TCTLOrListNode) {
						TCTLOrListNode node1 = (TCTLOrListNode) orProp1;

						for (TCTLAbstractStateProperty p : node1.getProperties()) {
							disjunctions.add(p);
						}
					} else {
						disjunctions.add(orProp1);
					}

					if (orProp2 instanceof TCTLOrListNode) {
						TCTLOrListNode node2 = (TCTLOrListNode) orProp2;

						for (TCTLAbstractStateProperty p : node2.getProperties()) {
							disjunctions.add(p);
						}
					} else {
						disjunctions.add(orProp2);
					}
					TCTLOrListNode orListNode = new TCTLOrListNode(disjunctions);
					parseStack.push(orListNode);
					break;

				case RuleConstants.RULE_AND_AND: // <And> ::= <And> and <Not>
				case RuleConstants.RULE_AND_AMPAMP: // <And> ::= <And> '&&'
													// <Not>
					TCTLAbstractStateProperty andProp2 = parseStack.pop();
					TCTLAbstractStateProperty andProp1 = parseStack.pop();
					ArrayList<TCTLAbstractStateProperty> conjunctions = new ArrayList<TCTLAbstractStateProperty>();
					if (andProp1 instanceof TCTLAndListNode) {
						TCTLAndListNode node1 = (TCTLAndListNode) andProp1;

						for (TCTLAbstractStateProperty p : node1.getProperties()) {
							conjunctions.add(p);
						}
					} else {
						conjunctions.add(andProp1);
					}

					if (andProp2 instanceof TCTLAndListNode) {
						TCTLAndListNode node2 = (TCTLAndListNode) andProp2;

						for (TCTLAbstractStateProperty p : node2.getProperties()) {
							conjunctions.add(p);
						}
					} else {
						conjunctions.add(andProp2);
					}
					TCTLAndListNode andListNode = new TCTLAndListNode(conjunctions);
					parseStack.push(andListNode);
					break;

				case RuleConstants.RULE_NOT_NOT_LPARAN_RPARAN: // <Not> ::= not '(' <Expr> ')'
				case RuleConstants.RULE_NOT_EXCLAM_LPARAN_RPARAN: // <Not> ::= '!' '(' <Expr> ')'
					TCTLNotNode notNode = new TCTLNotNode(parseStack.pop());
					parseStack.push(notNode);
					break;

				case RuleConstants.RULE_EXPR: // <Expr> ::= <Or>
				case RuleConstants.RULE_OR: // <Or> ::= <And>
				case RuleConstants.RULE_AND: // <And> ::= <Not>
				case RuleConstants.RULE_NOT: // <Not> ::= <Factor>
				case RuleConstants.RULE_FACTOR: // <Factor> ::= <AtomicProposition>
				case RuleConstants.RULE_FACTOR2:
				case RuleConstants.RULE_FACTOR_LPARAN_RPARAN: // <Factor> ::= '(' <Expr>')'
					break;
				case RuleConstants.RULE_FACTOR_TRUE: //<Factor> ::= True
					parseStack.push(new TCTLTrueNode());
					break;
				case RuleConstants.RULE_FACTOR_FALSE: //<Factor> ::= False
					parseStack.push(new TCTLFalseNode());
					break;
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_LT_NUM: // <AtomicProposition> ::= Identifier '<' Num
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_LTEQ_NUM: // <AtomicProposition> ::= Identifier '<=' Num
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_EQ_NUM: // <AtomicProposition> ::= Identifier '=' Num
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_EQEQ_NUM: // <AtomicProposition> ::= Identifier '==' Num
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_GTEQ_NUM: // <AtomicProposition> ::= Identifier '>=' Num
				case RuleConstants.RULE_ATOMICPROPOSITION1_IDENTIFIER_GT_NUM: // <AtomicProposition> ::= Identifier '>' Num
					String place = (String) createObjectFromTerminal(parser.currentReduction().getToken(0));
					String op = (String) createObjectFromTerminal(parser.currentReduction().getToken(1));
					Integer n = (Integer) createObjectFromTerminal(parser.currentReduction().getToken(2));
					parseStack.push(new TCTLAtomicPropositionNode(place, op, n));
					break;
				case RuleConstants.RULE_ATOMICPROPOSITION2_IDENTIFIER_DOT: // <AtomicProposition> ::= Identifier '.' <AtomicProposition1>
					TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) parseStack.pop();
					String template = (String) createObjectFromTerminal(parser.currentReduction().getToken(0));
					node.setTemplate(template);
					parseStack.push(node);
					break;
				}

				// // ************************************** log file
				// System.out.println("gpMsgReduction");
				// Reduction myRed = parser.currentReduction();
				// System.out.println(myRed.getParentRule().getText());
				// // ************************************** end log

				break;

			case gpMsgAccept:
				/* The program was accepted by the parsing engine */

				// // ************************************** log file
				// System.out.println("gpMsgAccept");
				// // ************************************** end log

				done = true;

				break;

			case gpMsgNotLoadedError:
				/* Load the Compiled Grammar Table file first. */

				// // ************************************** log file
				// System.out.println("gpMsgNotLoadedError");
				// // ************************************** end log
				done = true;
				throw new ParseException(ERROR_PARSING_QUERY_MESSAGE,0);
			case gpMsgLexicalError: /* Place code here to handle a illegal or unrecognized token To recover, pop the token from the stack: Parser.PopInputToken */
			case gpMsgSyntaxError:
				/*
				 * This is a syntax error: the source has produced a token that
				 * was not expected by the LALR State Machine. The expected
				 * tokens are stored into the Tokens() list. To recover, push
				 * one of the expected tokens onto the parser's input queue (the
				 * first in this case): You should limit the number of times
				 * this type of recovery can take place.
				 */
				done = true;
				Token theTok = parser.currentToken();
				System.out.println("Token not expected: "+ (String) theTok.getData());
				throw new ParseException(ERROR_PARSING_QUERY_MESSAGE,0);

				// // ************************************** log file
				// System.out.println("gpMsgSyntaxError");
				// // ************************************** end log

			case gpMsgCommentError:
				/*
				 * The end of the input was reached while reading a comment.
				 * This is caused by a comment that was not terminated
				 */

				// // ************************************** log file
				// System.out.println("gpMsgCommentError");
				// // ************************************** end log

				done = true;

				break;

			case gpMsgInternalError:
				/*
				 * Something horrid happened inside the parser. You cannot
				 * recover
				 */

				// // ************************************** log file
				// System.out.println("gpMsgInternalError");
				// // ************************************** end log
				done = true;
				throw new ParseException(ERROR_PARSING_QUERY_MESSAGE,0);

			}
		}
		try {
			parser.closeFile();
		} catch (ParserException parse) {
			System.out.println("**PARSER ERROR**\n" + parse.toString());
			System.exit(1);
		}

		return root;
	}

	private Object createObjectFromTerminal(Token token) {
		Object retObj = null;

		switch (token.getTableIndex()) {

		case SymbolConstants.SYMBOL_LT: // '<'
		case SymbolConstants.SYMBOL_LTEQ: // '<='
		case SymbolConstants.SYMBOL_EQ: // '='
		case SymbolConstants.SYMBOL_GT: // '>'
		case SymbolConstants.SYMBOL_GTEQ: // '>='
			String op = (String) token.getData();
			retObj = op;
			break;
		case SymbolConstants.SYMBOL_EQEQ: // '=='
			retObj = "=";
			break;
		case SymbolConstants.SYMBOL_IDENTIFIER: // Identifier
			String id = (String) token.getData();
			retObj = id;
			break;
		case SymbolConstants.SYMBOL_NUM: // Num
			Integer n = Integer.parseInt((String) token.getData());
			retObj = n;
		default:
			break; // should not happen
		}

		return retObj;
	}
}
