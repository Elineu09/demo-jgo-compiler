package model.entities;

import model.enums.TokenType;

public interface IScanner {
    char lerCaractere();
    void voltarCaractere();
    void analex();
    void gravarTokenLexema(TokenType type, String lexeme);
}