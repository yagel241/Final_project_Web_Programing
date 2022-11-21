
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * this interface defines the functionality required for a Matrix Handler
 */

public interface IHandler {
    public abstract void handle(String clientId, InputStream fromClient,
                                OutputStream toClient) throws IOException, ClassNotFoundException;
    public abstract void resetMembers();
    public abstract void stop();
}
