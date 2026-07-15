package com.avianage.corejava.day1.modifiers.package1;

public class SpecifierDemo {

    public static int num1 = 10;
    protected static int num2 = 20;
    /*default*/ static int num3 = 30;
    private static int num4 = 40;

    public static void main(String[] args) {

        System.out.println(SpecifierDemo.num1);
        System.out.println(SpecifierDemo.num2);
        System.out.println(SpecifierDemo.num3);
        System.out.println(SpecifierDemo.num4);

    }

}