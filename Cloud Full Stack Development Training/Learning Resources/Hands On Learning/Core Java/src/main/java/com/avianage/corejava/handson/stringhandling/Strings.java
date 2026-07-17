package com.avianage.corejava.handson.stringhandling;

public class Strings {
    static void main(String[] args) {
        /*
        Strings in Java
        A String is a sequence of characters. In Java, String is a class — not a
        primitive — but it gets special treatment that makes it feel like one.

        String name2 = "Sonu";   // looks like a primitive assignment
        Under the hood, "Sonu" is an object of class java.lang.String,
        and the variable name2 holds a reference to it.
        */

        // Creating Strings
        String name       = "Sonu";
        String department = "Engineering";
        String empty      = "";               // empty string — not null

        // Using new (uncommon)
        String name1 = new String("Sonu");   // explicitly creates a new object in heap

        // Important: Strings are immutable
        String name2 = "Sonu";
        name2 = name2 + " Sharma";

        System.out.println(name2);   // Sonu Sharma

        /*
        Looks like the string changed. It did not. What happened:

        "Sonu" object created in memory
        "Sonu" + " Sharma" created a new object "Sonu Sharma" in memory
        name now points to the new object
        The original "Sonu" is unchanged — it will eventually be garbage collected

        Why immutability?

        Security: String is widely used for passwords, file paths, network addresses.
                  If it were mutable, one piece of code could change a string that
                  another piece of code is holding.
        Thread safety: Immutable objects are automatically safe for concurrent access.
        String pool: Enables sharing of string objects, saving memory.
         */

        // String Pool
        String a = "Engineering";
        String b = "Engineering";
        String c = new String("Engineering");   // forced new object on heap

        System.out.println(a == b);              // true  — same pool object
        System.out.println(a == c);              // false — c is a different heap object
        System.out.println(a.equals(c));         // true  — same content

        // Rule: Use .equals() to compare String content. Never use == for Strings.


    }
}
