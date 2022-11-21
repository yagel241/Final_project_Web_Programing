import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * this class introduce the final project of "web Programing" course
 **/

public class Demo {
    static String ipAddr = "localhost";
    static int port = 8010;
    static int SIZE = 5;
    static int MAX_VALUE = 1000;

// create matrix for introduction of dijextra
    static int[][] originalMatrix1 = {
            {10,10,200,300},
            {1000,5,20,1000},
            {100,400,50,500},
            {100,200,20,10}};

    static int[][] originalMatrix = {
            {1,1,0,0},
            {0,0,0,1},
            {1,0,0,1},
            {1,0,0,0}};

// create start index and end index for introduction of dijextra
    static Index startIndex2 = new Index(0,0, originalMatrix1[0][0]);
    static Index endIndex2 = new Index(3,3,originalMatrix1[3][3]);
    static Index[] indices2 = new Index[]{startIndex2,endIndex2};

    // generate random number 0/1
    private static int generateRandomNumber(int maxValue) {
        return (int) (Math.random() * (maxValue + 1));
    }

    // generate random matrix
    private static int[][] generateRandomMatrix(int size, int maxValue) {
        int[][] arr = new int[size][size];
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                arr[i][j] = generateRandomNumber(maxValue);
            }
        }
        return arr;
    }

    private static int[][] randomOnesMatrix() {
        return generateRandomMatrix(SIZE, 1);
    }

    // generate random Index
    private static Index randomIndex(int[][] arr) {
        int row, col;
        do {
            row = generateRandomNumber(SIZE - 1);
            col = generateRandomNumber(SIZE - 1);
        }
        while (arr[row][col] == 0);
        return new Index(row, col, arr[row][col]);
    }

    // generate random arr[Index]
    private static Index[] randomStartEndIndices(int[][] arr) {
        Index start = null;
        Index end = randomIndex(arr);
        do {
            start = randomIndex(arr);
        } while (start == end);
        return new Index[] {start, end};
    }

    /**
     * this function generate Matrix Clients
     * and give each client to performed is tasks
     * @throws IOException - if the Matrix Client couldn't create connection with server
     */
    private static void runClient() throws IOException {
        int[][] arr2d = randomOnesMatrix();
        MatrixClient c1 = new MatrixClient(ipAddr, port, arr2d);
        c1.sendMatrix();
        c1.sendOnes();
        c1.getNumOfSubs();
        c1.getShortestPath(randomStartEndIndices(arr2d));
        c1.getEasiestPath(randomStartEndIndices(arr2d));
    }

    public static void main(String[] args) throws IOException {
        // create new server
        TcpServer server = new TcpServer(port);

        ThreadPoolExecutor tasksThreadPool = new ThreadPoolExecutor(10, 20, 2,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        // command the server to start running
        server.run(() -> new MatrixHandler(tasksThreadPool));

        // creat threadPool for giving each client a different thread
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 20, 2,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        // create one Matrix client with hard coded matrix for introduce the Dijextra algorithm
        MatrixClient c1 = new MatrixClient(ipAddr, port, originalMatrix1);
        c1.sendMatrix();
        c1.getNumOfSubs();
        c1.getShortestPath(indices2);
        c1.getEasiestPath(indices2);

        int numClients = 2;

        // generate 2 Matrix Clients
        for(int i=0;i<numClients;i++ ){
            threadPool.execute(()-> {
                try {
                    runClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        // shutting down threadPool
        threadPool.shutdown();
        // wait for the threadPool to be terminated
        while (!threadPool.isTerminated()) ;
        // shutting down the server
        server.stop();
    }

}
