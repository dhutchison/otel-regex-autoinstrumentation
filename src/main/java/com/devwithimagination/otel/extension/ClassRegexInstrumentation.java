package com.devwithimagination.otel.extension;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ClassRegexInstrumentation implements TypeInstrumentation {
    // Create a Logger instance for logging.
    private static Logger logger = Logger.getLogger(ClassRegexInstrumentation.class.getName());

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        logger.info("TEST typeMatcher");
        return nameStartsWith("com.devwithimagination");
        // return isAnnotatedWith(named("org.springframework.stereotype.Service"))
        // .or(isAnnotatedWith(named("org.springframework.stereotype.Component")))
        // .or(isAnnotatedWith(named("org.springframework.stereotype.Repository")));
    }

    @Override
    public void transform(TypeTransformer typeTransformer) {
        logger.info("TEST transform");
        typeTransformer.applyAdviceToMethod(isPublic().and(isMethod()), this.getClass().getName() + "$SpringBeanAdvice");
    }

    @SuppressWarnings("unused")
    public static class ClassAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static Scope onEnter(@Advice.Origin Method method,
                                    @Advice.Local("otelSpan") Span span,
                                    @Advice.Local("otelScope") Scope scope) {
            // Get a Tracer instance from OpenTelemetry.
            Tracer tracer = GlobalOpenTelemetry.getTracer("class-regex-extended", "semver:1.0.0");

            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            // create a new span
            span = tracer.spanBuilder(methodName).startSpan();
            // Make this new span the current active span.
            scope = span.makeCurrent();

            // Return the Scope instance. This will be used in the exit advice to end the span's scope.
            return scope;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Local("otelSpan") Span span,
                                  @Advice.Local("otelScope") Scope scope,
                                  @Advice.Thrown Throwable throwable) {

            // close the scope to end it
            scope.close();

            if (throwable != null) {
                span.setStatus(StatusCode.ERROR, "Exception thrown in method");
            }
            // End the span. This makes it ready to be exported to the configured exporter (e.g., Jaeger, Zipkin).
            span.end();
        }
    }
}
