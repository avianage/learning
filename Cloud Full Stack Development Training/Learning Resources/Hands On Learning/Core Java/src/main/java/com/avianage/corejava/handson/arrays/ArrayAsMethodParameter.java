package com.avianage.corejava.handson.arrays;

import java.util.Arrays;

public class ArrayAsMethodParameter {
    // Array as a Method Parameters and Return Types
    // Array as parameter
    public static double calculateTotal(double[] salaries) {
        double total = 0;
        for (double s : salaries) total += s;
        return total;
    }

    // Array as return type
    public static double[] applyRaise(double[] salaries, double percent) {
        double[] raised = new double[salaries.length];
        for (int i = 0; i < salaries.length; i++) {
            raised[i] = salaries[i] * (1 + percent / 100);
        }
        return raised;
    }

    public static void doubleAll(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= 2;       // modifies the original!
        }
    }


    public static void main(String[] args) {
        double[] salaries = {75000, 82000, 55000, 91000, 68000};

        System.out.println("Total: " + calculateTotal(salaries));

        double[] raised = applyRaise(salaries, 10);
        System.out.println("After 10% raise: " + Arrays.toString(raised));

        // Important: Arrays are passed by reference — the method receives the address of the array,
        // not a copy.
        // Modifying elements inside a method changes the original array:

        int[] data = {1, 2, 3};
        doubleAll(data);
        System.out.println(Arrays.toString(data));   // [2, 4, 6] — original changed


    }





}

