/**
 * Main class for the FISH file sharing toolkit for ID2212
 *
 * (c) 2016 Emil Tullstedt <emiltu(a)kth.se>
 */
package is.mjuk.fish;

import java.util.function.Function;

public class Fish {
    public static void main(String[] argv) {
        Function<Integer, Integer> sq = x -> x * x;
        int x = sq.apply(3);
        System.out.println(x);
    }
}
