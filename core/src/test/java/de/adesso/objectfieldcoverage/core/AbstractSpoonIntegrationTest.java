package de.adesso.objectfieldcoverage.core;

import spoon.Launcher;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public abstract class AbstractSpoonIntegrationTest {

    protected CtModel buildModel(String... sourceFilePaths) {
        var launcher = new Launcher();

        getSpoonResources(sourceFilePaths)
                .forEach(launcher::addInputResource);

        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setAutoImports(true);
        launcher.run();

        return launcher.getModel();
    }

    protected CtClass<?> findClassWithSimpleName(CtModel model, String simpleName) {
        return model.getElements(new TypeFilter<CtClass<?>>(CtClass.class)).stream()
                .filter(clazz -> simpleName.equals(clazz.getSimpleName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    protected CtInterface<?> findInterfaceWithSimpleName(CtModel model, String simpleName) {
        return model.getElements(new TypeFilter<CtInterface<?>>(CtInterface.class)).stream()
                .filter(clazz -> simpleName.equals(clazz.getSimpleName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    protected CtMethod<?> findMethodWithSimpleName(CtType<?> type, String simpleName) {
        return type.getMethods().stream()
                .filter(method -> method.getSimpleName().equals(simpleName))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private List<SpoonResource> getSpoonResources(String... sourceFilePaths) {
        return Arrays.stream(sourceFilePaths)
                .map(sourceFilePath -> {
                    try {
                        var sourceFile = new File(getClass().getClassLoader().getResource(sourceFilePath).toURI());
                        return SpoonResourceHelper.createResource(sourceFile);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

}
