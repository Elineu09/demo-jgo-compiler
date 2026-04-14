package model.enums;

public enum TokenType {

    // KEYWORDS
    BREAK, DEFAULT, FUNC, INTERFACE, SELECT,
    CASE, DEFER, GO, MAP, STRUCT,
    CHAN, ELSE, GOTO, PACKAGE, SWITCH,
    CONST, FALLTHROUGH, IF, RANGE, TYPE,
    CONTINUE, FOR, IMPORT, RETURN, VAR,

    // IDENTIFIERS
    IDENTIFIER,

    // LITERALS
    INT_LITERAL,
    FLOAT_LITERAL,
    IMAGINARY_LITERAL,
    RUNE_LITERAL,      // char 
    STRING_LITERAL,
    RAW_STRING_LITERAL,

    TRUE, FALSE,       // bool literals
    NIL,               // null equivalente

    // OPERATORS

    // Aritméticos
    PLUS, MINUS, MULTIPLY, DIVIDE, MOD,

    // Incremento / Decremento 
    INCREMENT, DECREMENT,

    // Atribuição
    ASSIGN,            // =
    DEFINE,            // :=

    PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MOD_ASSIGN,
    AND_ASSIGN, OR_ASSIGN, XOR_ASSIGN,
    LEFT_SHIFT_ASSIGN, RIGHT_SHIFT_ASSIGN,
    BIT_CLEAR_ASSIGN,  // &^

    // Relacionais
    EQUAL, NOT_EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,

    // Lógicos
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT, XOR,

    // Bitwise
    BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT, // &, |, ^, ^
    BIT_CLEAR,         // &^

    // Shift
    LEFT_SHIFT, RIGHT_SHIFT, // <<, >>

    // Canal
    ARROW,             // <-

    // SEPARATORS
    LPAREN, RPAREN,           // ( )
    LBRACE, RBRACE,           // { }
    LBRACKET, RBRACKET,       // [ ]
    COMMA,                    // ,
    DOT,                      // .
    SEMICOLON,                // ; 
    COLON,                    // :
    ELLIPSIS,                 // ...

    // COMMENTS
    LINE_COMMENT,             // //
    BLOCK_COMMENT,            // /* */

    // SPECIAL
    WHITESPACE,
    NEWLINE,
    UNKNOWN,
    EOF
}
