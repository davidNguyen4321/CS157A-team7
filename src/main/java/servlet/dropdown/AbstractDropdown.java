package servlet.dropdown;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.error.ServletSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractDropdown extends HttpServlet {

		protected abstract String getTable();
	    protected abstract String getIdCol();
	    protected abstract String getNameCol();
	    protected String getImageCol() {return null; }
	    protected String getActiveCol() {return null; }
	    
	    private final DropdownDAO dropdownDAO = new DropdownDAO();
	    private final ObjectMapper mapper = new ObjectMapper();
		
		@Override
		public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
					    
		    DropdownRequest dropdownRequest = new DropdownRequest(
		    		ServletSecurity.getValidatedParameters(request, new String[] {"searchTerm", "excludeArray"}, false)
		    );
		    			
            
            try (PrintWriter pw = response.getWriter()) {
                pw.write(mapper.writeValueAsString(dropdownDAO.search(this, dropdownRequest)));
            }
	    }
}