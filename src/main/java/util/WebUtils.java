package util;

public class WebUtils {
    private WebUtils() {}
	
	// Converts user input into harmless text rather than executable code
	public static String esc(String s) {
		if (s == null) return "";
		
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
	
	public static String escJS(String input) {
	    if (input == null) {
	        return null;
	    }
	    return input.replace("\\", "\\\\")
	                .replace("\"", "\\\"")
	                .replace("'", "\\'")
	                .replace("\n", "\\n")
	                .replace("\r", "\\r")
	                .replace("<", "\\u003C")
	                .replace(">", "\\u003E");
	}
	
	// Fallback for anonymous user or no profile image
    public static String or(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}