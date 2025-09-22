package rs.ac.bg.fon.ebanking.correlation;

import org.slf4j.MDC;

public final class Correlation {
    private Correlation(){}
    public static String id() { return MDC.get("cid"); }
}
