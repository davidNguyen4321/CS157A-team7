package util;

import java.util.logging.Logger;

public final class Loggers {
    public static final Logger SECURITY = Logger.getLogger("SecurityLog");
    public static final Logger OPERATION = Logger.getLogger("OperationLog");

    private Loggers() {}
}