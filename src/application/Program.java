package application;

import java.util.List;

import model.entities.Lexer;
import model.entities.Parser;
import util.FileReaderUtil;

public class Program {
    public static void main(String[] args) {
        String path = "C:/temp/compiladores/programa.go"; 
        
        System.out.println("### INICIANDO ANÁLISE LÉXICA ###");
        List<String> sourceCode = FileReaderUtil.readSourceFile(path);
        
        Lexer lexer = new Lexer(sourceCode);
        lexer.analex(); 
        
        System.out.println("### INICIANDO ANÁLISE SINTÁTICA ###");
        Parser parser = new Parser(lexer.getTokens());
        
        parser.parse();
    }
} 