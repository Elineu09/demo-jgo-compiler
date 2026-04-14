package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReaderUtil {
    public static List<String> readSourceFile(String path) {
        File file = new File(path);
        List<String> sourceCode = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                // Adicionamos a quebra de linha no final para o Lexer saber quando a linha acaba
                sourceCode.add(line + '\n'); 
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error opening file! " + e.getMessage());
        }
        
        return sourceCode;
    }
}