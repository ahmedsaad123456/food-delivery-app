package com.example.food_delivery_app.config;


import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;


// TaskDecorator that copies the SecurityContext and MDC from the caller thread to the worker thread.
public class ContextPropagatingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {
        // ---- this part runs on the CALLER thread (e.g. the Tomcat request thread) ----
        SecurityContext callerContext = SecurityContextHolder.getContext();
        // a NEW context object holding the same Authentication, so the two threads never share one mutable object
        SecurityContext copiedContext = SecurityContextHolder.createEmptyContext();
        copiedContext.setAuthentication(callerContext.getAuthentication());
        Map<String, String> callerMdc = MDC.getCopyOfContextMap(); // may be null if MDC is empty

        return () -> {
            // ---- this part runs on the WORKER thread ----

            // save what the worker thread had BEFORE us (usually nothing) so we can restore it later
            SecurityContext previousContext = SecurityContextHolder.getContext();
            Map<String, String> previousMdc = MDC.getCopyOfContextMap();
            try {
                // set the SecurityContext and MDC for the worker thread to what the caller thread had
                SecurityContextHolder.setContext(copiedContext);
                if (callerMdc != null) MDC.setContextMap(callerMdc); else MDC.clear();
                task.run();
            } finally {
                // restore what the worker thread had BEFORE us (usually nothing) and never leave leftovers
                if (previousContext.getAuthentication() == null) SecurityContextHolder.clearContext();
                else SecurityContextHolder.setContext(previousContext);
                if (previousMdc != null) MDC.setContextMap(previousMdc); else MDC.clear();
            }
        };
    }
}