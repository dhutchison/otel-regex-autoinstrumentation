package com.devwithimagination.otel.extension;

import java.util.List;
import java.util.logging.Logger;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import static java.util.Collections.singletonList;

@AutoService(InstrumentationModule.class)
public final class ClassRegexInstrumentationModule extends InstrumentationModule {
    private static Logger logger = Logger.getLogger(ClassRegexInstrumentationModule.class.getName());

    public ClassRegexInstrumentationModule() {
        super("class-regex-extended", "classregexextended");
        logger.info("Adding class-regex-extension");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<String> getAdditionalHelperClassNames() {
        return List.of(ClassRegexInstrumentation.class.getName(), "io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new ClassRegexInstrumentation());
    }
}
