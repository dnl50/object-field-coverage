package de.adesso.objectfieldcoverage.core.finder.pseudo.generator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.ClassFactory;

import java.util.Set;

/**
 * {@link PseudoClassGenerator} implementation using spoons {@link spoon.reflect.factory.ClassFactory} to create
 * new classes.
 */
@Slf4j
public class PseudoClassGeneratorImpl implements PseudoClassGenerator {

    /**
     * <b>Note:</b> This implementation does not verify that the class does not already exist!
     *
     * {@inheritDoc}
     */
    @Override
    public CtClass<?> generatePseudoClass(ClassFactory factory, String simpleClassName, String packageQualifiedName) {
        if(StringUtils.isBlank(simpleClassName)) {
            throw new IllegalArgumentException("The simple class name cannot be blank!");
        }

        var fullyQualifiedName = buildFullyQualifiedPseudoClassName(simpleClassName, packageQualifiedName);
        var newlyCreatedClass = factory.create(fullyQualifiedName);
        newlyCreatedClass.setModifiers(Set.of(ModifierKind.PUBLIC));

        log.info("Created new public pseudo class '{}'!", newlyCreatedClass);

        return newlyCreatedClass;
    }

    /**
     *
     * @param simpleClassName
     *          The simple class name, not blank.
     *
     * @param packageQualifiedName
     *          The qualified name of the package in which the class should be generated in. A blank string
     *          indicates the default package. Is not allowed to end with a dot.
     *
     * @return
     *          The fully qualified name of the pseudo class.
     */
    private String buildFullyQualifiedPseudoClassName(String simpleClassName, String packageQualifiedName) {
        var stringBuilder = new StringBuilder();

        var defaultPackage = StringUtils.isBlank(packageQualifiedName);
        if(!defaultPackage) {
            stringBuilder.append(packageQualifiedName.trim())
                    .append('.');
        }

        stringBuilder.append(simpleClassName.trim())
                .append(PSEUDO_CLASS_SUFFIX);

        return stringBuilder.toString();
    }

}
