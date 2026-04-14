package model.entities;

import model.enums.TokenType;

public class Token {
    private TokenType type;
    private String value;
    private int line; 
    private int column;
    
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }
    
    @Override 
    public String toString() {
        return String.format("[%02d:%02d] %s(\"%s\")", line, column, type, value);
    }
}