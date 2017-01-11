/**
 * (c) 2016-2017 Emil Tullstedt <emiltu(a)kth.se>
 */
package is.mjuk.fish;

import java.lang.NumberFormatException;

/**
 * Static functions and constants that can be used for convenience
 */
public class Helpers {
    /*
     * The ANSI codes are stolen from Stack Overflow
     * http://stackoverflow.com/questions/5762491/
     */
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    /**
     * This class contains only static functions
     */
    private Helpers() {
    }

    /**
     * Gets the value at a specific position in an array or a default value
     *
     * @param array Target array
     * @param pos Position in the array to look for the value
     * @param default_value Return value iff there are &lt;= pos elements in
     * array
     * @return The string at the given position in the array, or if the array
     * contains fewer elements than the element points out default_value
     */
    public static String get(String[] array, int pos, String default_value) {
        if (array.length <= pos) {
            return default_value;
        }
        return array[pos];
    }

    /**
     * Pretty printer for error messages
     * <p>
     * Prints an error message to stderr with hints of expected result and
     * achieved result
     *
     * @param title General description of the error
     * @param expected Hint for what was expected
     * @param got Explanation of what appeared
     */
    public static void print_err(String title, String expected, String got) {
        System.err.format(Helpers.RED + "[ERROR] " + Helpers.RESET + "%s\n",
            title); 
        System.err.format("\tExpected: %s\n", expected);
        System.err.format("\tGot: " + Helpers.YELLOW + "%s\n" + Helpers.RESET, 
            got);
    }

    /**
     * Pretty printer for error messages
     * <p>
     * Prints an error message to stderr with details
     * 
     * @param title General description of the error
     * @param details Detailed information about the error
     */
    public static void print_err(String title, String details) {
        System.err.format(Helpers.RED + "[ERROR] " + Helpers.RESET + "%s\n",
            title); 
        System.err.format("\tDetails: " + Helpers.YELLOW + "%s\n" + Helpers.RESET, 
            details);
    }
}
