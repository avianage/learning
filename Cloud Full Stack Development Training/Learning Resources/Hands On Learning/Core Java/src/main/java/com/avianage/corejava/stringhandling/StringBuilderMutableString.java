package com.avianage.corejava.stringhandling;

public class StringBuilderMutableString {
    static void main(String[] args) {
        //Every + operation on strings creates a new object.
        // In a loop, this creates many short-lived objects:

        // Bad — creates 1000 String objects
        String result = "";
        for (int i = 0; i < 1000; i++) {
            result += i;   // new String object each time
        }

        // Good — modifies a single StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(i);
        }
        String result1 = sb.toString();

        // StringBuilder Methods
        StringBuilder sb1 = new StringBuilder("Employee: ");

        sb1.append("Sonu");                  // Employee: Sonu
        sb1.append(", ID: ").append(101);    // Employee: Sonu, ID: 101 — chaining
        sb1.insert(0, ">>> ");               // >>> Employee: Sonu, ID: 101
        sb1.delete(0, 4);                    // Employee: Sonu, ID: 101
        sb1.replace(0, 8, "Staff");          // Staff: Sonu, ID: 101
        sb1.reverse();                       // 101 :DI ,unoS :ffatS

        StringBuilder sb2 = new StringBuilder("Hello");
        sb2.deleteCharAt(0);                // ello
        sb2.setCharAt(0, 'Y');              // Yllo

        System.out.println(sb2.length());       // 4
        System.out.println(sb2.charAt(1));      // 'l'
        System.out.println(sb2.indexOf("l"));   // 1
        System.out.println(sb2.toString());     // Yllo

        /*
        StringBuffer — Thread-Safe StringBuilder

        StringBuffer is identical to StringBuilder but all its
        methods are synchronized (thread-safe).
        Class 	    Mutable 	Thread-Safe 	    Performance
        String 	        No 	    Yes (immutable) 	Fast for reads, slow for repeated modification
        StringBuilder 	Yes 	No 	                Fastest for single-threaded string building
        StringBuffer 	Yes,    Yes 	            Slightly slower than StringBuilder due to synchronization

        Use:
        String — for values that don't change
        StringBuilder — for building strings in single-threaded code (default choice)
        StringBuffer — only when multiple threads access the same mutable string (rare)
        */



    }
}
