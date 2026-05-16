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
}
