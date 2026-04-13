package application;

import java.util.Map;

import util.FileReaderUtil;

public class Program {

	public static void main(String[] args) {
		String path = "C:/temp/compiladores/programa.go";
		
		Map<String, String> sourceCode = FileReaderUtil.readSourceFile(path);
		
		sourceCode.forEach((key, value) -> System.out.printf("%s %s%n",key,value));
	}

}
