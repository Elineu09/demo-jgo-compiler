package model.entities;

import java.util.*;

import model.enums.TokenType;

public class Lexer implements IScanner {

	private List<String> sourceCode;
	private int linhaAtual = 0;
	private int colunaAtual = 0;

	private List<Token> symbolTable = new ArrayList<>();
	private Map<String, TokenType> keywords = new HashMap<>();

	public Lexer(List<String> sourceCode) {
		this.sourceCode = sourceCode;
		initKeywords();
	}

	private void initKeywords() {
        keywords.put("break", TokenType.BREAK);
        keywords.put("default", TokenType.DEFAULT);
        keywords.put("func", TokenType.FUNC);
        keywords.put("interface", TokenType.INTERFACE);
        keywords.put("select", TokenType.SELECT);

        keywords.put("case", TokenType.CASE);
        keywords.put("defer", TokenType.DEFER);
        keywords.put("go", TokenType.GO);
        keywords.put("map", TokenType.MAP);
        keywords.put("struct", TokenType.STRUCT);

        keywords.put("chan", TokenType.CHAN);
        keywords.put("else", TokenType.ELSE);
        keywords.put("goto", TokenType.GOTO);
        keywords.put("package", TokenType.PACKAGE);
        keywords.put("switch", TokenType.SWITCH);

        keywords.put("const", TokenType.CONST);
        keywords.put("fallthrough", TokenType.FALLTHROUGH);
        keywords.put("if", TokenType.IF);
        keywords.put("range", TokenType.RANGE);
        keywords.put("type", TokenType.TYPE);

        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("for", TokenType.FOR);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("var", TokenType.VAR);

        // PREDECLARED IDENTIFIERS
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("nil", TokenType.NIL);
    }

	@Override
	public char lerCaractere() {
		if (linhaAtual >= sourceCode.size())
			return '\0';

		String linha = sourceCode.get(linhaAtual);

		if (colunaAtual >= linha.length()) {
			linhaAtual++;
			colunaAtual = 0;
			return '\n';
		}

		return linha.charAt(colunaAtual++);
	}

	@Override
	public void voltarCaractere() {
		if (colunaAtual > 0) {
			colunaAtual--;
		} else if (linhaAtual > 0) {
			linhaAtual--;
			colunaAtual = sourceCode.get(linhaAtual).length();
		}
	}

	@Override
	public void gravarTokenLexema(TokenType type, String lexeme) {

		if (type == TokenType.IDENTIFIER && keywords.containsKey(lexeme)) {
			type = keywords.get(lexeme);
		}

		Token token = new Token(type, lexeme, linhaAtual + 1, colunaAtual);
		symbolTable.add(token);

		System.out.println(token);
	}

	@Override
	public void analex() {

		char c = lerCaractere();

		while (c != '\0') {

			while (Character.isWhitespace(c)) {
				c = lerCaractere();
			}

			if (c == '\0')
				break;

			int state = 0;
			StringBuilder lexeme = new StringBuilder();
			boolean done = false;

			while (!done) {

				switch (state) {

				// Estado inicial
				case 0:

					if (Character.isLetter(c) || c == '_') {
						state = 1;
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (Character.isDigit(c)) {
						state = 2;
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '"') {
						state = 10; // string literal
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '\'') {
						state = 11; // rune literal
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '/') {
						state = 20; // comentário ou divisão
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == ':') {
						state = 30; // := ou :
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '<') {
						state = 40; // <, <=, <<, <-
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '>') {
						state = 41; // >, >=, >>
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '&') {
						state = 42; // &, &&, &^
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '|') {
						state = 43; // |, ||
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '+') {
						state = 44; // +, ++, +=
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '-') {
						state = 45; // -, --, -=
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '=') {
						state = 46; // =, ==
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '*') {
						state = 47; // *, *=
						lexeme.append(c);
						c = lerCaractere();
					}

					else if (c == '%') {
						state = 48; // %, %=
						lexeme.append(c);
						c = lerCaractere();
					}

					else {
						lexeme.append(c);
						gravarTokenLexema(mapSingleChar(c), lexeme.toString());
						done = true;
						c = lerCaractere();
					}

					break;

				// Identificador
				case 1:
					if (Character.isLetterOrDigit(c) || c == '_') {
						lexeme.append(c);
						c = lerCaractere();
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.IDENTIFIER, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// Número inteiro
				case 2:
					if (Character.isDigit(c)) {
						lexeme.append(c);
						c = lerCaractere();
					} else if (c == '.') {
						state = 3;
						lexeme.append(c);
						c = lerCaractere();
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.INT_LITERAL, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// Número float
				case 3:
					if (Character.isDigit(c)) {
						lexeme.append(c);
						c = lerCaractere();
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.FLOAT_LITERAL, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// String literal
				case 10:
					if (c != '"') {
						lexeme.append(c);
						c = lerCaractere();
					} else {
						lexeme.append(c);
						gravarTokenLexema(TokenType.STRING_LITERAL, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// Rune literal
				case 11:
					lexeme.append(c);
					c = lerCaractere();

					if (c == '\'') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.RUNE_LITERAL, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// '/' — comentário ou divisão
				case 20:
					if (c == '/') {
						state = 21; // comentário de linha
					} else if (c == '*') {
						state = 22; // comentário de bloco
					} else if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.DIVIDE_ASSIGN, lexeme.toString());
						done = true;
						c = lerCaractere();
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.DIVIDE, lexeme.toString());
						done = true;
						c = lerCaractere();
					}
					break;

				// Comentário de linha
				case 21:
					while (c != '\n' && c != '\0') {
						lexeme.append(c);
						c = lerCaractere();
					}
					gravarTokenLexema(TokenType.LINE_COMMENT, lexeme.toString());
					done = true;
					break;

				// Comentário de bloco
				case 22:
					while (true) {
						if (c == '*' && lerCaractere() == '/') {
							break;
						}
						c = lerCaractere();
					}
					gravarTokenLexema(TokenType.BLOCK_COMMENT, lexeme.toString());
					done = true;
					c = lerCaractere();
					break;

				// ':' — := ou :
				case 30:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.DEFINE, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.COLON, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '<' — <, <=, <<, <-
				case 40:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.LESS_EQUAL, lexeme.toString());
					} else if (c == '<') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.LEFT_SHIFT, lexeme.toString());
					} else if (c == '-') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.ARROW, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.LESS, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '>' — >, >=, >>
				case 41:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.GREATER_EQUAL, lexeme.toString());
					} else if (c == '>') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.RIGHT_SHIFT, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.GREATER, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '&' — &, &&, &^
				case 42:
					if (c == '&') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.LOGICAL_AND, lexeme.toString());
					} else if (c == '^') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.BIT_CLEAR, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.BITWISE_AND, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '|' — |, ||
				case 43:
					if (c == '|') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.LOGICAL_OR, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.BITWISE_OR, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '+' — +, ++, +=
				case 44:
					if (c == '+') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.INCREMENT, lexeme.toString());
					} else if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.PLUS_ASSIGN, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.PLUS, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '-' — -, --, -=
				case 45:
					if (c == '-') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.DECREMENT, lexeme.toString());
					} else if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.MINUS_ASSIGN, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.MINUS, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '=' — =, ==
				case 46:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.EQUAL, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.ASSIGN, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '*' — *, *=
				case 47:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.MULTIPLY_ASSIGN, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.MULTIPLY, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;

				// '%' — %, %=
				case 48:
					if (c == '=') {
						lexeme.append(c);
						gravarTokenLexema(TokenType.MOD_ASSIGN, lexeme.toString());
					} else {
						voltarCaractere();
						gravarTokenLexema(TokenType.MOD, lexeme.toString());
					}
					done = true;
					c = lerCaractere();
					break;
				}
			}
		}

		gravarTokenLexema(TokenType.EOF, "EOF");
	}

	private TokenType mapSingleChar(char c) {

		switch (c) {
		case '(':
			return TokenType.LPAREN;
		case ')':
			return TokenType.RPAREN;
		case '{':
			return TokenType.LBRACE;
		case '}':
			return TokenType.RBRACE;
		case '[':
			return TokenType.LBRACKET;
		case ']':
			return TokenType.RBRACKET;
		case ';':
			return TokenType.SEMICOLON;
		case ',':
			return TokenType.COMMA;
		case '.':
			return TokenType.DOT;
		default:
			return TokenType.UNKNOWN;
		}
	}
}