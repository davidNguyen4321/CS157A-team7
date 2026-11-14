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
	
	// Fallback for anonymous user or no profile image
    public static String or(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}