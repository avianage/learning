package com.avianage.corejava.dtypes;

public class TypeConversion {
    static void main(String[] args) {
        /*
        Type Conversion

         Widening (Implicit / Automatic)
         byte -> short -> int -> long -> float -> double
                                   ^
                                   |
                                  char
        */
        int employeeId = 1001;
        long id = employeeId;
        double salary = employeeId;

        System.out.println(id);
        System.out.println(salary);

        // Narrowing (Explicit / Manual)
        double salary1 = 75999.99;
        int routed = (int) salary1;
        System.out.println(routed);

        long bigInt = 9999999999L;
        int smallId = (int) bigInt;

        // Truncation - Not Rounding Off;
        double d = 9.99;
        int i = (int) d;
        System.out.println(i); // 9, not 10 - decimal part is dropped

        // Type Promotion in Expression
        byte b1 = 10;
        byte b2 = 20;
        // byte b3 = b1 + b2;  // CE -> b1 + b2 produces an int, not a byte
        int b3 = b1 + b2;      // correct -> result in int;

        /*
        Rules:
        - Arithmatic involving byte or short -> result in int;
        - Arithmatic involving long -> result in long;
        - Arithmatic involving float -> result in float;
        - Arithmativ involving double -> result in double;
         */

        int base = 50000;
        double factor = 1.15;
        double result = base * factor; // int * double -> double : automatic


    }
}
