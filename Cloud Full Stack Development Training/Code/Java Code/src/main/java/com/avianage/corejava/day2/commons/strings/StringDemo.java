package com.avianage.corejava.day2.commons.strings;

public class StringDemo {
    static void main(String[] args) {
        String str = "abcdef";
        System.out.println(str);
        System.out.println(str.length());
        System.out.println(str.charAt(0));
        System.out.println(str.concat(str));
        System.out.println(String.valueOf(10 == 10));
    }
}
