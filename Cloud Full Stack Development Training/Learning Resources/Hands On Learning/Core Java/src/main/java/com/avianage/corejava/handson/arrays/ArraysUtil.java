package com.avianage.corejava.handson.arrays;

import java.util.Arrays;

public class ArraysUtil {
    static void main(String[] args) {
        int[] scores = {45, 90, 23, 78, 56};

        // Sort — ascending order, modifies the original array
        Arrays.sort(scores);
        System.out.println(Arrays.toString(scores));   // [23, 45, 56, 78, 90]

        // Binary search — array must be sorted first
        int idx = Arrays.binarySearch(scores, 56);
        System.out.println("Found at index: " + idx);  // Found at index: 2

        // Fill — set all elements to a value
        int[] bonuses = new int[5];
        Arrays.fill(bonuses, 1000);
        System.out.println(Arrays.toString(bonuses));  // [1000, 1000, 1000, 1000, 1000]

        // Copy
        int[] copy = Arrays.copyOf(scores, scores.length);       // full copy
        int[] part = Arrays.copyOfRange(scores, 1, 4);           // elements at index 1, 2, 3
        System.out.println(Arrays.toString(part));               // [45, 56, 78]

        // Equals — compare contents
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        System.out.println(a == b);              // false — different objects
        System.out.println(Arrays.equals(a, b)); // true  — same contents

        // toString — readable representation (do not just print the array variable directly)
        System.out.println(scores);              // [I@1b6d3586  ← useless
        System.out.println(Arrays.toString(scores)); // [23, 45, 56, 78, 90]  ← useful


        // Sorting Arrays of Objects
        // Arrays.sort() works on primitive arrays automatically. For object arrays, the objects
        // must implement Comparable, or you provide a Comparator.
        // This is covered properly in Module 26 (Collections and Generics).

        String[] names = {"Ponu", "Sonu", "Monu", "Gonu", "Tonu"};
        Arrays.sort(names);   // lexicographic sort — works because String implements Comparable
        System.out.println(Arrays.toString(names));   // [Gonu, Monu, Ponu, Sonu, Tonu]
    }
}
