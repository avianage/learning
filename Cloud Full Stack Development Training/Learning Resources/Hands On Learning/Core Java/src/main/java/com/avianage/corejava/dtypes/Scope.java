package com.avianage.corejava.dtypes;

public class Scope {
    // A variable only exists within the block (pair of {}) where it is declared:

    public static void main(String[] args) {

        int employeeId = 101;        // exists for the rest of main

        if (employeeId > 100) {
            String status = "Active"; // exists only inside this if block
            System.out.println(status);
        }

        // System.out.println(status);   // compile error — status is out of scope here
        System.out.println(employeeId); // fine — employeeId is still in scope

        // var - Local Type Inference
        // Java 10 introduced var for local variables.
        // The compiler infers the type from the initializer:
        var name       = "Sonu";        // inferred as String
        var salary     = 75000.0;       // inferred as double
        var employeeId1 = 1001;          // inferred as int

        // Note: var is just syntactic sugar — the type is still fixed at compile time.
        // You cannot reassign a different type later.
        // var can only be used for local variables — not fields, parameters, or return types

    }


}
