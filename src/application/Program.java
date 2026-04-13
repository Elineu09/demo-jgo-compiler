package application;

import java.util.List;

import util.FileReaderUtil;

public class Program {

	public static void main(String[] args) {
		String path = "C:/temp/programa.txt";
		
		List<String> code = FileReaderUtil.readSourceFile(path);
		
		code.forEach(System.out::println);
	}

}
