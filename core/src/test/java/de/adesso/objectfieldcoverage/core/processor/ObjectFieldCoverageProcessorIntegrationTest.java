package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.core.util.ClasspathUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import spoon.MavenLauncher;
import spoon.support.QueueProcessingManager;

class ObjectFieldCoverageProcessorIntegrationTest {

    @Test
    @Disabled
    void process() throws Exception {
        var launcher = new MavenLauncher("C:\\Users\\Daniel\\Documents\\JavaProjects\\commons-lang", MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setAutoImports(true);
        launcher.run();

        var factory = launcher.getFactory();
        var processingManager = new QueueProcessingManager(factory);

        var targetExecutableFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(TargetExecutableFinder.class);
        var fieldFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(AccessibilityAwareFieldFinder.class);
        var testMethodFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(TestMethodFinder.class);
        var assertionFinders = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(AssertionFinder.class);
        var equalsMethodAnalyzers = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(EqualsMethodAnalyzer.class);
        var invocationThrowableAnalyzers = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(InvocationThrowableAnalyzer.class);

        var processor = new ObjectFieldCoverageProcessor(targetExecutableFinders, fieldFinders, testMethodFinders,
                assertionFinders, equalsMethodAnalyzers, invocationThrowableAnalyzers);
        processingManager.addProcessor(processor);
        processingManager.process(factory.Class().getAll());
    }

}
