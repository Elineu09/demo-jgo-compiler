package model.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // PREDECLARED IDENTIFIERS (tratados como tokens)
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("nil", TokenType.NIL);
    }

    @Override
    public char lerCaractere() {
        if (linhaAtual >= sourceCode.size()) {
            return '\0';
        }

        String linha = sourceCode.get(linhaAtual);

        if (colunaAtual >= linha.length()) {
            linhaAtual++;
            colunaAtual = 0;
            return lerCaractere(); 
        }

        char c = linha.charAt(colunaAtual);
        colunaAtual++;
        return c;
    }

    @Override
    public void voltarCaractere() {
        if (colunaAtual > 0) {
            colunaAtual--;
        } else if (linhaAtual > 0) {
            linhaAtual--;
            colunaAtual = sourceCode.get(linhaAtual).length() - 1;
        }
    }

    @Override
    public void gravarTokenLexema(TokenType type, String lexeme) {
        if (type == TokenType.IDENTIFIER && keywords.containsKey(lexeme)) {
            type = keywords.get(lexeme);
        }
        
        Token token = new Token(type, lexeme, linhaAtual + 1, colunaAtual);
        symbolTable.add(token);
        System.out.println("Gravado: " + token);
    }

    @Override
    public void analex() {
        char c = lerCaractere();

        // O loop principal continua até encontrarmos o fim do ficheiro (EOF)
        while (c != '\0') {
            
            // 1. Ignorar espaços em branco e quebras de linha no estado inicial
            while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                c = lerCaractere();
            }

            // Se chegámos ao fim do ficheiro depois de ignorar os espaços, saímos do loop
            if (c == '\0') break;

            int state = 0; // Reiniciamos o estado para o q0
            StringBuilder lexeme = new StringBuilder();
            boolean tokenFound = false;

            // Loop secundário para construir um único token
            while (!tokenFound && c != '\0') {
                switch (state) {
                    
                    case 0: // ESTADO INICIAL (q0)
                        if (Character.isLetter(c) || c == '_') {
                            state = 1; // Vai para q1 (Identificador / Palavra-chave)
                            lexeme.append(c);
                            c = lerCaractere();
                        } 
                        else if (Character.isDigit(c)) {
                            state = 2; // Vai para q2 (Número Inteiro)
                            lexeme.append(c);
                            c = lerCaractere();
                        } 
                        else if (c == '=') {
                            state = 54; // Vai para q54 (Verificar Atribuição ou Igualdade)
                            lexeme.append(c);
                            c = lerCaractere();
                        } 
                        else {
                            // Símbolos de 1 caractere (pontuação, parênteses, etc.)
                            lexeme.append(c);
                            TokenType type = TokenType.UNKNOWN;
                            
                            // Exemplos de mapeamento direto:
                            if (c == '[') type = TokenType.LBRACKET;
                            else if (c == ']') type = TokenType.RBRACKET;
                            else if (c == '(') type = TokenType.LPAREN;
                            else if (c == ')') type = TokenType.RPAREN;
                            else if (c == '+') type = TokenType.PLUS;
                            // ... podes adicionar os restantes símbolos de 1 caractere aqui

                            gravarTokenLexema(type, lexeme.toString());
                            tokenFound = true;
                            c = lerCaractere(); // Lê o próximo para o token seguinte
                        }
                        break;

                    case 1: // q1: CONSTRUINDO IDENTIFICADOR
                        if (Character.isLetterOrDigit(c) || c == '_') {
                            lexeme.append(c);
                            c = lerCaractere();
                        } else {
                            // Lemos "outro" caractere que não pertence ao identificador!
                            voltarCaractere(); 
                            gravarTokenLexema(TokenType.IDENTIFIER, lexeme.toString());
                            tokenFound = true;
                            c = lerCaractere(); // Relê o caractere devolvido para iniciar o próximo token
                        }
                        break;

                    case 2: // q2: CONSTRUINDO NÚMERO INTEIRO
                        if (Character.isDigit(c)) {
                            lexeme.append(c);
                            c = lerCaractere();
                        } else {
                            // Lemos "outro" caractere (ex: um espaço ou um ponto e vírgula)
                            voltarCaractere();
                            gravarTokenLexema(TokenType.INT_LITERAL, lexeme.toString());
                            tokenFound = true;
                            c = lerCaractere();
                        }
                        break;

                    case 54: // q54: LEMOS UM '=' NO q0
                        if (c == '=') {
                            // Lemos outro '=', logo o lexema é "=="
                            lexeme.append(c);
                            gravarTokenLexema(TokenType.EQUAL, lexeme.toString());
                            tokenFound = true;
                            c = lerCaractere();
                        } else {
                            // Lemos "outro" caractere, então era apenas uma atribuição "="
                            voltarCaractere();
                            gravarTokenLexema(TokenType.ASSIGN, lexeme.toString());
                            tokenFound = true;
                            c = lerCaractere();
                        }
                        break;
                }
            }
        }
        
        // Quando o loop termina, gravamos o token final
        gravarTokenLexema(TokenType.EOF, "EOF");
    }
}