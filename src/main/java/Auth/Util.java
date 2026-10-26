package Auth;

public class Util {
	/** 
	 * HTML escaping and image fallbacks:
	 * 
	 * index.jsp prints database text unescaped which is an XSS hole... use
	 * Util.esc for user supplied text.
	 *
	 */
	public static String esc(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    public static String or(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}
