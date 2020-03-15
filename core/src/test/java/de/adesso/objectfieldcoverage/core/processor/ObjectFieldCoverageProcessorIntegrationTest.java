package de.adesso.objectfieldcoverage.core.processor;

import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.support.QueueProcessingManager;

import java.io.File;

class ObjectFieldCoverageProcessorIntegrationTest {

    @Test
    void process() throws Exception {
        var launcher = new Launcher();
        launcher.addInputResource(asSpoonResource("/processor/Melon.java"));
        launcher.addInputResource(asSpoonResource("/processor/MelonTest.java"));
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setAutoImports(true);
        launcher.run();

        var factory = launcher.getFactory();
        var processingManager = new QueueProcessingManager(factory);
        var processor = new ObjectFieldCoverageProcessor();
        processingManager.addProcessor(processor);
        processingManager.process(factory.Class().getAll());
    }

    private SpoonResource asSpoonResource(String cpRelativePath) throws Exception {
        var resourceFile = new File(getClass().getResource(cpRelativePath).toURI());

        return SpoonResourceHelper.createResource(resourceFile);
    }

}
