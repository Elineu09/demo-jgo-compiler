package application;

import java.util.List;
import model.entities.Lexer;
import util.FileReaderUtil;

public class Program {
    public static void main(String[] args) {
        String path = "C:/temp/compiladores/programa.go"; 
        
        List<String> sourceCode = FileReaderUtil.readSourceFile(path);
        
        Lexer lexer = new Lexer(sourceCode);
        lexer.analex(); 
    }
} 