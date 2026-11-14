package servlet.dropdown;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Appliances/Search")
public class ApplianceDropdown extends AbstractDropdown {
    @Override protected String getTable() { return "APPLIANCES"; }
    @Override protected String getIdCol() { return "APPLIANCE_ID"; }
    @Override protected String getNameCol() { return "APPLIANCE_NAME"; }
    @Override protected String getImageCol() { return "APPLIANCE_IMAGE_PATH"; }
}