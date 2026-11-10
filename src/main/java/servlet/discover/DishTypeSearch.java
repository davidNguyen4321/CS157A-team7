package servlet.discover;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/DishTypes/Search")
public class DishTypeSearch extends AbstractSearch {
    @Override protected String getTable() { return "DISH_TYPES"; }
    @Override protected String getIdCol() { return "DISH_TYPE_ID"; }
    @Override protected String getNameCol() { return "DISH_TYPE_NAME"; }
    @Override protected String getImageCol() { return null; } // No image
}