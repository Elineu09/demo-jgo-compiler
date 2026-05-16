package model.exceptions;

public class SyntaxException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public SyntaxException(String message) {
		super(message);
	}
}
