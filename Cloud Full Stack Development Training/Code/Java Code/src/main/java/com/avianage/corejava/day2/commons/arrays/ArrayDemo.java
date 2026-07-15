package com.avianage.corejava.day2.commons.arrays;

import java.util.Arrays;

public class ArrayDemo {
    static void main(String[] args) {
        int[] arr = {25, 31, 17, 9, 22};
        System.out.println("Original Array");
        for (int a: arr){
            System.out.println(a);
        }
        System.out.println(arr.length);
        Arrays.sort(arr);
        System.out.println("Sorted Array");
        for (int a: arr){
            System.out.println(a);
        }
    }
}
