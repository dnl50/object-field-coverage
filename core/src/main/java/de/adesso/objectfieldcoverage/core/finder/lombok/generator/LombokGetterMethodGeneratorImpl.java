package de.adesso.objectfieldcoverage.core.finder.lombok.generator;

import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.MethodFactory;

import java.util.*;

/**
 * {@link LombokGetterMethodGenerator} implementation using Spoon's {@link MethodFactory} to generate
 * getter methods. See <a href="https://projectlombok.org/features/GetterSetter">Lombok's Documentation</a>
 * for more information. This implementation does not take settings made in Lombok's properties file
 * into account, so the standard Java-Beans getter naming convention.
 */
@Slf4j
public class LombokGetterMethodGeneratorImpl implements LombokGetterMethodGenerator {

    /**
     * @inheritDoc
     *
     * @implNote The access levels {@link AccessLevel#PACKAGE} and {@link AccessLevel#MODULE} are handled
     * equally.
     *
     * @throws IllegalArgumentException When the givne {@code accessLevel} is neither
     * {@link AccessLevel#PUBLIC}, {@link AccessLevel#PROTECTED}, {@link AccessLevel#PRIVATE}
     * {@link AccessLevel#PACKAGE} nor {@link AccessLevel#MODULE}.
     */
    @Override
    public boolean isGetterMethodWithDifferentAccessModifierPresent(CtField<?> field, AccessLevel accessLevel) {
        Objects.requireNonNull(field, "field cannot be null!");
        Objects.requireNonNull(accessLevel, "accessLevel cannot be null!");

        var existingGetterMethodOptional = this.getExistingGetterMethodOnDeclaringType(field);
        if(existingGetterMethodOptional.isEmpty()) {
            log.debug("No lombok getter method for field '{}' declared on type '{}' present!",
                    field.getSimpleName(), field.getDeclaringType().getQualifiedName());
            return false;
        }

        log.debug("Lombok getter method for field '{}' declared on type '{}' present!",
                field.getSimpleName(), field.getDeclaringType().getQualifiedName());

        var existingGetterMethod = existingGetterMethodOptional.get();
        var isPublic = existingGetterMethod.isPublic();
        var isProtected = existingGetterMethod.isProtected();
        var isPrivate = existingGetterMethod.isPrivate();
        var isPackagePrivate = !isPublic && !isProtected && !isPrivate;

        switch (accessLevel) {
            case PUBLIC:
                return !isPublic;
            case PROTECTED:
                return !isProtected;
            case PRIVATE:
                return !isPrivate;
            case PACKAGE: case MODULE:
                return !isPackagePrivate;
            default:
                throw new IllegalArgumentException(String.format("Unknown access level '%s' specified!", accessLevel.name()));
        }
    }

    /**
     * @inheritDoc
     *
     * @implNote Uses Spoons {@link MethodFactory} to generate a getter method. Always generates a
     * instance method getter (non-static getter).
     */
    @Override
    public <T> CtMethod<T> generateGetterMethod(CtField<T> field, AccessLevel accessLevel) {
        Objects.requireNonNull(field, "field cannot be null!");
        Objects.requireNonNull(field, "accessLevel cannot be null!");

        if(AccessLevel.NONE == accessLevel) {
            throw new IllegalArgumentException("'NONE' is not a valid access level");
        }

        var existingGetterMethodOptional = this.getExistingGetterMethodOnDeclaringType(field);
        if(existingGetterMethodOptional.isPresent()) {
            log.debug("Lombok getter method for field '{}' declared on type '{}' present! No new method will be generated!",
                    field.getSimpleName(), field.getDeclaringType().getQualifiedName());
            return existingGetterMethodOptional.get();
        }

        log.debug("No lombok getter method for field '{}' declared on type '{}' present! New getter method " +
                        "with access level '{}' will be generated!", field.getSimpleName(),
                field.getDeclaringType().getQualifiedName(), accessLevel);

        var methodFactory = new MethodFactory(field.getFactory());
        var declaringType = field.getDeclaringType();
        var getterMethodName = this.lombokGetterMethodName(field);

        var accessModifiers = new HashSet<ModifierKind>(1);
        switch (accessLevel) {
            case PUBLIC:
                accessModifiers.add(ModifierKind.PUBLIC);
                break;
            case PROTECTED:
                accessModifiers.add(ModifierKind.PROTECTED);
                break;
            case PRIVATE:
                accessModifiers.add(ModifierKind.PRIVATE);
                break;
        }

        return methodFactory.create(declaringType, accessModifiers, field.getType(), getterMethodName, List.of(), Set.of());
    }

    /**
     *
     * @param field
     *          The {@link CtField} to get the generated getter method prefix for,
     *          not {@code null}.
     *
     * @return
     *          <i>is</s> in case the given {@code field} is of primitive type {@code boolean}
     *          or reference type {@link Boolean}. <i>get</i> is returned otherwise.
     */
    private String getterMethodPrefix(CtField<?> field) {
        var fieldType = field.getType();
        var fieldTypeName = fieldType.getQualifiedName();
        var isOfBooleanPrimitiveType = (fieldType.isPrimitive() && "boolean".equals(fieldTypeName));
        var isOfBooleanReferenceType = "java.lang.Boolean".equals(fieldTypeName);

        return (isOfBooleanPrimitiveType || isOfBooleanReferenceType) ? "is" : "get";
    }

    /**
     *
     * @param field
     *          The field to get the getter method name for, not {@code null}.
     *
     * @return
     *          The name of the getter method generated by lombok.
     *
     * @see #getterMethodPrefix(CtField)
     */
    private String lombokGetterMethodName(CtField<?> field) {
        var fieldPostFix = StringUtils.capitalize(field.getSimpleName());
        return String.format("%s%s", getterMethodPrefix(field), fieldPostFix);
    }

    /**
     *
     * @param field
     *          The field to get the getter method with the same name as lombok's generated
     *          getter method for, not {@code null}.
     *
     * @param <T>
     *          The type of the field, not {@code null}.
     *
     * @return
     *          An optional containing a method with the same name and return type as
     *          lombok's generated getter method in case it is present on the given {@code field}s
     *          declaring type.
     */
    private <T> Optional<CtMethod<T>> getExistingGetterMethodOnDeclaringType(CtField<T> field) {
        var getterMethodName = lombokGetterMethodName(field);
        var existingGetterMethodOnDeclaringType = field.getDeclaringType()
                .getMethod(field.getType(), getterMethodName);

        return Optional.ofNullable(existingGetterMethodOnDeclaringType);
    }

}
