package de.adesso.objectfieldcoverage.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract class used to find <i>accessible</i> fields of a given type. The meaning of <i>accessible</i>
 * depends on the implementation. Transient and static final fields are omitted.
 * <br/>
 * Implements the {@link Chainable} interface since multiple implementations might be used one after another.
 */
@Slf4j
public abstract class AccessibilityAwareFieldFinder implements Chainable<Pair<CtType<?>, CtTypeReference<?>>> {

    /**
     *
     * @param accessingType
     *          The type whose methods could potentially access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is accessible from the given {@code accessingType}.
     *          {@code false} is returned otherwise.
     */
    public abstract boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field);

    /**
     * This method is required since it is important to know through which typed element (e.g. the field
     * itself or a getter method) a given {@link CtField} can be accessed in a given {@link CtType}.
     * </P>
     * <b>Example</b>: Further down the line in the analysis process it must be known that the
     * {@code getX()} method is an alias for the field {@code x}, so an assertion on the the invocation result of the
     * {@code getX()} method inside a test method is counted as an assertion on field {@code x}.
     * <p/>
     * Implementations might throw an unchecked exception when this method is invoked with a {@link CtType}
     * and {@link CtField} for which the {@link #isFieldAccessible(CtType, CtField)} method returns false.
     *
     * @param accessingType
     *          The type whose methods can access the given {@code field}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code accessingType}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          The typed elements which grant access to the given {@code field}. Must contain at least one
     *          element when {@link #isFieldAccessible(CtType, CtField)} returns {@code true}. Implementations
     *          might throw unchecked exceptions when {@link #isFieldAccessible(CtType, CtField)} returns {@code false}.
     */
    public abstract <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field);

    /**
     *
     * @param accessingType
     *          The type whose methods could potentially access the given {@code typeRef}'s
     *          fields, not {@code null}.
     *
     * @param typeRef
     *          The reference of the type to get the accessible fields of, not {@code null}.
     *
     * @return
     *          A list of all fields which are accessible from the given {@code accessingType} combined with the
     *          typed element which grants access to the field. Includes fields which are directly declared in
     *          the given {@code typeRef} or in any super-type. Transient and constant fields are omitted.
     */
    @SuppressWarnings("unchecked")
    public List<AccessibleField<?>> findAccessibleFields(CtType<?> accessingType, CtTypeReference<?> typeRef) {
        Objects.requireNonNull(accessingType, "The accessing type cannot be null!");
        Objects.requireNonNull(typeRef, "The type reference of the type containing fields cannot be null!");

        return findFieldsInType(typeRef).stream()
                .filter(field -> !isTransient(field) && !isCompileTimeConstant(field))
                .filter(field -> this.isFieldAccessible(accessingType, field))
                .map(field -> {
                    var pseudo = isPseudoField(field);
                    var accessGrantingElements = this.findAccessGrantingElements(accessingType, field);

                    @SuppressWarnings("rawtypes")
                    var accessibleField = (AccessibleField<?>) new AccessibleField(field, Set.copyOf(accessGrantingElements), pseudo);
                    return accessibleField;
                })
                .collect(Collectors.toList());
    }

    /**
     * <b>Note:</b> This method may be overridden by implementing classes when they do not rely on fields which
     * are actually part of the given {@link CtTypeReference}.
     *
     * @param typeRef
     *          The reference of the type to get the {@link CtField}s of, not {@code null}.
     *
     * @return
     *          A set containing {@link CtTypeReference#getAllFields() all fields} whose field declaration
     *          is present.
     */
    protected Set<CtField<?>> findFieldsInType(CtTypeReference<?> typeRef) {
        return typeRef.getAllFields().stream()
                .map(fieldRef -> {
                    var fieldDeclaration = fieldRef.getFieldDeclaration();

                    if(fieldDeclaration == null) {
                        log.warn("No field declaration for field '{}' found!", fieldRef.getQualifiedName());
                    }

                    return fieldDeclaration;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param field
     *          The {@link CtField} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is declared transient. {@code false} is returned
     *          otherwise.
     */
    protected boolean isTransient(CtField<?> field) {
        return hasModifiers(field, ModifierKind.TRANSIENT);
    }

    /**
     *
     * @param field
     *          The {@link CtField} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is declared static and final. {@code false} is returned
     *          otherwise.
     */
    protected boolean isCompileTimeConstant(CtField<?> field) {
        return hasModifiers(field, ModifierKind.STATIC, ModifierKind.FINAL);
    }

    /**
     *
     * @param field
     *          The {@link CtField field} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is a pseudo field. {@code false} is returned
     *          otherwise.
     */
    protected boolean isPseudoField(CtField<?> field) {
        return false;
    }

    /**
     * <b>Note:</b> Java modules added in Java 9 are not taken into account.
     *
     * @param accessingType
     *          The {@link CtType} which wants to access the given {@code modifiable}, not {@code null}.
     *
     * @param member
     *          The {@link CtTypeMember} the given {@code accessingType} wants to access, not {@code null}.
     *
     * @return
     *      {@code true}, if the given {@code member} is <i>accessible</i> according to {@code $6.6} of the
     *      Java Language Specification. {@code false} is returned otherwise.
     */
    protected boolean isAccessibleAccordingToJls(CtType<?> accessingType, CtTypeMember member) {
        if(!isDeclaringTypeAccessible(accessingType, member)) {
            return false;
        }

        if(isDeclaredInSameTopLevelType(accessingType, member)) {
            return true;
        }

        var memberAccessModifier = AccessModifier.of(member);

        switch (memberAccessModifier) {
            case PRIVATE:
                // previous if statement would have returned true already
                return false;

            case PUBLIC:
                return true;

            case PACKAGE:
                return isInSamePackageAsDeclaringType(member, accessingType);

            case PROTECTED:
                return isInSamePackageAsDeclaringType(member, accessingType) || isRealSubClassOfDeclaringClass(member, accessingType);

            default:
                throw new IllegalStateException(String.format("Unknown AccessModifier '%s'!", memberAccessModifier));
        }
    }

    /**
     *
     * @param accessingType
     *          The {@link CtType} which wants to access the given {@code typeMember}, not {@code null}.
     *
     * @param typeMember
     *          The {@link CtTypeMember} which the given {@code accessingType} wants to access, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typeMember}'s {@link CtTypeMember#getDeclaringType()}
     *          can be access by the given {@code accessingType} according to {@code $6.6} of the Java Language
     *          Specification (Java SE 11 Edition). {@code false} is returned otherwise.
     */
    private boolean isDeclaringTypeAccessible(CtType<?> accessingType, CtTypeMember typeMember) {
        // same top level type = accessible, no matter the specified access modifier of the member
        if(isDeclaredInSameTopLevelType(accessingType, typeMember)) {
            return true;
        }

        var mostStrictAccessModifier = findMostStrictAccessModifierInDeclaringTypeChain(typeMember);

        switch (mostStrictAccessModifier) {
            case PUBLIC:
                return true;

            case PRIVATE:
                // the first if statement would have returned true already
                return false;

            case PACKAGE: case PROTECTED:
                return isInSamePackageAsDeclaringType(typeMember, accessingType);

            default:
                throw new IllegalStateException(String.format("Unknown AccessModifier '%s'!", mostStrictAccessModifier));
        }
    }

    /**
     * Walks up the declaring type chain of a given {@link CtTypeMember} until either the declaring type
     * is {@code null} or the declaring type declares itself.
     *
     * @param typeMember
     *          The {@link CtTypeMember} to get the most strict {@link AccessModifier} of.
     *
     * @return
     *          The {@link AccessModifier} with the highest strictness of any of the types in the
     *          declaring type chain.
     */
    private AccessModifier findMostStrictAccessModifierInDeclaringTypeChain(CtTypeMember typeMember) {
        var declaringTypes = new ArrayList<CtType<?>>();
        var currentDeclaringType = typeMember.getDeclaringType();

        while(currentDeclaringType != null) {
            declaringTypes.add(currentDeclaringType);

            var nextDeclaringType = currentDeclaringType.getDeclaringType();
            if(nextDeclaringType == null || nextDeclaringType.equals(currentDeclaringType)) {
               break;
            }

            currentDeclaringType = nextDeclaringType;
        }

        return declaringTypes.stream()
                .map(AccessModifier::of)
                .min(Enum::compareTo)
                .orElseThrow(() -> new IllegalStateException("Given CtTypeMember does not have a declaring type!"));
    }

    /**
     *
     * @param member
     *          The first {@link CtTypeMember}, not {@code null}.
     *
     * @param otherMember
     *          The other {@link CtTypeMember}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code member}'s and {@code otherMember}'s {@link CtTypeMember#getTopLevelType()
     *          top level type} are equal. {@code false} is returned otherwise.
     */
    private boolean isDeclaredInSameTopLevelType(CtTypeMember member, CtTypeMember otherMember) {
        var memberTopLevelType = member.getTopLevelType();
        var otherMemberTopLevelType = otherMember.getTopLevelType();

        return memberTopLevelType.equals(otherMemberTopLevelType);
    }

    /**
     * Walks up the super-class chain of the given {@code type} until either
     * the given {@code member}'s declaring class is reached or the super-class
     * is {@code null}.
     *
     * @param member
     *          The member to get the declaring class of, not {@code null}.
     *
     * @param type
     *          The type which contains the methods which could potentially
     *          access the given {@code member}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} is a <i>real</i> subclass of the declaring
     *          class of the given {@code member}. {@code false} is returned otherwise.
     */
    protected boolean isRealSubClassOfDeclaringClass(CtTypeMember member, CtType<?> type) {
        var fieldDeclaringType = member.getDeclaringType();

        var currentSuperClassReferenceType = type.getSuperclass();

        while(currentSuperClassReferenceType != null) {
            var currentSuperClassType = currentSuperClassReferenceType.getTypeDeclaration();
            if(fieldDeclaringType.equals(currentSuperClassType)) {
                return true;
            }

            currentSuperClassReferenceType = currentSuperClassReferenceType.getSuperclass();
        }

        return false;
    }

    /**
     * Compares the packages of the given {@code member}'s declaring type and the
     * given {@code type}.
     *
     * @param member
     *          The member to get the declaring type of, not {@code null}.
     *
     * @param type
     *          The type which wants to access the access the given {@code member}, not {@code null}.
     *
     * @return
     *          {@code true}, if the declaring type of the given {@code member} is in the
     *          same package as the given {@code type}. {@code false} is returned otherwise.
     */
    protected boolean isInSamePackageAsDeclaringType(CtTypeMember member, CtType<?> type) {
        var declaringTypePackage = member.getDeclaringType()
                .getPackage();
        var typePackage = type.getPackage();

        return declaringTypePackage.equals(typePackage);
    }

    /**
     *
     * @param field
     *          The {@link CtField} to check, not {@code null}.
     *
     * @param modifierKind
     *          A {@link ModifierKind} which must be present, not {@code null}.
     *
     * @param additionalModifierKinds
     *          Additional {@link ModifierKind}s which must be present, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field}'s modifiers contain the given {@link ModifierKind}s. {@code false}
     *          is returned otherwise.
     */
    private boolean hasModifiers(CtField<?> field, ModifierKind modifierKind, ModifierKind... additionalModifierKinds) {
        var fieldModifiers = field.getModifiers();

        if(!fieldModifiers.contains(modifierKind)) {
            return false;
        }

        if(additionalModifierKinds != null) {
            for(var additionalModifierKind : additionalModifierKinds) {
                if(!fieldModifiers.contains(additionalModifierKind)) {
                    return false;
                }
            }
        }

        return true;
    }

}
