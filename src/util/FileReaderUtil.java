package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileReaderUtil {
	public static Map<String, String> readSourceFile(String path) {
		File file = new File(path);
		Map<String, String> code = new LinkedHashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();

			int i = 1;
			while (line != null) {
				code.put(String.valueOf(i), line);
				line = br.readLine();
				i++;
			}
			
		} catch (IOException e) {
			System.out.println("Error opening file! " + e.getMessage());
		}
		
		return code;
	}
}
