import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * this class is the "real" clients in this program
 */
public class MatrixClient extends Client {

    int[][] arr2d;

    // constructor
    public MatrixClient(String ipAddr, int port, int[][] arr2d) throws IOException {
        super(ipAddr, port); // using client constructor
        this.arr2d = arr2d;
    }

    /**
     * this function asks for Matrix from the server
     * it sends the String "matrix" for the switch case to know what it should do
     * and then sends the 2d array
     * and waits for Matrix to return from the server
     */
    public void sendMatrix() {
        this.send("matrix", "matrix");
        this.send(this.arr2d, "2D array");
        Matrix m = (Matrix)this.receive();
        this.info(String.format("My matrix is: %n%s", m));
    }

    /**
     * this function asks for groups of ones that the matrix have
     * the function send the String "getones" to the server
     * and expect to receive Set<Set<Index>> that the value is one
     * and print it to the screen
     */
    public void sendOnes() {
        this.send("getOnes", "ones");
        Set<Set<Index>> ones = (Set<Set<Index>>) this.receive();
        String onesString = ones.stream().map(v -> v.toString()).collect(Collectors.joining("\n"));
        this.info("My connected components are: " + onesString);
    }

    /**
     * this function asks for the shortest path from start index to end index
     * first it sends the String "getShortestPath" to the server
     * second it sends the Index[] indices that contain the start index and the end index
     * third expect to receive the shortest path
     * forth it prints the path on the screen
     * @param indices - contain the start index and the end index
     */
    public void getShortestPath(Index[] indices) {
        this.send("getShortestPath", "shortest path");
        this.send(indices, "indices");
        List<Index> path = (List<Index>) this.receive();
        this.info(String.format("The shortest path: %n%s", path));
    }

    /**
     * this function asks for the number of submarines in the matrix
     * first it sends the String "getNumOfSubs" to the server
     * second expect to receive the number of submarines in the matrix
     */
    public void getNumOfSubs(){
        this.send("getNumOfSubs", "number of subs");
        Integer numSubs = (int) this.receive();
        this.info(String.format("Num subs: %d", numSubs));
    }

    /**
     * this function asks for the lightest path from start index to end index
     * first it sends the String "getEasiestPath" to the server
     * second it sends the Index[] indices that contain the start index and the end index
     * third expect to receive the lightest path
     * forth it prints the path on the screen
     * @param indices - contain the start index and the end index
     */
    public void getEasiestPath(Index[] indices) {
        this.send("getEasiestPath", "easiest path");
        this.send(indices, "indices");
        List<Index> path = (List<Index>) this.receive();
        this.info(String.format("The easiest path: %n%s", path));
    }
}
