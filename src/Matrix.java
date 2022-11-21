import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Index class implements Serializable
 * the implementation of Serializable is for Send data as bytes
 * and Read data as bytes then transform to meaningful data.
 */
public class Matrix implements Serializable {

    int[][] primitiveMatrix;

    //constructor
    public Matrix(int[][] oArray){
        List<int[]> list = new ArrayList<>();
        for (int[] row : oArray) {
            int[] clone = row.clone();
            list.add(clone);
        }
        primitiveMatrix = list.toArray(new int[0][]);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int[] row : primitiveMatrix) {
            stringBuilder.append(Arrays.toString(row));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * this function return collection of Index
     * the collection that this function returns is the neighbors of
     * the index the function gets
     * @param index - the index we want to get he's neighbors
     * @return - the neighbors of a specific Index
     */
    public Collection<Index> getNeighbors(final Index index) {
        Collection<Index> list = new ArrayList<>();
        int extracted = -1;
        try{
            extracted = primitiveMatrix[index.row+1][index.column];
            list.add(new Index(index.row+1,index.column, extracted));
        }catch (ArrayIndexOutOfBoundsException ignored){}
        try{
            extracted = primitiveMatrix[index.row][index.column+1];
            list.add(new Index(index.row,index.column+1, extracted));
        }catch (ArrayIndexOutOfBoundsException ignored){}
        try{
            extracted = primitiveMatrix[index.row-1][index.column];
            list.add(new Index(index.row-1,index.column, extracted));
        }catch (ArrayIndexOutOfBoundsException ignored){}
        try{
            extracted = primitiveMatrix[index.row][index.column-1];
            list.add(new Index(index.row,index.column-1, extracted));
        }catch (ArrayIndexOutOfBoundsException ignored){}
        return list;
    }
    // getter
    public int getValue(Index index) {
        return primitiveMatrix[index.row][index.column];
    }

    /**
     * this function searching for the reachable neighbors (the value equals to one)
     * @param index - the index we want to get he's reachable
     * @return - the reachables from a specific Index
     */
    protected Collection<Index> getReachables(Index index) {
        return this.getNeighbors(index).stream()
                .filter(i-> getValue(i)==1)
                .collect(Collectors.toList());
    }

    private Set<Index> getSingleComponent(Index index) {
        Set<Index> reachables = new HashSet<>(this.getReachables(index));
        reachables.add(index);
        while (true) {
            Set<Index> newReachables = reachables.stream()
                    .flatMap(idx -> this.getReachables(idx).stream())
                    .filter(idx -> !reachables.contains(idx))
                    .collect(Collectors.toSet());
            if (newReachables.size() == 0) {
                break;
            }
            reachables.addAll(newReachables);
        }
        return reachables.stream().sorted((o1, o2) ->
                o1.getRow() - o2.getRow() == 0 ?
                        o1.getColumn() - o2.getColumn() : o1.getRow() - o2.getRow())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * this function searching for group of ones in the matrix
     * using the getSingleComponent function that returns list of connected ones
     * for every index the getConnectedComponents sends to getSingleComponent
     * @return - Set<Set<Index>> means list of lists' every list is a different group of ones in the matrix
     */
    public Set<Set<Index>> getConnectedComponents(ThreadPoolExecutor threadPool) throws InterruptedException {
        List<Promise<Set<Index>>> connectedComponents = new ArrayList<>();
        for (int i = 0; i < this.primitiveMatrix.length; i++) {
            for (int j = 0; j < this.primitiveMatrix[i].length; j++) {
                Index index = new Index(i, j);
                if (this.primitiveMatrix[i][j] != 0) {
                    Promise<Set<Index>> promise = new Promise<>();
                    threadPool.execute(() -> promise.setValue(this.getSingleComponent(index)));
                    connectedComponents.add(promise);
                }
            }
        }

        Set<Set<Index>> set = new HashSet<>();
        for (Promise<Set<Index>> x : connectedComponents) {
            Set<Index> value = x.getValue();
            set.add(value);
        }
        return set;
    }

    /**
     * validate that indices build from two indexes only
     * and also validate that the start index and the end index are not the same index
     * and there value is note zero
     * @param indices - contains start index and end index
     * @throws Exception
     */
    private void validateIndices(Index[] indices) throws Exception {
        if (indices.length != 2) {
            throw new Exception("indices does not match length = 2!");
        }
        if (indices[0].equals(indices[1]) || indices[0].getValue() == 0 || indices[1].getValue() == 0) {
            throw new Exception("start index equals to end index or one of them equals to zero");
        }
    }

    /**
     * @param indices - contains start index and end index
     * @return -      * this function returns the shortest path using the BFS class
     * @throws Exception - when the indices are illegal
     */
    public List<Index> getShortestPath(Index[] indices) throws Exception {
        validateIndices(indices);
        return new BFS<Index>().traverse(
                new TraversableMatrix(this, indices[0], indices[1]));
    }

    private boolean isRectangle(Collection<Index> component) {
        List<Integer> columns = component.stream()
                .map(Index::getColumn)
                .sorted()
                .collect(Collectors.toList())
                ;
        List<Integer> rows = component.stream()
                .map(Index::getRow)
                .sorted()
                .collect(Collectors.toList())
                ;
        Index leftUpperIndex = new Index(columns.get(0), rows.get(0));
        Index rightLowerIndex = new Index(columns.get(columns.size() - 1), rows.get(rows.size() - 1));
        Collection<Index> rectangle = this.generateRectangle(leftUpperIndex, rightLowerIndex);
        return component.size() == rectangle.size();
    }

    private Collection<Index> generateRectangle(Index start, Index end) {
        return IntStream.rangeClosed(start.getRow(), end.getRow())
                .mapToObj(i -> IntStream.rangeClosed(start.getColumn(), end.getColumn())
                        .mapToObj(j -> new Index(i, j)))
                .flatMap(Function.identity())
                .collect(Collectors.toList())
                ;
    }

    /**
     * this function returns the number of submarines in the matrix
     * first the function gets the connected Components
     * second creates a Rectangle with the indexes of the connected Components we got
     * using another function isRectangle
     * and checks if the size of the rectangle equals to the size of validSubs
     * if they are equal it returned the size of valid subs (number of subs) else return zero
     * @return - this function returns the number of submarines in the matrix
     */
    public int getNumOfSubs(ThreadPoolExecutor threadPool) throws InterruptedException {
        Collection<Set<Index>> connComponents = this.getConnectedComponents(threadPool);
        Collection<Set<Index>> validSubs = connComponents.stream()
                .filter(c -> c.size() > 1 && this.isRectangle(c))
                .collect(Collectors.toList());
        return validSubs.size() == connComponents.size() ? validSubs.size() : 0;
    }

    /**
     *
     * @param indices - contains start index and end index
     * @return - this function returned the Weighted Shortest Path using Dijextra class
     * @throws Exception if indices id illegal
     */
    public List<Index> getWeightedShortestPath(Index[] indices) throws Exception {
        validateIndices(indices);
        return new Dijkstra<Index>().traverse(
                new TraversableMatrix(this, indices[0], indices[1]));
    }
}
