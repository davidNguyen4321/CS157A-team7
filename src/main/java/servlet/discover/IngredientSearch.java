package servlet.discover;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Ingredients/Search")
public class IngredientSearch extends AbstractSearch {
    @Override protected String getTable() { return "INGREDIENTS"; }
    @Override protected String getIdCol() { return "INGREDIENT_ID"; }
    @Override protected String getNameCol() { return "INGREDIENT_NAME"; }
    @Override protected String getImageCol() { return "INGREDIENT_IMAGE_PATH"; }
}