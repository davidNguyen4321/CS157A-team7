package servlet.discover;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Cuisines/Search")
public class CuisineDropdown extends DropdownSearch {
    @Override protected String getTable() { return "CUISINES"; }
    @Override protected String getIdCol() { return "CUISINE_ID"; }
    @Override protected String getNameCol() { return "CUISINE_NAME"; }
    @Override protected String getImageCol() { return null; } // No image
}