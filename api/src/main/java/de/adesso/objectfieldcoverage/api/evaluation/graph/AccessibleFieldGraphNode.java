package de.adesso.objectfieldcoverage.api.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representation of a node in a {@link AccessibleFieldGraph}. Each node represents a single
 * {@link spoon.reflect.declaration.CtField} which grants access to zero or more other fields.
 * <p/>
 * <b>Example:</b>
 * <br/>
 * Let's say {@code this} graph node represents the {@code address} field in the given
 * {@code Person} class.
 * <pre>
 *     public class Person {
 *
 *         private Address address;
 *
 *         // getter & setter
 *     }
 *
 *     public class Address {
 *
 *         private String name;
 *
 *         // getter & setter
 *     }
 * </pre>
 * The child nodes would contain the node representation of the {@code name} field in the {@code Address}
 * class, since having access to the {@code address} field also grants access to the {@code name} field.
 *
 * @see AccessibleField
 */
@Getter
@ToString
public class AccessibleFieldGraphNode {

    /**
     * The {@link AccessibleField} instance this node refers to.
     */
    private final AccessibleField<?> accessibleField;

    /**
     * The child nodes of {@code this} node. Might contain {@code this} node itself (cyclic reference). Uses
     * a list internally because of consistency problems with Java's hash set implementations.
     */
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final List<AccessibleFieldGraphNode> children;

    /**
     *
     * @param accessibleField
     *          The {@link AccessibleField} instance this node refers to, not {@code null}.
     *
     * @param children
     *          The child nodes of {@code this} node. Might contain {@code this} node itself (cyclic reference),
     *          not {@code null}.
     */
    public AccessibleFieldGraphNode(AccessibleField<?> accessibleField, Collection<AccessibleFieldGraphNode> children) {
        this.accessibleField = accessibleField;
        this.children = new ArrayList<>(children);
    }

    /**
     *
     * @param children
     *          The child nodes which should be added to {@code this} node, not {@code null}.
     */
    public void addChildren(Collection<? extends AccessibleFieldGraphNode> children) {
        this.children.addAll(children);
    }

    /**
     * A pseudo field node does not have any child nodes.
     *
     * @return
     *          {@code true}, if the {@link #getAccessibleField() accessible field} of {@code this} node
     *          {@link AccessibleField#isPseudo() is a pseudo field}. {@code false} is returned otherwise.
     */
    public boolean isPseudoFieldNode() {
        return accessibleField.isPseudo();
    }

    /**
     *
     * @param accessibleField
     *          The {@link AccessibleField}, not {@code null}.
     *
     * @return
     *          The node with its {@link #getAccessibleField() accessible field} set to the
     *          given {@code accessibleField}.
     */
    public static AccessibleFieldGraphNode of(AccessibleField<?> accessibleField) {
        Objects.requireNonNull(accessibleField, "The AccessibleField of a node cannot be null!");

        return new AccessibleFieldGraphNode(accessibleField, new ArrayList<>());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.accessibleField)
                .append(this.getFirstLevelChildrenWithoutChildren())
                .hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(!(obj instanceof AccessibleFieldGraphNode)) {
            return false;
        }

        var other = (AccessibleFieldGraphNode) obj;

        return new EqualsBuilder()
                .setTestRecursive(true)
                .append(this.accessibleField, other.accessibleField)
                .append(this.getFirstLevelChildrenWithoutChildren(), other.getFirstLevelChildrenWithoutChildren())
                .isEquals();
    }

    /**
     *
     * @return
     *          A <b>unmodifiable</b> set representation of the child nodes.
     */
    public Set<AccessibleFieldGraphNode> getChildren() {
        return Set.copyOf(children);
    }

    /**
     *
     * @return
     *          A set containing the {@link AccessibleFieldGraphNode} of the direct children of {@code this}
     *          node without any child nodes set.
     */
    private Set<AccessibleFieldGraphNode> getFirstLevelChildrenWithoutChildren() {
        return children.stream()
                .map(childNode -> new AccessibleFieldGraphNode(childNode.getAccessibleField(), List.of()))
                .collect(Collectors.toSet());
    }

}
