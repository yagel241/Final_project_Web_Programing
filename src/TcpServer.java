import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TcpServer {
    /*
    Motivation: 2 running programs on remote hosts to communicate with each other over a network network connection.
    How is it done?
    Application layer only translate packets/data streams to meaningful data. How is the data sent?
    Transport layer:
    End-to-end connection between hosts for applications
    It provides services like:
    connection-oriented communication, Data integrity and error correction, flow control and congestion control.

    Application layer:
    Process to process delivery of data - an instance of a running program is represented by a process.
    Application layer "thinks" that it communicates with another process running on the local machine

    Transport layer - in charge of multiplexing. The ability to transport different kinds of data on the communication line
    enables sending streams of data/packets from various applications simultaneously over a network.

    A user enter in the address bar in a browser: http://www.ynet.co.il
    1. We want to send an HTTP GET request - in order ynet's homepage. HTTP is an application layer protocol
    2. In order to communicate with another machine over the internet - we need its IP address.
    3. The browser checks if an associated IP address is available in one of 3 caches:
    - Browser cache
    - OS cache
    - Default gateway cache - Local router
    4. If no address is available in cache, we need to send an A-Record request to a DNS server.
    5. Browser needs to send request to a DNS server in the application layer.
    6. Browser needs the services of the transport layer. DNS request in application layer needs either UDP/TCP protocol services
    7. Browser needs to ask the OS to open a UDP socket.
    8. Only after UDP socket is created, a DNS request can be sent over the application layer
    9. DNS server sent the response, i.e., the IP address of ynet webserver.
    10. Now we know "where" to send the HTTP GET request
    11. HTTP protocol uses TCP in transport layer - browser now asks the OS to open a TCP socket.
    12. Now the browser can send the HTTP GET request

    A socket os an endpoint to communicate between 2 machines.
    An abstraction for a 2-way data pipeline between 2 machines.
    - Server socket - listens and accepts incoming connections.
    - Operational socket (Client socket) - read/write to a data stream

    Each socket is associated with a transport protocol (TCP/UDP) and information such as the IP Address and Port number
    Port is used to differ between kinds of data transmitted over the same network connection
     */

    private final int port; // port associated with the server
    private boolean stopServer;  // if the server should continue serving clients
    private ThreadPoolExecutor threadPool; // handle multiple clients concurrently
    private Supplier<IHandler> requestHandlerFactory; // setting every client with its own handler

    // constructor
    public TcpServer(int port) {
        this.port = port;
        this.stopServer = false;
        threadPool = null;
        requestHandlerFactory = null;
    }

    /**
     *
     * @param concreteHandlerFactory - the specific handler we about to use
     *
     * starts the server in a new thread
     * waiting for a client to connect to the server
     * after the client is connected to the server
     * the threadPool gives the client thread to run on and manage the clients
     *
     */
    public void run(Supplier<IHandler> concreteHandlerFactory) {
        this.requestHandlerFactory = concreteHandlerFactory;

        new Thread(() -> {
            threadPool = new ThreadPoolExecutor(10, 20, 10,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            try {
                /*
                 if no port is specified - one will be automatically allocated by OS
                 backlog parameter- number of maximum pending requests
                 ServerSocket constructor - socket creation + bind to a specific port
                 Server Socket API:
                 1. create socket
                 2. bind to a specific port number
                 3. listen for incoming connections (a client initiates a tcp connection with server)
                 4. try to accept (if 3-way handshake is successful)
                 5. return operational socket
                 */
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Server: Waiting for clients");
                while (!stopServer) {
                    Socket serverToSpecificClient = serverSocket.accept(); // 2 operations: listen()+accept()
                    System.out.println("Server: Accepted connection with client " +
                            serverToSpecificClient.getRemoteSocketAddress().toString().split(":")[1]);
                /*
                 server will handle each client in a separate thread
                 define every client as a Runnable task to execute
                 */
                    threadPool.execute(() -> {
                        try {
                            requestHandlerFactory.get().handle(
                                    serverToSpecificClient.getRemoteSocketAddress().toString().split(":")[1],
                                    serverToSpecificClient.getInputStream(),
                                    serverToSpecificClient.getOutputStream());
                            // finished handling client. now close all streams
                            serverToSpecificClient.getInputStream().close();
                            serverToSpecificClient.getOutputStream().close();
                            serverToSpecificClient.close();
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println(e.getMessage());
                        }
                    });
                }
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        if (!stopServer) {
            stopServer = true;
            if (threadPool != null) { // avoid situation that someone stopped the server
                // without ever invoking run method
                threadPool.shutdown();
                System.out.println("Server: Stopped listening to new requests");
                System.exit(0);
            }
        }
    }
}