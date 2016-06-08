package ca.marklauman.dominionpicker.shuffler;

import java.util.ArrayList;
import java.util.Random;

/** Manages a pool of raffle entrants holding differing amounts of "tickets" per entrant.
 *  Internally, this structure is stored as a binary tree with differing amounts of tickets
 *  attached to each node.
 *
 *  <p>Entrants can be added to the pool with {@link #add(int, Object)} and removed with
 *  {@link #remove(T)}. A random entrant can be drawn (and removed from the pool) with
 *  {@link #draw()}.</p>
 *
 *  @author Mark Lauman */
@SuppressWarnings("unused")
public class RafflePool<T> {

    /** Random instance used to draw items. */
    private final Random random = new Random();
    /** The value attached to each entrant in the raffle. */
    private final ArrayList<T> values;
    /** The number of tickets given to each entrant. */
    private final ArrayList<Integer> tickets;
    /** The sum of all tickets owned by left-children of each entrant. */
    private final ArrayList<Integer> ticketSums;


    ////////////////////////////////////////////////////////////////////
    //                Basic Methods (Nothing complex)                 //
    ////////////////////////////////////////////////////////////////////
    /** Create a RafflePool of unknown size.
     *  {@link #RafflePool(int)} is preferred, as resizing the pool is computationally expensive.
     *  For mass-resizing, call {@link #ensureCapacity(int)}*/
    public RafflePool() {
        values = new ArrayList<>();
        tickets = new ArrayList<>();
        ticketSums = new ArrayList<>();
    }

    /** Create a RafflePool of known size. The pool can grow beyond this size later,
     *  but resizing the pool is an expensive operation.
     *  @param numEntrants The expected number of entrants into the raffle.
     *                     (Not the amount of tickets they own) */
    public RafflePool(int numEntrants) {
        values = new ArrayList<>(numEntrants);
        tickets = new ArrayList<>(numEntrants);
        ticketSums = new ArrayList<>(numEntrants);
    }

    /** Ensures that after this operation the RafflePool can hold the specified
     *  number of entrants without growing further.
     *  @param minimumCapacity  The minimum capacity asked for. */
    public void ensureCapacity(int minimumCapacity) {
        values.ensureCapacity(minimumCapacity);
        tickets.ensureCapacity(minimumCapacity);
        ticketSums.ensureCapacity(minimumCapacity);
    }

    /** Clears the RafflePool, allowing new entries to be added. */
    public void clear() {
        values.clear();
        tickets.clear();
        ticketSums.clear();
    }

    /** Get the parent of the entry at the given position. */
    private static int getParent(int entry) {
        return (entry-1) / 2;
    }

    /** Get the left child of this entry */
    private int getLeftChild(int entry) {
        return 2*entry + 1;
    }

    /** Get the right child of this entry */
    private int getRightChild(int entry) {
        return 2*entry + 2;
    }

    /** Check if {@code child} is the left child of {@code parent}. */
    private boolean isLeftChild(int parent, int child) {
        return child == getLeftChild(parent);
    }

    /** Check if {@code child} is the right child of {@code parent}. */
    private boolean isRightChild(int parent, int child) {
        return child == getRightChild(parent);
    }

    /** Get the total number of entries in this pool. */
    public int numEntries() {
        return values.size();
    }

    /** Get the total amount of tickets in the pool. */
    public int numTickets() {
        if(numEntries() == 0) return 0;
        int position = 0, sum = 0;
        do {
            sum += ticketSums.get(position) + tickets.get(position);
            position = getRightChild(position);
        } while(position < numEntries());
        return sum;
    }

    /** Draw an entrant from the pool at random, then remove it from the pool.
     *  @return The drawn entry. */
    public T draw() {
        if(numEntries() == 0) return null;
        if(numEntries() == 1) return removeEntry(0);
        return removeEntry(findEntry(random.nextInt(numTickets())));
    }

    /** Draw an entrant from the pool at random, then remove it from the pool.
     *  @param verbose Set to true to make the drawn ticket be displayed.
     *  @return The drawn entry. */
    public T draw(boolean verbose) {
        if(numEntries() == 0) return null;
        if(numEntries() == 1) return removeEntry(0);
        int ticket = random.nextInt(numTickets());
        if(verbose) System.out.print(ticket);
        return removeEntry(findEntry(ticket));
    }


    /** Remove the first entrant tied to this value.
     *  This is expensive, and should be avoided where possible. */
    public boolean remove(T value) {
        int entry = findEntry(value);
        boolean found = entry != -1;
        if(found) removeEntry(entry);
        return found;
    }


    @Override
    public String toString() {
        return "RafflePool{values  "+values
            +"\n           tickets "+tickets
            +"\n           sum     "+ticketSums+"}";
    }


    ////////////////////////////////////////////////////////////////////
    //                        Complex Methods                         //
    ////////////////////////////////////////////////////////////////////
    /** Add an entrant to the raffle, paired to the given value. If you call this method twice
     *  with the same value, then each call will be treated as a different entrant in the draw.
     *  @param numTickets Number of raffle tickets for this entrant.
     *  @param value Value to be returned when this entrant is drawn. */
    public void add(int numTickets, T value) {
        if(numTickets < 1)
            throw new IllegalArgumentException("numTickets must be a positive integer bigger than 0");

        // Add the node
        int position = numEntries();
        values.add(value);
        tickets.add(numTickets);
        ticketSums.add(0);
        updateParentSums(position, numTickets);
    }


    /** Update the ticketSum for all the parents of {@param position}.
     * @param position The position of the node that has been changed.
     * @param ticketDiff The difference between the old number of tickets for this entry
     *                   and the new number of tickets for this entry. */
    private void updateParentSums(int position, int ticketDiff) {
        if(position == 0 || ticketDiff == 0) return;

        int child = position;
        int parent = getParent(child);
        while(parent != 0) {
            if(isLeftChild(parent, child))
                ticketSums.set(parent, ticketDiff+ticketSums.get(parent));
            child = parent;
            parent = getParent(child);
        }
        if(isLeftChild(0, child))
            ticketSums.set(0, ticketDiff+ticketSums.get(0));
    }


    /** Get the total number of tickets owned by entries before this one.
     *  This method is the inverse of {@link #findEntry(int)}.
     *  @param position The position of the entry in the pool. */
    private int getSum(int position) {
        if(position == 0) return ticketSums.get(0);
        if(numEntries() <= position)
            throw new IndexOutOfBoundsException("This item is outside of the pool");

        int sum = ticketSums.get(position);
        while(0 < position) {
            int parent = getParent(position);
            if(isRightChild(parent, position))
                sum += ticketSums.get(parent) + tickets.get(parent);
            position = parent;
        }
        return sum;
    }


    /** Find the first entry with {@param findSum} tickets before it.
     *  This method is the inverse of {@link #getSum(int)}.
     *  @param findSum The sum of all entries before this one.
     *  @return The position of that entry in the pool, or -1 if there is no pool left. */
    private int findEntry(int findSum) {
        int sum = 0, pos = 0;
        while(pos < numEntries()) {
            // The total number of tickets before this node
            int nodeSum = sum + ticketSums.get(pos);
            // The number of tickets owned by this node
            int nodeTickets = tickets.get(pos);

            // Iterate based off of the contents of this node
            if(findSum < nodeSum)                  // This node has a larger sum
                pos = getLeftChild(pos);
            else if(findSum < nodeSum+nodeTickets) // This node matches our sum
                return pos;
            else {                                 // This node has a smaller sum
                sum = nodeSum + nodeTickets;
                pos = getRightChild(pos);
            }
        }
        throw new InternalError("FindEntry exceeded the bounds of the pool");
    }


    /** Find the first entry with the given value in the RafflePool.
     *  @return The position of that entry in the pool, or -1 for no entry found. */
    private int findEntry(T findValue) {
        if(findValue == null) return findNullEntry();
        int pos = 0;
        for(T value : values) {
            if(findValue == value || findValue.equals(value))
                return pos;
            pos++;
        }
        return -1;
    }


    /** Find the first entry with a null value in the RafflePool.
     *  @return The position of that entry in the pool, or -1 for no entry found. */
    private int findNullEntry() {
        int pos = 0;
        for(T value : values) {
            if(value == null)
                return pos;
            pos++;
        }
        return -1;
    }


    /** Removes the entry at the given position.
     *  @param position Position of the entry in the RafflePool.
     *  @return The value bound to this entry */
    private T removeEntry(int position) {
        final int lastItem = numEntries()-1;
        if(lastItem == -1) return null;
        final T value = values.get(position);

        // If the list has only 1 item, no sum updates are necessary
        if(lastItem == 0) {
            values.remove(0);
            tickets.remove(0);
            ticketSums.remove(0);
            return value;
        }

        // Swap the last item in the raffle pool into this position (less updates that way)

        // Update the parents of each node
        final int lastTickets = tickets.get(lastItem);
        updateParentSums(position, lastTickets - tickets.get(position));
        updateParentSums(lastItem, -lastTickets);

        // Swap the last node into this one
        values.set(position, values.get(lastItem));
        tickets.set(position, tickets.get(lastItem));
        // ticketSum is already correct, no need to overwrite

        // Remove the last node
        values.remove(lastItem);
        tickets.remove(lastItem);
        ticketSums.remove(lastItem);

        // Lastly, return the value
        return value;
    }
}