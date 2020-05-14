package de.adesso.objectfieldcoverage.core.graph.util;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.core.graph.AccessibleFieldGraphNode;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//TODO: JavaDoc

@Slf4j
public class AccessibleFieldGraphBuilder {

    /**
     * The {@link AccessibilityAwareFieldFinder}s used to build the individual graph nodes
     * with.
     */
    private final Set<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * The type to build the {@link AccessibleFieldGraph} for.
     */
    private final CtType<?> accessingType;

    /**
     * A map which maps a {@link CtField} to its corresponding {@link AccessibleField} instance.
     * Used to prevent duplicate {@link AccessibleField} instances to be created for the same {@link CtField},
     * which might occur when different subclasses with a common field, which is declared in a superclass,
     * are used as a type of a field {@link AccessibilityAwareFieldFinder#findAccessibleFields(CtType, CtType)
     * which can be accessed} through a method/direct-field-access chain.
     */
    private final Map<CtField<?>, AccessibleField<?>> fieldToAccessibleFieldMap;

    /**
     * A map which maps a {@link CtType} to a set of {@link AccessibleFieldGraphNode child nodes} which
     * have been discovered to be accessible from the {@link #accessingType}. Contains cached results
     * to increase performance. Once a {@link CtType} is added to this map the corresponding
     * set does not need to be modified.
     */
    private final Map<CtType<?>, Set<AccessibleFieldGraphNode>> typeToChildNodesMap;

    /**
     * A map which maps the {@link CtTypeReference} of a {@link CtField} to the {@link AccessibleFieldGraphNode nodes}
     * which were created for {@link CtField}s with the same {@link CtType}. Used to add newly discovered child nodes
     * to all affected nodes at once. Uses the {@link CtTypeReference} to support {@link AccessibleFieldGraphNode nodes}
     * which have been generated for types of which the {@link CtType} is not available.
     */
    private final Map<CtTypeReference<?>, Set<AccessibleFieldGraphNode>> typeRefToNodesMap;

    public static AccessibleFieldGraph buildGraph(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders,
                                                  CtType<?> accessingType,
                                                  CtClass<?> clazzContainingFieldsToAccess) {
        Objects.requireNonNull(fieldFinders, "The AccessibilityAwareFieldFinder collection cannot be null!");
        Objects.requireNonNull(accessingType, "The CtType for which the graph should be bulit cannot be null!");
        Objects.requireNonNull(clazzContainingFieldsToAccess, "The CtType to start the built process at cannot be null!");

        return new AccessibleFieldGraphBuilder(fieldFinders, accessingType)
                .buildGraphInternal(clazzContainingFieldsToAccess);
    }

    /**
     * <b>Note:</b> Declared private since the entry point for this class is the static
     * {@link #buildGraph(Collection, CtType, CtClass)} method.
     *
     * @param fieldFinders
     *          The {@link AccessibilityAwareFieldFinder}s which are used to build the individual graph nodes with,
     *          not {@code null}.
     *
     * @param accessingType
     *          The type to build the graph for, not {@code null}.
     */
    private AccessibleFieldGraphBuilder(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders,
                                        CtType<?> accessingType) {
        this.fieldFinders = Set.copyOf(fieldFinders);
        this.accessingType = accessingType;

        this.fieldToAccessibleFieldMap = new HashMap<>();
        this.typeToChildNodesMap = new HashMap<>();
        this.typeRefToNodesMap = new HashMap<>();
    }

    /**
     *
     * @param startingPoint
     *          The {@link CtType} which will be the first
     *
     * @return
     */
    private AccessibleFieldGraph buildGraphInternal(CtType<?> startingPoint) {
        var isFirstLoop = true;
        var processedFieldDeclaringTypes = new HashSet<CtType<?>>();
        var fieldDeclaringTypeProcessingQueue = new LinkedList<CtType<?>>();
        fieldDeclaringTypeProcessingQueue.add(startingPoint);

        var rootNodes = new HashSet<AccessibleFieldGraphNode>();

        do {
            var currentlyProcessedType = fieldDeclaringTypeProcessingQueue.removeFirst();
            processedFieldDeclaringTypes.add(currentlyProcessedType);

            var accessibleFieldsInProcessedType = this.findAccessibleFields(currentlyProcessedType);
            var newlyCreatedNodes = this.createNewNodes(accessibleFieldsInProcessedType);

            if(isFirstLoop) {
                rootNodes.addAll(newlyCreatedNodes);
            }

            typeToChildNodesMap.put(currentlyProcessedType, newlyCreatedNodes);

            accessibleFieldsInProcessedType.stream()
                    .map(AccessibleField::getActualField)
                    .map(CtField::getType)
                    .map(CtTypeReference::getDeclaration)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(Predicate.not(processedFieldDeclaringTypes::contains))
                    .forEach(fieldDeclaringTypeProcessingQueue::add);

            isFirstLoop = false;
        } while(!fieldDeclaringTypeProcessingQueue.isEmpty());

        // set children nodes
        processedFieldDeclaringTypes.forEach(processedFieldDeclaringType -> {
            var existingNodesForCurrentTypeRef = typeRefToNodesMap.getOrDefault(processedFieldDeclaringType.getReference(), Set.of());
            var childNodesForCurrentTypeRef = typeToChildNodesMap.getOrDefault(processedFieldDeclaringType, Set.of());

            existingNodesForCurrentTypeRef.forEach(node -> node.addChildren(childNodesForCurrentTypeRef));
        });

        return new AccessibleFieldGraph(rootNodes);
    }

    /**
     * Creates a new {@link AccessibleFieldGraphNode} for a set of {@link AccessibleField}s. The newly created
     * nodes will be appended to the corresponding set contained in the {@link #typeRefToNodesMap}.
     *
     * @param accessibleFields
     *          The set of {@link AccessibleField}s for which a new {@link AccessibleFieldGraphNode}
     *          should be created for every element, not {@code null}. The {@link AccessibleField#getActualField()
     *          actual field} of every element must be unique. Duplicates will be created otherwise.
     *
     * @return
     *          The newly created {@link AccessibleFieldGraphNode}s, one for each {@link AccessibleField}
     *          contained in the given {@code accessibleFields} set.
     */
    private Set<AccessibleFieldGraphNode> createNewNodes(Set<AccessibleField<?>> accessibleFields) {
        return accessibleFields.stream()
                .map(accessibleField -> {
                    var actualFieldTypeReference = accessibleField.getActualField().getType();
                    var nodeToAdd = AccessibleFieldGraphNode.of(accessibleField);

                    if(!typeRefToNodesMap.containsKey(actualFieldTypeReference)) {
                        typeRefToNodesMap.put(actualFieldTypeReference, new HashSet<>());
                    }

                    typeRefToNodesMap.get(actualFieldTypeReference)
                            .add(nodeToAdd);

                    return nodeToAdd;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Combines the result of every {@link AccessibilityAwareFieldFinder} into a single result
     * using the {@link AccessibleField#uniteAll(Collection)} method.
     * <p/>
     * All <i>new</i> fields are added to the {@link #fieldToAccessibleFieldMap}.
     *
     * @param typeContainingFieldsToAccess
     *          The {@link CtType} which contains fields which the {@link #accessingType} wants to
     *          access, not {@code null}.
     *
     * @return
     *          A set containing exactly one {@link AccessibleField} element for each accessible
     *          {@link CtField}.
     *
     * @see #getExistingOrAddNew(AccessibleField)
     */
    private Set<AccessibleField<?>> findAccessibleFields(CtType<?> typeContainingFieldsToAccess) {
        var allFoundAccessibleFields = fieldFinders.stream()
                .map(fieldFinder -> fieldFinder.findAccessibleFields(accessingType, typeContainingFieldsToAccess))
                .flatMap(Collection::stream)
                .map(this::getExistingOrAddNew)
                .collect(Collectors.toSet());

        return AccessibleField.uniteAll(allFoundAccessibleFields);
    }

    /**
     * Adds the given {@link AccessibleField} instance to the {@link #fieldToAccessibleFieldMap}
     * in case the {@link CtField} which the {@link AccessibleField} refers to is not already
     * associated with a {@link AccessibleField} instance.
     *
     * @param accessibleField
     *          The {@link AccessibleField} instance to add to the {@link #fieldToAccessibleFieldMap}
     *          map, not {@code null}.
     *
     * @return
     *          The given {@code accessibleField} in case the no entry with the same {@link CtField}
     *          key is present. The existing {@link AccessibleField} instance which
     *          refers to the same {@link CtField} is returned otherwise.
     */
    private AccessibleField<?> getExistingOrAddNew(AccessibleField<?> accessibleField) {
        var actualField = accessibleField.getActualField();

        return fieldToAccessibleFieldMap.computeIfAbsent(actualField, k -> accessibleField);
    }

}
