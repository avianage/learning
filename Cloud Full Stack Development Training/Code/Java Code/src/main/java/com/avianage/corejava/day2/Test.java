package com.avianage.corejava.day2;

public class Test {
    static void main(String[] args) {
        Integer i = 200;
        Integer j = 200;

        System.out.println(i == j);

        Integer k = 10;
        Integer l = 10;
        System.out.println(k == l);
        System.out.println(k.equals(l));

    }
}
