package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.core.finder.AccessibilityAwareFieldFinderChain;
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
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * The accessing type to build the {@link AccessibleFieldGraph} for.
     */
    private final CtType<?> accessingType;

    /**
     * A map which maps a {@link CtTypeReference} to a set of {@link AccessibleFieldGraphNode child nodes} which
     * have been discovered to be accessible from the {@link #accessingType}. Contains cached results
     * to increase performance. Once a {@link CtTypeReference} is added to this map the corresponding
     * set does not need to be modified.
     */
    private final Map<CtTypeReference<?>, Set<AccessibleFieldGraphNode>> typeRefToChildNodesMap;

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
     *          The type which accesses fields to build the graph for, not {@code null}.
     */
    public AccessibleFieldGraphBuilder(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders,
                                       CtType<?> accessingType) {
        Objects.requireNonNull(fieldFinders, "The AccessibilityAwareFieldFinder collection cannot be null!");
        Objects.requireNonNull(accessingType, "The CtType for which the graph should be built cannot be null!");

        this.fieldFinders = List.copyOf(fieldFinders);
        this.accessingType = accessingType;

        this.typeRefToChildNodesMap = new HashMap<>();
        this.typeRefToNodesMap = new HashMap<>();
    }

    /**
     * @param typeRefContainingFieldsToAccess
     *          The {@link CtTypeReference} to start the graph building process at, not {@code null}.
     *
     * @param fieldFilter
     *          A function mapping a ({@link AccessibleField}, origin {@link CtTypeReference}) pair to a boolean value indicating
     *          whether the {@link AccessibleField} should be included in the graph, not {@code null}. Useful when a graph
     *          should be built which conforms to an additional precondition.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    public AccessibleFieldGraph buildGraph(CtTypeReference<?> typeRefContainingFieldsToAccess,
                                           BiPredicate<AccessibleField<?>, CtTypeReference<?>> fieldFilter) {
        Objects.requireNonNull(typeRefContainingFieldsToAccess, "The CtTypeReference to start the built process at cannot be null!");
        Objects.requireNonNull(fieldFilter, "The filter predicate cannot be null!");

        return buildGraphInternal(typeRefContainingFieldsToAccess, fieldFilter);
    }

    /**
     * Static entrypoint method for building an {@link AccessibleFieldGraph} in which <b>every</b> field is included.
     *
     * @param typeRefContainingFieldsToAccess
     *          The {@link CtTypeReference} to start the graph building process at, not {@code null}.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     *
     * @see #buildGraph(CtTypeReference, BiPredicate)
     */
    public AccessibleFieldGraph buildGraph(CtTypeReference<?> typeRefContainingFieldsToAccess) {
        return buildGraph(typeRefContainingFieldsToAccess, (field, originType) -> true);
    }

    /**
     * Pseudo fields are primitive type fields by definition, so no further analysis is performed when a
     * pseudo field is reached.
     *
     * @param startingPoint
     *          The {@link CtTypeReference} which will be the first reference of a type to analyze for accessible fields,
     *          not {@code null}.
     *
     * @param fieldFilter
     *          A function mapping a ({@link AccessibleField}, origin {@link CtTypeReference}) pair to a boolean value indicating
     *          whether the {@link AccessibleField} should be included in the graph, not {@code null}. Useful when a graph
     *          should be built which conforms to an additional precondition.
     *
     * @return
     *          The resulting {@link AccessibleFieldGraph}.
     */
    private AccessibleFieldGraph buildGraphInternal(CtTypeReference<?> startingPoint, BiPredicate<AccessibleField<?>, CtTypeReference<?>> fieldFilter) {
        log.info("Starting graph build process (starting type: '{}', accessing type: '{}')!",
                startingPoint.getQualifiedName(), accessingType.getQualifiedName());

        var isFirstLoop = true;
        var processedFieldDeclaringTypes = new HashSet<CtTypeReference<?>>();
        var fieldDeclaringTypeProcessingQueue = new LinkedList<CtTypeReference<?>>();
        fieldDeclaringTypeProcessingQueue.add(startingPoint);

        var rootNodes = new HashSet<AccessibleFieldGraphNode>();

        do {
            var currentlyProcessedTypeRef = fieldDeclaringTypeProcessingQueue.removeFirst();
            processedFieldDeclaringTypes.add(currentlyProcessedTypeRef);

            log.info("Looking for new fields in '{}' (accessing type: '{}')...",
                    currentlyProcessedTypeRef.getQualifiedName(), accessingType.getQualifiedName());

            var accessibleFieldsInProcessedType = this.findAccessibleFields(currentlyProcessedTypeRef, fieldFilter);

            if(accessibleFieldsInProcessedType.isEmpty() && isFirstLoop) {
                log.info("No accessible fields in '{}' found in first loop! Breaking loop...",
                        currentlyProcessedTypeRef.getQualifiedName());
                break;
            }

            log.info("Found {} accessible fields in '{}'!",
                    accessibleFieldsInProcessedType.size(), currentlyProcessedTypeRef.getQualifiedName());

            var newlyCreatedNodes = this.createNewNodes(accessibleFieldsInProcessedType);

            if(isFirstLoop) {
                rootNodes.addAll(newlyCreatedNodes);
            }

            typeRefToChildNodesMap.put(currentlyProcessedTypeRef, newlyCreatedNodes);

            accessibleFieldsInProcessedType.stream()
                    .filter(Predicate.not(AccessibleField::isPseudo))
                    .map(AccessibleField::getActualField)
                    .map(CtField::getType)
                    .map(CtTypeReference::getTypeErasure)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(Predicate.not(processedFieldDeclaringTypes::contains))
                    .forEach(fieldDeclaringTypeProcessingQueue::add);

            isFirstLoop = false;
        } while(!fieldDeclaringTypeProcessingQueue.isEmpty());

        // set children nodes in each created node at the end of the process so no
        // update is required in the meantime. pseudo fields do not have child nodes by definition
        processedFieldDeclaringTypes.forEach(processedFieldDeclaringType -> {
            var existingNodesForCurrentTypeRef = typeRefToNodesMap.getOrDefault(processedFieldDeclaringType, Set.of());
            var childNodesForCurrentTypeRef = typeRefToChildNodesMap.getOrDefault(processedFieldDeclaringType, Set.of());

            existingNodesForCurrentTypeRef.stream()
                    .filter(Predicate.not(AccessibleFieldGraphNode::isPseudoFieldNode))
                    .forEach(node -> node.addChildren(childNodesForCurrentTypeRef));
        });

        log.info("Finished graph build process (starting type: '{}', accessing type: '{}')! The resulting tree has " +
                        "{} root node(s) and {} node(s) in total!", startingPoint.getQualifiedName(), accessingType.getQualifiedName(),
                rootNodes.size(), typeRefToNodesMap.values().stream().mapToInt(Set::size).sum());

        return new AccessibleFieldGraph(rootNodes, startingPoint, accessingType.getReference());
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
    private Set<AccessibleField<?>> findAccessibleFields(CtTypeReference<?> typeContainingFieldsToAccess,
                                                         BiPredicate<AccessibleField<?>, CtTypeReference<?>> fieldFilter) {
        return new AccessibilityAwareFieldFinderChain(fieldFinders)
                .findAccessibleFields(accessingType, typeContainingFieldsToAccess).stream()
                .filter(accessibleField -> fieldFilter.test(accessibleField, typeContainingFieldsToAccess))
                .collect(Collectors.toSet());
    }

}
