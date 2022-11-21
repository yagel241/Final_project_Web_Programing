import java.util.Collection;

/**
 * This interface defines the functionality required for a traversable graph
 */
public interface Traversable<T extends Comparable<T>> {
    Node<T> getOrigin();
    Node<T> getTarget();
    Collection<Node<T>> getReachableNodes(Node<T> someNode);
}
