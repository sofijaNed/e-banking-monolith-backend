package rs.ac.bg.fon.ebanking.correlation;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (context != null) MDC.setContextMap(context); else MDC.clear();
            try { runnable.run(); }
            finally {
                if (previous != null) MDC.setContextMap(previous); else MDC.clear();
            }
        };
    }
}
