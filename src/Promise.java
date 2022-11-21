/**
 * A class that wraps a value in a thread-safe manner,
 * The value can be calculated in one thread, and requested in another one,
 * If the value has been called before it was calculated, the thread will be blocked
 * until setValue is called.
 *
 * @param <T> The type of the value
 */
public class Promise<T> {

    private final Object syncRoot = new Object();
    private T value;

    /**
     * Gets the value of the promise if exists,
     * else, blocks the thread until the value will exist
     * @throws InterruptedException If the thread has been interrupted while waiting for the value.
     */
    public T getValue() throws InterruptedException {
        if (value != null) {
            return value;
        }

        synchronized (syncRoot) {
            if (value == null) {
                syncRoot.wait();
            }

            return value;
        }
    }

    /**
     * Sets the value of the promise and "unlock" all the threads which are waiting
     * for the value in getValue.
     * @param value the value to set
     */
    public void setValue(T value) {
        synchronized (syncRoot) {
            this.value = value;
            syncRoot.notifyAll();
        }
    }
}
