package ca.marklauman.dominionpicker.test;

import org.junit.Test;

import ca.marklauman.dominionpicker.shuffler.RafflePool;

/** Class devoted to testing {@link RafflePool} and ensuring it works to standard.
 *  @author Mark Lauman */
public class RaffleTest {

    private static void println(Object object) {
        System.out.println(object);
    }

    private static void print(Object object) {
        System.out.print(object);
    }

    private static void fillPool(RafflePool<Long> pool, int maxValue) {
        for(int i=1; i<=maxValue; i++)
            pool.add(i, (long)i);
    }

    @Test
    public void basicTest() {
        println("Basic Pool:");
        RafflePool<Long> pool = new RafflePool<>(15);
        println(pool);

        println("With 3 values:");
        fillPool(pool, 3);
        println(pool);
        println("tickets="+pool.numTickets());

        println("Clear the pool:");
        pool.clear();
        println(pool);

        println("Attempt to draw:");
        println("drawRes="+pool.draw());

        println("Attempt to draw with just root:");
        pool.add(1, 1L);
        println("drawRes="+pool.draw());
        println("drawRes="+pool.draw());

        println("\n==========================");
        println(  "     Removal Tests");
        println(  "==========================");
        fillPool(pool, 15);
        println(pool);
        println("tickets="+pool.numTickets());
        println("Remove 9: "+pool.remove(9L));
        println(pool);
        println("tickets="+pool.numTickets());
        println("Draw: "+pool.draw()+", "+pool.draw()+", "+pool.draw()+", "
                        +pool.draw()+", "+pool.draw());
        println(pool);
        println("tickets="+pool.numTickets());
        println("Remove 9: "+pool.remove(9L));
        println(pool);
        print("Remove:");
        for(int i=0; i<10; i++)
            print(" "+pool.draw());
        println("\n"+pool);
    }

    @Test
    public void randomTest() {
        RafflePool<Long> pool = new RafflePool<>(15);
        for(int i=0; i<10; i++) {
            fillPool(pool, 15);
            print("Draw: ");
            for(int v=0; v<16; v++) {
                Long draw = pool.draw(true);
                print(","+draw+" ");
            }
            println("");
        }
        println("");
        for(int i=0; i<10; i++) {
            fillPool(pool, 15);
            print("Draw: ");
            for(int v=0; v<16; v++) {
                Long draw = pool.draw();
                if(draw == null) print(" null");
                else if(draw < 10) print("  "+draw);
                else print(" "+draw);
            }
            println("");
        }
    }
}