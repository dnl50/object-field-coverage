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
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for building a {@link AccessibleFieldGraph} for a pair of (<i>accessing type</i>, <i>accessed type</i>).
 * The <i>accessing</i> type is seen as constant, so a single instance of {@code this} class may be used
 * to build multiple graphs for multiple <i>accessed</i> types. Each graph built by this util class uses as few as
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
     * The accessing type to build the {@link AccessibleFieldGraph} for.
     */
    private final CtType<?> accessingType;

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
     *
     * @param fieldFinders
     *          The {@link AccessibilityAwareFieldFinder}s which are used to build the individual graph nodes with,
     *          not {@code null}.
     *
     * @param accessingType
     *          The type to build the graph for, not {@code null}.
     */
    public AccessibleFieldGraphBuilder(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders,
                                       CtType<?> accessingType) {
        Objects.requireNonNull(fieldFinders, "The AccessibilityAwareFieldFinder collection cannot be null!");
        Objects.requireNonNull(accessingType, "The CtType for which the graph should be built cannot be null!");

        this.fieldFinders = Set.copyOf(fieldFinders);
        this.accessingType = accessingType;

        this.typeToChildNodesMap = new HashMap<>();
        this.typeRefToNodesMap = new HashMap<>();
    }

    /**
     * @param typeContainingFieldsToAccess
     *          The {@link CtType} to start the graph building process at, not {@code null}.
     *
     * @param fieldFilter
     *          A function mapping a ({@link AccessibleField}, origin {@link CtType}) pair to a boolean value indicating
     *          whether the {@link AccessibleField} should be included in the graph, not {@code null}. Useful when a graph
     *          should be built which conforms to an additional precondition.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    public AccessibleFieldGraph buildGraph(CtType<?> typeContainingFieldsToAccess,
                                           BiPredicate<AccessibleField<?>, CtType<?>> fieldFilter) {
        Objects.requireNonNull(typeContainingFieldsToAccess, "The CtType to start the built process at cannot be null!");
        Objects.requireNonNull(fieldFilter, "The filter predicate cannot be null!");

        return buildGraphInternal(typeContainingFieldsToAccess, fieldFilter);
    }

    /**
     * Static entrypoint method for building an {@link AccessibleFieldGraph} in which <b>every</b> field is included.
     *
     * @param typeContainingFieldsToAccess
     *          The {@link CtType} to start the graph building process at, not {@code null}.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     *
     * @see #buildGraph(CtType, BiPredicate)
     */
    public AccessibleFieldGraph buildGraph(CtType<?> typeContainingFieldsToAccess) {
        return buildGraph(typeContainingFieldsToAccess, (field, originType) -> true);
    }

    /**
     *
     * @param startingPoint
     *          The {@link CtType} which will be the first type to analyze for accessible fields,
     *          not {@code null}.
     *
     * @param fieldFilter
     *          A function mapping a ({@link AccessibleField}, origin {@link CtType}) pair to a boolean value indicating
     *          whether the {@link AccessibleField} should be included in the graph, not {@code null}. Useful when a graph
     *          should be built which conforms to an additional precondition.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    private AccessibleFieldGraph buildGraphInternal(CtType<?> startingPoint, BiPredicate<AccessibleField<?>, CtType<?>> fieldFilter) {
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

            var accessibleFieldsInProcessedType = this.findAccessibleFields(currentlyProcessedType, fieldFilter);

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

        return new AccessibleFieldGraph(rootNodes, startingPoint.getReference(), accessingType.getReference());
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
     *
     * @param typeContainingFieldsToAccess
     *          The {@link CtType} which contains fields which the {@link #accessingType} wants to
     *          access, not {@code null}.
     *
     * @param fieldFilter
     *          A function mapping a ({@link AccessibleField}, origin {@link CtType}) pair to a boolean value indicating
     *          whether the {@link AccessibleField} should be included in the graph, not {@code null}. Useful when a graph
     *          should be built which conforms to an additional precondition.
     *
     * @return
     *          An <b>unmodifiable</b> set containing the {@link AccessibleField} instances which are accessible
     *          in the given {@code typeContainingFieldsToAccess} and which are not filtered out by the given
     *          {@code fieldFilter}.
     */
    private Set<AccessibleField<?>> findAccessibleFields(CtType<?> typeContainingFieldsToAccess,
                                                         BiPredicate<AccessibleField<?>, CtType<?>> fieldFilter) {
        return new AggregatingAccessibilityAwareFieldFinder(fieldFinders)
                .findAccessibleFields(accessingType, typeContainingFieldsToAccess).stream()
                .filter(accessibleField -> fieldFilter.test(accessibleField, typeContainingFieldsToAccess))
                .collect(Collectors.toSet());
    }


}
