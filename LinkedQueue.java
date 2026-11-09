import java.util.NoSuchElementException;

/**
 * A bounded FIFO queue using singly linked nodes.
 *
 * @author Ryan Gilligan
 * @version 2026.11.08
 * @param <T> the type of element stored
 */
public class LinkedQueue<T>
{
    private Node<T> head;
    private Node<T> tail;
    private int size;
    private final int capacity;

    /**
     * Creates an empty queue.
     *
     * @param capacity maximum number of elements
     * @throws IllegalArgumentException if capacity is not positive
     */
    public LinkedQueue(int capacity)
    {
        if (capacity <= 0)
        {
            throw new IllegalArgumentException(
                "Capacity must be positive.");
        }

        this.capacity = capacity;
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Adds an element to the back.
     *
     * @param item element to add
     * @throws IllegalArgumentException if item is null
     * @throws IllegalStateException if the queue is full
     */
    public void enqueue(T item)
    {
        if (item == null)
        {
            throw new IllegalArgumentException(
                "Cannot enqueue null.");
        }

        if (size == capacity)
        {
            throw new IllegalStateException("Queue is full.");
        }

        Node<T> newNode = new Node<T>(item);

        if (isEmpty())
        {
            head = newNode;
        }
        else
        {
            tail.next = newNode;
        }

        tail = newNode;
        size++;
    }

}

