package com.avianage.corejava.arrays;

public class IteratingOverArrays {
    static void main(String[] args) {
        // Using for loop
        String[] names    = {"Sonu", "Monu", "Tonu", "Ponu", "Gonu"};
        double[] salaries = {75000, 82000, 55000, 91000, 68000};

        for (int i = 0; i < names.length; i++) {
            System.out.printf("%-5s → %.2f%n", names[i], salaries[i]);
        }

        /*
        Sonu  → 75000.00
        Monu  → 82000.00
        Tonu  → 55000.00
        Ponu  → 91000.00
        Gonu  → 68000.00
         */

        // Using For Each
        double[] salaries1 = {75000, 82000, 55000, 91000, 68000};
        double total = 0;

        for (double salary : salaries1) {
            total += salary;
        }
        System.out.println("Total payroll: " + total);   // 371000.0


    }
}

