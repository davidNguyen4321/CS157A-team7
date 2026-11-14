package servlet.dropdown;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Users/Search")
public class AuthorDropdown extends AbstractDropdown {
    @Override protected String getTable() { return "USERS"; }
    @Override protected String getIdCol() { return "USER_ID"; }
    @Override protected String getNameCol() { return "USER_NAME"; }
    @Override protected String getImageCol() { return "USER_IMAGE_PATH"; }
    @Override protected String getActiveCol() {return "USER_STATUS"; }
}