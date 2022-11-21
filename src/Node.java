
/**
 * This class wraps a concrete object and supplies getters and setters
 *
 * @param <T>
 *
 */
public class Node<T extends Comparable<T>> implements Comparable<Node<T>> {

    private T data;
    private Node<T> parent;

    // constructor
    public Node(T someObject, final Node<T> discoveredBy) {
        this.data = someObject;
        this.parent = discoveredBy;
    }
    // constructor
    public Node(T someObject){
        this(someObject,null);
    }
    // getter
    public T getData() {
        return data;
    }
    // setter
    public void setData(T data) {
        this.data = data;
    }
    // getter
    public Node<T> getParent() {
        return parent;
    }
    // setter
    public void setParent(Node<T> parent) {
        this.parent = parent;
    }

    /*
    This is used when accessing objects multiple times with comparisons,
    when using a HashTable
    Set<Node<T>> finished - this will work only if concrete object are different
    Node<Index> Node<Coordinate> Node<ComputerLocation>
    Node<Index> Node<Index> Node<Index>
    */
    @Override
    public int hashCode() {
        return data != null ? data.hashCode():0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false; // checks if the object we got is instance of Node
        Node<?> state1 = (Node<?>) o;
        return this.data.equals((T) state1.getData()); // checks if both objects have the same data and return true or false
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int compareTo(Node<T> o) {
        return this.getData().compareTo(o.getData());
    }
}
