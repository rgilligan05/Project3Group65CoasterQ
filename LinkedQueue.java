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
/**
     * Removes and returns the front element.
     *
     * @return removed element
     * @throws NoSuchElementException if the queue is empty
     */
    public T dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty.");
        }

        T result = head.data;
        head = head.next;
        size--;

        if (isEmpty())
        {
            tail = null;
        }

        return result;
    }

    /**
     * Returns the front element without removing it.
     *
     * @return front element
     * @throws NoSuchElementException if the queue is empty
     */
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty.");
        }

        return head.data;
    }

    /**
     * Checks whether the queue is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return size == 0;
    }

    /**
     * Returns the number of stored elements.
     *
     * @return queue size
     */
    public int size()
    {
        return size;
    }

    /**
     * An internal singly linked node.
     *
     * @param <T> the stored element type
     */
    private static class Node<T>
    {
        private final T data;
        private Node<T> next;

        /**
         * Creates a node.
         *
         * @param data element to store
         */
        private Node(T data)
        {
            this.data = data;
            next = null;
        }
    }
}

