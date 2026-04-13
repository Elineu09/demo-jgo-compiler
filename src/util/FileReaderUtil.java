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
		List<String> code = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();

			int i = 1;
			while (line != null) {
				// System.out.printf("%d %s%n", i, line);
				code.add(line);
				line = br.readLine();
				i++;
			}
			
		} catch (IOException e) {
			System.out.println("Error opening file! " + e.getMessage());
		}
		
		return code;
	}
}
