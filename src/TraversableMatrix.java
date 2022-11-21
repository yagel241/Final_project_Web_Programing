import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class implement adapter/wrapper/decorator design pattern
 * we're using this class for creating a TraversableMatrix
 */
public class TraversableMatrix implements Traversable<Index> {
    protected final Matrix matrix;
    protected Index startIndex;
    protected Index endIndex;

    // constructor
    public TraversableMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    // constructor
    public TraversableMatrix(Matrix matrix, Index start, Index end) {
        this(matrix);
        this.startIndex = start;
        this.endIndex = end;
    }

    /**
     *
     * @return - start Index Wrapped in a Node
     * @throws NullPointerException - if index == null
     */
    @Override
    public Node<Index> getOrigin() throws NullPointerException {
        if (this.startIndex == null) throw new NullPointerException("start index is not initialized");
        return new Node<>(this.startIndex);
    }

    /**
     *
     * @return - end Index Wrapped in a Node
     * @throws NullPointerException - if index == null
     */
    @Override
    public Node<Index> getTarget() throws NullPointerException {
        if (this.endIndex == null) throw new NullPointerException("end index is not initialized");
        return new Node<>(this.endIndex);
    }

    /**
     * the function search for the reachable nodes around the node it got
     * reachable node is a neighbor node that not equals to zero
     * @param someNode - node that we want to find the reachable nodes around him
     * @return - List<Node<Index>> of the reachable indices
     */
    @Override
    public Collection<Node<Index>> getReachableNodes(Node<Index> someNode) {
        List<Node<Index>> reachableIndices = new ArrayList<>();
        for (Index index : this.matrix.getNeighbors(someNode.getData())) {
            if (matrix.getValue(index) != 0) {
                Node<Index> indexNode = new Node<>(index, someNode);
                reachableIndices.add(indexNode);
            }
        }
        return reachableIndices;
    }

    @Override
    public String toString() {
        return matrix.toString();
    }

}