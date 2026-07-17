package com.avianage.corejava.handson.arrays;

public class Arrays {
    static void main(String[] args) {
        /*
        What Is an Array?
        An array is a fixed-size, ordered collection of elements of the same type.
        All elements live in contiguous memory, and each is accessed by
        its index (position), starting at 0.

        String[] employees:
        Index →  [0]     [1]     [2]     [3]     [4]
                 "Sonu"  "Monu"  "Tonu"  "Ponu"  "Gonu"

        Key properties:
        - Fixed size — once created, size cannot change
        - Zero-indexed — first element is at index 0, last is at length - 1
        - Homogeneous — all elements must be the same type
        - Objects on the heap — the array itself is an object even if it
          holds primitives
         */

        // Declaration

        int[] salaries;
        String[] names;

        // Creation - allocates memory on heap;
        salaries = new int[5];  // array of 5 ints, all initialised to 0;
        names = new String[5];  // array of 5 strings, all initialised to ""

        // Declaration
        double[] precentages = new double[4];   // initilised to 0.0;
        boolean[] flags = new boolean[3];       // initialized to false

        /*
        Default values after creation:

        Type                    	    Default Value
        int, byte, short, long	        0
        float, double	                0.0
        char	                        '\u0000' (null character)
        boolean	                        false
        Object types (String, etc.)	    null
         */

        // Initializing Arrays
        // Assigning elements individually
        int[] salaries1 = new int[3];
        salaries1[0] = 75000;
        salaries1[1] = 82000;
        salaries1[2] = 55000;

        // Declaration and values together
        // Concise syntax — size inferred from the values
        String[] names1    = {"Sonu", "Monu", "Tonu", "Ponu", "Gonu"};
        double[] salaries2 = {75000.0, 82000.0, 55000.0, 91000.0, 68000.0};
        int[]    ids      = {101, 102, 103, 104, 105};

        // Using New
        String[] departments = new String[]{"Engineering", "HR", "Finance"};

        // Anonymous Array
        System.out.println((new String[]{"Sonu", "Monu", "Tonu"}));   // anonymous array

        // Accessing and Modyfying Array Elements
        String[] employees = {"Sonu", "Monu", "Tonu", "Ponu", "Gonu"};

        // Read
        System.out.println(employees[0]);    // Sonu
        System.out.println(employees[4]);    // Gonu

        // Modify
        employees[2] = "Ronu";
        System.out.println(employees[2]);    // Ronu

        // Array length — property, not a method (no parentheses)
        System.out.println(employees.length);  // 5

        // OUT OF BOUND
        // System.out.println(employees[5]);   // ArrayIndexOutOfBoundsException — valid indices: 0–4
        // System.out.println(employees[-1]);  // ArrayIndexOutOfBoundsException — no negative indices


    }
}
