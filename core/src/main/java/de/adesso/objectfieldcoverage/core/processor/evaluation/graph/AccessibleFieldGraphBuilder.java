package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.core.finder.AggregatingAccessibilityAwareFieldFinder;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for building a {@link AccessibleFieldGraph} for a pair of (<i>accessing type</i>, <i>accessed type</i>).
 * The <i>accessing</i> type is seen as constant, so a single instance of {@code this} class may be used
 * to build multiple graphs for <i>accessed</i> types. Each graph built by this util class uses as few as
 * possible {@link AccessibleFieldGraphNode node}s by reusing a node when it is the representation of the
 * same {@link AccessibleField}.
 *
 * @see AccessibilityAwareFieldFinder
 */
@Slf4j
public class AccessibleFieldGraphBuilder {

    /**
     * The {@link AccessibilityAwareFieldFinder}s used to build the individual graph nodes
     * with. Each graph node represents a unique {@link AccessibleField}.
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

    /**
     * Static entrypoint method for building a {@link AccessibleFieldGraph}.
     *
     * @param fieldFinders
     *          The {@link AccessibilityAwareFieldFinder}s which are used to find accessible fields
     *          in the given {@code clazzContainingFieldsToAccess} and all transitively reachable fields,
     *          not {@code null}.
     *
     * @param accessingType
     *          The {@link CtType} which wants to access {@link CtField}s inside the {@code clazzContainingFieldsToAccess}
     *          and all transitively reachable fields, not {@code null}.
     *
     * @param clazzContainingFieldsToAccess
     *          The {@link CtType} to start the graph building process at, not {@code null}.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    public static AccessibleFieldGraph buildGraph(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders,
                                                  CtType<?> accessingType,
                                                  CtType<?> clazzContainingFieldsToAccess) {
        Objects.requireNonNull(fieldFinders, "The AccessibilityAwareFieldFinder collection cannot be null!");
        Objects.requireNonNull(accessingType, "The CtType for which the graph should be built cannot be null!");
        Objects.requireNonNull(clazzContainingFieldsToAccess, "The CtType to start the built process at cannot be null!");

        return new AccessibleFieldGraphBuilder(fieldFinders, accessingType)
                .buildGraphInternal(clazzContainingFieldsToAccess);
    }

    /**
     * <b>Note:</b> Declared private since the entry point for this class is the static
     * {@link #buildGraph(Collection, CtType, CtType)} method.
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
     *          The {@link CtType} which will be the first type to analyze for accessible fields,
     *          not {@code null}.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    private AccessibleFieldGraph buildGraphInternal(CtType<?> startingPoint) {
        log.info("Starting graph build process (starting type: '{}', accessing type: '{}')!",
                startingPoint.getQualifiedName(), accessingType.getQualifiedName());

        var isFirstLoop = true;
        var processedFieldDeclaringTypes = new HashSet<CtType<?>>();
        var fieldDeclaringTypeProcessingQueue = new LinkedList<CtType<?>>();
        fieldDeclaringTypeProcessingQueue.add(startingPoint);

        var rootNodes = new HashSet<AccessibleFieldGraphNode>();

        do {
            var currentlyProcessedType = fieldDeclaringTypeProcessingQueue.removeFirst();
            processedFieldDeclaringTypes.add(currentlyProcessedType);

            log.info("Looking for new fields in '{}' (accessing type: '{}')...",
                    currentlyProcessedType.getQualifiedName(), accessingType.getQualifiedName());

            var accessibleFieldsInProcessedType = this.findAccessibleFields(currentlyProcessedType);

            log.info("Found {} accessible fields in '{}'!",
                    currentlyProcessedType.getQualifiedName(), accessibleFieldsInProcessedType.size());

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

        // set children nodes in each created node at the end of the process so no
        // update is required in the meantime
        processedFieldDeclaringTypes.forEach(processedFieldDeclaringType -> {
            var existingNodesForCurrentTypeRef = typeRefToNodesMap.getOrDefault(processedFieldDeclaringType.getReference(), Set.of());
            var childNodesForCurrentTypeRef = typeToChildNodesMap.getOrDefault(processedFieldDeclaringType, Set.of());

            existingNodesForCurrentTypeRef.forEach(node -> node.addChildren(childNodesForCurrentTypeRef));
        });

        log.info("Finished graph build process (starting type: '{}', accessing type: '{}')! The resulting tree has" +
                "{} root nodes and {} nodes in total!", startingPoint.getQualifiedName(), accessingType.getQualifiedName(),
                rootNodes.size(), typeRefToNodesMap.values().stream().mapToInt(Set::size).sum());

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
     *          An <b>unmodifiable</b> set containing exactly one {@link AccessibleField} element for each accessible
     *          {@link CtField}.
     *
     * @see #getExistingOrAddNew(AccessibleField)
     */
    private Set<AccessibleField<?>> findAccessibleFields(CtType<?> typeContainingFieldsToAccess) {
        var accessibleFields = new AggregatingAccessibilityAwareFieldFinder(fieldFinders)
                .findAccessibleFields(accessingType, typeContainingFieldsToAccess);

        return Set.copyOf(accessibleFields);
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
