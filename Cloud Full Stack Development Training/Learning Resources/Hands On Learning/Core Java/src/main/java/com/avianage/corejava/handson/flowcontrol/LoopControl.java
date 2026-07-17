package com.avianage.corejava.handson.flowcontrol;

public class LoopControl {

    static void main(String[] args) {
        // Break
        var employees1 = new String[]{"Sonu", "Monu", "TERMINATED", "Ponu", "Gonu"};

        for (String name : employees1) {
            if (name.equals("TERMINATED")) {
                System.out.println("Invalid record found — stopping.");
                break;
            }
            System.out.println("Processing: " + name);
        }

        // Output:
        // Processing: Sonu
        // Processing: Monu
        // Invalid record found — stopping.

        // Continue

        double[] salaries = {75000, -1, 82000, -1, 55000};   // -1 means data missing

        for (double salary : salaries) {
            if (salary < 0) {
                System.out.println("Skipping invalid salary record.");
                continue;
            }
            System.out.println("Valid salary: " + salary);
        }

        // Output:
        // Valid salary: 75000.0
        // Skipping invalid salary record.
        // Valid salary: 82000.0
        // Skipping invalid salary record.
        // Valid salary: 55000.0

        // Labeled Break and Continue
        String[] departments = {"Engineering", "HR", "Finance"};
        String[] employees   = {"Sonu", "Monu", "TARGET", "Ponu"};

        outer:
        for (String dept : departments) {
            for (String emp : employees) {
                if (emp.equals("TARGET")) {
                    System.out.println("Found in " + dept + " — stopping all loops.");
                    break outer;    // breaks out of the outer loop, not just inner
                }
                System.out.println(dept + " — " + emp);
            }
        }
    }
}
