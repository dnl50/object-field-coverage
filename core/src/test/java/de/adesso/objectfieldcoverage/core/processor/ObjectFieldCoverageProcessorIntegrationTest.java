package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.handler.JUnitAssertionInvocationHandler;
import de.adesso.objectfieldcoverage.core.util.ClasspathUtils;
import org.junit.jupiter.api.Test;
import spoon.MavenLauncher;
import spoon.support.QueueProcessingManager;

import java.util.List;

class ObjectFieldCoverageProcessorIntegrationTest {

    @Test
    void process() {
        var launcher = new MavenLauncher("C:\\Users\\Daniel\\Documents\\JavaProjects\\commons-lang", MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
        var launcherEnv = launcher.getEnvironment();
        launcherEnv.setComplianceLevel(11);
        launcherEnv.setCommentEnabled(false);
        launcherEnv.setShouldCompile(false);
        launcher.run();

        var factory = launcher.getFactory();
        var processingManager = new QueueProcessingManager(factory);

        var targetExecutableFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(TargetExecutableFinder.class);
        var fieldFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(AccessibilityAwareFieldFinder.class);
        var testMethodFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(TestMethodFinder.class);
        var equalsMethodAnalyzers = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(EqualsMethodAnalyzer.class);
        var invocationThrowableAnalyzers = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(InvocationThrowableAnalyzer.class);
        var invocationResultTracker = new InvocationResultTracker();

        var invocationHandlers = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(JUnitAssertionInvocationHandler.class);
        var junitAssertionFinder = new JUnitAssertionFinder(invocationHandlers);

        var processor = new ObjectFieldCoverageProcessor(targetExecutableFinders, fieldFinders, testMethodFinders,
                List.of(junitAssertionFinder), equalsMethodAnalyzers,invocationThrowableAnalyzers, invocationResultTracker);
        processor.getSettings()
                .setOnlyIncludeAnnotated(true);

        processingManager.addProcessor(processor);
        processingManager.process(factory.Class().getAll());
    }

}
