package de.adesso.objectfieldcoverage.api.evaluation.graph;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * The representation path through a {@link AccessibleFieldGraph}. A path is a finitely long sequence of
 * {@link AccessibleFieldGraphNode nodes} where the first element is a {@link AccessibleFieldGraph#getRootNodes() root}.
 * and every node at index {@code i} is contained in the {@link AccessibleFieldGraphNode#getChildren() child nodes} of
 * the node at index {@code i-1}.
 */
@Getter
@Setter
@EqualsAndHashCode
public class Path implements Iterable<AccessibleFieldGraphNode> {

    /**
     * The nodes which form the path. The first element <b>must</b> be a {@link AccessibleFieldGraph#getRootNodes() root}
     * node of the backing {@link AccessibleFieldGraph}.
     * <p/>
     * All but the first node must fulfill the following contract: The node at index {@code i} must
     * be a {@link AccessibleFieldGraphNode#getChildren() child node} of the element at index {@code i-1}.
     */
    private final List<AccessibleFieldGraphNode> nodes;

    /**
     * Initializes the internal list with a modifiable copy of the given {@code nodes} list.
     *
     * @param nodes
     *          The nodes the path consists of. Must be a valid path as specified on the
     *          class level JavaDoc. Cannot contain {@code null} elements.
     */
    public Path(List<AccessibleFieldGraphNode> nodes) {
        if(!isValidPath(nodes)) {
            throw new IllegalArgumentException("The given nodes list is not a valid path!");
        }

        this.nodes = (nodes == null) ? new ArrayList<>() : new ArrayList<>(nodes);
    }

    /**
     * Variable arity constructor for convenience.
     *
     * @param nodes
     *          The nodes the path consists of.
     */
    public Path(AccessibleFieldGraphNode... nodes) {
        this(nodes == null ? List.of() : Arrays.asList(nodes));
    }

    /**
     * Default no-arg constructor for an empty path.
     *
     * @see #Path(List)
     */
    public Path() {
        this(List.of());
    }

    /**
     * Copy constructor.
     *
     * @param path
     *          The path to copy.
     */
    public Path(Path path) {
        this(path == null ? List.of() : path.nodes);
    }

    /**
     * The length of a path is the number of nodes in it.
     *
     * @return
     *          The length of the {@code this} path.
     */
    public int getLength() {
        return nodes.size();
    }

    /**
     *
     * @return
     *          An optional of the last node of {@code this} path or an empty
     *          optional in case the path has a {@link #getLength() length} of zero.
     */
    public Optional<AccessibleFieldGraphNode> getLast() {
        return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(nodes.size() - 1));
    }

    /**
     *
     * @return
     *          {@code true}, if at least one node in {@code this} path appears more than once. {@code false}
     *          is returned otherwise.
     */
    public boolean containsLoop() {
        return Set.copyOf(nodes).size() < nodes.size();
    }

    /**
     *
     * @param node
     *          The node which should be appended to {@code this} path.
     *
     * @return
     *          {@code this} path.
     */
    public Path append(AccessibleFieldGraphNode node) {
        if(node != null) {
            var lastNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);

            if (lastNode != null && !isChildNodeOf(lastNode, node)) {
                throw new IllegalArgumentException("The given node is not a child node of the current last node!");
            }

            nodes.add(node);
        }

        return this;
    }

    /**
     *
     * @return
     *          An iterator of the list containing the nodes of the path. The next {@link AccessibleFieldGraphNode node}
     *          returned by this iterator
     */
    @Override
    public Iterator<AccessibleFieldGraphNode> iterator() {
        return nodes.iterator();
    }

    /**
     * The paths are equal, when this method returns {@code true} for both {@code a.startsWith(b)} and
     * {@code b.startsWith(a)}.
     *
     * @param other
     *          The {@link Path} which might be a prefix of {@code this} path, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@link Path} is a prefix of {@code this} path. {@code false} is returned
     *          otherwise.
     */
    public boolean startsWith(Path other) {
        Objects.requireNonNull(other, "Path cannot be null!");

        if(this == other) {
            return true;
        } else if(this.getLength() < other.getLength()) {
            return false;
        }

        for(var otherPathIndex = 0; otherPathIndex < other.nodes.size(); otherPathIndex++) {
            if(!other.nodes.get(otherPathIndex).equals(nodes.get(otherPathIndex))) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @return
     *          {@code true}, when the given list of {@link AccessibleFieldGraphNode nodes} are a valid
     *          path as specified on the {@link #nodes} field. {@code false} is returned otherwise.
     */
    private boolean isValidPath(List<AccessibleFieldGraphNode> nodes) {
        if(nodes == null || nodes.size() <= 1) {
            return true;
        }

        for(int i = (nodes.size() - 1); i > 0; i--) {
            var currentlyInspectedNode = nodes.get(i);
            var parentNode = nodes.get(i - 1);

            if(!isChildNodeOf(parentNode, currentlyInspectedNode)) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param parentNode
     *          The parent node.
     *
     * @param childNode
     *          The child node.
     *
     * @return
     *          {@code true}, if both nodes are not {@code null} and the given {@code parent}'s
     *          {@link AccessibleFieldGraphNode#getChildren() child nodes} contains the given {@code childNode}.
     *          {@code false} is returned otherwise.
     */
    private boolean isChildNodeOf(AccessibleFieldGraphNode parentNode, AccessibleFieldGraphNode childNode) {
        return parentNode != null && childNode != null && parentNode.getChildren().contains(childNode);
    }

    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();
        stringBuilder.append("Path(length=")
                .append(getLength())
                .append(", simpleNamesOfFieldsOnPath=[");

        if(!nodes.isEmpty()) {
            var firstNodeSimpleName = nodes.get(0)
                    .getAccessibleField()
                    .getActualField()
                    .getSimpleName();
            stringBuilder.append(firstNodeSimpleName);

            for (int nodeIndex = 1; nodeIndex < nodes.size(); nodeIndex++) {
                var currentNode = nodes.get(nodeIndex);
                var simpleNameOfField = currentNode.getAccessibleField()
                        .getActualField()
                        .getSimpleName();

                stringBuilder.append("->");
                stringBuilder.append(simpleNameOfField);
            }
        }

        stringBuilder.append("])");
        return stringBuilder.toString();
    }

}
