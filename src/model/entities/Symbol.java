package model.entities;

public class Symbol {
	private String name;
	private String type; 
	private String category; 
	private int scopeLevel;
	
	private boolean isInitialized;

	public Symbol(String name, String type, String category, int scopeLevel) {
		super();
		this.name = name;
		this.type = type;
		this.category = category;
		this.scopeLevel = scopeLevel;
		this.isInitialized = false;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getCategory() {
		return category;
	}

	public int getScopeLevel() {
		return scopeLevel;
	}
	
	@Override 
	public String toString() {
		return String.format("Symbol[Name: %s], Type: %s, Category: %s, Scope: %d]", name, type, category, scopeLevel);
	}
	
	
	
}
