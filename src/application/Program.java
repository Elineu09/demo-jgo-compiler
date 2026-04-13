package application;

import java.util.Map;

import util.FileReaderUtil;

public class Program {

	public static void main(String[] args) {
		String path = "C:/temp/programa.txt";
		
		Map<String, String> code = FileReaderUtil.readSourceFile(path);
		
		code.forEach((key, value) -> System.out.printf("%s %s%n",key,value));
	}

}
