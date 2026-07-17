package com.avianage.corejava.day2.oop.polymorphism;

public class Calc {

    public static void addNums(int i, long j) {
        System.out.println(i + j);
    }

    public static void addNums(long i, int j) {
        System.out.println(i + j);
    }

    public static void addNums(int i, int j) {
        System.out.println(i + j);
    }

    public static void addNums(int i, int j, int k) {
        System.out.println(i + j + k);
    }

    public static void addNums(int i, int j, int k, int l) {
        System.out.println(i + j + k + l);
    }
}