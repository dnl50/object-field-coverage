package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtLocalVariableReference;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InvocationResultTracker {

    public boolean accessesTargetInvocationResult(CtExpression<?> expression, CtInvocation<?> targetMethodInvocation) {
        if(expression.equals(targetMethodInvocation)) {
            return true;
        }

        var expressionParentMethod = expression.getParent(CtMethod.class);
        var invocationTargetMethod = targetMethodInvocation.getParent(CtMethod.class);

        if(!expressionParentMethod.equals(invocationTargetMethod)) {
            log.warn("The given expression and target method invocation do not have the same parent method!");
            return false;
        }

        if(expression instanceof CtVariableRead) {
            var variableRead = (CtVariableRead<?>) expression;
            var readVariableRef = variableRead.getVariable();

            if(readVariableRef instanceof CtLocalVariableReference) {
                var readLocalVariableRef = (CtLocalVariableReference<?>) readVariableRef;

                var defaultExpression = readLocalVariableRef.getDeclaration()
                        .getDefaultExpression();

                if(defaultExpression == null) {
                    log.warn("Local variable '{}' does not have a default expression!",
                            readLocalVariableRef.getSimpleName());
                    return false;
                }

                return accessesTargetInvocationResult(defaultExpression, targetMethodInvocation);
            }
        }

        if(expression instanceof CtTargetedExpression) {
            var targetedExpression = (CtTargetedExpression<?, ?>) expression;

            return accessesTargetInvocationResult(targetedExpression.getTarget(), targetMethodInvocation);
        }

        return false;
    }

    /**
     *
     * @param expression
     *          The expression which is based on the result of the given {@code targetMethodInvocation},
     *          not {@code null}.
     *
     * @param targetExecutableInvocation
     *          The
     *
     * @param accessibleFieldGraph
     *
     *
     * @return
     *          An optional containing the
     */
    public Optional<Path> getPathPrefixForAccess(CtExpression<?> expression,
                                                 CtInvocation<?> targetExecutableInvocation,
                                                 AccessibleFieldGraph accessibleFieldGraph) {
        if(!accessesTargetInvocationResult(expression, targetExecutableInvocation)) {
            log.warn("The expression '{}' does not access the result of " +
                    "the target invocation '{}'!", expression, targetExecutableInvocation);
            return Optional.empty();
        }

        return Optional.ofNullable(getPathPrefixForAccessInternal(expression, targetExecutableInvocation,
                accessibleFieldGraph, new ArrayList<>()));
    }

    private Path getPathPrefixForAccessInternal(CtExpression<?> expression,
                                               CtInvocation<?> targetMethodInvocation,
                                               AccessibleFieldGraph accessibleFieldGraph,
                                               List<CtTypedElement<?>> accessingElements) {
        if(expression.equals(targetMethodInvocation)) {
            Collections.reverse(accessingElements);

            var path = new Path();
            var currentNodes = accessibleFieldGraph.getRootNodes();

            for(var element : accessingElements) {
                var childNodeWithAccessGrantingElement = findNodeWithAccessGrantingElement(currentNodes, element);

                if(childNodeWithAccessGrantingElement == null) {
                    return null;
                }

                path.append(childNodeWithAccessGrantingElement);
                currentNodes = Set.of(childNodeWithAccessGrantingElement);
            }

            return path;
        }

        if(expression instanceof CtVariableRead) {
            var variableRead = (CtVariableRead<?>) expression;
            var readVariableRef = variableRead.getVariable();

            if(readVariableRef instanceof CtLocalVariableReference) {
                var readLocalVariableRef = (CtLocalVariableReference<?>) readVariableRef;

                var defaultExpression = readLocalVariableRef.getDeclaration()
                        .getDefaultExpression();

                if(defaultExpression == null) {
                    log.warn("Local variable '{}' does not have a default expression!",
                            readLocalVariableRef.getSimpleName());
                    return null;
                }

                return getPathPrefixForAccessInternal(defaultExpression, targetMethodInvocation,
                        accessibleFieldGraph, accessingElements);
            }
        }

        if(expression instanceof CtInvocation) {
            var intermediateInvocation = (CtInvocation<?>) expression;
            var intermediateInvocationTarget = intermediateInvocation.getTarget();
            var invokedIntermediateExecutable = intermediateInvocation.getExecutable()
                    .getExecutableDeclaration();

            accessingElements.add(invokedIntermediateExecutable);
            return getPathPrefixForAccessInternal(intermediateInvocationTarget, targetMethodInvocation, accessibleFieldGraph,
                    accessingElements);
        }

        if(expression instanceof CtFieldAccess) {
            var intermediateFieldAccess = (CtFieldAccess<?>) expression;
            var intermediateFieldAccessTarget = intermediateFieldAccess.getTarget();
            var accessedField = intermediateFieldAccess.getVariable()
                    .getFieldDeclaration();

            accessingElements.add(accessedField);
            return getPathPrefixForAccessInternal(intermediateFieldAccessTarget, targetMethodInvocation, accessibleFieldGraph,
                    accessingElements);
        }

        return null;
    }

    /**
     *
     * @param nodes
     *          The {@link AccessibleFieldGraphNode}s to find the child node in whose {@link AccessibleField}
     *          contains the given {@code accessGrantingElement} as an access granting element, not {@code null}.
     *
     * @param accessGrantingElement
     *          The access granting element to find a child node for, not {@code null}.
     *
     * @return
     *          The {@link AccessibleFieldGraphNode} whose {@link AccessibleField#getAccessGrantingElements() access
     *          granting elements} contains the given {@code accessGrantingElement} or {@code null} if no such
     *          node exists.
     */
    private AccessibleFieldGraphNode findNodeWithAccessGrantingElement(Collection<AccessibleFieldGraphNode> nodes,
                                                                       CtTypedElement<?> accessGrantingElement) {
        var childNodeWithAccessGrantingElement = nodes.stream()
                .filter(childNode -> childNode.getAccessibleField().getAccessGrantingElements().contains(accessGrantingElement))
                .findFirst();

        if(childNodeWithAccessGrantingElement.isEmpty()) {
            var nodeShortRepresentations = nodes.stream()
                    .map(AccessibleFieldGraphNode::getAccessibleField)
                    .map(AccessibleField::getActualField)
                    .map(CtField::getShortRepresentation)
                    .collect(Collectors.toSet());

            log.warn("No child node of nodes for '{}' with access granting element '{}' present!",
                    nodeShortRepresentations, accessGrantingElement);
            return null;
        }

        return childNodeWithAccessGrantingElement.get();
    }

}
