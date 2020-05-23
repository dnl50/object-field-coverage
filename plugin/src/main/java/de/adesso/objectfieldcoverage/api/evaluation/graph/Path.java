package de.adesso.objectfieldcoverage.api.evaluation.graph;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
     * Initializes the internal list with a <b>unmodifiable</b> copy of the given
     * {@code nodes} list.
     *
     * @param nodes
     *          The nodes the path consists of. Must be a valid path as specified on the
     *          class level JavaDoc. Cannot contain {@code null} elements.
     */
    public Path(List<AccessibleFieldGraphNode> nodes) {
        if(!isValidPath(nodes)) {
            throw new IllegalArgumentException("The given nodes list is not a valid path!");
        }

        this.nodes = (nodes == null) ? List.of() : List.copyOf(nodes);
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
     *          An iterator of the list containing the nodes of the path. The next {@link AccessibleFieldGraphNode node}
     *          returned by this iterator
     */
    @Override
    public Iterator<AccessibleFieldGraphNode> iterator() {
        return nodes.iterator();
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

            if(currentlyInspectedNode == null || parentNode == null || !parentNode.getChildren().contains(currentlyInspectedNode)) {
                return false;
            }
        }

        return true;
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
