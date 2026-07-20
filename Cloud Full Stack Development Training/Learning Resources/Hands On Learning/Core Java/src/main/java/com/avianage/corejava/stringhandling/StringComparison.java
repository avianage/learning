package com.avianage.corejava.stringhandling;

public class StringComparison {
    static void main(String[] args) {
        String a = "Sonu";
        String b = "Sonu";
        String c = new String("Sonu");

        // == compares references
        System.out.println(a == b);       // true  (same pool object)
        System.out.println(a == c);       // false (c is a new heap object)

        // .equals() compares content
        System.out.println(a.equals(b));  // true
        System.out.println(a.equals(c));  // true

        // .equalsIgnoreCase()
        System.out.println("SONU".equalsIgnoreCase("sonu"));   // true

        // compareTo — lexicographic comparison
        System.out.println("Apple".compareTo("Banana"));  // negative (A < B)
        System.out.println("Banana".compareTo("Apple"));  // positive (B > A)
        System.out.println("Apple".compareTo("Apple"));   // 0 (equal)
    }
}
