import java.util.*;
import java.util.stream.Collectors;

/**
 * this class extends from BFS class
 * @param <T extends Comparable>
 *
 *     when calling BFS.traverse using this class
 *     we are using PriorityQueue for order the shortest path
 *     due to the lightest weight.
 *
 *     the PriorityQueue is sort himself by the lightest weight
 *
 */


public class Dijkstra<T extends Comparable<T>> extends BFS<T> {

    Dijkstra() {
        this.workingQueue = ThreadLocal.withInitial(PriorityQueue::new);
    }

    @Override
    protected void addToQueue(Node<T> node) {
        this.workingQueue.get().add(node);
    }

    @Override
    protected Node<T> removeFromQueue() {
        return ((PriorityQueue<Node<T>>) this.workingQueue.get()).poll();
    }

}
