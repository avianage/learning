package com.avianage.corejava.handson.flowcontrol;

import java.util.Scanner;

public class Loops {
    static void main() {
        // While Loop

        // Print active employees from a list (simulated with index)
        int index = 0;
        int totalEmployees = 5;

        while (index < totalEmployees) {
            System.out.println("Processing employee #" + (index + 1));
            index++;
        }

        // Example
        // Classic use: keep prompting until valid input
        Scanner scanner = new Scanner(System.in);
        int salary = -1;

        while (salary <= 0) {
            System.out.print("Enter salary (must be positive): ");
            salary = scanner.nextInt();
        }
        System.out.println("Salary accepted: " + salary);

        // Do while loop
        int attempts = 0;

        do {
            System.out.println("Attempt " + (attempts + 1) + ": validating employee data...");
            attempts++;
        } while (attempts < 3);

        // Output:
        // Attempt 1: validating employee data...
        // Attempt 2: validating employee data...
        // Attempt 3: validating employee data...

        // For Loop;
        // syntax: for (initialization; condition; update)
        for (int i = 1; i <= 5; i++) {
            System.out.println("Employee #" + i);
        }

        // All three parts are optional:
        int i = 0;
        for (; i < 5; ) {    // init and update moved outside
            System.out.println(i);
            i++;
        }

        // Infinite loop
        //for (;;) {
            // runs forever — needs a break to exit
        //}

        // For Each Loop
        String[] employees3 = new String[] {"Sonu", "Monu"};
        for (String name : employees3) {
            System.out.println("Hello, " + name);
        }

        double[] salaries = {75000, 82000, 55000, 91000, 68000};
        double   total    = 0;

        for (double salary1 : salaries) {
            total += salary1;
        }
        System.out.println("Total payroll: " + total);   // 371000.0

        // Nested Loops:
        // Generate a simple employee-project assignment matrix
        String[] employees2 = {"Sonu", "Monu", "Tonu"};
        String[] projects  = {"Alpha", "Beta", "Gamma"};

        System.out.printf("%-10s", "");
        for (String project : projects) {
            System.out.printf("%-10s", project);
        }
        System.out.println();

        for (String emp : employees2) {
            System.out.printf("%-10s", emp);
            for (String project : projects) {
                System.out.printf("%-10s", "Yes");   // all assigned for demo
            }
            System.out.println();
        }

        // Choosing Right Loop
        /*
        Situation	                            Best Choice
        Known count / index needed	            for
        Iterating elements, no index needed	    for-each
        Loop until condition is met	            while
        Must run at least once	                do-while
        Need to exit early on a condition	    Any loop + break
        Need to skip some iterations	        Any loop + continue
        */

    }
}
