package de.adesso.objectfieldcoverage.api.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

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
@AllArgsConstructor
public class AccessibleFieldGraphNode {

    /**
     * The {@link AccessibleField} instance this node refers to.
     */
    private final AccessibleField<?> accessibleField;

    /**
     * The child nodes of {@code this} node. Might contain {@code this} node
     * itself (cyclic reference).
     * <br/>
     * Must be a {@link LinkedHashSet} since a normal {@link HashSet} causes issues when adding child nodes.
     */
    @ToString.Exclude
    private final Set<AccessibleFieldGraphNode> children;

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

        return new AccessibleFieldGraphNode(accessibleField, new LinkedHashSet<>());
    }

    @Override
    public int hashCode() {
        //TODO: causes illegal reflective access JVM warnings
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(!(obj instanceof AccessibleFieldGraphNode)) {
            return false;
        }

        return new EqualsBuilder()
                .setTestRecursive(true)
                .append(this, obj)
                .isEquals();
    }

}
