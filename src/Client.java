/**
         * Client wishes to initiate a connection with a remote/local server
         * We need:
         * 1. address of the server
         * 2. port
         *
         * There are 2 kinds of sockets:
         * ServerSocket- listens and accepts connections
         * Operational socket (client socket) - 2 way pipeline to read/write messages
         */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * client is an abstract class that create socket between client to server
 * and also create Object Output Stream and ObjectInputStream
 * also giving each client an id
 *
 * this class also know to receive object from handler
 * this class also know to send object to handler
 *
 * this class send and receive objects using Object Output Stream and Object Input Stream
 *
 * this class also printing info message and error message to screen
 */
public abstract class Client {

    String id;
    Socket socket;
    ObjectOutputStream toServer;
    ObjectInputStream fromServer;

    public Client(String ipAddr, int port) throws IOException {
        this.socket = new Socket(ipAddr, port);
        this.id = "Client " + this.socket.getLocalSocketAddress().toString().split(":")[1];
        this.toServer = new ObjectOutputStream(socket.getOutputStream());
        this.fromServer = new ObjectInputStream(socket.getInputStream());
    }

    protected void send(Object object) {
        this.send(object, null);
    }

    /**
     *
     * @param object - this function getting some object
     * @param description - this function getting some String description
     *
     * this function sending the object that she received to the handler
     * using object output stream
     */
    protected void send(Object object, String description) {
        if (description == null) {
            description = "data";
        }
        try {
            this.toServer.writeObject(object);
        } catch (IOException e) {
            this.err("Failed to send " + description + " to server");
        }
    }

    /**
     *
     * @return - receiving object from server using object input stream and return it to handler
     */
    protected Object receive() {
        try {
            Object response = this.fromServer.readObject();
            this.info("Received response from server");
            return response;
        } catch (IOException | ClassNotFoundException e) {
            this.err("Failed to receive response from server");
        }
        return null;
    }

    protected void info(String msg) {
        System.out.println(this.id + ": " + msg);
    }

    protected void err(String msg) {
        System.err.println(this.id + ": " + msg);
    }

}

