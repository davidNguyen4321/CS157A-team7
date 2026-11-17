package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Logger;

public final class Loggers {
    public static final Logger SECURITY = Logger.getLogger("SecurityLog");
    public static final Logger OPERATION = Logger.getLogger("OperationLog");

    private Loggers() {}
    
    public static String describe(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        int userId = (session == null) ? null : (Integer) session.getAttribute("userId");

        return request.getMethod() + " " + request.getRequestURI()
             + " from " + request.getRemoteAddr()
             + " (user " + userId + ")";
    }
}