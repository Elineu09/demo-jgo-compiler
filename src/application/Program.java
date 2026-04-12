package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Program {

	public static void main(String[] args) {
		String path = "C:/temp/programa.txt";
		
		File file = new File(path);
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			String line = br.readLine();
			
			int i = 1;
			while (line != null) {
				System.out.printf("%d %s%n", i, line);
				line = br.readLine();
				i++;
			}
		} catch (IOException e) {
			System.out.println("Error opening file! " + e.getMessage());
		}

	}

}
