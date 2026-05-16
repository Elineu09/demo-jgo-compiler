package model.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.enums.TokenType;
import model.exceptions.SyntaxException;

public class Parser {
	private List<Token> tokens;
	private int currentTokenIndex;
	private Token currentToken;

	// Tabela de símbolos completa
	private Map<String, Symbol> symbolTable;
	private int currentScopeLevel;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		this.currentTokenIndex = 0;
		this.symbolTable = new HashMap<>();
		this.currentScopeLevel = 0;

		if (!tokens.isEmpty()) {
			this.currentToken = tokens.get(0);
		}
	}

	// Avança para o próximo token
	private void advance() {
		currentTokenIndex++;
		if (currentTokenIndex < tokens.size()) {
			currentToken = tokens.get(currentTokenIndex);
		} else {
			currentToken = tokens.get(tokens.size() - 1);
		}
	}

	// Valida se o toke atual é o esperado
	private Token match(TokenType expectedType) {
		if (currentToken.getType() == expectedType) {
			Token consumedToken = currentToken;
			advance();
			return consumedToken;
		} else {
			throw new SyntaxException(String.format("Syntax Error. Expected %s, but found %s on lexem '%s'.",
					expectedType, currentToken.getType(), currentToken.toString()));
		}
	}

	// Regista variáveis na Tabela de Símbolos
	private void addSymbol(String name, String type, String category) {
		if (symbolTable.containsKey(name) && symbolTable.get(name).getScopeLevel() == currentScopeLevel) {
			throw new SyntaxException(
					"Initial Semantic Error: identifier '" + name + "' was already delcared in this scope.");
		}
		Symbol symbol = new Symbol(name, type, category, currentScopeLevel);
		symbolTable.put(name, symbol);
		System.out.println("Symbol table -> Added: " + symbol);
	}

	// Top-Down

	public void parse() {
		try {
			parseProgram();
			System.out.println("\nSyntatic analysis successfully completed!");
		} catch (SyntaxException e) {
			System.err.println(e.getMessage());
		}
	}

	// Program -> "package" IDENTIFICADOR ListaImports ListaDeclaracoes EOF
	private void parseProgram() {
		match(TokenType.PACKAGE);
		match(TokenType.IDENTIFIER);
		
		parseImports();
		parseDeclarations();
		
		match(TokenType.EOF);
	}
	
	// Imports 
	private void parseImports() {
		while(currentToken.getType() == TokenType.IMPORT) {
			advance(); // consome 'import'
			
			if (currentToken.getType() == TokenType.LPAREN) {
				advance();
				while (currentToken.getType() == TokenType.STRING_LITERAL) {
					advance();
				}
				match(TokenType.RPAREN);
			} else {
				match(TokenType.STRING_LITERAL);
			}
		}
	}
	
	
	// Declarações (var and func)
	
	private void parseDeclarations() {
		while (currentToken.getType() == TokenType.VAR || currentToken.getType() == TokenType.FUNC) {
			if (currentToken.getType() == TokenType.VAR) {
				parseVarDeclaration();
			} else if (currentToken.getType() == TokenType.FUNC) {
				parseFuncDeclaration();
			}
		}
	}
	
	// DeclaracaoVar -> "var" IDENTIFICADOR TipoOpcional InicializacaoOpcional
	private void parseVarDeclaration() {
		match(TokenType.VAR);
		Token idToken = match(TokenType.IDENTIFIER);
		
		String type ="inferred";
		
		// verifica se declarou tipo explícito 
		if (isTypeKeyword(currentToken.getType())) {
			type = currentToken.getType().toString();
			advance();
		}
		
		// iniicalização opcional
		if (currentToken.getType() == TokenType.ASSIGN) {
			advance();
			parseExpression();
		}
		
		addSymbol(idToken.getValue(), type, "VAR");
	}
	
	// DeclaracaoFunc -> "func" IDENTIFICADOR "(" ParametrosOpcionais ")" RetornoOpcional Bloco
	
	private void parseFuncDeclaration() {
		match(TokenType.FUNC);
		Token idToken = match(TokenType.IDENTIFIER);
		
		addSymbol(idToken.getValue(), "func_type", "FUNC");
		
		match(TokenType.LPAREN);
		parseOptionalParameters();
		match(TokenType.RPAREN);
		
		// retorno opcional 
		if (isTypeKeyword(currentToken.getType())) {
			advance();
		}
		
		parseBlock();
	}
	
	private void parseOptionalParameters() {
		if (currentToken.getType() == TokenType.IDENTIFIER) {
			match(TokenType.IDENTIFIER);
			if (isTypeKeyword(currentToken.getType())) advance();
			
			while (currentToken.getType() == TokenType.COMMA) {
				advance();
				match (TokenType.IDENTIFIER);
				if (isTypeKeyword(currentToken.getType())) advance();
			}
		}
	}
	
	// Blocos e comandos
	
	private void parseBlock() {
		match(TokenType.LBRACE);
		currentScopeLevel++;
		
		while (currentToken.getType() != TokenType.RBRACE && currentToken.getType() != TokenType.EOF) {
			parseStatement();
			
			// consome ponto e vírgula opcional
			if (currentToken.getType() == TokenType.SEMICOLON) {
				advance();
			}
		}
		
		currentScopeLevel--;
		match(TokenType.RBRACE);
	}
	
	// Comando 
	private void parseStatement() {
		TokenType type = currentToken.getType();
		
		if (type == TokenType.VAR) {
			parseVarDeclaration();
		} else if (type == TokenType.IF) {
			parseIfStatement();
		} else if (type == TokenType.FOR) {
			parseForStatement();
		} else if (type == TokenType.RETURN) {
			advance();
			if (currentToken.getType() != TokenType.RBRACE && currentToken.getType() != TokenType.SEMICOLON) {
				parseExpression();
			}
		} else if (type == TokenType.IDENTIFIER) {
			/* * FACTORIZAÇÃO À ESQUERDA RESOLVIDA: 
             * Um IDENTIFICADOR pode iniciar uma atribuição (=), uma atribuição curta (:=), 
             * um incremento (++), uma chamada de função `()` ou um acesso a array `[]`.
             */
			
			Token idToken = match(TokenType.IDENTIFIER);
			
			if (currentToken.getType() == TokenType.DEFINE) {
				// atribuição curta: x := 5
				advance();
				parseExpression();
				addSymbol(idToken.getValue(), "inferred", "VAR");
			} else if (currentToken.getType() == TokenType.ASSIGN || currentToken.getType() == TokenType.PLUS_ASSIGN) {
				// atribuição clássica: x = 5
				advance();
				parseExpression();
			} else if (currentToken.getType() == TokenType.INCREMENT || currentToken.getType() == TokenType.DECREMENT) {
				// incremento/decremento: x++ ou x--
				advance();
			} else if (currentToken.getType() == TokenType.LPAREN ) {
				// chamada de função: func(x)
				advance();
				parseOptionalArguments();
				match(TokenType.RPAREN);
			} else if (currentToken.getType() == TokenType.LBRACKET) {
				// acesso a array
				advance();
				parseExpression();
				match(TokenType.RBRACKET);
				if (currentToken.getType() == TokenType.ASSIGN) {
					advance();
					parseExpression();
				} else {
					throw new SyntaxException("Invalid command starting with the identifier " + idToken.getValue());
				}
			} else {
				parseExpression();
			}
		}
	}
	
	private void parseIfStatement() {
		match(TokenType.IF);
		parseExpression();
		parseBlock();
		
		// Resolve ambiguidade "Dangling Else" - Associa ao IF mais próximo
		if (currentToken.getType() == TokenType.ELSE) {
			advance();
			if (currentToken.getType() == TokenType.IF) {
				parseIfStatement(); // else if
			} else {
				parseBlock(); // else puro
			}
		}
	}
	
	private void parseForStatement() {
		// Fatorização do For Clássico vs For Condicional
        // Se após ler as expressões encontrarmos um ';', é clássico.
        // Simplificação top-down sem backtracking: Analisamos até ao Bloco '{'
        
        // Como o for clássico tem ";", e o condicional não, verificamos o Lookahead.
		if (currentToken.getType() != TokenType.LBRACE) {
			// Vamos consumir até ao bloco, no teu trabalho podes assumir "parseExpression" 
            // e se houver ';', consome as outras partes.
			parseExpression();
			
			if (currentToken.getType() == TokenType.SEMICOLON) {
				advance(); // Primeiro ;
				parseExpression();
				match(TokenType.SEMICOLON); // Segundo ;
				
				// Update do for (x++)
				if (currentToken.getType() == TokenType.IDENTIFIER) {
					advance();
					if (currentToken.getType() == TokenType.INCREMENT || currentToken.getType() == TokenType.DECREMENT) {
						advance();
					} else if (currentToken.getType() == TokenType.ASSIGN) {
						advance();
						parseExpression();
					}
				}
			}
		}
		parseBlock();
	}
	
	// Expressões (descendente recursivo com precedência)
	
	private void parseExpression() {
		parseLogicalOr();
	}
	
	private void parseLogicalOr() {
		parseLogicalAnd();
		while (currentToken.getType() == TokenType.LOGICAL_OR) {
			advance();
			parseLogicalAnd();
		}
	}
	
	private void parseLogicalAnd() {
		parseRelational();
		while (currentToken.getType() == TokenType.LOGICAL_AND) {
			advance();
			parseRelational();
		}
	}
	
	private void parseRelational() {
		parseSum();
		while (isRelationalOperator(currentToken.getType())) {
			advance();
			parseSum();
		}
	}
	
	private void parseSum() {
		parseMult();
		// A recursividade à esquerda foi eliminada com este while (EBNF)
		while (currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS) {
			advance();
			parseMult();
		}
	}
	
	private void parseMult() {
		parseUnary();
		while(currentToken.getType() == TokenType.MULTIPLY || currentToken.getType() == TokenType.DIVIDE || currentToken.getType() == TokenType.MOD) {
			advance();
			parseUnary();
		}
	}
	
	private void parseUnary() {
		if (currentToken.getType() == TokenType.MINUS || currentToken.getType() == TokenType.LOGICAL_NOT) {
			advance();
			parseUnary();
		} else {
			parseFactor();
		}
	}
	
	private void parseFactor() {
		TokenType type = currentToken.getType();
		
		if (isLiteral(type)) {
			advance(); // consome número, string ou booleano
		} else if (type == TokenType.LPAREN) {
			advance();
			parseExpression();
			match(TokenType.RPAREN);
		} else if (type == TokenType.IDENTIFIER) {
			// Factorização no fator
			advance();
			
			if (currentToken.getType() == TokenType.LPAREN) {
				// chamada de função: funcName(...)
				advance();
				parseOptionalArguments();
				match(TokenType.RPAREN);
			} else if (currentToken.getType() == TokenType.LBRACKET) {
				// acesso a array: array[...]
				advance();
				parseExpression();
				match(TokenType.RBRACKET);
			} else if (currentToken.getType() == TokenType.DOT) {
				// caminho qualificado: obj.propriedade
				advance();
				match(TokenType.IDENTIFIER);
			}
		} else {
			throw new SyntaxException("Invalid Expression. Unexpected factor " + currentToken.getValue());
		}
	}
	
	private void parseOptionalArguments() {
		if (currentToken.getType() != TokenType.RPAREN) {
			parseExpression();
			while (currentToken.getType() == TokenType.COMMA) {
				advance();
				parseExpression();
			}
		}
	}
	
	// Helpers para agrupar Tokens
	
	private boolean isTypeKeyword(TokenType type) {
		return type == TokenType.IDENTIFIER || type == TokenType.INTERFACE || type == TokenType.MAP;
	}
	
	private boolean isRelationalOperator(TokenType type) {
		return type == TokenType.EQUAL || type == TokenType.NOT_EQUAL ||
	               type == TokenType.GREATER || type == TokenType.GREATER_EQUAL ||
	               type == TokenType.LESS || type == TokenType.LESS_EQUAL;
	}
	
	private boolean isLiteral (TokenType type) {
		return type == TokenType.INT_LITERAL || type == TokenType.FLOAT_LITERAL ||
	               type == TokenType.STRING_LITERAL || type == TokenType.RAW_STRING_LITERAL ||
	               type == TokenType.TRUE || type == TokenType.FALSE;
	}
}


