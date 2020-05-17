package de.adesso.objectfieldcoverage.api.evaluation.graph;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

/**
 * Graph representation of the fields which can be accessed from a given {@link spoon.reflect.declaration.CtClass}.
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
     * An empty graph with no {@link #getRootNodes() root nodes} and therefore no
     * nodes at all.
     */
    public static final AccessibleFieldGraph EMPTY_GRAPH = new AccessibleFieldGraph(Set.of());

    /**
     * These are the nodes from which the graph was built.
     */
    private final Set<AccessibleFieldGraphNode> rootNodes;

    /**
     *
     * @param rootNodes
     *          The root nodes of the newly constructed graph, not {@code null}.
     */
    public AccessibleFieldGraph(Collection<AccessibleFieldGraphNode> rootNodes) {
        this.rootNodes = Set.copyOf(rootNodes);
    }

    /**
     *
     * @param rootNodes
     *          The root nodes of the newly constructed graph, not {@code null}.
     *
     * @see #AccessibleFieldGraph(Collection)
     */
    public AccessibleFieldGraph(AccessibleFieldGraphNode... rootNodes) {
        this(Arrays.asList(rootNodes));
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

}
