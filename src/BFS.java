import java.util.*;

/**
 *
 * @param <T> getting parameter T extends Comparable<T>
 *           this function implements the BFS search
 *           create working queue that tells us which index we're working on right now
 *           create visited set that tells us if we are already visited the index that we want to work on now
 *           and generates the shortest path from startIndex to endIndex
 */
public class BFS<T extends Comparable<T>> {

    ThreadLocal<Collection<Node<T>>> workingQueue;
    ThreadLocal<Set<Node<T>>> visited = ThreadLocal.withInitial(LinkedHashSet::new);

    BFS() {
        this.workingQueue = ThreadLocal.withInitial(LinkedList::new);
    }

    public List<T> traverse(Traversable<T> someGraph) {
        /*
         add origin to the workingQueue
         set origin to the visited set
         while workingQueue is not empty
             popped = removeFromQueue
             invoke getReachableNodes method on popped node
             for each reachableNode in reachableNodes
                 if current reachableNode equals to endNode
                     return the path from reachableNode to startNode
                 else if current reachableNode is yet to discover (not in visited set)
                     add reachableNode to the workingQueue
                     set reachableNode to the visited set
        */
        this.addToQueue(someGraph.getOrigin());
        this.setAsVisited(someGraph.getOrigin());
        while(!this.isQueueEmpty()) {
            Node<T> popped = this.removeFromQueue();
            Collection<Node<T>> reachableNodes = someGraph.getReachableNodes(popped);
            for (Node<T> singleReachableNode : reachableNodes) {
                if (singleReachableNode.equals(someGraph.getTarget())) {
                    return this.generatePath(singleReachableNode);
                } else if (!this.isVisited(singleReachableNode)) {
                    this.addToQueue(singleReachableNode);
                    this.setAsVisited(singleReachableNode);
                }
            }
        }
        return null;
    }
    // checks if queue is empty
    protected boolean isQueueEmpty() {
        return this.workingQueue.get().isEmpty();
    }
    // add node to the queue
    protected void addToQueue(Node<T> node) {
        ((LinkedList<Node<T>>) this.workingQueue.get()).push(node);
    }
    // remove node from queue
    protected Node<T> removeFromQueue() {
        return ((LinkedList<Node<T>>) this.workingQueue.get()).poll();
    }
    // add node to visited set
    protected void setAsVisited(Node<T> node) {
        this.visited.get().add(node);
    }
    // checks i we already visited this node
    protected boolean isVisited(Node<T> node) {
        return this.visited.get().contains(node);
    }

    /*
    this function generate the path from node to start node
    by pushing node.data to list then going to the node parent
    while node is note null
     */
    protected List<T> generatePath(Node<T> node) {
        LinkedList<T> path = new LinkedList<>();
        while (node != null) {
            path.push(node.getData());
            node = node.getParent();
        }
        return path;
    }
}
