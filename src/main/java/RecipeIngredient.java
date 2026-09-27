
public class RecipeIngredient {
	String name;
	int id;
	Double amount;
	String amountunit;
	public RecipeIngredient(String name, Double amount, String amountunit) {
		this.name = name;
		this.amount = amount;
		this.amountunit = amountunit;
	}
	
	public RecipeIngredient(String name, int amount, String amountunit) {
		this.name = name;
		this.amount = Double.valueOf(amount);
		this.amountunit = amountunit;
	}
}
