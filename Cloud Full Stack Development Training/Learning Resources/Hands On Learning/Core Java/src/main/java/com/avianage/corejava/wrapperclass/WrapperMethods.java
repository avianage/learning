package com.avianage.corejava.wrapperclass;

public class WrapperMethods {
    static void main(String[] args) {
        // Integer

        int a = 10;
        int b = 20;
        Integer.parseInt("42");            // String → int: 42
        Integer.valueOf(42);               // int → Integer
        Integer.toString(42);              // int → String: "42"
        Integer.toBinaryString(42);        // "101010"
        Integer.toHexString(42);           // "2a"
        Integer.toOctalString(42);         // "52"
        int maxValue = Integer.MAX_VALUE;    // 2147483647
        int minValue = Integer.MIN_VALUE;    // -2147483648
        Integer.compare(a, b);               // compare two ints: negative, 0, or positive
        Integer.max(a, b);                   // larger of two ints
        Integer.min(a, b);                   // smaller of two ints
        Integer.sum(a, b);                   // sum (useful as method reference)
        Integer.bitCount(42);             // number of 1-bits: 3

        // Double
        double result = 100.0;
        Double.parseDouble("3.14");        // String → double
        Double.isNaN(result);                 // true if result is Not-a-Number
        Double.isInfinite(result);            // true if division by zero etc.
        double maxVal = Double.MAX_VALUE;     // largest double value
        double minVal = Double.MIN_VALUE;     // smallest positive double value (not most negative)
        Double.compare(a, b);                 // compare two doubles

        // Characters
        Character.isLetter('A');              // true
        Character.isDigit('5');              // true
        Character.isWhitespace(' ');          // true
        Character.isUpperCase('A');           // true
        Character.isLowerCase('a');           // true
        Character.toUpperCase('a');           // 'A'
        Character.toLowerCase('A');           // 'a'
        Character.isAlphabetic('Z');    // true

        // Boolean
        Boolean.parseBoolean("true");         // true
        Boolean.parseBoolean("TRUE");        // true (case-insensitive)
        Boolean.parseBoolean("yes");          // false — only "true" (any case) returns true
        Boolean.toString(true);               // "true"
    }
}
