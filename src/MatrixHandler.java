
import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * This class handles' server.Matrix-related tasks
 */
public class MatrixHandler implements IHandler {
    private final ThreadPoolExecutor tasksThreadPool;

    private Matrix matrix;
    private volatile boolean doWork = true;
    private String requester;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private Promise<Set<Set<Index>>> connectedComponents = new Promise<>();
    private Promise<Integer> numOfSubs = new Promise<>();

    public MatrixHandler(ThreadPoolExecutor tasksThreadPool) {
        this.tasksThreadPool = tasksThreadPool;

        // Setting initial values for connectedComponents and numOfSubs so the server won't be stuck
        // if client has asked for one of those without sending a matrix first.
        this.connectedComponents.setValue(new HashSet<>());
        this.numOfSubs.setValue(-1);
    }

    @Override
    public void resetMembers() {
        this.matrix = null;
        this.requester = null;
        this.inputStream = null;
        this.outputStream = null;
        this.doWork = true;
    }

    /**
     * this function closes the input and output streams
     */
    public void stop() {
        this.doWork = false;
        try {
            this.inputStream.close();
            this.outputStream.close();
        } catch (IOException e) {
            System.out.println("Server: Failed to close I/O streams");
        }
    }

    /**
     * this function print to the screen some info messages
     * @param msg - the message
     */
    private void info(String msg) {
        System.out.println("Server: " + msg);
    }

    /**
     * this function print to the screen error messages
     * @param msg - the message
     */
    private void err(String msg) {
        System.err.println("Server: " + msg);
    }

    /**
     * this function receive object using object input stream
     * prints the description of the object it got
     * @param description - description of what the handler receive
     * @return - the object it received through the object input stream
     */
    private Object receive(String description) {
        try {
            Object object = this.inputStream.readObject();
            this.info("Received " + description + " from " + this.requester);
            return object;
        } catch (IOException | ClassNotFoundException e) {
            this.err("Failed to receive " + description + " from " + this.requester);
        }
        return null;
    }

    /**
     * the function sends object through the object output stream
     * @param object - the function gets the object it should send
     * @param description - the description of what the function got
     */
    private void send(Object object, String description) {
        try {
            this.outputStream.writeObject(object);
        } catch (IOException e) {
            this.err("Failed to send " + description + " to " + this.requester);
        }
    }

    /**
     * this function is using for the switch case
     * the string it returned tells the handler what to do next
     * @return - the function returns the string it received from the input stream
     */
    private String receiveRequest() {
        try {
            String request = this.inputStream.readObject().toString();
            this.info("Received request '" + request + "' from " + this.requester);
            return request;
        } catch (IOException | ClassNotFoundException e) {
            this.err("Failed to receive request from " + this.requester);
        }
        return null;
    }

    @Override
    public void handle(String clientId, InputStream fromClient, OutputStream toClient)
            throws IOException {
        /*
        Send data as bytes.
        Read data as bytes then transform to meaningful data
        ObjectInputStream and ObjectOutputStream can read and write both primitives and objects
         */
        this.requester = clientId;
        this.inputStream = new ObjectInputStream(fromClient);
        this.outputStream = new ObjectOutputStream(toClient);

        // handle client's tasks using switch case
        while (this.doWork) {
            try {
                switch (Objects.requireNonNull(this.receiveRequest())) {
                    case "matrix": {
                        int[][] tempArray = (int[][]) this.receive("2D array");
                        this.matrix = new Matrix(tempArray);
                        // Empty the connected components and numOfSubs, because we've got a new Matrix to calculate those for it.
                        this.connectedComponents = new Promise<>();
                        this.numOfSubs = new Promise<>();

                        // Parallelizing connectedComponents and numOfSubs
                        tasksThreadPool.execute(() -> {
                            try {
                                this.connectedComponents.setValue(this.matrix.getConnectedComponents(tasksThreadPool));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        tasksThreadPool.execute(() -> {
                            try {
                                this.numOfSubs.setValue(this.matrix.getNumOfSubs(tasksThreadPool));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        this.send(this.matrix, "matrix");
                        break;
                    }
                    case "getOnes": {
                        this.send(this.connectedComponents.getValue(), "all ones of matrix");
                        break;
                    }
                    case "getShortestPath": {
                        Index[] indices = (Index[]) this.receive("indices");
                        if (indices.length != 2) {
                            throw new Exception("the indices are illegal");
                        }
                        this.info("Start: " + indices[0] + ",  end: " + indices[1] + ".");
                        this.send(this.matrix.getShortestPath(indices), "shortest path:");
                        break;
                    }
                    case "getNumOfSubs": {
                        this.send(this.numOfSubs.getValue(), "number of subs");
                        break;
                    }
                    case "getEasiestPath": {
                        Index[] indices = (Index[]) this.receive("indices");
                        this.info("Start: " + indices[0] + ",  end: " + indices[1] + ".");
                        this.send(this.matrix.getWeightedShortestPath(indices), "weighted shortest path:");
                        break;
                    }
                    case "stop": {
                        doWork = false;
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } catch (Exception ignored) {
                break;
            }
        }
    }

}
