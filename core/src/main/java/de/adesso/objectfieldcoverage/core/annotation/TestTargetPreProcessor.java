package de.adesso.objectfieldcoverage.core.annotation;

import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtMethod;

import java.util.Objects;

@Slf4j
public class TestTargetPreProcessor {

    public CtMethod<?> addTestTargetAnnotation(CtMethod<?> testMethod) {
        if(isAlreadyAnnotated(testMethod)) {
            log.debug("Test target of method '{}#{}' already specified! Not adding any more annotation!",
                    testMethod.getDeclaringType().getQualifiedName(),
                    testMethod.getSimpleName());

            return testMethod;
        }

        log.error("TestTargetPreProcessor not implemented yet!");
        return testMethod;
    }

    private boolean isAlreadyAnnotated(CtMethod<?> testMethod) {
        return Objects.nonNull(testMethod.getAnnotation(TestTarget.class));
    }

}
