package de.adesso.objectfieldcoverage.api.evaluation.graph;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

/**
 * Immutable graph representation of the fields which can be accessed from a given {@link spoon.reflect.declaration.CtClass}.
 * The graph can contain cycles (and is therefore not a tree) when a field can be reached by method/field-access
 * chaining which has the same type as the starting field.
 * <p/>
 * Example:
 * <pre>
 *     public class Person {
 *
 *         private Person mother;
 *
 *         private Person father;
 *
 *     }
 * </pre>
 * The resulting graph would have multiple cycles:
 * <ul>
 *     <li>{@code mother} &rarr; {@code mother}</li>
 *     <li>{@code mother} &rarr; {@code father} &rarr; {@code mother}</li>
 *     <li>{@code father} &rarr; {@code father}</li>
 *     <li>{@code father} &rarr; {@code mother} &rarr; {@code father}</li>
 * </ul>
 *
 * @implNote The iteration order is unspecified since it uses a {@link Set} iterator internally.
 *
 * @see de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder
 * @see AccessibleFieldGraphNode
 */
@Getter
@ToString
@EqualsAndHashCode
public class AccessibleFieldGraph implements Iterable<AccessibleFieldGraphNode> {

    /**
     * These are the nodes from which the graph was built.
     */
    private final Set<AccessibleFieldGraphNode> rootNodes;

    /**
     * The {@link CtTypeReference} of the type which {@code this} graph belongs to.
     */
    private final CtTypeReference<?> describedTypeRef;

    /**
     * The {@link CtTypeReference} of the type which accesses the {@link #getDescribedTypeRef()}'s fields.
     */
    private final CtTypeReference<?> accessingTypeRef;

    /**
     * Lazily initialized field containing the result of the {@link #getTransitiveReachabilityPaths()}
     * method.
     */
    @Getter(AccessLevel.NONE)
    private Set<Path> transitiveReachabilityPaths;

    /**
     *
     * @param rootNodes
     *          The root nodes of the newly constructed graph, not {@code null}.
     *
     * @param describedTypeRef
     *          The {@link CtTypeReference} of the type which the graph belongs to, not {@code null}.
     *
     * @param accessingTypeRef
     *          The {@link CtTypeReference} of the type which accesses the {@code describedType}'s fields,
     *          not {@code null}.
     */
    public AccessibleFieldGraph(Collection<AccessibleFieldGraphNode> rootNodes, CtTypeReference<?> describedTypeRef,
                                CtTypeReference<?> accessingTypeRef) {
        this.describedTypeRef = Objects.requireNonNull(describedTypeRef, "The CtTypeReference of the described type cannot be null!");
        this.accessingTypeRef = Objects.requireNonNull(accessingTypeRef, "The CtTypeReference of the accessing type cannot be null!");
        this.rootNodes = Set.copyOf(rootNodes);
    }

    /**
     *
     * @param rootNodes
     *          The root nodes of the newly constructed graph, not {@code null}.
     *
     * @param describedTypeRef
     *          The {@link CtTypeReference} of the type which the graph belongs to.
     *
     * @param accessingTypeRef
     *          The {@link CtTypeReference} of the type which accesses the {@code describedType}'s fields,
     *          not {@code null}.
     *
     * @see #AccessibleFieldGraph(Collection, CtTypeReference, CtTypeReference)
     */
    public AccessibleFieldGraph(CtTypeReference<?> describedTypeRef, CtTypeReference<?> accessingTypeRef, AccessibleFieldGraphNode... rootNodes) {
        this(rootNodes != null ? Arrays.asList(rootNodes) : List.of(), describedTypeRef, accessingTypeRef);
    }

    /**
     *
     * @param describedType
     *          The {@link CtTypeReference} of the type which the empty graph belongs to.
     *
     * @param accessingTypeRef
     *          The {@link CtTypeReference} of the type which accesses the {@code describedType}'s fields,
     *          not {@code null}.
     *
     * @return
     *          A {@link AccessibleFieldGraph} without any nodes.
     */
    public static AccessibleFieldGraph empty(CtTypeReference<?> describedType, CtTypeReference<?> accessingTypeRef) {
        return new AccessibleFieldGraph(describedType, accessingTypeRef);
    }

    /**
     * Iterative method to get all nodes in {@code this} graph. Traverses the {@link AccessibleFieldGraphNode#getChildren()
     * child nodes} of each {@link #getRootNodes() root node} until no new nodes are found.
     *
     * @return
     *          A set containing all {@link AccessibleFieldGraphNode} which {@code this}
     *          graph contains.
     */
    public Set<AccessibleFieldGraphNode> getAllNodes() {
        var newNodes = new LinkedList<>(rootNodes);
        var allNodes = new HashSet<>(rootNodes);

        while(!newNodes.isEmpty()) {
            var currentNode = newNodes.removeFirst();

            currentNode.getChildren().stream()
                    .filter(allNodes::add)
                    .forEach(newNodes::addLast);
        }

        return allNodes;
    }

    /**
     *
     * @return
     *          The set iterator of the set returned by {@link #getAllNodes()}.
     */
    @Override
    public Iterator<AccessibleFieldGraphNode> iterator() {
        return getAllNodes().iterator();
    }

    /**
     * Returns a set of paths which end with a leaf node or a node which creates a cycle as described by
     * the <i>transitive reachability set</i> (<i>transitive Erreichbarkeitsmenge</i>).
     *
     * @return An <b>unmodifiable</b> set containing the {@link Path}s in the <i>transitive reachability set</i>
     * (<i>transitive Erreichbarkeitsmenge</i>), not {@code null}.
     */
    public Set<Path> getTransitiveReachabilityPaths() {
        if(transitiveReachabilityPaths != null) {
            return transitiveReachabilityPaths;
        }

        var fullyExploredPaths = new HashSet<Path>();
        var basePaths = new LinkedList<Path>();
        rootNodes.stream()
                .map(Path::new)
                .forEach(basePaths::add);

        while(!basePaths.isEmpty()) {
            var currentBasePath = basePaths.removeFirst();

            var currentPathExtensionNodes = currentBasePath.getLast()
                    .get()
                    .getChildren();

            if(currentPathExtensionNodes.isEmpty()) {
                fullyExploredPaths.add(currentBasePath);
            } else {
                currentPathExtensionNodes.stream()
                        .map(extension -> new Path(currentBasePath).append(extension))
                        .forEach(extendedPath -> {
                            if(extendedPath.containsLoop()) {
                                fullyExploredPaths.add(extendedPath);
                            } else {
                                basePaths.addLast(extendedPath);
                            }
                        });
            }
        }

        this.transitiveReachabilityPaths = Set.copyOf(fullyExploredPaths);
        return transitiveReachabilityPaths;
    }

}
