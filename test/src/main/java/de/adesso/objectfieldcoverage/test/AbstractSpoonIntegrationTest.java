package de.adesso.objectfieldcoverage.test;

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

/**
 * Abstract base class for abstract Spoon integration tests.
 */
public abstract class AbstractSpoonIntegrationTest {

    /**
     * Uses Java 11 as the compliance level and disables compilation on the internally used Launcher.
     *
     * @param sourceFilePaths
     *          The paths of the Java files which be part of the built model relative to the resources
     *          directory, may be {@code null}.
     *
     * @return
     *          The built model.
     */
    protected CtModel buildModel(String... sourceFilePaths) {
        var launcher = new Launcher();

        getSpoonResources(sourceFilePaths)
                .forEach(launcher::addInputResource);

        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setShouldCompile(false);
        launcher.run();

        return launcher.getModel();
    }

    /**
     *
     * @param model
     *          The model to find the class in, not {@code null}.
     *
     * @param simpleName
     *          The simple name of the {@link CtClass}, not {@code null}.
     *
     * @return
     *          The {@link CtClass} with the given {@code simpleName}.
     *
     * @throws NoSuchElementException
     *          When no {@link CtClass} with a matching name was found in the given {@code model}.
     */
    protected static CtClass<?> findClassWithSimpleName(CtModel model, String simpleName) {
        return model.getElements(new TypeFilter<CtClass<?>>(CtClass.class)).stream()
                .filter(clazz -> simpleName.equals(clazz.getSimpleName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     *
     * @param model
     *          The model to find the interface in, not {@code null}.
     *
     * @param simpleName
     *          The simple name of the {@link CtInterface}, not {@code null}.
     *
     * @return
     *          The {@link CtInterface} with the given {@code simpleName}.
     *
     * @throws NoSuchElementException
     *          When no {@link CtInterface} with a matching name was found in the given {@code model}.
     */
    protected static CtInterface<?> findInterfaceWithSimpleName(CtModel model, String simpleName) {
        return model.getElements(new TypeFilter<CtInterface<?>>(CtInterface.class)).stream()
                .filter(clazz -> simpleName.equals(clazz.getSimpleName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     *
     * @param type
     *          The type to find the method in, not {@code null}.
     *
     * @param simpleName
     *          The simple name of the {@link CtMethod}, not {@code null}.
     *
     * @return
     *          The {@link CtMethod} with the given {@code simpleName}.
     *
     * @throws NoSuchElementException
     *          When no {@link CtMethod} with a matching name was found in the given {@code type}.
     */
    protected static CtMethod<?> findMethodWithSimpleName(CtType<?> type, String simpleName) {
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
